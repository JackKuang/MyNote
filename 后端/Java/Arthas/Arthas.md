[TOC]

# Arthas

## 一、介绍

* `Arthas` 是Alibaba开源的Java诊断工具，深受开发者喜爱。在线排查问题，无需重启；动态跟踪Java代码；实时监控JVM状态。

* `Arthas` 支持JDK 6+，支持Linux/Mac/Windows，采用命令行交互模式，同时提供丰富的 `Tab` 自动补全功能，进一步方便进行问题的定位和诊断。

## 二、 基础命令

启动一个普通项目

```sh
wget https://alibaba.github.io/arthas/arthas-demo.jar
java -jar arthas-demo.jar
```

启动 arthas

```sh
wget https://alibaba.github.io/arthas/arthas-boot.jar
java -jar arthas-boot.jar
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

  ```sh
  watch demo.MathGame primeFactors returnObj
  ```

  ```sh
  watch demo.MathGame primeFactors params
  ```

  ```sh
  watch demo.MathGame primeFactors params[0]
  ```

### 2.6 exit

* 用 `exit` 或者 `quit` 命令可以退出Arthas
* `exit/quit`命令只是退出当前session，arthas server还在目标进程中运行。

### 2.7 stop

* 完全退出Arthas，可以执行 `stop` 命令。

## 三、进阶命令

启动一个SpringBoot项目

```sh
wget https://github.com/hengyunabc/katacoda-scenarios/raw/master/demo-arthas-spring-boot.jar
java -jar demo-arthas-spring-boot.jar
```

启动Arthas

```sh
wget https://alibaba.github.io/arthas/arthas-boot.jar
java -jar arthas-boot.jar --target-ip 0.0.0.0
```

### 3.1 查看JVM信息

#### 3.1.1 sysprop

* `sysprop` 可以打印所有的System Properties信息。

* 也可以指定单个key：

  ```sh
  sysprop java.version
  ```

* 也可以通过`grep`来过滤： 

  ```sh
  sysprop | grep user
  ```

* 可以设置新的value： 

  ```sh
  sysprop testKey testValue
  ```

#### 3.1.2 sysenv

* `sysenv` 命令可以获取到环境变量。和`sysprop`命令类似。

#### 3.1.3 jvm

* `jvm` 命令会打印出`JVM`的各种详细信息

### 3.2 sc/sm查看已加载的类

#### 3.2.1 sc

* 命令可以查找到所有JVM已经加载到的类。

* 如果搜索的是接口，还会搜索所有的实现类。比如查看所有的`Filter`实现类：

  ```sh
  sc javax.servlet.Filter
  ```

* 通过`-d`参数，可以打印出类加载的具体信息，很方便查找类加载问题。

  ```sh
  sc -d javax.servlet.Filter
  ```

* `sc`支持通配，比如搜索所有的`StringUtils`：

  ```sh
  sc *StringUtils
  ```

#### 3.2.2 sm

* `sm`命令则是查找类的具体函数。比如：

  ```sh
  sm java.math.RoundingMode
  ```
  
* 通过`-d`参数可以打印函数的具体属性：

  ```sh
  sm -d java.math.RoundingMode
  ```

* 也可以查找特定的函数，比如查找构造函数：

  ```sh
  sm java.math.RoundingMode <init>
  ```

### 3.3 jad

* 可以通过 `jad` 命令来反编译代码：

  ```sh
  jad com.example.demo.arthas.user.UserController
  ```

* 通过`--source-only`参数可以只打印出在反编译的源代码：

  ```sh
  jad --source-only com.example.demo.arthas.user.UserController
  ```

### 3.4 ognl

* 可以动态执行代码。

#### 3.4.1 调用static函数

```sh
ognl '@java.lang.System@out.println("hello ognl")'
```

可以检查`Terminal 1`里的进程输出，可以发现打印出了`hello ognl`。

#### 3.4.2  获取静态类的静态字段

* 获取`UserController`类里的`logger`字段：

  ```sh
  ognl -c 1be6f5c3 @com.example.demo.arthas.user.UserController@logger
  ```

* 还可以通过`-x`参数控制返回值的展开层数。比如：

  ```sh
  ognl -c 1be6f5c3 -x 2 @com.example.demo.arthas.user.UserController@logger
  ```

#### 3.4.3  执行多行表达式，赋值给临时变量，返回一个List

* ```sh
  ognl '#value1=@System@getProperty("java.home"), #value2=@System@getProperty("java.runtime.name"), {#value1, #value2}'
  ```

* ```sh
  $ ognl '#value1=@System@getProperty("java.home"), #value2=@System@getProperty("java.runtime.name"), {#value1, #value2}'
  @ArrayList[
      @String[/Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home/jre],
      @String[Java(TM) SE Runtime Environment],
  ]
  ```

* OGNL特殊用法请参考：https://github.com/alibaba/arthas/issues/71

* OGNL表达式官方指南：https://commons.apache.org/proper/commons-ognl/language-guide.html

### 3.5 排查函数调用异常现象

#### 3.5.1 查看UserController的 参数/异常

* 在Arthas里执行：

  ```sh
  watch com.example.demo.arthas.user.UserController * '{params, throwExp}'
  ```

  1. 第一个参数是类名，支持通配
  2. 第二个参数是函数名，支持通配

* 如果想把获取到的结果展开，可以用`-x`参数：

  ```sh
  watch com.example.demo.arthas.user.UserController * '{params, throwExp}' -x 2
  ```

#### 3.5.2 返回值表达式

* 在上面的例子里，第三个参数是`返回值表达式`，它实际上是一个`ognl`表达式，它支持一些内置对象：

  | 变量名    | 变量解释                                                     |
  | --------- | ------------------------------------------------------------ |
  | loader    | 本次调用类所在的 ClassLoader                                 |
  | clazz     | 本次调用类的 Class 引用                                      |
  | method    | 本次调用方法反射引用                                         |
  | target    | 本次调用类的实例                                             |
  | params    | 本次调用参数列表，这是一个数组，如果方法是无参方法则为空数组 |
  | returnObj | 本次调用返回的对象。当且仅当 `isReturn==true` 成立时候有效，表明方法调用是以正常返回的方式结束。如果当前方法无返回值 `void`，则值为 null |
  | throwExp  | 本次调用抛出的异常。当且仅当 `isThrow==true` 成立时有效，表明方法调用是以抛出异常的方式结束。 |
  | isBefore  | 辅助判断标记，当前的通知节点有可能是在方法一开始就通知，此时 `isBefore==true` 成立，同时 `isThrow==false` 和 `isReturn==false`，因为在方法刚开始时，还无法确定方法调用将会如何结束。 |
  | isThrow   | 辅助判断标记，当前的方法调用以抛异常的形式结束。             |
  | isReturn  | 辅助判断标记，当前的方法调用以正常返回的形式结束。           |

* 你可以利用这些内置对象来组成不同的表达式。比如返回一个数组：

  ```sh
  watch com.example.demo.arthas.user.UserController * '{params[0], target, returnObj}'
  ```

#### 3.5.3 条件表达式

* `watch`命令支持在第4个参数里写条件表达式（第一个参数大于100），比如：

  ```sh
  watch com.example.demo.arthas.user.UserController * returnObj 'params[0] > 100'
  ```

#### 3.5.4 当异常时捕获

* `watch`命令支持`-e`选项，表示只捕获抛出异常时的请求：

  ```sh
  watch com.example.demo.arthas.user.UserController * "{params[0],throwExp}" -e
  ```

#### 3.5.5 按照耗时进行过滤

* watch命令支持按请求耗时进行过滤，比如：

  ```sh
  watch com.example.demo.arthas.user.UserController * '{params, returnObj}' '#cost>200'
  ```

## 3.6 热更新代码

通过`jad`/`mc`/`redefine` 命令实现动态更新代码的功能。

### 3.6.1 jad反编译UserController

* 反编译代码

  ```
  jad --source-only com.example.demo.arthas.user.UserController > /tmp/UserController.java
  ```

### 3.6.2 vim修改源代码

* 找到对应的逻辑，修改代码

### 3.6.3 sc查找加载UserController的ClassLoader

* 查找加载该类的ClassLoader

  ```sh
  sc -d *UserController
  ```

  ```sh
  sc -d *UserController | grep classLoaderHash
  ```

### 3.6.4 mc 编译

* 保存好`/tmp/UserController.java`之后，使用`mc`(Memory Compiler)命令来编译，并且通过`-c`参数指定ClassLoader：

  ```sh
  mc -c 1be6f5c3 /tmp/UserController.java -d /tmp
  ```

### 3.6.5 redefine 加载编译

* 再使用`redefine`命令重新加载新编译好的`UserController.class`：

  ```
  redefine /tmp/com/example/demo/arthas/user/UserController.class
  ```

## 3.7 动态更新应用Logger Level

在这个案例里，动态修改应用的Logger Level。

### 3.7.1 查找UserController的ClassLoader

```sh
sc -d com.example.demo.arthas.user.UserController | grep classLoaderHash
 classLoaderHash   1be6f5c3
```

### 3.7.2 用ognl获取logger

```
ognl -c 1be6f5c3 '@com.example.demo.arthas.user.UserController@logger'
```

```sh
$ ognl -c 1be6f5c3 '@com.example.demo.arthas.user.UserController@logger'
@Logger[
    serialVersionUID=@Long[5454405123156820674],
    FQCN=@String[ch.qos.logback.classic.Logger],
    name=@String[com.example.demo.arthas.user.UserController],
    level=null,
    effectiveLevelInt=@Integer[20000],
    parent=@Logger[Logger[com.example.demo.arthas.user]],
    childrenList=null,
    aai=null,
    additive=@Boolean[true],
    loggerContext=@LoggerContext[ch.qos.logback.classic.LoggerContext[default]],
]
```

```
可以知道UserController@logger实际使用的是logback。可以看到level=null，则说明实际最终的level是从root logger里来的。
```

### 3.7.3 单独设置UserController的logger level

```sh
ognl -c 1be6f5c3 '@com.example.demo.arthas.user.UserController@logger.setLevel(@ch.qos.logback.classic.Level@DEBUG)'
```

再次获取`UserController@logger`，可以发现已经是`DEBUG`了：

```sh
ognl -c 1be6f5c3 '@com.example.demo.arthas.user.UserController@logger'
$ ognl -c 1be6f5c3 '@com.example.demo.arthas.user.UserController@logger'
@Logger[
    serialVersionUID=@Long[5454405123156820674],
    FQCN=@String[ch.qos.logback.classic.Logger],
    name=@String[com.example.demo.arthas.user.UserController],
    level=@Level[DEBUG],
    effectiveLevelInt=@Integer[10000],
    parent=@Logger[Logger[com.example.demo.arthas.user]],
    childrenList=null,
    aai=null,
    additive=@Boolean[true],
    loggerContext=@LoggerContext[ch.qos.logback.classic.LoggerContext[default]],
]
```

### 3.7.4  修改logback的全局logger level

通过获取`root` logger，可以修改全局的logger level：

```sh
ognl -c 1be6f5c3 '@org.slf4j.LoggerFactory@getLogger("root").setLevel(@ch.qos.logback.classic.Level@DEBUG)'
```

## 3.8 获取Spring Context

展示获取spring context，再获取bean，然后调用函数。

### 3.8.1 使用tt命令获取到spring context

* `tt`即 TimeTunnel，它可以记录下指定方法每次调用的入参和返回信息，并能对这些不同的时间下调用进行观测。

* [https://alibaba.github.io/arthas/tt.html](https://alibaba.github.io/arthas/tt.html)

  ```sh
  tt -t org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter invokeHandlerMethod
  ```

* 发送请求时可以看到，tt命令捕获了一个请求

  ```sh
  $ tt -t org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdaptePress Q or Ctrl+C to abort.
  Affect(class-cnt:1 , method-cnt:1) cost in 252 ms.
   INDE  TIMESTAMP    COST(  IS-R  IS-  OBJECT     CLASS               METHOD
   X                  ms)    ET    EXP
  -----------------------------------------------------------------------------------------
   1000  2019-02-15   4.583  true  fal  0xc93cf1a  RequestMappingHand  invokeHandlerMethod
         15:38:32     923          se              lerAdapter
  ```

### 3.8.2 使用tt命令从调用记录里获取到spring context

*  ```
  tt -i 1000 -w 'target.getApplicationContext()'
  ```

* ```
  $ tt -i 1000 -w 'target.getApplicationContext()'
  @AnnotationConfigEmbeddedWebApplicationContext[
      reader=@AnnotatedBeanDefinitionReader[org.springframework.context.annotation.AnnotatedBeanDefinitionReader@2e457641],
      scanner=@ClassPathBeanDefinitionScanner[org.springframework.context.annotation.ClassPathBeanDefinitionScanner@6eb38026],
      annotatedClasses=null,
      basePackages=null,
  ]
  Affect(row-cnt:1) cost in 439 ms.
  ```

### 3.8.3 获取spring bean，并调用函数

* ```
  tt -i 1000 -w 'target.getApplicationContext().getBean("helloWorldService").getHelloMessage()'
  ```

## 3.9 排查HTTP请求返回401

* 我们知道`401`通常是被权限管理的`Filter`拦截了，那么到底是哪个`Filter`处理了这个请求，返回了401

#### 3.9.1 跟踪所有的Filter函数

* 开始trace：

  ```
  trace javax.servlet.Filter *
  ```

* 访问请求

* 可以在调用树的最深层，找到`AdminFilterConfig$AdminFilter`返回了`401`：

  ```
  +---[3.806273ms] javax.servlet.FilterChain:doFilter()
  |   `---[3.447472ms] com.example.demo.arthas.AdminFilterConfig$AdminFilter:doFilter()
  |       `---[0.17259ms] javax.servlet.http.HttpServletResponse:sendError()
  ```

#### 3.9.2 通过stack获取调用栈

* 上面是通过`trace`命令来获取信息，从结果里，我们可以知道通过`stack`跟踪

* 访问请求

* `HttpServletResponse:sendError()`，同样可以知道是哪个`Filter`返回了`401`

* 执行：

  ```
  stack javax.servlet.http.HttpServletResponse sendError 'params[0]==401'
  ```

* 就可以看到方法栈了

## 3.10 排查HTTP请求返回404

404是内容找不到。那么到底是哪个Servlet处理了这个请求，返回了404？

### 3.10.1 跟踪所有的Servlet函数

* 开始trace：

  ```
  trace javax.servlet.Servlet * > /tmp/servlet.txt
  ```

* 发送请求
* 查看servlet日志数据

## 3.11 理解Spring Boot应用的ClassLoader结构

## 3.12 查找Top N线程

* 查看所有线程信息

```
thread
```

* 查看具体线程的栈

* 查看线程ID 16的栈：

```
thread 16
```

* 查看CPU使用率top n线程的栈

```
thread -n 3
```

* 查看5秒内的CPU使用率top n线程栈

```
thread -n 3 -i 5000
```

* 查找线程是否有阻塞

```
thread -b
```

## 3.13 Web Console

* Arthas支持通过Web Socket来连接。
* 当在本地启动时，可以访问 http://127.0.0.1:8563/ ，通过浏览器来使用Arthas。

## 3.14 arthas-boot支持的参数

`arthas-boot.jar` 支持很多参数，可以执行 `java -jar arthas-boot.jar -h` 来查看。

### 3.14.1 允许外部访问

默认情况下， arthas server侦听的是 `127.0.0.1` 这个IP，如果希望远程可以访问，可以使用`--target-ip`的参数。

```
java -jar arthas-boot.jar --target-ip
```

### 3.14.2 列出所有的版本

```
java -jar arthas-boot.jar --versions
```

使用指定版本：

```
java -jar arthas-boot.jar --use-version 3.1.0
```

### 3.14.3 只侦听Telnet端口，不侦听HTTP端口

```
java -jar arthas-boot.jar --telnet-port 9999 --http-port -1
```

### 3.14.4 打印运行的详情

```
java -jar arthas-boot.jar -v
```