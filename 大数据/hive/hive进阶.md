## 一、Hive表的数据压缩

### 1. 压缩性能评价

1. 压缩比：压缩比越高、压缩后文件越小；压缩比越高越好。
2. 压缩时间：越快越好。
3. 再分割：已经压缩的格式文件是否可以再分割，可以分割的格式允许单一文件由多个Mapper程序处理，可以更好的并行化

### 2. 常见的压缩格式

| 压缩方式 | 压缩比 | 压缩速度 | 解压缩速度 | 是否可分割 |
| :------: | :----: | :------: | :--------: | :--------: |
|   gzip   | 13.4%  | 21 MB/s  |  118 MB/s  |     否     |
|  bzip2   | 13.2%  | 2.4MB/s  |  9.5MB/s   |     是     |
|   lzo    | 20.5%  | 135 MB/s |  410 MB/s  |     是     |
|  snappy  | 22.2%  | 172 MB/s |  409 MB/s  |     是     |

### 3. Hadoop 编码、解码方式

| 压缩格式 |             对应的编码/解码器              |
| :------: | :----------------------------------------: |
| DEFLATE  | org.apache.hadoop.io.compress.DefaultCodec |
|   Gzip   |  org.apache.hadoop.io.compress.GzipCodec   |
|  BZip2   |  org.apache.hadoop.io.compress.BZip2Codec  |
|   LZO    |     com.hadoop.compress.lzo.LzopCodec      |
|  Snappy  | org.apache.hadoop.io.compress.SnappyCodec  |

### 4. 数据压缩使用

* hive表中间数据压缩intermediate

  ```bash
  #设置为true为激活中间数据压缩功能，默认是false，没有开启
  set hive.exec.compress.intermediate=true;
  #设置中间数据的压缩算法
  set mapred.map.output.compression.codec=org.apache.hadoop.io.compress.SnappyCodec;
  ```

* hive表最终数据压缩output

  ```bash
  set hive.exec.compress.output=true;
  set mapred.output.compression.codec=org.apache.hadoop.io.compress.SnappyCodec;
  ```

## 二、 hive表的文件存储格式

### 1. 格式说明

* Hive支持的存储数的格式主要有：**textFile**、**sequencefile**、**orc**、**parquet**。
* 其中textFile为默认格式，建表时默认为这个格式，导入数据时会直接把数据文件拷贝到hdfs上不进行处理。**sequencefile**、**orc**、**parquet**格式的表不能直接从本地文件导入数据，数据要先导入到TextFile格式的表中，然后再从textFile表中用insert导入到sequencefile、orc、parquet表中。
* **textFile **和 **sequencefile**的存储格式都是基于行存储的；
* **orc** 和 **parquet** 是基于列式存储的。

### 2. 文件存储格式对比





## 三、Hive 函数

### 1. 默认函数

```sql
show functions;
desc function max;
```

### 2. 自定义函数

用户自定义函数类别分为三种：

1. UDF (User-Defuned-Funtion)    一进一出
2. UDAF(User-Defined Aggregation Funciton)     聚合函数，多进一出
3. UDTF(User-Defined Table0Generating Function)    一进多出



