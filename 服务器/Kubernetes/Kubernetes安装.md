# 系统初始化

## 一、设置主机名称以及Host文件的相互解析

```sh
hostnamectl set-hostname node01
```

## 二、安装依赖包

```sh
yum install -y conntrack nptdate ipvsadm jq iptables curl sysstat libseccomp wget vim net-tools git
```

## 三、设置防火墙为iptables并设置为空规则

```sh
systemctl stop firewalld && systemctl disable firewalld
yum -y install iptables-services  &&  systemctl  start iptables  &&  systemctl  enable iptables &&  iptables -F  &&  service iptables save
```

## 四、关闭swap与selinux

```sh
swapoff -a && sed -i '/ swap / s/^\(.*\)$/#\1/g' /etc/fstab
setenforce 0 && sed -i 's/^SELINUX=.*/SELINUX=disabled/' /etc/selinux/config
```

## 五、调整内核参数，对于K8S

```sh
cat > kubernetes.conf << EOF
net.bridge.bridge-nf-call-iptables=1
net.bridge.bridge-nf-call-ip6tables=1
net.ipv4.ip_forward=1 net.ipv4.tcp_tw_recycle=0
vm.swappiness=0 # 禁止使用 swap 空间，只有当系统 OOM 时才允许使用它
vm.overcommit_memory=1 # 不检查物理内存是否够用
vm.panic_on_oom=0 # 开启 OOM
fs.inotify.max_user_instances=8192
fs.inotify.max_user_watches=1048576
fs.file-max=52706963
fs.nr_open=52706963
net.ipv6.conf.all.disable_ipv6=1 net.netfilter.nf_conntrack_max=2310720
EOF
cp kubernetes.conf /etc/sysctl.d/kubernetes.conf
sysctl -p /etc/sysctl.d/kubernetes.conf
```

## 六、调整系统时区

```sh
# 设置系统时区为 中国/上海
# 将当前的 UTC 时间写入硬件时钟
# 重启依赖于系统时间的服务
timedatectl set-timezone Asia/Shanghai
timedatectl set-local-rtc 0
systemctl restart rsyslog
systemctl restart crond
```

## 七、关闭不需要的服务

```sh
systemctl stop postfix && systemctl disable postfix
```

## 八、是指rsyslogd和systemd joyrnald

```sh
mkdir /var/log/journal # 持久化保存日志的目录
mkdir /etc/systemd/journald.conf.d
cat > /etc/systemd/journald.conf.d/99-prophet.conf <<EOF
[Journal]
# 持久化保存到磁盘
Storage=persistent

# 压缩历史日志
Compress=yes

SyncIntervalSec=5m
RateLimitInterval=30s
RateLimitBurst=1000

# 最大占用空间 10G
SystemMaxUse=10G

# 单日志文件最大 200M
SystemMaxFileSize=200M

# 日志保存时间 2 周
MaxRetentionSec=2week

# 不将日志转发到 syslog
ForwardToSyslog=no
EOF
systemctl restart systemd-journald
```

## 九、升级内核

CentOS 7.x 系统自带的 3.10.x 内核存在一些 Bugs，导致运行的 Docker、Kubernetes 不稳定，例如： rpm -Uvh http://www.elrepo.org/elrepo-release-7.0-3.el7.elrepo.noarch.rpm

```sh
rpm -Uvh http://www.elrepo.org/elrepo-release-7.0-3.el7.elrepo.noarch.rpm
# 安装完成后检查 /boot/grub2/grub.cfg 中对应内核 menuentry 中是否包含 initrd16 配置，如果没有，再安装一次！
yum --enablerepo=elrepo-kernel install -y kernel-lt
# 设置开机从新内核启动
grub2-set-default 'CentOS Linux (4.4.189-1.el7.elrepo.x86_64) 7 (Core)'

```

* 用的内核版本高忽略本问题

# Kubeadm部署安装

## 一、Kube-proxy开启ipvs的前置条件

```sh
modprobe br_netfilter

cat > /etc/sysconfig/modules/ipvs.modules <<EOF
#!/bin/bash
modprobe -- ip_vs 
modprobe -- ip_vs_rr
modprobe -- ip_vs_wrr
modprobe -- ip_vs_sh
modprobe -- nf_conntrack_ipv4
EOF
chmod 755 /etc/sysconfig/modules/ipvs.modules && bash /etc/sysconfig/modules/ipvs.modules && lsmod | grep -e ip_vs -e nf_conntrack_ipv4
```

* 问题：

  ```sh
  [root@node01 ~]# chmod 755 /etc/sysconfig/modules/ipvs.modules && bash /etc/sysconfig/modules/ipvs.modules && lsmod | grep -e ip_vs -e nf_conntrack_ipv4
  modprobe: FATAL: Module nf_conntrack_ipv4 not found.
  
  ```

* 原因：

  使用的是内核4.19版本，这个版本起，nf_conntrack_ipv4已更新为nf_conntrack。修改nf_conntrack_ipv4为nf_conntrack

* 解决：

  ```sh
  modprobe br_netfilter
  
  cat > /etc/sysconfig/modules/ipvs.modules <<EOF
  #!/bin/bash
  modprobe -- ip_vs 
  modprobe -- ip_vs_rr
  modprobe -- ip_vs_wrr
  modprobe -- ip_vs_sh
  modprobe -- nf_conntrack
  EOF
  chmod 755 /etc/sysconfig/modules/ipvs.modules && bash /etc/sysconfig/modules/ipvs.modules && lsmod | grep -e ip_vs -e nf_conntrack
  ```

## 二、安装Docker

```sh
yum install -y yum-utils device-mapper-persistent-data lvm2

yum-config-manager \
--add-repo \
http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo

yum update -y
yum install -y docker-ce


mkdir -p /etc/docker
cat > /etc/docker/daemon.json <<-EOF
{
  "registry-mirrors": ["https://lycm6amm.mirror.aliyuncs.com"],
  "exec-opts":  ["native.cgroupdriver=systemd"],
  "log-driver":  "json-file",
  "log-opts":  {
    "max-size":  "100m"
  }
}
EOF
systemctl daemon-reload
systemctl restart docker
systemctl enable docker


```

## 三、安装 **Kubeadm** （主从配置）

```sh
cat > /etc/yum.repos.d/kubernetes.repo <<EOF 
[kubernetes]
name=Kubernetes
baseurl=http://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-x86_64
enabled=1
gpgcheck=0
repo_gpgcheck=0
gpgkey=http://mirrors.aliyun.com/kubernetes/yum/doc/yum-key.gpg http://mirrors.aliyun.com/kubernetes/yum/doc/rpm-package-key.gpg
EOF

yum -y install kubeadm kubectl kubelet
systemctl enable kubelet.service

```

## 四、初始化主节点

```sh
kubeadm config print init-defaults > kubeadm-config.yaml
vim kubeadm-config.yaml
# 修改地址
advertiseAddress: 172.16.134.1
kubernatesVersion: v1.18.0
# 增加
networking:
# flannel 默认网段
  podSubnet: 10.244.0.0/16
#添加
---
apiVersion: kubeproxy.config.k8s.io/v1alpha1
kind: KubeProxyConfiguration
featureGates:
  SupportIPVSProxyMode: true
mode: ipvs

kubeadm init --config=kubeadm-config.yaml --experimental-upload-certs | tee kubeadm-init.log
```

* 问题：

  ```sh
  [root@node01 ~]# kubeadm init --config=kubeadm-config.yaml --experimental-upload-certs | tee kubeadm-init.log
  unknown flag: --experimental-upload-certs
  To see the stack trace of this error execute with --v=5 or higher
  ```

* 原因：

  执行kubeadm ini时，在v1.16及以上版本会报错：unknown flag: `--experimental-upload-certs`，新版本不再支持此参数了，变更为：`--upload-certs`即可

* 解决：

  ```sh
  kubeadm init --config=kubeadm-config.yaml --upload-certs | tee kubeadm-init.log
  ```



继续执行：

* 问题：

  	[WARNING Hostname]: hostname "node01" could not be reached
    	[WARNING Hostname]: hostname "node01": lookup node01 on 100.100.2.138:53: no such host

* 原因：

  node01，ip解析没配置；

* 解决：

  修改/etc/hosts



继续执行：

* 问题：

  ```
  [preflight] Running pre-flight checks
  error execution phase preflight: [preflight] Some fatal errors occurred:
  	[ERROR NumCPU]: the number of available CPUs 1 is less than the required 2
  [preflight] If you know what you are doing, you can make a check non-fatal with `--ignore-preflight-errors=...`
  ```

* 原因：

  k8s要求2核以上，云服务器只有1核。

* 解决：

  在不提升配置的情况下，忽略错误

  ```sh
  kubeadm init --config=kubeadm-config.yaml --upload-certs --ignore-preflight-errors=NumCPU | tee kubeadm-init.log
  ```



继续执行

* 问题：

  * 发现镜像拉取速度特别慢

* 原因：

  * 网络

* 解决：

  * 修改配置文件：

    ```
    imageRepository: registry.aliyuncs.com/google_containers
    ```

  * 增加参数

    ```
    kubeadm init --config=kubeadm-config.yaml --upload-certs --image-repository=registry.aliyuncs.com/google_containers --ignore-preflight-errors=NumCPU | tee kubeadm-init.log
    ```



* 执行成功：

  * 日志中的内容

  ```sh
  [preflight] Running pre-flight checks
  # 拉镜像
  [preflight] Pulling images required for setting up a Kubernetes cluster
  [preflight] This might take a minute or two, depending on the speed of your internet connection
  [preflight] You can also perform this action in beforehand using 'kubeadm config images pull'
  # 配置文件
  [kubelet-start] Writing kubelet environment file with flags to file "/var/lib/kubelet/kubeadm-flags.env"
  [kubelet-start] Writing kubelet configuration to file "/var/lib/kubelet/config.yaml"
  [kubelet-start] Starting the kubelet
  # https证书
  [certs] Using certificateDir folder "/etc/kubernetes/pki"
  [certs] Generating "ca" certificate and key
  [certs] Generating "apiserver" certificate and key
  ....
  
  
  [addons] Applied essential addon: CoreDNS
  [addons] Applied essential addon: kube-proxy
  
  Your Kubernetes control-plane has initialized successfully!
  
  # 启动成功
  
  To start using your cluster, you need to run the following as a regular user:
  
    mkdir -p $HOME/.kube
    # 拷贝集群的管理员权限
    sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
    sudo chown $(id -u):$(id -g) $HOME/.kube/config
  
  You should now deploy a pod network to the cluster.
  Run "kubectl apply -f [podnetwork].yaml" with one of the options listed at:
    https://kubernetes.io/docs/concepts/cluster-administration/addons/
  
  Then you can join any number of worker nodes by running the following on each as root:
  
  kubeadm join 172.16.134.1:6443 --token abcdef.0123456789abcdef \
      --discovery-token-ca-cert-hash sha256:74c8bd14a97d1a2d14381e0d56576ea71599da10d975949455396e14e125c159 
  
  ```

## 五、启动准备

```sh
# 拷贝集群的管理员权限
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

```sh
[root@node01 ~]# kubectl get node
NAME     STATUS     ROLES    AGE     VERSION
node01   NotReady   master   7m12s   v1.18.2
```

## 六、部署网络

```sh
wget https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml

kubectl create -f kube-flannel.yml
```

```sh
[root@node01 flannel]# kubectl get pod -n kube-system
NAME                             READY   STATUS     RESTARTS   AGE
coredns-7ff77c879f-dzwfd         0/1     Pending    0          19m
coredns-7ff77c879f-lfjfz         0/1     Pending    0          19m
etcd-node01                      1/1     Running    0          19m
kube-apiserver-node01            1/1     Running    0          19m
kube-controller-manager-node01   1/1     Running    0          19m
kube-flannel-ds-amd64-w87zn      0/1     Init:0/1   0          36s
kube-proxy-nbjmd                 1/1     Running    0          19m
kube-scheduler-node01            1/1     Running    0          19m
[root@node01 flannel]# kubectl get node
NAME     STATUS     ROLES    AGE   VERSION
node01   NotReady   master   19m   v1.18.2
```

等待系统初始化，中间一段时间系统会卡顿（系统资源不够）

```sh
[root@node01 ~]# kubectl get pod -n kube-system
NAME                             READY   STATUS    RESTARTS   AGE
coredns-7ff77c879f-dzwfd         1/1     Running   0          23m
coredns-7ff77c879f-lfjfz         1/1     Running   0          23m
etcd-node01                      1/1     Running   0          23m
kube-apiserver-node01            1/1     Running   0          23m
kube-controller-manager-node01   1/1     Running   1          23m
kube-flannel-ds-amd64-w87zn      1/1     Running   0          4m48s
kube-proxy-nbjmd                 1/1     Running   0          23m
kube-scheduler-node01            1/1     Running   1          23m
[root@node01 ~]# kubectl get node
NAME     STATUS   ROLES    AGE   VERSION
node01   Ready    master   23m   v1.18.2
```

## 七、其他节点加入

执行日志中的加入命令

```sh
kubeadm join 172.16.134.1:6443 --token abcdef.0123456789abcdef \
    --discovery-token-ca-cert-hash sha256:74c8bd14a97d1a2d14381e0d56576ea71599da10d975949455396e14e125c159 
```

如果命令找不到，主节点执行

```
kubeadm token create --print-join-command
```

提示加入成功

```
This node has joined the cluster:
* Certificate signing request was sent to apiserver and a response was received.
* The Kubelet was informed of the new secure connection details.
```

Master查看k8s服务状态


```
[root@Jack etc]# kubectl get pod -n kube-system
NAME                             READY   STATUS              RESTARTS   AGE
coredns-7ff77c879f-dzwfd         1/1     Running             0          11h
coredns-7ff77c879f-lfjfz         1/1     Running             0          11h
etcd-node01                      1/1     Running             0          11h
kube-apiserver-node01            1/1     Running             0          11h
kube-controller-manager-node01   1/1     Running             2          11h
kube-flannel-ds-amd64-jc76t      0/1     Pending             0          32s
kube-flannel-ds-amd64-w87zn      1/1     Running             0          11h
kube-proxy-lntlr                 0/1     ContainerCreating   0          32s
kube-proxy-nbjmd                 1/1     Running             0          11h
kube-scheduler-node01            1/1     Running             2          11h
[root@Jack etc]# kubectl get node
NAME     STATUS     ROLES    AGE   VERSION
node01   Ready      master   11h   v1.18.2
node02   NotReady   <none>   63s   v1.18.2

```

```
kubectl get pod -n kube-system [-w]
kubectl get node [-w]

-w作为持续观测
```

期间：新的节点会pull docker，构建docker容器

最终，空节点加入成功

```
[root@Jack install-k8s]# kubectl get pod -n kube-system
NAME                             READY   STATUS    RESTARTS   AGE
coredns-7ff77c879f-dzwfd         1/1     Running   0          11h
coredns-7ff77c879f-lfjfz         1/1     Running   0          11h
etcd-node01                      1/1     Running   0          11h
kube-apiserver-node01            1/1     Running   0          11h
kube-controller-manager-node01   1/1     Running   5          11h
kube-flannel-ds-amd64-jc76t      1/1     Running   0          25m
kube-flannel-ds-amd64-w87zn      1/1     Running   0          11h
kube-proxy-lntlr                 1/1     Running   0          25m
kube-proxy-nbjmd                 1/1     Running   0          11h
kube-scheduler-node01            1/1     Running   5          11h
[root@Jack install-k8s]# kubectl get node
NAME     STATUS   ROLES    AGE   VERSION
node01   Ready    master   11h   v1.18.2
node02   Ready    <none>   25m   v1.18.2
```

至此，Docker集群构建成功。
