# alias 命令行别名
 alias new_name="command" 添加
 unalias new_name 删除
# 常用的alias
 1. 应用实用 / 应用程序：
 alias install="sudo yum install -y"
 2. 更新系统：
 alias update="sudo yum update -y"
 3. 升级系统：
 alias upgrade="sudo yum upgrade -y"
 4. 换 root 用户：
 alias root="sudo su -"
 5. 切换到 “user” 用户, 其中 user 设置为你的用户名：
 alias user="su user"
 6. 显示列出所有可用端口、状态还有 IP：
 alias myip="ip -br -c a"
 7. ssh 到你的服务器 myserver：
 alias myserver="ssh user@my_server_ip”
 8. 列出系统中所有进程：
 alias process="ps -aux"
 9. 检查系统的服务状态：
 alias sstatus="sudo systemctl status"
 10. 重启系统服务：
 alias srestart="sudo systemctl restart"
 11. 按名称杀死进程：
 alias kill="sudo pkill"
 12. 显示系统系统总使用内存和空闲内存 ：
 alias mem="free -h"
 13. 示系统 CPU 框架结构、CPU 数量、线程数等：
 alias cpu="lscpu"
 14. 显示系统总磁盘大小：
 alias disk="df -h"
 15. 显示当前系统 Linux 发行版本（适用于 CentOS、Fedora 和 Red Hat)：
 alias os="cat /etc/redhat-release"
