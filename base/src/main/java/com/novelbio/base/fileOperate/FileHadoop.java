package com.novelbio.base.fileOperate;

import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.Progress;
import org.apache.hadoop.util.Progressable;

public class FileHadoop {
	FileSystem fsHDFS;
	Path dst;
	
	/**
	 * 输入另一个fileHadoop的内容，仅获得其配置信息，不获得其具体文件名
	 * @param fileHadoop
	 */
	public FileHadoop(FileSystem fsHDFS, String hdfsFilePath) {
		this.fsHDFS = fsHDFS;
		setHDFSFilePath(hdfsFilePath);
	}
	
	public FileHadoop(String url, String userName, String hdfsFilePath) {
		this(url, userName);
		setHDFSFilePath(hdfsFilePath);
	}
	
	/**
	 * @param url url可以仅有网址+端口，也可以加上hdfs前缀
	 * 如可以为192.168.0.188:9000
	 * 可以为192.168.0.188:9000
	 * 可以为hdfs://192.168.0.188:9000/
	 * @param userName
	 */
	public FileHadoop(String url, String userName) {
		if (!url.endsWith("/")) {
			url = url + "/";
		}
		if (!url.startsWith("hdfs://")) {
			if (!url.startsWith(":") && !url.startsWith("//")) {
				url = "hdfs://" + url;
			} else if (url.startsWith(":")) {
				url = "hdfs" + url;
			} else if (url.startsWith("//")) {
				url = "hdfs:" + url;
			}
		}
		
		// 在你的文件地址前自动添加：hdfs://192.168.0.188:9000/
		Configuration conf = new Configuration(); 
		conf.set("fs.default.name", url);
		// 指定用户名
		conf.set("hadoop.job.user", userName);
		try { fsHDFS = FileSystem.get(conf); } catch (IOException e) { e.printStackTrace(); }
	}
		
	/**
	 * 设定hdfs的文件路径
	 * @param hdfsFilePath
	 */
	public void setHDFSFilePath(String hdfsFilePath) {
		dst = new Path(hdfsFilePath);
	}
	
	/**
	 * 返回设定的文件系统
	 * @return
	 */
	public FileSystem getFsHDFS() {
		return fsHDFS;
	}
	
	/**
	 * 返回文件在hdfs上的文件名
	 * @return
	 */
	public String getFileNameHdfs() {
		//TODO 看一下返回的文件名是否正确
		return dst.getName();
	}
	
	/**
	 * @return 返回文件总结信息
	 * 通过该summary可以获得文件长度等信息
	 */
	public ContentSummary getContentSummary() {
		try {
			return fsHDFS.getContentSummary(dst);
		} catch (IOException e) {
			return null;
		}
	}
	
	public FSDataInputStream getInputStream() {
		try {
			return fsHDFS.open(dst);
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * 根据文件产生一个流
	 * @param overwrite  false：如果文件存在，则返回nulll
	 * @return
	 */
	public FSDataOutputStream getOutputStreamNew(boolean overwrite) {
		try {
			return fsHDFS.create(dst, overwrite);
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * 根据文件产生一个流，如果文件不存在则返回null
	 * 如果文件存在则衔接上去
	 * @param overwrite
	 * @return
	 */
	public FSDataOutputStream getOutputStreamAppend() {
		try {
			return fsHDFS.append(dst);
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * 创建一个文件夹
	 * @param path 路径+“/”+文件夹名字
	 * @throws IOException
	 */
	public void mkdirHDFSFolder(Path path) throws IOException {
		fsHDFS.mkdirs(path);
	}
	
	/**删除文件
	 * @throws IOException */
	public void removeHDFSfile(Path path) throws IOException {
		fsHDFS.delete(path, true);
	}
}


