# Zookeeper集群搭建

1. 下载、解压

2. 配置环境变量

   ```bash
   ZOOKEEPER_HOME=/opt/zookeeper-3.4.14
   PATH=$PATH:$ZOOKEEPER_HOME/bin
   ```

 3. 修改`zoo_sample.cfg`文件名为`zoo.cfg`

 4. 修改`zoo.cfg`的配置

    ```bash
    # 修改
    dataDir=/tmp/zookeeper
    # 增加
    server.1=node1:2888:3888
    server.2=node2:2888:3888
    server.3=node3:2888:3888
    
    # observer模式
    peerType=observer
    server.1:localhost:2181:3181:observer  
    ```

	5.  在`dataDir(/tmp/zookeeper)`路径下，创建myid。不同节点上内容不同，依次为1,2,3

	6.  依次启动节点。

     ```bash
     zkServer.sh start
     ```

7. 查看状态

   ```bash
   zkServer.sh status
   # leader为主节点，follower为从节点
   ```

   