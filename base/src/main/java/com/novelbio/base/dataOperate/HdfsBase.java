package com.novelbio.base.dataOperate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;

public class HdfsBase {
	public static String HEAD = "";
	private static Configuration conf;
	
	static{ 
		InputStream in = HdfsBase.class.getClassLoader().getResourceAsStream("hdfs.properties");
		Properties properties = new Properties();
		try {
			properties.load(in);
			HEAD = properties.getProperty("defaultName");
			conf = new Configuration();
			// 在你的文件地址前自动添加：hdfs://192.168.0.188:9000/
			conf.set("fs.default.name", properties.getProperty("defaultName"));
			// 指定用户名
			conf.set("hadoop.job.user", properties.getProperty("user"));
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally{
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	} 
	
	
	public static FileSystem getFileSystem(){
		FileSystem hdfs = null;
		try {
			hdfs = FileSystem.get(conf);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return hdfs;
	}
	
	
	public static void main(String[] args) throws IOException {
		Path path = new Path("hdfs://192.168.0.104:9000/abc/aaa.txt");
		System.out.println(HdfsBase.HEAD);
		DistributedFileSystem hdfs = (DistributedFileSystem)HdfsBase.getFileSystem();
		DatanodeInfo[] dataNodeStats = hdfs.getDataNodeStats();
		for (DatanodeInfo dataNode : dataNodeStats) {
			System.out.println(dataNode.getHostName() + "t"
					+ dataNode.getName());
		}
	}

}
