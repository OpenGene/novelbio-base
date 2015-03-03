package com.novelbio.base.dataOperate;

import java.io.IOException;

import org.powermock.api.mockito.PowerMockito;

import junit.framework.TestCase;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.ImageUtils;

public class TestImageOperate extends TestCase {
	
	public void testConvertTiff2Png() throws IOException {
		String tiffImagePath = "/media/nbfs/nbCloud/testCode/testImageConvert/intersection.tiff";
		String pngImagePath = "/media/nbfs/nbCloud/testCode/testImageConvert/intersection.png";
		FileOperate.delFile(pngImagePath);
		ImageUtils.read(tiffImagePath);
		assertEquals(true, FileOperate.isFileExist(pngImagePath));
		
	}

}
