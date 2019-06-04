# Nginx
Nginx 是一个高性能的 Web 和反向代理服务器，它有着许多非常优越的特性：
1. **Web服务器**：Nginx作为Web服务器，与Apache相比较，其占用更少的资源，支持更多的并发连接，有着更好更强的效率。
2. **负载均衡**：根据策略实现负载均衡，通过负载均衡实现服务性能提升以及服务可用性。
3. **反向代理**：用户不知道是那台服务器，客户端访问代理服务器服务。（正向代理，服务器不知道那个服务，客户通过代理服务器访问服务）

## Nginx 安装

一般来说，大部分的项目都可以在官网找到方法可以安装。


以下是编译安装Nginx
```
1.安装依赖pcre包
cd /usr/local/src
wget ftp://ftp.csx.cam.ac.uk/pub/software/programming/pcre/pcre-8.39.tar.gz 
tar -zxvf pcre-8.37.tar.gz
cd pcre-8.34
./configure
make
make install


2. 安装依赖zlib包
cd /usr/local/src
wget http://zlib.net/zlib-1.2.11.tar.gz
tar -zxvf zlib-1.2.11.tar.gz
cd zlib-1.2.11
./configure
make
make install

3. 安装依赖openssl包
cd /usr/local/src
wget https://www.openssl.org/source/openssl-1.0.1t.tar.gz
tar -zxvf openssl-1.0.1t.tar.gz

./config --prefix=/usr/local/openssl && make && make install 

4，nginx安装（以上三个依赖安装完后）
cd /usr/local
wget  http://nginx.org/download/nginx-1.9.0.tar.gz
tar -zxvf nginx-1.9.0.tar.gz
cd nginx-1.9.0
./configure && make && make install

```

## Nginx 基础配置
```
#运行用户
user nobody;
#启动进程,通常设置成和cpu的数量相等
worker_processes  1;

#全局错误日志及PID文件
#error_log  logs/error.log;
#error_log  logs/error.log  notice;
#error_log  logs/error.log  info;

#pid        logs/nginx.pid;

#工作模式及连接数上限
events {
    #epoll是多路复用IO(I/O Multiplexing)中的一种方式,
    #仅用于linux2.6以上内核,可以大大提高nginx的性能
    use   epoll; 

    #单个后台worker process进程的最大并发链接数    
    worker_connections  1024;

    # 并发总数是 worker_processes 和 worker_connections 的乘积
    # 即 max_clients = worker_processes * worker_connections
    # 在设置了反向代理的情况下，max_clients = worker_processes * worker_connections / 4  为什么
    # 为什么上面反向代理要除以4，应该说是一个经验值
    # 根据以上条件，正常情况下的Nginx Server可以应付的最大连接数为：4 * 8000 = 32000
    # worker_connections 值的设置跟物理内存大小有关
    # 因为并发受IO约束，max_clients的值须小于系统可以打开的最大文件数
    # 而系统可以打开的最大文件数和内存大小成正比，一般1GB内存的机器上可以打开的文件数大约是10万左右
    # 我们来看看360M内存的VPS可以打开的文件句柄数是多少：
    # $ cat /proc/sys/fs/file-max
    # 输出 34336
    # 32000 < 34336，即并发连接总数小于系统可以打开的文件句柄总数，这样就在操作系统可以承受的范围之内
    # 所以，worker_connections 的值需根据 worker_processes 进程数目和系统可以打开的最大文件总数进行适当地进行设置
    # 使得并发总数小于操作系统可以打开的最大文件数目
    # 其实质也就是根据主机的物理CPU和内存进行配置
    # 当然，理论上的并发总数可能会和实际有所偏差，因为主机还有其他的工作进程需要消耗系统资源。
    # ulimit -SHn 65535

}

http {
    #设定mime类型,类型由mime.type文件定义
    include    mime.types;
    default_type  application/octet-stream;
    #设定日志格式
    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';

    access_log  logs/access.log  main;

    #sendfile 指令指定 nginx 是否调用 sendfile 函数（zero copy 方式）来输出文件，
    #对于普通应用，必须设为 on,
    #如果用来进行下载等应用磁盘IO重负载应用，可设置为 off，
    #以平衡磁盘与网络I/O处理速度，降低系统的uptime.
    sendfile     on;
    #tcp_nopush     on;

    #连接超时时间
    #keepalive_timeout  0;
    keepalive_timeout  65;
    tcp_nodelay     on;

    #开启gzip压缩
    gzip  on;
    gzip_disable "MSIE [1-6].";

    #设定请求缓冲
    client_header_buffer_size    128k;
    large_client_header_buffers  4 128k;


    #设定虚拟主机配置
    server {
        #侦听80端口
        listen    80;
        #定义使用 www.nginx.cn访问
        server_name  www.nginx.cn;

        #定义服务器的默认网站根目录位置
        root html;

        #设定本虚拟主机的访问日志
        access_log  logs/nginx.access.log  main;

        #默认请求
        location / {
            
            #定义首页索引文件的名称
            index index.php index.html index.htm;   

        }

        # 定义错误提示页面
        error_page   500 502 503 504 /50x.html;
        location = /50x.html {
        }

        #静态文件，nginx自己处理
        location ~ ^/(images|javascript|js|css|flash|media|static)/ {
            
            #过期30天，静态文件不怎么更新，过期可以设大一点，
            #如果频繁更新，则可以设置得小一点。
            expires 30d;
        }

        #PHP 脚本请求全部转发到 FastCGI处理. 使用FastCGI默认配置.
        location ~ .php$ {
            fastcgi_pass 127.0.0.1:9000;
            fastcgi_index index.php;
            fastcgi_param  SCRIPT_FILENAME  $document_root$fastcgi_script_name;
            include fastcgi_params;
        }

        #禁止访问 .htxxx 文件
            location ~ /.ht {
            deny all;
        }
    }
}
```

## Nginx平滑升级
[http://www.nginx.cn/nginxchscommandline#reload%20bin](http://www.nginx.cn/nginxchscommandline#reload%20bin)

## Nginx Location


location匹配命令
```
~      #波浪线表示执行一个正则匹配，区分大小写
~*    #表示执行一个正则匹配，不区分大小写
^~    #^~表示普通字符匹配，如果该选项匹配，只匹配该选项，不匹配别的选项，一般用来匹配目录
=      #进行普通字符精确匹配
@     #"@" 定义一个命名的 location，使用在内部定向时，例如 error_page, try_files
```

优先级

1. =前缀的指令严格匹配这个查询。如果找到，停止搜索。
2. 所有剩下的常规字符串，最长的匹配。如果这个匹配使用^〜前缀，搜索停止。
3. 正则表达式，在配置文件中定义的顺序。
4. 如果第3条规则产生匹配的话，结果被使用。否则，使用第2条规则的结果。

示例：
```
location  = / {
  # 只匹配"/".
  [ configuration A ] 
}
location  / {
  # 匹配任何请求，因为所有请求都是以"/"开始
  # 但是更长字符匹配或者正则表达式匹配会优先匹配
  [ configuration B ] 
}
location ^~ /images/ {
  # 匹配任何以 /images/ 开始的请求，并停止匹配 其它location
  [ configuration C ] 
}
location ~* .(gif|jpg|jpeg)$ {
  # 匹配以 gif, jpg, or jpeg结尾的请求. 
  # 但是所有 /images/ 目录的请求将由 [Configuration C]处理.   
  [ configuration D ] 
}
/ -> 符合configuration A
/documents/document.html -> 符合configuration B
/images/1.gif -> 符合configuration C
/documents/1.jpg ->符合 configuration D
```


@location例子
```
error_page 404 = @fetch;

location @fetch(
proxy_pass http://fetch;
)
```

## Nginx rewrite

Nginx rewrite 执行顺序
1. 执行server块的rewrite指令(这里的块指的是server关键字后{}包围的区域，其它xx块类似)
2. 执行location匹配
3. 执行选定的location中的rewrite指令

[http://www.nginx.cn/216.html](http://www.nginx.cn/216.html)



## Nginx 反向代理

```
## Basic reverse proxy server ##
## Apache backend for www.redis.com.cn ##
upstream apachephp  {
    server ip:8080; #Apache
}

## Start www.redis.com.cn ##
server {
    listen 80;
    server_name  www.redis.com.cn;

    access_log  logs/redis.access.log  main;
    error_log  logs/redis.error.log;
    root   html;
    index  index.html index.htm index.php;

    ## send request back to apache ##
    location / {
        proxy_pass  http://apachephp;

        #Proxy Settings
        proxy_redirect     off;
        proxy_set_header   Host             $host;
        proxy_set_header   X-Real-IP        $remote_addr;
        proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
        proxy_next_upstream error timeout invalid_header http_500 http_502 http_503 http_504;
        proxy_max_temp_file_size 0;
        proxy_connect_timeout      90;
        proxy_send_timeout         90;
        proxy_read_timeout         90;
        proxy_buffer_size          4k;
        proxy_buffers              4 32k;
        proxy_busy_buffers_size    64k;
        proxy_temp_file_write_size 64k;
   }
}
## End www.redis.com.cn ##
```

## Nginx 负载均衡

```
#设定http服务器，利用它的反向代理功能提供负载均衡支持
http {

    #设定mime类型,类型由mime.type文件定义
    include             /etc/nginx/mime.types;
    default_type    application/octet-stream;

    #设定日志格式
    access_log        /var/log/nginx/access.log;

    #省略上文有的一些配置节点
    #。。。。。。。。。。

    #设定负载均衡的服务器列表
    upstream mysvr {
        #weigth参数表示权值，权值越高被分配到的几率越大
        server 192.168.8.1x:3128 weight=5;
        #本机上的Squid开启3128端口,不是必须要squid
        server 192.168.8.2x:80    weight=1;
        server 192.168.8.3x:80    weight=6;
    }
        
    upstream mysvr2 {
        #weigth参数表示权值，权值越高被分配到的几率越大
        server 192.168.8.x:80    weight=1;
        server 192.168.8.x:80    weight=6;
    }

    #第一个虚拟服务器
    server {
        #侦听192.168.8.x的80端口
        listen             80;
        server_name    192.168.8.x;

        #对aspx后缀的进行负载均衡请求
        location ~ .*.aspx$ {
            #定义服务器的默认网站根目录位置
            root     /root; 
            #定义首页索引文件的名称
            index index.php index.html index.htm;
            
            #请求转向mysvr 定义的服务器列表
            proxy_pass    http://mysvr ;

            #以下是一些反向代理的配置可删除.

            proxy_redirect off;

            #后端的Web服务器可以通过X-Forwarded-For获取用户真实IP
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;

            #允许客户端请求的最大单文件字节数
            client_max_body_size 10m; 

            #缓冲区代理缓冲用户端请求的最大字节数，
            client_body_buffer_size 128k;

            #nginx跟后端服务器连接超时时间(代理连接超时)
            proxy_connect_timeout 90;

            #连接成功后，后端服务器响应时间(代理接收超时)
            proxy_read_timeout 90;

            #设置代理服务器（nginx）保存用户头信息的缓冲区大小
            proxy_buffer_size 4k;

            #proxy_buffers缓冲区，网页平均在32k以下的话，这样设置
            proxy_buffers 4 32k;

            #高负荷下缓冲大小（proxy_buffers*2）
            proxy_busy_buffers_size 64k; 

            #设定缓存文件夹大小，大于这个值，将从upstream服务器传
            proxy_temp_file_write_size 64k;    

        }
    }
}
```

### nginx 屏蔽ip

1. 查询需要屏蔽的ip
```
awk '{print $1}' nginx.access.log |sort |uniq -c|sort -n
```

可以查询到 次数以及ip

```
  13610 202.112.113.192
  95772 180.169.22.135
 337418 219.220.141.2
 558378 165.91.122.67
 ```

 2. 新建blockip.conf
 ```
 deny 165.91.122.67; 
 ```

 3. 引入新的conf
 在nginx的conf中增加
 ```
 include blockip.conf; 
 ```
4. 重启nginx

高级用法
```
# 屏蔽单个ip访问

deny IP; 
# 允许单个ip访问

allow IP; 
# 屏蔽所有ip访问

deny all; 
# 允许所有ip访问

allow all; 
#屏蔽整个段即从123.0.0.1到123.255.255.254访问的命令

deny 123.0.0.0/8
#屏蔽IP段即从123.45.0.1到123.45.255.254访问的命令

deny 124.45.0.0/16
#屏蔽IP段即从123.45.6.1到123.45.6.254访问的命令

deny 123.45.6.0/24
如果你想实现这样的应用，除了几个IP外，其他全部拒绝，
那需要你在blockip.conf中这样写

allow 1.1.1.1; 
allow 1.1.1.2;
deny all; 
单独网站屏蔽IP的方法，把include blocksip.conf; 放到网址对应的在server{}语句块，
所有网站屏蔽IP的方法，把include blocksip.conf; 放到http {}语句块。
```