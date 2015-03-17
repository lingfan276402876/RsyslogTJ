package cn.uc.rsyslog.util;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志工具类
 * 
 * @author sunguoqiang
 * 
 */
public class LogUtil {
	/**
	 * 系统日志
	 */
	private static final Logger SystemLog = LoggerFactory.getLogger("systemLog");
	/**
	 * 接收日志
	 */
	private static final Logger inMessageLog = LoggerFactory.getLogger("inMessageLog");
	/**
	 * 取出日志
	 */
	private static final Logger outMessageLog = LoggerFactory.getLogger("outMessageLog");

	public static void systemLog(String msg) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(msg);
		SystemLog.info(buffer.toString());
	}

	public static void systemLogInfo(String msg) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(msg);
		SystemLog.info(buffer.toString());
	}

	public static void systemLogError(String msg) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(msg);
		SystemLog.error(buffer.toString());
	}

	public static void inMessageLog(String msg) {
		if (RandomUtils.nextInt(100)  >= 1) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(msg);
			inMessageLog.info(buffer.toString());
		}

	}

	public static void outMessageLog(String msg) {
		if (RandomUtils.nextInt(100) >= 1) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(msg);
			outMessageLog.info(buffer.toString());
		}
	}

	public static void customLog(Logger logger, String msg) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(msg);
		logger.info(buffer.toString());
	}
}
