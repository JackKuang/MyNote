# NFS

* 共享文件夹
* 优势：
  * 节省本地存储空间，将常用的数据存放在一台NFS服务器上且可以通过网络访问，那么本地终端将可以减少自身存储空间的使用。
  * 用户不需要在网络中的每个机器上都建有Home目录，Home目录可以放在NFS服务器上且可以在网络上被访问使用。
  * 一些存储设备CDROM和Zip（一种高储存密度的磁盘驱动器与磁盘）等都可以在网络上被别的机器使用。这可以减少整个网络上可移动介质设备的数量。

## 安装

### 1. 服务器端安装相应的软件包

```sh
yum -y install rpcbind nfs-utils
```

### 2. 服务器端关闭SElinux和防火墙

```sh
setenforce 0
systemctl stop firewalld
```

### 3. 创建共享目录（也可以不创建，直接共享现存的目录），并且赋予权限

```sh
mkdir /share
chmod 777 /share 
```

### 4. 修改共享配置文件 /etc/exports ,然后重新加载exports文件：exportfs -a

```sh
# vim /etc/exports
/share 172.16.134.1/24(rw,sync,no_root_squash)   
/1                          #代表共享出来的目录
192.168.43.0/24   #允许192.168.43.0/24的网络访问此共享。
rw                         #表示权限 读写
sync                      #表示同步写入
no_root_squash     #表示客户机以root访问时赋予本地root权限
```

```sh
exportfs -a
```

### 5. 服务端启动服务

```sh
systemctl start rpcbind
systemctl start nfs
systemctl enable rpcbind
systemctl enable nfs
```

### 6. 客户端关闭SElinux和防火墙：

```sh
setenforce 0
service iptables stop
```

### 7. 客户端安装rpcbind

```sh
yum -y install nfs-utils
yum -y install rpcbind
systemctl start rpcbind
systemctl enable rpcbind
systemctl start nfs
systemctl enable nfs
```

### 8. 创建挂载点，查看，并且挂载

```sh
mkdir /data
showmount -e 172.16.134.1
mount -t nfs 172.16.134.1:/share /data

# 开机启动
vim /etc/fstab
# 添加磁盘挂载
172.16.134.1:/share     /data nfs defaults      0       0
# 立即生效
mount -a 
```

### 9. 测试文件

```sh
date > now
cat now
```

## 异常

### 1. rpcbind无法启动

```
    [root@node02 html]# systemctl start rpcbind
    A dependency job for rpcbind.service failed. See 'journalctl -xe' for details.

```

* 修改 /etc/sysctl.conf 文件

```shell
# 添加如下两行
net.ipv6.conf.all.disable_ipv6 = 1
net.ipv6.conf.default.disable_ipv6 = 1
```

* 修改 rpcbind.socket 文件
  * 首先查找 rpcbind.socket 文件路径

```shell
find /etc/ -name '*rpcbind.socket*'
vim /etc/systemd/system/sockets.target.wants/rpcbind.socket
```

* 编辑 /etc/systemd/system/sockets.target.wants/rpcbind.socket 文件，将其中 ListenStream 关于IPv6的部分注释掉，如下：

```shell
[Unit]
Description=RPCbind Server Activation Socket

[Socket]
ListenStream=/var/run/rpcbind.sock

# RPC netconfig can't handle ipv6/ipv4 dual sockets
BindIPv6Only=ipv6-only
ListenStream=0.0.0.0:111
ListenDatagram=0.0.0.0:111
#ListenStream=[::]:111
#ListenDatagram=[::]:111

[Install]
WantedBy=sockets.target
```

* 重新启动 nfs 服务

```shell
systemctl daemon-reload
systemctl restart nfs
```