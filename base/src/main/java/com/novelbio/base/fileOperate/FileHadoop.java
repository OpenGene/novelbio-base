package com.novelbio.base.fileOperate;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.web.resources.OverwriteParam;
import org.apache.hadoop.security.UserGroupInformation.HadoopLoginModule;

import com.novelbio.base.dataOperate.HdfsBase;

public class FileHadoop extends File{
	private static final long serialVersionUID = 1L;
	
	FileSystem fsHDFS;
	Path dst;
	FileStatus fileStatus;
	
	/**
	 * 输入另一个fileHadoop的内容，仅获得其配置信息，不获得其具体文件名
	 * @param fileHadoop
	 * @throws IOException 
	 */
	public FileHadoop(String hdfsFilePath) throws IOException {
		super(hdfsFilePath);
		this.fsHDFS = HdfsBase.getFileSystem();
		dst = new Path(hdfsFilePath);
		fileStatus = fsHDFS.getFileStatus(dst);
	}
	
	/**
	 * 返回设定的文件系统
	 * @return
	 */
	public FileSystem getFsHDFS() {
		return fsHDFS;
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
	
	/** 返回小于0表示出错 使用length()代替*/
	@Deprecated
	public long getFileSize() {        
		try {
			return fsHDFS.getFileStatus(dst).getLen();
		} catch (IOException e) {
			return -1;
		}
	}
	
	public boolean writeln(String fileContent,boolean overwrite) {
		try {
			FSDataOutputStream fsStream = getOutputStreamNew(overwrite);
			fsStream.writeUTF(fileContent);
			String lineSeparater =  (String) java.security.AccessController.doPrivileged(
		               new sun.security.action.GetPropertyAction("line.separator"));
			fsStream.writeUTF(lineSeparater);
			fsStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 根据文件产生一个流
	 * @param overwrite  false：如果文件不存在，则返回nulll
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
	 * 找到上级文件全路径
	 * @param fileName
	 * @return
	 */
	@Override
	public String getParent(){
		return dst.getParent().toString();
	}
	
	/**
	 * 取得文件名
	 * @return
	 */
	@Override
	public String getName() {
		return dst.getName();
	}
	
	/**
	 * 是不是目录
	 */
	@Override
	public boolean isDirectory() {
		return fileStatus.isDir();
	}
	/**
	 * 存不存在此文件
	 * @return
	 */
	
	@Override
	public boolean exists() {
		try {
			return fsHDFS.exists(dst);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 列出子文件
	 * @return
	 */
	@Override
	public String[] list() {
		FileStatus[] fileStatus;
		try {
			fileStatus = fsHDFS.listStatus(dst);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		String[] files = new String[fileStatus.length-1];
		for (int i = 0; i < fileStatus.length; i++) {
			files[i] = fileStatus[i].getPath().toString();
		}
		return files;
	}
	
	@Override
	public boolean isFile() {
		return !isDirectory();
	}
	
	@Override
	public long length() {
		return fileStatus.getLen();
	}

	@Override
	public File getParentFile() {
		// TODO Auto-generated method stub
		return super.getParentFile();
	}

	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return super.getPath();
	}

	@Override
	public boolean isAbsolute() {
		// TODO Auto-generated method stub
		return super.isAbsolute();
	}

	@Override
	public String getAbsolutePath() {
		return dst.getParent().toString();
	}

	@Override
	public File getAbsoluteFile() {
		// TODO Auto-generated method stub
		return super.getAbsoluteFile();
	}

	@Override
	public String getCanonicalPath() throws IOException {
		// TODO Auto-generated method stub
		return super.getCanonicalPath();
	}

	@Override
	public File getCanonicalFile() throws IOException {
		// TODO Auto-generated method stub
		return super.getCanonicalFile();
	}

	@Override
	@Deprecated
	public URL toURL() throws MalformedURLException {
		// TODO Auto-generated method stub
		return super.toURL();
	}

	@Override
	public URI toURI() {
		// TODO Auto-generated method stub
		return super.toURI();
	}

	@Override
	public boolean canRead() {
		// TODO Auto-generated method stub
		return super.canRead();
	}

	@Override
	public boolean canWrite() {
		// TODO Auto-generated method stub
		return super.canWrite();
	}

	@Override
	public boolean isHidden() {
		// TODO Auto-generated method stub
		return super.isHidden();
	}

	@Override
	public long lastModified() {
		// TODO Auto-generated method stub
		return super.lastModified();
	}

	@Override
	public boolean createNewFile() throws IOException {
		return fsHDFS.createNewFile(dst);
	}

	@Override
	public boolean delete() {
		try {
			return fsHDFS.delete(dst, false);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void deleteOnExit() {
		// TODO Auto-generated method stub
		super.deleteOnExit();
	}

	@Override
	public String[] list(FilenameFilter filter) {
		// TODO Auto-generated method stub
		return super.list(filter);
	}

	@Override
	public File[] listFiles() {
		String[] files = list();
		List<File> lsFiles = new ArrayList<File>();
		for (int i = 0; i < files.length; i++) {
			FileHadoop fileHadoop = null;
			try {
				fileHadoop = new FileHadoop(files[i]);
			} catch (IOException e) {
				e.printStackTrace();
			}
			lsFiles.add(fileHadoop);
		}
		return lsFiles.toArray(new File[lsFiles.size()-1]);
	}

	@Override
	public File[] listFiles(FilenameFilter filter) {
		// TODO Auto-generated method stub
		return super.listFiles(filter);
	}

	@Override
	public File[] listFiles(FileFilter filter) {
		// TODO Auto-generated method stub
		return super.listFiles(filter);
	}

	@Override
	public boolean mkdir() {
		try {
			return fsHDFS.mkdirs(dst);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean mkdirs() {
		// TODO Auto-generated method stub
		return super.mkdirs();
	}

	@Override
	public boolean renameTo(File dest) {
		// TODO Auto-generated method stub
		return super.renameTo(dest);
	}

	@Override
	public boolean setLastModified(long time) {
		// TODO Auto-generated method stub
		return super.setLastModified(time);
	}

	@Override
	public boolean setReadOnly() {
		// TODO Auto-generated method stub
		return super.setReadOnly();
	}

	@Override
	public boolean setWritable(boolean writable, boolean ownerOnly) {
		// TODO Auto-generated method stub
		return super.setWritable(writable, ownerOnly);
	}

	@Override
	public boolean setWritable(boolean writable) {
		// TODO Auto-generated method stub
		return super.setWritable(writable);
	}

	@Override
	public boolean setReadable(boolean readable, boolean ownerOnly) {
		// TODO Auto-generated method stub
		return super.setReadable(readable, ownerOnly);
	}

	@Override
	public boolean setReadable(boolean readable) {
		// TODO Auto-generated method stub
		return super.setReadable(readable);
	}

	@Override
	public boolean setExecutable(boolean executable, boolean ownerOnly) {
		// TODO Auto-generated method stub
		return super.setExecutable(executable, ownerOnly);
	}

	@Override
	public boolean setExecutable(boolean executable) {
		// TODO Auto-generated method stub
		return super.setExecutable(executable);
	}

	@Override
	public boolean canExecute() {
		// TODO Auto-generated method stub
		return super.canExecute();
	}

	@Override
	public long getTotalSpace() {
		// TODO Auto-generated method stub
		return super.getTotalSpace();
	}

	@Override
	public long getFreeSpace() {
		// TODO Auto-generated method stub
		return super.getFreeSpace();
	}

	@Override
	public long getUsableSpace() {
		// TODO Auto-generated method stub
		return super.getUsableSpace();
	}

	@Override
	public int compareTo(File pathname) {
		// TODO Auto-generated method stub
		return super.compareTo(pathname);
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

}


