package cn.uc.rsyslog.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import cn.uc.rsyslog.service.ExcuteLogService;
import cn.uc.rsyslog.util.ConfigUtil;
import cn.uc.rsyslog.util.ConnectionPool;
import cn.uc.rsyslog.util.JedisUtil;
import cn.uc.rsyslog.util.LogDto;
import cn.uc.rsyslog.util.LogUtil;
import cn.uc.rsyslog.util.properties.LogDataBase;
import cn.uc.rsyslog.util.properties.LogFiled;
import cn.uc.rsyslog.util.properties.LogTable;
import cn.uc.rsyslog.util.properties.Props;

public class FightLogService implements ExcuteLogService {
	// 存放预处理sql语句的需要的参数的队列
	private static volatile BlockingQueue<Map<Integer, Object>> queue = new LinkedBlockingQueue<Map<Integer, Object>>();
	private volatile String static_sql = null;
	private volatile Connection connection = null;
	private volatile PreparedStatement statement = null;
	//是否可用
	private static  volatile boolean isUse = true;
	//日志对应的logTable对象
    private  static LogTable logTable = null;
    //定时器
    private  Timer timer = new Timer();
    /**
     * 默认构造器
     */
	public FightLogService() {
		timer.schedule(new timerTask(), 0, ConfigUtil.commitRate*1000);
	}

	@Override
	public void initSql(LogTable logTable1) {
		logTable = logTable1;
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
		loFiledes = LogDataBase.logTables.get("fightgame.lottery")
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
	public synchronized void excute(LogDto logDto) throws Exception {
		if (logDto != null && logDto.getLogBody() != null) {
			String message = logDto.getLogBody();
			//新消息的字段
			String[] messageFiled = message.split("\\|");
			//REDIS存储玩家的战斗信息等接受到玩家的拾取漂浮物及抽奖操作
			String uid = messageFiled[Integer.parseInt(logTable.getSpecial().split("\\|")[1])];
            processFight(uid);
			cacheFight(getkey(uid),message);
		}
	}
	/**
	 * 处理战斗日志
	 * @param uid
	 * @throws Exception
	 */
	public static void processFight(String uid) throws Exception 
	{
		List<String> cacheMessages = getFightMessage(uid);
		List<LogFiled> loFiledes = logTable.getLogFiledes();
		if(cacheMessages!=null && cacheMessages.size()>0)
		{
			for (String cacheMessage : cacheMessages) {
				if(StringUtils.isBlank(cacheMessage))
				{
					continue;
				}
				Map<Integer, Object> para = new HashMap<Integer, Object>();
				//缓存消息中的字段
				String[] messageFiled = cacheMessage.split("\\|");
				uid = messageFiled[Integer.parseInt(logTable.getSpecial().split("\\|")[1])];
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
						value =  String.valueOf(ExpressionEvaluator.evaluate(expression, variables));
					}
					para.put(i, value);
					i++;
				}
				para.put(i,  GainPropService.getGainProp(uid, 6));
				para.put(++i,  LotteryLogService.getLottery(uid));
				while (!isUse) {
					LogUtil.systemLog("等待数据库记录入库---");
					Thread.sleep(1000);
				}
				queue.put(para);
			
			}
		}
	}
	
	/**
	 * 缓存玩家战斗日志
	 * 
	 * @param key
	 * @param vaule
	 */
	private void cacheFight(String key,String value) {
		Jedis jedis = null;
		boolean broken = false;
		try {
			jedis = JedisUtil.getJedis();
			long i = jedis.lpush(key, value);
			LogUtil.systemLogInfo("缓存战斗数据成功-----"+i);
		} catch (Exception e) {
			if (e instanceof JedisConnectionException)
				broken = true;
			e.printStackTrace();
		} finally {
			if (jedis != null) {
				if (broken) {
					JedisUtil.returnBrokenResource(jedis);
				} else {
					JedisUtil.returnResource(jedis);
				}
			}
		}
	}
	/**
	 * 获取key
	 * @param uid
	 * @return
	 */
	public static String getkey(String uid)
	{
		if(uid==null)
		{
			uid="*";
		}
		String fightKey = "RSYSLOG#FIGHT#QUEUE#"+uid;
		return fightKey;
	}
	
	/**
	 * 获取缓存中的战斗 用于关联抽奖与漂浮物日志
	 * @param uid 玩家账号
	 * @param time 获取漂浮物的时间
	 */
	public static List<String> getFightMessage(String uid) {
		Jedis jedis = null;
		boolean broken = false;
		List<String> messages = new ArrayList<String>();
		String message = null;
		try {
			jedis = JedisUtil.getJedis();
			Set<String> keys = new HashSet<String>();
            keys.add(getkey(uid));
			if(keys!=null && keys.size()>0)
			{
				for (String key : keys) {
					message = jedis.lpop(key);
					if(StringUtils.isNotBlank(message))
					{
						messages.add(message);
					}
				}
			}
			
		} catch (Exception e) {
			if (e instanceof JedisConnectionException)
				broken = true;
			e.printStackTrace();
		} finally {
			if (jedis != null) {
				if (broken) {
					JedisUtil.returnBrokenResource(jedis);
				} else {
					JedisUtil.returnResource(jedis);
				}
			}
		}
	   return messages;
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
	
	class timerTask extends TimerTask
	{
		/**
	     * 提交数据库
	     */
		public void run()
		{
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
						if(o!=null && !"null".equals(o))
						{
							statement.setObject(key,o);
						}else
						{
							statement.setObject(key,null);
						}
					}
					statement.addBatch();
			}
			LogUtil.systemLog("向数据库提交"+size+"条战斗日志数据");
			statement.executeBatch();
			connection.commit();
			} catch (Exception e) {
				LogUtil.systemLogError("向数据库提交战斗数据出错，异常信息如下："+e.getMessage());
				e.printStackTrace();
			}finally{
				closeConnection();
			}
		}
	}
}
