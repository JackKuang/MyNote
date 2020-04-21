# AWK

AWK是一种处理文本文件的语言，是一个强大的文本分析工具。

## 一、基本用法

* log.txt

  ```
  2 this is a test
  3 Are you like awk
  This's a test
  10 There are orange,apple,mongo
  ```

### 1.1 用法一：提取文本内容

* ```sh
  # 行匹配语句 awk '' 只能用单引号
  awk '{[pattern] action}' {filenames}
  ```

* ```sh
  awk '{print $1,$4}' log.txt
  
  2 a
  3 like
  This's 
  10 orange,apple,mongo
```
  
* ```sh
  # 格式化输出
  awk '{printf "%-8s %-10s\n",$1,$4}' log.txt
  
  2        a         
  3        like      
  This's             
  10       orange,apple,mongo
  
  ```

### 1.2 分割字符串

* ```sh
  # 使用","分割
  awk -F , '{print $1,$2}' log.txt
  
  2 this is a test 
  3 Are you like awk 
  This's a test 
  10 There are orange apple
```
  
* ```sh
  # 或者使用内建变量
  awk 'BEGIN{FS=","} {print $1,$2}' log.txt
  
  2 this is a test 
  3 Are you like awk 
  This's a test 
  10 There are orange apple
```
  
* ```
  #使用多个分隔符.先使用空格分割，然后对分割结果再使用","分割
  awk -F '[ ,]' '{print $1,$2,$5}' log.txt
  
  2 this test
  3 Are awk
  This's a 
  10 There apple
  ```

### 1.3 设置变量

* ```sh
  # 设置变量
  awk -va=1 '{print $1,$1+a}' log.txt
  
  2 3
  3 4
  This's 1
  10 11
```
  
* ```sh
  # 设置变量
   awk -va=1 -vb=s '{print $1,$1+a,$1b}' log.txt
   
   2 3 2s
   3 4 3s
   This's 1 This'ss
   10 11 10s
```
  
### 1.4 使用脚本
  * ```
    awk -f {awk脚本} {文件名}
    ```

## 二、运算符

| 运算符                  | 描述                             |
| :---------------------- | :------------------------------- |
| = += -= *= /= %= ^= **= | 赋值                             |
| ?:                      | C条件表达式                      |
| \|\|                    | 逻辑或                           |
| &&                      | 逻辑与                           |
| ~ 和 !~                 | 匹配正则表达式和不匹配正则表达式 |
| < <= > >= != ==         | 关系运算符                       |
| 空格                    | 连接                             |
| + -                     | 加，减                           |
| * / %                   | 乘，除与求余                     |
| + - !                   | 一元加，减和逻辑非               |
| ^ ***                   | 求幂                             |
| ++ --                   | 增加或减少，作为前缀或后缀       |
| $                       | 字段引用                         |
| in                      | 数组成员                         |

* 过滤第一列大于2的行

  ```sh
  awk '$1>2' log.txt
  
  3 Are you like awk
  This's a test
  10 There are orange,apple,mongo
  ```

* 过滤第一列等于2的行

  ```sh
  awk '$1==2 {print $1,$3}' log.txt
  
  2 is
  ```

* 过滤第一列大于2并且第二列等于'Are'的行

  ```sh
  awk '$1>2 && $2=="Are" {print $1,$2,$3}' log.txt
  
  3 Are you
  ```

## 三、内建变量

| 变量        | 描述                                                       |
| :---------- | :--------------------------------------------------------- |
| $n          | 当前记录的第n个字段，字段间由FS分隔                        |
| $0          | 完整的输入记录                                             |
| ARGC        | 命令行参数的数目                                           |
| ARGIND      | 命令行中当前文件的位置(从0开始算)                          |
| ARGV        | 包含命令行参数的数组                                       |
| CONVFMT     | 数字转换格式(默认值为%.6g)ENVIRON环境变量关联数组          |
| ERRNO       | 最后一个系统错误的描述                                     |
| FIELDWIDTHS | 字段宽度列表(用空格键分隔)                                 |
| FILENAME    | 当前文件名                                                 |
| FNR         | 各文件分别计数的行号                                       |
| FS          | 字段分隔符(默认是任何空格)                                 |
| IGNORECASE  | 如果为真，则进行忽略大小写的匹配                           |
| NF          | 一条记录的字段的数目                                       |
| NR          | 已经读出的记录数，就是行号，从1开始                        |
| OFMT        | 数字的输出格式(默认值是%.6g)                               |
| OFS         | 输出记录分隔符（输出换行符），输出时用指定的符号代替换行符 |
| ORS         | 输出记录分隔符(默认值是一个换行符)                         |
| RLENGTH     | 由match函数所匹配的字符串的长度                            |
| RS          | 记录分隔符(默认是一个换行符)                               |
| RSTART      | 由match函数所匹配的字符串的第一个位置                      |
| SUBSEP      | 数组下标分隔符(默认值是/034)                               |

* ```sh
  awk 'BEGIN{printf "%4s %4s %4s %4s %4s %4s %4s %4s %4s\n","FILENAME","ARGC","FNR","FS","NF","NR","OFS","ORS","RS"; printf "---------------------------------------------\n"} {printf "%4s %4s %4s %4s %4s %4s %4s %4s %4s\n", FILENAME,ARGC,FNR,FS,NF,NR,OFS,ORS,RS}' log.txt
  FILENAME ARGC  FNR   FS   NF   NR  OFS  ORS   RS
  ---------------------------------------------
  log.txt    2    1         5    1         
      
  
  log.txt    2    2         5    2         
      
  
  log.txt    2    3         3    3         
      
  
  log.txt    2    4         4    4
  ```

* ```sh
  # 输出顺序号 NR, 匹配文本行号
  awk '{print NR,FNR,$1,$2,$3}' log.txt
  
  1 1 2 this is
  2 2 3 Are you
  3 3 This's a test
  4 4 10 There are
  ```

* ```sh
  # 指定输出分割符
  awk '{print NR,FNR,$1,$2,$3}' OFS=' $ ' log.txt
  1 $ 1 $ 2 $ this $ is
  2 $ 2 $ 3 $ Are $ you
  3 $ 3 $ This's $ a $ test
  4 $ 4 $ 10 $ There $ are
  
  awk 'BEGIN{OFS=" $ "}{print NR,FNR,$1,$2,$3}' log.txt
  1 $ 1 $ 2 $ this $ is
  2 $ 2 $ 3 $ Are $ you
  3 $ 3 $ This's $ a $ test
  4 $ 4 $ 10 $ There $ are
  
  ```

## 四、使用正则，字符串匹配

* ```sh
  # 输出第二列包含 "th"，并打印第二列与第四列
  # ~ 表示模式开始。// 中是模式。
  awk '$2 ~ /th/ {print $2,$4}' log.txt
  
  this a
  ```

* ```sh
  awk '/,/' log.txt
  
  10 There are orange,apple,mongo
  
  awk '/\w{3}/ ' log.txt
  
  2 this is a test
  3 Are you like awk
  This's a test
  10 There are orange,apple,mongo
  ```

## 五、模式取反

! 用作取反

* ```sh
  # 正则取反
  awk '$2 !~ /th/ {print $2,$4}' log.txt
  
  Are like
  a 
  There orange,apple,mongo
  ```

* ```sh
  awk '!/\w{5}/' log.txt
  
  2 this is a test
  3 Are you like awk
  This's a test
  ```

## 六、实例

* 打印hello world

  ```sh
  awk 'BEGIN { print "Hello, world!" }'
  Hello, world!
  ```

* 计算文件大小

  ```
  ls -l /root/sh | awk '{sum+=$5} END {print sum}'
  ```

* 从文件中找出长度大于80的行

  ```sh
  awk 'length>80' log.txt
  ```

* 打印九九乘法表

  ```sh
  seq 9 | sed 'H;g' | awk -v RS='' '{for(i=1;i<=NF;i++)printf("%dx%d=%d%s", i, NR, i*NR, i==NR?"\n":"\t")}'
  ```

  