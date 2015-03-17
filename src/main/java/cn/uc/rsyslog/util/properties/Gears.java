package cn.uc.rsyslog.util.properties;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
 * 装备配置文件
 * @author sungq
 *
 */
public class Gears{
	private static Map<Integer, Gear> gears = new HashMap<Integer, Gear>();
	private static FileUtil fileUtil = new FileUtil();
	static {
		URL u = ConfigUtil.class.getProtectionDomain().getCodeSource().getLocation();
		File f = new java.io.File(u.getFile());
		try {
			u = new File(f.getParent() + File.separator +"resources"+File.separator +"bussiness"+File.separator +"gears.xml").toURI().toURL();
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
					LogUtil.systemLog("装备文件更新");
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
			String type = element.attributeValue("quality");
			if("0".equals(type))
			{
				type = "白装";
			}
			if("1".equals(type))
			{
				type = "绿装";
			}
			if("2".equals(type))
			{
				type = "蓝装";
			}
			if("3".equals(type))
			{
				type = "紫装";
			}
			if("4".equals(type))
			{
				type = "橙装";
			}
			if("5".equals(type))
			{
				type = "黑装";
			}
			List<Element> eList = element.elements();
			for (Iterator iterator = eList.iterator(); iterator.hasNext();) {
				Element element2 = (Element) iterator.next();
				Gear gear = new Gear();
				gear.setName(element2.attributeValue("name"));
				gear.setId(Integer.parseInt(element2.attributeValue("id")));
				gear.setType(type);
				gears.put(gear.getId(), gear);
			}
		}
	
	}
	public static String getGearsName(int id){
		return gears.get(id)!=null?gears.get(id).getName():"未知装备，装备id为："+id;
	}
}
