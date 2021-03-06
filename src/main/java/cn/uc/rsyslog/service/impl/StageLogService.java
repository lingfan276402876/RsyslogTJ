package cn.uc.rsyslog.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
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

import cn.uc.rsyslog.service.ExcuteLogService;
import cn.uc.rsyslog.util.ConfigUtil;
import cn.uc.rsyslog.util.ConnectionPool;
import cn.uc.rsyslog.util.LogDto;
import cn.uc.rsyslog.util.LogUtil;
import cn.uc.rsyslog.util.properties.LogDataBase;
import cn.uc.rsyslog.util.properties.LogFiled;
import cn.uc.rsyslog.util.properties.LogTable;
import cn.uc.rsyslog.util.properties.Props;

public class StageLogService  implements ExcuteLogService {
	// 存放预处理sql语句的需要的参数的队列
	private volatile BlockingQueue<Map<Integer, Object>> queue = new LinkedBlockingQueue<Map<Integer, Object>>();
	private volatile String static_sql = null;
	private volatile Connection connection = null;
	private volatile PreparedStatement statement = null;
	// 是否可用
	private volatile boolean isUse = true;
	private LogTable logTable = null;
	//定时器
	Timer timer = new Timer();
	//构造器
	public StageLogService() {
		timer.scheduleAtFixedRate(new timerTask(), 0,
				ConfigUtil.commitRate * 1000);
	}
	@Override
	public void initSql(LogTable logTable) {
		this.logTable = logTable;
		StringBuffer sb = new StringBuffer(logTable.getSqlExcute());
		sb.append(" " + logTable.getTable() + "(");
		List<LogFiled> loFiledes = logTable.getLogFiledes();
		int i = 0;
		for (LogFiled logFiled : loFiledes) {
			if (i > 0)
				sb.append(",");
			sb.append(logFiled.getTableFiled());
			i++;
		}
		loFiledes = LogDataBase.logTables.get("fightgame.fightGainProp")
				.getLogFiledes();
		for (LogFiled logFiled : loFiledes) {
			if (i > 0)
				sb.append(",");
			sb.append(logFiled.getTableFiled());
			i++;
		}
		sb.append(") values(");
		for (int j = 0; j < i; j++) {
			if (j != 0) {
				sb.append(",");
			}
			sb.append("?");
		}

		sb.append(");");
		this.static_sql = sb.toString();
	}

	@Override
	public void excute(LogDto logDto) throws Exception {
		if (logDto != null && logDto.getLogBody() != null) {
			String message = logDto.getLogBody();
			List<LogFiled> loFiledes = logTable.getLogFiledes();
			String[] messageFiled = message.split("\\|");
			// REDIS存储玩家的战斗信息等接受到玩家的拾取漂浮物及抽奖操作
			String uid = messageFiled[Integer.parseInt(logTable.getSpecial()
					.split("\\|")[1])];
			Map<Integer, Object> para = new HashMap<Integer, Object>();
			messageFiled = message.split("\\|");
			int i = 1;
			for (LogFiled logFiled : loFiledes) {
				String value = "";
				if (logFiled.getLogFiledIndex() != null) {
					value = messageFiled[logFiled.getLogFiledIndex()];
				} else if (StringUtils.isNotBlank(logFiled.getConstant())) {
					value = logFiled.getConstant();
				} else if (StringUtils.isNotBlank(logFiled.getExpression())) {
					String expression = logFiled.getExpression();
					List<Variable> variables = new ArrayList<Variable>();
					String[] temps = logFiled.getVariables();
					for (String temp : temps) {
						String temp2 = temp.substring(2, temp.length() - 2);
						if (temp2.startsWith("P")) {
							int var_index = Integer.parseInt(temp2.substring(1,
									temp2.length()));
							int propId = Integer
									.parseInt(messageFiled[var_index]);
							variables.add(Variable.createVariable(temp,
									Props.getPropName(propId)));
						} else if (logFiled.getExpression().startsWith("$")) { // 函数
							int var_index = Integer.parseInt(temp2);
							String str = messageFiled[var_index];
							variables.add(Variable.createVariable(temp, str));
						} else {
							int var_index = Integer.parseInt(temp2);
							String o = messageFiled[var_index];
							variables.add(Variable.createVariable(temp, o));
						}
					}
					value = String.valueOf(ExpressionEvaluator.evaluate(
							expression, variables));
				}
				para.put(i, value);
				i++;
			}
			para.put(i, GainPropService.getGainProp(uid, 20));
			while (!isUse) {
				LogUtil.systemLog("等待数据库记录入库---");
				Thread.sleep(1000);
			}
			queue.put(para);
		}
	}

	/**
	 * 获取key
	 * 
	 * @param uid
	 * @return
	 */
	public String getkey(String uid) {
		String fightKey = "RSYSLOG#STAGE#QUEUE#" + uid;
		return fightKey;
	}

	public void initConnection() {
		connection = ConnectionPool.getConnection();
		try {
			statement = connection.prepareStatement(static_sql.toString());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭连接
	 */
	public void closeConnection() {
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		isUse = true;
	}

	class timerTask extends TimerTask {
		/**
		 * 提交数据库
		 */
		public void run() {
			int size = queue.size();
			if(size<1)
				return;
			isUse = false;
			try {
				initConnection();
				while (!queue.isEmpty()) {
					Map<Integer, Object> para = queue.take();
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
				LogUtil.systemLog("向数据库提交" + size + "条关卡日志数据");
				statement.executeBatch();
				connection.commit();
			} catch (Exception e) {
				LogUtil.systemLogError("向数据库提交关卡战斗数据出错，异常信息如下："
						+ e.getMessage());
				e.printStackTrace();
			} finally {
				closeConnection();
			}
		}
	}
}
