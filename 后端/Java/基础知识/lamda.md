# JAVA8 lambda表达式
## 优点

1. 传递数据变成传递行为（函数），也就是说允许你通过表达式来代替功能接口。
2. 它有参数列表、函数主体、返回值类型等，但是它没有名称，所以它传递的函数准确的来说应该是会传递匿名函数。
3. 以更少的代码实现相同的功能，代码更加简洁清爽。

## 基本语法

1. 特性

    1. 函数式接口有且只有一个接口;（同方法，不同参数）
    2. jdk8 增加default关键字，可以有多个默认实现发给发
    3. 如果方法是Object类当中的方法，忽略第一条（equals、toString、HashCode）

1. 语法
    ```
    (parameters) -> expression
    ```

    ```
    (parameters) ->{
        statements;
    }
    ```
2. 

lambda 表达式写法。
1. 单行表达式:
```java
// 参数 -> 逻辑处理
(n,a)->n + a
// 加入参说明
(String n,Integer a)->n + a
```
2. 多行语句块
```java
(n,a)->{
	return n+a;
}
```
3. 方法引用
 - 普通方法引用
 - 静态方法引用
 - 构造函数引用

 ```java
 //普通方法引用
 new Object::function
 //静态方法引用
 Object::function
// 构造函数引用
Supplier suppplier = Object::new；
// 第一个参数的方法引用
// 第一个参数作为对象，后面的就是普通方法引用
String::substring 

 ```

 ## 常用表达式
 1. Function
    接收一个参数，返回一个结果。

2. Consumer
    接受一个参数，
    ForEach处理，不需要返回结果。

3. Supplier
    不接受任何参数，执行返回结果（构造函数引用）

4. Predicate
    接受一个参数，返回boolean，
    filter 过滤 


// 工具类，把对象的ifesle转化为Optional处理



Optional.of(name)。