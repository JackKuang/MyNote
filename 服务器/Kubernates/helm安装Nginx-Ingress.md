# K8S之Nginx-Ingress

Nginx-Ingress是k8s的入口。

负责安装的jenkins、nginx都要通过nginx-ingress作为统一入口访问。

之前看视频学习，是按照官方网站的方式，是下载了`mandatory.yaml`和`service-nodeport.yaml`，然后调用`kubectl apply -f`创建。

但是，我再次访问的时候https://kubernetes.github.io/ingress-nginx/已经没有这种方式安装了。

再次尝试新的方式安装，就是Helm安装了。

* 版本说明
  * k8s:v1.18.2
  * helm:v3.2.1
  * nginx-ingress:v0.25.0

## 一、安装HELM

先下载helm，访问github地址

https://github.com/helm/helm/releases

下载，解压

```sh
cp helm /usr/local/bin
chmod a+x /user/local/bin
helm version
```

## 二、下载Nginx-Ingress

```sh
helm repo add apphub-incubator https://apphub.aliyuncs.com/incubator/ 
# 下载
helm pull apphub-incubator/nginx-ingress
```

## 三、配置 nginx-ingress

修改values.yaml

```yaml
hostNetwork: false 改为 true
type: LoadBalancer 改为 NodePort
rbac：
  create: false 改为 true
```

## 四、安装 nginx-ingress

```sh
*## 第一个 nginx-ingress 是 release 名。第二个 nginx-ingress 是 chart 解压目录。*
helm install nginx-ingress nginx-ingress
```

### 4.1 修改 deployment version

```sh
Error: unable to build kubernetes objects from release manifest: unable to recognize "": no matches for kind "Deployment" in version "extensions/v1beta1"
```

如果有如上报错，需要修改 nginx-ingress deployment 文件的 apiVersion。

```sh
grep -irl "extensions/v1beta1" nginx-ingress | grep deploy | xargs sed -i 's#extensions/v1beta1#apps/v1#g'
```

### 4.2 添加 deployment selector

```
Error: unable to build kubernetes objects from release manifest: error validating "": error validating data: ValidationError(Deployment.spec): missing required field "selector" in io.k8s.api.apps.v1.DeploymentSpec
```

​    如果有如上报错，需要在 deployment 文件添加 selector：

```
vi nginx-ingress/templates/controller-deployment.yaml
```

```
vi nginx-ingress/templates/default-backend-deployment.yaml
```

   修改后再次执行，安装成功：

## 五、nginx-ingress 组成

```
kubectl get pods -n nginx-ingress
```

由上图可知，nginx-ingress 包括 2 个组件：

1）nginx-ingress-controller：nginx-ingress 控制器，负责 nginx-ingress pod 的生命周期管理。nginx-ingress pod 本质就是 nginx。用来处理请求路由等功能。这也是为什么称 nginx-ingress pod 是集群流量入口的缘故。

2）nginx-ingress-default-backend：默认后端。如果你没有配置路由或路由配错了，将会由此 pod 兜底，一般会显示 404 给你。