[TOC]

## 一、HDFS

### 1. HDFS特点？为什么使用HDFS

* **高容错**
  * 多副本策略
* **高吞吐**
  * 供高吞吐量的顺序读/写操作，不太适合大量随机读的应用场景
  * 会以提高时间延迟为代价
* **大文件**
  * 可支持GB到TB级别的数据
  * 但是不适合小文件存储
* **简单一致性模型**
  * 一次写入多次读取 (write-once-read-many) 的访问模型
* **跨平台移植性**
  * 跨平台移植性，其他数据平台都可以接入到HDFS。
* **扩展性**
  * Hadoop可以部署在简单的机子上，而且扩展及其方便。

### 2. Hadoop版本，区别，发行版本，你项目中的使用的是哪个？

* 版本
  * Hadoop1.0
  * Hadoop2.0 & Hadoop3.0
    * 相比较增加了Yarn
* 发行版本
  * Apache，官网下载版本
  * Cloudera，CDH
  * Hortonworks，HDP（18年与Cloudera公司合并成为全球第二大开源公司）
* CDH6.3.2版本，CDH6.3.3版本开始就要收费了。
  * 其中Hadoop版本为 3.0.0+cdh6.3.2

### 3. 简单描述一下HDFS的读写流程

* 写文件
  1. 客户端向NameNode发出写文件请求
  2. NameNode检查，先将操作写入EditLog，并返回输出流对象
  3. client端按128MB的块切分文件
  4. client发送到DataNode，其他多个DataNode构成pipeline管道完成传输。
  5. DataNode写完一个块后，会返回确认信息
  6. 关闭输输出流
  7. 发送完成信号给NameNode
* 读文件
  1. client与NameNode通信获取文件块位置信息
  2. 从DataNode中获取到数据，一个不行换下一个。
  3. client校验数据

### 4. HDFS什么时候进入安全模式

* 首先，安全模式是hadoop的一种保护机制，用于保护集群中的数据块的安全性，比方说集群刚启动的时候，节点没启动完全，这个时候就会进入安全模式。
* 安全模式下，文件只读不可修改。
* 进入安全模式的几种情况
  * 存储元数据空间不足，内存不足
  * 数据副本数未达到block比例要求
  * 存活的DataNode少于配置的数量，默认是0

### 5. HDFS HA高可用原理，高可用时有那些节点？作用？

* Hadoop为主从结构，一旦主节点宕机，会导致整个服务不可用。整个时候，Hadoop的高可用就需要体现在NameNode节点的高可用。
* 依赖JournalNode集群，NameNode的元数据存储服务都会放入该集群，保证切换NameNode，元数据依旧高可用。
* 依赖于Zookeeper，每个NameNode都会有一个ZKFC服务，一旦NameNode不可用，ZKFC就会上报Zookeeper，其他正常的NameNode就会转活跃。

### 6. Hadoop中NameNode、DataNode和Client三者之间的通信方式是什么？

* 通信方式
  * client和namenode之间是通过rpc通信；
  * datanode和namenode之间是通过rpc通信；
  * client和datanode之间是通过简单的socket通信;
* DataNode与NameNode
  * 每1小时DataNode的向NameNode上报所有的块的列表
  * 每3秒DataNode向NameNode发送一次心跳，NameNode返回给该DataNode的命令。

## 二、Yarn

### 1. 运行原理

* 还是属于经典的主从架构（Master/Slave）。
* 一个ResourceManager(RM)和多个NodeManager(NM)构成。
* YARN工作原理简述：
  1. `Client` 提交作业到 YARN 上；
  2. `Resource Manager` 选择一个 `Node Manager`，启动一个 `Container` 并运行 `Application Master` 实例；
  3. `Application Master` 根据实际需要向 `Resource Manager` 请求更多的 `Container` 资源（如果作业很小, 应用管理器会选择在其自己的 JVM 中运行任务）；
  4. `Application Master` 通过获取到的 `Container` 资源执行分布式计算。

### 2. Yarn中有哪些资源调度器

* **FIFO Scheduler**
  * 队列：First In First Out
  * 不需要任何配置，但是它不适用于共享集群。大的应用可能会占用所有的集群资源，导致其他应用被阻塞
* **Capacity Scheduler**
  * 资源队列
  * 分队列使用，弹性调整，集群空闲可借用其他队列资源使用
* **Fair Scheduler**
  * 所有运行的job动态的调整系统资源

## 三、MapReduce

### 1. 核心思想

* 分而治之
  * 复杂的计算任务，单台服务器无法胜任时，可将此大任务切分成一个个小的任务，小任务分别在不同的服务器上**并行**的执行；最终再汇总每个小任务的结果
* 任务阶段
  * Map阶段（切分成一个个小的任务）
  * Reduce阶段（汇总小任务的结果）

### 2. 描述一下MR数据倾斜的原因以及解决办法？

* 原因：
  1. 某一个区域的数据量要远远大于其他区域。
  2. 部分记录的大小远远大于平均值。
* 判断：
  1. 观察，Yarn中某个task执行时间过程
  2. 代码，关注数据，设定一个阈值（判断迭代器大小），一旦超过这个阈值，就可以观察到该数据
* 解决方案：
  1. 自定义分区
  2. Combine
  3. 抽样和范围分区
  4. 数据大小倾斜的自定义策略

### 3. 简单描述一下MR过程中的Suffle过程

* Suffle--->Map
  	1. 分区Partition
   	2. 写入环形内存缓冲区
   	3. 执行溢出写
   	4. 归并Merge
 * Suffle--->Reduce
   1. 复制Copy
   2. 归并Merge
   3. Reduce阶段
