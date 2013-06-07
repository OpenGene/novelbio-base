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
	 * 中间等级
	 * 分割 NCBIID的title和内容
	 * 如NCBI@@protein coding
	 * "@@"
	 */
	public static final String SEP_INFO = "@@";
	
	/**
	 * 最低等级
	 * 分割同一个数据库的两个不同的注释信息
	 * "#/#"
	 */
	public static final String SEP_INFO_SAMEDB = "#/#";
	
	public static final String SEP_AND = "&&";
}
