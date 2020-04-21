# 一、CDH介绍

## 1.1 为什么使用CDH，CDH的优势在哪里？

- 首先我们看下如果自己搭建Hadoop生态圈，可能遇到什么问题：

  1. 运维麻烦，安装、升级节点费时费力，需要花很长的时间、人力成本。
  2. 监控麻烦，如果节点发生故障怎么办？如何快速响应，自己安装势必会一个个节点去排查，万一集群数量大压力就很大。
  3. 版本控制，使用了Hadoop某个版泵之后，Hive、Hbase等版本有依赖，过高或者过低都会存在风险点。
  4. 资源监控，万一某个节点磁盘满了，需要其他监控数据排查。
  5. 项目交接，如果集群管理交接，需要把管理任务分配给另外的人，配置这些东西交接会很复杂。
  6. ....

- 那么，有没有工具可以帮我们界面化管理、小白式操作呢？有就是CDH、DHP，下面简单介绍下两款工具

  - CDH：全称(Cloudera’s Distribution Including Apache Hadoop)。目前国内大部分公司使用，最流行的版泵。可yum安装，安装后可使用免费版和企业版（免费体验60天）（6.3.3之后没有免费版本）。
  - DHP：全称(Hortonworks Data Platform)。18年Hortonworks就已经和Cloudera合并了，现在访问其官网自动跳转到了Cloudera，所以显然CDH更加厉害。

  

- CDH优势：

  - 节点管理方便，不在式传统的连接主机，只要管理页面配置即可。

  - 实时监控，可实时监控服务状态、服务器性能等。

  - 灵活配置，配置属性也是直接界面操作。

  - 版本稳定，基于Apache Hadoop作出了改动，编写代码要依赖cdh仓库的版本。

    

# 二、CDH安装

### 2.1 配置yum源（全部）

- - rpm --import https://archive.cloudera.com/cm6/6.3.1/redhat7/yum/RPM-GPG-KEY-cloudera

### 2.2 安装jdk（全部）

- - yum install java-1.8.0-openjdk-devel

### 2.3 安装Cloudera Manager（主）

- - yum install cloudera-manager-daemon cloudera-manager-server

### 2.4 配置 Auto-TLS（全部）

- - sudo JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.232.b09-0.el7_7.x86_64/ /opt/cloudera/cm-agent/bin/certmanager setup --configure-services

###     2.5 配置mysql（主）

- - wget https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.46.tar.gz
  - tar zxvf mysql-connector-java-5.1.46.tar.gz
  - cd mysql-connector-java-5.1.46
  - sudo mkdir -p /usr/share/java/
  - sudo cp mysql-connector-java-5.1.46-bin.jar /usr/share/java/mysql-connector-java.jar

### 2.6 连接mysql（主）

- - /opt/cloudera/cm/schema/scm_prepare_database.sh mysql scm scm -h 190.1.1.124 -P 3307 -p scm

### 2.7 启动（主）

- - systemctl start cloudera-scm-server
  - tail -f /var/log/cloudera-scm-server/cloudera-scm-server.log

### 2.8 访问（全部）

- - ip:7180端口即可访问（admin/admin，cloudera/cloudera） 
  - 访问之后添加集群，先添加在主机上装agent节点

### 2.9 安装agent（主）

- - yum install cloudera-manager-agent cloudera-manager-daemon

### 3.0 完成

- - 按提示操作，大部分过程会直接下载安装完成即可。后面安装完成后，每一台节点安装agent，加入到集群即可。

# 三、问题

- 如果是正常情况下，安装会很快，但是事情并不完全是这样。

## 3.1 Yum源

- - 由于是国外的镜像资源，下载会比较慢，所以建议使用VPN的PC下载之后复制并安装
  - https://archive.cloudera.com/cm6/6.3.1/redhat7/yum/RPMS/x86_64/

## 3.2 Parcels下载

- - 这个包里面包含了Hadoop、Hive等包。
  - 同样是国外的镜像源，这个下载也会很慢，注意下载版本
  - https://archive.cloudera.com/cdh6/6.3.2/parcels/
  - 下载完成之后复制到CDH配置的Parcel 目录，默认/opt/cloudera/parcel-repo，下载完成之后，复制到其他服务器并配置。