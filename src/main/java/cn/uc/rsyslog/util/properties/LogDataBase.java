package cn.uc.rsyslog.util.properties;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import cn.uc.rsyslog.service.ExcuteLogService;
import cn.uc.rsyslog.util.ConfigUtil;
import cn.uc.rsyslog.util.DataType;
import cn.uc.rsyslog.util.LogUtil;

/**
 * 日志和数据库表结构对应关系
 * @author sungq
 *
 */
public class LogDataBase {
	/**
	 * 日志配置文件
	 */
	public static volatile Map<String, LogTable> logTables = new HashMap<String, LogTable>();
	/**
	 * 日志分析类对象
	 */
	public static volatile Map<String, ExcuteLogService> services = new HashMap<String, ExcuteLogService>();
	/**
	 * 游戏日志对应的登记器
	 */
	private static volatile Map<String, Logger> loggers = new HashMap<String, Logger>();
	private static long lastModifyTime = 0;
	
	private static int pre_month = 0;
	static {
		URL u = ConfigUtil.class.getProtectionDomain().getCodeSource().getLocation();
		File f = new java.io.File(u.getFile());
		try {
			u = new File(f.getParent() + File.separator +"resources"+File.separator +"bussiness"+File.separator +"log_database.xml").toURI().toURL();
		} catch (MalformedURLException e2) {
			e2.printStackTrace();
		}
		final URL url = u;
		try {
			initXml(url);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
		Runnable command = new Runnable(){
			@Override
			public void run() {
				if (isUpdate(url)) {
					try {
						initXml(url);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
			}
		};
		executor.scheduleAtFixedRate(command, 0, 10, TimeUnit.SECONDS);
	}
	/**
	 * 判断文件是否有更新
	 * @param file
	 * @return
	 */
	private static boolean isUpdate(URL url)
	{
	  File file = new File(url.getFile());
	  if(file.exists())	
	  {
		  if(lastModifyTime == 0)
		  {
			  lastModifyTime = file.lastModified();
			  LogUtil.systemLog("系统配置文件初始化！");
			  return true;
		  }
		  long currentFileModifyTime = file.lastModified();
		  if(currentFileModifyTime>lastModifyTime)
		  {
			  lastModifyTime = currentFileModifyTime;
			  LogUtil.systemLog("系统配置文件有更新，重新初始化文件！");
			  return true;
		  }else
		  {
			  Calendar calendar = Calendar.getInstance();
			  if(pre_month != calendar.get(Calendar.MONTH)+1)
			  {
				  LogUtil.systemLog("自动切换入库表名，系统重新初始化配置文件！");
				  return true;
			  }
		  }
		  return false;
	  }
	  return false;
	}
	/**
	 * 初始化xml文件
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void initXml(URL url) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		SAXReader reader = new SAXReader();
		Document document = null;
		try {
			document = reader.read(url);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		Element root = document.getRootElement();
		for (Iterator<Element> i = root.elementIterator(); i.hasNext();) {
			Element element = (Element) i.next();
			LogTable logTable = new LogTable();
			logTable.setLogType(element.attributeValue("type"));
			logTable.setSqlExcute(element.attributeValue("sqlExcute"));
			Calendar calendar = Calendar.getInstance();
			pre_month = calendar.get(Calendar.MONTH)+1;
			logTable.setTable(element.attributeValue("table")+"_"+calendar.get(Calendar.YEAR)+"_"+pre_month);
			logTable.setClassName(element.attributeValue("className"));
			logTable.setSpecial(element.attributeValue("special"));
			List<Element> eList = element.elements();
			for (Iterator iterator = eList.iterator(); iterator.hasNext();) {
				Element element2 = (Element) iterator.next();
				if(element2.getName().equalsIgnoreCase("indexs"))
				{
					for (Object o : element2.elements("index")) {
						Element indexElement = (Element) o;
						TableIndex tableIndex = new TableIndex();
						tableIndex.setIndexName(indexElement.attributeValue("name"));
						tableIndex.setColumns(indexElement.attributeValue("columns"));
						logTable.getTableIndexs().add(tableIndex);
					}
				}else
				{
					LogFiled logFiled = new LogFiled();
					logFiled.setConstant(element2.attributeValue("constant"));
					logFiled.setTableFiled(element2.attributeValue("name"));
					String dataType = element2.attributeValue("dataType");
					if (StringUtils.isNotBlank(dataType)) {
						logFiled.setDataType(DataType.valueOf(dataType));
					}
					String index = element2.attributeValue("index");
					if (StringUtils.isNotBlank(index)) {
						logFiled.setLogFiledIndex(Integer.parseInt(index));
					}
					logFiled.setExpression(element2.attributeValue("expression"));
					String variables = element2.attributeValue("variables");
					if (StringUtils.isNotBlank(variables)) {
						logFiled.setVariables(variables.split("\\|"));
					}
					if (logTable.getLogFiledes() == null) {
						logTable.setLogFiledes(new ArrayList<LogFiled>());
					}
					if (logTable.getUnConstantFiledes() == null) {
						logTable.setUnConstantFiledes(new ArrayList<LogFiled>());
					}
					logTable.getLogFiledes().add(logFiled);
					if(StringUtils.isBlank(logFiled.getConstant()))
					{
						logTable.getUnConstantFiledes().add(logFiled);
					}
				}
				
			}
			logTable.initTable();
			logTables.put(logTable.getLogType(), logTable);
		}
		for (LogTable logTable : logTables.values()) {
			if (logTable!= null) {
				ExcuteLogService logService = null;
				if(services.get(logTable.getLogType())==null)
				{
					logService =  (ExcuteLogService) Class.forName(logTable.getClassName()).newInstance();
					logService.initSql(logTable);
					services.put(logTable.getLogType(),logService );
				}else
				{
					logService = services.get(logTable.getLogType());
					logService.initSql(logTable);
				}
				loggers.put(logTable.getLogType(),createLogger(logTable.getLogType()));
			 }
		}
	}
	
	public static Logger getLogger(String logType)
	{
		return loggers.get(logType);
	}
	
	private static Logger createLogger(String logType){
		    Logger logger = LoggerFactory.getLogger(logType);
	        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();  
	        ch.qos.logback.classic.Logger newLogger = (ch.qos.logback.classic.Logger)logger;  
	        newLogger.detachAndStopAllAppenders();  
	        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<ILoggingEvent>();  
	        TimeBasedRollingPolicy<ILoggingEvent> policy = new TimeBasedRollingPolicy<ILoggingEvent>();  
	        policy.setContext(loggerContext);  
	        policy.setMaxHistory(0);  
	        policy.setFileNamePattern(ConfigUtil.logDir+"/"+logType+".log.%d{yyyy-MM-dd_HH-mm}");  
	        policy.setParent(appender);  
	        policy.start();  
	        PatternLayoutEncoder encoder = new PatternLayoutEncoder();  
	        encoder.setContext(loggerContext);  
	        encoder.setPattern("%msg%n");  
	        encoder.start();  
	        appender.setRollingPolicy(policy);  
	        appender.setContext(loggerContext);  
	        appender.setEncoder(encoder);  
	        appender.setPrudent(true);
	        appender.start();       
	        newLogger.addAppender(appender);  
	        newLogger.setLevel(Level.INFO);  
	        newLogger.setAdditive(false);  
	        return logger;
	}
}
