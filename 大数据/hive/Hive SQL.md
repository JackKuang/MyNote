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

## 3. 高级查询

### 3.1 拆分数据：explode

* lateral view用于和split、explode等UDTF一起使用的，能将一行数据拆分成多行数据，在此基础上可以对拆分的数据进行聚合，lateral view首先为原始表的每行调用UDTF，UDTF会把一行拆分成一行或者多行，lateral view在把结果组合，产生一个支持别名表的虚拟表。
* 其中explode还可以用于将hive一列中复杂的array或者map结构拆分成多行。

```sql
-- array
SELECT explode(names) AS name FROM t1;

-- map
SELECT explode(name) AS (myKey,myValue) FROM t1;

-- string ex:a,b,c
SELECT explode(split(name,'m')) name FROM t1;

-- json string（）
-- 		1. 查询全部
select explode(split(regexp_replace(regexp_replace(sale_info,'\\[\\{',''),'}]',''),'},\\{')) as  sale_info from explode_lateral_view;
-- 		2. 查询monthSales字段（该sql会异常，UDTF explode不能写在别的函数内）
select get_json_object(explode(split(regexp_replace(regexp_replace(sale_info,'\\[\\{',''),'}]',''),'},\\{')),'$.monthSales') as  sale_info from explode_lateral_view;
```

* **LATERAL  VIEW**配合使用

  ```sql
  -- 使用LATERAL  VIEW能查询多个字段，
  select goods_id2,sale_info from explode_lateral_view
  LATERAL VIEW explode(split(goods_id,',')) goods as goods_id2;
  -- LATERAL VIEW explode(split(goods_id,',')) goods，相当于一张外摆哦
  
  -- 将json内容，转化为二维表，字段内容，转json，再get
  select get_json_object(concat('{',sale_info_1,'}'),'$.source') as source,
  get_json_object(concat('{',sale_info_1,'}'),'$.monthSales') as monthSales,
  get_json_object(concat('{',sale_info_1,'}'),'$.userCount') as monthSales, 
  get_json_object(concat('{',sale_info_1,'}'),'$.score') as monthSales from explode_lateral_view
  LATERAL VIEW explode(split(regexp_replace(regexp_replace(sale_info,'\\[\\{',''),'}]',''),'},\\{'))sale_info as sale_info_1;
  ```

### 3.2 行转列：CONCAT/CONCAT_WS/CONCAT_SET

* CONCAT(string A/col, string B/col…)：返回输入字符串连接后的结果，支持任意个输入字符串;

* CONCAT_WS(separator, str1, str2,...)：它是一个特殊形式的 CONCAT()。第一个参数剩余参数间的分隔符。分隔符可以是与剩余参数一样的字符串。如果分隔符是 NULL，返回值也将为 NULL。这个函数会跳过分隔符参数后的任何 NULL 和空字符串。分隔符将被加到被连接的字符串之间;

* COLLECT_SET(col)：函数只接受基本数据类型，它的主要作用是将某字段的值进行去重汇总，产生array类型字段。

  ```sql
  select
  	t1.base,
  	concat_ws('|', collect_set(t1.name)) name
  from
  	(select
       	name,
       	concat(constellation, "," , blood_type) base
       from
       	person_info) t1
  group by t1.base;
  -- 注意group by，它合并了相同的数据，而内部子查询却没有
  
  ```

### 3.3 列转行 explose

* EXPLODE(col)：将hive一列中复杂的array或者map结构拆分成多行。
* LATERAL VIEW，与上面讲的3.1基本一致
  * 用法：LATERAL VIEW udtf(expression) tableAlias AS columnAlias
  * 解释：用于和split, explode等UDTF一起使用，它能够将一列数据拆成多行数据，在此基础上可以对拆分后的数据进行聚合。

### 3.4 Reflect函数

* reflect函数可以支持在sql中调用java中的自带函数，秒杀一切udf函数。

  ```sql
  
  select reflect(class_name,method_name,col1,col2) from test_udf2;
  -- 调用
  select reflect("java.lang.Math","max",col1,col2) from test_udf;
  ```

* 使用apache commons中的函数，commons下的jar已经包含在hadoop的classpath中，所以可以直接使用。

  ```sql
  select reflect("org.apache.commons.lang.math.NumberUtils","isNumber","123")
  ```

### 3.5 窗口函数

* hive当中也带有很多的窗口函数以及分析函数，主要用于以下这些场景

  1. 用于分区排序
  2. 动态Group By 
  3. Top N 
  4. 累计计算 
  5. 层次查询

* 窗口函数

  * FIRST_VALUE：取分组内排序后，截止到当前行，第一个值
  * LAST_VALUE： 取分组内排序后，截止到当前行，最后一个值
  * LEAD(col,n,DEFAULT) ：用于统计窗口内往下第n行值。第一个参数为列名，第二个参数为往下第n行（可选，默认为1），第三个参数为默认值（当往下第n行为NULL时候，取默认值，如不指定，则为NULL） 
  * LAG(col,n,DEFAULT) ：与lead相反，用于统计窗口内往上第n行值。第一个参数为列名，第二个参数为往上第n行（可选，默认为1），第三个参数为默认值（当往上第n行为NULL时候，取默认值，如不指定，则为NULL）

* OVER 从句

  1. 使用标准的聚合函数COUNT、SUM、MIN、MAX、AVG 

  2. 使用PARTITION BY语句，使用一个或者多个原始数据类型的列 

  3. 使用PARTITION BY与ORDER BY语句，使用一个或者多个数据类型的分区或者排序列

  4. 使用窗口规范，窗口规范支持以下格式：

     ```sql
     (ROWS | RANGE) BETWEEN (UNBOUNDED | [num]) PRECEDING AND ([num] PRECEDING | CURRENT ROW | (UNBOUNDED | [num]) FOLLOWING)
     (ROWS | RANGE) BETWEEN CURRENT ROW AND (CURRENT ROW | (UNBOUNDED | [num]) FOLLOWING)
     (ROWS | RANGE) BETWEEN [num] FOLLOWING AND (UNBOUNDED | [num]) FOLLOWING
     -- 当ORDER BY后面缺少窗口从句条件，窗口规范默认是 RANGE BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW.
     -- 当ORDER BY和窗口从句都缺失, 窗口规范默认是 ROW BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING.
     -- OVER从句支持以下函数， 但是并不支持和窗口一起使用它们。 
     -- Ranking函数: Rank, NTile, DenseRank, CumeDist, PercentRank. Lead 和 Lag 函数.
     ```
     
  5. 示例：

     * 求范围内sum
  
      ```sql
     --  select 
      user_id,
      user_type,
      sales,
      --分组内所有行
      sum(sales) over(partition by user_type) AS sales_1 ,
      sum(sales) over(order  by user_type) AS sales_2 , 
      --默认为从起点到当前行，如果sales相同，累加结果相同
      sum(sales) over(partition by user_type order by sales asc) AS sales_3,
      --从起点到当前行，结果与sales_3不同。 根据排序先后不同，可能结果累加不同
      sum(sales) over(partition by user_type order by sales asc rows between unbounded preceding and current row) AS sales_4,
      --当前行+往前3行 
      sum(sales) over(partition by user_type order by sales asc rows between 3 preceding and current row) AS sales_5,
      --当前行+往前3行+往后1行
      sum(sales) over(partition by user_type order by sales asc rows between 3 preceding and 1 following) AS sales_6,
      --当前行+往后所有行 
      sum(sales) over(partition by user_type order by sales asc rows between current row and unbounded following) AS sales_7 
      from 
      order_detail 
      order by 
          user_type,
          sales,
          user_id;
     -- 注意:
      -- 结果和ORDER BY相关,默认为升序
     -- 如果不指定ROWS BETWEEN,默认为从起点到当前行;
     -- 如果不指定ORDER BY，则将分组内所有值累加;
     
     关键是理解ROWS BETWEEN含义,也叫做WINDOW子句：
     -- PRECEDING：往前
     -- FOLLOWING：往后
     -- CURRENT ROW：当前行
     -- UNBOUNDED：无界限（起点或终点）
     -- UNBOUNDED PRECEDING：表示从前面的起点 
     -- UNBOUNDED FOLLOWING：表示到后面的终点
     -- 其他COUNT、AVG，MIN，MAX，和SUM用法一样。
      ```
     * 使用first_value和last_value求分组后的第一个和最后一个值
  
     ```sql
     select 
         user_id,
         user_type,
         ROW_NUMBER() OVER(PARTITION BY user_type ORDER BY sales) AS row_num,  
         first_value(user_id) over (partition by user_type order by sales desc) as max_sales_user,
         first_value(user_id) over (partition by user_type order by sales asc) as min_sales_user,
         last_value(user_id) over (partition by user_type order by sales desc) as curr_last_min_user,
         last_value(user_id) over (partition by user_type order by sales asc) as curr_last_max_user
     from 
         order_detail;
     ```

### 3.6 分析函数

1. ROW_NUMBER()：

   从1开始，按照顺序，生成分组内记录的序列,比如，按照pv降序排列，生成分组内每天的pv名次,ROW_NUMBER()的应用场景非常多，再比如，获取分组内排序第一的记录;获取一个session中的第一条refer等。 

2. RANK() ：

   生成数据项在分组中的排名，排名相等会在名次中留下空位 

3. DENSE_RANK() ：

   生成数据项在分组中的排名，排名相等会在名次中不会留下空位 

   ```sql
   select 
       user_id,user_type,sales,
       -- 顺序
       ROW_NUMBER() over (partition by user_type order by sales desc) as rn,
       -- 排名 1,2,2,4
       RANK() over (partition by user_type order by sales desc) as r,
       -- 排名 1,2,2,3
       DENSE_RANK() over (partition by user_type order by sales desc) as dr
   from
       order_detail;  
   ```

   

4. CUME_DIST ：

   小于等于当前值的行数/分组内总行数。比如，统计小于等于当前薪水的人数，所占总人数的比例 

5. PERCENT_RANK ：

   分组内当前行的RANK值-1/分组内总行数-1 

6. NTILE(n) ：

   用于将分组数据按照顺序切分成n片，返回当前切片值，如果切片不均匀，默认增加第一个切片的分布。NTILE不支持ROWS BETWEEN。

   ```sql
   select 
       user_type,sales,
       --分组内将数据分成2片
       NTILE(2) OVER(PARTITION BY user_type ORDER BY sales) AS nt2,
       --分组内将数据分成3片    
       NTILE(3) OVER(PARTITION BY user_type ORDER BY sales) AS nt3,
       --分组内将数据分成4片    
       NTILE(4) OVER(PARTITION BY user_type ORDER BY sales) AS nt4,
       --将所有数据分成4片
       NTILE(4) OVER(ORDER BY sales) AS all_nt4
   from 
     
   order by 
       user_type,
       sales;
   -- nt2，nt3，nt3，all_nt4都是所在组的顺序，如1,2,3,4等
   
   -- 使用NTILE求取sales前20%的用户id
   select
       user_id
   from
   (select  user_id, NTILE(5) OVER(ORDER BY sales desc) AS nt
       from  order_detail
   )A
   where nt=1;
   ```

### 3.7 增强的聚合Grouping、Cuhe、Rollup

这几个分析函数通常用于OLAP中，不能累加，而且需要根据不同维度上钻和下钻的指标统计，比如，分小时、天、月的UV数。

GROUPING SETS 在一个GROUP BY查询中，根据不同的维度组合进行聚合，等价于将不同维度的GROUP BY结果集进行UNION ALL, 其中的GROUPING__ID，表示结果属于哪一个分组集合。

* 按照user_type和sales分别进行分组求取数据

  ```sql
  
  -- 以user_type、sales、user_type+sale作为一个组来统计select
      user_type,
      sales,
      count(user_id) as pv,
      GROUPING__ID 
  from 
      order_detail
  group by 
      user_type,sales
  GROUPING SETS(user_type,sales) 
  ORDER BY 
      GROUPING__ID;
  -- 以user_type、sales作为一个组来统计，两个gorup by
  ```

* 按照user_type，sales，以及user_type + salse  分别进行分组求取统计数据

   ```sql
  select
      user_type,
      sales,
      count(user_id) as pv,
      GROUPING__ID 
  from 
      order_detail
  group by 
      user_type,sales
  GROUPING SETS(user_type,sales,(user_type,sales)) 
  ORDER BY 
      GROUPING__ID;
  -- 以user_type、sales、user_type+sale作为一个组来统计，三个gorup by
  
  ----------------------------------------------------------------------
  select name, work_space[0] as main_place, count(employee_id) as emp_id_cnt
  from employee
  group by name, work_space[0]
  GROUPING SETS((name,work_space[0]), name, ());
   
  // 上面语句与下面语句等效
   
  select name, work_space[0] as main_place, count(employee_id) as emp_id_cnt
  from employee
  group by name, work_space[0]
  UNION ALL
  select name, work_space[0] as main_place, count(employee_id) as emp_id_cnt
  from employee
  group by name
  UNION ALL
  select name, work_space[0] as main_place, count(employee_id) as emp_id_cnt
  ```

* #### cube进行聚合

  不进行任何的分组，按照user_type进行分组，按照sales进行分组，按照user_type+sales进行分组求取统计数据

  ```sql
  select
      user_type,
      sales,
      count(user_id) as pv,
      GROUPING__ID 
  from 
      order_detail
  group by 
      user_type,sales
  WITH CUBE 
  ORDER BY 
      GROUPING__ID;
      
  -------------------------------------------------------
  select a, b, c from table group by a, b, c WITH ROLLUP;
  // 等价于下面语句
  select a, b, c from table group by a, b, c
  GROUPING SETS((a,b,c),(a,b),(a),());
  ```

* #### ROLLUP进行聚合

  rollup是CUBE的子集，以最左侧的维度为主，从该维度进行层级聚合。

  ```sql
  select
      user_type,
      sales,
      count(user_id) as pv,
      GROUPING__ID 
  from 
      order_detail
  group by 
      user_type,sales
  WITH ROLLUP 
  ORDER BY 
      GROUPING__ID;
  
  -----------------------------------------
  A  NULL 7 0
  B  NULL 3 0
  C  NULL 4 0
  A  1    1 
  
  -------------------------------------------------------
  select a, b, c from table group by a, b, c WITH ROLLUP;
  // 等价于下面语句
  select a, b, c from table group by a, b, c
  GROUPING SETS((a,b,c),(a,b),(a,c),(b,c),(a),(b),(c),());
  ```

## 4. 内置函数与自定义函数