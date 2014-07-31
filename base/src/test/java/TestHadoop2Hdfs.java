import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FsStatus;
import org.apache.hadoop.fs.Path;

import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileHadoop;


public class TestHadoop2Hdfs {
	public static void main(String[] args) {
		 Configuration conf = new Configuration();
//		  conf.set("fs.defaultFS", "hdfs://cluster1");
//		  conf.set("dfs.nameservices", "cluster1");
//		  conf.set("dfs.ha.namenodes.cluster1", "nn1,nn2");
//		  conf.set("dfs.namenode.rpc-address.cluster1.nn1", "192.168.0.180:8020");
//		  conf.set("dfs.namenode.rpc-address.cluster1.nn2", "192.168.0.181:8020");
//		  conf.set("dfs.client.failover.proxy.provider.cluster1", "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
//		  FileSystem fs = null;
//		  Path path = null;
//		  try {
//			  path = new Path("/test/chrAll.fa.fai");
//			  fs =  FileSystem.get(conf);
//			  FileStatus fsStatus = fs.getFileStatus(path);
//			  fs.exists(path);
//			  System.out.println(fs.exists(path));
////		     FileStatus[] list = fs.listStatus(new Path("/test/"));
////		     for (FileStatus file : list) {
////		       System.out.println(file.getPath().getName());
////		      }
//		  } catch (Exception e) {
//		     e.printStackTrace();
//		  } finally{
//		      try {
//		        fs.close();
//		      } catch (IOException e) {
//		        e.printStackTrace();
//		      }
//		  }
//		  InputStream is = null;
//		  try {
//			  fs =  FileSystem.get(conf);
//			  is = fs.open(new Path("/test/chrAll.fa.fai"));
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
		FileHadoop fileHadoop = new FileHadoop("/hdfs:/test/chrAll.fa.fai");
		System.out.println(fileHadoop.exists());
		TxtReadandWrite txtRead = new TxtReadandWrite("/hdfs:/test/chrAll.fa.fai");
		for (String string : txtRead.readlines()) {
			System.out.println(string);
		}
	}
}
