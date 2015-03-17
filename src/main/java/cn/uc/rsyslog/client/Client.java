package cn.uc.rsyslog.client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import cn.uc.rsyslog.util.ConfigUtil;
import cn.uc.rsyslog.util.ConnectionPool;

public class Client {
    public Client() {
	}
	public void startClient() throws SQLException {
		Socket socket;
		PrintWriter out;
		try {
			while (true) {
				socket = new Socket(ConfigUtil.server, ConfigUtil.port);
				out = new PrintWriter(socket.getOutputStream(), true);
				long index = loadIndex();
				loadLog(out,index);
				out.close();
				socket.close();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 从数据库中获取日志
	 * @param index
	 * @return
	 * @throws SQLException
	 */
	private void loadLog(PrintWriter out,long index) throws SQLException {
		String log = null;
		Connection connection = ConnectionPool.getConnection("jdbc2");
		try {
			String sql = "select message from fightGameLog limit ?,"+ConfigUtil.queueMaxsize;
			java.sql.PreparedStatement ps = connection.prepareStatement(sql);
			ps.setLong(1,index);
			ResultSet set = ps.executeQuery();
			int size = 0;
			while (set.next()) {
				log = "client_".concat(set.getString("message"));
				out.println(log);
				out.flush();
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				size++;
			}
			updateIndex(size);
			set.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
	}
	/**
	 * 获取日志索引记录数
	 * @throws SQLException 
	 */
	public long loadIndex() throws SQLException
	{
		long index = 0;
		Connection connection = ConnectionPool.getConnection("jdbc2");
		try {
			String sql = "select log_index from log_index";
			java.sql.PreparedStatement ps = connection.prepareStatement(sql);
			ResultSet set = ps.executeQuery();
			while (set.next()) {
				index = set.getLong("log_index");
			}
			set.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
		return index;
	}
	/**
	 * 更新日志索引记录数
	 * @throws SQLException 
	 */
	public static synchronized int updateIndex(int pageSize) throws SQLException
	{
		int index = 0;
		Connection connection = ConnectionPool.getConnection("jdbc2");
		try {
			String sql = "update log_index set log_index=log_index+"+pageSize;
			java.sql.PreparedStatement ps = connection.prepareStatement(sql);
			ps.executeUpdate();
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
		return index;
	}
	public static void main(String[] args) throws SQLException {
			Timer timer = new Timer();
			timer.schedule(new TestTimerTask(), 0l, 1000*10l);
	}

	static class TestTimerTask extends TimerTask {
		@Override
		public void run() {
			System.out.println(Calendar.getInstance().getTime());

		}
	}

}
