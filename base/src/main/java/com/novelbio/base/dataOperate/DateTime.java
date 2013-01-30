package com.novelbio.base.dataOperate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.omg.CosNaming._BindingIteratorImplBase;
/**
 * 时间日期类
 * 可以用来计算程序运行时间
 * @author zong0jie
 *
 */
public class DateTime {
	long start = 0;
	/**
	 * 自动设定起始时间
	 */
	public DateTime() {
		start = System.currentTimeMillis(); //获取最初时间
	}
	/**
	 * 设定起始时间
	 */
	public void setStartTime() {
		start = System.currentTimeMillis(); //获取最初时间
	}
	
	/**
	 * 从设定starttime开始返回运行时间，单位ms
	 * @return
	 */
	public long getEclipseTime() {
		long end=System.currentTimeMillis(); //获取运行结束时间
		return end-start; 
	}
	
	/**
	 * 返回当前日期，格式 "yyyy-MM-dd"
	 * @return
	 */
	public static String getDate() {
	     SimpleDateFormat formatDate= new SimpleDateFormat( "yyyy-MM-dd");
	     Date currentDate = new Date(); //得到当前系统时间
	     return formatDate.format(currentDate); //将日期时间格式化
	}
	/**
	 * 返回当前日期加上一个随机数，做唯一文件编码用，格式 "yyyy-MM-ddhhss"
	 * @return
	 */
	public static String getDateAndRandom() {
	     SimpleDateFormat formatDate= new SimpleDateFormat( "yyyy-MM-ddhhss");
	     Date currentDate = new Date(); //得到当前系统时间
	     String date = formatDate.format(currentDate); //将日期时间格式化
	     Random random = new Random(System.currentTimeMillis());
	     short Tmp = (short)random.nextInt();
	     return date + Tmp;
	}
	/**
	 * 返回当前日期，格式 "yyyy-MM-dd-hh-mm-ss"
	 * @return
	 */
	public static String getDateDetail() {
	     SimpleDateFormat formatDate= new SimpleDateFormat( "yyyy-MM-dd-hh-mm-ss");
	     Date currentDate = new Date(); //得到当前系统时间
	     return formatDate.format(currentDate); //将日期时间格式化
	}
	/**
	 * 返回当前日期，格式 "yyyy-MM"
	 * @return
	 */
	public static String getDateMM() {
	     SimpleDateFormat formatDate= new SimpleDateFormat( "yyyy-MM");
	     Date currentDate = new Date(); //得到当前系统时间
	     return formatDate.format(currentDate); //将日期时间格式化
	}
}
