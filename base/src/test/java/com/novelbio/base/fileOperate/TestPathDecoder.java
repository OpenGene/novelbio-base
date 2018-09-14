package com.novelbio.base.fileOperate;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class TestPathDecoder {
	
	@Test
	public void testPath() {
		PathDecoder pathDecoder = new PathDecoder("./my/path/../to/my/name/");
		String result = pathDecoder.getAbsPathWithSep();
		Assert.assertEquals("my/to/my/name/", result);
		
		pathDecoder = new PathDecoder("my/path/../to/my/name/");
		result = pathDecoder.getAbsPathWithSep();
		Assert.assertEquals("my/to/my/name/", result);
		
		pathDecoder = new PathDecoder("my/path/././../../to/my/name/");
		result = pathDecoder.getAbsPathWithSep();
		Assert.assertEquals("to/my/name/", result);
		
		pathDecoder = new PathDecoder("my/path/././../to/my/../name/");
		result = pathDecoder.getAbsPathWithSep();
		Assert.assertEquals("my/to/name/", result);
		
		pathDecoder = new PathDecoder("/../../");
		result = pathDecoder.getAbsPathWithSep();
		Assert.assertEquals("/", result);
		
		pathDecoder = new PathDecoder("./");
		result = pathDecoder.getAbsPathWithSep();
		Assert.assertEquals("", result);
		
		pathDecoder = new PathDecoder("");
		result = pathDecoder.getAbsPathWithSep();
		Assert.assertEquals("", result);
		
		pathDecoder = new PathDecoder("/tmp/../../");
		result = pathDecoder.getAbsPathWithSep();
		Assert.assertEquals("/", result);
		
		pathDecoder = new PathDecoder("./tmp");
		result = pathDecoder.getAbsPathWithSep();
		Assert.assertEquals("tmp", result);
		
		pathDecoder = new PathDecoder("tmp");
		result = pathDecoder.getAbsPathWithSep();
		Assert.assertEquals("tmp", result);
		
		pathDecoder = new PathDecoder("//my/path/././../to/my/..///name///");
		result = pathDecoder.getAbsPathWithSep();
		Assert.assertEquals("/my/to/name/", result);
		
		
		pathDecoder = new PathDecoder("//my/path/././../to/my/..///name///");
		result = pathDecoder.getAbsPathWithSep();
		Assert.assertEquals("/my/to/name/", result);
	}

	@Test
	public void testHeadPath() {
		PathDecoder pathDecoder = new PathDecoder("hdfs:/../my/path/../to/my/name/");
		String result = pathDecoder.getAbsPathWithSep();
		Assert.assertEquals("hdfs:/my/to/my/name/", result);
		
		pathDecoder = new PathDecoder("hdfs://domain/../my/path/../to/my/name/");
		result = pathDecoder.getAbsPathWithSep();
		Assert.assertEquals("hdfs://domain/my/to/my/name/", result);
		
		pathDecoder = new PathDecoder("/hdfs://domain/../my/path/../to/my:/:name/");
		result = pathDecoder.getAbsPathWithSep();
		Assert.assertEquals("hdfs://domain/my/to/my:/:name/", result);
		
		pathDecoder = new PathDecoder("/hdfs://domain/../my../../..//path/../to/my/name/");
		result = pathDecoder.getAbsPathWithSep();
		Assert.assertEquals("hdfs://domain/to/my/name/", result);
		
		pathDecoder = new PathDecoder("file:///domain/../my../../..//path/../to/my/name/");
		result = pathDecoder.getAbsPathWithSep();
		Assert.assertEquals("file:///to/my/name/", result);
		
		pathDecoder = new PathDecoder("file:///domain/..");
		result = pathDecoder.getAbsPathWithSep();
		Assert.assertEquals("file:///", result);
		
		pathDecoder = new PathDecoder("file:///");
		result = pathDecoder.getAbsPathWithSep();
		Assert.assertEquals("file:///", result);
	}
	
	@Test
	public void testGetName() throws IOException {
		PathDecoder pathDecoder = new PathDecoder("/../my/path/../to/my/name/");
		File file = new File("/../my/path/../to/my/name/");
		System.out.println(file);
		String result = pathDecoder.getName();
		Assert.assertEquals(file.getName(), result);
	}
	
	@Test
	public void testCanonical() throws IOException {
		Assert.assertEquals(true, PathDecoder.isAbsPath("hdfs:/"));
		Assert.assertEquals(true, PathDecoder.isAbsPath("/my/.././"));
		
		PathDecoder pathDecoder = new PathDecoder("/../my/path/../to/my/name/");
		File file = new File("/../my/path/../to/my/name/");
		String result = pathDecoder.getAbsPathWithoutEndSep();
		Assert.assertEquals(file.getCanonicalPath(), result);
	}
	
	
	@Test
	public void testGetParentName() throws IOException {
		String path = "/path/../../../to/my/name/fff";
		PathDecoder pathDecoder = new PathDecoder(path);
		Assert.assertEquals("/to/my/name/", pathDecoder.getParentPathNameWithSep());
		
		path = "/";
		pathDecoder = new PathDecoder(path);
		Assert.assertEquals("/", pathDecoder.getParentPathNameWithSep());
		
		path = "hdfs:/";
		pathDecoder = new PathDecoder(path);
		Assert.assertEquals("hdfs:/", pathDecoder.getParentPathNameWithSep());
		
		
		path = "hdfs://domain/ss";
		pathDecoder = new PathDecoder(path);
		Assert.assertEquals("hdfs://domain/", pathDecoder.getParentPathNameWithSep());
	}
	
	@Test
	public void testGetPathWithSep() throws IOException {
		String path = "/path/../../../to/my/name/fff";
		PathDecoder pathDecoder = new PathDecoder(path);
		Assert.assertEquals("/to/my/name/", pathDecoder.getPathWithSep());
		
		path = "/";
		pathDecoder = new PathDecoder(path);
		Assert.assertEquals("/", pathDecoder.getPathWithSep());
		
		path = "hdfs:/myhome/result/test";
		pathDecoder = new PathDecoder(path);
		Assert.assertEquals("hdfs:/myhome/result/", pathDecoder.getPathWithSep());
		
	}
}
