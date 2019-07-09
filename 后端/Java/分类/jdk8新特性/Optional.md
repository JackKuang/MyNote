# Optional

## 概述
到目前为止，臭名昭著的空指针异常是导致Java应用程序失败的最常见原因。以前，为了解决空指针异常，Google公司著名的Guava项目引入了Optional类，Guava通过使用检查空值的方式来防止代码污染，它鼓励程序员写更干净的代码。受到Google Guava的启发，Optional类已经成为Java 8类库的一部分。
Optional实际上是个容器：它可以保存类型T的值，或者仅仅保存null。Optional提供很多有用的方法，这样我们就不用显式进行空值检测。
Optional类的Javadoc描述如下：这是一个可以为null的容器对象。如果值存在则isPresent()方法会返回true，调用get()方法会返回该对象。

## 常用的测试实现

Optional 避免了代码中的if else中空值的判断与处理。
    ```java
        Student student = new Student("张三");
        System.out.println(Optional.of(student).orElse(new Student("李四")).name);
        //张三
        Student student2 = null;
        System.out.println(Optional.ofNullable(student2).orElse(new Student("李四")).name);
        //李四
        System.out.println(Optional.ofNullable(student).map(a -> a.name));
        //Optional[张三]
        // ** flat的内部方法出参不一致
        System.out.println(Optional.ofNullable(student).flatMap(a -> Optional.of(a.name)));
        //Optional[张三]
    ```