package com.novelbio.base.dataOperate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;

public class HdfsBase {
	public static FileSystem getFileSystem(){
		InputStream in = HdfsBase.class.getClassLoader().getResourceAsStream("hdfs.properties");
		Properties p = new Properties();
		try {
			p.load(in);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Configuration conf = new Configuration();
		// 在你的文件地址前自动添加：hdfs://192.168.0.188:9000/
		conf.set("fs.default.name", p.getProperty("defaultName"));
		// 指定用户名
		conf.set("hadoop.job.user", p.getProperty("user"));
		FileSystem hdfs = null;
		try {
			hdfs = FileSystem.get(conf);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return hdfs;
	}
	
	
	public static void main(String[] args) throws IOException {
		DistributedFileSystem hdfs = (DistributedFileSystem)HdfsBase.getFileSystem();
		DatanodeInfo[] dataNodeStats = hdfs.getDataNodeStats();
		for (DatanodeInfo dataNode : dataNodeStats) {
			System.out.println(dataNode.getHostName() + "t"
					+ dataNode.getName());
		}
	}

}
