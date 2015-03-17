package cn.uc.rsyslog.main;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.uc.rsyslog.analyze.LogAnalyze;
import cn.uc.rsyslog.util.ConfigUtil;
import cn.uc.rsyslog.util.LogUtil;
import cn.uc.rsyslog.util.properties.LogDataBase;

/**
 * 
 * @ClassName Server
 * @Description TODO
 * @author sungq@ucweb.com
 * @date 2014年9月19日
 */
public class Server {
	private int port = ConfigUtil.port;
	private static ServerSocket serverSocket;
	private static ExecutorService executorService;// 线程池
	private final static int POOL_SIZE = 10;// 单个CPU线程池大小

	public Server() throws IOException {
		serverSocket = new ServerSocket(port);
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_SIZE);
		LogUtil.systemLog("服务器启动");
		addShutDownFile();
	}

	public void serverStart() {
		try {
			Socket scoket = null;
			while (true) {
				if(executorService.isShutdown())
				{
					break;
				}
				scoket = serverSocket.accept();
				ServerThread serverthread = new ServerThread(scoket);
				addShutdownHook(serverthread);
				executorService.execute(serverthread);
			}
		} catch (IOException e) {
			e.printStackTrace();
			LogUtil.systemLog("程序出现异常");
		}
	}

	/**
	 * 注册hook程序，保证线程能够完整执行
	 * 
	 * @param checkThread
	 */
	private static void addShutdownHook(final ServerThread checkThread) {
		// 为了保证TaskThread不在中途退出，添加ShutdownHook
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				LogUtil.systemLog("收到关闭信号，hook起动，开始检测线程状态 ...");
				try {
					checkThread.getSocket().close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				LogUtil.systemLog("检测超时，放弃等待，退出hook，此时线程会被强制关闭");
			}
		});
	}
    /**
     * 生成一个shutdownfile文件
     */
	public void addShutDownFile()
	{
		//启动shutdownfile监听线程  
        new Thread() {  
            public void run() {  
                File shutDownFile = new File("a.shutdown");  
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
                        	executorService.shutdownNow();
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
		if(args == null || args.length<1)
		{
			System.err.println("必须指定启动模式(0:RXD 启动接收日志线程 1：启动分析日志入库线程)");
			System.exit(0);
		}
		String type = args[0];
		int mode = 0;
		try {
			mode = Integer.parseInt(type);
		} catch (Exception e) {
			System.err.println("启动模式只能为数字(0:RXD 启动接收日志线程 1：启动分析日志入库线程)");
			System.exit(0);
		}
		try {
			if(mode==0)
			{
				new Server().serverStart();
			}else if(mode == 1)
			{
				new LogDataBase();
				Thread thread = new Thread(new LogAnalyze());
				thread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}