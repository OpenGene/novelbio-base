package com.novelbio.base.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.lang3.StringUtils;

public class ObjectUtil {

	/**
	 * 对象深度克隆<br/>
	 * <b>注意:对瞬态的属性,会丢失值</b>
	 * 
	 * @param t
	 * @return
	 */
	public static <T> T deepClone(T t) {
		T tc = null;
		try {
			ByteArrayOutputStream bo = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bo);
			out.writeObject(t);
			out.close();

			// 从流里读出来
			ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
			ObjectInputStream oi = new ObjectInputStream(bi);
			tc = (T) oi.readObject();
			oi.close();

			return tc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tc;
	}

	/**
	 * 改变为驼峰命名法<br>
	 * 当前仅支持下划线区分的命名法，如the_people_name<br>
	 * 转换后为 thePeopleName
	 * 
	 * @param fieldName
	 *            使用下划线的命名法，如a_b_c
	 * @return
	 */
	public static String toCamel(String fieldName) {
		// 后续扩展时，只需将split的过程扩展即可支持其他命名转换为驼峰
		String[] arrName = fieldName.split("_");
		return joinUseCamelCase(arrName);
	}

	/**
	 * 转化为列表的驼峰命名<br>
	 * ls前缀
	 * 
	 * @param fieldName
	 *            使用下划线的命名法，如a_b_c
	 * @return
	 */
	public static String toCamelForList(String fieldName) {
		String[] arrName = fieldName.split("_");

		// 添加ls到列表首位
		String[] arrListName = new String[arrName.length + 1];
		arrListName[0] = "ls";
		System.arraycopy(arrName, 0, arrListName, 1, arrName.length);

		return joinUseCamelCase(arrListName);
	}

	/**
	 * 使用驼峰命名法，将命名的各个词组合起来
	 * @param arrName
	 * @return
	 */
	private static String joinUseCamelCase(String[] arrName) {
		boolean isFirst = true;
		StringBuilder sb = new StringBuilder();
		for (String str : arrName) {
			// 为空时，跳过当前循环
			if (StringUtils.isBlank(str)) {
				continue;
			}
			if (isFirst) { // 首个词不变
				sb.append(str);
				isFirst = false;
			} else { // 其他的首字母大写
				char firstChar = str.charAt(0);
				firstChar = Character.toUpperCase(firstChar); // 首字母大学
				sb.append(firstChar).append(str.substring(1)); //
			}
		}
		return sb.toString();
	}
}
