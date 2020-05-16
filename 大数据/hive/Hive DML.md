# Hive分区

## 一、Hive分区表

### 1. 分区表创建

#### 1.1 分区表概念

​	在文件系统上建立文件夹，把表的数据放在不同文件夹下面，加快查询速度。

​	大表分区分为多个，确定查询范围。增加查询速度。

#### 1.2 分区表状态

  * 一个字段的分区表

    ```sql
    create table student_partition1(
    id int, 
    name string, 
    age int)
    partitioned by (dt string)
    row format delimited fields terminated by '\t'; 
    -------
    partitioned by (dt string)
    -------
    ```

* 两个字段的分区表

  ```sql
  create table student_partition2(
  id int, 
  name string, 
  age int)
  partitioned by (month string, day string)
  row format delimited fields terminated by '\t'; 
  ----------------------------
  partitioned by (month string, day string)
  ----------------------------
  ```

## 二、Hive修改表结构

### 2.1 修改表名称

```sql
alter table table_name1 rename to table_name2;
```

### 2.2 查看表结构信息

```sql
desc student_partition3;
desc formatted student_partition3;
```

###  2.3 增加/修改/替换列信息

```sql
-- 增加列
alter table student_par add columns(col string);
-- 修改列
alter table student_par change old_col new_col int;
-- 替换列
alter table student_par replace cloumns(eid INT empid Int)
-- 删除列,原有的又col1，col2，删除col3
alter table student_par replace cloumns(col1 string,col2 string)
```

### 2.4 增加/删除/查看分区

```sql
-- 添加单个分区
alter table student_partition1 add partition(dt='20170601') ;
 
-- 添加多个分区
alter table student_partition1 add partition(dt='20170602') partition(dt='20170603'); 
-- 删除分区
alter table student_partition1 drop partition (dt='20170601');
alter table student_partition1 drop partition (dt='20170602'),partition (dt='20170603');
```

```sql
show partitions table_name
```



## 三、Hive数据导入

### 1. 向表中加载数据**load**

```sql
load data [local] inpath 'dataPath' overwrite | into table student [partition (partcol1=val1,…)];
```

1. load data: 加载数据
2. local:表示从本地加载到Hive表，否则是从HDFS加载到Hive表中
3. inpath:加载的文件路径
4. overwarite:表示覆盖表中已有的数据，否则表示追加
5. into table:加载到的目标表
6. partition:指定分区表

```sql
-- 普通表：
load data local inpath '/opt/bigdata/data/person.txt' into table person

-- 分区表：
load data local inpath '/opt/bigdata/data/person.txt' into table person partition（dt="20190505"）
```

### 2. 查询插入**insert**

```sql
insert into/overwrite table  tableName  select xxxx from tableName

insert into table student_partition1 partition(dt="2019-07-08") select * from student1;
```

### 3. 查询并创建表并导入**as select**

```sql
create table if not exists tableName as select id, name from tableName;
```

### 4. 加载数据路径并创建表并导入**location**

```sql
create table if not exists student1(
id int, 
name string)
row format delimited fields terminated by '\t'
location '/user/hive/warehouse/student1';
```

### 5. 在HDFS上，导入导出操作**export、import**

```sql
create table student2 like student1;
export table student1  to   '/export/student1';
import table student2  from '/export/student1';
```

## 四、Hive数据导出

### 1. insert 导出

```sql
insert overwrite local directory '/opt/bigdata/export/student' 
select * from student;
```

```sql
-- 本地
insert overwrite local directory '/opt/bigdata/export/student'
row format
delimited fields terminated by  ','
select * from student;
```

```sql
-- HDFS
insert overwrite directory '/export/'
row format
delimited fields terminated by  ','
select * from student;
```

### 2. 复制HDFS文件到本地

```bash
hdfs dfs -get /user/hive/warehouse/student/student.txt /opt/bigdata/data
```

### 3. hive shell 命令导出

* hive -e "sql语句" >   file

* hive -f  sql文件   >    file

  ```sql
  bin/hive -e 'select * from default.student;' > /opt/bigdata/data/student1.txt
  -- >是覆盖，>>是追加
  ```

### 4.在HDFS上，导入导出操作**export、import**

```sql
create table student2 like student1;
export table student1  to   '/export/student1';
import table student2  from '/export/student1';
```

##  五、文件上传HDFS，如何数据关联

1. 上传数据后修复液

   dfs -mkdir -p 分区表名

   dfs -put 分区目录

   msck repair table 表名

2. 上传数据后添加分区

   dfs -mkdir -p 分区表名

   dfs -put 分区目录

   alter table 表明 add partition();

## 六、静态分区与动态分区

### 1.静态分区

* 表的分区字段的值需要开发人员手动给定

  1. 创建分区表

     ```sql
     create table order_partition(
     order_number string,
     order_price  double,
     order_time string
     )
     partitioned BY(month string)
     row format delimited fields terminated by '\t';
     ```

  2. 准备数据order_created.txt

     ```
       10001	100	2019-03-02
       10002	200	2019-03-02
       10003	300	2019-03-02
       10004	400	2019-03-03
       10005	500	2019-03-03
       10006	600	2019-03-03
       10007	700	2019-03-04
       10008	800	2019-03-04
       10009	900	2019-03-04
     ```

  3.  加载到分区表

     ```sql
         load data local inpath '/opt/bigdata/data/order_created.txt' overwrite into table order_partition partition(month='2019-03');
     ```

  4. 查询结果

     ```sql
       select * from order_partition where month='2019-03';
         
       10001   100.0   2019-03-02      2019-03
       10002   200.0   2019-03-02      2019-03
       10003   300.0   2019-03-02      2019-03
       10004   400.0   2019-03-03      2019-03
       10005   500.0   2019-03-03      2019-03
       10006   600.0   2019-03-03      2019-03
       10007   700.0   2019-03-04      2019-03
       10008   800.0   2019-03-04      2019-03
       10009   900.0   2019-03-04      2019-03
     ```

### 2. 动态分区

* 按照需求实现把数据自动导入到表的不同分区中

  1. 创建表

     ```sql
     --创建普通表
     create table t_order(
         order_number string,
         order_price  double, 
         order_time   string
     )row format delimited fields terminated by '\t';
     
     --创建目标分区表
     create table order_dynamic_partition(
         order_number string,
         order_price  double    
     )partitioned BY(order_time string)
     row format delimited fields terminated by '\t';
     ```

  2. 准备数据order_created.txt

     ```sql
     10001	100	2019-03-02 
     10002	200	2019-03-02
     10003	300	2019-03-02
     10004	400	2019-03-03
     10005	500	2019-03-03
     10006	600	2019-03-03
     10007	700	2019-03-04
     10008	800	2019-03-04
     10009	900	2019-03-04
     ```

  3. 向普通表t_order加载数据

     ```sql
     load data local inpath '/opt/bigdata/data/order_created.txt' overwrite into table t_order;
     ```

  4. 动态加载到分区表中

     ```sql
     ---要想进行动态分区，需要设置参数
     hive> set hive.exec.dynamic.partition=true; //使用动态分区
     -- 它的默认值是strick，即不允许分区列全部是动态的，这是为了防止用户有可能原意是只在子分区内进行动态建分区，但是由于疏忽忘记为主分区列指定值了，这将导致一个dml语句在短时间内创建大量的新的分区（对应大量新的文件夹），对系统性能带来影响。所以
     hive> set hive.exec.dynamic.partition.mode=nonstrict; //非严格模式
        
         insert into table order_dynamic_partition partition(order_time) select order_number,order_price,order_time from t_order;
         
     --注意字段查询的顺序，分区字段放在最后面。否则数据会有问题。
     ```
   ```
  
   ```
5. 查看分区
  
     ```sql
     show partitions order_dynamic_partition;
     ```
  
  6. 分区不是越多越好
     1. 分区有上线，默认1000；
     2. 过多分区意味这更多小文件，增加HDFS负担。
     3. 一个分区一个Map，增加jvm性能压力。

## 六、分桶表

* 分桶是对分区之下进行更细粒度的划分

* 分桶将整个数据内容安装某列属性值取hash值进行区分，具有相同hash值的数据进入到同一个文件中

* 可以理解为MapReduce中的Partitioner

* 作用

  1. 取样sampling更高效。没有分区的话需要扫描整个数据集。
  2. 提升某些查询操作效率，例如map、side、join

* 示例

  1. 系统配置

     ```
     set hive.enforce.bucketing=true;  开启对分桶表的支持
     set mapreduce.job.reduces=4; 设置与桶相同的reduce个数（默认只有一个reduce）
     ```

  2. 创建表

     ```sql
     --分桶表
     create table user_buckets_demo(id int, name string)
     clustered by(id) 
     into 4 buckets 
     row format delimited fields terminated by '\t';
     
     --普通表
     create table user_demo(id int, name string)
     row format delimited fields terminated by '\t';
     ```

  3. 加载数据到普通表

     ```sql
     load data local inpath '/opt/bigdata/data/buckets.txt' into table user_demo;
     ```

  4. 加载到分桶表

     ```sql
     insert into table user_buckets_demo select * from user_demo;
     ```

  5. 执行完成之后，Hadoop目录下会有多个文件

* 运用：

  tablesample抽样语句，语法：tablesample(bucket  x  out  of  y)

  * x表示从第几个桶开始取数据
  * y表示桶数的倍数，一共需要从 ==桶数/y==  个桶中取数据

  ```sql
  select * from user_buckets_demo tablesample(bucket 1 out of 2)
  
  -- 需要的总桶数=4/2=2个
  -- 先从第1个桶中取出数据
  -- 再从第1+2=3个桶中取出数据
  ```

  