## 一、生产环境集群部署

### 1.1 方案背景

假设每天集群需要承载10亿数据。一天24小时，晚上12点到凌晨8点几乎没多少数据。使用二八法则估计，也就是80%的数据（8亿）会在16个小时涌入，而且8亿的80%的数据（6.4亿）会在这16个小时的20%时间（3小时）涌入。QPS计算公式=640000000÷(3*60*60)=6万，也就是说高峰期的时候咱们的Kafka集群要抗住每秒6万的并发。
磁盘空间计算，每天10亿数据，每条50kb，也就是46T的数据。保存2副本，46*2=92T，保留最近3天的数据。故需要 92 * 3 = 276T

### 1.2 QPS角度

部署Kafka，Hadoop，MySQL，大数据核心分布式系统，一般建议大家直接采用物理机，不建议用一些低配置的虚拟机。QPS这个东西，不可能是说，你只要支撑6万QPS，你的集群就刚好支撑6万QPS就可以了。假如说你只要支撑6w QPS，2台物理机绝对绝对够了，单台物理机部署kafka支撑个几万QPS是没问题的。但是这里有一个问题，我们通常是建议，公司预算充足，尽量是让高峰QPS控制在集群能承载的总QPS的30%左右，所以我们搭建的kafka集群能承载的总QPS为20万~30万才是安全的。所以大体上来说，需要5~7台物理机来部署，基本上就很安全了，每台物理机要求吞吐量在每秒几万条数据就可以了，物理机的配置和性能也不需要特别高。

### 1.3 磁盘角度

我们现在需要5台物理机，需要存储276T的数据，所以大概需要每台存储60T的数据，公司一般的配置是11块盘，这样的话，我们一个盘7T就搞定。

SSD就是固态硬盘，比机械硬盘要快，那么到底是快在哪里呢？其实SSD的快主要是快在磁盘随机读写，就要对磁盘上的随机位置来读写的时候，SSD比机械硬盘要快。像比如说是MySQL这种系统，就应该使用SSD了。比如说我们在规划和部署线上系统的MySQL集群的时候，一般来说必须用SSD，性能可以提高很多，这样MySQL可以承载的并发请求量也会高很多，而且SQL语句执行的性能也会提高很多。
Kafka集群，物理机是用昂贵的SSD呢？还是用普通的机械硬盘呢？因为==**写磁盘的时候kafka是顺序写的**==。机械硬盘顺序写的性能机会跟内存读写的性能是差不多的，所以对于Kafka集群来说我们使用机械硬盘就可以了。

### 1.4 内存角度

kafka自身的jvm是用不了过多堆内存的，因为kafka设计就是规避掉用jvm对象来保存数据，避免频繁fullgc导致的问题，所以一般kafka自身的jvm堆内存，分配个10G左右就够了，剩下的内存全部留给os cache。
那服务器需要多少内存够呢？我们估算一下，大概有100个topic，所以要保证有100个topic的leader partition的数据在os chache里。100个topic，一个topic有5个partition。那么总共会有500个partition。每个partition的大小是1G，我们有2个副本，也就是说要把100个topic的leader partition数据都驻留在内存里需要1000G的内存。我们现在有5台服务器，所以平均下来每天服务器需要200G的内存，但是其实partition的数据我们没必要所有的都要驻留在内存里面，只需要25%的数据在内存就行，200G * 0.25 = 50G就可以了。所以一共需要60G的内存，故我们可以挑选64G内存的服务器也行，大不了partition的数据再少一点在内存，当然如果是128G内存那就更好。

### 1.5 CPU角度

CPU规划，主要是看你的这个进程里会有多少个线程，线程主要是依托多核CPU来执行的，如果你的线程特别多，但是CPU核很少，就会导致你的CPU负载很高，会导致整体工作线程执行的效率不太高，我们前面讲过Kafka的Broker的模型。acceptor线程负责去接入客户端的连接请求，但是他接入了之后其实就会把连接分配给多个processor，默认是3个，但是说实话一般生产环境的话呢 ，建议大家还是多加几个，整体可以提升kafka的吞吐量比如说你可以增加到6个，或者是9个。另外就是负责处理请求的线程，是一个线程池，默认是8个线程，在生产集群里，建议大家可以把这块的线程数量稍微多加个2倍~3倍，其实都正常，比如说搞个16个工作线程，24个工作线程。后台会有很多的其他的一些线程，比如说定期清理7天前数据的线程，Controller负责感知和管控整个集群的线程，副本同步拉取数据的线程，这样算下来每个broker起码会有上百个线程。根据经验4个cpu core，一般来说几十个线程，在高峰期CPU几乎都快打满了。8个cpu core，也就能够比较宽裕的支撑几十个线程繁忙的工作。所以Kafka的服务器一般是建议16核，基本上可以hold住一两百线程的工作。当然如果可以给到32 cpu core那就最好不过了。

### 1.6 网卡角度

现在的网基本就是千兆网卡（1GB / s），还有万兆网卡（10GB / s）。kafka集群之间，broker和broker之间是会做数据同步的，因为leader要同步数据到follower上去，他们是在不同的broker机器上的，broker机器之间会进行频繁的数据同步，传输大量的数据。每秒两台broker机器之间大概会传输多大的数据量？高峰期每秒大概会涌入6万条数据，约每天处理10000个请求，每个请求50kb，故每秒约进来488M数据，我们还有副本同步数据，故高峰期的时候需要488M * 2 = 976M/s的网络带宽，所以在高峰期的时候，使用千兆带宽，网络还是非常有压力的。

### 1.7 Kafka集群配置

10亿数据，6w/s的吞吐量，276T的数据，5台物理机
硬盘：11（SAS） * 7T，7200转
内存：64GB/128GB，JVM分配10G，剩余的给os cache
CPU：16核/32核
网络：千兆网卡，万兆更好

### 1.8 集群配置

1. server.properties配置文件核心参数配置

   ```
   【broker.id】
   每个broker都必须自己设置的一个唯一id
   
   【log.dirs】
   这个极为重要，kafka的所有数据就是写入这个目录下的磁盘文件中的，如果说机器上有多块物理硬盘，那么可以把多个目录挂载到不同的物理硬盘上，然后这里可以设置多个目录，这样kafka可以数据分散到多块物理硬盘，多个硬盘的磁头可以并行写，这样可以提升吞吐量。
   
   【zookeeper.connect】
   连接kafka底层的zookeeper集群的
   
   【Listeners】
   broker监听客户端发起请求的端口号
   
   【unclean.leader.election.enable】
   默认是false，意思就是只能选举ISR列表里的follower成为新的leader，1.0版本后才设为false，之前都是true，允许非ISR列表的follower选举为新的leader
   
   【delete.topic.enable】
   默认true，允许删除topic
   
   【log.retention.hours】
   可以设置一下，要保留数据多少个小时，这个就是底层的磁盘文件，默认保留7天的数据，根据自己的需求来就行了
   
   【min.insync.replicas】
   acks=-1（一条数据必须写入ISR里所有副本才算成功），你写一条数据只要写入leader就算成功了，不需要等待同步到follower才算写成功。但是此时如果一个follower宕机了，你写一条数据到leader之后，leader也宕机，会导致数据的丢失。
   ```

2. kafka-start-server.sh中的jvm设置 

   ```
   export KAFKA_HEAP_OPTS=”-Xmx6g -Xms6g -XX:MetaspaceSize=96m -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:G1HeapRegionSize=16M -XX:MinMetaspaceFreeRatio=50 -XX:MaxMetaspaceFreeRatio=80”
   ```

## 二、Kafka集群常用命令

### 2.1 启动服务

```bash
服务启动：
./kafka-server-start.sh -daemon ../config/server.properties 
停止服务：
kafka-server-stop.sh

如果报错：
No kafka server to stop
修改kafka-server-stop.sh
将 PIDS=$(ps ax | grep -i 'kafka.Kafka' | grep java | grep -v grep | awk '{print $1}')
修改为 PIDS=$(jps -lm | grep -i 'kafka.Kafka' | awk '{print $1}')
```

### 2.2 创建主题

```bash
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test
```

### 2.3 查看主题

```bash
bin/kafka-topics.sh --list --zookeeper localhost:2181
```

### 2.4 发送消息

```bash
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test
This is a message
This is another message
```

### 2.5 消费消息

```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning
This is a message
This is another message
```

### 2.6 自带的脚本测试

```
测试生产数据
bin/kafka-producer-perf-test.sh --topic test-topic --num-records 500000 --record-size 200 --throughput -1 --producer-props bootstrap.servers=hadoop03:9092,hadoop04:9092,hadoop05:9092 acks=-1
测试消费数据
bin/kafka-consumer-perf-test.sh --broker-list hadoop03:9092,hadoop04:9092,hadoop53:9092 --fetch-size 2000 --messages 500000 --topic test-topic 
```

## 三、Kafka监控软件

### 3.1 功能介绍

1. 管理多个kafka集群
2. 便捷的检查kafka集群状态(topics,brokers,备份分布情况,分区分布情况)
3. 选择你要运行的副本
4. 基于当前分区状况进行
5. 可以选择topic配置并创建topic(0.8.1.1和0.8.2的配置不同)
6. 删除topic(只支持0.8.2以上的版本并且要在broker配置中设置delete.topic.enable=true)
7. Topic list会指明哪些topic被删除（在0.8.2以上版本适用）
8. 为已存在的topic增加分区
9. 为已存在的topic更新配置
10. 在多个topic上批量重分区
11. 在多个topic上批量重分区(可选partition broker位置)



### 3.2 编译安装KafkaManager

1. 准备安装环境

   ```
   （1）Kafka 0.8.1.1 or 0.8+
   （2）sbt 0.13.x
   （3）Java 8+
   ```

2. 安装sbt

   ```
   curl https://bintray.com/sbt/rpm/rpm > bintray-sbt-rpm.repo
   mv bintray-sbt-rpm.repo /etc/yum.repos.d/
   yum install sbt
   ```

3. 编译KafkaManager

   ```
   git clone https://github.com/yahoo/kafka-manager.git
   cd kafka-manager
   sbt clean dist
   ```

4. 修改配置

   ```
   #　编译成功后，会在target/universal下生成一个zip包， 解压并修改配置文件
   unzip kafka-manager-1.3.0.4.zip
   vim kafka-manager-1.3.0.4/conf/application.conf
   
   application.conf中的kafka-manager.zkhosts的值设置为你的zk地址
   
   如： *kafka-manager.zkhosts="h2:2181"*
   	*kafka-manager.zkhosts=${?ZK_HOSTS}*
   	*pinned-dispatcher.type="PinnedDispatcher"*
   	*pinned-dispatcher.executor="thread-pool-executor"*
   ```

5. 启动

   ```
   nohup bin/kafka-manager -Dconfig.file=conf/application.conf -Dhttp.port=9001 1>/dev/null 2>/dev/null &
   ```

6. 访问ip:9001即可访问服务了。