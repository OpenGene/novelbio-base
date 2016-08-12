package com.novelbio.base.dataStructure;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestPatternOperate {

	@Test
	public void testGetMatcher() {
		PatternOperate patternOperate = new PatternOperate("(abc)\\w*\\.1\\.fq\\.gz$");
		String filename = "abcads.1.fq.gz";
		assertEquals(filename, patternOperate.getPatFirst(filename));
	}

}
