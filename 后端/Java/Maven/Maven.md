# 一、Maven

## 1.1 Maven阿里云仓库

```xml
<mirror>
    <id>alimaven</id>
    <name>aliyun maven</name>
    <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
    <mirrorOf>central</mirrorOf>        
</mirror>
```

## 1.2 scope作用

* compile，缺省值，适用于所有阶段，会打包进项目。
* provided，类似compile，期望JDK、容器或使用者会提供这个依赖。
* runtime，只在运行时使用，如JDBC驱动，适用运行和测试阶段。
* test，只在测试时使用，用于编译和运行测试代码。不会随项目发布。
* system，类似provided，需要显式提供包含依赖的jar，Maven不会在Repository中查找它。