package com.novelbio.base;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;

public class PathDetail {
	/** 临时文件夹中的文件保留若干天 */
	static final int tmpFileRemainDay = 6;
	static Properties properties;
	static String rworkspace;
	static String tmpPath;
	static String tmpHdfsPath;
	static String rworkspaceTmp;
	
	static {
		initial();
	}
	private static void initial() {
		String configPath = FileOperate.isWindows() ? "configWindows.properties" : "config.properties";
		InputStream in = PathDetail.class.getClassLoader().getResourceAsStream(configPath);
		properties = new Properties();
		try {
			properties.load(in);
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally{
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		tmpHdfsPath = properties.getProperty("tmpHdfsPath");
		//TmpPath
		tmpPath = properties.getProperty("TMPpath");
		if (FileOperate.createFolders(tmpPath)) {
//			file.setReadable(true, false);
//			file.setWritable(true, false);
			System.setProperty("java.io.tmpdir", tmpPath);
		} else {
			tmpPath = System.getProperty("java.io.tmpdir");
		}
		
		//getRworkspace()
		rworkspace = null;
		String path = properties.getProperty("Rworkspace");
		if (path != null && !path.equals("")) {
			rworkspace = path + FileOperate.getSepPath();
		} else {
			rworkspace = getProjectPath() + "rscript"  + FileOperate.getSepPath();
		}
		if (!FileOperate.createFolders(rworkspace)) {
			rworkspace = null;
		}
		
		rworkspaceTmp = getRworkspace() + "tmp"  + FileOperate.getSepPath();
		if (!FileOperate.createFolders(rworkspaceTmp)) {
			rworkspaceTmp = null;
		}
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
		return rworkspaceTmp;
	}
	/** 内部自动加空格 */
	public static String getRscriptWithSpace() {
		return properties.getProperty("R_SCRIPT") + " ";
	}
	
	/** 内部不加空格 */
	public static String getRscript() {
		return properties.getProperty("R_SCRIPT");
	}
	/** 一个大的能容纳一些中间过程的文件夹 */
	public static String getTmpPath() {
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

	/**
	 * 取得报告模板所在位置
	 * @return
	 */
	public static String getReportTempPath() {
		return properties.getProperty("reportTemplatePath");
	}
	
	public static String getRworkspace() {
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

}
