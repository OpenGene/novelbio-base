package com.novelbio.base.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.PathDetail;
import com.novelbio.base.StringOperate;
import com.novelbio.jsr203.objstorage.CloudConstant;

/**
 * 服务运行环境判定.是hadoop还是阿里云
 * 
 * @author novelbio fans.fan
 *
 */
public class ServiceEnvUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(ServiceEnvUtil.class);
	
	public static final String ENV_HADOOP = "hadoop";
	public static final String ENV_ALIYUN = "aliyun";
	public static final String ENV_TENCENT = "tencent";
	
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

	public static boolean isCloudEnv() {
		return isAliyunEnv() || isTencentEnv();
	}

	public static boolean isAliyunEnv() {
		return ENV_ALIYUN.equals(env);
	}
	
	public static boolean isTencentEnv() {
		return ENV_TENCENT.equals(env);
	}
	
	private static Boolean isDbKeepAlive = null;
	
	/**
	 * 批量计算时,数据库是否保持长连接
	 * @return 是则返回true. 不是返回false
	 */
	public static boolean isDbKeepAlive() {
		if (isDbKeepAlive == null) {
			if (isBatchCompute() && StringOperate.isEqual(System.getenv(DB_KEEPALIVE), "true")) {
				isDbKeepAlive = true;
			} else if (isBatchCompute()) {
				isDbKeepAlive = false;
			} else {
				isDbKeepAlive = true;
			}
			logger.info("isDbKeepAlive=" + isDbKeepAlive);
		}
		return isDbKeepAlive;
	}
	
	private static Boolean isBatchCompute = null;

	/**
	 * 是否阿里云批量计算环境. 批量计算中会有下面这个环境变量
	 * @return 是则返回true. 不是返回false
	 */
	public static boolean isBatchCompute() {
		if (isBatchCompute == null) {
			isBatchCompute = !StringOperate.isRealNull(getJobId());
			logger.info("isBatchCompute=" + isBatchCompute);
		}
		return isBatchCompute;
	}
	
	public static String getJobId() {
		return System.getenv(CloudConstant.JobId);
	}
	
	public static void main(String[] args) {
		System.out.println(isCloudEnv());
	}
	
}
