# Logstash

## 一、介绍

* logstash就是一个具备实时数据传输能力的管道，负责将数据信息从管道的输入端传输到管道的输出端；与此同时这根管道还可以让你根据自己的需求在中间加上滤网，Logstash提供里很多功能强大的滤网以满足你的各种应用场景。是一个input | filter | output 的数据流。

* 使用参考文档： 

  https://www.elastic.co/guide/en/logstash/current/index.html

## 二、安装

* Logstash安装简单，参考网站https://www.elastic.co/guide/en/logstash/current/installing-logstash.html

## 三、Input 插件

### 3.1 stdin标准输入和stdout标准输出

* 使用标准的输入与输出组件，实现将我们的数据从控制台输入，从控制台输出

```sh
bin/logstash -e 'input{stdin{}}output{stdout{codec=>rubydebug}}'
# 控制台输入hello
{
      "@version" => "1",
          "host" => "node01",
    "@timestamp" => 2018-10-13T08:33:13.126Z,
       "message" => "hello"
}
```

### 3.2 监控日志文件变化

* Logstash使用一个名叫 *FileWatch* 的 Ruby Gem 库来监听文件变化。这个库支持 glob 展开文件路径，而且会记录一个叫 *.sincedb* 的数据库文件来跟踪被监听的日志文件的当前读取位置。所以，不要担心 logstash 会漏过你的数据。

```sh
# 创建配置文件
vim config/monitor_file.conf
input{
    file{
        path => "/data/tomcat.log"
        type => "log"
        start_position => "beginning"
    }
}
output{
        stdout{
        codec=>rubydebug
        }
}
# ==========
# 检查配置文件是否可用
bin/logstash -f config/monitor_file.conf -t
Config Validation Result: OK. Exiting Logstash

# ==========
# 启动服务
bin/logstash -f config/monitor_file.conf

# ==========
echo "hello logstash" >> /data/tomcat.log
```

* 其他参数说明
  * path=>表示监控的文件路径
  * type=>给类型打标记，用来区分不同的文件类型。
  * start_postion=>从哪里开始记录文件，默认是从结尾开始标记，要是你从头导入一个文件就把改成”beginning”.
  * discover_interval=>多久去监听path下是否有文件，默认是15s
  * exclude=>排除什么文件
  * close_older=>一个已经监听中的文件，如果超过这个值的时间内没有更新内容，就关闭监听它的文件句柄。默认是3600秒，即一个小时。
  * sincedb_path=>监控库存放位置(默认的读取文件信息记录在哪个文件中)。默认在：/data/plugins/inputs/file。
  * sincedb_write_interval=> logstash 每隔多久写一次 sincedb 文件，默认是 15 秒。
  * stat_interval=>logstash 每隔多久检查一次被监听文件状态（是否有更新），默认是 1 秒。

### 3.3 JDBC插件

* jdbc插件允许我们采集某张数据库表当中的数据到我们的logstash当中来。

```sh
vim config/jdbc.conf

input {
  jdbc {
    jdbc_driver_library => "/data/mysql-connector-java-5.1.38.jar"
    jdbc_driver_class => "com.mysql.jdbc.Driver"
    jdbc_connection_string => "jdbc:mysql://127.0.0.1:3306/mydb"
    jdbc_user => "root"
    jdbc_password => "123456"

    use_column_value => true
    tracking_column => "tno"
  #  parameters => { "favorite_artist" => "Beethoven" }
    schedule => "* * * * *"
    statement => "SELECT * from courses where tno > :sql_last_value ;"
  }
}

output{
        stdout{
        codec=>rubydebug
        }
}

# ==========
# 启动服务
bin/logstash -f config/jdbc.conf
```

### 3.4 systlog插件

* syslog机制负责记录内核和应用程序产生的日志信息，管理员可以通过查看日志记录，来掌握系统状况

```sh
vim config/syslog.conf

input{
    tcp{
        port=> 6789
        type=> syslog
    }
    udp{
        port=> 6789
        type=> syslog
    }
}

filter{
    if [type] == "syslog" {
        grok {
                match => { "message" => "%{SYSLOGTIMESTAMP:syslog_timestamp} %{SYSLOGHOST:syslog_hostname} %{DATA:syslog_program}(?:\[%{POSINT:syslog_pid}\])?: %{GREEDYDATA:syslog_message}" }
                add_field => [ "received_at", "%{@timestamp}" ]
                add_field => [ "received_from", "%{host}" ]
        }
       date {
             match => [ "syslog_timestamp", "MMM  d HH:mm:ss", "MMM dd HH:mm:ss" ]
           }
    }
}

output{
    stdout{
        codec=> rubydebug
    }
}


# ==========
# 启动服务
bin/logstash -f config/syslog.conf

# 修改系统日志配置文件
sudo vim /etc/rsyslog.conf
# 添加一行
*.* @@127.0.0.1:6789
# 重启系统日志服务
sudo systemctl restart rsyslog
```

## 四、Filter插件

Logstash之所以强悍的主要原因是filter插件；通过过滤器的各种组合可以得到我们想要的结构化数据。 

### 4.1 grok正则表达式 

* grok正则表达式是logstash非常重要的一个环节；可以通过grok非常方便的将数据拆分和索引 

  语法格式： 

  (?<name>pattern) 

  ?<name>表示要取出里面的值，pattern就是正则表达式 

* 简单测试

```sh
vim config/filter.conf

input {stdin{}} filter {
        grok {
                match => {
"message" => "(?<date>\d+\.\d+)\s+"
                }       
        }       
}       
output {stdout{codec => rubydebug}}

# ==========
# 启动服务
bin/logstash -f config/filter.conf

# 控制台输入 2020.6.27 今天暴雨
```

### 4.2使用grok收集nginx日志数据

* nginx一般打印出来的日志格式如下

  ```
  36.157.150.1 - - [05/Nov/2018:12:59:28 +0800] "GET /phpmyadmin_8c1019c9c0de7a0f/js/get_scripts.js.php?scripts%5B%5D=jquery/jquery-1.11.1.min.js&scripts%5B%5D=sprintf.js&scripts%5B%5D=ajax.js&scripts%5B%5D=keyhandler.js&scripts%5B%5D=jquery/jquery-ui-1.11.2.min.js&scripts%5B%5D=jquery/jquery.cookie.js&scripts%5B%5D=jquery/jquery.mousewheel.js&scripts%5B%5D=jquery/jquery.event.drag-2.2.js&scripts%5B%5D=jquery/jquery-ui-timepicker-addon.js&scripts%5B%5D=jquery/jquery.ba-hashchange-1.3.js HTTP/1.1" 200 139613 "-" "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36"
  ```

  这种日志是非格式化的，通常，我们获取到日志后，还要使用mapreduce 或者spark 做一下清洗操作，

  就是将非格式化日志编程格式化日志；

  在清洗的时候，如果日志的数据量比较大，那么也是需要花费一定的时间的；

  所以可以使用logstash 的grok 功能，将nginx 的非格式化数据采集成格式化数据：

* 安装grok插件

  ```sh
  cd LOGSTASH_HOME
  vim Gemfile
  #source 'https://rubygems.org'    # 将这个镜像源注释掉
  source https://gems.ruby-china.com/  # 配置成中国的这个镜像源
  # 准备在线安装
  bin/logstash-plugin  install logstash-filter-grok
  ```

* 配置运行

  ```sh
  vim config/monitor_nginx.conf
  
  input {stdin{}}
  filter {
  grok {
  match => {
  "message" => "%{IPORHOST:clientip} \- \- \[%{HTTPDATE:time_local}\] \"(?:%{WORD:method} %{NOTSPACE:request}(?:HTTP/%{NUMBER:httpversion})?|%{DATA:rawrequest})\" %{NUMBER:status} %{NUMBER:body_bytes_sent} %{QS:http_referer} %{QS:agent}"
           }
       }
  }
  output {stdout{codec => rubydebug}}
  
  # ==========
  # 启动服务
  bin/logstash -f config/monitor_nginx.conf
  ```

## 五、Output插件

### 5.1 stdout标准输出

```sh
bin/logstash -e 'input{stdin{}}output{stdout{codec=>rubydebug}}
```

### 5.2 将采集数据保存到file文件中

```sh
vim config/output_file.conf
output {
    file {
        path => "/data/%{+YYYY-MM-dd}-%{host}.txt"
        codec => line {
            format => "%{message}"
        }
        flush_interval => 0
    }
}

# ==========
# 启动服务
bin/logstash -f config/output_file.conf
```

### 5.3 将采集数据保存到elasticsearch

```sh
vim config/output_es.conf
input {stdin{}}
output {
    elasticsearch {
        hosts => ["node01:9200"]
        index => "logstash-%{+YYYY.MM.dd}"
    }
}

# ==========
# 启动服务
bin/logstash -f config/output_es.conf

```

