利用vertx实现一些小demo

java version:11

vertx version:4.0.0-milestone4

yamibo模块实现对php论坛的模拟登陆爬虫

#利用openssl生成rsa密钥
openssl genrsa -out private.pem 2048 #生成rsa私钥，X509编码，2048位
#标准JDK无法按原样读取此文件，必须先将其转换为PKCS8格式：
openssl pkcs8 -topk8 -inform PEM -in private.pem -out private_key.pem -nocrypt
#从私钥文件中提取公钥，X509编码：
openssl rsa -in private.pem -outform PEM -pubout -out public_key.pem