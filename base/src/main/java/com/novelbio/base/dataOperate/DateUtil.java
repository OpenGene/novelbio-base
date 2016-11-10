package com.novelbio.base.dataOperate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * 时间日期类
 * 可以用来计算程序运行时间
 * @author zong0jie
 *
 */
public class DateUtil {
	enum Week {
		   星期一,星期二,星期三,星期四,星期五,星期六,星期日  
		}  
	/**
	 * 年-月-日
	 */
	public static final String PATTERN_YYYY_MM_DD = "yyyy-MM-dd";

	/** 年月日 */
	public static final String PATTERN_YYYYMMDD = "yyyyMMdd";
	/** 年2位月日 */
	public static final String PATTERN_YYMMDD = "yyMMdd";

	/** 当前日期和时间 */
	public static final String PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss";
	
	/** 年月 yyyy-MM */
	public static final String PATTERN_YYYYMM = "yyyy-MM";
	/** 当前日期的年份 */
	public static final String PATTERN_YYYY = "yyyy";
	
	/** 当前日期的月份 */
	public static final String PATTERN_MM = "MM";
	/**
	 * 年-月-日
	 */
	public static final String PATTERN_hhmm = "hh_mm";
	
	long start = 0;
	/**
	 * 自动设定起始时间
	 */
	public DateUtil() {
		start = System.currentTimeMillis(); //获取最初时间
	}
	/**
	 * 设定起始时间
	 */
	public void setStartTime() {
		start = System.currentTimeMillis(); //获取最初时间
	}
	
	public static long getNowTimeLong() {
		return new Date().getTime();
	}
	
	public static String getNowTimeLongRandom() {
		return new Date().getTime() + "_" + getRandomAbs();
	}
	
	/**
	 * 获取当前时间.格式yyyy-MM-dd HH:mm:ss
	 * @date 2015年9月14日
	 * @return
	 */
	public static String getNowTimeStr() {
		return DateUtil.date2String(new Date(), DateUtil.PATTERN_DATETIME);
	}
	

	
	/**
	 * 从设定starttime开始返回运行时间，单位ms
	 * @return
	 */
	public long getElapseTime() {
		long end=System.currentTimeMillis(); //获取运行结束时间
		return end-start; 
	}
	
	/**
	 * 把date类型的日期转换成String
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String date2String(Date date,String pattern) {
		SimpleDateFormat sf = new SimpleDateFormat(pattern);
		return sf.format(date);
	}
	/**
	 * 把long类型的日期转换成String
	 * @param dateLong
	 * @param pattern
	 * @return
	 */
	public static String date2String(long dateLong, String pattern) {
		Date date = new Date(dateLong);
		return date2String(date, pattern);
	}
	
//	/**
//	 * 
//	 * 描述:把日历弄的日期转成String <br>
//	 * 作者 : gaozhu <br>
//	 * 日期 : 2012-11-16 <br>
//	 * 参数: 
//	 * 返回值 String
//	 * 异常
//	 */
//	public static String calendar2String(Calendar calendar){
//		return calendar.get(Calendar.YEAR)+"-"
//				+calendar.get(Calendar.MONTH)+"-"
//				+(calendar.get(Calendar.DATE)<10?("0"+calendar.get(Calendar.DATE)):(calendar.get(Calendar.DATE)));
//	}
	
	/**
	 * String转date
	 * 传入yyyy-MM-dd格式,返回的long型时间 是0时0分0秒 
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static Date string2Date(String date,String pattern) {
		try {
			SimpleDateFormat sf = new SimpleDateFormat(pattern);
			Date newdate;
			newdate = sf.parse(date);
			return newdate;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * long值转date
	 * 
	 * @date 2016年7月31日
	 * @author novelbio fans.fan
	 * @param date
	 * @return
	 */
	public static Date Long2Date(long date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date);
		return calendar.getTime();
	}
	
	
	
	/**
	 * String转Long
	 * @param date
	 * @param pattern 格式类似：yyyy-MM-dd HH:mm:ss，注意大小写
	 * @return
	 */
	public static Long string2DateLong(String date,String pattern){
		Date newDate = string2Date(date,pattern);
		if (newDate != null) {
			return newDate.getTime();
		}
		return null;
	}
	
	public static boolean isWeek(String week){
		for(int i=0;i<Week.values().length;i++){
			if(week.equals(Week.values()[i].toString())){
				return true;
			}
		}
		return false;
	}
	/**
	 * 返回当前日期，格式 "yyyy-MM-dd"
	 * @return
	 */
	public static String getDate() {
	     SimpleDateFormat formatDate= new SimpleDateFormat(PATTERN_YYYY_MM_DD);
	     Date currentDate = new Date(); //得到当前系统时间
	     return formatDate.format(currentDate); //将日期时间格式化
	}
	/**
	 * 返回当前日期，格式 "yyyy-MM-dd"
	 * @return
	 */
	public static String getDate(String pattern) {
	     SimpleDateFormat formatDate= new SimpleDateFormat(pattern);
	     Date currentDate = new Date(); //得到当前系统时间
	     return formatDate.format(currentDate); //将日期时间格式化
	}
	/**
	 * 返回当前日期，格式“yyyyMMddHHmmssSSS”
	 * @return
	 */
	public static String getDateMSAndRandom() {
		SimpleDateFormat formatDate = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		Date currentDate = new Date();
		String date = formatDate.format(currentDate);
		return date + "_" + getRandomAbs();
	}
	
	/**
	 * 返回当前日期加上一个随机数，做唯一文件编码用，格式 "yyyy-MM-ddhhss"
	 * @return
	 */
	public static String getDateAndRandom() {
	     SimpleDateFormat formatDate= new SimpleDateFormat( "yyyy_MM_dd_hh_mm_ss");
	     Date currentDate = new Date(); //得到当前系统时间
	     String date = formatDate.format(currentDate); //将日期时间格式化
	     return date + "_" + getRandomAbs();
	}
	
	/** 获得随机的正数，不用负数的原因是会在tmp路径中加入 - ，类似 2016_10_29_12_31_23_-2345
	 * 部分生物信息算法会根据 - 进行截取，然后会报错 */
	private static int getRandomAbs() {
		  Random random = new Random(System.currentTimeMillis());
		  int tmp = (short)random.nextInt();
		  tmp = Math.abs(tmp);
		  return tmp;
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
	 * 返回当前日期，格式 "YYYY"
	 * @return
	 */
	public static String getDateYear() {
	     SimpleDateFormat formatDate= new SimpleDateFormat( "yyyy");
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
	
	/**
	 * 计算两个日期之间相差的天数
	 * 
	 * @param smdate　较小的时间
	 * @param bdate　较大的时间
	 * @return 相差天数
	 */
	public static int daysBetween(Date smdate, Date bdate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(smdate);
		long time1 = cal.getTimeInMillis();
		cal.setTime(bdate);
		long time2 = cal.getTimeInMillis();
		long between_days = (time2 - time1) / (1000 * 3600 * 24);

		return Integer.parseInt(String.valueOf(between_days));
	}
	
}
