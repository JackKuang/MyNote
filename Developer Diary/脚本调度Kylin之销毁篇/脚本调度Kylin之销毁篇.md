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

## 五、按Cube任务删除

* 前面几个任务创建任务的逆序（创建datasource⬅创建Model⬅创建Cube⬅创建Job）进行删除数据。期间格子任务的数据是分离的，这对于清理所有的历史数据是合理的，但是对于定期清除数据却不合理。
* 所以，重新写了一个脚本，针对Cube进行处理。以一下顺序清理Cube
  * 查询Cube列表
  * 遍历Cube
    * 查询Cube下所有历史Job，全部删除
    * Disable Cube
    * 查询Cube下所有历史Segments，全部删除
    * Drop Cube

```python
# 删除Cube
import json
import urllib.request
import datetime

kylin_host = "http://190.1.1.121:7070/kylin"

# 包含密码登陆授权
headers = {"Authorization": "Basic QURNSU46S1lMSU4=", "Content-Type": "application/json"}

list_cubes_template = "{}/api/cubes?limit={}&offset={}"
list_job_template = "{}/api/jobs?cubeName={}&jobSearchMode=ALL&limit=15&offset=0&timeFilter=4"
drop_job_template = "{}/api/jobs/{}/drop"
disable_cubes_template = "{}/api/cubes/{}/disable"
delete_cubes_segs_template = "{}/api/cubes/{}/segs/{}"
drop_cubes_template = "{}/api/cubes/{}"
disable_model_template = "{}/api/models/{}"


# 有些Cube标记损坏，无法清理，这里定义了数组，跳过这些Cube
skipCube = [""]

# 删除Job
def deleteJobs(name):
    loop = True
    while loop:
        list_job_url = list_job_template.format(kylin_host, name)
        req = urllib.request.Request(list_job_url, headers=headers)
        response = urllib.request.urlopen(req)
        json_str = response.read().decode('utf-8')
        moniter_list = json.loads(json_str)
        if len(moniter_list) == 0:
            loop = False
        for moniter in moniter_list:
            uuid = moniter['uuid']
            drop_job_url = drop_job_template.format(kylin_host, uuid)
            req = urllib.request.Request(drop_job_url, headers=headers)
            req.get_method = lambda: 'DELETE'
            response = urllib.request.urlopen(req)
            # DELETE请求
            json_str = response.read().decode('utf-8')

# 删除Cube
def deleteCube(name, segments):
    disable_cubes_url = disable_cubes_template.format(kylin_host, name)
    req = urllib.request.Request(disable_cubes_url, headers=headers)
    req.get_method = lambda: 'PUT'
    json_str = urllib.request.urlopen(req)

    for se in segments:
        delete_cubes_segs_url = delete_cubes_segs_template.format(kylin_host, name, se['name'])
        req = urllib.request.Request(delete_cubes_segs_url, headers=headers)
        req.get_method = lambda: 'DELETE'
        response = urllib.request.urlopen(req)
        # DELETE请求
        json_str = response.read().decode('utf-8')

    url = drop_cubes_template.format(kylin_host, name)
    req = urllib.request.Request(url, headers=headers)
    req.get_method = lambda: 'DELETE'
    response = urllib.request.urlopen(req)
    # DELETE请求
    json_str = response.read().decode('utf-8')

# 清理Cube任务
def cleanCube():
    offset = 0
    limit = 15
    now = (datetime.datetime.now() + datetime.timedelta(days=-60))

    loop = True
    while loop:
        list_monitor_url = list_cubes_template.format(kylin_host, limit, offset)  # 登陆
        req = urllib.request.Request(list_monitor_url, headers=headers)
        response = urllib.request.urlopen(req)
        json_str = response.read().decode('utf-8')
        moniter_list = json.loads(json_str)
        if len(moniter_list) == 0:
            loop = False
        time_values = {}
        for moniter in moniter_list:
            uuid = moniter['uuid']
            name = moniter['name']
            segments = moniter['segments']
            last_build = 0
            for segment in segments:
                if last_build < segment['last_build_time']:
                    last_build = segment['last_build_time']
            last_build_time = datetime.datetime.fromtimestamp(last_build / 1000)
            if now.__ge__(last_build_time):
                if name not in skipCube:
                    time_values[uuid] = True
                    print("DELETE {}".format(name))
                    # 删除job
                    deleteJobs(name)
                    deleteCube(name, segments);

        if len(time_values) == 0:
            offset = offset + limit


if __name__ == '__main__':
    cleanCube()

```

