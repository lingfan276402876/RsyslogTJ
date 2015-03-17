package cn.uc.rsyslog.util;

import org.apache.commons.lang.StringUtils;

import cn.uc.rsyslog.util.properties.Gates;
import cn.uc.rsyslog.util.properties.Props;
import cn.uc.rsyslog.util.properties.Skills;
import cn.uc.rsyslog.util.properties.Weapons;

public class SelfFunction {

	/**
	 * 返回金松果收支的
	 * @param type
	 * type=1 买道具 2 松子兑换 3 U点兑换 4 合成装备 5 融合装备 6 升级武器 7 升级技能 8 收徒 9 退出师门 10 天梯赛收费 11 复仇 12 天梯赛商店 13 登陆奖励 14 挑战关卡 15 出售装备 16 开金宝箱
	 * @return
	 */
	public String goldType(String t) {
		int type = parseInt(t);
		String remark = "";
		switch (type) {
		case 1:
            remark = "买道具";
			break;
		case 2:
            remark = "松子兑换";
			break;
		case 3:
            remark = "U点兑换";
			break;
		case 4:
            remark = "合成装备";
			break;
		case 5:
            remark = "融合装备";
			break;
		case 6:
            remark = "升级武器";
			break;
		case 7:
            remark = "升级技能";
			break;
		case 8:
            remark = "收徒";
			break;
		case 9:
            remark = "退出师门";
			break;
		case 10:
            remark = "天梯赛收费";
			break;
		case 11:
            remark = "复仇";
			break;
		case 12:
            remark = "天梯赛商店";
			break;
		case 13:
            remark = "登陆奖励";
			break;
		case 14:
            remark = "挑战关卡";
			break;
		case 15:
            remark = "出售装备";
			break;
		case 16:
            remark = "开金宝箱";
			break;
		default:
			remark = "未知";
			break;
		}
		return remark;
	}
	/**
	 * 竞技场使用道具
	 * @param useProp 道具种类
	 * @param propCount 道具数量
	 * @return
	 */
	public String arenaUseProp(String useProp,String propCount)
	{
		String remark = "";
		switch (Integer.parseInt(useProp)) {
		case 1:
            remark = "消耗"+propCount+"点体力";
			break;
		case 2:
            remark = "消耗"+propCount+"个英雄帖";;
			break;
		case 3:
            remark = "消耗"+propCount+"个勇气徽章";;
			break;
		default:
			remark = "未知";
			break;
		}
		return remark;
	}
	/**
	 * 为师傅加油
	 * @param type
	 * @return
	 */
	public String rankFighting(String type)
	{
		String remark = "";
		switch (Integer.parseInt(type)) {
		case 1:
            remark = "加成5%的力量";
			break;
		case 2:
            remark = "加成5%的敏捷";;
			break;
		case 3:
            remark = "加成5%的速度";;
			break;
		default:
			remark = "未知";
			break;
		}
		return remark;
	}
	/**
	 * 玩家升级奖励
	 * @param power
	 * @param agility
	 * @param speed
	 * @param hp
	 * @param weaponId
	 * @param skillId
	 * @param propId
	 * @return
	 */
	public String levelUpReward(String power,String agility,String speed,String hp,String skillId,String weaponId,String propId)
	{
		StringBuffer remark = new StringBuffer("增加");
		int power_ = parseInt(power);
		if(power_>0)
		{
			remark.append(power_+"力量,");
		}
		int agility_ = parseInt(agility);
		if(agility_>0)
		{
			remark.append(agility_+"敏捷,");
		}
		int speed_ = parseInt(speed);
		if(speed_>0)
		{
			remark.append(speed_+"速度,");
		}
		int hp_ = parseInt(hp);
		if(hp_>0)
		{
			remark.append(hp_+"生命,");
		}
		int weaponId_ = parseInt(weaponId);
		if(weaponId_>0)
		{
			remark.append("。获得了武器“"+Weapons.getWeaponName(weaponId_)+"”");
		}
		int skillId_ = parseInt(skillId);
		if(skillId_>0)
		{
			remark.append("。获得了技能“"+Skills.getSkillName(skillId_)+"”");
		}
		int propId_ = parseInt(propId);
		if(propId_>0)
		{
			remark.append("。获得了道具“"+Props.getPropName(propId_)+"”");
		}
		return remark.toString();
	}
	
	/**
	 * 根据金杯数量判断竞技场输赢
	 * @param arenaChampionCount
	 * @return
	 */
	public String joinResult(String arenaChampionCount)
	{
		if(arenaChampionCount!=null)
		{
			int c = parseInt(arenaChampionCount);
			if(c>0)
			{
				return "成功";
			}else
			{
				return "失败";
			}
		}else
		{
			return "失败";
		}
	}
	/**
	 * 获取战斗的对象
	 * @param opponent
	 * @return
	 */
	public String getOpponent(String opponent)
	{
		if(StringUtils.isEmpty(opponent))
		{
			return null;
		}else if("null".equals(opponent))
		{
			return "NPC";
		}else
		{
			return opponent;
		}
	}
	/**
	 * 获取关卡名称
	 * @param id
	 * @return
	 */
	public String stage(String id)
	{
		if(StringUtils.isEmpty(id))
		{
			return "未知";
		}else
		{
			return Gates.getGateName(Integer.parseInt(id));
		}
	}
	public String gem(String id)
	{
		int propId = parseInt(id);
		if(propId>0)
		{
			return "以外获得了一颗"+Props.getPropName(propId);
		}else
		{
			return "null";
		}
	}
	
	public String prop(String prop_id,String count)
	{
		int propId = parseInt(prop_id);
		int propCount = parseInt(count);
		if(propId>0 && propId!=15)
		{
			return Props.getPropName(propId)+"道具"+propCount+"个";
		}else if(propId==15)
		{
			return Props.getPropName(propId)+propCount+"点";
		}else
		{
			return "未知道具"+propCount+"个，道具id为"+prop_id;
		}
	}
	
	private int parseInt(String s)
	{
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {
			return 0;
		}
	}
}
