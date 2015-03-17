#!/bin/sh
:>nohup.out
nohup java -Xmx4096m -Xms4096m -Xmn512m -XX:+HeapDumpOnOutOfMemoryError -XX:+UseConcMarkSweepGC -XX:CMSFullGCsBeforeCompaction=5 -XX:+UseCMSCompactAtFullCollection -jar -Dlogback.configurationFile=resources/logback_analyze.xml rsyslog_tj.jar 1 > nohup_analyze.out 2>&1 &

