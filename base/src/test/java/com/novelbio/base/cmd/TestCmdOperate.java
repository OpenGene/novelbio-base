package com.novelbio.base.cmd;

import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.dataStructure.ArrayOperate;

import junit.framework.TestCase;

public class TestCmdOperate extends TestCase {
	public void testCmdReal() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("samtools");
		lsCmd.add("index");
		lsCmd.add("/hdfs:/nbcloud/test.bam");
		lsCmd.add(">");
		lsCmd.add("/hdfs:/nbcloud22/test.bam");
		lsCmd.add("2>");
		lsCmd.add("/hdfs:/nbcloud22/test.log");
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		String cmd = cmdOperate.getCmdExeStrReal();
		assertEquals("samtools index /media/hdfs/nbcloud/test.bam > /media/hdfs/nbcloud22/test.bam 2> /media/hdfs/nbcloud22/test.log", cmd);
	}
	
	public void testCmdModify() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("samtools");
		lsCmd.add("index");
		lsCmd.add("/hdfs:/nbcloud/test.bam");
		lsCmd.add(">");
		lsCmd.add("/hdfs:/nbcloud22/test.bam");
		lsCmd.add("2>");
		lsCmd.add("/hdfs:/nbcloud22/test.log");
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		String cmd = cmdOperate.getCmdExeStrModify();
		assertEquals("samtools index test.bam > test.bam 2> test.log", cmd);
	}
	
	public void testCmdRun() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("samtools");
		lsCmd.add("index");
		lsCmd.add("/hdfs:/nbcloud/test.bam");
		lsCmd.add(">");
		lsCmd.add("/hdfs:/nbcloud22/test.bam");
		lsCmd.add("2>");
		lsCmd.add("/hdfs:/nbcloud22/test.log");
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		String cmd = ArrayOperate.cmbString(cmdOperate.cmdPath.getRunCmd(), " ");
		assertEquals("samtools index /media/hdfs/nbcloud/test.bam", cmd);
	}
}
