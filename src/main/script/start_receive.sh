#!/bin/sh
:>nohup.out
nohup java -Xmx2048m -Xms2048m -Xmn512m -XX:+HeapDumpOnOutOfMemoryError -XX:+UseConcMarkSweepGC -XX:CMSFullGCsBeforeCompaction=5 -XX:+UseCMSCompactAtFullCollection -jar -Dlogback.configurationFile=resources/logback_receive.xml rsyslog_tj.jar 0  > nohup_receive.out 2>&1 &
 
 