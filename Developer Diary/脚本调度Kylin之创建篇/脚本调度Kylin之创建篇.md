# 脚本调度Kylin之创建篇

## 一、背景

* 大数据环境下的离线数据存储，考虑到持久性以及扩展性上，架构上使用的是Hive作为数据结构存储。
* 同时，也面临着Hive查询速度不够快的问题，在报表查询面临着需要快速查询到数据结果下。直接从Hive中查询已经无法满足要求，必须要有一个更加快速的查询工具来进行预处理。
* 那么，就是Kylin了，Kylin基于Hive、HBase等架构之上，可以基于Hive进行数据的多维度的数据预处理，并把结果存储到HBase中。当用户需要对数据进行查询的时候，可通过查询Kylin获取到查询结果。
* Kylin有一个统一的Web配置平台。但是一旦我们创建了任务，如果每天调度执行一次，就需要定时调度Kylin去处理。

## 二、思路

* 考虑到是Hive调度之后，直接执行Kylin调度任务，最快想到的就是用Shell脚本来直接调度Kylin。
* 基本思路如下，用curl模拟请求。
  * 界面创建一个模板任务，获取请求地址与请求体
  * curl模拟登陆cookie
  * curl模拟加载数据源
  * curl模拟创建Model
  * curl模拟创建Cubes
  * curl模拟构建任务

## 三、Coding

### 3.1 定义Kylin全局变量

```shell
kylin_url=http://123456:7070/kylin
```

### 3.2 登陆授权获取Cookie

```shell
curl -c cookfile.txt -X POST \
-H "Authorization:Basic QURNSU46S1lMSU4=" \
-H "Content-Type: application/json" \
${kylin_url}/api/user/authentication
```

* QURNSU46S1lMSU4=为加密算法（username:password的Base64密码加密）

### 3.3 创建数据源

获取到查询接口为`${kylin_url}/api/tables/${hiveTableName}/${projectName}`

```shell
curl -b cookfile.txt \
-X POST \
-H "Content-Type:application/json" \
-d '{"calculate":false}'  \
${kylin_url}/api/tables/${hiveTableName}/${projectName}
```

calculate为true或者false统计预计算

### 3.4 创建Model

获取到查询接口为`${kylin_url}/api/models`

可自行在页面上创建，复制一个请求体，以此作为模板后，后续的创建都以模板为准

```shell

curl -b cookfile.txt \
-X POST \
-H "Content-Type:application/json" \
-d '{"modelDescData":".........","project":"testProject"}' \
${kylin_url}/api/models
```

body体

```json
{
	"modelDescData": "........",
	"project": "tbl_biz_log_plus"
}
```

modelDescData内部为json转义内容

```json
{
	"name": "${model名好处呢给}",
	"description": "",
	"fact_table": "${事实表名称}",
	"lookups": [],
	"filter_condition": "",
	"dimensions": [{
		"table": "${事实表名称}'",
		"columns": ["${维度列1}", "${维度列2}", "${维度列3}", "${维度列4}"]
	}],
	"metrics": ["${量度列1}"],
	"partition_desc": {
		"partition_date_column": "${时间分区列}",
		"partition_type": "APPEND",
		"partition_date_format": "${时间分区列，yyyyMMdd，yyyy-MM-dd}"
	},
	"last_modified": 0
}
```

### 3.5 创建Cubes

获取到查询接口为`${kylin_url}/api/cubes`

可自行在页面上创建，复制一个请求体，以此作为模板后，后续的创建都以模板为准

```shell

curl -b cookfile.txt \
-X POST \
-H "Content-Type:application/json" \
-d '{"cubeDescData":"...","project":"tbl_biz_log_plus"}' \
${kylin_url}/api/cubes
```

3. body体

```json
{
"modelDescData": "........",
"project": "tbl_biz_log_plus"
}
```

modelDescData内部为json转义内容

```json
{
"name": "${Cube名称}",
"model_name": "${Mode名称}",
"description": "",
"dimensions": [{
"name": "${维度1}",
"table": "${表名}",
"column": "${维度1}"
}, {
"name": "${维度2}",
"table": "${表名}",
"column": "${维度2}"
}, {
"name": "${维度2}",
"table": "${表名}",
"column": "${维度2}"
}, {
"name": "${维度3}",
"table": "${表名}",
"column": "${维度3}"
}],
//统计量度
"measures": [{
"name": "_COUNT_",
"function": {
"expression": "COUNT",
"returntype": "bigint",
"parameter": {
"type": "constant",
"value": "1"
},
"configuration": {}
}
}, {
"name": "count_uid",
"function": {
"expression": "COUNT_DISTINCT",
"returntype": "hllc(16)",
"parameter": {
"type": "column",
"value": "${表名}.${量度1}"
}
},
"showDim": false
}],
"dictionaries": [],
// rowkey设计原则
"rowkey": {
"rowkey_columns": [{
"column": "${表名}.${维度1}",
"encoding": "dict",
"isShardBy": "false",
"encoding_version": 1
}, {
"column": "${表名}.${维度2}",
"encoding": "dict",
"isShardBy": "false",
"encoding_version": 1
}, {
"column": "${表名}.${维度3}",
"encoding": "dict",
"isShardBy": "false",
"encoding_version": 1
}, {
"column": "${表名}.${维度4}",
"encoding": "dict",
"isShardBy": "false",
"encoding_version": 1
}]
},
// 维度购机优化
"aggregation_groups": [{
"includes": ["${表名}.${维度4}"],
"select_rule": {
"hierarchy_dims": [
["TBL_BIZ_LOG_PLUS_'${startDate}'.APP_ID", "TBL_BIZ_LOG_PLUS_'${startDate}'.CHANNEL_ID"]
],
"mandatory_dims": ["TBL_BIZ_LOG_PLUS_'${startDate}'.DT"],
"joint_dims": []
}
}],
"mandatory_dimension_set_list": [],
"partition_date_start": 0,
"notify_list": [],
"hbase_mapping": {
"column_family": [{
"name": "F1",
"columns": [{
"qualifier": "M",
"measure_refs": ["_COUNT_"]
}]
}, {
"name": "F2",
"columns": [{
"qualifier": "M",
"measure_refs": ["count_uid"]
}]
}]
},
"volatile_range": "0",
"retention_range": "0",
"status_need_notify": ["ERROR", "DISCARDED", "SUCCEED"],
"auto_merge_time_ranges": [],
"engine_type": "2",
"storage_type": "2",
"override_kylin_properties": {}
}
```

### 3.6 构建任务

构建执行任务，开始时间与结束时间构建计算

```shell
today=$(date +%Y%m%d)
endTime=`date -d "${today} 00:00:00" +%s`
endTime=$[(endTime+8*60*60)*1000]
startTime=$[endTime-24*60*60*1000]

curl -b cookfile.txt \
-X PUT \
-H "Content-Type:application/json" \
-d '{"buildType":"BUILD","startTime":'${startTime}',"endTime":'${endTime}'}' \
${kylin_url}/api/cubes/${CubeName}/rebuild
```

## 四、总结

* 总体流程即为
  * 抓包
  * 登陆Cookie
  * 模拟请求
* 在定时执行完Hive任务之后，就可以直接调度Kylin执行任务调度功能了