package com.novelbio.base.util;

import java.text.DecimalFormat;
import java.util.Random;

public class CommonUtil {
	/**
	 * 工具类禁止实例化
	 */
	private CommonUtil() {
		throw new IllegalAccessError("Utility class");
	}

	/**
	 * 格式化文件大小，转换单位为B/KB/MB/GB
	 * @param size
	 * @return String
	 */
	public static String convertFileSize(long size) {
		long kb = 1024;
		long mb = kb * 1024;
		long gb = mb * 1024;

		if (size >= gb) {
			return String.format("%.1f GB", (float) size / gb);
		} else if (size >= mb) {
			float f = (float) size / mb;
			return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
		} else if (size >= kb) {
			float f = (float) size / kb;
			return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
		} else
			return String.format("%d B", size);
	}

	/**
	 * 格式化文件大小，转换单位为B/KB/MB/GB，取整
	 * @param size
	 * @return String
	 */
	public static String convertFileSizeToInt(long size) {
		long kb = 1024;
		long mb = kb * 1024;
		long gb = mb * 1024;

		if (size >= gb) {
			return String.format("%d GB", (int) size / gb);
		} else if (size >= mb) {
			return String.format("%d MB", (int) size / mb);
		} else if (size >= kb) {
			return String.format("%d KB", (int) size / kb);
		} else
			return String.format("%d B", size);
	}

	/**
	 * 格式化输出长整形数字，按逗号分隔
	 * @param data
	 * @return String
	 */
	public static String formatToSepara(long data) {
		DecimalFormat df = new DecimalFormat("#,###");
		return df.format(data);
	}

	/**
	 * 获取一个唯一ID，长度为13
	 * @return String
	 */
	public static String getUniqueId() {
		return Long.toString(System.currentTimeMillis() - (new Random()).nextInt(300));
	}
}
