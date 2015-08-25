package com.novelbio.base;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

public class StringOperate {

	private static int compare(String str, String target) {
		int d[][]; // 矩阵
		int n = str.length();
		int m = target.length();
		int i; // 遍历str的
		int j; // 遍历target的
		char ch1; // str的
		char ch2; // target的
		int temp; // 记录相同字符,在某个矩阵位置值的增量,不是0就是1
		if (n == 0) {
			return m;
		}
		if (m == 0) {
			return n;
		}
		d = new int[n + 1][m + 1];
		for (i = 0; i <= n; i++) { // 初始化第一列
			d[i][0] = i;
		}

		for (j = 0; j <= m; j++) { // 初始化第一行
			d[0][j] = j;
		}

		for (i = 1; i <= n; i++) { // 遍历str
			ch1 = str.charAt(i - 1);
			// 去匹配target
			for (j = 1; j <= m; j++) {
				ch2 = target.charAt(j - 1);
				if (ch1 == ch2) {
					temp = 0;
				} else {
					temp = 1;
				}

				// 左边+1,上边+1, 左上角+temp取最小
				d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + temp);
			}
		}
		return d[n][m];
	}

	private static int min(int one, int two, int three) {
		return (one = one < two ? one : two) < three ? one : three;
	}

	/**
	 * 
	 * 获取两字符串的相似度
	 * 
	 * 
	 * 
	 * @param str
	 * 
	 * @param target
	 * 
	 * @return
	 */

	public static float getSimilarityRatio(String str, String target) {
		if(str == null || target == null)
			return 0;
		return 1 - (float) compare(str, target) / Math.max(str.length(), target.length());
	}

	public static boolean isRealNull(String string) {
		if (string == null) {
			return true;
		}else  if(string.trim().equals("")) {
			return true;
		}else if(string.equals("null")){
			return true;
		}else {
			return false;
		}
	}
	
	public static boolean isEqual(String string1, String string2) {
		if ((string1 == null && string2 != null)
				||
				string1 != null && string2 == null
				) {
			return false;
		}
		
		if (string1 == null && string2 == null) {
			return true;
		}
		return string1.equals(string2);
	}
	
	// 代码是网上找的 不过感觉不错 大家可以试试
	/**
	 * 完全分割一个字符串
	 * @param key
	 * @return
	 */
	public static Set<String> splitAbsolute(String key){
		Set<String> setWords = new HashSet<>();
		for (int i = 1; i <= key.length(); i++) {
			setWords.add(key.substring(i-1, i));
		}
		return setWords;
	}
	
	/**
	 * 判断文本中是否有乱码，用于get提交出现乱码现象
	 * @param strText：要判断的文本
	 * @return
	 */
	public static String changeMessyCode(String strText) {
		String strUtf;
		String strIso;
		try {
			strUtf = new String(strText.getBytes("ISO-8859-1"), "utf-8");
			strIso = new String(strUtf.getBytes("utf-8"), "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return strText;
		}
		if (strIso.equals(strText)) {
			return strUtf;
		} else {
			return strText;
		}
	}
	
	/**
	 * 判断文本中是否有乱码，用于get提交出现乱码现象
	 * @param strText：要判断的文本
	 * @return
	 */
	public static boolean isHaveMessyCode(String strText) {
		String strUtf;
		String strIso;
		try {
			strUtf = new String(strText.getBytes("ISO-8859-1"), "utf-8");
			strIso = new String(strUtf.getBytes("utf-8"), "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return strIso.equals(strText);
	}
	
	/** 处理latex中的特殊字符，例如：如果含有“%”，则替换成“\%”（现实现“_”和“%”） */
	public static String handleSpecialSign(String str) {
		String strNew = str;
		if (str.contains("_")) {
			strNew = strNew.replace("_", "\\_");
		}
		if (str.contains("%")) {
			strNew = strNew.replace("%", "\\%");
		}
		return strNew;
	}
}
