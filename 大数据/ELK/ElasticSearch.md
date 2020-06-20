# ElasticSearch

## 一、介绍

* Elaticsearch，简称为es， es是一个开源的高扩展的分布式全文检索引擎，它可以近乎实时的存储、检索数据；本身扩展性很好，可以扩展到上百台服务器，处理PB级别的数据。es也使用Java开发并使用Lucene作为其核心来实现所有索引和搜索的功能，但是它的目的是通过简单的RESTful API来隐藏Lucene的复杂性，从而让全文搜索变得简单。

## 二、ElasticSearch使用案例

* 2013年初，GitHub抛弃了Solr，采取ElasticSearch 来做PB级的搜索。 “GitHub使用ElasticSearch搜索20TB的数据，包括13亿文件和1300亿行代码”
* 维基百科：启动以elasticsearch为基础的核心搜索架构
* SoundCloud：“SoundCloud使用ElasticSearch为1.8亿用户提供即时而精准的音乐搜索服务”
* 百度：百度目前广泛使用ElasticSearch作为文本数据分析，采集百度所有服务器上的各类指标数据及用户自定义数据，通过对各种数据进行多维分析展示，辅助定位分析实例异常或业务层面异常。目前覆盖百度内部20多个业务线（包括casio、云分析、网盟、预测、文库、直达号、钱包、风控等），单集群最大100台机器，200个ES节点，每天导入30TB+数据
* 新浪使用ES 分析处理32亿条实时日志
* 阿里使用ES 构建挖财自己的日志采集和分析体系

## 三、ElasticSearch安装

### 3.1 ElasticSearch安装

* 注意：ES不能使用root用户来启动，需要创建用户来执行操作。

* 官网下载解压

* 修改配置文件elastocsearch.yaml

  ```yaml
  #vim config/elastocsearch.yaml
  cluster.name: myes
  node.name: node01
  path.data: /kkb/install/elasticsearch-6.7.0/datas
  path.logs: /kkb/install/elasticsearch-6.7.0/logs
  network.host: 192.168.0.2
  http.port: 9200
  discovery.zen.ping.unicast.hosts: ["node01", "node02", "node03"]
  bootstrap.system_call_filter: false
  bootstrap.memory_lock: false
  http.cors.enabled: true
  http.cors.allow-origin: "*"
  ```

* 修改jvm.options

  ```sh
  # vim config/jvm.options
  
  -Xms2g
  -Xmx2g
  ```

* 项目发送到其他节点服务器

  ```sh
  scp ...
  ```

* 修改其他节点服务名称

  ```yaml
  #vim config/elastocsearch.yaml
  node.name: node02
  network.host: 192.168.0.3
  ```

* 修改系统配置

  ```sh
  # 解除打开文件数据的限制
  sudo vi /etc/security/limits.conf
  * soft nofile 65536
  * hard nofile 131072
  * soft nproc 2048
  * hard nproc 4096
  # 启动线程数限制
  sudo vi /etc/sysctl.conf
  vm.max_map_count=655360
  fs.file-max=655360
  # 启动生效
  sudo sysctl -p
  # 重新连接linux服务器
  # 验证结果
  [hadoop@node01 ~]$ ulimit -Hn
  131072
  [hadoop@node01 ~]$ ulimit -Sn
  65536
  [hadoop@node01 ~]$ ulimit -Hu
  4096
  [hadoop@node01 ~]$ ulimit -Su
  4096
  ```

* 执行启动

  ```sh
  nohup /bin/elasticsearch 2>&1 &
  ```

* 访问http://node01:9200

### 3.2 ElasticSearch-Head安装

* 克隆编译包并进行安装

  ```sh
  git clone https://github.com/mobz/elasticsearch-head.git
  # 进入安装目录
  cd elasticsearch-head
  # intall 才会有 node-modules
  npm install
  ```

* 修改Gruntfile.js

  ```sh
  # vim Gruntfile.js
  # 搜索hostname，修改到node01节点
  ```

* 修改app.js

  ```sh
  # vim _site/app.js
  # 更改 http://localhost:9200 为 http://node01:9200
  ```

* 启动

  ```
  nohup node_modules/grunt/bin/grunt server >/dev/null 2>&1 &
  ```

* 访问http://node01:9100

### 3.3 Kibana安装

* 下载解压

* 修改kibana.yml

  ```sh
  # vim config/kibana.yaml
  server.host: "node01"
  elasticsearch.hosts: ["http://node01:9200"]
  ```

* 启动

  ```
  nohup bin/kibana >/dev/null 2>&1 &
  ```

* 访问http://node01:5601

### 3.1 Docker 安装部署（Deployment）

```sh
# ElasticSearch
docker pull elasticsearch:7.8.0
docker rm -f elasticsearch
docker run -d --name elasticsearch -v /opt/docker/elasticsearch/data:/usr/share/elasticsearch/data -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:7.8.0

# elasticsearch-head
docker pull mobz/elasticsearch-head:5
docker rm -f elasticsearch-head
docker run -d --name elasticsearch-head -p 9100:9100 docker.io/mobz/elasticsearch-head:5

# kibana
docker pull kibana:7.8.0
docker rm -f kibana
# --link A:B
# 容器中域名B地址将会解析到A容器地址
# 就算重新创建容器，网络不一致，也可以网络通信
docker run -d --name kibana --link elasticsearch:elasticsearch -p 5601:5601 kibana:7.8.0
```

* 如果elasticsearch-head无法连接ElasticSearch集群，因为跨域，修改浏览器，允许跨域即可。

## 四、ElasticSearch核心概念

### 4.1 概述

Elasticsearch是面向文档(document oriented)的，这意味着它可以存储整个对象或文档(document)。然而它不仅仅是存储，还会索引(index)每个文档的内容使之可以被搜索。在Elasticsearch中，你可以对文档（而非成行成列的数据）进行索引、搜索、排序、过滤。

Elasticsearch比传统关系型数据库如下：

Relational DB -> Databases -> Tables -> Rows -> Columns
Elasticsearch -> Indices   -> Types  -> Documents -> Fields

#### 4.1.1 索引 index

* 一个索引就是一个拥有几分相似特征的文档的集合。比如说，你可以有一个客户数据的索引，另一个产品目录的索引，还有一个订单数据的索引。一个索引由一个名字来标识（必须全部是小写字母的），并且当我们要对对应于这个索引中的文档进行索引、搜索、更新和删除的时候，都要使用到这个名字。在一个集群中，可以定义任意多的索引。

#### 4.1.2 类型 type

* 在一个索引中，你可以定义一种或多种类型。一个类型是你的索引的一个逻辑上的分类/分区，其语义完全由你来定。通常，会为具有一组共同字段的文档定义一个类型。比如说，我们假设你运营一个博客平台并且将你所有的数据存储到一个索引中。在这个索引中，你可以为用户数据定义一个类型，为博客数据定义另一个类型，当然，也可以为评论数据定义另一个类型。

#### 4.1.3 文档 doucument

* 一个文档是一个可被索引的基础信息单元。比如，你可以拥有某一个客户的文档，某一个产品的一个文档，当然，也可以拥有某个订单的一个文档。文档以JSON（Javascript Object Notation）格式来表示，而JSON是一个到处存在的互联网数据交互格式。

  在一个index/type/type

#### 4.1.4 字段 field

* 相当于是数据表的字段，对文档数据根据不同属性进行的分类标识

#### 4.1.5 映射 mapping

* mapping是处理数据的方式和规则方面做一些限制，如某个字段的数据类型、默认值、分析器、是否被索引等等，这些都是映射里面可以设置的，其它就是处理es里面数据的一些使用规则设置也叫做映射，按着最优规则处理数据对性能提高很大，因此才需要建立映射，并且需要思考如何建立映射才能对性能更好。 

#### 4.1.6 接近实时 NRT

* ####  Elasticsearch是一个接近实时的搜索平台。这意味着，从索引一个文档直到这个文档能够被搜索到有一个轻微的延迟（通常是1秒以内）

#### 4.1.7 集群 cluster

* 一个集群就是由一个或多个节点组织在一起，它们共同持有整个的数据，并一起提供索引和搜索功能。一个集群由一个唯一的名字标识，这个名字默认就是“elasticsearch”。这个名字是重要的，因为一个节点只能通过指定某个集群的名字，来加入这个集群.

#### 4.1.8 节点 node

* 一个节点是集群中的一个服务器，作为集群的一部分，它存储数据，参与集群的索引和搜索功能。和集群类似，一个节点也是由一个名字来标识的，默认情况下，这个名字是一个随机的漫威漫画角色的名字，这个名字会在启动的时候赋予节点。这个名字对于管理工作来说挺重要的，因为在这个管理过程中，你会去确定网络中的哪些服务器对应于Elasticsearch集群中的哪些节点。
* 一个节点可以通过配置集群名称的方式来加入一个指定的集群。默认情况下，每个节点都会被安排加入到一个叫做“elasticsearch”的集群中，这意味着，如果你在你的网络中启动了若干个节点，并假定它们能够相互发现彼此，它们将会自动地形成并加入到一个叫做“elasticsearch”的集群中。
* 在一个集群里，只要你想，可以拥有任意多个节点。而且，如果当前你的网络中没有运行任何Elasticsearch节点，这时启动一个节点，会默认创建并加入一个叫做“elasticsearch”的集群。

#### 4.1.9 分片和复制 shards & replicas

* Elasticsearch提供了将索引划分成多份的能力，这些份就叫做分片。当你创建一个索引的时候，你可以指定你想要的分片的数量。每个分片本身也是一个功能完善并且独立的“索引”，这个“索引”可以被放置到集群中的任何节点上。分片很重要，主要有两方面的原因： 
  * 允许你**水平分割/扩展你的内容容量**。
  * 允许你在分片（潜在地，位于多个节点上）之上进行**分布式的、并行的操作**，进而提高性能/吞吐量。
* 至于一个分片怎样分布，它的文档怎样聚合回搜索请求，是完全由Elasticsearch管理的，对于作为用户的你来说，这些都是透明的。
* 在一个网络/云的环境里，失败随时都可能发生，在某个分片/节点不知怎么的就处于离线状态，或者由于任何原因消失了，这种情况下，有一个故障转移机制是非常有用并且是强烈推荐的。为此目的，Elasticsearch允许你创建分片的一份或多份拷贝，这些拷贝叫做**复制**分片。

* 复制之所以重要，有两个主要原因： 
  * 在分片/节点失败的情况下，提供了**高可用性**。因为这个原因，注意到复制分片从不与原/主要（original/primary）分片置于同一节点上是非常重要的。
  * 扩展你的**搜索量/吞吐量**，因为搜索可以在所有的复制上并行运行。
* 每个索引可以被分成多个分片。一个索引也可以被复制0次（意思是没有复制）或多次。一旦复制了，每个索引就有了主分片（作为复制源的原来的分片）和复制分片（主分片的拷贝）之别。**分片和复制的数量可以在索引创建的时候指定。在索引创建之后，你可以在任何时候动态地改变复制的数量，但是你事后不能改变分片的数量。**
* 默认情况下，Elasticsearch中的每个索引被分片5个主分片和1个复制，这意味着，如果你的集群中至少有两个节点，你的索引将会有5个主分片和另外5个复制分片（1个完全拷贝），这样的话每个索引总共就有10个分片。

## 五、管理索引

### 5.1 创建索引

```sh
# 创建名称为blog的索引
PUT /blog/?pretty
```

### 5.2 插入文档

```sh
# 在blog下，article类型中添加ID的为1的数据
PUT /blog/article/1?pretty
{"id": "1", "title": "What is lucene"}
```



### 5.3 查询文档

```sh
# 在blog下，article类型中查询ID的为1的数据
GET /blog/article/1?pretty
```

### 5.4 更新文档

```sh
## 在blog下，article类型中更新ID的为1的数据
PUT /blog/article/1?pretty
{"id": "1", "title": "What is lucene 2"}
```

### 5.5 搜索文档

```sh
# 在blog下，article类型中搜索id的为1的数据
GET blog/article/_search?q=id:1
# 在blog下，article类型中搜索title的为lucene的数据
GET blog/article/_search?q=title:lucene
```

### 5.6 删除文档

```sh
## 在blog下，article类型中删除ID的为1的数据
DELETE /blog/article/1?pretty
```

### 5.7 删除索引

```
# 删除名称为blog的索引
DELETE /blog/?pretty
```

## 六、数据查询

```sh
PUT /school
POST /school/student/_bulk
{ "index": { "_id": 1 }}
{ "name" : "liubei", "age" : 20 , "sex": "boy", "birth": "1996-01-02" , "about": "i like diaocan he girl" }
{ "index": { "_id": 2 }}
{ "name" : "guanyu", "age" : 21 , "sex": "boy", "birth": "1995-01-02" , "about": "i like diaocan" }
{ "index": { "_id": 3 }}
{ "name" : "zhangfei", "age" : 18 , "sex": "boy", "birth": "1998-01-02" , "about": "i like travel" }
{ "index": { "_id": 4 }}
{ "name" : "diaocan", "age" : 20 , "sex": "girl", "birth": "1996-01-02" , "about": "i like travel and sport" }
{ "index": { "_id": 5 }}
{ "name" : "panjinlian", "age" : 25 , "sex": "girl", "birth": "1991-01-02" , "about": "i like travel and wusong" }
{ "index": { "_id": 6 }}
{ "name" : "caocao", "age" : 30 , "sex": "boy", "birth": "1988-01-02" , "about": "i like xiaoqiao" }
{ "index": { "_id": 7 }}
{ "name" : "zhaoyun", "age" : 31 , "sex": "boy", "birth": "1997-01-02" , "about": "i like travel and music" }
{ "index": { "_id": 8 }}
{ "name" : "xiaoqiao", "age" : 18 , "sex": "girl", "birth": "1998-01-02" , "about": "i like caocao" }
{ "index": { "_id": 9 }}
{ "name" : "daqiao", "age" : 20 , "sex": "girl", "birth": "1996-01-02" , "about": "i like travel and history" }

```



### 6.1 查询 Query DSL

* 在上下文查询语境中，查询语句会询问文档与查询语句的匹配程度，它会判断文档是否匹配并计算相关性评分（_score）的值。例如：
  - 查找与 `full text search` 这个词语最佳匹配的文档
  - 查找包含单词 `run`，但是也包含`runs`, `running`, `jog` 或 `sprint`的文档
  - 同时包含着 `quick`, `brown` 和`fox`--- 单词间离得越近，该文档的相关性越高
  - 标识着 `lucene`, `search` 或 `java`--- 标识词越多，该文档的相关性越高

#### 6.1.1 match_all

* 通过match_all匹配后，会把所有的数据检索出来，容易导致GC。

```sh
# 把所有的数据检索出来
GET /school/student/_search?pretty
{
    "query": {
        "match_all": {}
    }
}
```

#### 6.1.2 match

* 关键子搜索查询

```sh
# 检索喜欢旅游的人
GET /school/student/_search?pretty
{
    "query": {
         "match": {"about": "travel"}
    }
}
```

#### 6.1.3 term/terms

* 使用term进行精确匹配（比如数字，日期，布尔值或 not_analyzed的字符串(未经分析的文本数据类型)）
* 使用terms匹配多个值
  * term主要是用于精确的过滤，比如说：”我爱你”
    * 在match下面匹配可以为包含：我、爱、你、我爱等等的解析器
    * 在term语法下面就精准匹配到：”我爱你”

```sh
# 使用term进行精确匹配
GET /school/student/_search?pretty
{
"query": {
   "bool": {
      "must": { "term": {"about": "travel"}},
      "should": {"term": {"sex": "boy"}}         
     }}
}
# 使用terms匹配多个值
GET /school/student/_search?pretty
{
"query": {
   "bool": {
      "must": { "terms": {"about": ["travel","history"]}}          
     }
  }
}

```

#### 6.1.4 range

* Range过滤允许我们按照指定的范围查找一些数据：操作范围：gt::大于，gae::大于等于,lt::小于，lte::小于等于

```sh
# 查找出大于20岁，小于等于25岁的学生
GET /school/student/_search?pretty
{
"query": {
   "range": {
    "age": {"gt":20,"lte":25}
         }
      }
}

```

#### 6.1.5 exits

* exists过滤可以找到文档中是否包含某个字段
* missing用法已经被抛弃了，可使用bool、must_not、exists组合使用

```
GET /school/student/_search?pretty
{
"query": {
   "exists": {
    "field": "age"  
         }
      }
}

GET /school/student/_search?pretty
{
"query": {
  "bool": {
    "must_not": {
      "exists": {
        "field": "age1"
        }
      }
    }
  }
}

```

### 6.2 bool组合查询

- 当出现多个查询语句组合的时候，可以用bool来包含。bool合并聚包含： 
  - must :: 多个查询条件的完全匹配,相当于 and 。
  - must_not :: 多个查询条件的相反匹配，相当于 not 。
  - should :: 至少有一个查询条件匹配, 相当于 or

```sh
# 查询非男性中喜欢旅行的人
GET /school/student/_search?pretty
{
"query": {
   "bool": {
      "must": { "match": {"about": "travel"}},
      "must_not": {"match": {"sex": "boy"}}
     }
  }
}

# 查询喜欢旅行的，性别可有可无
GET /school/student/_search?pretty
{
"query": {
   "bool": {
      "must": { "match": {"about": "travel"}},
      "should": {"match": {"sex": "boy"}}         
     }
  }
}

# 过滤出about字段包含travel并且年龄大于20岁小于30岁的同学
GET /school/student/_search?pretty
{
  "query": {
    "bool": {
      "must": [
        {"term": {
          "about": {
            "value": "travel"
          }
        }},{"range": {
          "age": {
            "gte": 20,
            "lte": 30
          }
        }}
      ]
    }
  }
}

```

### 6.3 过滤 Filter DSL

- 在上下文过滤语境中，查询语句主要解决文档是否匹配的问题，而不会在意匹配程度（相关性评分）。

  例如：

  -  `created` 的日期范围是否在 `2013` 到 `2014` ?
  -  `status` 字段中是否包含单词 "published" ?
  -  `lat_lon` 字段中的地理位置与目标点相距是否不超过10km ?

* 通常复杂的查询语句，我们也要配合**过滤语句来实现缓存**，用filter语句就可以来实现。

```sh
# 查询出喜欢旅行的，并且年龄是20岁的文档
GET /school/student/_search?pretty
{
  "query": {
   "bool": {
     "must": {"match": {"about": "travel"}},     
     "filter": [{"term":{"age": 20}}]
     }
  }
}
```



## 七、定义字段类型 mappings

* 在es当中，每个字段都会有默认的类型，根据我们第一次插入数据进去，es会自动帮我们推断字段的类型，当然我们也可以通过设置mappings来提前自定义我们字段的类型。

```sh
# 老版本,7.0之前
# 创建
PUT /school2
{
  "mappings": {
    "logs" : {
      "properties": {"messages" : {"type": "text"}}
    }
  }
}
# 添加
POST /school/_mapping/logs
{
  "properties": {"number" : {"type": "text"}}
}

# 查询
GET school2/logs/_mapping
```

```sh
# 老版本,7.0之后
# 创建
PUT /school2
{
  "mappings": {
      "properties": {"messages" : {"type": "text"}}
  }
}

# 添加
POST /school/_mapping/logs
{
  "properties": {"number" : {"type": "text"}}
}

# 查询
GET school2/_mapping
```

* 很多地方将ElasticSearch与数据库类比，比如将index看作数据库，type看作数据库表，id看作表结构中的主键。但是，在内部的机制中却有不同，在同一个数据库中，不同的表的字段可以名称相同，类型不同，但是在ES中，由于在不同映射类型中具有相同名称的字段在内部由相同的Lucene字段支持，因此在相同的索引中，如果给不同type设置相同filed，但是filed的属性不同的话，在操作比如删除时，就可能会删除失败。除此之外，存储在同一索引中具有很少或没有共同字段的不同实体会导致数据稀疏，并影响Lucene有效压缩文档的能力。因此在es7的时，废除类mapping types 的设置。在7.0到8.0之间，我们可以通过`include_type_name=false`指定是否使用mapping types。（7.0默认是true，7.0-8.0默认是false,true 开启，false关闭）

## 八、定义索引分片以及副本数 settings

* 所谓的settings就是用来修改索引分片和副本数的；
* 比如有的重要索引，副本数很少甚至没有副本，那么我们可以通过setting来添加副本数。
* **副本可修改，但是分片一旦创建，则不可修改。**

```sh
# 修改副本数为2
PUT /school2/_settings
{
  "number_of_replicas": 2
}

# 修改分片，此操作执行失败
PUT /school2/_settings
{
  "number_of_shards": 3
}

# 分片需要在创建的时候就要确认
PUT /school3
{
  "settings": {
  	"number_of_shards": 3
  }
}

```



