# 基本查询

## 1. 共同点

​	sql语言之间，语言特性基本一致

## 2. 特殊点

### 2.1 order by全局排序

​	这一点上，与其他数据库效果基本一致。

### 2.2 sort by 局部排序

​	sort by：每个reduce内部进行排序，对全局结果来说，并不是排序。

 1. 设置reduce个数：

    ```sql
    -- 设置reduce个数
    set mapreduce.job.reduces=3;
    -- 查看reduce个数
    set mapreduce.job.reduces;
    ```

	2. 查询排序，默认升序

    ```sql
    select * from table_name sort by col;
    ```

	3. 将结果导入到文件种

    ```sql
    insert overwrite local directory '/home/' select * from table_name sort by col;
    ```

### 2.3 distribute by 分区排序

​	类似MR种partition，采用hash算法，在Map端将查询结果hash值相同的结果分发到对应的reduce文件中。

​	Hive要求distribute by语句要卸载sort by语句之前。

```sql
insert overwrite local directory '/home/' select * from table_name distibute by col1 sort by col2;
```

### 2.4 cluster by

当distribute by和sort by字段相同时，可以使用cluster by的方式。

```sql
-- 两种写法一直
insert overwrite local directory '/home/' select * from table_name distibute by col1 sort by col1;

insert overwrite local directory '/home/' select * from table_name cluster by col1;
```

### 2.5 case... when...

两种写法

```
select *,
case sex
when "female" then 1 
when "male" then 0
end as flag 
from employee;

select *,
case 
when salary < 5000 then "低等收入" 
when salary>= 5000 and salary < 10000 then "中等收入"
when salary > 10000 then "高等收入"  
end  as level 
from employee;
```

### 2.6 避免MapReduce

Hive为了执行效率，简单的查询（select），不带count、sum这种复杂语句，不走map/reduce，直接读取hdfs文件进行filter进行过滤。（本地模式）

* 直接的查询不会进行Mapreduce：

  ```sql
  select * from table_name;
  ```

* 查询条件的过滤条件时分区字段也不会mapReduce

  ```sql
  select * from table_name_parti where month = '2019-08'
  ```

* 设置`set hive.exec.mode.local.auto=true;`hive还是会尝试本地模式。
* 当一个job满足如下条件才能真正使用本地模式：
  1. job的输入数据大小必须小于参数：hive.exec.mode.local.auto.inputbytes.max(默认128MB)
  2. job的map数必须小于参数：hive.exec.mode.local.auto.tasks.max(默认4)，（一个文件一个map）
  3. job的reduce数必须为0或者1