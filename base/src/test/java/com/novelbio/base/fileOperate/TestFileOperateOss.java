package com.novelbio.base.fileOperate;

import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public class TestFileOperateOss {
	@Test
	public void testGetPath0() {
		Path path = FileOperate.getPath("hdfs://cluster/home/novelbio/testHadoop", "fileName3");

		if (!FileOperate.isFileExist(path)) {
			FileOperate.createFolders(path);
		}
		Assert.assertEquals(true, FileOperate.isFileExist(path));

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
	
	@Test
	public void testCreateFolders() {
		Path path = FileOperate.getPath("oss://novelbrainsz/ccc/");
		FileOperate.createFolders(path);
		Assert.assertEquals(true, FileOperate.isFileDirectory(path));
		FileOperate.deleteFileFolder(path);
		Assert.assertEquals(false, FileOperate.isFileDirectory(path));
	}
	
}
