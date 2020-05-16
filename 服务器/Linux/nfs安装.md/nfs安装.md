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
/1 192.168.43.0/24 (rw,sync,no_root_squash)   
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
```

### 8. 创建挂载点，查看，并且挂载

```
mkdir /data
showmount -e 172.16.134.1
mount -t nfs 172.16.134.1:/share /data
```

### 9. 测试文件

```sh
date > now
cat now
```

