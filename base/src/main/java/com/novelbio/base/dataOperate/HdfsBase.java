package com.novelbio.base.dataOperate;

import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;

import com.novelbio.base.PathDetail;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;

public class HdfsBase {
	public static String HEAD = "";	
	static{
		HEAD = PathDetail.getHdfsHeadPath();
		
	}
	static class HdfsBaseHolder {
		static Configuration conf;
		static {
			conf = new Configuration();
			conf.set("dfs.permissions", "false");
		}
	}
	public static boolean isHdfs(String fileName) {
		if (fileName == null || fileName.equals("")) {
			return false;
		}
		String symbol = FileHadoop.getHdfsHeadSymbol();
		fileName = fileName.toLowerCase();
		return fileName.startsWith(symbol) ? true : false;
	}
	
	public static FileSystem getFileSystem(){
		FileSystem hdfs = null;
		try {
			hdfs = FileSystem.get(URI.create(HEAD), HdfsBaseHolder.conf);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return hdfs;
	}
	/**
	 * 创建一个文件夹
	 * @param path 路径+“/”+文件夹名字
	 * @throws IOException
	 */
	public static void mkdirHDFSFolder(Path path) throws IOException {
		getFileSystem().mkdirs(path);
	}
	
	/**删除文件
	 * @throws IOException */
	public static void removeHDFSfile(Path path) throws IOException {
		getFileSystem().delete(path, true);
	}
	
	public static void main(String[] args) throws IOException {
		String fileName = "hdfs://192.168.0.104:9000/abc/aaa";
		System.out.println(FileOperate.getParentPathName(fileName));
		System.out.println(HdfsBase.HEAD);
		DistributedFileSystem hdfs = (DistributedFileSystem)HdfsBase.getFileSystem();
		DatanodeInfo[] dataNodeStats = hdfs.getDataNodeStats();
		for (DatanodeInfo dataNode : dataNodeStats) {
			System.out.println(dataNode.getHostName() + "t"
					+ dataNode.getName());
		}
	}

}
