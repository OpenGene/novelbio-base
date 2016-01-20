package com.novelbio.base.fileOperate;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public class TestFileHadoop {
	@Test
	public void testConvertHdfs() {
		String file1 = "/hdfs:/nbCloud/testHadoopStreaming/chrAll.fa";
		String file2 = "/media/nbfs/nbCloud/testHadoopStreaming/chrAll.fa";
		String file3 = "hdfs:/nbCloud/testHadoopStreaming/chrAll.fa";
		String file4 = "/home/novelbio/nbCloud/testHadoopStreaming/chrAll.fa";
		Assert.assertEquals("hdfs:/nbCloud/testHadoopStreaming/chrAll.fa", FileHadoop.convertToHdfsPath(file1));
		Assert.assertEquals("hdfs:/nbCloud/testHadoopStreaming/chrAll.fa", FileHadoop.convertToHdfsPath(file2));
		Assert.assertEquals("hdfs:/nbCloud/testHadoopStreaming/chrAll.fa", FileHadoop.convertToHdfsPath(file3));
		Assert.assertEquals("/home/novelbio/nbCloud/testHadoopStreaming/chrAll.fa", FileHadoop.convertToHdfsPath(file4));

	}
	
	@Test
	public void testFileHadoop() throws IOException {
		String fileName = "/hdfs:/nbCloud/test/junittest/nbcplatform/testFileHadoop";
		FileHadoop file = new FileHadoop(fileName);
		file.mkdirs();
		Assert.assertTrue(FileOperate.isFileDirectory(file));
		FileHadoop fileTxt = new FileHadoop(file.getAbsolutePath() + "/fileTxt.txt");
		OutputStream is = FileOperate.getOutputStream(fileTxt);
		TxtReadandWrite txtWrite = new TxtReadandWrite(is);
		txtWrite.writefileln("some thing");
		txtWrite.writefileln("intresting");
		txtWrite.close();		
		FileOperate.isFileExistAndBigThan0(fileTxt.getAbsolutePath());
		TxtReadandWrite txtRead = new TxtReadandWrite(fileTxt);
		List<String> lsInfo = txtRead.readfileLs();
		Assert.assertEquals("some thing", lsInfo.get(0));
		Assert.assertEquals("intresting", lsInfo.get(1));
		txtRead.close();
		
//		fileTxt.deleteOnExit();
		Assert.assertTrue(FileOperate.isFileExist(fileTxt));
		
		System.out.println(fileTxt.delete());
		Assert.assertFalse(FileOperate.isFileExist(fileTxt));
	}
}
