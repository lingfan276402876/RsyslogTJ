package cn.uc.rsyslog.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.wltea.expression.ExpressionEvaluator;
import org.wltea.expression.datameta.Variable;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import cn.uc.rsyslog.service.ExcuteLogService;
import cn.uc.rsyslog.util.JedisUtil;
import cn.uc.rsyslog.util.LogDto;
import cn.uc.rsyslog.util.properties.LogDataBase;
import cn.uc.rsyslog.util.properties.LogFiled;
import cn.uc.rsyslog.util.properties.LogTable;
import cn.uc.rsyslog.util.properties.Props;

public class LotteryLogService implements ExcuteLogService{
	String sql = null;
	@Override
	public void initSql(LogTable logTable){
		
	}
	@Override
	public void excute(LogDto logDto) throws Exception {
		 if (logDto != null && logDto.getLogBody() != null) {
				String message = logDto.getLogBody();
				LogTable logTable = LogDataBase.logTables.get(logDto.getLogType());
				List<LogFiled> loFiledes = logTable.getLogFiledes();
				String[] messageFiled = message.split("\\|");
				String uid = messageFiled[Integer.parseInt(logTable.getSpecial().split("\\|")[1])];
				for (LogFiled logFiled : loFiledes) {
					String expression = logFiled.getExpression();						
					List<Variable> variables = new ArrayList<Variable>();  
					String[] temps = logFiled.getVariables();
					for (String temp : temps) {
						String temp2 = temp.substring(2,temp.length()-2);
						if(temp2.startsWith("P"))
						{
							int var_index = Integer.parseInt(temp2.substring(1,temp2.length()));
							int propId = Integer.parseInt(messageFiled[var_index]);
							variables.add(Variable.createVariable(temp,Props.getPropName(propId))); 
						} else if (logFiled.getExpression().startsWith("$")) { // 函数
							int var_index = Integer.parseInt(temp2);
							String str = messageFiled[var_index];
							variables.add(Variable
									.createVariable(temp, str));
						}
						else
						{
							int var_index = Integer.parseInt(temp2);
							int count = Integer.parseInt(messageFiled[var_index]);
							variables.add(Variable.createVariable(temp, count)); 
						}
					}
					String remark = String.valueOf(ExpressionEvaluator.evaluate(expression,variables));
                    cacheLottery(getkey(uid), remark);
				}
				FightLogService.processFight(uid);
			}
	}
	/**
	 * 缓存玩家战斗抽奖物品
	 * 
	 * @param key
	 * @param vaule
	 */
	private void cacheLottery(String key,String value) {
		Jedis jedis = null;
		boolean broken = false;
		try {
			jedis = JedisUtil.getJedis();
			jedis.lpush(key, value);
		} catch (Exception e) {
			if (e instanceof JedisConnectionException)
				broken = true;
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
	 * 获取玩家战斗抽奖物品
	 * 
	 * @param uid
	 */
	public static String getLottery(String uid) {
		Jedis jedis = null;
		boolean broken = false;
		StringBuilder builder = new StringBuilder("");
		try {
			jedis = JedisUtil.getJedis();
			String value = jedis.lpop(getkey(uid));
			if(value!=null)
			{
				builder.append(value+"; ");
			}
		} catch (Exception e) {
			if (e instanceof JedisConnectionException)
				broken = true;
		} finally {
			if (jedis != null) {
				if (broken) {
					JedisUtil.returnBrokenResource(jedis);
				} else {
					JedisUtil.returnResource(jedis);
				}
			}
		}
		return builder.toString();
	}
	/**
	 * 获取key
	 * @param uid
	 * @return
	 */
	public static String getkey(String uid)
	{
		String fightKey = "RSYSLOG#FIGHT#LOTTERY#"+uid;
		return fightKey;
	}
}
