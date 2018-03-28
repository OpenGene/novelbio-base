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
		CmdPathCluster cmdPathCluster = new CmdPathCluster(false);
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
		
		cmdOperate.prepareAndMoveFileIn();
		assertEquals("/home/novelbio/tmp/nbcloud22/test.bam", cmdOperate.getSaveStdOutFile());
		String cmd = cmdOperate.getCmdExeStrReal();
		
		assertEquals("samtools index /home/novelbio/mytest/result/test.bam /home/novelbio/tmp/nbcloud/test.bam "
				+ "> /home/novelbio/tmp/nbcloud22/test.bam 2> /home/novelbio/tmp/nbcloud22/test.log", cmd);
	}
	
	
	/**
	 * 测试如下功能
	 * task1 输入参数为 sh /tmp/script-test.sh /tmp/myresult/test/
	 * 会生成临时文件 /home/novelbio/tmp/myresult/test/subject/test/myfile/test.txt
	 * 获得结果文件 /tmp/myresult/test/subject/test/myfile/test.txt
	 * 
	 * 并且  /home/novelbio/tmp/myresult/ 里面已经存在的文件不会被拷贝出来
	 * 
	 * task2 输入上一个结果文件 /tmp/myresult/test/subject/test/myfile/test.txt
	 * 会拷贝为临时文件 /home/novelbio/tmp/myresult/test/subject/test/myfile/test.txt
	 * 
	 * 主要测试 当task2调用task1的输出文件时，要求把上一个输出的文件拷贝到其生成的临时文件夹的相同路径。
	 * 也就是把  /tmp/myresult/test/subject/test/myfile/test.txt 拷贝到 /home/novelbio/tmp/myresult/test/subject/test/myfile/test.txt
	 * 
	 * 但是如果  /home/novelbio/tmp/myresult/test/subject/test/myfile/ 下面有其他文件，他们不会被拷贝出来
	 */
	@Test
	public void testCmdCopyToTmp2() {
		String script = "/tmp/script-test.sh";
		generateScript(script);

		String out1 = "/tmp/myresult1/mytmp/test/";
		String out2 = "/tmp/myresult2/mytmp/test/";

		String tmpRealPath1 = tmpPath + "/mytmp/test/subject/test/myfile/";
		String tmpRealPath2 = tmpPath + "/mytmp1/test/subject/test/myfile/";
		String resultPath1 = out1 + "subject/test/myfile/";
		String resultPath2 = out2 + "subject/test/myfile/";

		FileOperate.deleteFileFolder(tmpPath + "/myresult/test/");
		FileOperate.deleteFileFolder(out1);
		FileOperate.deleteFileFolder(out2);
		FileOperate.createFolders(tmpRealPath1);
		FileOperate.createFolders(tmpRealPath2);

		FileOperate.copyFile(script, tmpRealPath1 + "file1", true);
		FileOperate.copyFile(script, FileOperate.getParentPathNameWithSep(tmpRealPath1) + "file2", true);
		FileOperate.copyFile(script, tmpRealPath2 + "file3", true);

		CmdPathCluster cmdPathCluster = new CmdPathCluster(false);
		FileOperate.createFolders(FileOperate.getParentPathNameWithSep(out1));
		FileOperate.createFolders(FileOperate.getParentPathNameWithSep(out2));
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("sh");
		lsCmd.add(script);
		lsCmd.add(out1);
		lsCmd.add(out2);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setCmdPathCluster(cmdPathCluster);
		cmdOperate.setCmdTmpPath(tmpPath);
		cmdOperate.setRedirectOutToTmp(true);
		cmdOperate.setRetainTmpFiles(true);
		cmdOperate.addCmdParamOutput(out1);
		cmdOperate.addCmdParamOutput(out2);

		cmdOperate.runWithExp();
		
		assertEquals("sh /tmp/script-test.sh /home/novelbio/tmp/mytmp/test/ /home/novelbio/tmp/mytmp1/test/", cmdOperate.getCmdExeStrReal());
		String tmpPath1 = cmdPathCluster.getTmpPathAlreadyExist(out1 + "subject/test/myfile/mytest/test.txt");
		assertEquals("/home/novelbio/tmp/mytmp/test/subject/test/myfile/mytest/test.txt", tmpPath1);
		String tmpPath2 = cmdPathCluster.getTmpPathAlreadyExist(out2 + "subject/test/myfile/mytest/tmp/");
		assertEquals("/home/novelbio/tmp/mytmp1/test/subject/test/myfile/mytest/tmp/", tmpPath2);
		String tmpPath3 = cmdPathCluster.getTmpPathAlreadyExist(out2 + "subject/test/myfile/");
		assertEquals("/home/novelbio/tmp/mytmp1/test/subject/test/myfile/", tmpPath3);
		
		assertTrue(FileOperate.isFileExistAndBigThan0(tmpRealPath1+ "test1.txt"));
		assertTrue(FileOperate.isFileExistAndBigThan0(tmpRealPath2 + "test2.txt"));
		assertTrue(FileOperate.isFileExistAndBigThan0(resultPath1 + "test1.txt"));
		assertTrue(FileOperate.isFileExistAndBigThan0(resultPath2 + "test2.txt"));
		
		assertEquals(2, cmdPathCluster.mapOutPath2TmpOutPath.size());
		//之前存在的文件不会被拷贝出来
		Assert.assertFalse(FileOperate.isFileExistAndBigThan0(resultPath1 + "file1"));
		Assert.assertFalse(FileOperate.isFileExistAndBigThan0(FileOperate.getParentPathNameWithSep(resultPath1) + "file2"));
		Assert.assertFalse(FileOperate.isFileExistAndBigThan0(resultPath2 + "file3"));

		lsCmd = new ArrayList<>();
		lsCmd.add("cat");
		lsCmd.add(resultPath1 + "test1.txt");
		lsCmd.add(resultPath2 + "test2.txt");
		
		cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setCmdPathCluster(cmdPathCluster);
		cmdOperate.setRedirectInToTmp(true);
		cmdOperate.addCmdParamInput(resultPath1 + "test1.txt");
		cmdOperate.addCmdParamInput(resultPath2 + "test2.txt");
		cmdOperate.prepareAndMoveFileIn();
		String cmd = cmdOperate.getCmdExeStrReal();
		assertEquals("cat " + tmpRealPath1 +  "test1.txt " + tmpRealPath2 +  "test2.txt", cmd);
	}
	
	public void generateScript(String script) {
		TxtReadandWrite txtWrite = new TxtReadandWrite(script, true);
		txtWrite.writefileln("#!/bin/bash");
		txtWrite.writefileln("outpath1=$1");
		txtWrite.writefileln("outpath2=$2");
		txtWrite.writefileln("mkdir ${outpath1}/subject/test/myfile/");
		txtWrite.writefileln("echo \"12345\t23456\t34567\" > ${outpath1}/subject/test/myfile/test1.txt");;
		txtWrite.writefileln("echo \"1234\t2345\t3456\" > ${outpath2}/subject/test/myfile/test2.txt");;
		txtWrite.close();
	}
}
