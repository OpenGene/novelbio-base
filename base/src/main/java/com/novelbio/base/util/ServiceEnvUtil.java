package com.novelbio.base.util;

import com.novelbio.base.PathDetail;

/**
 * 服务运行环境判定.是hadoop还是阿里云
 * 
 * @author novelbio fans.fan
 *
 */
public class ServiceEnvUtil {
	
	public static final String ENV_HADOOP = "hadoop";
	public static final String ENV_ALIYUN = "aliyun";
	private static String env;
	
	static{
		init();
	}
	
	private static void init() {
		env = PathDetail.getEnvName();
	}

	public static boolean isHadoopEnvRun() {
		return ENV_HADOOP.equals(env);
	}


	public static boolean isAliyunEnv() {
		return ENV_ALIYUN.equals(env);
	}

}
