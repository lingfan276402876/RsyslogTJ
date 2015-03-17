package cn.uc.rsyslog.util;
/**
 * 日志对象
 * 
 * @author sungq
 * 
 */
public class LogDto {
	/**
	 * 日志类型
	 */
	private String logType;
	/**
	 * 日志消息体
	 */
	private String logBody;
	public LogDto(String message) {
	  String[] messages = message.split("`");
	  this.logType = messages[0];
	  this.logBody = messages[1];
	}
	
	public String getLogType() {
		return logType;
	}

	public void setLogType(String logType) {
		this.logType = logType;
	}

	public String getLogBody() {
		return logBody;
	}

	public void setLogBody(String logBody) {
		this.logBody = logBody;
	}
}
