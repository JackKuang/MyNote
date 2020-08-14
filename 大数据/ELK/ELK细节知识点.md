## 一、Logstash

### 1.1 geoip插件

用于ip转地址位置

```properties
geoip {
	# 输入ip字段
    source => "ip" 
    # 输出数据字段
    target => "geoip"
    # 地理信息IP字段
    database => "D:\App\GeoLite\GeoLite2-City_20200616\GeoLite2-City.mmdb"
}
```

### 1.2 mutate插件

```properties

mutate {
	# 添加字段
    add_field => { "location" => "%{[geoip][location]}" }
    add_field => { "region_name" => "%{[geoip][region_name]}" }
	# 删除字段
    remove_field => [@timestamp","@version","type","geoip"]
}
```

### 1.3 ruby时间时区误差处理

```properties
ruby {
    code => "
    event.set('timestamp', event.get('@timestamp').time.localtime + 8*60*60)
    event.set('@timestamp',event.get('timestamp'))
    "
    remove_field => ["timestamp"]
}
```

* Logstash时间问题，在从Logstash读取数据，写入ElasticSearch，最后在Kibana数据展示的过程中，以下情况都是展示的是UTC时间：

  * Logstash打印到控制台
  * ElasticSearch查询

  但是实际在数据的报表展示中，所有的数据时间均是正常的。这个问题暂未解决。

### 1.4 时间字段提取

```properties
    date {
        match => [ "create_time", "UNIX_MS"]
        target => "@timestamp"
    }
```

## 二、ElasticSearch