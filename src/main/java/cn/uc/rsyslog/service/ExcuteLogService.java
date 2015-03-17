package cn.uc.rsyslog.service;

import cn.uc.rsyslog.util.LogDto;
import cn.uc.rsyslog.util.properties.LogTable;

public interface ExcuteLogService{
  /**
   *־
   * @param log
   */
  public void excute(LogDto logDto) throws Exception;
  /**
   * 初始化sql语句
   * @throws Exception
   */
  public void initSql(LogTable logTable);
  
}
