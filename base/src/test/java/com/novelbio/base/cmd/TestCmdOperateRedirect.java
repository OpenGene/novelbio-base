package com.novelbio.base.cmd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class TestCmdOperateRedirect {
	
	String tmpPath = "/home/novelbio/tmp";
	
	@Test
	public void testCmdRealCopyToTmp() {
		CmdPathCluster cmdPathCluster = new CmdPathCluster();
		cmdPathCluster.putTmpOut2Out("/home/novelbio/mytest/result/test.bam", "/hdfs:/nbcloud/result/test.bam");
		cmdPathCluster.putTmpOut2Out("/home/novelbio/mytest/result2/test.bam", "/hdfs:/nbcloud22/test.bam");
 
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("samtools");
		lsCmd.add("index");
		lsCmd.add("/hdfs:/nbcloud/result/test.bam");
		lsCmd.add("/hdfs:/nbcloud/test.bam");
		lsCmd.add(">");
		lsCmd.add("/hdfs:/nbcloud22/test.bam");
		lsCmd.add("2>");
		lsCmd.add("/hdfs:/nbcloud22/test.log");
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setCmdPathCluster(cmdPathCluster);
		cmdOperate.setCmdTmpPath(tmpPath);
		cmdOperate.setRedirectInToTmp(true);
		cmdOperate.setRedirectOutToTmp(true);
		cmdOperate.addCmdParamInput("/hdfs:/nbcloud/result/test.bam");
		cmdOperate.addCmdParamInput("/hdfs:/nbcloud/test.bam");

		cmdOperate.addCmdParamOutput("/hdfs:/nbcloud22/test.bam");
		cmdOperate.addCmdParamOutput("/hdfs:/nbcloud22/test.log");
		
		cmdOperate.prepare();
		assertEquals("/home/novelbio/tmp/nbcloud22/test.bam", cmdOperate.getSaveStdOutFile());
		String cmd = cmdOperate.getCmdExeStrReal();
		
		assertEquals("samtools index /home/novelbio/mytest/result/test.bam /home/novelbio/tmp/nbcloud/test.bam > /home/novelbio/tmp/nbcloud22/test.bam 2> /home/novelbio/tmp/nbcloud22/test.log", cmd);
	}
	
	@Test
	public void testCmdCopyToTmp2() {
		String script = "/tmp/script-test.sh";
		String out = "/tmp/myresult/test/";
		
		String tmpRealyPath = tmpPath + "/myresult/test/" + "/subject/test/myfile/";
		FileOperate.deleteFileFolder(tmpPath + "/myresult/test/");
		FileOperate.deleteFileFolder(out);
		FileOperate.createFolders(tmpRealyPath);
		
		generateScript(script);
		
		CmdPathCluster cmdPathCluster = new CmdPathCluster();
		FileOperate.createFolders(FileOperate.getParentPathNameWithSep(out));
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("sh");
		lsCmd.add(script);
		lsCmd.add(out);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setCmdPathCluster(cmdPathCluster);
		cmdOperate.setCmdTmpPath(tmpPath);
		cmdOperate.setRedirectOutToTmp(true);
		cmdOperate.setRetainTmpFiles(true);
		cmdOperate.addCmdParamOutput(out);
		cmdOperate.runWithExp();
		
		assertEquals("sh /tmp/script-test.sh /home/novelbio/tmp/myresult/test/", cmdOperate.getCmdExeStrReal());
		String tmpPath = cmdPathCluster.getTmpPathAlreadyExist(out + "subject/test/myfile/test.txt");
		assertEquals("/home/novelbio/tmp/myresult/test/subject/test/myfile/", tmpPath);
		assertTrue(FileOperate.isFileExistAndBigThan0("/home/novelbio/tmp/myresult/test/subject/test/myfile/test.txt"));
		assertTrue(FileOperate.isFileExistAndBigThan0("/tmp/myresult/test/subject/test/myfile/test.txt"));
		
		lsCmd = new ArrayList<>();
		lsCmd.add("cat");
		lsCmd.add(out + "subject/test/myfile/test.txt");
		cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setCmdPathCluster(cmdPathCluster);
		cmdOperate.setRedirectInToTmp(true);
		cmdOperate.addCmdParamInput(out + "subject/test/myfile/test.txt");
		cmdOperate.prepare();
		String cmd = cmdOperate.getCmdExeStrReal();
		cmdOperate.runWithExp();
		assertEquals("cat /home/novelbio/tmp/myresult/test/subject/test/myfile/test.txt", cmd);
		cmdOperate.runWithExp();
	}
	
	public void generateScript(String script) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(script, true);
		txtWrite.writefileln("#!/bin/bash");
		txtWrite.writefileln("outpath=$1");
		txtWrite.writefileln("mkdir ${outpath}/subject/test/myfile/");
		txtWrite.writefileln("echo \"12345\t23456\t34567\" > ${outpath}/subject/test/myfile/test.txt");;
		txtWrite.close();
	}
}
