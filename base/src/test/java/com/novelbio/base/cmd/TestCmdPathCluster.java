package com.novelbio.base.cmd;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class TestCmdPathCluster {
	
	@Test
	public void testMergePath() {
		List<String> lsPaths = new ArrayList<>();
		
		lsPaths.add("/media/winA/mywork");
		lsPaths.add("/media/winB/mywork");
		lsPaths.add("/media/winC/mywork/gef/fse");
		lsPaths.add("/media/winC/mywork/abc");
		lsPaths.add("/media/winC/mywork/gef/kk");
		lsPaths.add("/media/winC/mywork/abc/gfc");

		Set<String> setPaths = CmdPathCluster.mergeParentPath(lsPaths);
		
		Set<String> lsExp = new HashSet<>();
		lsExp.add("/media/winA/");
		lsExp.add("/media/winB/");
		lsExp.add("/media/winC/mywork/");
		
		assertEquals(lsExp, setPaths);
		
	}
	
}
