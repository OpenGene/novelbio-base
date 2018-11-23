package com.novelbio.base.dataOperate;

import java.text.ParseException;
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
	
	/** 当前日期和时间 */
	public static final String PATTERN_DATETIMEMM = "yyyy-MM-dd HH:mm";
	
	/** 年月 yyyy-MM */
	public static final String PATTERN_YYYYMM = "yyyy-MM";
	/** 当前日期的年份 */
	public static final String PATTERN_YYYY = "yyyy";
	
	/** 当前日期的月份 */
	public static final String PATTERN_MM = "MM";
	/**
	 * 年-月-日
	 */
	public static final String PATTERN_hhmm = "HH_mm";
	
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
	public static String date2String(Long dateLong, String pattern) {
		if (dateLong == null) {
			dateLong = System.currentTimeMillis();
		}
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
	public static Date Long2Date(Long date) {
		if (date == null) {
			date = System.currentTimeMillis();
		}
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
	     SimpleDateFormat formatDate= new SimpleDateFormat( "yyyy_MM_dd_HH_mm_ss");
	     Date currentDate = new Date(); //得到当前系统时间
	     String date = formatDate.format(currentDate); //将日期时间格式化
	     return date + "_" + getRandomAbs();
	}
	
	/** 获得随机的正数，不用负数的原因是会在tmp路径中加入 - ，类似 2016_10_29_12_31_23_-2345
	 * 部分生物信息算法会根据 - 进行截取，然后会报错 */
	private static int getRandomAbs() {
		  Random random = new Random();
		  int tmp = (short)random.nextInt();
		  tmp = Math.abs(tmp);
		  return tmp;
	}
	/**
	 * 返回当前日期，格式 "yyyy-MM-dd-hh-mm-ss"
	 * @return
	 */
	public static String getDateDetail() {
	     SimpleDateFormat formatDate= new SimpleDateFormat( "yyyy-MM-dd-HH-mm-ss");
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
	     Date currentDate = new Date(); //得到当前系统时间
	     return getDateMM(currentDate); //将日期时间格式化
	}
	
	public static String getDateMM(Date date) {
	     SimpleDateFormat formatDate= new SimpleDateFormat( "yyyy-MM");
	     return formatDate.format(date); //将日期时间格式化
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
		return daysBetween(time1, time2);
	}
	
	public static int daysBetween(long time1, long time2) {
		long between_days = (time2 - time1) / (1000 * 3600 * 24);
		return Integer.parseInt(String.valueOf(between_days));
	}
	
	/**
	 * 获取两个事件之间相差的分钟数
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public static Long getDateBetween(Date startDate, Date endDate) {
		if (startDate == null || endDate == null) {
			return 0L;
		}
		return (endDate.getTime() - startDate.getTime()) / 60_000; 
	}
	
	/**
	 * 获取date日期所在月的开始时间.即该月的1号0时0分0秒
	 * 
	 * @param date
	 * @return
	 */
	public static long getMonthStart(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1, 0, 0, 0);
		return calendar.getTimeInMillis();
	}

	/**
	 * 获取该月的结束时间.即该月最后一天的23点59分59秒
	 * 
	 * @param date
	 * @return
	 */
	public static long getMonthEnd(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int MaxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		// 按你的要求设置时间
		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), MaxDay, 23, 59, 59);
		return calendar.getTimeInMillis();
	}
	
	/**
	 * 获取一天时间的开始.即当天的0时0分0秒
	 * 
	 * @param date
	 * @return
	 */
	public static long getDayStart(Date date) {	
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		return calendar.getTimeInMillis();
	}
	
	/**
	 * 获取一天结束的时间.即当天的23点59分59秒
	 * 
	 * @param date
	 * @return
	 */
	public static long getDayEnd(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		return calendar.getTimeInMillis();
	}
	
	/**
	 *  将2018-02-07T09:29:09Z格式的转为date
	 *  
	 * @author novelbio fans.fan
	 * @date 2018年9月20日
	 * @param dateUTC
	 * @return
	 * @throws ParseException
	 */
	public static Date getUTCDate(String dateUTC) throws ParseException {
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		return sdf1.parse(dateUTC);//拿到Date对象
	}
	
	public static void main(String[] args) {
		String d = "2018-02-07T09:29:09Z";
		try {
			Date date = getUTCDate(d);
			System.out.println(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
}
