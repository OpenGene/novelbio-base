package com.novelbio.base.cmd;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.novelbio.base.PathDetail;
import com.novelbio.base.util.ServiceEnvUtil;

public class TestCmdOperateAli {

	static SoftAssertions soft = new SoftAssertions();
	static String env;
	@BeforeClass
	public static void before() {
		env = PathDetail.properties.getProperty("env");
		PathDetail.properties.put("env", ServiceEnvUtil.ENV_ALIYUN);
		
	}
	
	@Test
	public void testCmdReal() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("samtools");
		lsCmd.add("index");
		lsCmd.add("oss://nbcloud/test.bam");
		lsCmd.add(">");
		lsCmd.add("oss://nbcloud/nbcloud22/test.bam");
		lsCmd.add("2>");
		lsCmd.add("oss://nbcloud/nbcloud22/test.log");
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		
		cmdOperate.prepareAndMoveFileIn();
		assertEquals("oss://nbcloud/nbcloud22/test.bam", cmdOperate.getSaveStdOutFile());
		
		String cmd = cmdOperate.getCmdExeStrReal();
		System.out.println(cmd);
	}

	@AfterClass
	public static void after() {
		PathDetail.properties.put("env", env);
		soft.assertAll();
	}
}
