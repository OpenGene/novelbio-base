package com.novelbio.base.fileOperate;

import org.junit.Assert;
import org.junit.Test;

public class TestFileOperate {
	@Test
	public void testFile() {
		String aa = "/home/novelibo/test";
		Assert.assertEquals(2, FileOperate.getFolderParentNumber(aa));
		
		aa = "\\/\\/\\////home\\novelibo/\\test///ter4\rdftg";
		Assert.assertEquals(4, FileOperate.getFolderParentNumber(aa));
		
		aa = "fsefsae";
		Assert.assertEquals(0, FileOperate.getFolderParentNumber(aa));
	}
}
