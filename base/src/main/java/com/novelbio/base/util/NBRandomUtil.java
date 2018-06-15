package com.novelbio.base.util;

import java.util.UUID;

import org.apache.commons.lang3.RandomUtils;

/**
 * 
 * @author novelbio liqi
 * @date 2018年5月23日 下午2:03:55
 */
public class NBRandomUtil {

	/**
	 * 获取从0到maxint之间的随机数
	 * 
	 * @param maxInt
	 * @return
	 */
	public static int getInt(int maxInt) {
		if (maxInt == 0) {
			maxInt = 1;
		}
		int ret = RandomUtils.nextInt(0, maxInt);
		return ret;
	}

	/**
	 * 获取随机数转换的字符串，字符串长度由length指定<br>
	 * 取长度为5位的字符串 "00023"
	 * 
	 * @param length
	 * @return
	 */
	public static String getString(int length) {
		int ranInt = RandomUtils.nextInt(0, Integer.MAX_VALUE);
		String strInt = ranInt + "";
		if (strInt.length() > length) {
			strInt = strInt.substring(strInt.length() - length);
		} else {
			strInt = String.format("%0" + length + "d", ranInt);
		}
		return strInt;
	}

	/**
	 * 获取指定长度的uuid字符串，去掉符号"-"<br>
	 * 参数超过32返回一个uuid，参数在0-32之间，截取uuid头部对于长度字符。小于等于0抛出非法参数异常<br>
	 * UUID version为随即UUID
	 * 
	 * @param length
	 *            大于0
	 * @return
	 */
	public static String getUUIDString(int length) {
		if (length < 1) {
			throw new IllegalArgumentException("the length must biger than 0. length = " + length);
		}
		String uuid = UUID.randomUUID().toString().replace("-", "");
		if (length > uuid.length()) {
			return uuid;
		} else {
			return uuid.substring(0, length);
		}
	}
}
