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
 * 技能配置文件
 * @author gj
 * @authon xied
 * @version1.1
 */
public class Skills{
	private static Map<Integer, Skill> skills = new HashMap<Integer, Skill>();
	private static FileUtil fileUtil = new FileUtil();
	static {
		URL u = ConfigUtil.class.getProtectionDomain().getCodeSource().getLocation();
		File f = new java.io.File(u.getFile());
		try {
			u = new File(f.getParent() + File.separator +"resources"+File.separator +"bussiness"+File.separator +"skills.xml").toURI().toURL();
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
					LogUtil.systemLog("技能配置文件更新");
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
			Skill skill = new Skill();
			skill.setSkillId(Integer.parseInt(element.attributeValue("id")));
			skill.setSkillName(element.attributeValue("name"));
			skills.put(skill.getSkillId(), skill);
		}
	}
	public static String getSkillName(Integer skillId)
	{
		Skill skill = skills.get(skillId);
		if(skill!=null)
		{
			return skill.getSkillName();
		}else
		{
			return "未知技能，技能id为"+skillId;
		}
	}
}
