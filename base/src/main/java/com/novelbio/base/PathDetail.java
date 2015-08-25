package com.novelbio.base;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.fileOperate.FileOperate;

public class PathDetail {
	/** 临时文件夹中的文件保留若干天 */
	static final int tmpFileRemainDay = 6;
	private static Properties properties;
	private static String rworkspace;
	private static String tmpPath;
	private static String tmpHdfsPath;
	private static String rworkspaceTmp;
	
	static {
		initial();
	}
	private static void initial() {
		String configPath = "config.properties";
		InputStream in = PathDetail.class.getClassLoader().getResourceAsStream(configPath);
		properties = new Properties();
		try {
			properties.load(in);
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		} finally{
			try {
				if(in != null){
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		tmpHdfsPath = properties.getProperty("tmpHdfsPath");
	}
	
	/** 返回jar所在的路径 */
	@Deprecated
	public static String getProjectPath() {
		java.net.URL url = PathDetail.class.getProtectionDomain().getCodeSource().getLocation();
		String filePath = null;
		try {
			filePath = HttpFetch.decode(url.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		filePath = FileOperate.getParentPathNameWithSep(filePath);
		return FileOperate.addSep(filePath);
	}
	/** 返回jar所在的路径，路径分隔符都为"/" */
	public static String getProjectPathLinux() {
		java.net.URL url = PathDetail.class.getProtectionDomain().getCodeSource().getLocation();
		String filePath = null;
		try {
			filePath = HttpFetch.decode(url.getPath());
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
			filePath = HttpFetch.decode(url.getPath());
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
			if (!FileOperate.createFolders(rworkspaceTmp)) {
				rworkspaceTmp = "";
			}
		}
		return rworkspaceTmp;
	}
	
	/** 内部自动加空格 */
	public static String getRscriptWithSpace() {
		return properties.getProperty("R_SCRIPT") + " ";
	}
	
	/** hadoop Streaming的jar包 */
	public static String getHdpStreamingJar() {
		return properties.getProperty("hadoopStreamingJar");
	}
	
	/** 内部不加空格 */
	public static String getRscript() {
		return properties.getProperty("R_SCRIPT");
	}
	/** 一个大的能容纳一些中间过程的文件夹 */
	public static String getTmpPath() {
		if (tmpPath == null) {
			synchronized (properties) {
				tmpPath = properties.getProperty("TMPpath");
				if (FileOperate.createFolders(tmpPath)) {
//					file.setReadable(true, false);
//					file.setWritable(true, false);
					System.setProperty("java.io.tmpdir", tmpPath);
				} else {
					tmpPath = System.getProperty("java.io.tmpdir");
				}
			}
		}

		return tmpPath;
	}
	
	public static void cleanTmpPath() {
		deleteFileFolder(FileOperate.getFile(tmpPath), tmpPath);
	}
	
	/**
	 * 清理临时文件夹中的过时文件
	 * @param file
	 * @param parentPath
	 * @return 文件夹是否被清空
	 */
	public static boolean deleteFileFolder(File file, String parentPath) {
		boolean isClear = true;
		if (FileOperate.isFileExist(file)) {
			long createTime = DateUtil.getNowTimeLong() - FileOperate.getTimeLastModify(file);
			if (createTime > tmpFileRemainDay * 24*3600 * 1000) {
				FileOperate.delFile(file);
				return true;
			} else {
				return false;
			}
		} else if (FileOperate.isFileDirectory(file)) {
			List<String> lsFile = FileOperate.getFoldFileNameLs(file.getAbsolutePath(), "*", "*");
			for (String string : lsFile) {
				File file2 = FileOperate.getFile(string);
				boolean isClearSub = deleteFileFolder(file2, parentPath);
				if (isClear && !isClearSub) {
					isClear = false;
				}
			}
			if (isClear && !file.getAbsolutePath().equals(parentPath)) {
				FileOperate.delFile(file);
				return true;
			} else {
				return false;
			}
		}
		return true;
	}
	
	/** 一个大的能容纳一些中间过程的文件夹hdfs开头 */
	public static String getTmpHdfsPath() {
		return tmpHdfsPath;
	}
	
	
	/** 在tmp文件夹下新建一个随机文件名的临时文件夹，注意每次返回的都不一样 */
	public static String getTmpPathRandom() {
		String tmpPath = FileOperate.addSep(getTmpPath()) + "tmp" + DateUtil.getDateAndRandom();
		if (!FileOperate.createFolders(tmpPath)) {
			tmpPath = null;
		}
		return tmpPath;
	}
	
	/** 在tmp文件夹下新建一个随机文件名的临时文件夹，注意每次返回的都不一样，最后有“/” */
	public static String getTmpPathRandomWithSep(String prefix) {
		String tmpPath = FileOperate.addSep(getTmpPath()) + prefix + DateUtil.getDateAndRandom() + FileOperate.getSepPath();
		if (!FileOperate.createFolders(tmpPath)) {
			tmpPath = null;
		}
		return tmpPath;
	}
	
	/** 在tmp文件夹下新建一个随机文件名的临时文件夹，注意每次返回的都不一样，最后有“/” */
	public static String getRandomWithSep(String tmpPath, String prefix) {
		String tmpPathResult = FileOperate.addSep(tmpPath) + prefix + DateUtil.getDateAndRandom() + FileOperate.getSepPath();
		if (!FileOperate.createFolders(tmpPathResult)) {
			tmpPathResult = null;
		}
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
				if (!FileOperate.createFolders(rworkspace)) {
					rworkspace = null;
				}
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
		return properties.getProperty("hdfs-xml");
	}
	public static String getHdpCoreXml() {
		return properties.getProperty("hdfs-core-xml");
	}
	public static String getHdpYarnXml() {
		return properties.getProperty("yarn-xml");
	}
	public static String getHdpHdfsHeadSymbol() {
		return properties.getProperty("hdfsHeadSymbol");
	}
	
	public static String getLogoPath() {
		return properties.getProperty("logoImgPath");
	}
	
	//=========   zookeeper ===========================
	
	/** 连接到zookeeper server的site */
	public static String getZookeeperServerSite() {
		return properties.getProperty("zookeeperSite");
	}
	/** 连接到zookeeper server的site */
	public static String getZookeeperLock() {
		return properties.getProperty("znodeLock");
	}
}
