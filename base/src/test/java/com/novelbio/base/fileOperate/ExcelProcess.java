package com.novelbio.base.fileOperate;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class ExcelProcess {

	public static void main(String[] args) {

		System.out.println("start...");
		TxtReadandWrite excelOperate = new TxtReadandWrite("/home/novelbio/windows/data.txt");
		ArrayList<String> lsData = excelOperate.readfileLs();
		excelOperate.close();

		String REGEX_EMAIL = "^\\w[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?";
		
		TxtReadandWrite err = new TxtReadandWrite("/home/novelbio/windows/dataErr.txt", true);
		int row = 0;
		for (String email : lsData) {
			email = email.trim().replace("\n", "");
			if (StringOperate.isRealNull(email)) {
				err.writefileln("ng | email=" + email + "=");
				row++;
				continue;
			}
			if (!Pattern.matches(REGEX_EMAIL, email)) {
				err.writefileln("ng | email=" + email + "=");
				row++;
				continue;
			}
			err.writefileln("ok | email=" + email + "=");
			row++;
		}
		err.flush();
		err.close();

		System.out.println("end...");
	}

}
