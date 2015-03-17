package cn.uc.rsyslog.test;

public class ExceptionDemo1 {
	public static void main(String args[]) {
		Math m = new Math();
		try {
			int temp = m.div(10, 0);
			System.out.println(temp);
		} catch (Exception e) {
			 System.out.println(e);
		}// try.catch不是一定要跟一个finally作为出口吗?为什么这里没有?
	}
}
