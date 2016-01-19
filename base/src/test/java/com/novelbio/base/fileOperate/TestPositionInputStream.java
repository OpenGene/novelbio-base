package com.novelbio.base.fileOperate;

import java.io.BufferedInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestPositionInputStream {
	String fileName = "src/test/resources/testIOStream";
	SeekablePathInputStream seekablePathInputStream;
	PositionInputStream InputStream;
	BufferedInputStream bfIsSeek;
	BufferedInputStream bfIsNormal;
	
	@Before
	public void initial() throws IllegalArgumentException, IOException {
		seekablePathInputStream = new SeekablePathInputStream(FileOperate.getPath(fileName));
		InputStream = new PositionInputStream(FileOperate.getInputStream(fileName));
		bfIsSeek = new BufferedInputStream(seekablePathInputStream);
		bfIsNormal = new BufferedInputStream(InputStream);
	}
	
	@Test
	public void testPosition() throws IOException {
		byte[] bts = new byte[100];
		byte[] btn = new byte[100];

		bfIsSeek.read(bts);
		bfIsNormal.read(btn);
		Assert.assertArrayEquals(bts, btn);
		Assert.assertEquals(seekablePathInputStream.position(), InputStream.getPos());
		
		bfIsNormal.mark(1000);
		bfIsSeek.mark(1000);

		bfIsSeek.read(bts);
		bfIsNormal.read(btn);
		Assert.assertArrayEquals(bts, btn);
		Assert.assertEquals(seekablePathInputStream.position(), InputStream.getPos());
		
		bfIsSeek.reset();
		bfIsNormal.reset();
		
		Assert.assertArrayEquals(bts, btn);
		
	}
}
