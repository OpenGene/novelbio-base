package com.novelbio.base.fileOperate;

import org.junit.Assert;
import org.junit.Test;

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
}
