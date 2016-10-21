package com.novelbio.base.cmd;

import org.junit.Assert;
import org.junit.Test;

public class TestCmdPathAli {
	
	@Test
	public void testConvertAli2Loc() {
		String path = "oss://bucket/mypath/file";
		String inPath = CmdPathAli.convertAli2Loc(path, true);
		Assert.assertEquals("/home/novelbio/oss/.inmap./mypath/file", inPath);
		
		path = "oss://bucket/mypath/.inmap./file";
		inPath = CmdPathAli.convertAli2Loc(path, true);
		Assert.assertEquals("/home/novelbio/oss/.inmap./mypath/.inmap./file", inPath);
		
		path = "oss://novelbiotest/nbCloud/public/nbcplatform/genome/";
		inPath = CmdPathAli.convertAli2Loc(path, true);
		System.out.println(inPath);
		Assert.assertEquals("/home/novelbio/oss/.inmap./nbCloud/public/nbcplatform/genome/", inPath);
		
		String outPath = CmdPathAli.convertAli2Loc(path, false);
		Assert.assertEquals("/home/novelbio/oss/.outmap./mypath/.inmap./file", outPath);
		
		path = "/home/novelbio/oss/.outmap./mypath/.inmap./file";
		inPath = CmdPathAli.convertAli2Loc(path, true);
		outPath = CmdPathAli.convertAli2Loc(path, false);
		Assert.assertEquals("/home/novelbio/oss/.inmap./mypath/.inmap./file", inPath);
		Assert.assertEquals(path, outPath);

		path = "/home/novelbio/oss/.inmap./mypath/.inmap./file";
		inPath = CmdPathAli.convertAli2Loc(path, true);
		outPath = CmdPathAli.convertAli2Loc(path, false);
		Assert.assertEquals("/home/novelbio/oss/.inmap./mypath/.inmap./file", inPath);
		Assert.assertEquals("/home/novelbio/oss/.outmap./mypath/.inmap./file", outPath);
	}
}
