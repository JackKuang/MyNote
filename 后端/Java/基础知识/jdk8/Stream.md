# java8 stream

## 简介

Stream 使用一种类似用 SQL 语句从数据库查询数据的直观方式来提供一种对 Java 集合运算和表达的高阶抽象。

这种风格将要处理的元素集合看作一种流， 流在管道中传输， 并且可以在管道的节点上进行处理， 比如筛选， 排序，聚合等。
元素流在管道中经过中间操作（intermediate operation）的处理，最后由最终操作(terminal operation)得到前面处理的结果。

+--------------------+       +------+   +------+   +---+   +-------+
| ----steam流转换---- +-----> |---------中间操作---------|  |最终操作|
+--------------------+       +------+   +------+   +---+   +-------+
| stream of elements +-----> |filter+-> |sorted+-> |map+-> |collect|
+--------------------+       +------+   +------+   +---+   +-------+
| stream of elements +-----> |filter+-> |sorted+-> |map+-> |collect|
+--------------------+       +------+   +------+   +---+   +-------+

## 特性

* `不存储数据`：流是基于数据源的对象，本身不存储数据元素，而是通过通道将数据传递给操作。
* `函数式编程`：流的操作不会改变数据源。
* `延迟操作`：流的很多中间操作都是延迟执行的，中间操作仅是用于记录处理过程，真正的执行是在`terminal操作`中。
* `无限流（无限数据源）`：stream流中的数据不是一次性加载到内存中，而是在用的时候生成，由此可以实现无限流。对于无限流可以通过短路操作在有限时间内完成执行。
* `纯消费`：流的元素只能访问一次，类似Iterator，操作没有回头路。

## Steam 流的创建

1. 通过集合的stream（）和parallelStream()方法。
    ```java
    Stream<Student> stream = studentList.stream();
    Stream<Student> parallelStream = studentList.parallelStream();
    ```
2. 通过流的静态方法
    ```java
    (1) 通过Stream.of(Object[])
        Stream<String> stream = Stream.of("a", "b", "c");
        stream.forEach(System.out::print);

    (2) 通过IntStream().range(int,int)
        IntStream intStream = IntStream.range(0, 10);
        intStream.forEach(System.out::print);
        
    (3) 通过BufferedReader.lines()
        File file = new File("f:/file.txt");
        BufferedReader buf = new BufferedReader(new FileReader(file));
        Stream<String> stream = buf.lines();
        stream.forEach(System.out::println);
    ```

## 中间操作（intermediate operation）

1.  `distinct`：去重

    ```java
    // 去重判断需要重写hashCode与equals方法
    studentStream.distinct().forEach(System.out::println);
    ```

2. `filter`：过滤

    ```java
    // 过滤写法
    studentStream.filter(s -> s.age > 10).forEach(System.out::println);
    ```

3. `map`：类型转换 

    ```java
    // 类型转换
    studentStream.map(s -> s.name).forEach(System.out::println);
    ```
    `flatMap`:
    
    ```java
    // map是一对一映射
    // flatMap是将2维的集合映射成一维
    List<String> list = Arrays.asList("beijing changcheng", "beijing gugong", "beijing tiantan", "gugong tiananmen");
    //map只能将分割结果转成一个List,所以输出为list对象
    list.stream().map(item -> Arrays.stream(item.split(" "))).forEach(System.out::println);
    System.out.println("------------------------------------");
    //如果我们想要每个list里的元素，还需要一层foreach
    list.stream().map(item -> Arrays.stream(item.split(" "))).forEach(n-> {
        n.forEach(System.out::println);
    });
    System.out.println("------------------------------------");
    //flatmap可以将字符串分割成各自的list之后直接合并成一个List
    //也就是flatmap可以将一个2维的集合转成1维度
    list.stream().flatMap(item -> Arrays.stream(item.split(" "))).forEach(System.out::println);
    ```
    
4. `sorted`：将流中的元素按照自然排序方式进行排序，sorted(Comparator<? super T> comparator)可以指定排序的方式。对于有序流，排序是稳定的。对于非有序流，不保证排序稳定。
    ```java
    // 类型转换
    studentStream.sorted((a, b) -> a.age - b.age).forEach(System.out::println);
    ```

5. `limit`：获取n个。如果是并行流，结果可能不一致

    ```java
    // 获取前n个
    studentStream.limit(2).forEach(System.out::println);
    ```
    
6. `skip`：返回丢弃了前n个元素的流，如果流中的元素小于或者等于n，则返回空的流
    ```java
    // 跳过
    studentStream.skip(2).forEach(System.out::println);
   ```
    

## 最终操作(terminal operation)
1. `match`：用于匹配满足条件的流中的元素
    ```java
    System.out.println(studentList.stream().allMatch((a) -> a.age > 10));
    System.out.println(studentList.stream().anyMatch((a) -> a.age > 10));
    System.out.println(studentList.stream().noneMatch((a) -> a.age > 10));
    ```
2. `count`：统计
    ```java
    System.out.println(studentList.stream().count());
    ```
3. `collect`：收集数据结果
    ```java
    System.out.println(studentList.stream().map(s -> s.name).collect(Collectors.joining(",")));
    System.out.println(studentList.stream().map(s -> s.age).collect(Collectors.averagingInt(s -> s)));
    ```
4. `find`：查找
    ```java
    System.out.println(studentList.stream().filter(s -> s.age > 10).findAny());
    ```
5. `forEach`：遍历
    ```java
    System.out.println(
        studentList.stream().max(new Comparator<Student>() {
            @Override
            public int compare(Student o1, Student o2) {
                return o1.age - o2.age;
            }
        }));

    ```
6. `reduce`：计算
    ```java
    System.out.println(studentList.stream().map(s -> s.age).reduce((a, b) -> a + b));
    ```

7. `toArray`：数据转化数组

8. `concat`：将两个stream连接

## 并行流


    -- 数据量大且复杂，多核的情况下考虑。
&ensp; 所有的流操作都可以串行执行或者并行执行。
除非显示地创建并行流，否则Java库中创建的都是串行流。 

&ensp; Collection.stream()为集合创建串行流，Collection.parallelStream()为集合创建并行流。通过parallel()方法可以将串行流转换成并行流,sequential()方法将流转换成串行流。

&ensp; parallel stream通过默认的ForkJoinPool实现并行处理。处理的过程采用分治进行，将整个任务切分成多个段，分别对各个段进行处理。通过parallel stream对数据进行处理会使数据失去原始的顺序性。

&ensp; 流的原始顺序性依赖于数据源的有序性。在使用并行流会改变流中元素的处理顺序，破坏流的原始顺序性，所以在使用并行流对数据源进行处理时应确定数据源的元素满足结合性。

&ensp; 可以使用`forEachOrdered()`可以保持数据源原有的顺序性，或者通过`sorted()`重新定义数据的顺序。
```
    IntStream.range(1, 10).forEach(System.out::print);
    System.out.println();
    IntStream.range(1, 10).parallel().forEach(System.out::print);
    System.out.println();
    IntStream.range(1, 10).parallel().forEachOrdered(System.out::print);
    //输出 123456789
    //  658793241  随机
    //  123456789
```
&ensp; 流可以从非线程安全的集合中创建，当流执行终点操作的时候，非concurrent数据源不应该被改变。
```
    List<String> list = new ArrayList<>();
    list.add("one");
    list.add("two");
    list.stream().forEach(s -> list.add("three"));
    //并发异常 ：java.util.ConcurrentModificationException
```


&ensp; 而concurrent数据源可以被修改，不会出现并发问题。
```
    List<String> list = new CopyOnWriteArrayList<>(Arrays.asList("one", "two"));
    list.stream().forEach(s -> list.add("three"));
    //正常执行
```
&ensp; 由于stream的延迟操作特性，在执行终点操作前可以修改数据源，在执行终点操作时会将修改应用。
```
    List<String> list = new ArrayList<>();
    list.add("one");
    list.add("two");
    Stream<String> listStream = list.stream();
    list.add("three");
    listStream.forEach(System.out::println);
    //输出 one two three
```