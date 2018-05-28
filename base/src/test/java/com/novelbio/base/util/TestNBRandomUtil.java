package com.novelbio.base.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author novelbio liqi
 * @date 2018年5月23日 下午2:30:44
 */
public class TestNBRandomUtil {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetInt() {
		int maxInt = 999;
		int errorCnt = 0;
		for (int i = 0; i < maxInt; i++) {
			int rInt = NBRandomUtil.getInt(maxInt);
			if (rInt > maxInt) {
				errorCnt++;
			}
		}
		assertThat(errorCnt == 0);
	}

	@Test
	public void testGetString() {
		int maxInt = 15; // 字符长度
		int loopCnt = 10; //	每个长度循环次数
		for (int i = 1; i <= maxInt; i++) {
			for (int j =0; j< loopCnt ; j++) {
				String strInt = NBRandomUtil.getString(i);
//				System.out.println(strInt);
				assertThat(strInt.length() == i);
			}
		}
	}

}
