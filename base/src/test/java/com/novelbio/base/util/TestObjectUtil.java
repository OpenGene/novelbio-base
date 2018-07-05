package com.novelbio.base.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * 
 * @author novelbio liqi
 * @date 2018年7月4日 上午10:15:21
 */
public class TestObjectUtil {

	@Test
	public void testToCamel() {
		String fieldName = "abc_def_gh";
		String camelName = ObjectUtil.toCamel(fieldName);
		assertEquals("abcDefGh", camelName);

		fieldName = "_test_the_name_";
		camelName = ObjectUtil.toCamel(fieldName);
		assertEquals("testTheName", camelName);
		
		fieldName = "name";
		camelName = ObjectUtil.toCamel(fieldName);
		assertEquals("name", camelName);
	}

	@Test
	public void testToCamelForList() {
		String fieldName = "abc_def_gh";
		String camelName = ObjectUtil.toCamelForList(fieldName);
		assertEquals("lsAbcDefGh", camelName);

		fieldName = "_test_the_name_";
		camelName = ObjectUtil.toCamelForList(fieldName);
		assertEquals("lsTestTheName", camelName);
		
		fieldName = "names";
		camelName = ObjectUtil.toCamelForList(fieldName);
		assertEquals("lsNames", camelName);
	}
}
