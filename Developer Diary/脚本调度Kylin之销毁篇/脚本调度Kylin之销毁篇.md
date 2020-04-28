# 脚本调度Kylin之销毁篇

## 一、背景

* 在使用了Kylin之后，经过几次版本迭代，对Kylin的使用越来越成熟。
* 既然对前面数据有了创建，也会有数据的销毁。如果配置了需要任务，点击删除会很麻烦。
* 内容的结构基本上按层级分，如果直接删除上一级，下一级的内容不会删除，同时，如果你再删除下一级的内容，就会报错，导致更难删除。Kylin的内容层级为：
  * project
  * datasource
  * model
  * cube
  * job
* 因为大量的数据是存储在HBase之中的，而只删除部分数据的话无法确定HBase的表，只能通过Kylin来处理，让Kylin来决定要删除的数据。

## 二、思路

* 销毁不再使用shell脚本进行销毁，因为接口涉及到json解析，直接使用shell脚本不容易获解析json。
* 同样是脚本，Python处理起来就会比shell更加方便
* 基本思路如下，用curl模拟请求。
  * 界面创建一个模板任务，获取请求地址与请求体
  * 模拟登陆cookie
  * 操作删除数据
* shell脚本调度Kylin进行清理数据。

## 三、Coding

### 3.1 登陆请求头

```shell
headers = {"Authorization": "Basic QURNSU46S1lMSU4=", "Content-Type": "application/json"}
```

QURNSU46S1lMSU4=为加密算法（username:password的Base64密码加密）

### 3.2 删除job

* 先通过一个接口查询到所有的jobs（默认15条）
* 再执行删除请求

```python
import json
import urllib.request

kylin_host = "http://${服务地址}/kylin"
list_monitor_url = "{}/api/jobs?jobSearchMode=ALL&limit=15&offset=0&projectName=${项目名称}&timeFilter=4".format(kylin_host)

drop_job_url_template = "{}/api/jobs/{}/drop"
# 包含密码登陆授权
headers = {"Authorization": "Basic QURNSU46S1lMSU4=", "Content-Type": "application/json"}
# 登陆
req = urllib.request.Request(list_monitor_url, headers=headers)
response = urllib.request.urlopen(req)
json_str = response.read().decode('utf-8')
job_list = json.loads(json_str)

for job in job_list:
    uuid = job['uuid']
    print(uuid)
    drop_monitor_url = drop_job_url_template.format(kylin_host, uuid)
    req = urllib.request.Request(drop_monitor_url, headers=headers)
    req.get_method = lambda: 'DELETE'
    response = urllib.request.urlopen(req)
    # DELETE请求
    json_str = response.read().decode('utf-8')
    print(json_str)
```

### 3.3 删除Cube

* 先通过一个接口查询到所有的cubes（默认15条）
* disable掉cube
* 删除segtments
* 删除cube

```python
# 定期清理Kylin重复任务
import json
import urllib.request

kylin_host = "http://${服务地址}/kylin"
list_monitor_url = "{}/api/cubes?limit=15&offset=0&projectName=${项目名称}".format(kylin_host)

disable_url_template = "{}/api/cubes/{}/disable"
delete_url_template= "{}/api/cubes/{}/segs/{}"
drop_url_template= "{}/api/cubes/{}"
# 包含密码登陆授权
headers = {"Authorization": "Basic QURNSU46S1lMSU4=", "Content-Type": "application/json"}
# 登陆
req = urllib.request.Request(list_monitor_url, headers=headers)
response = urllib.request.urlopen(req)
json_str = response.read().decode('utf-8')
cube_list = json.loads(json_str)

for cube in cube_list:
    name = cube['name']
    segments = cube['segments']
    print(name)
    drop_monitor_url = disable_url_template.format(kylin_host, name)
    req = urllib.request.Request(drop_monitor_url, headers=headers)

    for se in segments:
        print(se['name'])
        url = delete_url_template.format(kylin_host, name,se['name'])
        req = urllib.request.Request(url, headers=headers)
        req.get_method = lambda: 'DELETE'
        try:
            response = urllib.request.urlopen(req)
            # DELETE请求
            json_str = response.read().decode('utf-8')

    url = drop_url_template.format(kylin_host, name)
    req = urllib.request.Request(url, headers=headers)
    req.get_method = lambda: 'DELETE'
    response = urllib.request.urlopen(req)
    # DELETE请求
    json_str = response.read().decode('utf-8')
```

### 3.3 删除model

* 先通过一个接口查询到所有的model（默认全部）
* 再执行删除请求

```python
# 定期清理Kylin重复任务
import json
import urllib.request


kylin_host = "http://${服务地址}/kylin"
list_monitor_url = "{}/api/models?projectName=${项目名称}".format(kylin_host)

disable_model_template = "{}/api/models/{}"
# 包含密码登陆授权
headers = {"Authorization": "Basic QURNSU46S1lMSU4=", "Content-Type": "application/json"}
# 登陆
req = urllib.request.Request(list_monitor_url, headers=headers)
response = urllib.request.urlopen(req)
json_str = response.read().decode('utf-8')
model_list = json.loads(json_str)
for model in model_list:
    name = model['name']
    url = disable_model_template.format(kylin_host, name)
    req = urllib.request.Request(url, headers=headers)
    req.get_method = lambda: 'DELETE'
    response = urllib.request.urlopen(req)
    # DELETE请求
    json_str = response.read().decode('utf-8')
    print(json_str)
```

### 3.4 删除datasource

* 先通过一个接口查询到所有的model（默认全部）
* 再执行删除请求

```python
import json
import urllib.request

kylin_host = "http://${服务地址}/kylin"
list_monitor_url = "{}/api/tables?ext=true&project=${项目名称}".format(kylin_host)

disable_model_template = "{}/api/tables/{}.{}/${项目名称}"
# 包含密码登陆授权
headers = {"Authorization": "Basic QURNSU46S1lMSU4=", "Content-Type": "application/json"}
# 登陆
req = urllib.request.Request(list_monitor_url, headers=headers)
response = urllib.request.urlopen(req)
json_str = response.read().decode('utf-8')
datasource_list = json.loads(json_str)
for datasource in datasource_list:
    database = datasource['database']
    name = datasource['name']
    url = disable_model_template.format(kylin_host, database,name)
    req = urllib.request.Request(url, headers=headers)
    req.get_method = lambda: 'DELETE'
    response = urllib.request.urlopen(req)
    # DELETE请求
    json_str = response.read().decode('utf-8')
    print(json_str)

```

### 3.5 最重要一步

* 其实，到这里，Kylin再HBase中的HTable并没有删除。

* 还需要调度Kylin执行一次数据的删除

  ```shell
  cd ${KYLIN_HOME}/bin
  #查看kylin可清理数据 
  ./kylin.sh org.apache.kylin.tool.StorageCleanupJob --delete false
  #执行清理
  ./kylin.sh org.apache.kylin.tool.StorageCleanupJob --delete true
  ```

* 删除前后对比HBase的Region数量，会由数据的比变动

## 四、总结

* 这里总结一下几个抓包的请求与接口

  | 功能        | 请求方式 | 接口地址                                                     |
  | ----------- | -------- | ------------------------------------------------------------ |
  | 查询jobs    | GET      | {}/api/jobs?jobSearchMode=ALL&limit=15&offset=0&projectName=${项目名称}&timeFilter=4 |
  | 删除job     | DELETE   | {}/api/jobs/${jobUuid}/drop                                  |
  | 查询Cube    | GET      | {}/api/cubes?limit=15&offset=0&projectName=${项目名称}       |
  | diable Cube | DELETE   | {}/api/cubes/{cubeName}/disable                              |
  | 删除Segment | DELETE   | {}/api/cubes/{cubeName}/segs/{segmentName}                   |
  | 删除Cube    | DELETE   | {}/api/cubes/{cubeName}                                      |
  | 查询Model | GET | {}/api/models?projectName=${项目名称} |
  | 删除Model | DELETE | {}/api/models/{modelName} |
  | 查询Datasource | GET | {}/api/tables?ext=true&project=${项目名称} |
  | 删除Datasource | DELETE | {}/api/tables/{datasourceDatabase}.{DatasourceName}/${项目名称} |
  
* 删除完成之后，记得调度Kylin清理任务