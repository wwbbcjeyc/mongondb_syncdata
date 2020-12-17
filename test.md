打包
nohup java -jar mogodb_syncdata-1.0-SNAPSHOT.jar > /root/work/1.txt >&1 &
运行jar包后多出两份文件，监控日志文件
tail -f mongodb_data_log.log 
