package cn.uc.rsyslog.util.properties;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import cn.uc.rsyslog.util.ConfigUtil;
import cn.uc.rsyslog.util.FileUtil;
import cn.uc.rsyslog.util.LogUtil;

/**
 * 道具配置文件
 * 道具获得几率分为挑战NPC和玩家，分别计算
 * @see PlayrRewardProps,NpcRewardProps
 * @author gj
 * @authon xied
 * @version1.1
 */
public class RankShop{
	private static Map<Integer, RankGoods> rankgoods = new HashMap<Integer, RankGoods>();
	private static FileUtil fileUtil = new FileUtil();
	static {
		URL u = ConfigUtil.class.getProtectionDomain().getCodeSource().getLocation();
		File f = new java.io.File(u.getFile());
		try {
			u = new File(f.getParent() + File.separator +"resources"+File.separator +"bussiness"+File.separator +"rankShop.xml").toURI().toURL();
		} catch (MalformedURLException e2) {
			e2.printStackTrace();
		}
		final URL url = u;
		try {
			initialize(url);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
		Runnable command = new Runnable(){
			@Override
			public void run() {
				if (fileUtil.isUpdate(url)) {
					LogUtil.systemLog("天梯赛商品文件更新");
					try {
						initialize(url);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}	
			}
			
		};
		executor.scheduleAtFixedRate(command, 0, 10, TimeUnit.SECONDS);
	}
	
	private static void initialize(URL url) throws Exception {
		SAXReader reader = new SAXReader();
		Document document = reader.read(url);
		Element root = document.getRootElement();
		for (Iterator i = root.elementIterator(); i.hasNext();) {
			Element element = (Element) i.next();
			
			RankGoods goods=new RankGoods();
			
			goods.setId(Integer.parseInt(element.attributeValue("id")));
			goods.setName(element.attributeValue("name"));
			goods.setGoodsId(Integer.parseInt(element.attributeValue("goodsId")));
			int type = Integer.parseInt(element.attributeValue("type"));
			if(type==1)
				goods.setType(GoodsType.PROP);
			else if(type==2)
				goods.setType(GoodsType.GEAR);
			goods.setCup(Integer.parseInt(element.attributeValue("cup")));
			goods.setGold(Integer.parseInt(element.attributeValue("gold")));
			goods.setIntegral(Integer.parseInt(element.attributeValue("integral")));
			goods.setTimesLimit(Integer.parseInt(element.attributeValue("timesLimit")));
			goods.setCount(Integer.parseInt(element.attributeValue("count")));
			rankgoods.put(goods.getId(), goods);
		}
	
	}
	public static String getRankGoodsName(int id){
		return rankgoods.get(id)!=null?rankgoods.get(id).getName():"未知商品，商品id为："+id;
	}
	
	public enum GoodsType{
		PROP,GEAR;
	}
}
