package com.novelbio.base.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.novelbio.base.StringOperate;

/**
 * json工具类
 * @author novelbio
 */
public class JsonUtil {
	private static final String ArrayFlag = "[]";
	/** 合并字符串时默认使用的分隔符：逗号 */
	private static String STRING_SEPARATOR = ",";

	public static void main(String[] args) {
		String json = "{\"data\":{\"counts\":{\"dg\":0,\"rx\":0,\"pi\":0,\"dl\":0,\"ca\":0,\"vip\":0,\"pw\":0,\"hap\":0,\"va\":0,\"pubs\":0},\"terms\": [{\"id\":1, \"name\":\"name1\"}, {\"id\":2, \"name\":\"name2\"}]},\"@status\":\"success\"}";
		System.out.println(JsonUtil.remove(json, new String[] {"data.terms[].name", "data.counts", "@status"}));
		
//		json = "{\"clinicalAnnotations\":[\"982040717\", \"982040718\"]}";
//		List<String> lsIds = JsonUtil.getStringLists(json, "clinicalAnnotations[]");
//		for (String string : lsIds) {
//			System.out.println(string);
//		}
//
//		json = "{\"data\":{\"drugLabels\":[{\"id\":\"PA166123409\",\"name\":\"Annotation1\"}, {\"id\": \"PA166159586\", \"name\": \"Annotation2\"}]}}";
//		lsIds = JsonUtil.getStringLists(json, "data.drugLabels[].id");
//		for (String string : lsIds) {
//			System.out.println(string);
//		}
//
//		json = "{\"data\":{\"d1\":\"d1value\", \"ds\":[{\"id\":\"PA1\",\"name\":\"An1\"}, {\"id\": \"PA2\", \"name\": \"A2\"}]}, \"version\":\"5\"}";
//		System.out.println(JsonUtil.getString(json, "data.ds[].id"));
//		System.out.println(JsonUtil.getString(json, "version"));
//		System.out.println(JsonUtil.getString(json, "data.d1"));
//
//		json = "{\"data\":{\"d1\":[{\"id\":\"1\",\"name\":[{\"f\":\"f1\",\"k\":\"k1\"},{\"f\":\"f2\",\"k\":\"k2\"}]}, {\"id\": \"2\", \"name\":[{\"f\":\"f3\",\"k\":\"k3\"},{\"f\":\"f4\",\"k\":\"k4\"}]}]}}";
//		System.out.println(JsonUtil.getString(json, "data.d1[].name[].f"));
//		System.out.println(JsonUtil.getString(json, "data.d1[].name[]"));
//		System.out.println(JsonUtil.getString(json, "data.d1[].name[].f", true));
//		
//		JSONArray list = JsonUtil.getJSONArray(json, "data.d1[]");
//		for (int i = 0; i < list.size(); i++) {
//			System.out.println(list.getJSONObject(i).toJSONString());
//		}
//		
//		list = JsonUtil.getJSONArray(json, "");
//		for (int i = 0; i < list.size(); i++) {
//			System.out.println(list.getJSONObject(i).toJSONString());
//		}
//		
//		Map<String, String> map = JsonUtil.getMap(json, " key1 : data.d1[].name[],key2 : data.d1[].name[].f , key3: data.d1[] ");
//		for (String key : map.keySet()) {
//			System.out.println(key + " = " + map.get(key));
//		}
//		System.out.println(JSON.toJSONString(map));
//		Map<String, String> map2 = JsonUtil.getMap(json, " key1 : data.d1[].name[],key2 : data.d1[].name[].f , key3: data.d1[] ");
//		for (String key : map2.keySet()) {
//			System.out.println(key + " = " + map2.get(key));
//		}
//		List<Map<String, String>> lsMaps = new ArrayList<>();
//		lsMaps.add(map);
//		lsMaps.add(map2);
//		System.out.println("lsMaps" + JSON.toJSONString(lsMaps));
//		
//		JSONArray array = JSON.parseArray(JSON.toJSONString(lsMaps));
//		for (int i = 0; i < array.size(); i++) {
//			System.out.println(array.getString(i));
//		}
	}
	
	/**
	 * 设置默认的字符串分隔符,默认为逗号
	 * @param separator 例如：分号";"
	 */
	public static void setStringSeparator(String separator) {
		STRING_SEPARATOR = separator;
	}

	/**
	 * 通过查询jsonStr获取字符串列表
	 * @param jsonStr 例如：{"data":{"drugLabels":[{"id":"PA166123409","name":"Annotation1"}, {"id": "PA166159586", "name": "Annotation2"}]}}
	 * @param query 例如：data.drugLabels[].id
	 * @return List<String> 例如：PA166123409, PA166159586
	 */
	public static List<String> getStringLists(String jsonStr, String query) {
		return getStringLists(JSON.parseObject(jsonStr), query);
	}

	/**
	 * 通过查询jsonObject对象获取字符串列表
	 * @param jsonObject 例如：{"data":{"drugLabels":[{"id":"PA166123409","name":"Annotation1"}, {"id": "PA166159586", "name": "Annotation2"}]}}
	 * @param query 例如：data.drugLabels[].id
	 * @return List<String> 例如：PA166123409, PA166159586
	 */
	public static List<String> getStringLists(JSONObject jsonObject, String query) {
		List<String> list = new ArrayList<>();
		if(StringOperate.isRealNull(query) || null == jsonObject) {
			return list;
		}
		List<String> attrs = Lists.newArrayList(query.split("\\."));
		getStringLists(list, jsonObject, 0, attrs, false);
		return list;
	}

	/**
	 * 通过查询jsonStr获取字符串，数组的情况，会使用STRING_SEPARATOR分隔合并为一个字符串
	 * @param jsonStr
	 * @param query 例如：data.d1[].name[].f / data.d1 / data.d1[].name[] 等等
	 * @return String 例如：PA166159586 / PA1,PA2 等等
	 */
	public static String getString(String jsonStr, String query) {
		return getString(JSON.parseObject(jsonStr), query, STRING_SEPARATOR);
	}
	
	/**
	 * 通过查询jsonStr获取字符串，数组的情况，会使用STRING_SEPARATOR分隔合并为一个字符串
	 * @param jsonStr
	 * @param query 例如：data.d1[].name[].f / data.d1 / data.d1[].name[] 等等
	 * @param onlyFirst 是否仅返回第一个。当查询到多个值的时候可以指定仅返回第一个命中的值，默认是返回全部值。
	 * @return String 例如：PA166159586 / PA1,PA2 等等
	 */
	public static String getString(String jsonStr, String query, boolean onlyFirst) {
		return getString(JSON.parseObject(jsonStr), query, STRING_SEPARATOR, onlyFirst);
	}

	/**
	 * 通过查询jsonObject获取字符串，数组的情况，会使用STRING_SEPARATOR分隔合并为一个字符串
	 * @param jsonObject
	 * @param query 例如：data.d1[].name[].f / data.d1 / data.d1[].name[] 等等
	 * @return String 例如：PA166159586 / PA1,PA2 等等
	 */
	public static String getString(JSONObject jsonObject, String query) {
		return getString(jsonObject, query, STRING_SEPARATOR);
	}
	
	/**
	 * 通过查询jsonObject获取字符串，数组的情况，会使用STRING_SEPARATOR分隔合并为一个字符串
	 * @param jsonObject
	 * @param query 例如：data.d1[].name[].f / data.d1 / data.d1[].name[] 等等
	 * @param onlyFirst 是否仅返回第一个。当查询到多个值的时候可以指定仅返回第一个命中的值，默认是返回全部值。
	 * @return String 例如：PA166159586 / PA1,PA2 等等
	 */
	public static String getString(JSONObject jsonObject, String query, boolean onlyFirst) {
		return getString(jsonObject, query, STRING_SEPARATOR, onlyFirst);
	}

	/**
	 * 通过查询jsonStr获取字符串，数组的情况，会使用separator分隔合并为一个字符串
	 * @param jsonObject
	 * @param query 例如：data.d1[].name[].f / data.d1 / data.d1[].name[] 等等
	 * @param separator 分隔符，例如分号;
	 * @return String 例如：PA166159586 / PA1;PA2 等等
	 */
	public static String getString(String jsonStr, String query, String separator) {
		return getString(JSON.parseObject(jsonStr), query, separator);
	}

	/**
	 * 通过查询jsonObject获取字符串，数组的情况，会使用separator分隔合并为一个字符串
	 * @param jsonObject
	 * @param query 例如：data.d1[].name[].f / data.d1 / data.d1[].name[] 等等
	 * @param separator 分隔符，例如分号;
	 * @return String 例如：PA166159586 / PA1;PA2 等等
	 */
	public static String getString(JSONObject jsonObject, String query, String separator) {
		return getString(jsonObject, query, separator, false);
	}
	
	/**
	 * 通过查询jsonObject获取字符串，数组的情况，会使用separator分隔合并为一个字符串
	 * @param jsonObject
	 * @param query 例如：data.d1[].name[].f / data.d1 / data.d1[].name[] 等等
	 * @param separator 分隔符，例如分号;
	 * @param onlyFirst 是否仅返回第一个。当查询到多个值的时候可以指定仅返回第一个命中的值，默认是返回全部值。
	 * @return String 例如：PA166159586 / PA1;PA2 等等
	 */
	public static String getString(JSONObject jsonObject, String query, String separator, boolean onlyFirst) {
		List<String> list = new ArrayList<>();
		if(StringOperate.isRealNull(query) || null == jsonObject) {
			return null;
		}
		List<String> attrs = Lists.newArrayList(query.split("\\."));
		getStringLists(list, jsonObject, 0, attrs, onlyFirst);
		return Joiner.on(separator).skipNulls().join(list);
	}

	/**
	 * 递归函数，循环解析查询属性
	 * @param list 查询结果列表
	 * @param jsonObject json对象
	 * @param index 当前查询属性索引
	 * @param attrs 查询属性列表
	 * @param onlyFirst 是否仅返回第一个。当查询到多个值的时候可以指定仅返回第一个命中的值，默认是返回全部值。
	 */
	private static void getStringLists(List<String> list, JSONObject jsonObject, int index, List<String> attrs, boolean onlyFirst) {
		if(null == jsonObject) {
			return;
		}
		if (index >= attrs.size()) {
			// 全部查询属性均解析完毕
			return;
		}
		if(onlyFirst && list.size() == 1) {
			return;
		}
		String attr = attrs.get(index).trim();
		if(attr.length() == 0) {
			return;
		}
		if ((attrs.size() - index) == 1) {
			// 最后一个解析属性
			if (attr.contains(ArrayFlag)) {
				JSONArray array = jsonObject.getJSONArray(attr.replace(ArrayFlag, ""));
				if(null == array) {
					return;
				}
				for (int i = 0; i < array.size(); i++) {
					list.add(array.getString(i));
				}
			} else {
				list.add(jsonObject.getString(attr));
			}
		} else {
			if (attr.contains(ArrayFlag)) {
				JSONArray array = jsonObject.getJSONArray(attr.replace(ArrayFlag, ""));
				if(null == array) {
					return;
				}
				for (int i = 0; i < array.size(); i++) {
					getStringLists(list, array.getJSONObject(i), (index + 1), attrs, onlyFirst);
				}
			} else {
				getStringLists(list, jsonObject.getJSONObject(attr), (index + 1), attrs, onlyFirst);
			}
		}
	}
	
	/**
	 * 从jsonStr中获取指定查询语句的JSONArray
	 * @param jsonStr
	 * @param query 例如：data.d1[]
	 * @return JSONArray
	 */
	public static JSONArray getJSONArray(String jsonStr, String query) {
		return getJSONArray(JSON.parseObject(jsonStr), query);
	}
	
	/**
	 * 从jsonObject中获取指定查询语句的JSONArray
	 * @param jsonObject
	 * @param query 例如：data.d1[]
	 * @return JSONArray
	 */
	public static JSONArray getJSONArray(JSONObject jsonObject, String query) {
		JSONArray list = new JSONArray();
		if(StringOperate.isRealNull(query) || null == jsonObject) {
			return list;
		}
		
		List<String> attrs = Lists.newArrayList(query.split("\\."));
		getJSONArray(list, jsonObject, 0, attrs);
		return list;
	}
	
	/**
	 * 从jsonObject中获取指定查询语句的JSONArray
	 * @param list 返回结果
	 * @param jsonObject
	 * @param index 当前查询属性索引
	 * @param attrs 查询属性列表
	 */
	private static void getJSONArray(JSONArray list, JSONObject jsonObject, int index, List<String> attrs) {
		if(null == jsonObject) {
			return;
		}
		if (index >= attrs.size()) {
			// 全部查询属性均解析完毕
			return;
		}
		String attr = attrs.get(index).trim();
		if(attr.length() == 0) {
			return;
		}
		if ((attrs.size() - index) == 1) {
			// 最后一个解析属性
			if (attr.contains(ArrayFlag)) {
				JSONArray array = jsonObject.getJSONArray(attr.replace(ArrayFlag, ""));
				if(null == array) {
					return;
				}
				for (int i = 0; i < array.size(); i++) {
					list.add(array.getJSONObject(i));
				}
			} else {
				list.add(jsonObject.getJSONObject(attr));
			}
		} else {
			if (attr.contains(ArrayFlag)) {
				JSONArray array = jsonObject.getJSONArray(attr.replace(ArrayFlag, ""));
				if(null == array) {
					return;
				}
				for (int i = 0; i < array.size(); i++) {
					getJSONArray(list, array.getJSONObject(i), (index + 1), attrs);
				}
			} else {
				getJSONArray(list, jsonObject.getJSONObject(attr), (index + 1), attrs);
			}
		}
	}
	
	/**
	 * 通过查询jsonStr获取字符串Map
	 * @param jsonStr
	 * @param mapQuerys 格式：key:query,key:query,... 例如：key1:data.d1[].name[].f,key2:data.d1[].version,...
	 * @return Map<String, String>
	 */
	public static Map<String, String> getMap(String jsonStr, String mapQuerys) {
		return getMap(JSON.parseObject(jsonStr), mapQuerys);
	}
	
	/**
	 * 通过查询jsonObject获取字符串Map
	 * @param jsonObject
	 * @param mapQuerys 格式：key:query,key:query,... 例如：key1:data.d1[].name[].f,key2:data.d1[].version,...
	 * @return Map<String, String>
	 */
	public static Map<String, String> getMap(JSONObject jsonObject, String mapQuerys) {
		Map<String, String> map = new HashMap<>();
		if(StringOperate.isRealNull(mapQuerys) || null == jsonObject) {
			return map;
		}
		
		List<String> lsQuerys = Lists.newArrayList(mapQuerys.split(","));
		for (String query : lsQuerys) {
			String[] keyQuery = query.split(":");
			if(keyQuery.length == 2) {
				getMap(map, jsonObject, keyQuery[0], keyQuery[1]);
			}
		}
		return map;
	}
	
	/**
	 * 通过查询jsonObject获取字符串Map
	 * @param map 返回结果
	 * @param jsonObject
	 * @param key 指定的map的key
	 * @param query 查询语句，例如：data.d1[].version
	 */
	private static void getMap(Map<String, String> map, JSONObject jsonObject, String key, String query) {
		if(StringOperate.isRealNull(query)) {
			return;
		}
		
		map.put(key, getString(jsonObject, query));
	}
	
	/**
	 * 通过查询JSONArray获取List<Map<String, String>>
	 * @param array
	 * @param mapQuerys 格式：key:query,key:query,... 例如：key1:data.d1[].name[].f,key2:data.d1[].version,...
	 * @return List<Map<String, String>>
	 */
	public static List<Map<String, String>> getListMap(JSONArray array, String mapQuerys) {
		List<Map<String, String>> lsMaps = new ArrayList<>();
		if(StringOperate.isRealNull(mapQuerys) || null == array) {
			return lsMaps;
		}
		
		for (int i = 0; i < array.size(); i++) {
			lsMaps.add(getMap(array.getJSONObject(i), mapQuerys));
		}
		return lsMaps;
	}
	
	/**
	 * 删除指定原始jsonStr字符串中的子对象querys列表
	 * @param jsonStr 待删除子对象的原始json字符串
	 * @param querys 例如：new String[] {"data.terms[].name", "data.counts", "status"}
	 * @return String 删除了指定子对象后的json字符串
	 */
	public static String remove(String jsonStr, String[] querys) {
		JSONObject jsonObject = JSON.parseObject(jsonStr);
		for (String query : querys) {
			remove(jsonObject, query);
		}
		return jsonObject.toJSONString();
	}
	
	/**
	 * 删除指定原始jsonStr字符串中的子对象query
	 * @param jsonStr 待删除子对象的原始json字符串
	 * @param query 例如："data.terms[].name", "data.counts" 等等
	 * @return String 删除了指定子对象后的json字符串
	 */
	public static String remove(String jsonStr, String query) {
		JSONObject jsonObject = remove(JSON.parseObject(jsonStr), query);
		if(jsonObject == null) {
			return null;
		} else {
			return jsonObject.toJSONString();
		}
	}
	
	/**
	 * 删除指定原始对象jsonObject中的子对象querys列表
	 * @param jsonObject 待删除子对象的原始对象，注意：该对象将被修改
	 * @param querys 例如：new String[] {"data.terms[].name", "data.counts", "status"}
	 * @return JSONObject 删除了指定子对象后的jsonObject
	 */
	public static JSONObject remove(JSONObject jsonObject, String[] querys) {
		for (String query : querys) {
			remove(jsonObject, query);
		}
		return jsonObject;
	}
	
	/**
	 * 删除指定原始jsonObject中的子对象query
	 * @param jsonStr 待删除子对象的原始json字符串
	 * @param query 例如："data.terms[].name", "data.counts" 等等
	 * @return JSONObject 删除了指定子对象后的jsonObject
	 */
	public static JSONObject remove(JSONObject jsonObject, String query) {
		if(StringOperate.isRealNull(query) || null == jsonObject) {
			return null;
		}
		List<String> attrs = Lists.newArrayList(query.split("\\."));
		
		remove(jsonObject, 0, attrs);
		
		return jsonObject;
	}
	
	/**
	 * 删除指定jsonObject中的子对象
	 * @param jsonObject
	 * @param index 当前查询属性索引
	 * @param attrs 查询属性列表
	 */
	private static void remove(JSONObject jsonObject, int index, List<String> attrs) {
		if(null == jsonObject) {
			return;
		}
		if (index >= attrs.size()) {
			// 全部查询属性均解析完毕
			return;
		}
		String attr = attrs.get(index).trim();
		if(attr.length() == 0) {
			return;
		}
		if ((attrs.size() - index) == 1) {
			// 最后一个解析属性
			if (attr.contains(ArrayFlag)) {
				jsonObject.remove(attr.replace(ArrayFlag, ""));
			} else {
				jsonObject.remove(attr);
			}
		} else {
			if (attr.contains(ArrayFlag)) {
				JSONArray array = jsonObject.getJSONArray(attr.replace(ArrayFlag, ""));
				if(null == array) {
					return;
				}
				for (int i = 0; i < array.size(); i++) {
					remove(array.getJSONObject(i), (index + 1), attrs);
				}
			} else {
				remove(jsonObject.getJSONObject(attr), (index + 1), attrs);
			}
		}
	}
}
