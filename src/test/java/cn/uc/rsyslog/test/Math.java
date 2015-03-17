package cn.uc.rsyslog.test;

import java.lang.reflect.InvocationTargetException;

public class Math {
	public int div(int i,int j) throws InvocationTargetException{
		System.out.println("===进行操作前====");
		int temp = 0;
		try{
			temp =i/j;
		}catch(Exception e){
			throw new InvocationTargetException(new Throwable("1111111111"));//抛出异常
		}finally{
			System.out.println("===进行操作后====");
		}
		return temp;
	}
}
