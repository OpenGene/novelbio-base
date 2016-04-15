package com.novelbio.base.dataOperate;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.powermock.api.mockito.PowerMockito;

import junit.framework.TestCase;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.plot.ImageUtils;

public class TestImageOperate extends TestCase {
	
	public void testConvertTiff2Png() throws IOException {
		String tiffImagePath = "src/test/resources/images/intersection.tiff";
		String pngImagePath = "src/test/resources/images/intersection.png";
		FileOperate.delFile(pngImagePath);
		BufferedImage bufferedImage = ImageUtils.read(tiffImagePath);
		ImageUtils.saveBufferedImage(bufferedImage, pngImagePath);
		assertEquals(true, FileOperate.isFileExist(pngImagePath));
		FileOperate.delFile(pngImagePath);

	}

}
