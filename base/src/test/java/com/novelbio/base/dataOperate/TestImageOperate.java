package com.novelbio.base.dataOperate;

import java.io.IOException;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.fileOperate.ImageOperate;

import junit.framework.TestCase;

public class TestImageOperate extends TestCase {
	
	public void testConvertTiff2Png() throws IOException {
		String tiffImagePath = "/media/nbfs/nbCloud/testCode/testImageConvert/intersection.tiff";
		String pngImagePath = "/media/nbfs/nbCloud/testCode/testImageConvert/intersection.png";
		FileOperate.delFile(pngImagePath);
		ImageOperate.convertTiff2Png(tiffImagePath, pngImagePath);
		assertEquals(true, FileOperate.isFileExist(pngImagePath));
	}

}
