package cn.uc.rsyslog.util.properties;

import cn.uc.rsyslog.util.properties.RankShop.GoodsType;

public class RankGoods {
	private int id;
	private String name;
	private int goodsId;
	private GoodsType type;
	private int cup;
	private int gold;
	private int integral;
	private int timesLimit;
	private int count;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(int goodsId) {
		this.goodsId = goodsId;
	}
	
	public GoodsType getType() {
		return type;
	}
	public void setType(GoodsType type) {
		this.type = type;
	}
	public int getCup() {
		return cup;
	}
	public void setCup(int cup) {
		this.cup = cup;
	}
	public int getGold() {
		return gold;
	}
	public void setGold(int gold) {
		this.gold = gold;
	}
	public int getIntegral() {
		return integral;
	}
	public void setIntegral(int integral) {
		this.integral = integral;
	}
	public int getTimesLimit() {
		return timesLimit;
	}
	public void setTimesLimit(int timesLimit) {
		this.timesLimit = timesLimit;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
	public boolean isTimesLimit(){
		return this.timesLimit>0;
	}
}
