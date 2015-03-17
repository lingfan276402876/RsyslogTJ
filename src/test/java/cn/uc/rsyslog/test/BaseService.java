package cn.uc.rsyslog.test;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import cn.uc.rsyslog.util.ConfigUtil;

public class BaseService{
	// 定时器
	Timer timer = new Timer();

	public BaseService() {
		timer.schedule(new timerTask(), 0, ConfigUtil.commitRate * 1000);
	}
	class timerTask extends TimerTask {
		/**
		 * 提交数据库
		 */
		public void run() {
			System.out.println("qian"+new Date().toGMTString());
//			while (true) {
//				new Date();
//			}
			//System.out.println("hou"+new Date().toGMTString());
		}
	}
	public static void main(String[] args) {
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();  
        JoranConfigurator configurator = new JoranConfigurator();  
        configurator.setContext(lc);  
        lc.reset();  
        try {  
            configurator.doConfigure("resources/logback.xml"); 
        } catch (JoranException e) {  
            e.printStackTrace();  
        }  
	}
}
