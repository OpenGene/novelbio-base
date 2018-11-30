package com.novelbio.base.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.beans.BeanMap;

/**
 * 处理latex的保留字符转义。<br>
 * # $ % ^ & _ { } ~ \
 * 
 * @author novelbio liqi
 * @date 2018年9月11日 下午2:17:39
 */
public class LatexUtil {
	/** latex保留字符和替换字符 */
	public Map<Character, String> mapLatexKay2Instead = new HashMap<>();

	public LatexUtil() {
		mapLatexKay2Instead.put('#', "\\#");
		mapLatexKay2Instead.put('$', "\\$");
		mapLatexKay2Instead.put('%', "\\%");
		mapLatexKay2Instead.put('^', "\\^");
		mapLatexKay2Instead.put('&', "\\&");
		mapLatexKay2Instead.put('_', "\\_");
		mapLatexKay2Instead.put('{', "\\{");
		mapLatexKay2Instead.put('}', "\\}");
		mapLatexKay2Instead.put('~', "\\~");
		mapLatexKay2Instead.put('\\', "$\\backslash$");
	}

	/**
	 * 替换latex保留字符<br>
	 * c不是关键字时，返回null
	 * 
	 * @param c
	 *            待替换的字符
	 * @return str替换后的string，null表示未替换
	 */
	private String insteadKeyChar(Character c) {
		String insteadStr = null;
		if (mapLatexKay2Instead.containsKey(c)) {
			insteadStr = mapLatexKay2Instead.get(c);
		}
		return insteadStr;
	}

	/**
	 * 替换字符串中的latex保留字符
	 * 
	 * @param str
	 * @return
	 */
	public String insteadStr(String str) {
		StringBuilder sb = new StringBuilder();
		char[] arrChar = str.toCharArray();
		for (char c : arrChar) {
			String insteadKay = insteadKeyChar(c);
			if (insteadKay == null) {
				sb.append(c);
			} else {
				sb.append(insteadKay);
			}
		}
		return sb.toString();
	}

	/**
	 * 处理对象中的所有字符串中的latex保留字符<br>
	 * 对象的list，array，map中包含的字符串也会进行处理
	 * 
	 * @param t
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T insteadKey(T t) {
		if (t == null) {
			return t;
		} else if (t instanceof String) {
			String str = insteadStr((String) t);
			return (T) str;
		} else if (isBaseType(t)) { // 必须在string处理之后
			// 基本类型不处理
			return t;
		}
		T obj = null;
		if (t instanceof Collection) {
			obj = insteadList(t);
		} else if (t.getClass().isArray()) {
			// 暂时不支持array
			// obj = insteadArray((Object[]) t);
			obj = t;
		} else if (t instanceof Map) {
			obj = insteadMap(t);
		} else {
			BeanMap map = BeanMap.create(t);
			map = insteadMap(map);
			obj = t;
		}
		return obj;
	}

	/**
	 * 替换数组中的保留关键字
	 * 
	 * @param arrObj
	 * @return
	 */
	public Object[] insteadArray(Object[] arrObj) {

		Class genericClass = arrObj.getClass().getComponentType();
		Object[] objCopy = (Object[]) Array.newInstance(genericClass, arrObj.length);
		Object[] arrCopy = new Object[arrObj.length];
		for (int i = 0; i < arrObj.length; i++) {
			Object obj = arrObj[i];
			Object insteadObj = insteadKey(obj);
			arrCopy[i] = insteadObj;
		}
		return arrCopy;
	}

	/**
	 * 替换列表中的内容
	 * 
	 * @param collectionObj
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> T insteadList(T t) {
		try {
			T tCopy = (T) t.getClass().newInstance();
			Collection cCopy = (Collection) tCopy;
			Collection c = (Collection) t;
			for (Object cobj : c) {
				Object insteadObj = insteadKey(cobj);
				cCopy.add(insteadObj);
			}
			return tCopy;
		} catch (Exception e) {
		}
		return t;
	}

	/**
	 * 替换map内容中的关键字
	 * 
	 * @param mapObj
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> T insteadMap(T t) {
		Map map = (Map) t;
		for (Object mapKey : map.keySet()) {
			Object mapValue = map.get(mapKey);
			mapValue = insteadKey(mapValue);
			map.put(mapKey, mapValue);
		}
		return t;
	}

	/**
	 * 确认是否是基本类型<br>
	 * 当前确认的方法为，在java.lang包中的为基本类型
	 * 
	 * @param obj
	 * @return
	 */
	public boolean isBaseType(Object obj) {
		String className = obj.getClass().getName();
		if (className.startsWith("java.lang")) {
			return true;
		}
		return false;
	}

}
