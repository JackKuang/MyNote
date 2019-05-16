## 引入相关得包
```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-quartz</artifactId>
    </dependency>

````
## 开启定时任务注解
```java
    @Configuration
    @EnableScheduling
    public class SpringConfig {
    
    }
```

或者使用xml配置

```xml
    <task:annotation-driven>
```

## 配置不同的任务规则

```java
    @Scheduled(fixedDelay = 1000)
    public void scheduleFixedDelayTask() {
        System.out.println(
        "Fixed delay task - " + System.currentTimeMillis() / 1000);
    }

    @Scheduled(fixedRate = 1000)
    public void scheduleFixedRateTask() {
        System.out.println(
        "Fixed rate task - " + System.currentTimeMillis() / 1000);
    }
```
两个都是间隔多少ms执行，这里需要注意一下，fixedDelay总是在等待上一个执行结束之后，才会执行第二个。而fixedRate则不会等待。
针对使用场景来决定使用的哪一个，fixedDalay可以避免同时运行的问题，但是时间不可控制。而fixedRate可以保证时间，但是不安全。

```java
    @Scheduled(fixedDelay = 1000, initialDelay = 1000)
    public void scheduleFixedRateWithInitialDelayTask() {
    
        long now = System.currentTimeMillis() / 1000;
        System.out.println(
        "Fixed rate task with one second initial delay - " + now);
    }
```

**initialDelay**：第一次启动前等待多少ms才会启动。



```java
@Scheduled(cron = "0 15 10 15 * ?")
public void scheduleTaskUsingCronExpression() {
  
    long now = System.currentTimeMillis() / 1000;
    System.out.println(
      "schedule tasks using cron jobs - " + now);
}
```

下面这个是额外的XML配置
```xml
    <!-- Configure the scheduler -->
    <task:scheduler id="myScheduler" pool-size="10" />
    
    <!-- Configure parameters -->
    <task:scheduled-tasks scheduler="myScheduler">
        <task:scheduled ref="beanA" method="methodA"
        fixed-delay="5000" initial-delay="1000" />
        <task:scheduled ref="beanB" method="methodB"
        fixed-rate="5000" />
        <task:scheduled ref="beanC" method="methodC"
        cron="*/5 * * * * MON-FRI" />
    </task:scheduled-tasks>
```

cron 表达式，具体使用[Cron在线表达式](http://cron.qqe2.com/)

[Spring Scheduled Task教程](https://www.baeldung.com/spring-scheduled-tasks)