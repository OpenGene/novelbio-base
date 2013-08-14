package com.novelbio.base;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.fileOperate.FileOperate;

public class PathDetail {
	static Properties properties;
	static {
		initial();
	}
	private static void initial() {
		InputStream in = PathDetail.class.getClassLoader().getResourceAsStream("config.properties");
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
		System.setProperty("java.io.tmpdir", getTmpPath()); 
	}
	
	/** 设定java的临时文件夹 */
	public static void setTmpDir(String filePath) {
		File f = new File(filePath);
        if (!f.exists()) f.mkdirs();
        f.setReadable(true, false);
        f.setWritable(true, false);
        System.setProperty("java.io.tmpdir", f.getAbsolutePath()); // in loop so that last one takes effect
	}
	
	/** 返回jar所在的路径 */
	public static String getProjectPath() {
		java.net.URL url = PathDetail.class.getProtectionDomain().getCodeSource().getLocation();
		String filePath = null;
		try {
			filePath = HttpFetch.decode(url.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		filePath = FileOperate.getParentPathName(filePath);
		return FileOperate.addSep(filePath);
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
	/** 返回jar所在的路径，路径分隔符都为"/" */
	public static String getProjectPathLinux() {
		java.net.URL url = PathDetail.class.getProtectionDomain().getCodeSource().getLocation();
		String filePath = null;
		try {
			filePath = HttpFetch.decode(url.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		filePath = FileOperate.getParentPathName(filePath);
		return FileOperate.addSep(filePath).replace("\\", "/");
	}
	/** 零时文件的文件夹，没有就创建一个 */
	public static String getTmpConfFold() {
		String fold = getProjectPath() + "ConfFold" + FileOperate.getSepPath();
		FileOperate.createFolders(fold);
		return fold;
	}
	
	public static String getRworkspace() {
		String rworkspace = getProjectPath() + "rscript"  + FileOperate.getSepPath();
		FileOperate.createFolders(rworkspace);
		return rworkspace;
	}

	public static String getRworkspaceTmp() {
		return getRworkspace() + "tmp"  + FileOperate.getSepPath();
	}
	/** 内部自动加空格 */
	public static String getRscript() {
		return properties.getProperty("R_SCRIPT") + " ";
	}
	/** 一个大的能容纳一些中间过程的文件夹 */
	public static String getTmpPath() {
		String tmpPath = properties.getProperty("TMPpath");
		FileOperate.createFolders(tmpPath);
		return tmpPath;
	}
	/** 在tmp文件夹下新建一个随机文件名的临时文件夹 */
	public static String getTmpPathRandom() {
		String tmpPathRandom = FileOperate.addSep(getTmpPath()) + "tmp" + DateUtil.getDateAndRandom();
		FileOperate.createFolders(tmpPathRandom);
		return tmpPathRandom;
	}
	public static String getHdfsHead() {
		return properties.getProperty("hdfsHead");
	}
}
