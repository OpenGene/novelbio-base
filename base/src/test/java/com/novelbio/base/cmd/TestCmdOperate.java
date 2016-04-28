package com.novelbio.base.cmd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.novelbio.base.PathDetail;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class TestCmdOperate {

	SoftAssertions soft = new SoftAssertions();

	@Test
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
		assertEquals("samtools index " + PathDetail.getHdfsLocalPath() + "/nbcloud/test.bam > " + PathDetail.getHdfsLocalPath() + "/nbcloud22/test.bam 2> " + PathDetail.getHdfsLocalPath()
				+ "/nbcloud22/test.log", cmd);
	}

	@Test
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

	@Test
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
		assertEquals("samtools index " + FileOperate.addSep(PathDetail.getHdfsLocalPath()) + "nbcloud/test.bam", cmd);
	}

	@Test
	public void testCmdRun2() {
		List<String> lsCmd = Lists.newArrayList("java", "-version");
		CmdOperate cmdOperate = new CmdOperate(lsCmd);

		String cmd = ArrayOperate.cmbString(cmdOperate.cmdPath.getRunCmd(), " ");
		assertTrue("java -version".equals(cmd));

		cmdOperate.runWithExp();
		assertTrue(cmdOperate.isFinishedNormal());

		List<String> lsErrOut = cmdOperate.getLsErrOut();
		String currJdkVersion = System.getProperty("java.version");
		assertTrue(lsErrOut.get(0).contains(currJdkVersion));

		lsCmd = Lists.newArrayList("echo", "\"hello,world\"");
		cmdOperate = new CmdOperate(lsCmd);
		cmd = ArrayOperate.cmbString(cmdOperate.cmdPath.getRunCmd(), " ");
		soft.assertThat("echo \"hello,world\"").isEqualTo(cmd.trim());

		cmdOperate.runWithExp();
		assertTrue(cmdOperate.isFinishedNormal());
		lsErrOut = cmdOperate.getLsErrOut();
//		soft.assertThat(lsErrOut.get(0)).isEqualTo("hello,world");
		
		
	}

	@Test
	public void testSplitCmd() {
		String cmd = "samtools  index /hdfs:/home/novelbio/test.bam  >  /home/novelbio/test.sam";
		List<String> lsCmd = CmdOperate.splitCmd(cmd);
		List<String> lsCmdExpect = new ArrayList<>();
		lsCmdExpect.add("samtools");
		lsCmdExpect.add("index");
		lsCmdExpect.add("/hdfs:/home/novelbio/test.bam");
		lsCmdExpect.add(">");
		lsCmdExpect.add("/home/novelbio/test.sam");
		assertEquals(lsCmdExpect, lsCmd);

		cmd = "samtools index \"/hdfs:/home/novelbio/test.bam\" \" > \" /home/novelbio/test.sam";
		lsCmd = CmdOperate.splitCmd(cmd);
		lsCmdExpect = new ArrayList<>();
		lsCmdExpect.add("samtools");
		lsCmdExpect.add("index");
		lsCmdExpect.add("\"/hdfs:/home/novelbio/test.bam\"");
		lsCmdExpect.add("\" > \"");
		lsCmdExpect.add("/home/novelbio/test.sam");
		assertEquals(lsCmdExpect, lsCmd);

		cmd = "samtools index \"/hdfs:/home/novelbio/test.bam\" 'test\"Is ok '  \" > \" /home/novelbio/test.sam";
		lsCmd = CmdOperate.splitCmd(cmd);
		lsCmdExpect = new ArrayList<>();
		lsCmdExpect.add("samtools");
		lsCmdExpect.add("index");
		lsCmdExpect.add("\"/hdfs:/home/novelbio/test.bam\"");
		lsCmdExpect.add("'test\"Is ok '");
		lsCmdExpect.add("\" > \"");
		lsCmdExpect.add("/home/novelbio/test.sam");
		assertEquals(lsCmdExpect, lsCmd);
	}

	@After
	public void after() {
		soft.assertAll();
	}
}
