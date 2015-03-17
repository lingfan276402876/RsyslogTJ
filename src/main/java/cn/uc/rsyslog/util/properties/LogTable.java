package cn.uc.rsyslog.util.properties;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.uc.rsyslog.util.ConfigUtil;
import cn.uc.rsyslog.util.ConnectionPool;
import cn.uc.rsyslog.util.LogUtil;

/**
 * 日志表
 * 
 * @author sungq
 * 
 */
public class LogTable {
	/**
	 * 日志类型
	 */
	private String logType;
	/**
	 * 表名称
	 */
	private String table;
	/**
	 * sql语句执行方式
	 */
	private String sqlExcute;
	/**
	 * class名字
	 */
	private String className;
	/**
	 * 特殊字段 用于使用
	 */
	private String special;
	/**
	 * 日志字段
	 */
	private List<LogFiled> logFiledes = new ArrayList<LogFiled>();
	/**
	 * 索引字段
	 */
	private List<TableIndex> tableIndexs = new ArrayList<TableIndex>();
	/**
	 * 非常量日志字段
	 */
	private List<LogFiled> unConstantFiledes = new ArrayList<LogFiled>();

	public String getLogType() {
		return logType;
	}

	public void setLogType(String logType) {
		this.logType = logType;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getSqlExcute() {
		return sqlExcute;
	}

	public void setSqlExcute(String sqlExcute) {
		this.sqlExcute = sqlExcute;
	}

	public List<LogFiled> getLogFiledes() {
		return logFiledes;
	}

	public void setLogFiledes(List<LogFiled> logFiledes) {
		this.logFiledes = logFiledes;
	}

	public List<LogFiled> getUnConstantFiledes() {
		return unConstantFiledes;
	}

	public void setUnConstantFiledes(List<LogFiled> unConstantFiledes) {
		this.unConstantFiledes = unConstantFiledes;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getSpecial() {
		return special;
	}

	public void setSpecial(String special) {
		this.special = special;
	}

	public List<TableIndex> getTableIndexs() {
		return tableIndexs;
	}

	public void setTableIndexs(List<TableIndex> tableIndexs) {
		this.tableIndexs = tableIndexs;
	}

	public void initTable() {
		int tableCount = getTableCount(this.table);
		if (tableCount < 1) {
			StringBuilder builder = new StringBuilder("CREATE TABLE ");
			builder.append(this.table + "(");
			builder.append(" `id` bigint(20) NOT NULL AUTO_INCREMENT,");
			for (LogFiled field : logFiledes) {
				String sql = field.getDataType().getCreateTableSql(field.getTableFiled());
				if (sql != null) {
					builder.append(sql + ",");
				}
			}
			builder.append(" PRIMARY KEY (`id`)");
			builder.append(" )ENGINE=MyISAM DEFAULT CHARSET=utf8");
			LogUtil.systemLog(MessageFormat.format("创建表{0}的语句是{1}", this.table, builder.toString()));
			if (!ConfigUtil.IS_DEV) {
				Connection connection = ConnectionPool.getConnection();
				try {
					PreparedStatement preparedStatement = connection.prepareStatement(builder.toString());
					preparedStatement.execute();
					preparedStatement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					if (connection != null) {
						try {
							connection.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
			}
			LogUtil.systemLog(MessageFormat.format("创建表{0}成功", this.table));
		}
		createColum(this.table);
		createIndex(this.table);
	}

	public void createColum(String tableName) {
		if (!ConfigUtil.IS_DEV) {
			Connection connection = ConnectionPool.getConnection();
			try {
				PreparedStatement preparedStatement = connection.prepareStatement("SHOW COLUMNS FROM " + tableName);
				List<String> columnsList = new ArrayList<String>();
				ResultSet set = preparedStatement.executeQuery();
				while (set.next()) {
					columnsList.add(set.getString(1).toLowerCase());
				}
				String sql = "ALTER TABLE " + tableName + " add column ";
				connection.prepareStatement("");
				for (LogFiled filed : this.logFiledes) {
					if (!columnsList.contains(filed.getTableFiled().trim().toLowerCase())) {
						connection.prepareStatement(sql + " " + filed.getDataType().getCreateTableSql(filed.getTableFiled())).execute();
						connection.commit();
						LogUtil.systemLog(MessageFormat.format("为表{0}新增列{1}成功", this.table, filed.getTableFiled()));
					}
				}
				set.close();
				preparedStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (connection != null) {
					try {
						connection.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void createIndex(String tableName) {
		if (!ConfigUtil.IS_DEV) {
			Connection connection = ConnectionPool.getConnection();
			try {
				PreparedStatement preparedStatement = connection.prepareStatement("SHOW INDEX FROM " + tableName);
				Map<String, String> indexMap = new HashMap<String, String>();
				ResultSet set = preparedStatement.executeQuery();
				while (set.next()) {
					indexMap.put(set.getString(3).trim().toLowerCase(),set.getString(5).trim().toLowerCase());
				}
				set.close();
				for (TableIndex tableIndex : this.tableIndexs) {
					if (indexMap.get(tableIndex.getIndexName().trim().toLowerCase())==null) {
						String sql = "ALTER TABLE " + tableName + " add INDEX `" + tableIndex.getIndexName() + "`(" + tableIndex.getColumns() + ");";
						preparedStatement = connection.prepareStatement(sql);
						preparedStatement.executeUpdate();
						LogUtil.systemLog(MessageFormat.format("为表{0}新增索引{1}成功", this.table,tableIndex.getIndexName()));
					}
				}
				preparedStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (connection != null) {
					try {
						connection.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public int getTableCount(String tableName) {
		String tableSchema = getTableSchema();
		int tableCount = 0;
		if (!ConfigUtil.IS_DEV) {
			Connection connection = ConnectionPool.getConnection();
			try {
				PreparedStatement preparedStatement = connection.prepareStatement("select count(TABLE_NAME) from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA='" + tableSchema + "' and TABLE_NAME='" + tableName + "'");
				ResultSet set = preparedStatement.executeQuery();
				while (set.next()) {
					return set.getInt(1);
				}
				set.close();
				preparedStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (connection != null) {
					try {
						connection.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return tableCount;
	}

	public String getTableSchema() {
		if (ConfigUtil.IS_DEV) {
			return "";
		}
		String tableSchema = "";
		Connection connection = null;
		try {
			connection = ConnectionPool.getConnection();
			tableSchema = connection.getCatalog();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return tableSchema;
	}
}
