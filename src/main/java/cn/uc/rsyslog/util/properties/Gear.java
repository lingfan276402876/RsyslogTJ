package cn.uc.rsyslog.util.properties;

/**
 * 装备
 * 
 * @author sungq
 * 
 */
public class Gear {
	private String name;
	private int id;
	private String type;

	public String getName() {
		return name+"("+this.type+")";
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
