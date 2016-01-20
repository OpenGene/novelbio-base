package com.novelbio.base.fileOperate;

import hdfs.jsr203.PathDetailHdfs;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileContext;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
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
		conf = HdfsInit.getConf();
		fsHDFS = HdfsInit.getHdfs();
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

class HdfsInit {
	private static Configuration conf;
	private static FileSystem fs;
	static {
		conf = new Configuration();
		conf.addResource(new Path( PathDetail.getHdpHdfsXml()));
		conf.addResource(new Path(PathDetail.getHdpCoreXml()));
		
		conf.set("dfs.permissions.enabled", "false");
		conf.set("dfs.client.block.write.replace-datanode-on-failure.policy", "NEVER"); 
		conf.set("dfs.client.block.write.replace-datanode-on-failure.enable", "true"); 
		try {
	        fs = FileSystem.get(conf);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("cannot initial hadoop fs", e);
        }
	}
	
	public static Configuration getConf() {
		return conf;
	}
	
	public static FileSystem getHdfs() {
		return fs;
	}
}