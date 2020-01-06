# Arthas

## 一、介绍

* `Arthas` 是Alibaba开源的Java诊断工具，深受开发者喜爱。在线排查问题，无需重启；动态跟踪Java代码；实时监控JVM状态。

* `Arthas` 支持JDK 6+，支持Linux/Mac/Windows，采用命令行交互模式，同时提供丰富的 `Tab` 自动补全功能，进一步方便进行问题的定位和诊断。

## 二、 基础命令

启动一个普通项目

```
wget https://alibaba.github.io/arthas/arthas-demo.jar
java -jar arthas-demo.jar
```

启动 arthas

```
wget https://alibaba.github.io/arthas/arthas-demo.jar
java -jar arthas-demo.jar
```

### 2.1 dashboard

* `dashboard`查看当前系统的实时数据面板。

### 2.2 thread

*  `thread 1`命令会打印线程ID 1的栈。

### 2.3 sc

* 可以通过 `sc` 命令来查找JVM里已加载的类：

  ```sh
  sc -d *MathGame
  ```

### 2.4 jad

* 可以通过 `jad` 命令来反编译代码：

  ```sh
  jad demo.MathGame
  ```

### 2.5 watch

* 通过`watch`命令可以查看函数的参数/返回值/常信息。

  ```
  watch demo.MathGame primeFactors returnObj
  ```

  ```
  watch demo.MathGame primeFactors params
  ```

  ```
  watch demo.MathGame primeFactors params[0]
  ```

### 2.6 exit

* 用 `exit` 或者 `quit` 命令可以退出Arthas
* `exit/quit`命令只是退出当前session，arthas server还在目标进程中运行。

### 2.7 stop

* 完全退出Arthas，可以执行 `stop` 命令。

## 三、进阶命令

启动一个SpringBoot项目

```
wget https://github.com/hengyunabc/katacoda-scenarios/raw/master/demo-arthas-spring-boot.jar
java -jar demo-arthas-spring-boot.jar
```

启动Arthas

```
wget https://alibaba.github.io/arthas/arthas-boot.jar
java -jar arthas-boot.jar --target-ip 0.0.0.0
```

### 3.1 查看JVM信息

#### 3.1.1 sysprop

* `sysprop` 可以打印所有的System Properties信息。

* 也可以指定单个key：

  ```
  sysprop java.version
  ```

* 也可以通过`grep`来过滤： 

  ```
  sysprop | grep user
  ```

* 可以设置新的value： 

  ```
  sysprop testKey testValue
  ```

#### 3.1.2 sysenv

* `sysenv` 命令可以获取到环境变量。和`sysprop`命令类似。

#### 3.1.3 jvm

* `jvm` 命令会打印出`JVM`的各种详细信息