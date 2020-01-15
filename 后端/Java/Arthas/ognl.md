# OGNL

* 在使用Alibaba的Arthes时，看到了有使用ognl表达式。
* Arthas通过ognl表达式来
  1. **获取静态类的静态字段**、
  2. **调用静态函数**。
  3. **查询内置对象**

## 一、介绍

* OGNL的全称是**Object Graph Navigation Language(对象图导航语言)**

* OGNL表达式是一个独立的语言，**strut2**将其引入共同构造。
* OGNL语言强大于EL表达式，其可以访问java类中的对象，也可以访问对象的静态方法。