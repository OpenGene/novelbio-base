package com.novelbio.base;
/**
 * 分割符，常用分割符号
 * 可以都用这个分割符进行分割文件
 * @author zong0jie
 */
public class SepSign {
	/**
	 * 最高等级
	 * 分割两个来源的ID或两个Description
	 * "@//@"
	 */
	public static final String SEP_ID = "@//@";

	/**
	 * <b>不要往这个symbol中添加"/"或"\"</b><br>
	 * 中间等级
	 * 分割 NCBIID的title和内容
	 * 如NCBI@@protein coding
	 * "@@"
	 */
	public static final String SEP_INFO = "@@";
	/**
	 * <b>不要往这个symbol中添加"/"或"\"</b><br>
	 * 简单分隔符
	 * 如NCBI@protein coding
	 * "@"
	 */
	public static final String SEP_INFO_SIMPLE = "@";
	/**
	 * <b>不要往这个symbol中添加"/"或"\"</b><br>
	 * 中间等级
	 * 分割 NCBIID的title和内容
	 * 如NCBI@@protein coding
	 * "(+)"
	 */
	public static final String SEP_INFO2 = "~`~";
	
	
	/**
	 * 最低等级
	 * 分割同一个数据库的两个不同的注释信息
	 * "#/#"
	 */
	public static final String SEP_INFO_SAMEDB = "#/#";
	
//	/**
//	 * 分割url中的名字的
//	 */
//	public static final String SEP_URL = "$$";
	
}
