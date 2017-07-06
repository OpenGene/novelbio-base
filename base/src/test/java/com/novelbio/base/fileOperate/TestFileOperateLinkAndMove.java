package com.novelbio.base.fileOperate;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.TxtReadandWrite;

public class TestFileOperateLinkAndMove {
	static String tmpDir = PathDetail.getTmpPathRandom();
	static String tmpDir2 = PathDetail.getTmpPathRandom();
	static String tmpDirLink = PathDetail.getTmpPathRandomNotCreate();

	@Before
	public void Before() {
		FileOperate.linkFile(tmpDir, tmpDirLink, true);
		FileOperate.deleteOnExit(tmpDir);
		FileOperate.deleteOnExit(tmpDir2);
		FileOperate.deleteOnExit(tmpDirLink);

	}
	/**
	 * 把A文件夹链接到B
	 * 然后把B中的子文件剪切/拷贝到A中
	 * 理论上应该不变
	 * 
	 * 譬如 /home/tmp/A linkto /home/tmp/link
	 * 把 /home/tmp/link/file copyto /home/tmp/A
	 * 应该没变化
	 */
	@Test
	public void testLinkAndMove() {
		TxtReadandWrite txtWrite = new TxtReadandWrite(tmpDir + "test.txt", true);
		txtWrite.writefileln("test");
		txtWrite.close();
		TxtReadandWrite txtWrite2 = new TxtReadandWrite(tmpDir2 + "test.txt", true);
		txtWrite2.writefileln("test2222");
		txtWrite2.close();
		
		long fileSizeOld = FileOperate.getFileSizeLong(tmpDir + "test.txt");
		long fileSizeOther = FileOperate.getFileSizeLong(tmpDir2 + "test.txt");
		System.out.println(fileSizeOld);
		System.out.println(fileSizeOther);
		Assert.assertFalse(fileSizeOld == fileSizeOther);

		FileOperate.moveFile(true, tmpDir + "test.txt", tmpDirLink + "test.txt");
		Assert.assertEquals(fileSizeOld, FileOperate.getFileSizeLong(tmpDirLink + "test.txt"));
		
		FileOperate.moveFile(true, tmpDir + "test.txt", tmpDir2 + "test2.txt");
		Assert.assertEquals(fileSizeOld, FileOperate.getFileSizeLong(tmpDir2 + "test2.txt"));
	}
	
	@Test
	public void testDeleteLinkPath() {
		TxtReadandWrite txtWrite = new TxtReadandWrite(tmpDir + "test.txt", true);
		txtWrite.writefileln("test");
		txtWrite.close();
		
		Assert.assertTrue(FileOperate.isFileExist(tmpDirLink));
		Assert.assertTrue(FileOperate.isFileExist(tmpDirLink+ "test.txt"));
		Assert.assertEquals(FileOperate.getCanonicalPath(tmpDirLink+ "test.txt"), FileOperate.getCanonicalPath(tmpDir+ "test.txt"));
		FileOperate.deleteFileFolder(tmpDirLink);
		Assert.assertFalse(FileOperate.isFileExist(tmpDirLink));
		Assert.assertTrue(FileOperate.isFileExist(tmpDir + "test.txt"));
	}
}
