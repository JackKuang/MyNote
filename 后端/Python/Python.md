# Python3 

* **笔记说明：本笔记本并不记录全部内容，只做部分特殊内容**

## 一、简介

- **Python 是一种解释型语言：** 这意味着开发过程中没有了编译这个环节。类似于PHP和Perl语言。
- **Python 是交互式语言：** 这意味着，您可以在一个 Python 提示符 **>>>** 后直接执行代码。
- **Python 是面向对象语言:** 这意味着Python支持面向对象的风格或代码封装在对象的编程技术。
- **Python 是初学者的语言：**Python 对初级程序员而言，是一种伟大的语言，它支持广泛的应用程序开发，从简单的文字处理到 WWW 浏览器再到游戏。

## 二、基本语法

### 2.1 编码

```python
# -*- coding: cp-1252 -*-
# 指定编码
```

### 2.2 关键字

```python
>>> import keyword
>>> keyword.kwlist
['False', 'None', 'True', 'and', 'as', 'assert', 'break', 'class', 'continue', 'def', 'del', 'elif', 'else', 'except', 'finally', 'for', 'from', 'global', 'if', 'import', 'in', 'is', 'lambda', 'nonlocal', 'not', 'or', 'pass', 'raise', 'return', 'try', 'while', 'with', 'yield']
```

### 2.3 字符串

* 反斜杠可以用来转义，使用r可以让反斜杠不发生转义。。 如 r"this is a line with \n" 则\n会显示，并不是换行。
* 
  Python 中的字符串有两种索引方式，从左往右以 0 开始，从右往左以 -1 开始。
* Python中的字符串不能改变。
* Python 没有单独的字符类型，一个字符就是长度为 1 的字符串。
* 字符串的截取的语法格式如下：**变量[头下标:尾下标:步长]**

### 2.4 import 与 from ...  import

* 在 python 用 **import** 或者 **from...import** 来导入相应的模块。
  * 将整个模块(somemodule)导入，格式为： **import somemodule**
  * 从某个模块中导入某个函数,格式为： **from somemodule import somefunction**
  * 从某个模块中导入多个函数,格式为： **from somemodule import firstfunc, secondfunc, thirdfunc**
  * 将某个模块中的全部函数导入，格式为： **from somemodule import \***

## 三、基本数据类型

* Python3 中有六个标准的数据类型：
  * Number（数字）
  * String（字符串）
  * List（列表）
  * Tuple（元组）
  * Set（集合）
  * Dictionary（字典）

* Python3 的六个标准数据类型中：
  * **不可变数据（3 个）：**Number（数字）、String（字符串）、Tuple（元组）；
  * **可变数据（3 个）：**List（列表）、Dictionary（字典）、Set（集合）。

### 3.1 Number（数字）

* Python可以同时为多个变量赋值，如a, b = 1, 2。
* 一个变量可以通过赋值指向不同类型的对象。
* 数值的除法包含两个运算符：**/** 返回一个浮点数，**//** 返回一个整数。
* 在混合计算时，Python会把整型转换成为浮点数。

### 3.2 String（字符串）

* Python中的字符串用单引号 **'** 或双引号 **"** 括起来，同时使用反斜杠 **\** 转义特殊字符。
* Python 使用反斜杠(\)转义特殊字符，如果你不想让反斜杠发生转义，可以在字符串前面添加一个 r，表示原始字符串。
* 反斜杠(\)可以作为续行符，表示下一行是上一行的延续。也可以使用 **"""..."""** 或者 **'''...'''** 跨越多行。
* Python中的字符串有两种索引方式，从左往右以0开始，从右往左以-1开始。
* Python中的字符串不能改变。

### 3.3  List（列表）

* 和字符串一样，list可以被索引和切片。
* List可以使用+操作符进行拼接。
* List中的元素是可以改变的。

### 3.4 Tuple（元组）

* 元组（tuple）与列表类似，不同之处在于元组的元素不能修改。元组写在小括号 **()** 里，元素之间用逗号隔开。

### 3.5 Set（集合）

* 集合（set）是由一个或数个形态各异的大小整体组成的，构成集合的事物或对象称作元素或是成员。

* 基本功能是进行成员关系测试和删除重复元素。

* 可以使用大括号 **{ }** 或者 **set()** 函数创建集合，注意：创建一个空集合必须用 **set()** 而不是 **{ }**，因为 **{ }** 是用来创建一个空字典。

* ```python
  # set可以进行集合运算
  a = set('abracadabra')
  b = set('alacazam')
   
  print(a)
  print(a - b)     # a 和 b 的差集
  print(a | b)     # a 和 b 的并集
  print(a & b)     # a 和 b 的交集
  print(a ^ b)     # a 和 b 中不同时存在的元素
  
  
  {'b', 'a', 'c', 'r', 'd'}
  {'b', 'd', 'r'}
  {'l', 'r', 'a', 'c', 'z', 'm', 'b', 'd'}
  {'a', 'c'}
  {'l', 'r', 'z', 'm', 'b', 'd'}
  ```

### 3.6 Dictionary（字典）

* 列表是有序的对象集合，字典是无序的对象集合。两者之间的区别在于：字典当中的元素是通过键来存取的，而不是通过偏移存取。
* 
  字典是一种映射类型，字典用 **{ }** 标识，它是一个无序的 **键(key) : 值(value)** 的集合。
* 键(key)必须使用不可变类型。
* 键(key)必须是唯一、不可重复的。
* 创建空字典使用 **{ }**。

### 3.7 Python数据类型转换

| 函数                                                         | 描述                                                |
| :----------------------------------------------------------- | :-------------------------------------------------- |
| [int(x [,base])] | 将x转换为一个整数                                   |
| [float(x)] | 将x转换到一个浮点数                                 |
| [complex(real [,imag])] | 创建一个复数                                        |
| [str(x)]| 将对象 x 转换为字符串                               |
| [repr(x)]                                                    | 将对象 x 转换为表达式字符串                         |
| [eval(str)] | 用来计算在字符串中的有效Python表达式,并返回一个对象 |
| [tuple(s)]| 将序列 s 转换为一个元组                             |
| [list(s)] | 将序列 s 转换为一个列表                             |
| [set(s)]| 转换为可变集合                                      |
| [dict(d)] | 创建一个字典。d 必须是一个 (key, value)元组序列。   |
| [frozenset(s)]| 转换为不可变集合                                    |
| [chr(x)]| 将一个整数转换为一个字符                            |
| [ord(x)] | 将一个字符转换为它的整数值                          |
| [hex(x)]| 将一个整数转换为一个十六进制字符串                  |
| [oct(x)] | 将一个整数转换为一个八进制字符串                    |

## 四、运算符

### 4.1 算术运算符

* a = 10,b =20;

  | /    | 除 - x除以y                               | b / a 输出结果 2                                   |
  | ---- | ----------------------------------------- | -------------------------------------------------- |
  | %    | 取模 - 返回除法的余数                     | b % a 输出结果 0                                   |
  | **   | 幂 - 返回x的y次幂                         | a**b 为10的20次方， 输出结果 100000000000000000000 |
  | //   | 取整除 - 返回商的整数部分（**向下取整**） | >>> 9//2 4< >>> -9//2 -5                         |

### 4.2 逻辑运算符

* a = 10 ,b = 20;

  | 算符 | 逻辑表达式 | 描述                                                         | 实例                    |
  | :--- | :--------- | :----------------------------------------------------------- | :---------------------- |
  | and  | x and y    | 布尔"与" - 如果 x 为 False，x and y 返回 False，否则它返回 y 的计算值。 | **(a and b) 返回 20。** |
  | or   | x or y     | 布尔"或" - 如果 x 是非 0，它返回 x 的值，否则它返回 y 的计算值。 | **(a or b) 返回 10。**  |
  | not  | not x      | 布尔"非" - 如果 x 为 True，返回 False 。如果 x 为 False，它返回 True。 | not(a and b) 返回 False |

### 4.3 成员运算符

* | 运算符 | 描述                                                    | 实例                                              |
  | :----- | :------------------------------------------------------ | :------------------------------------------------ |
  | in     | 如果在指定的序列中找到值返回 True，否则返回 False。     | x 在 y 序列中 , 如果 x 在 y 序列中返回 True。     |
  | not in | 如果在指定的序列中没有找到值返回 True，否则返回 False。 | x 不在 y 序列中 , 如果 x 不在 y 序列中返回 True。 |

### 4.4 身份运算符

* 身份运算符用于比较两个对象的存储单元

  | 运算符 | 描述                                        | 实例                                                         |
  | :----- | :------------------------------------------ | :----------------------------------------------------------- |
  | is     | is 是判断两个标识符是不是引用自一个对象     | **x is y**, 类似 **id(x) == id(y)** , 如果引用的是同一个对象则返回 True，否则返回 False |
  | is not | is not 是判断两个标识符是不是引用自不同对象 | **x is not y** ， 类似 **id(a) != id(b)**。如果引用的不是同一个对象则返回结果 True，否则返回 False。 |

## 五、Number（数字）

* **Python 支持三种不同的数值类型：**

  * **整型(Int)** - 通常被称为是整型或整数，是正或负整数，不带小数点。Python3 整型是没有限制大小的，可以当作 Long 类型使用，所以 Python3 没有 Python2 的 Long 类型。
  * **浮点型(float)** - 浮点型由整数部分与小数部分组成，浮点型也可以使用科学计数法表示（2.5e2 = 2.5 x 102 = 250）
  * **复数( (complex))** - 复数由实数部分和虚数部分构成，可以用a + bj,或者complex(a,b)表示， 复数的实部a和虚部b都是浮点型。

* **Python 数字类型转换**

  - **int(x)** 将x转换为一个整数。
  - **float(x)** 将x转换到一个浮点数。
  - **complex(x)** 将x转换到一个复数，实数部分为 x，虚数部分为 0。
  - **complex(x, y)** 将 x 和 y 转换到一个复数，实数部分为 x，虚数部分为 y。x 和 y 是数字表达式。

* **数学函数**

  | 函数                                                         | 返回值 ( 描述 )                                              |
  | :----------------------------------------------------------- | :----------------------------------------------------------- |
  | [abs(x)](https://www.runoob.com/python3/python3-func-number-abs.html) | 返回数字的绝对值，如abs(-10) 返回 10                         |
  | [ceil(x)](https://www.runoob.com/python3/python3-func-number-ceil.html) | 返回数字的上入整数，如math.ceil(4.1) 返回 5                  |
  | cmp(x, y)                                                    | 如果 x < y 返回 -1, 如果 x == y 返回 0, 如果 x > y 返回 1。 **Python 3 已废弃，使用 (x>y)-(x<y) 替换**。 |
  | [exp(x)](https://www.runoob.com/python3/python3-func-number-exp.html) | 返回e的x次幂(ex),如math.exp(1) 返回2.718281828459045         |
  | [fabs(x)](https://www.runoob.com/python3/python3-func-number-fabs.html) | 返回数字的绝对值，如math.fabs(-10) 返回10.0                  |
  | [floor(x)](https://www.runoob.com/python3/python3-func-number-floor.html) | 返回数字的下舍整数，如math.floor(4.9)返回 4                  |
  | [log(x)](https://www.runoob.com/python3/python3-func-number-log.html) | 如math.log(math.e)返回1.0,math.log(100,10)返回2.0            |
  | [log10(x)](https://www.runoob.com/python3/python3-func-number-log10.html) | 返回以10为基数的x的对数，如math.log10(100)返回 2.0           |
  | [max(x1, x2,...)](https://www.runoob.com/python3/python3-func-number-max.html) | 返回给定参数的最大值，参数可以为序列。                       |
  | [min(x1, x2,...)](https://www.runoob.com/python3/python3-func-number-min.html) | 返回给定参数的最小值，参数可以为序列。                       |
  | [modf(x)](https://www.runoob.com/python3/python3-func-number-modf.html) | 返回x的整数部分与小数部分，两部分的数值符号与x相同，整数部分以浮点型表示。 |
  | [pow(x, y)](https://www.runoob.com/python3/python3-func-number-pow.html) | x**y 运算后的值。                                            |
  | [round(x [,n\])](https://www.runoob.com/python3/python3-func-number-round.html) | 返回浮点数x的四舍五入值，如给出n值，则代表舍入到小数点后的位数。 |
  | [sqrt(x)](https://www.runoob.com/python3/python3-func-number-sqrt.html) | 返回数字x的平方根。                                          |

* **随机数函数**

  | 函数                                                         | 描述                                                         |
  | :----------------------------------------------------------- | :----------------------------------------------------------- |
  | [choice(seq)](https://www.runoob.com/python3/python3-func-number-choice.html) | 从序列的元素中随机挑选一个元素，比如random.choice(range(10))，从0到9中随机挑选一个整数。 |
  | [randrange ([start,\] stop [,step])](https://www.runoob.com/python3/python3-func-number-randrange.html) | 从指定范围内，按指定基数递增的集合中获取一个随机数，基数默认值为 1 |
  | [random()](https://www.runoob.com/python3/python3-func-number-random.html) | 随机生成下一个实数，它在[0,1)范围内。                        |
  | [seed([x\])](https://www.runoob.com/python3/python3-func-number-seed.html) | 改变随机数生成器的种子seed。如果你不了解其原理，你不必特别去设定seed，Python会帮你选择seed。 |
  | [shuffle(lst)](https://www.runoob.com/python3/python3-func-number-shuffle.html) | 将序列的所有元素随机排序                                     |
  | [uniform(x, y)](https://www.runoob.com/python3/python3-func-number-uniform.html) | 随机生成下一个实数，它在[x,y]范围内。                        |

* **三角函数**

  | 函数                                                         | 描述                                              |
  | :----------------------------------------------------------- | :------------------------------------------------ |
  | [acos(x)](https://www.runoob.com/python3/python3-func-number-acos.html) | 返回x的反余弦弧度值。                             |
  | [asin(x)](https://www.runoob.com/python3/python3-func-number-asin.html) | 返回x的反正弦弧度值。                             |
  | [atan(x)](https://www.runoob.com/python3/python3-func-number-atan.html) | 返回x的反正切弧度值。                             |
  | [atan2(y, x)](https://www.runoob.com/python3/python3-func-number-atan2.html) | 返回给定的 X 及 Y 坐标值的反正切值。              |
  | [cos(x)](https://www.runoob.com/python3/python3-func-number-cos.html) | 返回x的弧度的余弦值。                             |
  | [hypot(x, y)](https://www.runoob.com/python3/python3-func-number-hypot.html) | 返回欧几里德范数 sqrt(x*x + y*y)。                |
  | [sin(x)](https://www.runoob.com/python3/python3-func-number-sin.html) | 返回的x弧度的正弦值。                             |
  | [tan(x)](https://www.runoob.com/python3/python3-func-number-tan.html) | 返回x弧度的正切值。                               |
  | [degrees(x)](https://www.runoob.com/python3/python3-func-number-degrees.html) | 将弧度转换为角度,如degrees(math.pi/2) ， 返回90.0 |
  | [radians(x)](https://www.runoob.com/python3/python3-func-number-radians.html) | 将角度转换为弧度                                  |

* **数学常量**

  | 常量 | 描述                                  |
  | :--- | :------------------------------------ |
  | pi   | 数学常量 pi（圆周率，一般以π来表示）  |
  | e    | 数学常量 e，e即自然常数（自然常数）。 |

* Python默认提供了很多函数，这些函数来说，都是比较简单、常用的函数。

## 六、String（字符串）

* **字符串格式化**

  | 符   号 | 描述                                 |
  | :------ | :----------------------------------- |
  | %c      | 格式化字符及其ASCII码                |
  | %s      | 格式化字符串                         |
  | %d      | 格式化整数                           |
  | %u      | 格式化无符号整型                     |
  | %o      | 格式化无符号八进制数                 |
  | %x      | 格式化无符号十六进制数               |
  | %X      | 格式化无符号十六进制数（大写）       |
  | %f      | 格式化浮点数字，可指定小数点后的精度 |
  | %e      | 用科学计数法格式化浮点数             |
  | %E      | 作用同%e，用科学计数法格式化浮点数   |
  | %g      | %f和%e的简写                         |
  | %G      | %f 和 %E 的简写                      |
  | %p      | 用十六进制数格式化变量的地址         |

* **三引号**

  ```python
  #!/usr/bin/python3
   
  para_str = """这是一个多行字符串的实例
  多行字符串可以使用制表符
  TAB ( \t )。
  也可以使用换行符 [ \n ]。
  """
  print (para_str)
  
  ---------
  
  这是一个多行字符串的实例
  多行字符串可以使用制表符
  TAB (    )。
  也可以使用换行符 [ 
   ]。
  ```

  ```
  三引号让程序员从引号和特殊字符串的泥潭里面解脱出来，自始至终保持一小块字符串的格式是所谓的WYSIWYG（所见即所得）格式的。
  
  一个典型的用例是，当你需要一块HTML或者SQL时，这时用字符串组合，特殊字符串转义将会非常的繁琐。
  ```

* **f-string**

  ```python
  >>> x = 1
  >>> print(f'{x+1}')   # Python 3.6
  2
  
  >>> x = 1
  >>> print(f'{x+1=}')   # Python 3.8
  'x+1=2'
  ```

* Unicode 字符串

  在Python2中，普通字符串是以8位ASCII码进行存储的，而Unicode字符串则存储为16位unicode字符串，这样能够表示更多的字符集。使用的语法是在字符串前面加上前缀 **u**。

  在Python3中，所有的字符串都是Unicode字符串。

## 七、List（列表）

* **删除列表元素**

  ```python
  del list[2]
  ```

* **列表脚本操作符**

  | Python 表达式                         | 结果                         | 描述                 |
  | :------------------------------------ | :--------------------------- | :------------------- |
  | len([1, 2, 3])                        | 3                            | 长度                 |
  | [1, 2, 3] + [4, 5, 6]                 | [1, 2, 3, 4, 5, 6]           | 组合                 |
  | ['Hi!'] * 4                           | ['Hi!', 'Hi!', 'Hi!', 'Hi!'] | 重复                 |
  | 3 in [1, 2, 3]                        | True                         | 元素是否存在于列表中 |
  | for x in [1, 2, 3]: print(x, end=" ") | 1 2 3                        | 迭代                 |

## 八、Tuple（元组）

* Python 的元组与列表类似，不同之处在于元组的元素不能修改。
* 元组使用小括号，列表使用方括号。
* 元组创建很简单，只需要在括号中添加元素，并使用逗号隔开即可。

* 元组中只包含一个元素时，需要在元素后面添加逗号，否则括号会被当作运算符使用：

  ```python
  >>>tup1 = (50)
  >>> type(tup1)     # 不加逗号，类型为整型
  <class 'int'>
   
  >>> tup1 = (50,)
  >>> type(tup1)     # 加上逗号，类型为元组
  <class 'tuple'>
  ```

## 九、Dictory（字典）

* 字典是另一种可变容器模型，且可存储任意类型对象。
* 感觉类似于List，List索引是数字，这个是任意类型对象

## 十、Set（集合）

* 集合（set）是一个无序的不重复元素序列。
* 可以使用大括号 **{ }** 或者 **set()** 函数创建集合，注意：创建一个空集合必须用 **set()** 而不是 **{ }**，因为 **{ }** 是用来创建一个空字典。

- 可以使用大括号 **{ }** 或者 **set()** 函数创建集合，注意：创建一个空集合必须用 **set()** 而不是 **{ }**，因为 **{ }** 是用来创建一个空字典。