package cn.uc.rsyslog.util;

import java.io.File;
import java.net.URL;

public class FileUtil {
	/**
	 * 文件最后更新时间
	 */
	private long lastModifyTime = 0;
	/**
	 * 判断文件是否有更新
	 * @param file
	 * @return
	 */
	public boolean isUpdate(URL url)
	{
	  File file = new File(url.getFile());
	  if(file.exists())	
	  {
		  if(lastModifyTime == 0)
		  {
			  lastModifyTime = file.lastModified();
			  return true;
		  }
		  long currentFileModifyTime = file.lastModified();
		  if(currentFileModifyTime>lastModifyTime)
		  {
			  lastModifyTime = currentFileModifyTime;
			  return true;
		  }
		  return false;
	  }
	  return false;
	}
	public long getLastModifyTime() {
		return lastModifyTime;
	}
	public void setLastModifyTime(long lastModifyTime) {
		this.lastModifyTime = lastModifyTime;
	}
	
}
