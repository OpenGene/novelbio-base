package com.novelbio.base.fileOperate;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestSeekablePathInputStream {
	String fileName = "src/test/resources/testIOStream";
	SeekablePathInputStream seekablePathInputStream;
	RandomAccessFile randomAccessFile;
	BufferedInputStream is;
	@Before
	public void initial() throws FileNotFoundException {
		String fileNameNew = "src/test/resources/testIOStream_copy";
		FileOperate.copyFile(fileName, fileNameNew, true);
		fileName = fileNameNew;
		seekablePathInputStream = new SeekablePathInputStream(FileOperate.getPath(fileName));
		randomAccessFile = new RandomAccessFile(new File(fileName), "r");
		is = new BufferedInputStream(new FileInputStream(fileName));
	}
	
	@Before
	public void After() throws FileNotFoundException {
		String fileNameNew = "src/test/resources/testIOStream_copy";
		FileOperate.DeleteFileFolder(fileNameNew);
	}
	
	@Test
	public void testRead() throws IOException {
		byte[] bufferS = new byte[100],  bufferR = new byte[100];
		seekablePathInputStream.read(bufferS, 25, 30);
		randomAccessFile.read(bufferR, 25, 30);
		Assert.assertArrayEquals(bufferS, bufferR);
		
		seekablePathInputStream.seek(2000);
		randomAccessFile.seek(2000);
		Assert.assertEquals(randomAccessFile.getFilePointer(), seekablePathInputStream.position());
		
		bufferS = new byte[100]; bufferR = new byte[100];
		seekablePathInputStream.read(bufferS, 25, 30);
		randomAccessFile.read(bufferR, 25, 30);
		Assert.assertArrayEquals(bufferS, bufferR);
		
		seekablePathInputStream.seek(432);
		randomAccessFile.seek(432);
		Assert.assertEquals(randomAccessFile.getFilePointer(), seekablePathInputStream.position());
		
		for (int i = 0; i < 1000; i++) {
			Assert.assertEquals(seekablePathInputStream.read(), randomAccessFile.read());
        }
		
		bufferS = new byte[100]; bufferR = new byte[100];
		seekablePathInputStream.readFully(bufferS, 25, 30);
		randomAccessFile.readFully(bufferR, 25, 30);
		Assert.assertArrayEquals(bufferS, bufferR);
		
		bufferS = new byte[100]; bufferR = new byte[100];
		seekablePathInputStream.readFully(bufferS);
		randomAccessFile.readFully(bufferR);
		Assert.assertArrayEquals(bufferS, bufferR);
		
		
		Assert.assertEquals(seekablePathInputStream.read(), randomAccessFile.read());
		Assert.assertEquals(seekablePathInputStream.readLine(), randomAccessFile.readLine());
		Assert.assertEquals(randomAccessFile.getFilePointer(), seekablePathInputStream.position());
		Assert.assertTrue(randomAccessFile.getFilePointer() == seekablePathInputStream.position());

		seekablePathInputStream.seek(1332);
		randomAccessFile.seek(1432);
		Assert.assertFalse(randomAccessFile.getFilePointer() == seekablePathInputStream.position());
		
		seekablePathInputStream.seek(1432);
		Assert.assertTrue(randomAccessFile.getFilePointer() == seekablePathInputStream.position());
	}
	
	@Test
	public void testOtherMethod() throws IOException {
		seekablePathInputStream.seek(2456);
		is.skip(2456);
		
		byte[] bufferS = new byte[350]; byte[] bufferR = new byte[350];
		seekablePathInputStream.read(bufferS, 25, 300);
		is.read(bufferR, 25, 300);
		Assert.assertArrayEquals(bufferS, bufferR);
		
		seekablePathInputStream.mark(1500);
		is.mark(1500);
		
		bufferS = new byte[500]; bufferR = new byte[500];
		seekablePathInputStream.read(bufferS, 25, 300);
		is.read(bufferR, 25, 300);
		Assert.assertArrayEquals(bufferS, bufferR);
		
		bufferS = new byte[500]; bufferR = new byte[500];
		seekablePathInputStream.read(bufferS, 25, 300);
		is.read(bufferR, 25, 300);
		Assert.assertArrayEquals(bufferS, bufferR);
		
	}
}
