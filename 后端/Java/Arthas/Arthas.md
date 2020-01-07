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

### 3.2 sc/sm查看已加载的类

#### 3.2.1 sc

* 命令可以查找到所有JVM已经加载到的类。

* 如果搜索的是接口，还会搜索所有的实现类。比如查看所有的`Filter`实现类：

  ```
  sc javax.servlet.Filter
  ```

* 通过`-d`参数，可以打印出类加载的具体信息，很方便查找类加载问题。

  ```
  sc -d javax.servlet.Filter
  ```

* `sc`支持通配，比如搜索所有的`StringUtils`：

  ```
  sc *StringUtils
  ```

#### 3.2.2 sm

* `sm`命令则是查找类的具体函数。比如：

  ```
  sm java.math.RoundingMode
  ```
  
* 通过`-d`参数可以打印函数的具体属性：

  ```
  sm -d java.math.RoundingMode
  ```

* 也可以查找特定的函数，比如查找构造函数：

  ```
  sm java.math.RoundingMode <init>
  ```

### 3.3 jad

* 可以通过 `jad` 命令来反编译代码：

  ```
  jad com.example.demo.arthas.user.UserController
  ```

* 通过`--source-only`参数可以只打印出在反编译的源代码：

  ```
  jad --source-only com.example.demo.arthas.user.UserController
  ```

### 3.4 ognl

* 可以动态执行代码。

#### 3.4.1 调用static函数

```
ognl '@java.lang.System@out.println("hello ognl")'
```

可以检查`Terminal 1`里的进程输出，可以发现打印出了`hello ognl`。

#### 3.4.2  获取静态类的静态字段

* 获取`UserController`类里的`logger`字段：

  ```
  ognl -c 1be6f5c3 @com.example.demo.arthas.user.UserController@logger
  ```

* 还可以通过`-x`参数控制返回值的展开层数。比如：

  ```
  ognl -c 1be6f5c3 -x 2 @com.example.demo.arthas.user.UserController@logger
  ```

#### 3.4.3  执行多行表达式，赋值给临时变量，返回一个List

* ```
  ognl '#value1=@System@getProperty("java.home"), #value2=@System@getProperty("java.runtime.name"), {#value1, #value2}'
  ```

* ```
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

  ```
  watch com.example.demo.arthas.user.UserController * '{params, throwExp}'
  ```

  1. 第一个参数是类名，支持通配
  2. 第二个参数是函数名，支持通配

* 如果想把获取到的结果展开，可以用`-x`参数：

  ```
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

  ```
  watch com.example.demo.arthas.user.UserController * '{params[0], target, returnObj}'
  ```

#### 3.5.3 条件表达式

* `watch`命令支持在第4个参数里写条件表达式（第一个参数大于100），比如：

  ```
  watch com.example.demo.arthas.user.UserController * returnObj 'params[0] > 100'
  ```

#### 3.5.4 当异常时捕获

* `watch`命令支持`-e`选项，表示只捕获抛出异常时的请求：

  ```
  watch com.example.demo.arthas.user.UserController * "{params[0],throwExp}" -e
  ```

#### 3.5.5 按照耗时进行过滤

* watch命令支持按请求耗时进行过滤，比如：

  ```
  watch com.example.demo.arthas.user.UserController * '{params, returnObj}' '#cost>200'
  ```