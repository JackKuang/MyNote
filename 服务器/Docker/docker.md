### Docker

[https://www.docker.com](https://www.docker.com)

Docker 可以让开发者打包他们的应用以及依赖包到一个轻量级、可移植的容器中，然后发布到任何流行的 Linux 机器上，也可以实现虚拟化。

## Docker应用
1. Web应用的自动化打包和发布。
2. 自动化测试和持续集成、发布。
3. 在服务行环境中部署和调整数据库或者其他后台应用。

### Docker的优缺点
1. 简化程序

    Docker让开发这将服务打包应用到一个容器之中，然后发布到任何Linux机器之中。方便以及快捷。

2. 多样选择

    Docker景象中包含众多的数据库运行环境，可以部署多种应用实例。

3. 节省开支、增加利用率

    云计算的时代到来，使用Docker可以实现服务器更有效的利用，提保证了虚拟化的安全。

### Docker安装

```
yum install docker
```

### Docker生命周期管理

#### Docker run 创建容器常用命令
[runoob run常用run命令](https://www.runoob.com/docker/docker-run-command.html)
|  命令 | 示例 | 说明  |
| ------------ | ------------ | ------------ |
| -d | -d | -d: 后台运行容器，并返回容器ID；常用，一般命令行运行得时候，保证容器一直运行，必须加上去。 |
| -p | -p 8080:80  | 主机的8080端口映射到容器的80端口中，常用  |
| -t | -t | 为容器重新分配一个伪输入终端，通常与 -i 同时使用；不常用  |
| --name  | --name="nginx" | 容器的名称定义为nginx，常用  |
| -h  | -h "node1" | 指定容器的hostname，常用，建议写成和容器名称一样的配置 |
| -v  | -v /data2:/data1 | 容器的/data1指向主机的/data2 |

使用技巧：
```
# 容器同步主机时间
-v /etc/localtime:/etc/localtime:ro
# 容器同步时区
-v /etc/timezone:/etc/timezone:ro
# 配置host，这样做的好处是在集群建立的时候，可以提前定义好集群配置，但是由于ip是不确定的，需要确认使用
 --add-host node1:172.17.0.3 
```


#### Docker start/stop/restart 容器生命管理
```
Docker restart/stop/restart [CONTAINER_ID]/[CONTAINER_NAME]
```

#### Docker rm 移除容器
```
# 删除一个或者多个容器
Docker rm [CONTAINER_ID]/[CONTAINER_NAME] ..
```


#### Docker exec 执行容器命令
```
# 执行容器中的终端
docker exec -it [CONTAINER_ID]/[CONTAINER_NAME] /bin/bash
```

