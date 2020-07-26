# macOS中Terminal使用技巧

### 一、Alias

* 这个命令在linux中最初是在Linux中使用到，但是大部分时间中，用到的命令很少真正使用到，因为命令都是那么几个。

* 切换到macOS之后，这条命令就体现了作用，因为除了系统相关的命令之外，还有很多app相关的命令操作，比方说VSCode中的code命令（虽然code命令可以在VSCode功能中部署），或是命令行启动Typora

* ```sh
  alias ll="ls -l"
  alias typora="open -a Typora"
  ```

### 二、open

* 在Terminal上，经常需要打开文件夹操作。这个时候，只要输入`type .`，就可以在Flinder。