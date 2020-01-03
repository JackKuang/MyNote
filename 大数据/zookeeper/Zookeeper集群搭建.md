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

6. 快照策略

   * **autopurge.snapRetainCount，autopurge.purgeInterval**
   * 客户端在与zookeeper交互过程中会产生非常多的日志，而且zookeeper也会将内存中的数据作为snapshot保存下来，这些数据是不会被自动删除的，这样磁盘中这样的数据就会越来越多。不过可以通过这两个参数来设置，让zookeeper自动删除数据。
   * autopurge.purgeInterval就是设置多少小时清理一次。
   * 而autopurge.snapRetainCount是设置保留多少个snapshot，之前的则删除。
   * 不过如果你的集群是一个非常繁忙的集群，然后又碰上这个删除操作，可能会影响zookeeper集群的性能，所以一般会让这个过程在访问低谷的时候进行，但是遗憾的是zookeeper并没有设置在哪个时间点运行的设置，所以有的时候我们会禁用这个自动删除的功能，而在服务器上配置一个cron，然后在凌晨来干这件事。