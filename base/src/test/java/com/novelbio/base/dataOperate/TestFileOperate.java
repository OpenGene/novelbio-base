package com.novelbio.base.dataOperate;

import com.novelbio.base.fileOperate.FileOperate;

import junit.framework.TestCase;

public class TestFileOperate extends TestCase {
	public void testFileLink() {
		FileOperate.linkFile("/media/nbfs/app/gs-yarn-basic/gs-yarn-basic-container-0.1.0.jar", "/media/nbfs/app/gs-yarn-basic/tset", true);
	}
	
	
	public void testFileSize(){
		long t =	FileOperate.getFileSizeLong("/home/novelbio");
		System.err.println(t/1024/1024/1024);
	}
}
