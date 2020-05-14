# Kubenate搭建Nginx

## 一、安装Nginx

```sh
kubectl run nginx-deployment-ali --image=nginx --port=80 --replicas=1
```

* 问题：

  ```
  # --replicas 被遗弃
  Flag --replicas has been deprecated, has no effect and will be removed in the future.
  ```

  rs、deployment等无法构建

* 原因：

  新版本的k8s不支持该模式了，更改为资源编排的模式使用：

  ```sh
  #vim nginx-deployment.yaml
  apiVersion: apps/v1
  kind: Deployment
  metadata:
    name: nginx-deployment
    labels:
      app: nginx
  spec:
    replicas: 2
    selector:
      matchLabels:
        app: nginx
    template:
      metadata:
        labels:
          app: nginx
      spec:
        containers:
        - name: nginx
          image: nginx
          ports:
          - containerPort: 80
  
  # 构建
  kubectl apply -f nginx-deployment.yaml
  ```

查看部署结果

对比一下1个独立的nginx-deployment-ali，两个合并的nginx-deployment

```sh
[root@node01 container]# kubectl get pod
NAME                               READY   STATUS    RESTARTS   AGE
nginx-deployment-ali               1/1     Running   0          39s
nginx-deployment-d46f5678b-rkccg   1/1     Running   0          167m
nginx-deployment-d46f5678b-wl49q   1/1     Running   0          130m
[root@node01 container]# kubectl get rs
NAME                         DESIRED   CURRENT   READY   AGE
nginx-deployment-d46f5678b   2         2         2       169m
[root@node01 container]# kubectl get deployment
NAME               READY   UP-TO-DATE   AVAILABLE   AGE
nginx-deployment   2/2     2            2           169m
```

## 二、暴露端口

* 这一层网络还只是Flannel层到Pob层的网络

```
kubectl expose deployment nginx-deployment --port=80 --target-port=80
```

查看端口映射

```sh
[root@node01 container]# ipvsadm -Ln
IP Virtual Server version 1.2.1 (size=4096)
Prot LocalAddress:Port Scheduler Flags
  -> RemoteAddress:Port           Forward Weight ActiveConn InActConn
# 有一个80
TCP  10.101.250.107:80 rr
  -> 10.244.1.11:80               Masq    1      0          0         
  -> 10.244.1.13:80               Masq    1      0          0    
```

这个时候，curl三个IP+端口，都可以看到请求

# 三、宿主机ip端口绑定

```
[root@node01 container]# kubectl get svc
NAME               TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)        AGE
kubernetes         ClusterIP   10.96.0.1        <none>        443/TCP        18h
nginx-deployment   ClusterIP    10.101.250.107   <none>       80/TCP   122m
```

需要对nginx-deployment进行修改

```yaml
kubectl edit svc nginx-deployment

# 末尾部分
spec:
  clusterIP: 10.101.250.107
  externalTrafficPolicy: Cluster
  ports:
  - nodePort: 30080 # 添加
    port: 80
    protocol: TCP
    targetPort: 80
  selector:
    app: nginx
  sessionAffinity: None
  type: NodePort # 修改

```

保存之后再次查询

```
[root@node01 container]# kubectl get svc
NAME               TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)        AGE
kubernetes         ClusterIP   10.96.0.1        <none>        443/TCP        18h
nginx-deployment   NodePort    10.101.250.107   <none>        80:30080/TCP   126m
```

现在 访问宿主机ip就可以了，这里做到了负载均衡。

同样，再次执行ipvsadm -Ln，就可以实现查看端口转发。