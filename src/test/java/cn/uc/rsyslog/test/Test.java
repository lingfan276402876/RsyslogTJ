package cn.uc.rsyslog.test;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Test {
	public static void main(String[] args) throws IOException {
//		/ System.out.println(ZipUtil.uncompress(ZipUtil.compress("中国China")));
		Scanner scanner = new Scanner(new File("/home/nemo/logs/tj/logs_analyze/systemLog.log.2014-11-26"));
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			//System.out.println(line);
			line = ZipUtil.unzipString(line);
			System.out.println(line);
		}
	}
}
