package cn.uc.rsyslog.service.impl;

import java.io.File;
import java.io.FileFilter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang.StringUtils;
import org.wltea.expression.ExpressionEvaluator;
import org.wltea.expression.datameta.Variable;

import cn.uc.rsyslog.analyze.LogAnalyze;
import cn.uc.rsyslog.service.ExcuteLogService;
import cn.uc.rsyslog.util.ConfigUtil;
import cn.uc.rsyslog.util.ConnectionPool;
import cn.uc.rsyslog.util.LogDto;
import cn.uc.rsyslog.util.LogUtil;
import cn.uc.rsyslog.util.properties.Gears;
import cn.uc.rsyslog.util.properties.LogDataBase;
import cn.uc.rsyslog.util.properties.LogFiled;
import cn.uc.rsyslog.util.properties.LogTable;
import cn.uc.rsyslog.util.properties.Props;
import cn.uc.rsyslog.util.properties.RankShop;
import cn.uc.rsyslog.util.properties.Skills;
import cn.uc.rsyslog.util.properties.Weapons;

public class BaseService implements ExcuteLogService {
	// 存放预处理sql语句的需要的参数的队列
	private volatile BlockingQueue<Map<Integer, Object>> queue = new LinkedBlockingQueue<Map<Integer, Object>>();
	// sql语句
	private volatile String static_sql = null;
	// load data infile 语句
	private volatile String loadFile_sql = null;
	// 日志对应的logTable对象
	private LogTable logTable = null;
	/**
	 * 日志目录
	 */
	private File logRoot = new File(ConfigUtil.logDir);
	/**
	 * 日志文件适配器
	 */
	private FileFilter logFileFilter = null;
	// 定时器
	Timer timer = new Timer();

	public BaseService() {
		timer.scheduleAtFixedRate(new timerTask(), 0, ConfigUtil.commitRate * 1000);
		logRoot = new File(ConfigUtil.logDir);
	}

	@Override
	public void initSql(LogTable logTable) {
		this.logTable = logTable;
		StringBuffer sb = new StringBuffer(logTable.getSqlExcute());
		sb.append(" " + logTable.getTable());
		StringBuilder filedBuilder = new StringBuilder(" (");
		StringBuilder valueBuilder = new StringBuilder(" (");
		StringBuilder constantBuilder = new StringBuilder(" set ");
		List<LogFiled> logFiledes = logTable.getLogFiledes();
		int i = 0;
		int constantCount = 0;
		for (LogFiled logFiled : logFiledes) {
			if (i > 0) {
				filedBuilder.append(",");
				valueBuilder.append(",");
			}
			filedBuilder.append("`"+logFiled.getTableFiled()+"`");
			if (StringUtils.isNotBlank(logFiled.getConstant())) {
				valueBuilder.append("'" + logFiled.getConstant() + "'");
				if (constantCount > 0) {
					constantBuilder.append(",");
				}
				constantBuilder.append(logFiled.getTableFiled() + "='" + logFiled.getConstant() + "'");
				constantCount++;
			} else {
				valueBuilder.append("?");
			}
			i++;
		}
		filedBuilder.append(")");
		valueBuilder.append(")");
		sb.append(filedBuilder).append(" values").append(valueBuilder);
		this.static_sql = sb.toString() + ";";
		sb = new StringBuffer("LOAD DATA LOCAL INFILE ? INTO TABLE ");
		sb.append(logTable.getTable()).append(" fields TERMINATED by '|' ");
		sb.append(filedBuilder);
		if (constantCount > 0) {
			sb.append(constantBuilder);
		}
		loadFile_sql = sb.toString() + ";";
	}

	@Override
	public void excute(LogDto logDto) throws Exception {
		if (logDto != null && logDto.getLogBody() != null) {
			if (ConfigUtil.mode == 0) {
				analyzeAndGetPara(logDto);
			} else if (ConfigUtil.mode == 1) {
				analyzeAndWriteFile(logDto);
			}
		}
	}

	/**
	 * 初始化连接
	 */
	public Connection initConnection() {
		return ConnectionPool.getConnection();
	}

	/**
	 * 关闭连接
	 */
	public void closeConnection(Connection connection) {
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	class timerTask extends TimerTask {
		/**
		 * 提交数据库
		 */
		public void run() {
			try {
				if (ConfigUtil.mode == 0) {
					insertLog();
				} else if (ConfigUtil.mode == 1) {
					loadDataInfileLog();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * 分析日志 把字段存入参数队列中 供入库
	 * 
	 * @param logDto
	 * @throws InterruptedException
	 */
	private void analyzeAndGetPara(LogDto logDto) throws InterruptedException {
		Map<Integer, Object> para = new HashMap<Integer, Object>();
		String message = logDto.getLogBody();
		List<LogFiled> loFiledes = logTable.getUnConstantFiledes();
		int i = 0;
		String[] messageFiled = message.split("\\|");
		i = 1;
		for (LogFiled logFiled : loFiledes) {
			if (logFiled.getLogFiledIndex() != null) {
				para.put(i, messageFiled[logFiled.getLogFiledIndex()]);
			} else if (StringUtils.isNotBlank(logFiled.getExpression())) {
				String expression = logFiled.getExpression();
				List<Variable> variables = new ArrayList<Variable>();
				String[] temps = logFiled.getVariables();
				for (String temp : temps) {
					String temp2 = temp.substring(2, temp.length() - 2);
					if (temp2.startsWith("P")) { // 道具
						int var_index = Integer.parseInt(temp2.substring(1, temp2.length()));
						int propId = Integer.parseInt(messageFiled[var_index]);
						variables.add(Variable.createVariable(temp, Props.getPropName(propId)));
					} else if (temp2.startsWith("R")) { // 天梯赛
						int var_index = Integer.parseInt(temp2.substring(1, temp2.length()));
						int rankId = Integer.parseInt(messageFiled[var_index]);
						variables.add(Variable.createVariable(temp, RankShop.getRankGoodsName(rankId)));
					} else if (temp2.startsWith("W")) { // 武器
						int var_index = Integer.parseInt(temp2.substring(1, temp2.length()));
						int weaponId = Integer.parseInt(messageFiled[var_index]);
						variables.add(Variable.createVariable(temp, Weapons.getWeaponName(weaponId)));
					} else if (temp2.startsWith("S")) { // 技能
						int var_index = Integer.parseInt(temp2.substring(1, temp2.length()));
						int skillId = Integer.parseInt(messageFiled[var_index]);
						variables.add(Variable.createVariable(temp, Skills.getSkillName(skillId)));
					} else if (logFiled.getExpression().startsWith("$")) { // 函数
						int var_index = Integer.parseInt(temp2);
						String str = messageFiled[var_index];
						variables.add(Variable.createVariable(temp, str));
					} else if (temp2.startsWith("G")) {
						int var_index = Integer.parseInt(temp2.substring(1, temp2.length()));
						int gearId = Integer.parseInt(messageFiled[var_index]);
						variables.add(Variable.createVariable(temp, Gears.getGearsName(gearId)));
					} else {
						int var_index = Integer.parseInt(temp2);
						int count = 0;
						try {
							count = Integer.parseInt(messageFiled[var_index]);
						} catch (Exception e) {
							LogUtil.systemLog("error:--var_index=" + var_index + "；message=" + message);
						}
						variables.add(Variable.createVariable(temp, count));
					}
				}
				Object o = String.valueOf(ExpressionEvaluator.evaluate(expression, variables));
				para.put(i, o);
			}
			i++;
		}
		queue.put(para);
		if (queue.size() >= ConfigUtil.queueMaxsize) {
			LogAnalyze.speedDown = true;
			insertLog();
		}
	}

	/**
	 * 分析日志并把分析结果写入文件，供入库
	 * 
	 * @param logDto
	 * @throws InterruptedException
	 */
	private void analyzeAndWriteFile(LogDto logDto) throws InterruptedException {
		String message = logDto.getLogBody();
		List<LogFiled> loFiledes = logTable.getUnConstantFiledes();
		String[] messageFiled = message.split("\\|");
		StringBuilder builder = new StringBuilder();
		for (LogFiled logFiled : loFiledes) {
			if(messageFiled.length>logFiled.getLogFiledIndex())
			{
				builder.append(messageFiled[logFiled.getLogFiledIndex()] + "|");
			}
		}
		LogUtil.customLog(LogDataBase.getLogger(logDto.getLogType()), builder.toString());
	}

	private void insertLog() {
		// 连接
		Connection connection = initConnection();
		if (connection == null) {
			LogUtil.systemLogError("数据连接为空！");
		}
		try {
			PreparedStatement statement = connection.prepareStatement(static_sql.toString());
			Collection<Map<Integer, Object>> c = new ArrayList<Map<Integer, Object>>();
			long start = System.currentTimeMillis();
			queue.drainTo(c);
			LogUtil.systemLog("队列拷贝用时：" + (System.currentTimeMillis() - start) + ",大小:" + c.size());
			int size = c.size();
			if (size < 1) {
				return;
			}
			for (Map<Integer, Object> para : c) {
				Set<Integer> keys = para.keySet();
				for (Integer key : keys) {
					Object o = para.get(key);
					if (o != null && !"null".equals(o)) {
						statement.setObject(key, o);
					} else {
						statement.setObject(key, null);
					}
				}
				statement.addBatch();
			}
			statement.executeBatch();
			connection.commit();
			LogUtil.systemLog("向数据库提交" + size + "条日志数据");
			statement.close();
		} catch (Exception e) {
			LogUtil.systemLogError("向数据库提交" + logTable.getLogType() + "出错，异常信息如下：" + e.getMessage());
			e.printStackTrace();
		} finally {
			closeConnection(connection);
		}
	}

	private void loadDataInfileLog() {
		// 连接
		Connection connection = initConnection();
		if (connection == null) {
			LogUtil.systemLogError("数据连接为空！");
		}
		try {
			File[] logsFile = logRoot.listFiles(getLogFilter());
			long start = 0;
			PreparedStatement statement = connection.prepareStatement(loadFile_sql.toString());
			for (File file : logsFile) {
				try {
					start = System.currentTimeMillis();
					statement.setObject(1, file.getPath());
					statement.executeUpdate();
					connection.commit();
					statement.clearParameters();
					long size = (file.length()/1024);
					file.delete();
					LogUtil.systemLog(MessageFormat.format("向数据库提交日志数据,文件名为{0},大小{1}kb,耗时{2}秒",file.getName(),size,(System.currentTimeMillis() - start)));
				} catch (Exception e) {
					LogUtil.systemLogError("向数据库提交" + logTable.getLogType() + "出错，异常信息如下：" + e.getMessage());
					e.printStackTrace();
				}
			}
			statement.close();
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.systemLogError("向数据库提交" + logTable.getLogType() + "出错，异常信息如下：" + e.getMessage());
		} finally {
			closeConnection(connection);
		}
	}

	private FileFilter getLogFilter() {
		if (logFileFilter != null) {
			return logFileFilter;
		}
		logFileFilter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				if (file!=null && file.getName().toLowerCase().startsWith(logTable.getLogType().toLowerCase()) && file.lastModified() < (System.currentTimeMillis() - ConfigUtil.fileModifyTimeDif * 60 * 1000l)) {
					return true;
				}
				return false;
			}
		};
		return logFileFilter;
	}
}
