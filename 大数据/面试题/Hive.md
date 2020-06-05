## Hive

### 1. 数据倾斜的原因以及解决方案

* 原因：
  1. key分布不均匀;
  2. 业务数据本身的特性;
  3. 建表时考虑不周;
  4. 某些SQL语句本身就有数据倾斜;
* 解决方案：
  1. 参数调节
     * hive.map.aggr = true
     * hive.groupby.skewindata=true
     * 有数据倾斜的时候进行负载均衡，当选项设定位true,生成的查询计划会有两个MR Job。第一个MR Job中，Map的输出结果集合会随机分布到Reduce中，每个Reduce做部分聚合操作，并输出结果，这样处理的结果是相同的Group By Key有可能被分发到不同的Reduce中，从而达到负载均衡的目的；第二个MR Job再根据预处理的数据结果按照Group By Key 分布到 Reduce 中（这个过程可以保证相同的 Group By Key 被分布到同一个Reduce中），最后完成最终的聚合操作。
  2. SQL 语句调节：
     * count distinct 时，将值为空的情况单独处理，如果是计算count distinct，可以不用处理，直接过滤，在最后结果中加1。如果还有其他计算，需要进行group by，可以先将值为空的记录单独处理，再和其他计算结果进行union。
     * 空Key处理
       * 空 key 过滤
         - 有时join超时是因为某些key对应的数据太多，而相同key对应的数据都会发送到相同的reducer上，从而导致内存不够。
         - 此时我们应该仔细分析这些异常的key，很多情况下，这些key对应的数据是异常数据，我们需要在SQL语句中进行过滤。
       * 空 key 转换
         - 有时虽然某个 key 为空对应的数据很多，但是相应的数据不是异常数据，必须要包含在 join 的结果中，此时我们可以表 a 中 key 为空的字段赋一个随机的值，使得数据随机均匀地分不到不同的 reducer 上。

### 2. 简单谈谈Hive与RDBMS的异同

* 相同：
  * SQL
* 不同：
  * Hive只支持插入、只读
  * Hive延迟高
  * Hive扩展方便

### 3. hive中order by、sort by、distribute by、cluster by各代表的意思

* order by：全局排序
* sort by：Reduce排序
* distribute by：类似于MR的partition排序，
* cluster by：=sort by + distribute by

### 4. hive有哪些方式保存元数据，各有哪些特点？

* 内嵌模式：默认是Derby，是Hive内嵌元数据的数据库
* 本地模式：存储到Mysql数据库中
* 远程模式：客户端利用Thrift协议通过MetaStoreServer访问元数据库。

### 5. hive的两张表关联，使用mapreduce怎么实现？

* 如果其中有一张表为小表，直接使用map端join的方式（map端加载小表）进行聚合。
* 如果两张都是大表，那么采用联合key，联合key的第一个组成部分是join on中的公共字段，第二部分是一个flag，0代表表A，1代表表B，由此让Reduce区分客户信息和订单信息；在Mapper中同时处理两张表的信息，将join on公共字段相同的数据划分到同一个分区中，进而传递到一个Reduce中，然后在Reduce中实现聚合。