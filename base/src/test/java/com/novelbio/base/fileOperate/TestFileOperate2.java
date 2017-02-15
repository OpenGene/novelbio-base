package com.novelbio.base.fileOperate;

import java.nio.file.Path;

import org.junit.Test;

import com.novelbio.base.dataOperate.TxtReadandWrite;

import junit.framework.Assert;

public class TestFileOperate2 {
	@Test
	public void testGetPath0() {
		Path path = FileOperate.getPath("hdfs://cluster/home/novelbio/testHadoop", "fileName3");

		if (!FileOperate.isFileFolderExist(path)) {
			FileOperate.createFolders(path);
		}
		Assert.assertEquals(true, FileOperate.isFileFolderExist(path));

	}

	@Test
	public void testGetPath1() {
		Path path = FileOperate.getPath("oss://novelbrain/home/novelbio/testHadoop", "fileName3");

		if (!FileOperate.isFileExist(path)) {
			TxtReadandWrite writter = new TxtReadandWrite(path);
			writter.writefile("this is a test file");
			writter.close();

		}
		Assert.assertEquals(true, FileOperate.isFileExist(path));

	}
}
