## 一、删除没用的镜像

```sh
docker rmi `docker images | grep none | awk  ‘{print $3}’`
```

