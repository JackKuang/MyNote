/**
 * 饿汉式
 * 
 */
public class SingleDemo {
    private static SingleDemo instance = new SingleDemo();
    //私有化构造器
    private SingleDemo() {
        //防止其他通过反射调用构造方法，破解单例
        if (instance != null) {
            throw new RuntimeException();
        }
    }

    //对外提供统一的访问点
    public static SingleDemo getInstance() {
        return instance;
    }
}

/*
优点
1.实例的初始化由JVM装载类的时候进行，保证了线程的安全性
2.实现简单方便
3.实例的访问效率高

缺点
1.不能实现懒加载，如果不调用getInstance(),那么这个类就白白的占据内存，资源的利用率不高

注意
1.防止通过反射调用构造方法破解单例模式。
2.防止通过反序列产生新的对象。
*/

/**
 * 懒汉式实现单例
 *
 */
public class SingleDemo2 {
    // 此处并不初始化实例
    private static SingleDemo2 instance;

    private SingleDemo2() {
        if (instance != null) {
            throw new RuntimeException();
        }
    }

    /**
     * 当调用此方法的时候才初始化实例， 为了实现线程安全，需要使用同步方法
     * 
     * @return
     */
    public static synchronized SingleDemo2 getInstance() {
        if (instance == null) {
            instance = new SingleDemo2();
        }
        return instance;
    }
}
/*

优点
1.只有使用这个类的时候才初始化实例，优化了资源利用率

缺点
1.为了实现线程安全，使用了同步方法获取，增加了访问的开销

注意
1.防止通过反射调用构造方法破解单例模式。
2.防止通过反序列产生新的对象。
*/