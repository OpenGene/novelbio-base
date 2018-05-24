package com.novelbio.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

public class TestMyBeanUtils {
	static Logger logger = LoggerFactory.getLogger(TestMyBeanUtils.class);

	public static void main(String[] args) throws URISyntaxException {
		logger.error("test");
		// URI uri = new URI("hdfs:/nbCloud/NT-01_gvcf.vcf");
		// System.out.println(uri.getScheme());
		// Path file = Paths.get(uri);
		// System.out.println("File [" + file.toString() + "] exists = '" +
		// Files.exists(file) + "'");
	}

	@Test
	public void testCopyMap2Object() {
		Map<String, Object> mapField2Val = new HashMap<>();

		String a = "string a";
		mapField2Val.put("a", a);

		Date b = new Date();
		mapField2Val.put("b", b);
		List<String> ltStr = new ArrayList<>();
		ltStr.add("ls01");
		ltStr.add("ls02");
		ltStr.add("ls03");
		mapField2Val.put("lsStr", ltStr);

		Set<Date> setDate = new HashSet<>();
		setDate.add(b);
		mapField2Val.put("setDate", setDate);

		Map<String, Object> mapTest = new HashMap<>();
		mapTest.put("1", 1);
		mapTest.put("2", new Date());
		mapTest.put("3", new JsonObject());
		mapField2Val.put("mapKey2Obj", mapTest);

		TestMyBeanUtilModel testModel = new TestMyBeanUtilModel();
		MyBeanUtils.copyMap2Object(mapField2Val, testModel);

		assertTrue(testModel.getA().equals(a)); // string
		assertTrue(testModel.getB().equals(b)); // date
		assertTrue(testModel.getSetDate().size() == 1);	// set<>

		assertTrue(testModel.getLsStr().size() == 3);	// list
		assertTrue(testModel.getMapKey2Obj().entrySet().size() == 3); //map

		mapField2Val.put("a", null);
		mapTest.put("1", "");
		mapField2Val.put("mapKey2Obj", mapTest);
		MyBeanUtils.copyMap2Object(mapField2Val, testModel); // map中仅修改key值=""
		
		assertNull(testModel.getA()); // null
		assertTrue(testModel.getMapKey2Obj().get("1").equals(""));	// map中仅修改key值=""
		
		String jj = "jjjj";
		mapField2Val.put("mapKey2Obj.1", jj);
		MyBeanUtils.copyMap2Object(mapField2Val, testModel);
		assertTrue(testModel.getMapKey2Obj().get("1").equals(jj)); // 通过a.b形式修改map实例a的b健数据
		
		TestMyBeanUtilModel innerModel = new TestMyBeanUtilModel();
		
		// map中有对象A，即map有嵌套数据，暂不支持
//		mapField2Val.put("inModel", innerModel);
//		testModel.getMapKey2Obj().put("inModel", mapField2Val);
//		String innerA ="innerA";
//		mapField2Val.put("mapKey2Obj.inModel.a", innerA);
//		MyBeanUtils.copyMap2Object(mapField2Val, testModel);
//		assertTrue(((TestMyBeanUtilModel)testModel.getMapKey2Obj().get("innerModel")).getA().equals(innerA)); // 通过a.b形式修改map实例TestMyBeanUtilModel的a属性
	}
}