# Flume日志采集框架

## 一、Flume框架介绍

![](img/flume.png)

```
在一个完整的离线大数据处理系统中，除了hdfs+mapreduce+hive组成分析系统的核心之外，还需要数据采集、结果数据导出、任务调度等不可或缺的辅助系统，而这些辅助工具在hadoop生态体系中都有便捷的开源框架。
```

- Flume是Cloudera提供的一个高可用的，高可靠的，分布式的**海量日志采集、聚合和传输的系统**；
- Flume支持在日志系统中定制各类数据发送方，用于收集数据；
- Flume提供对数据进行简单处理，并写到各种数据接受方（可定制）的能力。

## 二、Flume的架构

![](img/flume_flow.png)

* Flume 的核心是把数据从数据源收集过来，再送到目的地。为了保证输送一定成功，在送到目的地之前，会先缓存数据，待数据真正到达目的地后，删除自己缓存的数据。
* Flume分布式系统中==最核心的角色是agent==，flume采集系统就是由一个个agent所连接起来形成。
* ==每一个agent相当于一个数据传递员，内部有三个组件==
  - **source**
    - 采集组件，用于跟数据源对接，以获取数据
  - **channel**
    - 传输通道组件，缓存数据，用于从source将数据传递到sink
  - **sink**
    - 下沉组件，数据发送给最终存储系统或者下一级agent中

## 三、Flume采集系统结构图

### 1. 简单结构

* 单个agent采集数据

![](img/flume_flow.png)

### 2. 复杂结构

* 2个agent串联

  ![](img/UserGuide_image03.png)

* 多个agent串联

  ![](img/UserGuide_image02.png)

* 多个channel

  ![](img/UserGuide_image04.png)

## 四、Flume安装部署

1. 下载、解压

2. 修改配置文件

   ```bash
   # 复制配置文件
   mv flume-env.sh.template flume-env.sh
   # 添加java环境变量
   vim flume-env.sh
   export JAVA_HOME=/opt/bigdata/jdk
   ```

3. 设置环境变量

   ```
   # 配置flume的环境变量
   export FLUME_HOME=/opt/apache-flume-1.8.0-bin
   export PATH=$PATH:$FLUME_HOME/bin
   ```

## 五、Flume实战

### 5.1 采集目录到HDFS

* 一个目录中不断有新的文件产生，需要把目录中的文件不断地进行数据收集保存到HDFS上

  ![](img/Dir-Flume-HDFS.png)

* 配置文件dir2Hdfs.conf

  ```
  # Name the components on this agent
  a1.sources = r1
  a1.sinks = k1
  a1.channels = c1
  
  # 配置source
  ##注意：不能往监控目中重复丢同名文件
  a1.sources.r1.type = spooldir
  a1.sources.r1.spoolDir = /opt/bigdata/flumeData
  a1.sources.r1.fileHeader = true
  a1.sources.r1.channels = c1
  
  #配置channel
  a1.channels.c1.type = memory
  a1.channels.c1.capacity = 1000
  a1.channels.c1.transactionCapacity = 100
  
  #配置sink
  a1.sinks.k1.type = hdfs
  a1.sinks.k1.channel = c1
  a1.sinks.k1.hdfs.path = hdfs://node1:9000/spooldir/files/%y-%m-%d/%H%M/
  a1.sinks.k1.hdfs.filePrefix = events-
  a1.sinks.k1.hdfs.round = true
  a1.sinks.k1.hdfs.roundValue = 10
  a1.sinks.k1.hdfs.roundUnit = minute
  a1.sinks.k1.hdfs.rollInterval = 60
  a1.sinks.k1.hdfs.rollSize = 50
  a1.sinks.k1.hdfs.rollCount = 10
  a1.sinks.k1.hdfs.batchSize = 100
  a1.sinks.k1.hdfs.useLocalTimeStamp = true
  #生成的文件类型，默认是Sequencefile，可用DataStream，则为普通文本
  a1.sinks.k1.hdfs.fileType = DataStream
  ```

* 启动

  ```
  flume-ng agent -n a1 -c /opt/flume/myconf -f /opt/flume/myconf/dir2Hdfs.conf -Dflume.root.logger=info,console
  ```

### 5.2 采集文件到HDFS

* 监控一个文件如果有新增的内容就把数据采集到HDFS上

  ![](img/file-Flume-HDFS.png)

* 配置文件file2Hdfs.conf

  ```
  # Name the components on this agent
  a1.sources = r1
  a1.sinks = k1
  a1.channels = c1
  
  #配置source
  a1.sources.r1.type = exec
  a1.sources.r1.command = tail -F /opt/bigdata/flumeData/tail.log
  a1.sources.r1.channels = c1
  
  #配置channel
  a1.channels.c1.type
  #指定channel类型
  a1.channels.c1.type = file
  #检查点文件目录
  a1.channels.c1.checkpointDir=/opt/flume_checkpoint
  #缓存数据文件夹
  a1.channels.c1.dataDirs=/opt/flume_data
  
  
  #配置sink
  a1.sinks.k1.channel = c1
  a1.sinks.k1.type = hdfs
  a1.sinks.k1.hdfs.path = hdfs://node1:9000/tailFile/files/%y-%m-%d/%H%M/
  a1.sinks.k1.hdfs.filePrefix = events-
  a1.sinks.k1.hdfs.round = true
  a1.sinks.k1.hdfs.roundValue = 10
  a1.sinks.k1.hdfs.roundUnit = minute
  a1.sinks.k1.hdfs.rollInterval = 60
  a1.sinks.k1.hdfs.rollSize = 50
  a1.sinks.k1.hdfs.rollCount = 10
  a1.sinks.k1.hdfs.batchSize = 100
  a1.sinks.k1.hdfs.useLocalTimeStamp = true
  #生成的文件类型，默认是Sequencefile，可用DataStream，则为普通文本
  a1.sinks.k1.hdfs.fileType = DataStream
  ```

* 启动

  ```
  flume-ng agent -n a1 -c /opt/flume/myconf -f /opt/flume/myconf/file2Hdfs.conf -Dflume.root.logger=info,console
  ```

### 5.3 采集文件到控制台

* 监控一个文件如果有新增的内容就把数据采集之后打印控制台，通常用于测试/调试目的

* 配置文件tail-memory-logger.conf

  ```
  # Name the components on this agent
  a1.sources = r1
  a1.sinks = k1
  a1.channels = c1
  
  #配置source
  a1.sources.r1.type = exec
  a1.sources.r1.command = tail -F /opt/bigdata/flumeData/tail.log
  a1.sources.r1.channels = c1
  
  #配置channel
  a1.channels.c1.type = memory
  a1.channels.c1.capacity = 1000
  a1.channels.c1.transactionCapacity = 100
  
  #配置sink
  a1.sinks.k1.channel = c1
  #类型是日志格式
  a1.sinks.k1.type = logger
  ```

* 启动

  ```
  flume-ng agent -n a1 -c /opt/flume/myconf -f /opt/flume/myconf/tail-memory-logger.conf -Dflume.root.logger=info,console
  ```

## 六、高可用配置案例

#### 6.1 failover故障转移

![](img/flume-failover.png)

- 节点分配

|    名称    | 服务器主机名 |     ip地址      |    角色    |
| :--------: | :----------: | :-------------: | :--------: |
|   Agent1   |    node1     | 192.168.200.200 | WebServer  |
| Collector1 |    node2     | 192.168.200.210 | AgentMstr1 |
| Collector2 |    node3     | 192.168.200.220 | AgentMstr2 |

```
Agent1数据分别流入到Collector1和Collector2，Flume NG本身提供了Failover机制，可以自动切换和恢复。
```

* Agent1配置文件flume-client-failover.conf	

  ```bash
  #agent name
  a1.channels = c1
  a1.sources = r1
  a1.sinks = k1 k2
  
  #set gruop
  a1.sinkgroups = g1
  
  #set sink group
  a1.sinkgroups.g1.sinks = k1 k2
  
  #set source
  a1.sources.r1.channels = c1
  a1.sources.r1.type = exec
  a1.sources.r1.command = tail -F /opt/bigdata/flumeData/tail.log
  
  #set channel
  a1.channels.c1.type = memory
  a1.channels.c1.capacity = 1000
  a1.channels.c1.transactionCapacity = 100
  
  # set sink1
  a1.sinks.k1.channel = c1
  a1.sinks.k1.type = avro
  a1.sinks.k1.hostname = node2
  a1.sinks.k1.port = 52020
  
  # set sink2
  a1.sinks.k2.channel = c1
  a1.sinks.k2.type = avro
  a1.sinks.k2.hostname = node3
  a1.sinks.k2.port = 52020
  
  #set failover 故障转移
  a1.sinkgroups.g1.processor.type = failover
  # 优先权重
  a1.sinkgroups.g1.processor.priority.k1 = 10
  a1.sinkgroups.g1.processor.priority.k2 = 5
  a1.sinkgroups.g1.processor.maxpenalty = 10000
  
  #这里首先要申明一个sinkgroups,然后再设置2个sink ,k1与k2,其中2个优先级是10和5。
  #而processor的maxpenalty被设置为10秒，默认是30秒.表示故障转移的最大时间
  ```

* Collector1节点 flume-server-failover.conf

  ```
  #set Agent name
  a1.sources = r1
  a1.channels = c1
  a1.sinks = k1
  
  #set channel
  a1.channels.c1.type = memory
  a1.channels.c1.capacity = 1000
  a1.channels.c1.transactionCapacity = 100
  
  # set source
  a1.sources.r1.type = avro
  a1.sources.r1.bind = 0.0.0.0
  a1.sources.r1.port = 52020
  a1.sources.r1.channels = c1
  
  #配置拦截器
  a1.sources.r1.interceptors = i1 i2
  a1.sources.r1.interceptors.i1.type = timestamp
  a1.sources.r1.interceptors.i2.type = host
  a1.sources.r1.interceptors.i2.hostHeader=hostname
  
  #set sink to hdfs
  a1.sinks.k1.channel = c1
  a1.sinks.k1.type=hdfs
  a1.sinks.k1.hdfs.path=hdfs://node1:9000/failover/logs/%{hostname}
  a1.sinks.k1.hdfs.filePrefix=%Y-%m-%d
  a1.sinks.k1.hdfs.round = true
  a1.sinks.k1.hdfs.roundValue = 10
  a1.sinks.k1.hdfs.roundUnit = minute
  a1.sinks.k1.hdfs.rollInterval = 60
  a1.sinks.k1.hdfs.rollSize = 50
  a1.sinks.k1.hdfs.rollCount = 10
  a1.sinks.k1.hdfs.batchSize = 100
  a1.sinks.k1.hdfs.fileType = DataStream
  ```

* 启动

  ```
  # node2和node3上启动
  flume-ng agent -n a1 -c /opt/flume/myconf -f /opt/flume/myconf/flume-server-failover.conf -Dflume.root.logger=info,console
  # node1启动
  flume-ng agent -n a1 -c /opt/flume/myconf -f /opt/flume/myconf/ flume-client-failover.conf -Dflume.root.logger=info,console
  ```

#### 6.2 load balance负载均衡

- 实现多个flume采集数据的时候避免单个flume的负载比较高，实现多个flume采集器负载均衡。

- Agent1节点   flume-client-loadbalance.conf

  ```bash
  #agent name
  a1.channels = c1
  a1.sources = r1
  a1.sinks = k1 k2
  
  #set gruop
  a1.sinkgroups = g1
  
  #set sink group
  a1.sinkgroups.g1.sinks = k1 k2
  
  #set source
  a1.sources.r1.channels = c1
  a1.sources.r1.type = exec
  a1.sources.r1.command = tail -F /opt/bigdata/flumeData/tail.log
  
  #set channel
  a1.channels.c1.type = memory
  a1.channels.c1.capacity = 1000
  a1.channels.c1.transactionCapacity = 100
  
  
  # set sink1
  a1.sinks.k1.channel = c1
  a1.sinks.k1.type = avro
  a1.sinks.k1.hostname = node2
  a1.sinks.k1.port = 52020
  
  # set sink2
  a1.sinks.k2.channel = c1
  a1.sinks.k2.type = avro
  a1.sinks.k2.hostname = node3
  a1.sinks.k2.port = 52020
  
  #set load-balance
  a1.sinkgroups.g1.processor.type =load_balance
  # 默认是round_robin，还可以选择random
  a1.sinkgroups.g1.processor.selector = round_robin
  #如果backoff被开启，则sink processor会屏蔽故障的sink
  a1.sinkgroups.g1.processor.backoff = true
  ```

- Collector flume-server-loadbalance.conf

  ```
  #set Agent name
  a1.sources = r1
  a1.channels = c1
  a1.sinks = k1
  
  #set channel
  a1.channels.c1.type = memory
  a1.channels.c1.capacity = 1000
  a1.channels.c1.transactionCapacity = 100
  
  # set source
  a1.sources.r1.type = avro
  a1.sources.r1.bind = 0.0.0.0
  a1.sources.r1.port = 52020
  a1.sources.r1.channels = c1
  
  #配置拦截器
  a1.sources.r1.interceptors = i1 i2
  a1.sources.r1.interceptors.i1.type = timestamp
  a1.sources.r1.interceptors.i2.type = host
  a1.sources.r1.interceptors.i2.hostHeader=hostname
  a1.sources.r1.interceptors.i2.useIP=false
  
  #set sink to hdfs
  a1.sinks.k1.channel = c1
  a1.sinks.k1.type=hdfs
  a1.sinks.k1.hdfs.path=hdfs://node1:9000/loadbalance/logs/%{hostname}
  a1.sinks.k1.hdfs.filePrefix=%Y-%m-%d
  a1.sinks.k1.hdfs.round = true
  a1.sinks.k1.hdfs.roundValue = 10
  a1.sinks.k1.hdfs.roundUnit = minute
  a1.sinks.k1.hdfs.rollInterval = 60
  a1.sinks.k1.hdfs.rollSize = 50
  a1.sinks.k1.hdfs.rollCount = 10
  a1.sinks.k1.hdfs.batchSize = 100
  a1.sinks.k1.hdfs.fileType = DataStream
  ```

- 启动

  ```
  # node2和node3上启动
  flume-ng agent -n a1 -c /opt/flume/myconf -f /opt/flume/myconf/flume-server-loadbalance.conf -Dflume.root.logger=info,console
  # node1启动
  flume-ng agent -n a1 -c /opt/flume/myconf -f /opt/flume/myconf/ flume-client-loadbalance.conf -Dflume.root.logger=info,console
  ```

  