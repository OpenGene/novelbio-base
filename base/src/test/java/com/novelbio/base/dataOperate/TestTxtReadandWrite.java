package com.novelbio.base.dataOperate;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.novelbio.base.fileOperate.FileOperate;

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
		
//		try {
//			InputStream inputStream = FileOperate.getInputStream("/hdfs:/nbCloud/public/AllProject/project_52b7df12e4b06767c86da661/task_533e5f77e4b0971a65462e13/QualityControl_result/QCResults/BaseGCContent_Case3_AfterFilter.png");
//			byte[] b = null;
//			System.out.println(b.toString());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}
}
