package com.novelbio.base.cmd;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.novelbio.base.cmd.ConvertCmd.ConvertCmdGetFileName;
import com.novelbio.base.fileOperate.FileHadoop;

public class TestConvertCmd {
	
	@Test
	public void cmdConvertHdfs() {
		CmdOrderGenerator cmdPath = new CmdOrderGenerator();
		CmdMoveFile cmdMoveFile = CmdMoveFile.getInstance(true);
		List<String> lsCmd = new ArrayList<>();
		String inFile1 = "hdfs:/src/test/resources/testTrinity.fa";
		String inFile2 = "hdfs:/src/test/resources/testTrinity2.fa";
		String outFile = "/hdfs:/nbCloud/test/testCode/testCmdpath.out";
		lsCmd.add("bwa-index");
		lsCmd.add("--inPath=" + inFile1 + "," + inFile2);
		lsCmd.add("--outPath=" + outFile);
		cmdPath.setLsCmd(lsCmd);
		
		String[] ss = cmdPath.getRunCmd(cmdMoveFile);
		String inFileLocal1 = FileHadoop.convertToLocalPath(inFile1);
		String inFileLocal2 = FileHadoop.convertToLocalPath(inFile2);
		String outLocal = FileHadoop.convertToLocalPath(outFile);
		assertEquals("--inPath="+ inFileLocal1 + "," + inFileLocal2, ss[1]);
		assertEquals("--outPath="+ outLocal, ss[2]);
	}
	
	@Test
	public void cmdConvertGetFileName() {
		String cmd = "/home/novelbio/software/trinityrnaseq-2.1.1/util/support_scripts/../../Trinity --single \"/home/novelbio/tmp/2015-11-28-10-50-43-3610_RNAassembly_result2/Hap-1trinity/read_partitions/Fb_0/CBin_59/c5929.trinity.reads.fa\" --output \"/home/novelbio/tmp/2015-11-28-10-50-43-3610_RNAassembly_result2/Hap-1trinity/read_partitions/Fb_0/CBin_59/c5929.trinity.reads.fa.out\" --CPU 1";
		ConvertCmd.ConvertCmdGetFileName convertCmdGetFileName = new ConvertCmdGetFileName();
		String result = convertCmdGetFileName.convertCmd(cmd);
		assertEquals("Trinity --single \"c5929.trinity.reads.fa\" --output \"c5929.trinity.reads.fa.out\" --CPU 1", result);
	}
	
	@Test
	public void cmdConvertGetFileName2() {
		String cmd = "'/home/novelbio/../../Trinity' --single \"name=/home/novelbio/c5929.fa\" --output 'name=/home/novelbio/c5929.out' single='mm=\"/home/novelbio/test\"' --CPU 1";
		ConvertCmdGetFileName convertCmdGetFileName = new ConvertCmdGetFileName();
		String result = convertCmdGetFileName.convertCmd(cmd);
		assertEquals("'Trinity' --single \"name=c5929.fa\" --output 'name=c5929.out' single='mm=\"test\"' --CPU 1", result);
	}
}
