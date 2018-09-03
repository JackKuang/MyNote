# 使用Keytool工具生成证书及签名完整步骤
    1.创建证书库(keystore)及证书(Certificate)
    2.生成证书签名请求(CSR)
    3.将已签名的证书导入证书库
# 创建证书库(keystore)及证书(Certificate)
命令如下：
```
keytool -genkey -alias cms.hurenjieee.com -keyalg RSA –keysize 4096  -keypass password -sigalg SHA256withRSA -dname "cn=Jack,ou=hurenjieee,o=hurenjieee,l=Hangzhou,st=Zhejiang,c=CN" -storetype JKS -storepass password -keystore E:/.keystore
```
```
keytool -genkeypair \
        -alias www.mydomain.com \
        -keyalg RSA \
        –keysize 4096 \
        -keypass mypassword \
        -sigalg SHA256withRSA \
        -dname "cn=www.mydomain.com,ou=xxx,o=xxx,l=Beijing,st=Beijing,c=CN" \ 
        -validity 3650 \
        -keystore www.mydomain.com_keystore.jks \
        -storetype JKS \
        -storepass mypassword
```
解释：
- keytool 是jdk提供的工具，该工具名为”keytool“
- -alias www.mydomain.com 此处”www.mydomain.com“为别名，可以是任意字符，只要不提示错误即可。因一个证书库中可以存放多个证书，通过别名标识证书。
- -keyalg RSA 此处”RSA“为密钥的算法。可以选择的密钥算法有：RSA、DSA、EC。
- –keysize 4096 此处”4096“为密钥长度。keysize与keyalg默认对应关系： 
2048 (when using -genkeypair and -keyalg is “RSA”) 
1024 (when using -genkeypair and -keyalg is “DSA”) 
256 (when using -genkeypair and -keyalg is “EC”)
- -keypass mypassword 此处”mypassword “为本条目的密码(私钥的密码)。最好与storepass一致。
- -sigalg SHA256withRSA 此处”SHA256withRSA“为签名算法。keyalg=RSA时，签名算法有：MD5withRSA、SHA1withRSA、SHA256withRSA、SHA384withRSA、SHA512withRSA。keyalg=DSA时，签名算法有：SHA1withDSA、SHA256withDSA。此处需要注意：MD5和SHA1的签名算法已经不安全。
- -dname “cn=www.mydomain.com,ou=xxx,o=xxx,l=Beijing,st=Beijing,c=CN” 在此填写证书信息。”CN=名字与姓氏/域名,OU=组织单位名称,O=组织名称,L=城市或区域名称,ST=州或省份名称,C=单位的两字母国家代码”
- -validity 3650 此处”3650“为证书有效期天数。
- -keystore www.mydomain.com_keystore.jks 此处”www.mydomain.com_keystore.jks“为密钥库的名称。此处也给出绝对路径。默认在当前目录创建证书库。
- -storetype JKS 此处”JKS “为证书库类型。可用的证书库类型为：JKS、PKCS12等。jdk9以前，默认为JKS。自jdk9开始，默认为PKCS12。
- storepass mypassword 此处”mypassword “为证书库密码(私钥的密码)。最好与keypass 一致。
说明： 
- 上述命令，需要将 -dname 参数替换（尤其时域名要写对）、密码更改即可，其它可保持不变
# 生成证书签名请求(CSR)
```
keytool -certreq -keyalg RSA \
        -alias www.mydomain.com \
        -keystore www.mydomain.com_keystore.jks \
        -storetype JKS \
        -storepass mypassword \
        -file www.mydomain.com_certreq.csr
```
解释： 
- -file www.mydomain.com_certreq.csr 此处”www.mydomain.com_certreq.csr “为证书签名请求文件。
说明： 
- 将”www.mydomain.com_certreq.csr “文件发送给证书签名机构，然后等待证书签名机构将签名的证书发回，再进行下一步。
- 

# 将已签名的证书导入证书库
如果到了这步，应该会拿到两个证书。一个是签名机构的根证书（假定为GlobalSign_cert.cer），一个是www.mydomain.com的已签名证书（假定为www.mydomain.com_cert.cer）。两个证书均导入到证书库（www.mydomain.com_keystore.jks）中。

导入签名机构的根证书：
```
keytool -import -trustcacerts \
        -keystore www.mydomain.com_keystore.jks \
        -storepass mypassword \
        -alias root_GlobalSign \
        -file GlobalSign_cert.cer
```
说明： 
- alias和file两个参数进行替换。

# 导入www.mydomain.com的已签名证书
```
keytool -import -trustcacerts \
        -keystore www.mydomain.com_keystore.jks \
        -storepass mypassword \
        -alias www.mydomain.com \
        -file www.mydomain.com_cert.cer
```
说明： 
- -alias参数要与生成时一致，file参数进行替换。

# 辅助命令
查看证书库
```
keytool -list -v \
        -keystore www.mydomain.com_keystore.jks \
        -storepass mypassword
```
查看证书签名请求
```
keytool -printcertreq  -file www.mydomain.com_certreq.csr
```
查看已签名证书
```
keytool -printcert -file GlobalSign_cert.cer
keytool -printcert -file www.mydomain.com_cert.cer
```