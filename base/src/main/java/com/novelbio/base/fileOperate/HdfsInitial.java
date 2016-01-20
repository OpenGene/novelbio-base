package com.novelbio.base.fileOperate;

import hdfs.jsr203.HdfsConfInitiator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileContext;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.UnsupportedFileSystemException;

import com.novelbio.base.PathDetail;

public class HdfsInitial {
	private static final long serialVersionUID = 1L;
		
	static FileSystem fsHDFS;
	static Configuration conf;
	static FileContext fileContext;
	static {
		initial();
	}
	
	public static void initial() {
		conf = HdfsConfInitiator.getConf();
		fsHDFS = HdfsConfInitiator.getHdfs();
		try {
			fileContext = FileContext.getFileContext(conf);
		} catch (UnsupportedFileSystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Configuration getConf() {
		return conf;
	}
	
	public static String getSymbol() {
		return PathDetail.getHdpHdfsHeadSymbol();
	}
	
	/**
	 * 返回设定的文件系统
	 * @return
	 */
	public static FileSystem getFileSystem() {
		return fsHDFS;
	}
	
	public static String getHdfsLocalPath() {
		return PathDetail.getHdfsLocalPath();
	}

}
