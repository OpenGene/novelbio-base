package com.novelbio.base.cmd;

import org.junit.Assert;
import org.junit.Test;

public class TestCmdPathAli {
	
	@Test
	public void testConvertAli2Loc() {
		String path = "oss://bucket/mypath/file";
		String inPath = CmdOrderGeneratorAli.convertAli2Loc(path, true);
		Assert.assertEquals("/home/novelbio/oss/.inmap./mypath/file", inPath);
		
		path = "oss://bucket/mypath/.inmap./file";
		inPath = CmdOrderGeneratorAli.convertAli2Loc(path, true);
		Assert.assertEquals("/home/novelbio/oss/.inmap./mypath/.inmap./file", inPath);
		
		path = "oss://novelbiotest/nbCloud/public/nbcplatform/genome/";
		inPath = CmdOrderGeneratorAli.convertAli2Loc(path, true);
		System.out.println(inPath);
		Assert.assertEquals("/home/novelbio/oss/.inmap./nbCloud/public/nbcplatform/genome/", inPath);
		
		path = "oss://bucket/mypath/.inmap./file";
		String outPath = CmdOrderGeneratorAli.convertAli2Loc(path, false);
		Assert.assertEquals("/home/novelbio/oss/.outmap./mypath/.inmap./file", outPath);
		
		path = "/home/novelbio/oss/.outmap./mypath/.inmap./file";
		inPath = CmdOrderGeneratorAli.convertAli2Loc(path, true);
		outPath = CmdOrderGeneratorAli.convertAli2Loc(path, false);
		Assert.assertEquals("/home/novelbio/oss/.inmap./mypath/.inmap./file", inPath);
		Assert.assertEquals("/home/novelbio/oss/.outmap./mypath/.inmap./file", outPath);
		Assert.assertEquals(path, outPath);

		path = "/home/novelbio/oss/.inmap./mypath/.inmap./file";
		inPath = CmdOrderGeneratorAli.convertAli2Loc(path, true);
		outPath = CmdOrderGeneratorAli.convertAli2Loc(path, false);
		Assert.assertEquals("/home/novelbio/oss/.inmap./mypath/.inmap./file", inPath);
		Assert.assertEquals("/home/novelbio/oss/.outmap./mypath/.inmap./file", outPath);
	}
}
