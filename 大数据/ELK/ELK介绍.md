# ELK日志协议栈

## 一、ELK基本介绍

ELK是三个软件产品的首字母缩写，Elasticsearch，Logstash 和 Kibana。这三款软件都是开源软件，通常是配合使用，而且又先后归于 Elastic.co 公司名下，故被简称为 ELK 协议栈。 

* Elasticsearch是个开源分布式搜索引擎，它的特点有：分布式，零配置，自动发现，索引自动分片，索引副本机制，restful风格接口，多数据源，自动搜索负载等。

* Logstash是一个完全开源的工具，他可以对你的日志进行收集、过滤，并将其存储供以后使用（如，搜索）。

* Kibana 也是一个开源和免费的工具，它Kibana可以为 Logstash 和 ElasticSearch 提供的日志分析友好的 Web 界面，可以帮助您汇总、分析和搜索重要数据日志。

| 组件          | 语言       | 功能                                                         |
| ------------- | ---------- | ------------------------------------------------------------ |
| ElasticSearch | Java       | 实时的分布式搜索和分析引擎，他可以用于全文检索，结构化搜索以及分析，lucene。Solr |
| Logstash      | JRuby      | 具有实时渠道能力的数据收集引擎，包含输入、过滤、输出模块，一般在过滤模块中做日志格式化的解析工作 |
| Kibana        | JavaScript | 为ElasticSerach提供分析平台和可视化的Web平台。他可以ElasticSerach的索引中查找，呼唤数据，并生成各种维度的表图 |

## 二、ELK的参考资料

ELK官网：https://www.elastic.co/

ELK官网文档：https://www.elastic.co/guide/index.html

ELK中文手册：https://www.elastic.co/guide/cn/elasticsearch/guide/current/index.html

ELK中文社区：https://elasticsearch.cn/