package com.novelbio.base;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.fileOperate.FileOperate;

public class PathDetail {
	/** 临时文件夹中的文件保留若干天 */
	static final int tmpFileRemainDay = 6;
	public static Properties properties;
	private static String rworkspace;
	private static String tmpPath;
	private static String tmpHdfsPath;
	private static String rworkspaceTmp;
	private static String hadoophome;
	private static String hadoopstreaming = null;

	static {
		initial();
	}
	private static void initial() {
		String configPath = "configbase.properties";
		InputStream in = PathDetail.class.getClassLoader().getResourceAsStream(configPath);
		properties = new Properties();
		try {
			properties.load(in);
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		} finally{
			FileOperate.close(in);
		}
		tmpHdfsPath = properties.getProperty("tmpHdfsPath");
		
		hadoophome = System.getenv("HADOOP_HOME");
		if (hadoophome != null) {
			hadoophome = FileOperate.addSep(hadoophome);
		} else {
			//TODO 需要在docker中把HADOOP_HOME都配好
			hadoophome = "/home/novelbio/software/hadoop/";
		}
		
	}
	
	/** 返回jar所在的路径 */
	@Deprecated
	public static String getProjectPath() {
		java.net.URL url = PathDetail.class.getProtectionDomain().getCodeSource().getLocation();
		String filePath = null;
		try {
			filePath = StringOperate.decode(url.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		filePath = FileOperate.getParentPathNameWithSep(filePath);
		return FileOperate.addSep(filePath);
	}
	/** 返回jar所在的路径，路径分隔符都为"/"，以 "/"结尾 */
	public static String getProjectPathLinux() {
		java.net.URL url = PathDetail.class.getProtectionDomain().getCodeSource().getLocation();
		String filePath = null;
		try {
			filePath = StringOperate.decode(url.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		filePath = FileOperate.getParentPathNameWithSep(filePath);
		return FileOperate.addSep(filePath).replace("\\", "/");
	}
	
	/** 返回jar内部路径 */
	public static String getProjectPathInside() {
		java.net.URL url = PathDetail.class.getProtectionDomain().getCodeSource().getLocation();
		String filePath = null;
		try {
			filePath = StringOperate.decode(url.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return FileOperate.addSep(filePath);
	}

	/** 
	 * 文件最后带"/"<br>
	 * @return
	 */
	public static String getRworkspaceTmp() {
		if(rworkspaceTmp == null) {
			rworkspaceTmp = getRworkspace() + "tmp"  + File.separator;
			FileOperate.createFolders(rworkspaceTmp);
		}
		return rworkspaceTmp;
	}
	
	/** 内部自动加空格 */
	public static String getRscriptWithSpace() {
		return properties.getProperty("R_SCRIPT") + " ";
	}
	
	/** hadoop Streaming的jar包 */
	public static String getHdpStreamingJar() {
		if (hadoopstreaming == null) {
			List<String> lsHadoopStreaming = FileOperate.getLsFoldFileName(hadoophome + "share/hadoop/tools/lib", "hadoop-streaming", "jar");
			if (lsHadoopStreaming.isEmpty()) {
				throw new RuntimeException("cannot find hadoopStreaming jar");
			}
			hadoopstreaming = lsHadoopStreaming.get(0);
		}
		return hadoopstreaming;
	}
	
	/** 内部不加空格 */
	public static String getRscript() {
		return properties.getProperty("R_SCRIPT");
	}
	
	/** 一个大的能容纳一些中间过程的文件夹 */
	public static String getTmpPathWithOutSep() {
		if (tmpPath == null) {
			synchronized (properties) {
				tmpPath = properties.getProperty("TMPpath");
				try {
					FileOperate.createFolders(tmpPath);
				} catch (Exception e) {
					tmpPath = System.getProperty("java.io.tmpdir");
                }
			}
		}

		return tmpPath;
	}
	
	/** 一个大的能容纳一些中间过程的文件夹 */
	public static String getTmpPathWithSep() {
		return FileOperate.addSep(getTmpPathWithOutSep());
	}
	
	public static void cleanTmpPath() {
		deleteFileFolder(FileOperate.getPath(tmpPath), tmpPath);
	}
	
	/**
	 * 清理临时文件夹中的过时文件
	 * @param file
	 * @param parentPath
	 * @return 文件夹是否被清空
	 */
	private static void deleteFileFolder(Path file, String parentPath) {
		parentPath = FileOperate.getAbsolutePath(parentPath);
		boolean isClear = true;
		if (FileOperate.isFileExistAndNotDir(file)) {
			long createTime = DateUtil.getNowTimeLong() - FileOperate.getTimeLastModify(file);
			if (createTime > tmpFileRemainDay * 24*3600 * 1000) {
				FileOperate.deleteFileFolder(file);
			}
		} else if (FileOperate.isFileDirectory(file)) {
			List<String> lsFile = FileOperate.getLsFoldFileName(file);
			for (String string : lsFile) {
				Path file2 = FileOperate.getPath(string);
				deleteFileFolder(file2, parentPath);
			}
			if (isClear && !FileOperate.getAbsolutePath(file).equals(parentPath)) {
				FileOperate.deleteFileFolder(file);
			}
		}
		return;
	}
	
	/** 一个大的能容纳一些中间过程的文件夹hdfs开头 */
	public static String getTmpHdfsPath() {
		return tmpHdfsPath;
	}
	
	/** 在tmp文件夹下新建一个随机文件名的临时文件夹，注意每次返回的都不一样，最后有“/” */
	public static String getTmpPathRandom() {
		String tmpPath = getTmpPathWithSep() + "tmp" + DateUtil.getDateAndRandom() + FileOperate.getSepPath();
		FileOperate.createFolders(tmpPath);
		return tmpPath;
	}
	
	/** 在tmp文件夹下新建一个随机文件名的临时文件夹，注意每次返回的都不一样，最后有“/” */
	public static String getTmpPathRandomNotCreate() {
		String tmpPath = getTmpPathWithSep() + "tmp" + DateUtil.getDateAndRandom() + FileOperate.getSepPath();
		return tmpPath;
	}
	
	/** 在tmp文件夹下新建一个随机文件名的临时文件夹，注意每次返回的都不一样，最后有“/” */
	public static String getTmpPathRandomWithSep(String prefix) {
		String tmpPath = getTmpPathWithSep() + prefix + DateUtil.getDateAndRandom() + FileOperate.getSepPath();
		FileOperate.createFolders(tmpPath);
		return tmpPath;
	}
	
	/** 在tmp文件夹下新建一个随机文件名的临时文件夹，注意每次返回的都不一样，最后有“/” */
	public static String getRandomWithSep(String tmpPath) {
		return getRandomWithSep(tmpPath, "");
	}
	/** 在tmp文件夹下新建一个随机文件名的临时文件夹，注意每次返回的都不一样，最后有“/” */
	public static String getRandomWithSep(String tmpPath, String prefix) {
		String tmpPrefix = StringOperate.isRealNull(prefix)? "" : "_" + prefix;
		String tmpPathResult = FileOperate.addSep(tmpPath) + DateUtil.getDateAndRandom() + tmpPrefix + FileOperate.getSepPath();
		FileOperate.createFolders(tmpPath);
		return tmpPathResult;
	}
	
	/**
	 * 取得报告模板所在位置
	 * @return
	 */
	public static String getReportTempPath() {
		return properties.getProperty("reportTemplatePath");
	}
	
	public static String getRworkspace() {
		if (rworkspace == null) {
			synchronized (properties) {
				String path = properties.getProperty("Rworkspace");
				if (path != null && !path.equals("")) {
					rworkspace = path + File.separator;
				} else {
					rworkspace = getProjectPath() + "rscript"  + File.separator;
				}
				FileOperate.createFolders(rworkspace);
			}
		}
		
		return rworkspace;
	}
	/** 取得hdfs挂载的本地文件夹路径 */
	public static String getHdfsLocalPath() {
		return properties.getProperty("hdfsLocalPath");
	}
	/**
	 * 取得nbcFile的开头形式
	 * @return
	 */
	public static String getNBCFileHead() {
		return properties.getProperty("nbcFileHead");
	}
	
	public static String getHdpHdfsXml() {
		return hadoophome + "etc/hadoop/hdfs-site.xml";
	}
	public static String getHdpCoreXml() {
		return hadoophome + "etc/hadoop/core-site.xml";
	}
	public static String getHdpYarnXml() {
		return hadoophome + "etc/hadoop/yarn-site.xml";
	}
	public static String getHdpHdfsHeadSymbol() {
		return properties.getProperty("hdfsHeadSymbol");
	}
	public static String getLogoPath() {
		return properties.getProperty("logoImgPath");
	}
	public static String getHadoophome() {
		return hadoophome;
	}
	//=========  zookeeper  ===========================
	/** 连接到zookeeper server的site */
	public static String getZookeeperServerSite() {
		return properties.getProperty("zookeeperSite");
	}
	/** 连接到zookeeper server的site */
	public static String getZookeeperLock() {
		return properties.getProperty("znodeLock");
	}
	
	//=========  Docker  ===========================
	/** docker repository所在的ip和端口，末尾没有 "/" */
	public static String getDockerRepoIpPort() {
		return properties.getProperty("dockerRepositoryIp");
	}

	/** docker远程调用的端口号 */
	public static String getDockerRemotePort() {
		return properties.getProperty("dockerRemotePort");
	}
	
	//========== ssh ========================
	public static String getSshName() {
		return properties.getProperty("sshName");
	}
	public static String getSshIdrsaFile() {
		return properties.getProperty("sshidrsa");
	}

	public static String getEnvName() {
		return properties.getProperty("env");
	}

	/**
	 * redis服务器端的ip地址.端口走6379未配置
	 * 
	 * @return
	 */
	public static String getRedisServerIp() {
		return properties.getProperty("redisServerIp");
	}
}
