package cn.uc.rsyslog.util.properties;

import cn.uc.rsyslog.util.DataType;

/**
 * 配置文件filed
 * 
 * @author sungq
 * 
 */
public class LogFiled {
	/**
	 * 数据库表字段
	 */
	private String tableFiled;
	/**
	 * 日志对应的索引编号 从0开始
	 */
	private Integer logFiledIndex;
	/**
	 * 常量值
	 */
	private String constant;
	/**
	 * 表达式
	 */
	private String expression;
     
	private String[] variables;
	/**
	 * 数据类型
	 */
	private DataType dataType;
	public String getTableFiled() {
		return tableFiled;
	}

	public void setTableFiled(String tableFiled) {
		this.tableFiled = tableFiled;
	}

	public Integer getLogFiledIndex() {
		return logFiledIndex;
	}

	public void setLogFiledIndex(Integer logFiledIndex) {
		this.logFiledIndex = logFiledIndex;
	}

	public String getConstant() {
		return constant;
	}

	public void setConstant(String constant) {
		this.constant = constant;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String[] getVariables() {
		return variables;
	}

	public void setVariables(String[] variables) {
		this.variables = variables;
	}

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}
}
