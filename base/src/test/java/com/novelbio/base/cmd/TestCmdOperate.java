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
		
		cmdOperate.prepare();
		assertEquals("/hdfs:/nbcloud22/test.bam", cmdOperate.getSaveStdOutFile());
		
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
		String inFile = "/tmp/testcmd.txt";
		String outFile = "/tmp/grepResult.txt";
		TxtReadandWrite txtWrite = new TxtReadandWrite(inFile, true);
		for (int i = 0; i < 100; i++) {
			String in = i%2 == 0? "a" + i : "b" + i;
			txtWrite.writefileln(in);
		}
		txtWrite.close();
		
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("grep"); lsCmd.add("a"); lsCmd.add("<"); lsCmd.add(inFile);
		lsCmd.add(">"); lsCmd.add(outFile);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.runWithExp();
		TxtReadandWrite txtRead = new TxtReadandWrite(outFile);
		int i = 0;
		for (String string : txtRead.readlines()) {
			Assert.assertEquals("a" + i, string);
			i = i+2;
		}
		txtRead.close();
	}

	@Test
	public void testCmdRun2() {
		List<String> lsCmd = Lists.newArrayList("java", "-version");
		CmdOperate cmdOperate = new CmdOperate(lsCmd);

		String cmd = ArrayOperate.cmbString(cmdOperate.cmdOrderGenerator.getRunCmd(), " ");
		assertTrue("java -version".equals(cmd));

		cmdOperate.runWithExp();
		assertTrue(cmdOperate.isFinishedNormal());

		List<String> lsStdout = cmdOperate.getLsErrOut();
		String currJdkVersion = System.getProperty("java.version");
		assertTrue(lsStdout.get(0).contains(currJdkVersion));

		lsCmd = Lists.newArrayList("echo", "hello,world");
		cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setGetLsStdOut();
		cmd = ArrayOperate.cmbString(cmdOperate.cmdOrderGenerator.getRunCmd(), " ");
		soft.assertThat("echo hello,world").isEqualTo(cmd.trim());

		cmdOperate.runWithExp();
		assertTrue(cmdOperate.isFinishedNormal());
		lsStdout = cmdOperate.getLsStdOut();
		soft.assertThat(lsStdout.get(0)).isEqualTo("hello,world");
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
