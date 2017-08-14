package com.novelbio.base.util;

import com.novelbio.base.PathDetail;
import com.novelbio.base.StringOperate;

/**
 * 服务运行环境判定.是hadoop还是阿里云
 * 
 * @author novelbio fans.fan
 *
 */
public class ServiceEnvUtil {
	
	public static final String ENV_HADOOP = "hadoop";
	public static final String ENV_ALIYUN = "aliyun";
	public static final String DB_KEEPALIVE = "dbKeepAlive";
	private static String env;
	
	static{
		init();
	}
	
	private static void init() {
		env = PathDetail.getEnvName();
	}
	
	public static void setEnv(String env) {
		ServiceEnvUtil.env = env;
	}
	
	public static boolean isHadoopEnvRun() {
		return ENV_HADOOP.equals(env);
	}


	public static boolean isAliyunEnv() {
		return ENV_ALIYUN.equals(env);
	}
	
	/**
	 * 批量计算时,数据库是否保持长连接
	 * @return 是则返回true. 不是返回false
	 */
	public static boolean isDbKeepAlive() {
		return isBatchCompute() && StringOperate.isEqual(System.getenv(DB_KEEPALIVE), "true");
	}

	/**
	 * 是否阿里云批量计算环境. 批量计算中会有下面这个环境变量
	 * @return 是则返回true. 不是返回false
	 */
	public static boolean isBatchCompute() {
		return !StringOperate.isRealNull(System.getenv("BATCH_COMPUTE_OSS_HOST"));
	}
	
	public static void main(String[] args) {
		System.out.println(isAliyunEnv());
	}
	
}
