# Yarn

![appliction_yarn](img/application_yarn.jpg)

1. **可扩展性**：数据中心的数据处理能力继续快速增长，因为Yarn的ResourceManager仅专注于任务调度，能将集群的管理变得更加简单。
2. **兼容MapReduce**：MapReduce应用程序无需改动就可以在Yarn上运行
3. **提高集群使用率**：ResourceManager是以个纯粹调度系统，更具Capacity、FIFA或者SIAS等原则对集群进行优化利用，有利于提高集群资源的额利用
4. **更多支持**：Yarn处理提供了`数据处理`、`图形处理`、`迭代处理`以及`实时模型`，更全的功能页面
5. **灵活**：随着MapReduce成为了用户端库，它发展独立与地城的资源管理层，从而有着多种灵活的方式。

## Yarn 架构

* Yarn的架构还是经典的主从架构（Master/Slave）。
* 一个ResourceManager(RM)和多个NodeManager(NM)构成。


![yarn_1](img/yarn_1.png)

* 在Yarn体系结构中，全局ResorceManager作为主守护程序运行，作为架构中的全局的Master角色，通常在专用计算机上运行，它在各种竞应用程序之间仲裁可用的集群资源。ResourceManager跟踪集群上可用的活动节点和资源的数量，并协调用户体提交的应用程序获取哪些资源和时间。ResourceManger是具有此信息的单进程，因此它可以以共享，安全和多租户的方式进行调度决策（例如，根据应用程序优先级，队列容量，ACL，数据位置等）。

* 当用户提交应用时，将启动名为ApplicationMaster的轻量级进程实例，以协调应用程序种所有的任务执行。这包括监视任务，重新启动失败的任务，推测性地运行慢速任务以及计算应用程序计数器地总值，这些指责先前已经分配给所有工作的单个JobTracker。ApplicationMaster和属于其应用程序的任务在NodeManager控制的资源容器中运行。

* NodeManager时TaskTracker的更通用和高效的版本。NodeManager没有固定的map和reduce插槽，而是有许多动态创建的容器。容器的大小取决于它包含的资源量，例如内存、CPU、磁盘和网络IO。（目前，仅支持内存和CPU，csgroup未来可能用于控制磁盘和网络IO）。节点上的容器数量时配置参数和专用于从属守护程序和OS的资源之外的节点资源总量（例如总CPU和总内存）的乘积。

* ApplicationMaster可以在容器内运行任何类型的任务。例如，MapReduce ApplicaitonMaster请求内容启动map或reduce任务，而且Giraph ApplicationMaster请求执行Giragp任务。还可以实现运行特定任务的自定义 ApplicationMaster。

* 在Yarn中，MapReduce简单地降级为分布式应用程序的角色，现称之为MRv2。

* `一个可以运行任何分布式应用程序的集群`ResourceManager，NodeManager和容器不管相信应用程序或任务的类型。所有特定与应用程序框架的代码都被简单地移动到其ApplicationMaster，以便Yarn可以支持任何分布式框架——只要有人为它上线了ApplicationMaster。

* 正是这种通用方法，运行许多不同工作负载的Hadoop Yarn集群可以实现。数据中心的单个Hadoop集群剋运行MapReduce，Graph，Storm，Spark，Tez/Impala，MPI等

* 单集群有着许多的优点：
    1. `更高的集群利用率`，一个框架未使用的资源可以被另外一个框架使用。
    2. `降低运行成本`，因为只需要管理和调整一个“全能”集群
    3. `减少数据移动`，因为不需要在Hadoop Yarn和运行再不同机器集群上的系统之间移动数据。
    4. 管理单个集群还可以为数据处理提供更环保的解决方案


## Yarn 核心组件

| 组件名称 | 作用 |
|:------|:------|
|ResourceManager|是Master上一个独立运行的进程，负责集群统一的资源管理、调度、分配等|
|ApplicationManager|相当于Application的监护人和管理者，负责监控、管理这个Application的所有Attempt在cluster中个节点上的具体运行，同时向Yarn ResourceManager申请资源、返还资源。|
|NodeManager|是Slave上独立运行的一个进程，负责上报节点的状态(磁盘、内存、CPU等使用信息)|
|Container|是Yarn中分配资源的一个单位，包含内存、CPU等资源，Yarn以Container为单位分配资源|

ResourceManager 负责对NodeManager上资源进行统一管理和调度，当用户提交一个应用程序时，需要提供一个用以跟踪和管理这个程序的ApplictionMaster，它负责向ResourceManager是申请资源，并要求NodeManager启动可以占用一定资源的任务。由于不通的Application被分布到不同的节点上，因此相互之间不会影响。

Client向ResourceManger提交的每一个应用程序都必须有一个ApplicationMaster，它经过ResouceManger分配资源后，运行于某一个Slave节点的Container中，具体做事情的Task，同样也运行与某一个Slave节点的Container中。