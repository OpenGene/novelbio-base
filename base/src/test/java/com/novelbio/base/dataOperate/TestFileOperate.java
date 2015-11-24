package com.novelbio.base.dataOperate;

import org.junit.Test;

import com.novelbio.base.fileOperate.FileOperate;

import junit.framework.TestCase;

public class TestFileOperate extends TestCase {
//	public void testFileLink() {
//		FileOperate.linkFile("/media/nbfs/app/gs-yarn-basic/gs-yarn-basic-container-0.1.0.jar", "/media/nbfs/app/gs-yarn-basic/tset", true);
//	}
//	
	
	public void testFileSize() {
		long t = Long.MAX_VALUE;
		long a = 0;
		long start = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			a = t / 1024 / 1024 / 1024;
		}
		long end = System.currentTimeMillis();
		System.out.println("time1=" + (end - start));

		start = System.currentTimeMillis();
		for (int i = 0; i < 1000000; i++) {
			a = t >> 30;
		}
		end = System.currentTimeMillis();
		System.out.println("time2=" + (end - start));
		
		t = FileOperate.getFileSizeLong("/media/nbfs/nbCloud/public/AllProject/project_550a6f82e4b0b3b73a8e211e/task_559a3fb7e4b0095074afad32/other_result");
		System.out.println(t);
		System.out.println(t / 1024 / 1024 / 1024.0);
	}
	
	@Test
	public void testDelFile(){
		boolean isdel = FileOperate.delFile("/home/novelbio/abc.xls");
		System.out.println(isdel);
	}
}
