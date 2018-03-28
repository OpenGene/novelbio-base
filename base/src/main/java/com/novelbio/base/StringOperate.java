package com.novelbio.base;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.dataStructure.PatternOperate;

public class StringOperate {
	private static final Logger logger = LoggerFactory.getLogger(StringOperate.class);
	private static PatternOperate patternOperate = new PatternOperate("[`~!@#$%^&*()+=|{}':;',\\[\\]<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\\.\\.+");

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
	
	/** 注意，这个方法会将String值为字符串为 "null" 的返回为true */
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
		return string1 == null ? string2 == null : string1.equals(string2);
	}
	public static boolean isEqualIgnoreCase(String string1, String string2) {
		return string1 == null ? string2 == null : string1.equalsIgnoreCase(string2);
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
		if (isHaveMessyCode(strText)) {
			try {
				return new String(strText.getBytes("ISO-8859-1"), "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
            }
        }
		return strText;
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
	
	/** html解码还很薄弱 */
	public static String decode(String inputUrl) {
		String result = "";
		try {
			result = URLDecoder.decode(inputUrl, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.error("解码出错：" + inputUrl);
		}
		result = StringEscapeUtils.unescapeHtml(result);
		return result;
	}
	
	/** 是否存在特殊字符，注意单独的.不算特殊字符，连续的".."才算 */
	public static boolean isContainerSpecialCode(String str) {
		String info = patternOperate.getPatFirst(str);
		return !isRealNull(info);		
	}
	
	/** 把特殊字符都替换掉，注意单独的.不算特殊字符，连续的".."才算 */
	public static String replaceSpecialCode(String str) {
		Matcher m = patternOperate.getMatcher(str);
		return m.replaceAll("");
	}
	
	/** 首字母转小写 */
    public static String toLowerCaseFirstOne(String s)
    {
        if(Character.isLowerCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toLowerCase(s.charAt(0))).append(s.substring(1)).toString();
    }
    
    /**
     * 编码html中的符号.并将\n转为<br>标签
     * 
     * 如:<div>test</div>
     * 变成:&lt;div&gt;test&lt;/div&gt;
     * 
     * @date 2016年8月12日
     * @author novelbio fans.fan
     * @param content
     * @return
     */
    public static String escapeHtml(String content) {
    	if (isRealNull(content)) {
			return content;
		} else {
			return StringEscapeUtils.escapeHtml(content).replace("\n", "<br>").replace("\r\n", "<br>");
		}
    }
    
	/**
	 * 将ByteBuffer转为String类型
	 * 
	 * @param buffer
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public static String getString(ByteBuffer buffer) throws UnsupportedEncodingException {
		byte[] bytes = null;
		if(buffer.hasArray()) {
		    bytes = buffer.array();
		} else {
		    bytes = new byte[buffer.remaining()];
		    buffer.get(bytes);
		}
		return new String(bytes, "UTF-8");
	}

	/**
	 * 首字母转大写
	 * 
	 * @param s
	 * @return
	 */
	public static String toUpperCaseFirstOne(String s) {
		if(Character.isUpperCase(s.charAt(0)))
            return s;
        else
            return (new StringBuilder()).append(Character.toUpperCase(s.charAt(0))).append(s.substring(1)).toString();
	}
}
