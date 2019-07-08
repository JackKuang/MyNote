# Hadoop在Docker下环境变量配置

## 机器规划

3台主机，1台master、2台slaver/worker

mater:node3,ip:172.17.0.3

work:node4,ip:172.17.0.4
work:node5,ip:172.17.0.5


## 机器部署

1. 创建SSH容器
    ```bash
    docker build -t "centos7-ssh" .
    ```
    ```dockerfile
    FROM centos
    MAINTAINER Jack

    RUN yum install -y openssh-server sudo
    RUN sed -i 's/UsePAM yes/UsePAM no/g' /etc/ssh/sshd_config
    RUN yum install -y openssh-clients
    RUN yum install -y vim

    RUN echo "root:123456" | chpasswd
    RUN echo "root   ALL=(ALL)       ALL" >> /etc/sudoers
    RUN ssh-keygen -t dsa -f /etc/ssh/ssh_host_dsa_key
    RUN ssh-keygen -t rsa -f /etc/ssh/ssh_host_rsa_key

    # 授权本机访问可以访问，复制品之间可以项目访问
    RUN ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
    RUN cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
    RUN chmod 0600 ~/.ssh/authorized_keys

    RUN mkdir /var/run/sshd
    EXPOSE 22
    CMD ["/usr/sbin/sshd", "-D"]
    ```
    基于centos，并同时安装SSH、vim等工具

2. 创建JDK容器
    ```bash
    docker build -t "jdk8" .
    ```
    ```dockerfile
    FROM centos7-ssh
    MAINTAINER Jack

    ADD jdk-8u131-linux-x64.tar.gz /usr/local/

    RUN mv /usr/local/jdk1.8.0_131 /usr/local/jdk1.8

    ENV JAVA_HOME /usr/local/jdk1.8
    ENV CLASSPATH .:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
    ENV PATH $JAVA_HOME/bin:$JAVA_HOME/jre/bin:$PATH
    ```
    同目录下放jdk8安装包
    基于上一步创建的centos7-ssh，增加jdk8环境，并配置环境变量。

3. 创建Hadoop容器
    ```bash
    docker build -t "hadoop" .
    ```
    ```dockerfile
    FROM jdk8
    MAINTAINER Jack

    ADD hadoop-3.1.2.tar.gz /usr/local
    MV /usr/local/hadoop-3.1.2 /usr/local/hadoop

    ENV HADOOP_HOME /usr/local/hadoop
    ENV PATH $HADOOP_HOME/bin:$PATH
    ```
    同目录下放hadoop安装包
    基于上一步创建的jdk8，增加hadoop环境，并初始化hdfs

4. 修改Hadoop配置文件
    
    解压hadoop，修改以下配置文件

5. 集群搭建 

    ```bash
    docker build -t "hadoop2" .
    ```
    ```dockerfile
    FROM hadoop
    MAINTAINER Jack

    # 复制已经修改的配置文件
    COPY sbin/ /usr/local/hadoop/sbin
    COPY etc/hadoop/ /usr/local/hadoop/etc/hadoop

    # 增加执行权限，保证复制的配置文件可以执行
    RUN chmod +x /usr/local/hadoop/sbin/*.sh
    ```
