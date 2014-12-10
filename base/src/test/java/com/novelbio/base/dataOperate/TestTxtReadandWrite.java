package com.novelbio.base.dataOperate;

import java.util.List;

public class TestTxtReadandWrite {
	public static void main(String[] args) {
//		TxtReadandWrite txtWrite = new TxtReadandWrite(System.out);
//		for (int i = 0; i < 10; i++) {
//			txtWrite.writefileln("sss" + i);
//		}
//		txtWrite.close();
		TxtReadandWrite txtRead = new TxtReadandWrite("/hdfs:/nbCloud/public/AllProject/project_52b7df12e4b06767c86da661/task_533e5f77e4b0971a65462e13/QualityControl_result/QCResults/BaseGCContent_Case3_AfterFilter.png");
		List<String> lsString = txtRead.readfileLs();
		System.out.println(lsString.get(0));
		TxtReadandWrite txtWrite = new TxtReadandWrite("C:\\Documents and Settings\\Administrator\\桌面\\gooddd.png", true);
		txtWrite.writefile(lsString);
	}
}
