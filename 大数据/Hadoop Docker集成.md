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
    docker build -t "hadoop-3.1.2" .
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
   
    解压hadoop
    
    修改以下`etc/hadoop`目录下的一下配置文件，示例配置文件在本文件`Dockerfile/hadoop3.1.2/etc/hadoop`路径下

    * `core-site.xml`
    ```xml
    <configuration>
        <property>
            <name>fs.defaultFS</name>
            <value>hdfs://node1:9000</value>
        </property>
        <property>
            <name>io.file.buffer.size</name>
            <value>131072</value>
        </property>
        <property>
            <name>hadoop.tmp.dir</name>
            <value>/home/hadoop/tmp</value>
            <description>Abase for other temporary directories.</description>
        </property>
    </configuration>
    ```
    
    * `hadoop-env.sh`
    ```
    增加JAVA_HOME环境变量
    ```

    * `hdfs-site.xml`
    ```xml
    <configuration>
        <property>     
            <name>dfs.blocksize</name>  
            <value>268435456</value> 
        </property> 
        <property>     
            <name>dfs.namenode.handler.count</name>  
            <value>100</value> 
        </property> 
        <property>
            <name>dfs.namenode.name.dir</name>
            <value>/home/hadoop/dfs/name</value>
        </property>
        <property>
            <name>dfs.datanode.data.dir</name>
            <value>/home/hadoop/dfs/data</value>
        </property>
        <property>
            <name>dfs.replication</name>
            <value>3</value>
        </property>
    </configuration>
    ```
    
    * `mapred-site.xml`
    ```xml
    <configuration>
        <property>
            <name>mapreduce.framework.name</name>
            <value>yarn</value>
        </property>
        <property>   
            <name>yarn.app.mapreduce.am.env</name>    
            <value>HADOOP_MAPRED_HOME=${HADOOP_HOME}</value>  
        </property>
        <property>   
            <name>mapreduce.map.env</name>    
            <value>HADOOP_MAPRED_HOME=${HADOOP_HOME}</value>  
        </property>
        <property>   
            <name>mapreduce.reduce.env</name>    
            <value>HADOOP_MAPRED_HOME=${HADOOP_HOME}</value>  
        </property>
        <property>   
            <name>mapreduce.application.classpath</name>
            <value>$HADOOP_MAPRED_HOME/share/hadoop/mapreduce/*,$HADOOP_MAPRED_HOME/share/hadoop/mapreduce/lib/*</value>  
        </property>
    </configuration>
    ```

    * `workers`
    ```bash
        # namenode节点
        node2
        node3
    ```

    * `yarn-site.xml`
    ```xml
    <configuration>
        <!-- Site specific YARN configuration properties -->
        <property>   
            <name>yarn.resourcemanager.hostname</name>
            <value>node1</value> 
        </property>
        <property>
            <name>yarn.nodemanager.aux-services</name>
            <value>mapreduce_shuffle</value>
        </property>
        <property>
            <name>yarn.resourcemanager.address</name>
            <value>node1:8032</value>
        </property>
        <property>
            <name>yarn.resourcemanager.scheduler.address</name>
            <value>node1:8030</value>
        </property>
        <property>
            <name>yarn.resourcemanager.resource-tracker.address</name>
            <value>node1:8031</value>
        </property>
        <property>
            <name>yarn.resourcemanager.admin.address</name>
            <value>node1:8033</value>
        </property>
        <property>
            <name>yarn.resourcemanager.webapp.address</name>
            <value>node1:8088</value>
        </property>
        <property>
            <name>yarn.nodemanager.pmem-check-enabled</name>
            <value>false</value>
        </property>
        <property>
            <name>yarn.nodemanager.vmem-check-enabled</name>
            <value>false</value>
        </property>
    </configuration>
    ```

    修改以下`etc/hadoop`目录下的一下配置文件，示例配置文件在本文件`Dockerfile/hadoop3.1.2/sbin`路径下。
    
    * `start-dfs.sh`、`stop-dfs.sh`

    ```bash
    HDFS_DATANODE_USER=root
    #HADOOP_SECURE_DN_USER=hdfs
    HDFS_NAMENODE_USER=root
    HDFS_SECONDARYNAMENODE_USER=root
    HDFS_DATANODE_SECURE_USER=hdfs
    ```

    * `start-yarn.sh`、`stop-yarn.sh`
    ```bash
    YARN_RESOURCEMANAGER_USER=root
    HADOOP_SECURE_DN_USER=yarn
    YARN_NODEMANAGER_USER=root
    ```

5. 集群搭建 

    ```bash
    docker build -t "hadoop-3.1.2-workers" .
    ```
    ```dockerfile
    FROM hadoop-3.1.2
    MAINTAINER Jack

    # 复制已经修改的配置文件
    COPY sbin/ /usr/local/hadoop/sbin
    COPY etc/hadoop/ /usr/local/hadoop/etc/hadoop

    # 增加执行权限，保证复制的配置文件可以执行
    RUN chmod +x /usr/local/hadoop/sbin/*.sh
    ```

6. 创建Docker容器

    ```bash
    # node1
    docker run --name node1 -h node1 --add-host node1:172.17.0.3 --add-host node2:172.17.0.4 --add-host node3:172.17.0.5 -d -v /etc/localtime:/etc/localtime:ro -p 2201:22 hadoop-3.1.2-workers
    # node2
    docker run --name node2 -h node2 --add-host node1:172.17.0.3 --add-host node2:172.17.0.4 --add-host node3:172.17.0.5 -d -v /etc/localtime:/etc/localtime:ro -p 2202:22 hadoop-3.1.2-workers
    # node3
    docker run --name node3 -h node3 --add-host node1:172.17.0.3 --add-host node2:172.17.0.4 --add-host node3:172.17.0.5 -d -v /etc/localtime:/etc/localtime:ro -p 2203:22 hadoop-3.1.2-workers
    ```
    注意事项：  
    1. `-v /etc/localtime:/etc/localtime:ro`用于时间同步
    2. 实际个使用端口仅仅用于Docker容器内部网络，与其他机器交互需要配置网桥。
    3. 需要根据实际接口控制各节点的端口映射（docker）

7. 启动Docker集群
   
    `hdfs namenode -format`格式化
    
    进入`node1`节点，进入`$HADOOP_HOME/sbin`，启动`start-all.sh`，注意官方不建议执行`start-all.sh`。
    进入`node2`、`node3`节点，调用jps命令，查看java进程