kubectl explain pod

kubectl explain pod.spec

## 必须存在的属性

| 参数名                  | 类型   | 说明                                                         |
| ----------------------- | ------ | ------------------------------------------------------------ |
| version                 | String | k8s版本，基本上是v1。<br />可以用kubectl api-versions命令查看 |
| kind                    | String | 资源类型和角色，比如Pob                                      |
| metadata                | Object | 元数据对象                                                   |
| metadata.name           | String | 元数据对象名称                                               |
| metadata.namespace      | String | 元数据对象命名空间                                           |
| spec                    | String | 详细定义对象                                                 |
| spec.containers[]       | String | 容器列表                                                     |
| spec.containers[].name  | String | 容器名称                                                     |
| spec.containers[].image | String | 容器镜像                                                     |

## 主要对象（不写提供默认值）

| 参数名                                     | 类型   | 说明                                                         |
| ------------------------------------------ | ------ | ------------------------------------------------------------ |
| spec.containers[].name                     | String | 容器名称                                                     |
| spec.containers[].image                    | String | 容器镜像                                                     |
| spec.containers[].imagePullPolicy          | String | 镜像拉取车策略:<br />Always:每次拉去(默认)<br />Never:仅使用本地镜像<br />IfNotPresent:本地有使用本地,没有就拉 |
| spec.containers[].command[]                | List   | 启动命令                                                     |
| spec.containers[].args[]                   | List   | 启动参数                                                     |
| spec.containers[].workingDir               | String | 容器工作目录                                                 |
| spec.containers[].volumeMounts[]           | List   | 容器内部存储卷                                               |
| spec.containers[].volumeMounts[].name      | String |                                                              |
| spec.containers[].volumeMounts[].mountPath | String |                                                              |
| spec.containers[].volumeMounts[].readOnly  | String | 读写模式                                                     |
| spec.containers[].port[]                   | List   | 端口列表                                                     |
| spec.containers[].port[].name              | String | 名称                                                         |
| spec.containers[].port[].containerPort     | String | 容器端口号                                                   |
| spec.containers[].port[].hostPort          | String | 主机端口号                                                   |
| spec.containers[].port[].proocol           | String | TCP(默认)/UDP                                                |
| spec.containers[].env[]                    | List   | 环境变量                                                     |
| spec.containers[].env[].name               | String | 环境变量名称                                                 |
| spec.containers[].env[].val                | String | 环境变量值                                                   |
| spec.containers[].env[].resources          | String | 指定资源消耗                                                 |
| spec.containers[].resource                 | Object | 指定资源限制和资源请求的值                                   |
| spec.containers[].resource.limts           | Object | 指定容器运行时资源的运行上线                                 |
| spec.containers[].resource.limts.cpu       | String | cpu限制                                                      |
| spec.containers[].resource.limts.memory    | String | 内存限制                                                     |
| spec.containers[].resource.request         | Object | 指定容器启动和调度时的限制设置                               |
| spec.containers[].resource.request.cpu     | String | cpu限制                                                      |
| spec.containers[].resource.request.memory  | String | 内存限制                                                     |

## 额外的参数项目


| 参数名                | 类型    | 说明                                                         |
| --------------------- | ------- | ------------------------------------------------------------ |
| spec.restartPolicy    | String  | Always: 一旦失败,重启<br />OnFail:返回非零码重启<br />Never:都不重启 |
| spec.nodeSelector     | Object  | 指定节点运行                                                 |
| spec.imagePullSecrets | Object  | 定义pull时的secret名称                                       |
| spec.hostNetwork      | Boolean | 是否使用主机网络,默认false                                   |
