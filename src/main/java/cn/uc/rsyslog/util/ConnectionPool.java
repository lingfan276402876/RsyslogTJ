package cn.uc.rsyslog.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.dbcp.BasicDataSource;

/**
 * 连接池
 * 
 * @author Administrator
 * 
 */
public class ConnectionPool {
	public static Map<String,BasicDataSource> dses = new HashMap<String, BasicDataSource>();
	static {
		new ConnectionPool();
	}
	private ConnectionPool() {
		BasicDataSource ds1 = null;
		ds1 = new BasicDataSource();
		ds1.setDriverClassName("com.mysql.jdbc.Driver");
		ds1.setUrl(ConfigUtil.jdbc1mysqlUrl);
		ds1.setUsername(ConfigUtil.jdbc1username);
		ds1.setPassword(ConfigUtil.jdbc1password);
		ds1.setInitialSize(5);
		ds1.setMaxActive(50);
		ds1.setMaxIdle(10);
		ds1.setMaxWait(10000);
		ds1.setMinIdle(5);
		ds1.setTestOnBorrow(true);
		ds1.setTimeBetweenEvictionRunsMillis(1*1000);
		ds1.setMinEvictableIdleTimeMillis(1*1000);
		BasicDataSource ds2 = null;
		ds2 = new BasicDataSource();
		ds2.setDriverClassName("com.mysql.jdbc.Driver");
		ds2.setUrl(ConfigUtil.jdbc2mysqlUrl);
		ds2.setUsername(ConfigUtil.jdbc2username);
		ds2.setPassword(ConfigUtil.jdbc2password);
		ds2.setInitialSize(5);
		ds2.setMaxActive(100);
		ds2.setMaxIdle(50);
		ds2.setMaxWait(10000);
		ds2.setMinIdle(5);
		ds2.setTimeBetweenEvictionRunsMillis(1*1000);
		ds2.setMinEvictableIdleTimeMillis(1*1000);
		ds2.setTestOnBorrow(true);
		dses.put("jdbc1", ds1);
		dses.put("jdbc2", ds2);
	}

	public static Connection getConnection() {
		Connection con = null;
		BasicDataSource ds = dses.get("jdbc1");
		if (ds != null) {
			try {
				con = ds.getConnection();
			} catch (Exception e) {
				e.printStackTrace();
				LogUtil.systemLog("数据库连接异常："+e.getMessage());
			}
			try {
				con.setAutoCommit(false);
			} catch (SQLException e) {
				e.printStackTrace();
				LogUtil.systemLog("数据库连接异常："+e.getMessage());
			}
			return con;
		}
		return con;
	}
	public static Connection getConnection(String sourceName) {
		Connection con = null;
		BasicDataSource ds = dses.get(sourceName);
		if (ds != null) {
			try {
				con = ds.getConnection();
			} catch (Exception e) {
				LogUtil.systemLog("数据库连接异常："+e.getMessage());
			}
			try {
				con.setAutoCommit(false);
			} catch (SQLException e) {
				LogUtil.systemLog("数据库连接异常："+e.getMessage());
			}
			return con;
		}
		return con;
	}
	public static void main(String[] args) {
		while (true) {
			Connection connection = ConnectionPool.getConnection();
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
