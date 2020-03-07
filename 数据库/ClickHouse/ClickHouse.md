[TOC]

# ClickHouse

* ClickHouse作为一款高性能OLAP数据库，确实是在很多场景下，查询效率会有很大的提升。
* 但是其数据库与其他的数据库略有不同，所以需要了解与学习这个数据库。

## 一、简介

### 1.1 概况

* ClickHouse是一个用于联机分析(OLAP)的列式数据库管理系统(DBMS)。

#### 1.1.1 行式数据库

* 处于同一行中的数据总是被物理的存储在一起。
* 常见的行式数据库：MySQL、Postgres和MS SQL Server。

| Row  | name     | title | event |
| ---- | -------- | ----- | ----- |
| #0   | zhangsan | day1  | swim  |
| #1   | lisi     | day2  | sleep |
| #2   | wangwu   | day3  | eat   |
| #N   | ....     | ...   | ....  |

查询：

![row_oriented](ClickHouse.assets/row_oriented.gif)

#### 1.1.2 列式数据库

* 列式数据库总是将同一列的数据存储在一起，不同列的数据也总是分开存储。
* 常见的列式数据库有： Vertica、 Paraccel (Actian Matrix，Amazon Redshift)、 Sybase IQ、 Exasol、 Infobright、 InfiniDB、 MonetDB (VectorWise， Actian Vector)、 LucidDB、 SAP HANA、 Google Dremel、 Google PowerDrill、 Druid、 kdb+。

| Row   | #0       | #1    | #2     | #N   |
| ----- | -------- | ----- | ------ | ---- |
| name  | zhangsan | lisi  | wangwu | ...  |
| title | day1     | day2  | day3   | ...  |
| event | swim     | sleep | eat    | ...  |

查询：

![column_oriented](ClickHouse.assets/column_oriented.gif)

#### 1.1.3 数据库对比

* Input/Output
  * 针对分析类查询，通常只需要读取表的一小部分列。在列式数据库中你可以只读取你需要的数据。例如，如果只需要读取100列中的5列，这将帮助你最少减少20倍的I/O消耗。
  * 由于数据总是打包成批量读取的，所以压缩是非常容易的。同时数据按列分别存储这也更容易压缩。这进一步降低了I/O的体积。
  * 由于I/O的降低，这将帮助更多的数据被系统缓存。
  * 例如，查询“统计每个广告平台的记录数量”需要读取“广告平台ID”这一列，它在未压缩的情况下需要1个字节进行存储。如果大部分流量不是来自广告平台，那么这一列至少可以以十倍的压缩率被压缩。当采用快速压缩算法，它的解压速度最少在十亿字节(未压缩数据)每秒。换句话说，这个查询可以在单个服务器上以每秒大约几十亿行的速度进行处理。这实际上是当前实现的速度。
* CPU
  * 由于执行一个查询需要处理大量的行，因此在整个向量上执行所有操作将比在每一行上执行所有操作更加高效。同时这将有助于实现一个几乎没有调用成本的查询引擎。如果你不这样做，使用任何一个机械硬盘，查询引擎都不可避免的停止CPU进行等待。所以，在数据按列存储并且按列执行是很有意义的。
  * 有两种方法可以做到这一点：
    1. 向量引擎：所有的操作都是为向量而不是为单个值编写的。这意味着多个操作之间的不再需要频繁的调用，并且调用的成本基本可以忽略不计。操作代码包含一个优化的内部循环。
    2. 代码生成：生成一段代码，包含查询中的所有操作。
  * 这是不应该在一个通用数据库中实现的，因为这在运行简单查询时是没有意义的。但是也有例外，例如，MemSQL使用代码生成来减少处理SQL查询的延迟(只是为了比较，分析型数据库通常需要优化的是吞吐而不是延迟)。

#### 1.1.4 场景与数据库选择

* 进行了哪些查询，多久查询一次以及各类查询的比例；
* 每种查询读取多少数据————行、列和字节；
* 读取数据和写入数据之间的关系；
* 使用的数据集大小以及如何使用本地的数据集；
* 是否使用事务,以及它们是如何进行隔离的；
* 数据的复制机制与数据的完整性要求；
* 每种类型的查询要求的延迟与吞吐量等等。

#### 1.1.5 OLAP场景

* 大多数是读请求
* 数据总是以相当大的批(> 1000 rows)进行写入
* 不修改已添加的数据
* 每次查询都从数据库中读取大量的行，但是同时又仅需要少量的列
* 宽表，即每个表包含着大量的列
* 较少的查询(通常每台服务器每秒数百个查询或更少)
* 对于简单查询，允许延迟大约50毫秒
* 列中的数据相对较小： 数字和短字符串(例如，每个URL 60个字节)
* 处理单个查询时需要高吞吐量（每个服务器每秒高达数十亿行）
* 事务不是必须的
* 对数据一致性要求低
* 每一个查询除了一个大表外都很小
* 查询结果明显小于源数据，换句话说，数据被过滤或聚合后能够被盛放在单台服务器的内存中

|          | OLTP                                               | OLAP                                               |
| -------- | -------------------------------------------------- | -------------------------------------------------- |
| 全称     | 联机事务处理<br />on-line transaction processing   | 联机分析处理<br />on-line analytical processing    |
| 用户     | 操作人员                                           | 决策人员                                           |
| DB设计   | 面向应用                                           | 面向决策                                           |
| 数据     | 当前的、最新的<br />细节的<br />二维的<br />分立的 | 历史的<br />聚集的<br />多维的<br />集成的、统一的 |
| 存取     | 读/写多条记录                                      | 读上百万条记录                                     |
| 工作单位 | 简单的事务                                         | 复杂的查询                                         |
| DB大小   | 100MB-GB                                           | 100GB-TB                                           |
| 时间要求 | 实时性                                             | 对时间要求不高                                     |
| 主要应用 | 数据库                                             | 数据仓库                                           |

### 1.2 ClickHouse 特性

**真正的历史数据库管理系统**

* 在一个真正的列式数据库管理系统中，除了数据本身外不应该存在其他额外的数据。这意味着为了避免在值旁边存储它们的长度“number”，你必须支持固定长度数值类型。
* 例如，10亿个UInt8类型的数据在未压缩的情况下大约消耗1GB左右的空间，如果不是这样的话，这将对CPU的使用产生强烈影响。即使是在未压缩的情况下，紧凑的存储数据也是非常重要的，因为解压缩的速度主要取决于未压缩数据的大小。

**数据压缩**

* 在一些列式数据库管理系统中(例如：InfiniDB CE 和 MonetDB) 并没有使用数据压缩。但是, 若想达到比较优异的性能，数据压缩确实起到了至关重要的作用。

**数据的磁盘管理**

* 许多的列式数据库(如 SAP HANA, Google PowerDrill)只能在内存中工作，这种方式会造成比实际更多的设备预算。
* ClickHouse被设计用于工作在传统磁盘上的系统，它提供每GB更低的存储成本，但如果有可以使用SSD和内存，它也会合理的利用这些资源。

**多核心并行处理**

ClickHouse会使用服务器上一切可用的资源，从而以最自然的方式并行处理大型查询。

**多服务器分布式处理**

上面提到的列式数据库管理系统中，几乎没有一个支持分布式的查询处理。 在ClickHouse中，数据可以保存在不同的shard上，每一个shard都由一组用于容错的replica组成，查询可以并行地在所有shard上进行处理。这些对用户来说是透明的

**支持SQL**

ClickHouse支持基于SQL的声明式查询语言，该语言大部分情况下是与SQL标准兼容的。 支持的查询包括 GROUP BY，ORDER BY，IN，JOIN以及非相关子查询。 不支持窗口函数和相关子查询。

**向量引擎**

为了高效的使用CPU，数据不仅仅按列存储，同时还按向量(列的一部分)进行处理，这样可以更加高效地使用CPU。

#### 实时的数据更新

ClickHouse支持在表中定义主键。为了使查询能够快速在主键中进行范围查找，数据总是以增量的方式有序的存储在MergeTree中。因此，数据可以持续不断地高效的写入到表中，并且写入的过程中不会存在任何加锁的行为。

#### 索引

按照主键对数据进行排序，这将帮助ClickHouse在几十毫秒以内完成对数据特定值或范围的查找。

**适合在线查询**

在线查询意味着在没有对数据做任何预处理的情况下以极低的延迟处理查询并将结果加载到用户的页面中。

**支持近似计算**

* ClickHouse提供各种各样在允许牺牲数据精度的情况下对查询进行加速的方法：
  1. 用于近似计算的各类聚合函数，如：distinct values, medians, quantiles
  2. 基于数据的部分样本进行近似查询。这时，仅会从磁盘检索少部分比例的数据。
  3. 不使用全部的聚合条件，通过随机选择有限个数据聚合条件进行聚合。这在数据聚合条件满足某些分布条件下，在提供相当准确的聚合结果的同时降低了计算资源的使用。

**支持数据复制和数据完整性**

* ClickHouse使用异步的多主复制技术。当数据被写入任何一个可用副本后，系统会在后台将数据分发给其他副本，以保证系统在不同副本上保持相同的数据。在大多数情况下ClickHouse能在故障后自动恢复，在一些少数的复杂情况下需要手动恢复。

### 1.3 缺点

1. 没有完整的事务支持。
2. 缺少高频率，低延迟的修改或删除已存在数据的能力。仅能用于批量删除或修改数据，但这符合 [GDPR](https://gdpr-info.eu/)（通用数据保护条例）。
3. 稀疏索引使得ClickHouse不适合通过其键检索单行的点查询。

### 1.4 性能

* 根据Yandex的内部测试结果，ClickHouse表现出了比同类可比较产品更优的性能。你可以在 [这里](https://clickhouse.yandex/benchmark.html) 查看具体的测试结果。

#### 1.4.1 单个大查询的吞吐量

* 吞吐量可以使用每秒处理的行数或每秒处理的字节数来衡量。
* 如果数据被放置在page cache中，则一个不太复杂的查询在单个服务器上大约能够以2-10GB／s（未压缩）的速度进行处理（对于简单的查询，速度可以达到30GB／s）。如果数据没有在page cache中的话，那么速度将取决于你的磁盘系统和数据的压缩率。
* 如果一个磁盘允许以400MB／s的速度读取数据，并且数据压缩率是3，则数据的处理速度为1.2GB/s。这意味着，如果你是在提取一个10字节的列，那么它的处理速度大约是1-2亿行每秒。
* 对于分布式处理，处理速度几乎是线性扩展的，但这受限于聚合或排序的结果不是那么大的情况下。

#### 1.4.2 处理短查询的延迟时间

* 如果一个查询使用主键并且没有太多行(几十万)进行处理，并且没有查询太多的列，那么在数据被page cache缓存的情况下，它的延迟应该小于50毫秒(在最佳的情况下应该小于10毫秒)。 否则，延迟取决于数据的查找次数。
* 如果你当前使用的是HDD，在数据没有加载的情况下，查询所需要的延迟可以通过以下公式计算得知： 查找时间（10 ms） * 查询的列的数量 * 查询的数据块的数量。

#### 1.4.3 处理大量短查询的吞吐量

* 在相同的情况下，ClickHouse可以在单个服务器上每秒处理数百个查询（在最佳的情况下最多可以处理数千个）。
* 但是由于这不适用于分析型场景。因此我们建议每秒最多查询100次。

#### 1.4.4 数据的写入性能

* 我们建议每次写入不少于1000行的批量写入，或每秒不超过一个写入请求。
* 当使用tab-separated格式将一份数据写入到MergeTree表中时，写入速度大约为50到200MB/s。
* 如果您写入的数据每行为1Kb，那么写入的速度为50，000到200，000行每秒。如果您的行更小，那么写入速度将更高。
* 为了提高写入性能，您可以使用多个INSERT进行并行写入，这将带来线性的性能提升。

### 1.5 历史

* ClickHouse最初是为 [Yandex.Metrica](https://metrica.yandex.com/) [世界第二大Web分析平台](http://w3techs.com/technologies/overview/traffic_analysis/all) 而开发的。多年来一直作为该系统的核心组件被该系统持续使用着。目前为止，该系统在ClickHouse中有超过13万亿条记录，并且每天超过200多亿个事件被处理。它允许直接从原始数据中动态查询并生成报告。本文简要介绍了ClickHouse在其早期发展阶段的目标。
* Yandex.Metrica基于用户定义的字段，对实时访问、连接会话，生成实时的统计报表。这种需求往往需要复杂聚合方式，比如对访问用户进行去重。构建报表的数据，是实时接收存储的新数据。
* 截至2014年4月，Yandex.Metrica每天跟踪大约120亿个事件（用户的点击和浏览）。为了可以创建自定义的报表，我们必须存储全部这些事件。同时，这些查询可能需要在几百毫秒内扫描数百万行的数据，或在几秒内扫描数亿行的数据。

#### 1.5.1 聚合数据与非聚合数据

* 有一种流行的观点认为，想要有效的计算统计数据，必须要聚合数据，因为聚合将降低数据量。
* 但是数据聚合是一个有诸多限制的解决方案，例如：
  - 你必须提前知道用户定义的报表的字段列表
  - 用户无法自定义报表
  - 当聚合条件过多时，可能不会减少数据，聚合是无用的。
  - 存在大量报表时，有太多的聚合变化（组合爆炸）
  - 当聚合条件有非常大的基数时（如：url），数据量没有太大减少（少于两倍）
  - 聚合的数据量可能会增长而不是收缩
  - 用户不会查看我们为他生成的所有报告，大部分计算将是无用的
  - 各种聚合可能违背了数据的逻辑完整性
* 如果我们直接使用非聚合数据而不进行任何聚合时，我们的计算量可能是减少的。
* 然而，相对于聚合中很大一部分工作被离线完成，在线计算需要尽快的完成计算，因为用户在等待结果。

## 二、入门指南

### 2.1 安装

* 安装上推荐安装官方文档上来安装，整个安装过程也不麻烦。
* https://clickhouse.yandex/docs/zh/getting_started/install/
* 默认的一些配置
  * 启动程序在：/etc/init/clickhouse-server (start/stop/status)
  * 日志在：/var/log/clickhouse-server目录下
  * 配置文件：/etc/clickhouse-server/，其中config.xml为配置文件，user.xml为用户权限文件。

### 2.2  使用指南

* 安装官网：https://clickhouse.yandex/docs/zh/getting_started/tutorial/
* 如果节点安装，实际根据配置安装即可，现测试为目的，以Docker容器为基础进行配置集群。

#### 2.2.1 单节点配置

* ```sh
  docker run -d --name clickhouse-server --privileged  -p 8123:8123 -p 9000:9000 --ulimit nofile=262144:262144 --volume=/home/docker/volume/clickhouse-server:/var/lib/clickhouse yandex/clickhouse-server
  ```

* 直接通过工具即可连接

#### 2.2.2  集群安装

* CilckHouse集群式同构集群（ 还有一个是异构集群）
  1. 在所有的机器上安装ClickHouse
  2. 配置集群配置文件
  3. 创建表实例
  4. 创建分布式表

## 三、客户端

ClickHouse提供了两个网络接口

* [HTTP](https://clickhouse.tech/docs/zh/interfaces/http/)，记录在案，易于使用。8123
* [本地TCP](https://clickhouse.tech/docs/zh/interfaces/tcp/)，这有较少的开销。9000

### 3.1 命令行客户端

* 通过yum安装clickhouse-client后，直接使用clickhouse-client进行查询。

### 3.2 HTTP客户端查询

```sh
curl 'http://localhost:8123/'
Ok.
curl 'http://localhost:8123/?query=SELECT%201'
1
```

### 3.3 MySQL interface

* ClickHouse配置mysql连接端口

  ```
  <mysql_port>9004</mysql_port>
  ```

* mysql通过命令行连接

  ```
  mysql --protocol tcp -u default -P 9004
  ```

### 3.4 JDBC驱动

* https://github.com/ClickHouse/clickhouse-jdbc

* ```xml
  <dependency>
      <groupId>ru.yandex.clickhouse</groupId>
      <artifactId>clickhouse-jdbc</artifactId>
      <version>0.2</version>
  </dependency>
  ```

### 3.5 Python连接

* https://github.com/yurial/clickhouse-client

* ```python
  from clickhouse_driver import Client
  
  clickHouseClient = Client(host=host, port=port, database=database, user=user, password=password)
  ```

## 四、数据类型

| 分类                                                     | 数据类型           | 描述                                                         |
| -------------------------------------------------------- | ------------------ | ------------------------------------------------------------ |
| 整型范围                                                 | Int8               | [-128 : 127]                                                 |
|                                                          | Int16              | [-32768 : 32767]                                             |
|                                                          | Int32              | [-2147483648 : 2147483647]                                   |
|                                                          | Int64              | [-9223372036854775808 : 9223372036854775807]                 |
| 无符号整型范围                                           | UInt8              | [0 : 255]                                                    |
|                                                          | UInt16             | [0 : 65535]                                                  |
|                                                          | UInt32             | [0 : 4294967295]                                             |
|                                                          | UInt64             | [0 : 18446744073709551615]                                   |
| 浮点数                                                   | Float32            | 与C语言中的float一致                                         |
|                                                          | Float64            | 与C语言中的doule一致                                         |
| 有符号的定点数<br />可在加、减和乘法运算过程中保持精度。 | Decimal(P, S)      | P - 精度。有效范围：[1:38]，决定可以有多少个十进制数字（包括分数）。 <br />S - 规模。有效范围：[0：P]，决定数字的小数部分中包含的小数位数。 |
|                                                          | Decimal32(S)       | ( -1 * 10^(9 - S), 1 * 10^(9 - S) )                          |
|                                                          | Decimal64(S)       | ( -1 * 10^(18 - S), 1 * 10^(18 - S) )                        |
|                                                          | Decimal128(S)      | ( -1 * 10^(38 - S), 1 * 10^(38 - S) )                        |
| Boolean Values                                           |                    | 没有单独的类型来存储布尔值。<br />可以使用 UInt8 类型，取值限制为 0 或 1。 |
| String                                                   | String             | 字符串可以任意长度的。                                       |
|                                                          | FixedString        | FixedString(n)<br />固定长度 N 的字符串（N 必须是严格的正自然数）。<br />不推荐使用 |
| 时间                                                     | Date               | 日期类型，用两个字节存储，表示从 1970-01-01 (无符号) 到当前的日期值。<br />最小值输出为0000-00-00。 |
|                                                          | DateTime           | 时间戳类型。用四个字节（无符号的）存储 Unix 时间戳）。允许存储与日期类型相同的范围内的值。最小值为 0000-00-00 00:00:00。时间戳类型值精确到秒（没有闰秒） |
| 枚举                                                     | Enum8,Enum16       | `Enum8` 用 `'String'= Int8` 对描述。<br /> `Enum16` 用 `'String'= Int16` 对描述。<br />用户使用的是字符串常量，但所有含有 `Enum` 数据类型的操作都是按照包含整数的值来执行。这在性能方面比使用 `String` 数据类型更有效。 |
| 空                                                       | Nullable(TypeName) | 允许用特殊标记 ([NULL](https://clickhouse.tech/docs/zh/query_language/syntax/)) 表示"缺失值"，可以与 `TypeName` 的正常值存放一起。例如，`Nullable(Int8)` 类型的列可以存储 `Int8` 类型值，而没有值的行将存储 `NULL`。<br />注意点 使用 `Nullable` 几乎总是对性能产生负面影响，在设计数据库时请记住这一点 |

## 五、数据库引擎

ClickHouse可以使用自己的数据库引擎

也是以选择额外的Mysql数据库引擎。

### 5.1 Mysql数据引擎

MySQL引擎用于将远程的MySQL服务器中的表映射到ClickHouse中，并允许您对表进行`INSERT`和`SELECT`查询，以方便您在ClickHouse与MySQL之间进行数据交换。

`MySQL`数据库引擎会将对其的查询转换为MySQL语法并发送到MySQL服务器中，因此您可以执行诸如`SHOW TABLES`或`SHOW CREATE TABLE`之类的操作。

但您无法对其执行以下操作：

- ATTACH/DETACH
- DROP
- RENAME
- CREATE TABLE
- ALTER

**创建连接**

```mysql
CREATE DATABASE [IF NOT EXISTS] db_name [ON CLUSTER cluster]
ENGINE = MySQL('host:port', 'database', 'user', 'password')
```

**支持的类型对应**

| MySQL                                | ClickHouse         |
| ------------------------------------ | ------------------ |
| UNSIGNED TINYINT                     | UInt8              |
| TINYINT                              | Int8               |
| UNSIGNED SMALLINT                    | UInt16             |
| SMALLINT                             | Int16              |
| UNSIGNED INT<br />UNSIGNED MEDIUMINT | UInt32             |
| INT<br />MEDIUMINT                   | Int32              |
| UNSIGNED BIGINT                      | UInt64             |
| BIGINT                               | Int64              |
| FLOAT                                | Float32            |
| DOUBLE                               | Float64            |
| DATE                                 | Date               |
| DATETIME<br />TIMESTAMP              | DateTime           |
| BINARYY                              | FixedString        |
| 其他                                 | String             |
| 可为空                               | Nullable(TypeName) |

## 六、表引擎

表引擎（即表的类型）决定了：

- 数据的存储方式和位置，写到哪里以及从哪里读取数据
- 支持哪些查询以及如何支持。
- 并发数据访问。
- 索引的使用（如果存在）。
- 是否可以执行多线程请求。
- 数据复制参数。

### 6.1 MergeTree

* Clickhouse 中最强大的表引擎当属 `MergeTree` （合并树）引擎及该系列（`*MergeTree`）中的其他引擎。

* `MergeTree` 引擎系列的基本理念如下。当你有巨量数据要插入到表中，你要高效地一批批写入数据片段，并希望这些数据片段在后台按照一定规则合并。相比在插入时不断修改（重写）数据进存储，这种策略会高效很多。

* 主要特点:

  - 存储的数据按主键排序。
  - 允许使用分区，如果指定了 [分区键](https://clickhouse.tech/docs/zh/operations/table_engines/custom_partitioning_key/) 的话。
  - 支持数据副本。
  - 支持数据采样。

* **建表sql**

  ```sql
  CREATE TABLE [IF NOT EXISTS] [db.]table_name [ON CLUSTER cluster]
  (
      name1 [type1] [DEFAULT|MATERIALIZED|ALIAS expr1],
      name2 [type2] [DEFAULT|MATERIALIZED|ALIAS expr2],
      ...
      INDEX index_name1 expr1 TYPE type1(...) GRANULARITY value1,
      INDEX index_name2 expr2 TYPE type2(...) GRANULARITY value2
  ) ENGINE = MergeTree()
  [PARTITION BY expr]
  [ORDER BY expr]
  [PRIMARY KEY expr]
  [SAMPLE BY expr]
  [SETTINGS name=value, ...]
  example:
  
  CREATE TABLE dbName.tableName (
   `id` Int64,
   `value` String,
   `create_time` DateTime
  ) ENGINE = MergeTree ORDER BY (id) SETTINGS index_granularity = 8192
  
  
  ENGINE MergeTree() PARTITION BY toYYYYMM(EventDate) ORDER BY (CounterID, EventDate, intHash32(UserID)) SAMPLE BY intHash32(UserID) SETTINGS index_granularity=8192
  ```
  * `ENGINE` - 引擎名和参数。 `ENGINE = MergeTree()`. `MergeTree` 引擎没有参数。

  * `PARTITION BY` — [分区键](https://clickhouse.tech/docs/zh/operations/table_engines/custom_partitioning_key/) 。

    要按月分区，可以使用表达式 `toYYYYMM(date_column)` ，这里的 `date_column` 是一个 [Date](https://clickhouse.tech/docs/zh/data_types/date/) 类型的列。这里该分区名格式会是 `"YYYYMM"` 这样。

  * `ORDER BY` — 表的排序键。

    可以是一组列的元组或任意的表达式。 例如: `ORDER BY (CounterID, EventDate)` 。

  * `PRIMARY KEY` - 主键，如果要设成 [跟排序键不相同](https://clickhouse.tech/docs/zh/operations/table_engines/mergetree/)。

    默认情况下主键跟排序键（由 `ORDER BY` 子句指定）相同。 因此，大部分情况下不需要再专门指定一个 `PRIMARY KEY` 子句。

  * `SAMPLE BY` — 用于抽样的表达式。

    如果要用抽样表达式，主键中必须包含这个表达式。例如： `SAMPLE BY intHash32(UserID) ORDER BY (CounterID, EventDate, intHash32(UserID))` 。

  * `SETTINGS` — 影响 `MergeTree` 性能的额外参数：

    - `index_granularity` — 索引粒度。即索引中相邻『标记』间的数据行数。默认值，8192 。该列表中所有可用的参数可以从这里查看 [MergeTreeSettings.h](https://github.com/ClickHouse/ClickHouse/blob/master/dbms/src/Storages/MergeTree/MergeTreeSettings.h) 。
    - `index_granularity_bytes` — 索引粒度，以字节为单位，默认值: 10Mb。如果仅按数据行数限制索引粒度, 请设置为0(不建议)。
    - `enable_mixed_granularity_parts` — 启用或禁用通过 `index_granularity_bytes` 控制索引粒度的大小。在19.11版本之前, 只有 `index_granularity` 配置能够用于限制索引粒度的大小。当从大表(数十或数百兆)中查询数据时候，`index_granularity_bytes` 配置能够提升ClickHouse的性能。如果你的表内数据量很大，可以开启这项配置用以提升`SELECT` 查询的性能。
    - `use_minimalistic_part_header_in_zookeeper` — 数据片段头在 ZooKeeper 中的存储方式。如果设置了 `use_minimalistic_part_header_in_zookeeper=1` ，ZooKeeper 会存储更少的数据。更多信息参考『服务配置参数』这章中的 [设置描述](https://clickhouse.tech/docs/zh/operations/server_settings/settings/#server-settings-use_minimalistic_part_header_in_zookeeper) 。
    - `min_merge_bytes_to_use_direct_io` — 使用直接 I/O 来操作磁盘的合并操作时要求的最小数据量。合并数据片段时，ClickHouse 会计算要被合并的所有数据的总存储空间。如果大小超过了 `min_merge_bytes_to_use_direct_io` 设置的字节数，则 ClickHouse 将使用直接 I/O 接口（`O_DIRECT` 选项）对磁盘读写。如果设置 `min_merge_bytes_to_use_direct_io = 0` ，则会禁用直接 I/O。默认值：`10 * 1024 * 1024 * 1024` 字节。
    - `merge_with_ttl_timeout` — TTL合并频率的最小间隔时间。默认值: 86400 (1 天)。
    - `write_final_mark` — 启用或禁用在数据片段尾部写入最终索引标记。默认值: 1（不建议更改）。
    - `storage_policy` — 存储策略。 参见 [使用多个区块装置进行数据存储](https://clickhouse.tech/docs/zh/operations/table_engines/mergetree/#table_engine-mergetree-multiple-volumes).

* **数据副本**
  * 只有 MergeTree 系列里的表可支持副本：
    - ReplicatedMergeTree
    - ReplicatedSummingMergeTree
    - ReplicatedReplacingMergeTree
    - ReplicatedAggregatingMergeTree
    - ReplicatedCollapsingMergeTree
    - ReplicatedVersionedCollapsingMergeTree
    - ReplicatedGraphiteMergeTree
  * 副本是表级别的，不是整个服务器级的。所以，服务器里可以同时有复制表和非复制表。
  * 依赖于Zookeeper实现表复制
* **自定义分区键**
  * 一个分区是指按指定规则逻辑组合一起的表的记录集。可以按任意标准进行分区，如按月，按日或按事件类型。为了减少需要操作的数据，每个分区都是分开存储的。访问数据时，ClickHouse 尽量使用这些分区的最小子集。
* **ReplacingMergeTree**
  * 该引擎和[MergeTree](https://clickhouse.tech/docs/zh/operations/table_engines/mergetree/)的不同之处在于它会删除具有相同主键的重复项。
* **SummingMergeTree**
  * 当合并 `SummingMergeTree` 表的数据片段时，ClickHouse 会把所有具有相同主键的行合并为一行，该行包含了被合并的行中具有数值数据类型的列的汇总值。
* **AggregatingMergeTree**
  * 该引擎继承自 [MergeTree](https://clickhouse.tech/docs/zh/operations/table_engines/mergetree/)，并改变了数据片段的合并逻辑。 ClickHouse 会将相同主键的所有行（在一个数据片段内）替换为单个存储一系列聚合函数状态的行。
* **CollapsingMergeTree**
  * 该引擎继承于 [MergeTree](https://clickhouse.tech/docs/zh/operations/table_engines/mergetree/)，并在数据块合并算法中添加了折叠行的逻辑。
  * `CollapsingMergeTree` 会异步的删除（折叠）这些除了特定列 `Sign` 有 `1` 和 `-1` 的值以外，其余所有字段的值都相等的成对的行
  * 该引擎可以显著的降低存储量并提高 `SELECT` 查询效率。
* **VersionedCollapsingMergeTree**
  * 允许快速写入不断变化的对象状态。
  * 后台删除旧状态的数据，这大大减少了存储量。

### 6.2 Log

* 暂未找到该表的优势

### 6.3 外部表引擎

* Kafka
* MySQL
* JDBC
* ODBC
* HDFS