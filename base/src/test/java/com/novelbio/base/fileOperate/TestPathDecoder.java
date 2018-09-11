package com.novelbio.base.fileOperate;

import org.junit.Assert;
import org.junit.Test;

public class TestPathDecoder {
	
	@Test
	public void testPath() {
		PathDecoder pathDecoder = new PathDecoder("./my/path/../to/my/name/");
		String result = pathDecoder.getResult();
		Assert.assertEquals("my/to/my/name/", result);
		
		pathDecoder = new PathDecoder("my/path/../to/my/name/");
		result = pathDecoder.getResult();
		Assert.assertEquals("my/to/my/name/", result);
		
		pathDecoder = new PathDecoder("my/path/././../../to/my/name/");
		result = pathDecoder.getResult();
		Assert.assertEquals("to/my/name/", result);
		
		pathDecoder = new PathDecoder("my/path/././../to/my/../name/");
		result = pathDecoder.getResult();
		Assert.assertEquals("my/to/name/", result);
		
		pathDecoder = new PathDecoder("/");
		result = pathDecoder.getResult();
		Assert.assertEquals("/", result);
		
		pathDecoder = new PathDecoder("./");
		result = pathDecoder.getResult();
		Assert.assertEquals("", result);
		
		
		pathDecoder = new PathDecoder("//my/path/././../to/my/..///name///");
		result = pathDecoder.getResult();
		Assert.assertEquals("/my/to/name/", result);
	}
}
