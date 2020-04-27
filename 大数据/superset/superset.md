# 一、Supset简介



# 二、Superset on Docker

```sh
docker pull amancevice/superset
docker rm -f superset
docker run -d -p 8089:8088 --name superset -v /home/docker/data/superset:/home/superset amancevice/superset
```

* 设置用户名和密码(docker exec -it 容器ID fabmanager create-admin –app superset)
* 初始化数据库（docker exec -it 容器ID superset db upgrade）
* superset初始化（docker exec -it 容器ID superset init）
* 开启superset服务（docker exec -it 容器ID superset runserver）

```
docker exec -it superset fabmanager create-admin --app superset
docker exec -it superset superset db upgrade
docker exec -it superset superset init
docker exec -it superset superset run
docker exec -it superset superset-init

docker exec -it superset nohup superset run -h 0.0.0.0 > /dev/null 2>&1 & 
```

