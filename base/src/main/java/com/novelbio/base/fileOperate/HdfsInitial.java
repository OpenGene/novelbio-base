package com.novelbio.base.fileOperate;

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
		conf = new Configuration();
		conf.addResource(new Path( PathDetail.getHdpHdfsXml()));
		conf.addResource(new Path(PathDetail.getHdpCoreXml()));
		
		conf.set("dfs.permissions.enabled", "false");
		conf.set("dfs.client.block.write.replace-datanode-on-failure.policy", "NEVER"); 
		conf.set("dfs.client.block.write.replace-datanode-on-failure.enable", "true"); 
		conf.setBoolean( "dfs.support.append", true );

		try {
			fsHDFS = FileSystem.get(conf);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("cannot initial hadoop fs", e);
        }
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
	
	/**
	 * 返回设定的文件系统
	 * @return
	 */
	public static FileSystem getFileSystem() {
		return fsHDFS;
	}

}
