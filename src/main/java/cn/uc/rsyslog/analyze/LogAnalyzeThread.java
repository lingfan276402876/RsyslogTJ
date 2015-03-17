package cn.uc.rsyslog.analyze;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.StringUtils;

import redis.clients.jedis.Jedis;
import cn.uc.rsyslog.service.impl.SaveLog;
import cn.uc.rsyslog.util.JedisUtil;
import cn.uc.rsyslog.util.LogUtil;

public class LogAnalyzeThread implements Runnable {
	private ExecutorService executorService;// 线程池
	private final int POOL_SIZE = 10;// 单个CPU线程池大小
	public volatile static boolean speedDown = false;

	public LogAnalyzeThread() {
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_SIZE);
		LogUtil.systemLog("日志入库线程开始启动");
		addShutDownFile();
	}

	public void run() {
		Jedis jedis = null;
		try {
			jedis = JedisUtil.getJedis();
			startService(jedis);
		} catch (Exception e) {
			e.printStackTrace();
			JedisUtil.returnBrokenResource(jedis);
			LogUtil.systemLog("分析日志据线程挂掉！开始重启");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1){
				e1.printStackTrace();
			}
			run();
			LogUtil.systemLog("分析日志线程挂掉！重启成功");
		} finally {
			JedisUtil.returnResource(jedis);
		}

	}

	private void startService(Jedis jedis) {
		String logData = null;
		long startTime = 0;
		long cost = 0;
		while (true) {
			if (executorService.isShutdown()) {
				break;
			}
			startTime = System.currentTimeMillis();
			logData = jedis.lpop("rsyslog#message");
			if (StringUtils.isBlank(logData)) {
				try {
					Thread.sleep(1000l);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			if (speedDown) // 如果收到减速命令，则休眠一秒，等待日志入库线程
			{
				try {
					Thread.sleep(1000l);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				speedDown = false;
				LogUtil.systemLog("日志入库线程压力大，等待线程执行");
			}
			Thread thread = new Thread(new SaveLog(logData));
			executorService.execute(thread);
			LogUtil.outMessageLog(logData);
			cost = (System.currentTimeMillis() - startTime);
			if (cost > 500l)
				LogUtil.systemLog("取日志消耗时间:" + cost);
		}
	}

	/**
	 * 生成一个shutdownfile文件
	 */
	public void addShutDownFile() {
		// 启动shutdownfile监听线程
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
						if (executorService.isShutdown()) {
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
		new SaveLog("PlayerOnlineLog`2014-09-21 17:29:00|19|75041744|1|6416316841539057672").run();
	}
}
