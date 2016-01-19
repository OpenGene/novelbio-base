package com.novelbio.base.fileOperate;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.novelbio.base.dataOperate.TxtReadandWrite;

public class TestSeekablePathOutStream {
	SeekablePathOutputStream seekablePathInputStream;
	RandomAccessFile randomAccessFile;
	byte[] buffer = new byte[100];
	
	String fileName = "src/test/resources/testTrinity.fa";

	String fileNameNewSeek = "src/test/resources/testTrinity_copySeek.fa";
	String fileNameNewRandom = "src/test/resources/testTrinity_copyRadnom.fa";
	
	@Before
	public void initial() throws FileNotFoundException {
		FileOperate.copyFile(fileName, fileNameNewSeek, true);
		FileOperate.copyFile(fileName, fileNameNewRandom, true);
		seekablePathInputStream = new SeekablePathOutputStream(FileOperate.getPath(fileNameNewSeek));
		randomAccessFile = new RandomAccessFile(new File(fileNameNewRandom), "rw");
		
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = (byte) i;
        }
	}
	
	@Before
	public void After() throws FileNotFoundException {
		String fileNameNew = "src/test/resources/testTrinity_copy.fa";
		FileOperate.DeleteFileFolder(fileNameNew);
	}
	
	@Test
	public void testRead() throws IOException {
		Assert.assertTrue(FileOperate.getFileSizeLong(fileName) == FileOperate.getFileSizeLong(fileNameNewSeek));
		Assert.assertTrue(FileOperate.getFileSizeLong(fileNameNewSeek) == FileOperate.getFileSizeLong(fileNameNewRandom));

		seekablePathInputStream.write(buffer, 25, 30);
		randomAccessFile.write(buffer, 25, 30);
		
		seekablePathInputStream.seek(2000);
		randomAccessFile.seek(2000);
		
		seekablePathInputStream.write(buffer, 0, 100);
		randomAccessFile.write(buffer, 0, 100);
		
		seekablePathInputStream.seek(FileOperate.getFileSizeLong(fileName) - 1);
		seekablePathInputStream.write(buffer, 0, 100);
		randomAccessFile.seek(FileOperate.getFileSizeLong(fileName) - 1);
		randomAccessFile.write(buffer, 0, 100);
		
		Assert.assertTrue(randomAccessFile.getFilePointer() == seekablePathInputStream.position());
		randomAccessFile.close();
		seekablePathInputStream.close();
		
		Assert.assertTrue(FileOperate.getFileSizeLong(fileNameNewSeek) == FileOperate.getFileSizeLong(fileNameNewRandom));
		Assert.assertFalse(FileOperate.getFileSizeLong(fileName) == FileOperate.getFileSizeLong(fileNameNewRandom));

		InputStream iSeek = FileOperate.getInputStream(fileNameNewSeek);
		InputStream iRandom = FileOperate.getInputStream(fileNameNewRandom);
		byte[] bSeek = new byte[(int) (FileOperate.getFileSizeLong(fileName) + 200)];
		byte[] bRandom = new byte[(int) (FileOperate.getFileSizeLong(fileName) + 200)];
		
		iSeek.read(bSeek);
		iRandom.read(bRandom);
		Assert.assertArrayEquals(bSeek, bRandom);
		iSeek.close();
		iRandom.close();
	}
	
	@After
	public void clean() {
		FileOperate.DeleteFileFolder(fileNameNewRandom);
		FileOperate.DeleteFileFolder(fileNameNewSeek);
	}

}
