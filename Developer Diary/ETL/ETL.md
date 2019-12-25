# ETL

## 一、介绍

ETL，是英文 Extract-Transform-Load 的缩写，用来描述将数据从来源端经过抽取(extract)、转换(transform)、加载(load)至目的端的过程。ETL 是构建数据仓库的重要一环，用户从数据源抽取出所需的数据，经过数据清洗，最终按照预先定义好的数据仓库模型，将数据加载到数据仓库中去。

## 二、软件分为三类

* 这里只讨论开源的ETL工具解决方案

* Kettle
* DataX
* Sqoop
* Flume
* StreamSets

| 工具       | 开源程度        | HDFS | RDBMS | File | 扩展 | UI | 配置 | 分布式 | star |
| ---------- | --------------- | ---- | ----- | ---- | ---- | ---- | ---- | ---- | ---- |
| Kettle     | Pentaho         | √    | √     | √    | 2    | 4 |  | 4 | ![](https://img.shields.io/github/stars/pentaho/pentaho-kettle.svg) |
| DataX      | Alibaba         | √    | √     |      | 5   |      | 5 |      | ![](https://img.shields.io/github/stars/alibaba/dataX.svg) |
| Scoop      | Apache          | √    | √     |      | 5    |      | 4 |      | ![](https://img.shields.io/github/stars/apache/sqoop.svg) |
| Flume      | Apache/Cloudera | √    |       | √    | 5    |      | 4 | 4 | ![](https://img.shields.io/github/stars/apache/flume.svg) |
| StreamSets |  | √ | √ | √ |      |  |      |      |      |

