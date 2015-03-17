package cn.uc.rsyslog.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

/**
 * 属性文件工具类
 * @author sunguoqiang
 *
 */
public class ConfigUtil {
	private static FileUtil fileUtil = new FileUtil();
	/**
	 * 数据库连接url
	 */
	public static String jdbc1mysqlUrl;
	/**
	 * 连接用户名
	 */
	public static String jdbc1username;
	/**
	 * 连接密码
	 */
	public static String jdbc1password;
	/**
	 * 数据库连接url
	 */
	public static String jdbc2mysqlUrl;
	/**
	 * 连接用户名
	 */
	public static String jdbc2username;
	/**
	 * 连接密码
	 */
	public static String jdbc2password;
	/**
	 * jedis连接url
	 */
	public static String jedisUrl;
	/**
	 * jedis连接端口
	 */
	public static String jedisPort;
	
	public static Integer port;
	
	public static String server;
	/**
	 * 每次从数据库取数据的条数
	 */
	public static Integer queueMaxsize;
	/**
	 * 想数据库提交数据的频率
	 */
	public static Integer commitRate;
	
	public static boolean IS_DEV =false;
	/**
	 * 日志目录
	 */
	public static String logDir;
	/**
	 * 文件修改差值
	 */
	public static Integer fileModifyTimeDif=1;
	/**
	 * 日志分析模式
	 */
	public static Integer mode=1;
	/**
	 * 日志分析线程数
	 */
	public static Integer analyzeLogThreadCount=1;
	static {
		URL u = ConfigUtil.class.getProtectionDomain().getCodeSource().getLocation();
		File f = new java.io.File(u.getFile());
		try {
			u = new File(f.getParent() + File.separator +"resources"+File.separator +"config.properties").toURI().toURL();
		} catch (MalformedURLException e2) {
			e2.printStackTrace();
		}
		final URL url = u;
		try {
			initialize(url);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
		Runnable command = new Runnable(){
			@Override
			public void run() {
				if (fileUtil.isUpdate(url)) {
					try {
						initialize(url);
						LogUtil.systemLog("系统配置文件更新");
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}	
			}
			
		};
		executor.scheduleAtFixedRate(command, 0, 10, TimeUnit.SECONDS);
	}
	private static void initialize(URL url) throws Exception {
		try {
			PropertiesConfiguration config = new PropertiesConfiguration(url);
			FileChangedReloadingStrategy fc = new FileChangedReloadingStrategy();
			fc.setRefreshDelay(10);
			config.setReloadingStrategy(fc);
			jdbc1mysqlUrl = config.getString("jdbc1.mysqlUrl");
			jdbc1username = config.getString("jdbc1.username");
			jdbc1password = config.getString("jdbc1.password");
			jdbc2mysqlUrl = config.getString("jdbc2.mysqlUrl");
			jdbc2username = config.getString("jdbc2.username");
			jdbc2password = config.getString("jdbc2.password");
			jedisUrl = config.getString("jedisUrl");
			jedisPort = config.getString("jedisPort");
			queueMaxsize = config.getInteger("queueMaxsize", 100);
			commitRate = config.getInteger("commitRate", 10);
			port = config.getInteger("port", 1514);
			server = config.getString("server","127.0.0.1");
			logDir =  config.getString("logDir","");
			fileModifyTimeDif = config.getInteger("fileModifyTimeDif", 1);
			mode = config.getInteger("mode", 0);
			analyzeLogThreadCount = config.getInteger("analyzeLogThreadCount",4);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}
}
