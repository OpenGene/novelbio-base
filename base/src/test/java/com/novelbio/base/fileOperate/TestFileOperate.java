package com.novelbio.base.fileOperate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import junit.framework.TestResult;

import org.apache.fop.fo.properties.SrcMaker;
import org.apache.fop.render.txt.TXTRenderer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public class TestFileOperate {
	@Test
	public void testGetFolderParentNumber() {
		String aa = "/home/novelibo/test";
		Assert.assertEquals(2, FileOperate.getFolderParentNumber(aa));
		
		aa = "\\/\\/\\////home\\novelibo/\\test///ter4\rdftg";
		Assert.assertEquals(4, FileOperate.getFolderParentNumber(aa));
		
		aa = "fsefsae";
		Assert.assertEquals(0, FileOperate.getFolderParentNumber(aa));
	}
	
	@Test
	public void testGetTimeModify() {
		String fileName = "src/test/resources/testResult.txt";
		File file = new File(fileName);
		Path path = FileOperate.getPath(fileName);
		Assert.assertEquals(file.lastModified(), FileOperate.getTimeLastModify(path));
	}
	
	@Test
	public void testGetFileSizeLong() {
		String fileName = "src/test/resources/testResult.txt";
		File file = new File(fileName);
		Path path = FileOperate.getPath(fileName);
		Assert.assertEquals(file.length(), FileOperate.getFileSizeLong(path));
	}
	
	Path folderParent;
	Path folder;
	@Before
	public void createFolderFile() {
		String testFileFolder = "src/test/resources/";
		String testFileSubFolderTest = testFileFolder + "test/";
		String testFileSubFolder = testFileSubFolderTest + "my/test/";
		folderParent = Paths.get(testFileSubFolderTest);
		folder = Paths.get(testFileSubFolder);


		
		Path file = Paths.get(testFileFolder + "testResult.txt");
		Path file1 = Paths.get(testFileSubFolder + "/file1.fa");
		Path file2 = Paths.get(testFileSubFolder + "/file2.sfa");
		Path file3 = Paths.get(testFileSubFolder + "/file3.txt");
		
		Path folder1 = Paths.get(testFileSubFolder+"folder1");
		Path folder2 = Paths.get(testFileSubFolder+"folder2");
		
		Path file11 = Paths.get(folder1 + "/file11.fa");
		Path file12 = Paths.get(folder1 + "/file12.sfa");
		Path file13 = Paths.get(folder1 + "/file13.txt");
		
		Path file21 = Paths.get(folder2 + "/file21.fa");
		Path file22 = Paths.get(folder2 + "/file22.sfa");
		Path file23 = Paths.get(folder2 + "/file23.txt");

		
		FileOperate.createFolders(folder1);
		FileOperate.createFolders(folder2);
		
		FileOperate.copyFile(file, file1.toString(), true);
		FileOperate.copyFile(file, file2.toString(), true);
		FileOperate.copyFile(file, file3.toString(), true);
		
		FileOperate.copyFile(file, file11.toString(), true);
		FileOperate.copyFile(file, file12.toString(), true);
		FileOperate.copyFile(file, file13.toString(), true);

		FileOperate.copyFile(file, file21.toString(), true);
		FileOperate.copyFile(file, file22.toString(), true);
		FileOperate.copyFile(file, file23.toString(), true);
	}
	@After
	public void deleteFolder() {
		FileOperate.DeleteFileFolder(folderParent);
	}
	
	@Test
	public void testGetLsFoldPath() throws IOException {
		List<Path> lsPaths = FileOperate.getLsFoldPath(folder);
		Assert.assertEquals(5, lsPaths.size());
		int folderNum = 0, fileNum = 0;
		for (Path path : lsPaths) {
			if (FileOperate.isFileExist(path)) {
				fileNum++;
			} else {
				folderNum++;
			}
        }
		Assert.assertEquals(2, folderNum);
		Assert.assertEquals(3, fileNum);
		
		lsPaths = FileOperate.getLsFoldPath(folder, "file", "*");
		Assert.assertEquals(3, lsPaths.size());
		
		lsPaths = FileOperate.getLsFoldPath(folder, "*", "fa");
		Assert.assertEquals(2, lsPaths.size());
		
		lsPaths = FileOperate.getLsFoldPath(folder, "*", "^fa");
		Assert.assertEquals(1, lsPaths.size());
		
		lsPaths = FileOperate.getLsFoldPath(folder, "*", "sfa");
		Assert.assertEquals(1, lsPaths.size());
		
		lsPaths = FileOperate.getLsFoldPath(folder, "1", "sfa");
		Assert.assertEquals(0, lsPaths.size());
		
		List<String> lsPathStr = FileOperate.getLsFoldFileName(folder.toString());
		Assert.assertEquals(5, lsPathStr.size());
	}
	
	@Test
	public void testOutputStream() {
		String fileName = "src/test/resources/testFileOperate_write_outputstream.txt";
		Path path = FileOperate.getPath(fileName);
		OutputStream os = FileOperate.getOutputStream(path, false);
		TxtReadandWrite txtWrite = new TxtReadandWrite(os);
		txtWrite.writefileln("aaaaa");
		txtWrite.writefileln("bbbbb");
		txtWrite.close();
		Assert.assertTrue(FileOperate.isFileExist(path));
		
		//覆盖写入
		os = FileOperate.getOutputStream(path, false);
		txtWrite = new TxtReadandWrite(os);
		txtWrite.writefileln("aaaaa");
		txtWrite.writefileln("bbbbb");
		txtWrite.close();
		TxtReadandWrite txtRead = new TxtReadandWrite(fileName);
		List<String> lsList = txtRead.readfileLs();
		txtRead.close();
		Assert.assertEquals(2, lsList.size());
		Assert.assertEquals("aaaaa", lsList.get(0));
		Assert.assertEquals("bbbbb", lsList.get(1));
		
		//append写入
		os = FileOperate.getOutputStream(path, true);
		txtWrite = new TxtReadandWrite(os);
		txtWrite.writefileln("aaaaa");
		txtWrite.writefileln("bbbbb");
		txtWrite.close();
		txtRead = new TxtReadandWrite(fileName);
		lsList = txtRead.readfileLs();
		txtRead.close();
		Assert.assertEquals(4, lsList.size());
		Assert.assertEquals("aaaaa", lsList.get(0));
		Assert.assertEquals("bbbbb", lsList.get(1));
		Assert.assertEquals("aaaaa", lsList.get(2));
		Assert.assertEquals("bbbbb", lsList.get(3));
		
		FileOperate.DeleteFileFolder(path);
	}
	@Test
	public void testCopyAndMoveFileFolder() {
		String folderCopy = FileOperate.changeFileSuffix(folder.toString(), "_copy", null);
		FileOperate.copyFileFolder(folder, folderCopy, true);
		List<Path> lsPaths = FileOperate.getLsFoldPath(folder);
		Assert.assertEquals(5, lsPaths.size());
		try {
			List<Path> lsPathSub = Files.walk(FileOperate.getPath(folderCopy)).collect(Collectors.toList());
			Assert.assertEquals(12, lsPathSub.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String folderMove = FileOperate.changeFileSuffix(folder.toString(), "_move", null);
		FileOperate.copyFileFolder(folderCopy, folderMove, true);
		lsPaths = FileOperate.getLsFoldPath(folder);
		Assert.assertEquals(5, lsPaths.size());
		try {
			List<Path> lsPathSub = Files.walk(FileOperate.getPath(folderMove)).collect(Collectors.toList());
			Assert.assertEquals(12, lsPathSub.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileOperate.delAllFile(folderMove);
		Assert.assertTrue(FileOperate.isFileDirectory(folderMove));
		Assert.assertEquals(0, FileOperate.getFileSizeLong(folderMove));
		
		FileOperate.DeleteFileFolder(folderMove);
		FileOperate.DeleteFileFolder(folderCopy);
		Assert.assertFalse(FileOperate.isFileFoldExist(folderCopy));
		Assert.assertFalse(FileOperate.isFileFoldExist(folderMove));

	}
	
	@Test
	public void tsetIsFileExistAndSize() {
		Assert.assertFalse(FileOperate.isFileExist(folder));
		Assert.assertTrue(FileOperate.isFileDirectory(folder));
		Assert.assertTrue(FileOperate.isFileExist(folder + "/file1.fa"));
		Assert.assertFalse(FileOperate.isFileDirectory(folder + "/file1.fa"));
		Assert.assertFalse(FileOperate.isFileExist(folder+"/fsees"));
		Assert.assertFalse(FileOperate.isFileDirectory(folder+"/fsees"));
		
		Assert.assertTrue(FileOperate.isFileExistAndBigThanSize(folder + "/file1.fa", 0));
		Assert.assertTrue(FileOperate.isFileExistAndBigThanSize(folder.toString(), 0));
		
		Assert.assertFalse(FileOperate.isFileExistAndBigThanSize(folder + "/fsees", 0));
	}
}
