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
	static String tmpConfFold;
	static String rworkspace;
	static String tmpPath;
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
		
		//TmpPath
		tmpPath = properties.getProperty("TMPpath");
		if (FileOperate.createFolders(tmpPath)) {
			File file = new File(tmpPath);
			file.setReadable(true, false);
			file.setWritable(true, false);
			System.setProperty("java.io.tmpdir", tmpPath);
		} else {
			tmpPath = System.getProperty("java.io.tmpdir");
		}
		
		//getTmpConfFold()
		tmpConfFold = getProjectPath() + "ConfFold" + FileOperate.getSepPath();
		if (!FileOperate.createFolders(tmpConfFold)) {
			tmpConfFold = null;
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
	
	/** 设定java的临时文件夹(本地) */
	public static void setTmpDir(String filePath) {
		if (filePath != null && filePath.equals(PathDetail.tmpPath)) {
			return;
		}
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
	/** 零时文件的文件夹 */
	public static String getTmpConfFold() {
		return tmpConfFold;
	}
	
	/**获取订单附件路径
	 * 
	 * @return
	 */
	public static String  getOrderAppendixPath() {
		return FileHadoop.getHdfsHeadSymbol() + "/nbCloud/public/order/appendix/";
	}
	
	/**获取订单附件路径
	 * 
	 * @return
	 */
	public static String  getExperimentAppendixPath() {
		return FileHadoop.getHdfsHeadSymbol() + "/nbCloud/public/experiment/";
	}
	
	
	/**获取订单附件路径
	 * 
	 * @return
	 */
	public static String  getExperimentPathByLocal() {
		return "/media/hdfs/nbCloud/public/experiment/";
	}
	/**
	 * 获取当前合同的附件文件路径
	 * @return
	 */
	public static String getHDFSTmpFold() {
		String hdfsTmpPath = FileHadoop.getHdfsHeadSymbol() + "/nbCloud/public/contract/appendix/";
		return hdfsTmpPath;
	}
	
	public static String getRworkspace() {
		return rworkspace;
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
	
	
	/** 内部自动加空格 */
	public static String getRscript() {
		return properties.getProperty("R_SCRIPT");
	}
	/** 一个大的能容纳一些中间过程的文件夹 */
	public static String getTmpPath() {
		return tmpPath;
	}
	
	/** 在tmp文件夹下新建一个随机文件名的临时文件夹，注意每次返回的都不一样 */
	public static String getTmpPathRandom() {
		String tmpPath = FileOperate.addSep(getTmpPath()) + "tmp" + DateUtil.getDateAndRandom();
		if (!FileOperate.createFolders(tmpPath)) {
			tmpPath = null;
		}
		return tmpPath;
	}
	
	/**
	 * 得到hdfs上所有project保存的路径
	 * @return
	 */
	public static String getProjectSavePath() {
		return properties.getProperty("allProjectSavePath");
	}
	
	/**
	 * 取得公共文件保存文件夹
	 * @return
	 */
	public static String getPublicFileSavePath() {
		return properties.getProperty("publicFileSavePath");
	}
	
	/** 末尾没有"/" */
	public static String getSampleQCpath() {
		return properties.getProperty("sampleQCPath");
	}
	/** 末尾有"/" */
	public static String getSampleQCpathWithSep() {
		return properties.getProperty("sampleQCPath") + FileOperate.getSepPath();
	}
	/**
	 * 取得无项目的任务保存文件夹
	 * @return
	 */
	public static String getTestTasksSavePath(){
		return properties.getProperty("testTasksSavePath");
	}
	
	/**
	 * 得到项目附件存放文件夹的名称，有(/)
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
	
	/**
	 * 取得项目临时文件保存目录
	 * @return
	 */
	public static String getProjectTempSavePath() {
		return properties.getProperty("projectTempFolderName");
	}
	
	/**
	 * 取得报告模板所在位置
	 * @return
	 */
	public static String getReportTempPath() {
		return properties.getProperty("reportTempPath");
	}
}
