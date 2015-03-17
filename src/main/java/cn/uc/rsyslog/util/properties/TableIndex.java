package cn.uc.rsyslog.util.properties;
/**
 * 表索引
 * @ClassName TableIndex
 * @Description TODO
 * @author sungq@ucweb.com
 * @date 2014年12月1日
 */

public class TableIndex {
	/**
	 * 索引列
	 */
	private String indexName;
	/**
	 * 列名
	 */
	private String columns;

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public String getColumns() {
		return columns;
	}

	public void setColumns(String columns) {
		this.columns = columns;
	}

}
