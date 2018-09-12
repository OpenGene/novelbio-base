package com.novelbio.base.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * 
 * @author novelbio liqi
 * @date 2018年9月11日 下午5:00:38
 */
public class TestLatexUtil {

	@Test
	public void testInsteadKey() {

		LatexUtil util = new LatexUtil();

		Map<Object, Object> map = new HashMap<>();
		map.put("a", "#");
		map.put("b", "\\");
		Map insteadMap = util.insteadMap(map);
		assertEquals("\\#", insteadMap.get("a"));
		assertEquals("$\\backslash$", insteadMap.get("b"));

		List<String> ls = new ArrayList<>();
		ls.add("-#-");
		List<String> insteadLs = (List<String>) util.insteadList(ls);
		assertEquals("-\\#-", insteadLs.get(0));

		String str = "~";
		String insteadStr = util.insteadKey(str);
		assertEquals("\\~", insteadStr);

		String[] arrStr = new String[2];
		arrStr[0] = "$";
		arrStr[1] = "&";
		String[] insteadArr = util.insteadKey(arrStr);
		assertEquals("$", insteadArr[0]);
		assertEquals("&", insteadArr[1]);
	}

}
