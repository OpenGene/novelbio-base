package com.novelbio.base.fileOperate;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public class TestFileOperateCos {
	@Test
	public void testGetPath() {
		Path path = FileOperate.getPath("cos://novelbio-1255651097/aaa/bbb.txt");
		Assert.assertNotNull(path);
	}

	@Test
	public void testIsFileExist() {
		String pathLocal = TestFileOperateCos.class.getClassLoader().getResource("testTrinity.fa").getPath();
		Path path1 = FileOperate.getPath("cos://novelbio-1255651097/aaa/bbb.txt");
		FileOperate.moveFile(true, FileOperate.getPath(pathLocal), path1);
		Path path = FileOperate.getPath("cos://novelbio-1255651097/aaa/bbb.txt");
		Assert.assertEquals(true, FileOperate.isFileExist(path));
		Path path2 = FileOperate.getPath("cos://novelbio-1255651097/aaa/bbb2.txt");
		Assert.assertEquals(false, FileOperate.isFileExist(path2));
	}
	
	@Test
	public void testIsFileDirectory() {
		Path path = FileOperate.getPath("cos://novelbio-1255651097/aaa/");
		Assert.assertEquals(true, FileOperate.isFileExist(path));
		Assert.assertEquals(true, FileOperate.isFileDirectory(path));
		Path path2 = FileOperate.getPath("cos://novelbio-1255651097/bbb/");
		Assert.assertEquals(false, FileOperate.isFileDirectory(path2));
	}
	
	@Test
	public void testCreateFolders() {
		Path path = FileOperate.getPath("cos://novelbio-1255651097/ccc/");
		FileOperate.createFolders(path);
		Assert.assertEquals(true, FileOperate.isFileDirectory(path));
		FileOperate.deleteFileFolder(path);
		Assert.assertEquals(false, FileOperate.isFileDirectory(path));
	}
	
	@Test
	public void testGetLsFoldFileName() {
		Path path = FileOperate.getPath("cos://novelbio-1255651097/");
		ArrayList<String> lsPath = FileOperate.getLsFoldFileName(path);
		lsPath.forEach(tmpPath -> System.out.println(tmpPath));
		Assert.assertEquals(true, lsPath.size() > 0);
		Path path2 = FileOperate.getPath("cos://novelbio-1255651097/abc/");
		ArrayList<String> lsPath2 = FileOperate.getLsFoldFileName(path2);
		lsPath2.forEach(tmpPath -> System.out.println(tmpPath));
		//因为包含自身
		Assert.assertEquals(true, lsPath2.size() == 0);
	}
	
	@Test
	public void testGetSeekablePathInputStream() {
		Path path = FileOperate.getPath("cos://novelbio-1255651097/aaa/bbb.txt");
		InputStream inputStreamRaw = null;
		InputStream inputStream = null;
		try {
			inputStreamRaw = FileOperate.getSeekablePathInputStream(path);
			inputStream = new BufferedInputStream(new GZIPInputStream(inputStreamRaw, TxtReadandWrite.bufferLen));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		int len = 5120;
		byte buffer[] = new byte[len];
		int total = 0;
		String previewFile = null;
		try {
			inputStream.read(buffer, total, len);
			// end by fans.fan
			previewFile = new String(buffer, "utf-8");
		} catch (UnsupportedEncodingException e) {
		} catch (Exception e) {
		} finally {
			FileOperate.close(inputStream);
			FileOperate.close(inputStreamRaw);
		}
		System.out.println(previewFile);
	}
	
	@Test
	public void testMoveFile() {
//		String path = new File("testTrinity.fa");
//		Path path1 = FileOperate.getPath("cos://novelbio-1255651097/aaa/bbb.txt");
//		Path path2 = FileOperate.getPath("cos://novelbio-1255651097/aaa/ddd.txt");
//		FileOperate.copyFile(FileOperate.getPath(path), path1, true);
//		FileOperate.moveFile(true, path1, path2);
//		Assert.assertEquals(true, FileOperate.isFileExistAndBigThan0(path2));
//		Assert.assertEquals(false, FileOperate.isFileExist(path1));
	}
	
	
}
