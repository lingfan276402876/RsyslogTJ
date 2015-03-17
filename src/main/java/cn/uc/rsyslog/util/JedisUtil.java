package cn.uc.rsyslog.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
/**
 * 
 * @author sunguoqiang
 *
 */
public class JedisUtil {
	private static JedisPool jedisPool;
   static{
	   initialPool();
   }
	private static void initialPool() {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(100);
		config.setMaxIdle(50);
		config.setMaxWaitMillis(30000);
		config.setTestOnBorrow(true);
		jedisPool = new JedisPool(config, ConfigUtil.jedisUrl,Integer.parseInt(ConfigUtil.jedisPort));
	}
	public static Jedis getJedis() {
		return jedisPool.getResource();
	}
	public static void returnResource(Jedis jedis) {
		jedisPool.returnResource(jedis);
	}
	public static void returnBrokenResource(Jedis jedis) {
		jedisPool.returnBrokenResource(jedis);
	}
}
