# MiniDatabase
## 实习题目

迷你数据库管理系统        

## 实习环境

Intellij IDEA、命令提示符等   

## 实习要求

1. 掌握配置文件的使用方法。

2. 掌握文件操作和输入/输出流。

3. 掌握使用Object类进行通用编程的方法。

4. 掌握提供二次开发者通用API的编写方法。

5. 掌握关键词解析、正则表达式等文本分析技巧。

6. 掌握对象序列化的原理和方法。

7. 掌握并能运用Java的反射机制。

8. 掌握Comparable<T>接口，并将其应用于排序任务。

 

## 实习内容

### 1．实习背景介绍

结构化的数据库从上世纪70年代到现在一直是数据存取的主流。虽然近十年左右非结构化的数据库陆续出现，但大型软件中的数据存取的基础仍然是关系型数据库。

曾经流行的关系数据库包括DB2，Oracle、SQLServer、Access、MySQL等，现在常用的开源数据库是MySQL。我国自主研发了人大金仓数据库，但很遗憾没有流行起来。

关系数据的查询语言是SQL（Structural Query Language），使用该语言，可以在客户端上对数据库进行管理和对数据表进行增删改查等操作；各种编程语言一般都支持数据库的操作，也就是数据库管理系统提供API，程序向数据库管理系统发送SQL语句，得到反馈结果后，程序对反馈结果进行处理。

因此，一般的关系型数据库管理系统提供两种类型的交互：命令行交互（有时利用自带的或第三方软件可以进行图形界面操作）、API的交互方式。

此次实习我们要从底层设计一个数据库管理系统，不但实现命令行的SQL交互方式，也要实现相关的API。其实实现了交互API之后，命令行的方式做一层包装即可。

### 2．实习程序开发

（一）、建立个人子目录

**步骤1：**建立个人子目录

第一次上机时先在D盘上建立一个以自己学号+姓名为目录名的子目录，如学号为210824109的张三同学，就用“210824109张三”为子目录名。实习完成的源代码、Java字节码和实习报告三个文件都要放在这个文件夹下（称为上交文件夹）。

***\*步骤2：\****建立Java源代码文件

在所建立的文件夹下建立一个记事本文件TableManager.txt，并把它重命名为TableManager.java；可以建立任意多个辅助的其它类。

（二）、建立配置文件和数据表结构存取文件

***\*步骤1：\****创建配置文件

在当前运行目录下建立一个配置文件，“Config.properties”，程序中的所有配置信息都要到这个文件中获取。

***\*步骤2：\****创建数据表结构存取文件

数据表的结构信息存放在当前运行目录下，名称为“TableInfo.ti”，你可以让程序自己创建，也可以实现建立一个空的文件。

所谓的当前运行目录是指：系统启动时的目录。例如，你的.class文件是mypackage.MyMainClass，则执行时要使用命令java mypackage.MyMainClass，此时的当前目录不在目录mypackage中，而与目录mypackage同级，在同一个文件夹下。

（三）、编写代码和测试调试

除了TableManager类之外，可以添加任意多个辅助的类。

调试的时候，不但要成功运行命令行交互，而且要成功实现对API的调用。必须做到命令行交互的基础是调用API之后形成的，不能是单独一套。程序结构如下：

![img](file:///C:\Users\ZHOUZI~1\AppData\Local\Temp\ksohtml3652\wps1.png)

### 

## 实现方法

（1）.ti文件和.midb文件

① .ti文件：用于存放当前数据表的数量和所有数据表的表名。

② .midb文件：用于存放一个数据表的信息，其中包含了记录条数、字段个数、字段名称、字段类型以及所有记录。

具体如下图所示，每一块占据固定的可计算的位置SIZE。

![img](file:///C:\Users\ZHOUZI~1\AppData\Local\Temp\ksohtml3652\wps2.jpg) ![img](file:///C:\Users\ZHOUZI~1\AppData\Local\Temp\ksohtml3652\wps3.jpg)

图1.1 .ti文件和.midb文件的结构

（2）配置文件Config.properties

配置文件中包含存放所有数据表的文件夹路径dbHome、.ti文件的路径tiHome和关系运算符号列表operators。程序中的所有配置信息都要到这个文件中获取。

（3）构造方法

创建存放所有数据表的文件夹和.ti文件。一直循环，每次循环先输出“miDB>”，读取用户输入，进行不同的操作。当用户输入为空字符串时，进入下一轮循环；当用户输入为“quit”时，结束循环；当用户输入为“help”时，输出所有操作及其对应的含义，进入下一轮循环；当用户输入其他时，调用operate方法解析语句，输出解析后的语句，进入下一轮循环。

（4）operate方法

根据用户输入的第一个词，判断用户需要进行的操作，根据不同语句的不同正则表达式，判断用户输入的语句是否匹配成功。若匹配成功则调用对应的方法，否则则返回语法错误。返回解析后的结果。

（5）showTables方法

获取TableInfo.ti的第一个数据，即数据表的数量。循环遍历获取每一个数据表表名，返回包含所有数据表和数据表数量的字符串。

（6）descTable方法

查询是否存在该midb文件，若不存在则返回提示信息。否则，获取数据表字段的个数，遍历获取字段名称、字段类型和字段长度，返回字符串。

（7）createTable方法

查询是否存在该midb文件，若存在则返回提示信息。否则，将数据表表名写入.ti文件，并更新.ti文件中的数据表个数；创建.midb文件，将数据表的表结构写入.midb文件中。

（8）dropTable方法

查询是否存在该midb文件，若不存在则返回提示信息。否则，删除.midb文件，并更新.ti文件，返回删除成功的提示信息。

（9）selectData方法

查询是否存在该midb文件，若不存在则返回提示信息。否则，创建查询字段的列表，将所有信息存储到二维列表中，根据筛选和排序挑选对二维列表进行筛选和排序，返回格式化后的列表。

（10）insertTable方法

查询是否存在该midb文件，若不存在则返回提示信息。若列表数量与字段个数不匹配，返回提示信息。判断每一个记录的类型和插入记录的类型是否匹配，若不匹配，返回提示信息。否则插入该记录，更新.midb文件。

（11）updateTable方法

查询是否存在该midb文件，若不存在则返回提示信息。判断输入的字段名和字段类型是否匹配，若不匹配，返回提示信息。否则，更新所有匹配上的字段，更新.midb文件。



## 实习结果

（1）编译运行程序，根据配置文件Config.properties中存放所有数据表的文件夹路径和ti文件的路径，若对应的位置没有文件夹或文件，分别在对应的位置创建文件夹和文件。（如图2.1所示）

![img](file:///C:\Users\ZHOUZI~1\AppData\Local\Temp\ksohtml3652\wps4.jpg) 

图2.1 编译运行程序

（2）输入“help”（不区分大小写），查询所有操作介绍。（如图2.2所示）

![img](file:///C:\Users\ZHOUZI~1\AppData\Local\Temp\ksohtml3652\wps5.jpg) 

图2.2 help指令

（3）创建数据表，输入“create table Student(id int, name varchar(10), height decimal)”（其中create和table不区分大小写），创建名为Student的数据表，表中有3个字段分别为int类型的id、varchar类型最长为10的name、decimal类型的height。在包含所有数据表信息的文件夹下创建一个新的文件Student.midb用于存放数据表Student信息，将信息以写入文件中，并将Student写入TableInfo中。（如图2.3所示）

![img](file:///C:\Users\ZHOUZI~1\AppData\Local\Temp\ksohtml3652\wps6.jpg) 

![img](file:///C:\Users\ZHOUZI~1\AppData\Local\Temp\ksohtml3652\wps7.jpg) 

![img](file:///C:\Users\ZHOUZI~1\AppData\Local\Temp\ksohtml3652\wps8.jpg) 

图2.3 创建数据表

（4）向数据表写入记录。输入“insert into Student values(1, '张三', 180.5)”（其中insert、into、values不区分大小写）。当拼写出错时，提示“insert语法错误”；当类型与数据表的类型不匹配时，提示第几个参数所需要的类型；当字符串超过varchar的最长长度时，提示字符串长度过长。当输入正确时，提示向数据表写入一条记录成功，并对对应的.midb文件进行修改（如图2.4所示）

![img](file:///C:\Users\ZHOUZI~1\AppData\Local\Temp\ksohtml3652\wps9.jpg) 

![img](file:///C:\Users\ZHOUZI~1\AppData\Local\Temp\ksohtml3652\wps10.jpg) 

图2.4 写入记录

（5）显示所有数据表。输入“show tables”（其中show和tables不区分大小写），输出所有的数据表和数据表数量。（如图2.5所示）

![img](file:///C:\Users\ZHOUZI~1\AppData\Local\Temp\ksohtml3652\wps11.jpg) 

图2.5 显示所有数据表

（6）显示所有数据表。输入“desc table Student”（其中desc 和table不区分大小写），输出数据表的表结构。（如图2.6所示）

![img](file:///C:\Users\ZHOUZI~1\AppData\Local\Temp\ksohtml3652\wps12.jpg) 

图2.6 显示所有数据表

（7）筛选数据表。输入“select 字段名 from Student (where xxx) (order by xxx (desc/asc))”或带有（其中select、from、where和order by不区分大小写），输出满足条件的所有数据表。（如图2.7所示）

![img](file:///C:\Users\ZHOUZI~1\AppData\Local\Temp\ksohtml3652\wps13.jpg) 

![img](file:///C:\Users\ZHOUZI~1\AppData\Local\Temp\ksohtml3652\wps14.jpg)![img](file:///C:\Users\ZHOUZI~1\AppData\Local\Temp\ksohtml3652\wps15.jpg) 

![img](file:///C:\Users\ZHOUZI~1\AppData\Local\Temp\ksohtml3652\wps16.jpg) 

2.7 筛选数据表

（8）修改数据表。输入“update Student set height = 180.0 where height > 180”或带有（其中update、set、where不区分大小写），修改满足条件的所有数据表。（如2.8所示）

![img](file:///C:\Users\ZHOUZI~1\AppData\Local\Temp\ksohtml3652\wps17.jpg) 

![img](file:///C:\Users\ZHOUZI~1\AppData\Local\Temp\ksohtml3652\wps18.jpg) 

2.8 更新数据表

 

## 结论分析

通过本次课程设计，我掌握配置文件的使用方法、文件操作和输入/输出流、使用Object类进行通用编程的方法、提供二次开发者通用API的编写方法、关键词解析、正则表达式等文本分析技巧、掌握对象序列化的原理和方法，还掌握并能运用Java的反射机制和Comparable<T>接口，并将其应用于排序任务。但程序鲁棒性仍待加强。
