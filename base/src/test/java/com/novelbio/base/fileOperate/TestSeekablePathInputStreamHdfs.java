package com.novelbio.base.fileOperate;

import hdfs.jsr203.HdfsConfInitiator;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestSeekablePathInputStreamHdfs {
	String fileName = "src/test/resources/testIOStream";
	SeekablePathInputStream seekablePathInputStream;
	FSDataInputStream fsDataInputStream;
	@Before
	public void initial() throws IllegalArgumentException, IOException {
		String fileNameNew = "/hdfs:/nbCloud/test/junittest/nbcplatform/testIOStream";
		FileOperate.copyFile(fileName, fileNameNew, true);
		fileName = fileNameNew;
		seekablePathInputStream = new SeekablePathInputStream(FileOperate.getPath(fileName));
		
		fsDataInputStream = HdfsConfInitiator.getHdfs().open(new Path("hdfs:/nbCloud/test/junittest/nbcplatform/testIOStream"));
	}
	
	@After
	public void after() throws IllegalArgumentException, IOException {
		String fileNameNew = "/hdfs:/nbCloud/test/junittest/nbcplatform/testIOStream";
		FileOperate.deleteFileFolder(fileNameNew);
		seekablePathInputStream.close();
		fsDataInputStream.close();
	}
	
	@Test
	public void testRead() throws IOException {
		byte[] bufferS = new byte[100],  bufferR = new byte[100];
		seekablePathInputStream.read(bufferS, 25, 30);
		fsDataInputStream.read(bufferR, 25, 30);
		Assert.assertArrayEquals(bufferS, bufferR);
		
		seekablePathInputStream.seek(2000);
		fsDataInputStream.seek(2000);
		Assert.assertEquals(fsDataInputStream.getPos(), seekablePathInputStream.position());
		
		bufferS = new byte[100]; bufferR = new byte[100];
		seekablePathInputStream.read(bufferS, 25, 30);
		fsDataInputStream.read(bufferR, 25, 30);
		Assert.assertArrayEquals(bufferS, bufferR);
		
		seekablePathInputStream.seek(1432);
		fsDataInputStream.seek(1432);
		Assert.assertEquals(fsDataInputStream.getPos(), seekablePathInputStream.position());
		
		bufferS = new byte[100]; bufferR = new byte[100];
		seekablePathInputStream.readFully(bufferS, 25, 30);
		fsDataInputStream.readFully(bufferR, 25, 30);
		Assert.assertArrayEquals(bufferS, bufferR);
		
		bufferS = new byte[100]; bufferR = new byte[100];
		seekablePathInputStream.readFully(bufferS);
		fsDataInputStream.readFully(bufferR);
		Assert.assertArrayEquals(bufferS, bufferR);
		
		Assert.assertEquals(seekablePathInputStream.read(), fsDataInputStream.read());
		Assert.assertEquals(seekablePathInputStream.readLine(), fsDataInputStream.readLine());
		Assert.assertEquals(fsDataInputStream.getPos(), seekablePathInputStream.position());
		Assert.assertTrue(fsDataInputStream.getPos() == seekablePathInputStream.position());

		seekablePathInputStream.seek(1332);
		fsDataInputStream.seek(1432);
		Assert.assertFalse(fsDataInputStream.getPos() == seekablePathInputStream.position());
		
		seekablePathInputStream.seek(1432);
		Assert.assertTrue(fsDataInputStream.getPos() == seekablePathInputStream.position());
	}
}
