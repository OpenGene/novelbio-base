package com.novelbio.base;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.HttpFetch;
import com.novelbio.base.fileOperate.FileHadoop;
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
		String tmp = getTmpPath();
		if (FileOperate.isFileDirectory(tmp)) {
			System.setProperty("java.io.tmpdir", tmp);
			setTmpDir(tmp);
		}
	}
	
	/** 设定java的临时文件夹(本地) */
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
	/** 零时文件的文件夹，没有就创建一个(本地) */
	public static String getTmpConfFold() {
		String fold = getProjectPath() + "ConfFold" + FileOperate.getSepPath();
		FileOperate.createFolders(fold);
		return fold;
	}
	
	public static String getHDFSTmpFold() {
		String hdfsTmpPath = FileHadoop.getHdfsHeadSymbol() + "/nbCloud/public/contract/appendix/";
		return hdfsTmpPath;
	}
	
	public static String getRworkspace() {
		String rworkspace = null;
		String path = properties.getProperty("Rworkspace");
		if (path != null && !path.equals("")) {
			rworkspace = path + FileOperate.getSepPath();
		} else {
			rworkspace = getProjectPath() + "rscript"  + FileOperate.getSepPath();
		}
		FileOperate.createFolders(rworkspace);
		return rworkspace;
	}
	
	/** 
	 * 文件最后带"/"<br>
	 * 如果没有该文件夹就会自动创建一个
	 * @return
	 */
	public static String getRworkspaceTmp() {
		String file = getRworkspace() + "tmp"  + FileOperate.getSepPath();
		FileOperate.createFolders(file);
		return file;
	}
	/** 内部自动加空格 */
	public static String getRscriptWithSpace() {
		return properties.getProperty("R_SCRIPT") + " ";
	}
	
	
	/** 内部自动加空格 */
	public static String getRscript() {
		return properties.getProperty("R_SCRIPT");
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
	
	/**
	 * 得到hdfs上所有project保存的路径
	 * @return
	 */
	public static String getProjectSavePath() {
		return properties.getProperty("allProjectSavePath");
	}
	
	/** 
	 * 用{@link com.novelbio.base.fileOperate.FileHadoop#getHdfsHeadSymbol()}替换<br>
	 * 文件名前添加的HDFS的头，末尾没有"/" */
	@Deprecated
	public static String getHdfsHeadSymbol() {
		return properties.getProperty("hdfsHeadSymbol");
	}
	
	/** 
	 * 用{@link com.novelbio.base.fileOperate.FileHadoop#addHdfsHeadSymbol(path)}替换<br>
	 * 在输入的文件名前添加的HDFS的头<br>
	 * <b>务必输入绝对路径，也就是要以"/"开头</b>
	 * @param path
	 * @return
	 */
	@Deprecated
	public static String addHdfsHeadSymbol(String path) {
		return properties.getProperty("hdfsHeadSymbol") + path;
	}
	
	/** 
	 * 用{@link com.novelbio.base.fileOperate.FileHadoop#getHdfsHeadPath()}替换<br>
	 * hadoop实际的hdfs前缀，末尾没有"/" */
	@Deprecated
	public static String getHdfsHeadPath() {
		return properties.getProperty("hdfsHead");
	}
	
	/** 
	 * 用{@link com.novelbio.base.fileOperate.FileHadoop#getHdfsLocalPath()}替换<br>
	 * hdfs挂载在本地硬盘的路径 */
	@Deprecated
	public static String getHdfsLocalPath() {
		return properties.getProperty("hdfsLocalPath");
	}
	
	/**
	 * 取得公共文件保存文件夹
	 * @return
	 */
	public static String getPublicFileSavePath() {
		return properties.getProperty("publicFileSavePath");
	}
	
	/**
	 * 取得无项目的任务保存文件夹
	 * @return
	 */
	public static String getTestTasksSavePath(){
		return properties.getProperty("testTasksSavePath");
	}
	
	/**
	 * 得到项目附件存放文件夹的名称
	 * @return
	 */
	public static String getProjectAttachFolderName() {
		return properties.getProperty("projectAttachFolderName");
	}
	
	/**
	 * 取得nbcFile的开头形式
	 * @return
	 */
	public static String getNBCFileHead() {
		return properties.getProperty("nbcFileHead");
	}
	
	/**
	 * 取得project的rawData的存放路径
	 * @return
	 */
	public static String getRawDataFolderPath() {
		return properties.getProperty("rawDataFolderPath");
	}
	/**
	 * 取得task的rawData的存放文件名称
	 * @return
	 */
	public static String getTaskRawDataFolderName() {
		return properties.getProperty("taskRawDataFolderName");
	}
}
