package cn.uc.rsyslog.analyze;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.uc.rsyslog.service.impl.SaveLog;
import cn.uc.rsyslog.util.ConfigUtil;
import cn.uc.rsyslog.util.LogUtil;

public class LogAnalyze implements Runnable{
	private ExecutorService executorService;//线程池
    private final int POOL_SIZE=10;//单个CPU线程池大小
    public volatile static boolean speedDown = false;
    public LogAnalyze() {
		 executorService=Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()*POOL_SIZE);
	     LogUtil.systemLog("日志入库线程开始启动");
	     addShutDownFile();
	}
    @Override
	public void run(){
    	for (int i = 0; i < ConfigUtil.analyzeLogThreadCount; i++) {
			Runnable runnable = new LogAnalyzeThread();
			executorService.execute(runnable);
		}
	}
    /**
     * 生成一个shutdownfile文件
     */
	public void addShutDownFile()
	{
		//启动shutdownfile监听线程  
        new Thread() {  
            public void run() {  
                File shutDownFile = new File("b.shutdown");  
                if (!shutDownFile.exists()) {  
                    try {  
                        shutDownFile.createNewFile();  
                    } catch (IOException e) {  
                        e.printStackTrace();  
                    }  
                }  
                while (true) {  
                    try {  
                        if (shutDownFile.exists()) {  
                            Thread.sleep(1000l);  
                        } else {  
                        	executorService.shutdown();
                        }
                        if(executorService.isShutdown())
                        {
                        	System.exit(0);
                        }
                    } catch (InterruptedException e) {  
                        e.printStackTrace();  
                    }  
                }  
            }  
        }.start();  
    }
	public static void main(String[] args) {
		new SaveLog("PerfectPointLog`2014-11-17 15:15:48|||0|||提取|706|0|系统|176|战士一一|物品拍卖成功|成功拍卖[冰粹长剑]成交价：+300,000金币押金：+10,000金币手续费：-15,000金币拍卖完成，您获得了295,000金币。||0|").run();;
	}
}
