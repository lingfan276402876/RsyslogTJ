package cn.uc.rsyslog.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import cn.uc.rsyslog.util.JedisUtil;
import cn.uc.rsyslog.util.LogUtil;

public class ServerThread implements Runnable {
	private Socket socket;
	private BufferedReader in;

	public ServerThread(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		serverThread();
	}

	private void serverThread() {
		Jedis jedis = JedisUtil.getJedis();
		boolean broken = false;
		String b = null;
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			long startTime = 0;
			long endTime = 0;
			Long i = 0l;
			while ((b = in.readLine()) != null) {
				startTime = System.currentTimeMillis();
				try {
					i = jedis.lpush("rsyslog#message", b);
					endTime = System.currentTimeMillis();
					LogUtil.inMessageLog(b);
					if(i>500)
					{
						LogUtil.systemLog("redis队列存在的日志条数：" + i + ",消耗的时间：" + (endTime - startTime));
					}
				} catch (JedisConnectionException e) {
					if (jedis != null) {
						if (broken) {
							JedisUtil.returnBrokenResource(jedis);
						} else {
							JedisUtil.returnResource(jedis);
						}
					}
					jedis = JedisUtil.getJedis();
					i = jedis.lpush("rsyslog#message", b);
					endTime = System.currentTimeMillis();
					LogUtil.inMessageLog(b);
					if(i>500)
					{
						LogUtil.systemLog("redis队列存在的日志条数：" + i + ",消耗的时间：" + (endTime - startTime));
					}
				}
			}
			in.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof JedisConnectionException)
				broken = true;
			LogUtil.systemLogError("socket 接收数据出错！"+b);
		} finally {
			try {
				if (socket != null)
					socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (jedis != null) {
				if (broken) {
					JedisUtil.returnBrokenResource(jedis);
				} else {
					JedisUtil.returnResource(jedis);
				}
			}
		}
	}

	public Socket getSocket() {
		return socket;
	}

}