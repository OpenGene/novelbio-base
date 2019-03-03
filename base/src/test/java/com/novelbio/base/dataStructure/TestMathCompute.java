package com.novelbio.base.dataStructure;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestMathCompute {
	@Test
	public void testRoman2Int() {
		assertEquals(26, MathComput.roman2Int("XXVI"));
		assertEquals(16, MathComput.roman2Int("XVI"));
		assertEquals(11, MathComput.roman2Int("XI"));
		assertEquals(9, MathComput.roman2Int("IX"));
		assertEquals(8, MathComput.roman2Int("IIX"));
		assertEquals(7, MathComput.roman2Int("IIIX"));
		
		assertEquals(7, MathComput.roman2Int("VII"));
		assertEquals(8, MathComput.roman2Int("VIII"));
		assertEquals(9, MathComput.roman2Int("VIIII"));

	}
}
