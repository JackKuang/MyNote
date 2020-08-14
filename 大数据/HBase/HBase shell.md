### HBase常用shell操作

#### 1、进入Hbase客户端命令操作界面

~~~shell
hbase shell
~~~



#### 2、查看帮助命令

~~~ruby
hbase(main):001:0> help
~~~



#### 3、查看当前数据库中有哪些表

~~~ruby
hbase(main):006:0> list
~~~



#### 4、创建一张表

* 创建user表， 包含base_info、extra_info两个列族

~~~ruby
    hbase(main):007:0> create 'user', 'base_info', 'extra_info'

或者

create 'user', {NAME => 'base_info', VERSIONS => '3'}，{NAME => 'extra_info'}
~~~



#### 5、添加数据操作

* 向user表中插入信息，row key为 rk0001，列族base_info中添加name列标示符，值为zhangsan

~~~ruby
hbase(main):008:0> put 'user', 'rk0001', 'base_info:name', 'zhangsan'
~~~

* 向user表中插入信息，row key为rk0001，列族base_info中添加gender列标示符，值为female

~~~ruby
hbase(main):009:0> put 'user', 'rk0001', 'base_info:gender', 'female'
~~~

* 向user表中插入信息，row key为rk0001，列族base_info中添加age列标示符，值为20

~~~ruby
hbase(main):010:0>  put 'user', 'rk0001', 'base_info:age', 20
~~~

* 向user表中插入信息，row key为rk0001，列族extra_info中添加address列标示符，值为beijing

~~~ruby
hbase(main):011:0> put 'user', 'rk0001', 'extra_info:address', 'beijing'
~~~





#### 6、查询数据

##### 6.1 通过rowkey进行查询

* 获取user表中row key为rk0001的所有信息

~~~ruby
hbase(main):006:0> get 'user', 'rk0001'
~~~



##### 6.2 查看rowkey下面的某个列族的信息

* 获取user表中row key为rk0001，base_info列族的所有信息

~~~ruby
hbase(main):007:0> get 'user', 'rk0001', 'base_info'
~~~



##### 6.3 查看rowkey指定列族指定字段的值

* 获取user表中row key为rk0001，base_info列族的name、age列标示符的信息

~~~ruby
hbase(main):008:0> get 'user', 'rk0001', 'base_info:name', 'base_info:age'
~~~



##### 6.4 查看rowkey指定多个列族的信息

* 获取user表中row key为rk0001，base_info、extra_info列族的信息

~~~ruby
hbase(main):010:0> get 'user', 'rk0001', 'base_info', 'extra_info'

或者

hbase(main):011:0> get 'user', 'rk0001', {COLUMN => ['base_info', 'extra_info']}

或者
hbase(main):012:0> get 'user', 'rk0001', {COLUMN => ['base_info:name', 'extra_info:address']}
~~~

##### 6.5 指定rowkey与列值查询

* 获取user表中row key为rk0001，cell的值为zhangsan的信息

~~~ruby
hbase(main):013:0> get 'user', 'rk0001', {FILTER => "ValueFilter(=, 'binary:zhangsan')"}
~~~

##### 6.6 指定rowkey与列值模糊查询

* 获取user表中row key为rk0001，列标示符中含有a的信息

~~~ruby
hbase(main):015:0> get 'user', 'rk0001', {FILTER => "(QualifierFilter(=,'substring:a'))"}
~~~

##### 6.7 继续插入一批数据

~~~ruby
hbase(main):016:0> put 'user', 'rk0002', 'base_info:name', 'fanbingbing'

hbase(main):017:0> put 'user', 'rk0002', 'base_info:gender', 'female'

hbase(main):018:0> put 'user', 'rk0002', 'base_info:birthday', '2000-06-06'

hbase(main):019:0> put 'user', 'rk0002', 'extra_info:address', 'shanghai'
~~~

##### 6.8 查询所有数据

* 查询user表中的所有信息

~~~ruby
hbase(main):020:0> scan 'user'
~~~



##### 6.9 列族查询

* 查询user表中列族为 base_info 的信息

~~~ruby
hbase(main):021:0> scan 'user', {COLUMNS => 'base_info'}


hbase(main):022:0> scan 'user', {COLUMNS => 'base_info', RAW => true, VERSIONS => 5}

## Scan时可以设置是否开启Raw模式,开启Raw模式会返回包括已添加删除标记但是未实际删除的数据
## VERSIONS指定查询的最大版本数
~~~



##### 6.10 多列族查询

* 查询user表中列族为info和data的信息

~~~ruby
hbase(main):023:0> scan 'user', {COLUMNS => ['base_info', 'extra_info']}
hbase(main):024:0> scan 'user', {COLUMNS => ['base_info:name', 'extra_info:address']}

~~~



##### 6.11 指定列族与某个列名查询

* 查询user表中列族为base_info、列标示符为name的信息

~~~ruby
hbase(main):025:0> scan 'user', {COLUMNS => 'base_info:name'}
~~~



##### 6.12 指定列族与列名以及限定版本查询

* 查询user表中列族为base_info、列标示符为name的信息,并且版本最新的5个

~~~ruby
hbase(main):026:0> scan 'user', {COLUMNS => 'base_info:name', VERSIONS => 5}
~~~



##### 6.13 指定多个列族与按照数据值模糊查询

* 查询**user**表中列族为 **base_info** 和 **extra_info**且列标示符中含有a字符的信息

~~~ruby
hbase(main):027:0> scan 'user', {COLUMNS => ['base_info', 'extra_info'], FILTER => "(QualifierFilter(=,'substring:a'))"}
~~~



##### 6.14 rowkey的范围值查询

* 查询user表中列族为info，rk范围是[rk0001, rk0003)的数据

~~~ruby
hbase(main):028:0> scan 'user', {COLUMNS => 'base_info', STARTROW => 'rk0001', ENDROW => 'rk0003'}
~~~



##### 6.15 指定rowkey模糊查询

* 查询user表中row key以rk字符开头的

~~~ruby
hbase(main):029:0> scan 'user',{FILTER=>"PrefixFilter('rk')"}
~~~



#### 7、更新数据

* 更新操作同插入操作一模一样，只不过有数据就更新，没数据就添加


##### 7.1 更新数据值

* 把user表中rowkey为rk0001的base_info列族下的列name修改为zhangsansan

~~~ruby
hbase(main):030:0> put 'user', 'rk0001', 'base_info:name', 'zhangsansan'
~~~



##### 7.2 更新版本号

* 将user表的 **base_info** 列族版本号改为5

~~~ruby
hbase(main):031:0> alter 'user', NAME => 'base_info', VERSIONS => 5
~~~





#### 8、删除数据和表

##### 8.1 指定rowkey以及列名进行删除

* 删除user表row key为rk0001，列标示符为 base_info:name 的数据

~~~ruby
hbase(main):032:0>  delete 'user', 'rk0001', 'base_info:name' 
~~~



##### 8.2 指定rowkey，列名以及字段值进行删除

* 删除user表row key为rk0001，列标示符为base_info:name，timestamp为1392383705316的数据

~~~ruby
hbase(main):033:0> delete 'user', 'rk0001', 'base_info:age', 1564745324798
~~~



##### 8.3 删除列族

* 删除 base_info 列族

~~~ruby
hbase(main):034:0> alter 'user', NAME => 'base_info', METHOD => 'delete'

或者

hbase(main):035:0> alter 'user', 'delete' => 'base_info'
~~~



##### 8.4 清空表数据

* 删除user表数据

~~~ruby
hbase(main):036:0> truncate 'user'
~~~



##### 8.5 删除表

* 删除user表

~~~ruby
#先disable  再drop

hbase(main):036:0> disable 'user'
hbase(main):037:0> drop 'user'

#如果不进行disable，直接drop会报错
ERROR: Table user is enabled. Disable it first.
~~~

