#安装所需工具
yum install -y expect

yum install -y epel-release
yum install -y pssh
#免密登录
[ ! -f /root/.ssh/id_rsa.pub ] && ssh-keygen -t rsa -P '' &>/dev/null  # 密钥对不存在则创建密钥
while read line;do
        ip=`echo $line | cut -d " " -f1`             # 提取文件中的ip
        user_name=`echo $line | cut -d " " -f2`      #提取文件中的用户名
        pass_word=`echo $line | cut -d " " -f3`      # 提取文件中的密码
expect <<EOF
        spawn ssh-copy-id -i /root/.ssh/id_rsa.pub $user_name@$ip      
        expect {
                "yes/no" { send "yes\n";exp_continue}    
                "password" { send "$pass_word\n"}
        }
        expect eof
EOF

done < /root/host_ip.txt      # 读取存储ip的文件