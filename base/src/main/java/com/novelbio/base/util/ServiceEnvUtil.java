package com.novelbio.base.util;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.conf.YarnConfiguration;

import com.aliyun.oss.OSSClient;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.novelbio.base.PathDetail;
import com.novelbio.jsr203.bos.OssInitiator;
import com.novelbio.jsr203.bos.PathDetailOs;
import com.sun.tools.doclint.Env;

/**
 * 服务运行环境判定.是hadoop还是阿里云
 * 
 * @author novelbio fans.fan
 *
 */
public class ServiceEnvUtil {
	
	private static final String ENV_HADOOP = "hadoop";
	private static final String ENV_ALIYUN = "aliyun";
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
