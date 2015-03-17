package cn.uc.rsyslog.service.impl;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import cn.uc.rsyslog.service.ExcuteLogService;
import cn.uc.rsyslog.util.JedisUtil;
import cn.uc.rsyslog.util.LogDto;
import cn.uc.rsyslog.util.LogUtil;
import cn.uc.rsyslog.util.properties.LogDataBase;

public class SaveLog implements Runnable{
    private LogDto logDto;
    private String logData;
    public SaveLog(String logData) {
    	this.logData = logData;
		this.logDto = new LogDto(logData);
	}
	@Override
	public void run() {
		ExcuteLogService service = null;
		try {
			String type = logDto.getLogType();
			if (type != null) {
				service = LogDataBase.services.get(type);
				if(service != null)
				   service.excute(logDto);
			}
		} catch (Exception e) {
			LogUtil.systemLog("处理日志出错，message:"+logDto.getLogBody());
			LogUtil.systemLog("处理日志出错，messageType:"+logDto.getLogType());
			Jedis jedis = JedisUtil.getJedis();
			boolean broken = false;
			try {
				jedis.lpushx("rsyslog#message",logData);
				LogUtil.systemLog("日志重新加入队列中:"+logData);
                e.printStackTrace();
			} catch (Exception e1) {
				if (e1 instanceof JedisConnectionException)
					broken = true;
				e1.printStackTrace();
			}finally
			{
				if (jedis != null) {
					if (broken) {
						JedisUtil.returnBrokenResource(jedis);
					} else {
						JedisUtil.returnResource(jedis);
					}
				}
			}
		}
	}
}
