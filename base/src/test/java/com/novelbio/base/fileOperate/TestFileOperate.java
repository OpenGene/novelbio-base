package com.novelbio.base.fileOperate;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.jsr203.bos.OssFileSystem;
import com.novelbio.jsr203.bos.OssFileSystemProvider;
import com.novelbio.jsr203.bos.OssPath;

//该脚本执行,需本地的hosts和hadoop环境
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
		
		path = FileOperate.getPath("/abc/123/nofile.txt");
		Assert.assertEquals(-1, FileOperate.getFileSizeLong(path));
	}
	
	static Path folderParent = FileOperate.getPath("/hdfs:/nbCloud/test/junittest/nbcplatform/testFileOperate/");
	static Path folder;
	static String hdfsFileSubFolderTest = "/hdfs:/nbCloud/test/junittest/nbcplatform/testFileOperate/";
	static String hdfsFileSubFolderTestWithoutFirstSep = "hdfs:/nbCloud/test/junittest/nbcplatform/testFileOperate/";

	@BeforeClass
	public static void createFolderFile() {
		String testFileFolder = "src/test/resources/";
		
		String testFileSubFolder = FileOperate.removeSplashHead(hdfsFileSubFolderTest, false) + "my/test/";
		folder = FileOperate.getPath(testFileSubFolder);

		Path file = FileOperate.getPath(testFileFolder + "testResult.txt");
		Path file1 = FileOperate.getPath(testFileSubFolder + "/file1.fa");
		Path file2 = FileOperate.getPath(testFileSubFolder + "/file2.sfa");
		Path file3 = FileOperate.getPath(testFileSubFolder + "/file3.txt");
		
		Path folder1 = FileOperate.getPath(testFileSubFolder+"folder1");
		Path folder2 = FileOperate.getPath(testFileSubFolder+"folder2");
		
		Path file11 = FileOperate.getPath(folder1 + "/file11.fa");
		Path file12 = FileOperate.getPath(folder1 + "/file12.sfa");
		Path file13 = FileOperate.getPath(folder1 + "/file13.txt");
		
		Path file21 = FileOperate.getPath(folder2 + "/file21.fa");
		Path file22 = FileOperate.getPath(folder2 + "/file22.sfa");
		Path file23 = FileOperate.getPath(folder2 + "/file23.txt");

		
		FileOperate.createFolders(folder1);
		FileOperate.createFolders(folder2);
		
		FileOperate.copyFile(file, FileOperate.getAbsolutePath(file1), true);
		FileOperate.copyFile(file, file2, true);
		FileOperate.copyFile(file, file3, true);
		
		FileOperate.copyFile(file, file11, true);
		FileOperate.copyFile(file, file12, true);
		FileOperate.copyFile(file, file13, true);

		FileOperate.copyFile(file, file21, true);
		FileOperate.copyFile(file, file22, true);
		FileOperate.copyFile(file, file23, true);
	}
	
	@Test
	public void testGetLsFoldPath() throws IOException {
		List<Path> lsPaths = FileOperate.getLsFoldPath(folder);
		Assert.assertEquals(5, lsPaths.size());
		int folderNum = 0, fileNum = 0;
		for (Path path : lsPaths) {
			if (FileOperate.isFileExistAndNotDir(path)) {
				fileNum++;
			} else {
				folderNum++;
			}
        }
		Assert.assertEquals(2, folderNum);
		Assert.assertEquals(3, fileNum);
		
		lsPaths = FileOperate.getLsFoldPath(folder, "file", "*");
		Assert.assertEquals(3, lsPaths.size());
		for (Path path : lsPaths) {
			Assert.assertTrue(FileOperate.getAbsolutePath(path).startsWith(hdfsFileSubFolderTestWithoutFirstSep));
        }
		
		List<String> lsFileName = FileOperate.getLsFoldFileName(folder, "file", "*");
		for (String path : lsFileName) {
			Assert.assertTrue(path.startsWith(hdfsFileSubFolderTestWithoutFirstSep));
			Assert.assertTrue(FileOperate.getAbsolutePath(path).startsWith(hdfsFileSubFolderTestWithoutFirstSep));
        }
		
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
	
	@AfterClass
	public static void deleteFolder() {
		FileOperate.deleteFileFolder(folderParent);
	}
	
	@Test
	public void testOutputStream() throws IOException {
		String fileName = "src/test/resources/testFileOperate_write_outputstream.txt";
		Path path = FileOperate.getPath(fileName);
		OutputStream os = FileOperate.getOutputStream(path, false);
		TxtReadandWrite txtWrite = new TxtReadandWrite(os);
		txtWrite.writefileln("aaaaa");
		txtWrite.writefileln("bbbbb");
		txtWrite.close();
		Assert.assertTrue(FileOperate.isFileExistAndNotDir(path));
		
		//覆盖写入
		os = FileOperate.getOutputStream(path, false);
		txtWrite = new TxtReadandWrite(os);
		txtWrite.writefileln("aaaaa");
		txtWrite.writefileln("bbbbb");
		txtWrite.close();
		List<String> lsList = TxtReadandWrite.readfileLs(fileName);
		Assert.assertEquals(2, lsList.size());
		Assert.assertEquals("aaaaa", lsList.get(0));
		Assert.assertEquals("bbbbb", lsList.get(1));
		
		//append写入
		os = FileOperate.getOutputStream(path, true);
		txtWrite = new TxtReadandWrite(os);
		txtWrite.writefileln("aaaaa");
		txtWrite.writefileln("bbbbb");
		txtWrite.close();
		lsList = TxtReadandWrite.readfileLs(fileName);
		Assert.assertEquals(4, lsList.size());
		Assert.assertEquals("aaaaa", lsList.get(0));
		Assert.assertEquals("bbbbb", lsList.get(1));
		Assert.assertEquals("aaaaa", lsList.get(2));
		Assert.assertEquals("bbbbb", lsList.get(3));
		
		FileOperate.deleteFileFolder(path);
	}
	@Test
	public void testCopyAndMoveFileFolder() {
		String folderCopy = folderParent + "/copy";
		
		FileOperate.copyFileFolder(folder, folderCopy, true);
		List<Path> lsPaths = FileOperate.getLsFoldPath(folder);
		Assert.assertEquals(5, lsPaths.size());
		try {
			List<Path> lsPathSub = Files.walk(FileOperate.getPath(folderCopy)).collect(Collectors.toList());
			Assert.assertEquals(12, lsPathSub.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String folderMove = FileOperate.changeFileSuffix(folderParent.toString(), "move", null);
		FileOperate.moveFile(true, folderCopy, folderMove);
		lsPaths = FileOperate.getLsFoldPath(folder);
		Assert.assertEquals(5, lsPaths.size());
		Assert.assertFalse(FileOperate.isFileExistAndNotDir(folderCopy));
		try {
			List<Path> lsPathSub = Files.walk(FileOperate.getPath(folderMove)).collect(Collectors.toList());
			Assert.assertEquals(12, lsPathSub.size());
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileOperate.deleteFolderClean(folderMove);
		Assert.assertTrue(FileOperate.isFileDirectory(folderMove));
		Assert.assertEquals(0, FileOperate.getFileSizeLong(folderMove));
		
		FileOperate.deleteFileFolder(folderMove);
		FileOperate.deleteFileFolder(folderCopy);
		Assert.assertFalse(FileOperate.isFileExist(folderCopy));
		Assert.assertFalse(FileOperate.isFileExist(folderMove));

	}
	
	@Test
	public void tsetIsFileExistAndSize() {
		Assert.assertFalse(FileOperate.isFileExistAndNotDir(folder));
		Assert.assertTrue(FileOperate.isFileDirectory(folder));
		Assert.assertTrue(FileOperate.isFileExistAndNotDir(folder + "/file1.fa"));
		Assert.assertFalse(FileOperate.isFileDirectory(folder + "/file1.fa"));
		Assert.assertFalse(FileOperate.isFileExistAndNotDir(folder+"/fsees"));
		Assert.assertFalse(FileOperate.isFileDirectory(folder+"/fsees"));
		
		Assert.assertTrue(FileOperate.isFileExistAndBigThanSize(folder + "/file1.fa", 0));
		Assert.assertTrue(FileOperate.isFileExistAndBigThanSize(folder.toString(), 0));
		
		Assert.assertFalse(FileOperate.isFileExistAndBigThanSize(folder + "/fsees", 0));
		
		
		String path = "hdfs:/apps/simple/appYarn.zip";
		Assert.assertTrue(FileOperate.isFileExistAndNotDir(path));
	}
	
	@Test
	public void testGetLsFile() {
		List<Path> lsPath = FileOperate.getLsFoldPathRecur(FileOperate.getPath("src/test"), "*", "*", false);
		List<String> setFile = new ArrayList<>();
		for (Path path : lsPath) {
			setFile.add(path.toString());
		}
		Collection<File> lsFiles = FileUtils.listFiles(new File("src/test"), null, true);
		List<String> setFileExp = new ArrayList<>();
		for (File path : lsFiles) {
			setFileExp.add(path.getPath());
		}
		Collections.sort(setFile); Collections.sort(setFileExp);
		Assert.assertEquals(setFile, setFileExp);
		
		
		lsPath = FileOperate.getLsFoldPathRecur(FileOperate.getPath("src/test"), "*", "*", true);
		setFile = new ArrayList<>();
		for (Path path : lsPath) {
			setFile.add(path.toString());
		}
	}
	
	@Test
	public void testIsFilePathSame() {
		String name1 = "hdfs:/abc/def/ghi";
		String name2 = "/hdfs:/abc/def/ghi";
		Assert.assertEquals(true, FileOperate.isFilePathSame(name1, name2));
		
		name1 = "hdfs:/abc/def/../ghi";
		name2 = "/hdfs:/abc/ghi";
		Assert.assertEquals(true, FileOperate.isFilePathSame(name1, name2));

		name1 = "hdfs:/abc/../abc/./def/../../abc/def/ghi";
		name2 = "/hdfs:/abc/def/../def/ghi";
		Assert.assertEquals(true, FileOperate.isFilePathSame(name1, name2));

	}
	
	@Test
	public void testGetFileSuffix(){
		String file = "/abc/def/321.xls";
		String suffix = FileOperate.getFileSuffix(file);
		Assert.assertEquals("xls", suffix);
	}
	
	@Test
	public void testGetParentPathNameWithSep() {
		String path = "hdfs:/abs/sfe/se";
		String result = FileOperate.getParentPathNameWithSep(path);
//		for (int i = 0; i < 10; i++) {
//			result = FileOperate.getParentPathNameWithSep(result);
//		}
//		Assert.assertEquals("/hdfs:/", result);
		
		path = "//hdfs:/abs/sfe/se";
		result = FileOperate.getParentPathNameWithSep(path);
		for (int i = 0; i < 10; i++) {
			result = FileOperate.getParentPathNameWithSep(result);
		}
		Assert.assertEquals("/hdfs:/", result);
		
		path = "\\hdfs:\\abs\\sfe\\se";
		result = FileOperate.getParentPathNameWithSep(path);
		for (int i = 0; i < 10; i++) {
			result = FileOperate.getParentPathNameWithSep(result);
		}
		Assert.assertEquals("\\hdfs:\\", result);
		
		path = "//hdfs://abs//sfe//se";
		result = FileOperate.getParentPathNameWithSep(path);
		for (int i = 0; i < 10; i++) {
			result = FileOperate.getParentPathNameWithSep(result);
		}
		Assert.assertEquals("/hdfs:/", result);
		
		path = "hdfs://abs//sfe//se";
		result = FileOperate.getParentPathNameWithSep(path);
		for (int i = 0; i < 10; i++) {
			result = FileOperate.getParentPathNameWithSep(result);
		}
		Assert.assertEquals("hdfs:/", result);
		
		path = "hdfs:\\abs\\sfe\\se";
		result = FileOperate.getParentPathNameWithSep(path);
		for (int i = 0; i < 10; i++) {
			result = FileOperate.getParentPathNameWithSep(result);
		}
		Assert.assertEquals("hdfs:\\", result);
		
		path = "oss:/nbCloud/public/rawData/A__2016-09/project_57ea175c45ce95f1d60f8af5/small.txt";
		result = FileOperate.getParentPathNameWithSep(path);
		Assert.assertEquals("oss:/nbCloud/public/rawData/A__2016-09/project_57ea175c45ce95f1d60f8af5/", result);
		for (int i = 0; i < 10; i++) {
			result = FileOperate.getParentPathNameWithSep(result);
		}
		System.out.println(result);
		Assert.assertEquals("oss:/", result);
	}
	
//	@Test
	public void testGetParentPathNameWithSep2() {
		String path = "oss://novelbio/nbCloud/public/rawData/A__2016-09/project_57ea175c45ce95f1d60f8af5/small.txt";
		String result = FileOperate.getParentPathNameWithSep(path);
		System.out.println(result);
		Assert.assertEquals("oss://novelbio/nbCloud/public/rawData/A__2016-09/project_57ea175c45ce95f1d60f8af5/", result);
		for (int i = 0; i < 10; i++) {
			result = FileOperate.getParentPathNameWithSep(result);
		}
		Assert.assertEquals("oss://novelbio/", result);
		
	}

	@Test
	public void testDelFile() {
		FileOperate.deleteFileFolder("/home/novelbio/abc.def");
	}
	
	@Test
	public void testGetFileName() {
		String fileName = FileOperate.getFileName("/hdfs://cluster1AllProject/@2016-tmp/.Other@@DS:RawDataTask_result/@L@/nbcfile:/57e08ffe60b2003b1682cf1d");
		System.out.println(fileName);
	}
	
	@Test
	public void testMoveFolder() {
		FileOperate.deleteFileFolder("/hdfs:/nbCloud/public/software/MoveFolder");
		FileOperate.createFolders("/hdfs:/nbCloud/public/software/MoveFolder");
		
		FileOperate.moveFolder("/home/novelbio/git/snakerflow/snaker-nutz", "/hdfs:/nbCloud/public/software/MoveFolder", "", true, true);
		
		Assert.assertTrue(FileOperate.isFileExist("/hdfs:/nbCloud/public/software/MoveFolder/pom.xml"));
		Assert.assertTrue(FileOperate.isFileExist("/hdfs:/nbCloud/public/software/MoveFolder/src/test/resources/log4j.properties"));
		Assert.assertFalse(FileOperate.isFileExist("/home/novelbio/git/snakerflow/snaker-nutz"));
		
		FileOperate.createFolders("/home/novelbio/git/snakerflow/snaker-nutz");
		FileOperate.moveFolder("/hdfs:/nbCloud/public/software/MoveFolder", "/home/novelbio/git/snakerflow/snaker-nutz", "", true, true);
		
		Assert.assertFalse(FileOperate.isFileExist("/hdfs:/nbCloud/public/software/MoveFolder"));
		Assert.assertTrue(FileOperate.isFileExist("/home/novelbio/git/snakerflow/snaker-nutz/pom.xml"));
		Assert.assertTrue(FileOperate.isFileExist("/home/novelbio/git/snakerflow/snaker-nutz/src/test/resources/log4j.properties"));
	}
	
	public static void main(String[] args) {
		
		String pathStr1 = "nbCloud/public/AllProject/A__2016-12/project_58468c2c0cf23ee9307e8ea5/task_585798b444f45b7130562eb5/QualityControl_result/QCResults/";
		String pathStr2 = "oss://novelbrain/" + pathStr1;
		Path path = FileOperate.getPath(pathStr2);
		
		
//		List<Path> lsPaths = FileOperate.getLsFoldPathRecur(path, "*", "*", false);
		
		System.out.println("oss start...");
		long time11 = System.currentTimeMillis();
		try {
			for (int i = 0; i < 10; i++) {
				OssFileSystemProvider ossFileSystemProvider =	new OssFileSystemProvider();
				URI uri = new URI(pathStr2);
				Path ossPath = ossFileSystemProvider.getPath(uri);
				OssFileSystem ossFileSystem = new OssFileSystem(ossFileSystemProvider, uri);
				Iterator<Path>  paths = ossFileSystem.iteratorOf((OssPath) ossPath);
				List<Path> lsPaths  = new ArrayList<>();
				while (paths.hasNext()) {
					Path path2 = (Path) paths.next();
					lsPaths.add(path2);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		long time12 = System.currentTimeMillis();
		System.out.println("oss time=" + (time12 - time11));
		
		System.out.println("base start...");
		long time21 = System.currentTimeMillis();
		for (int i = 0; i < 10; i++) {
			System.out.println(i);
			List<Path> lsPaths = FileOperate.getLsFoldPathRecur(path, "*", "*", false);
		}
		long time22 = System.currentTimeMillis();
		System.out.println("base time=" + (time22 - time21));
	}
}
