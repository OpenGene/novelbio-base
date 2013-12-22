package com.novelbio.base.fileOperate;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.novelbio.base.PathDetail;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.HdfsBase;
import com.novelbio.base.dataStructure.ArrayOperate;

public class FileHadoop extends File {
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
		super(hdfsFilePath = copeToHdfsHeadSymbol(hdfsFilePath));
		this.fsHDFS = HdfsBase.getFileSystem();
		hdfsFilePath = hdfsFilePath.replace(FileHadoop.getHdfsHeadSymbol(), FileHadoop.getHdfsHeadPath());
		dst = new Path(hdfsFilePath);
		
	}
	
	/**
	 * 初始化
	 * @return
	 */
	private void init(){
		try{
			if(fileStatus != null)
				return;
			if (fsHDFS.exists(dst)) {
				fileStatus = fsHDFS.getFileStatus(dst);
			}
		}catch(Exception e){
		}
	}
	
	private static String copeToHdfsHeadSymbol(String hdfsFilePath) {
		if (hdfsFilePath.startsWith(FileHadoop.getHdfsHeadPath())) {
			hdfsFilePath = hdfsFilePath.replace(FileHadoop.getHdfsHeadPath(), FileHadoop.getHdfsHeadSymbol());
		}
		if (!hdfsFilePath.startsWith(FileHadoop.getHdfsHeadSymbol())) {
			hdfsFilePath = FileHadoop.addHdfsHeadSymbol(hdfsFilePath);
		}
		return hdfsFilePath;
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
	
	public String getModificationTime(){
		init();
		return DateUtil.date2String(new Date(fileStatus.getModificationTime()), DateUtil.PATTERN_DATETIME);
	}
	
	/**
	 * 找到上级文件全路径
	 * @param fileName
	 * @return
	 */
	@Override
	public String getParent() {
		try {
			return super.getParent();
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	/**
	 * 取得文件名
	 * @return
	 */
	@Override
	public String getName() {
		return super.getName();
	}
	
	/**
	 * 是不是目录
	 */
	@Override
	public boolean isDirectory() {
		if(fileStatus == null){
			try {
				return fsHDFS.isDirectory(dst);
			} catch (IOException e) {
				return false;
			}
		}else
			return fileStatus.isDir();
	}
	/**
	 * 存不存在此文件
	 * @return
	 */
	
	@Override
	public boolean exists() {
		if(fileStatus == null){
			try {
				return fsHDFS.exists(dst);
			} catch (IOException e) {
				return false;
			}
		}else
			return true;
		
	}
	
	/**
	 * 列出子文件
	 * @return
	 */
	@Override
	public String[] list() {
		FileStatus[] childrenFileStatus;
		try {
			childrenFileStatus = fsHDFS.listStatus(dst);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		String[] files = {};
		if (childrenFileStatus.length != 0) {
			files = new String[childrenFileStatus.length];
		}
		for (int i = 0; i < childrenFileStatus.length; i++) {
			files[i] = childrenFileStatus[i].getPath().getName();
		}
		return files;
	}

	@Override
	public boolean isFile() {
		return !isDirectory();
	}
	
	@Override
	public long length() {
		init();
		if(fileStatus == null)
			return 0;
		return fileStatus.getLen();
	}

	@Override
	public File getParentFile() {
		return super.getParentFile();
	}

	@Override
	public String getPath() {
		return super.getPath();
	}

	@Override
	public boolean isAbsolute() {
		// TODO Auto-generated method stub
		return super.isAbsolute();
	}

	@Override
	public String getAbsolutePath() {
		return super.getAbsolutePath();
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

	/** 出错返回 -1000 */
	@Override
	public long lastModified() {
		// TODO Auto-generated method stub
		init();
		try {
			return fileStatus.getModificationTime();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1000;
		}
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
				fileHadoop = new FileHadoop(FileOperate.addSep(getPath()) + files[i]);
			} catch (IOException e) {
				e.printStackTrace();
			}
			lsFiles.add(fileHadoop);
		}
		File[] children = ArrayOperate.converList2Array(lsFiles);
		if(children == null)
			return new File[]{};
		return ArrayOperate.converList2Array(lsFiles);
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
		try {
			return fsHDFS.mkdirs(dst);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 未测试
	 */
	@Override
	public boolean renameTo(File dest) {
		try {
			return fsHDFS.rename(dst, new Path(convertToMaprPath(dest.getPath())));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
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

	/** 文件名前添加的HDFS的头，末尾没有"/" */
	public static String getHdfsHeadSymbol() {
		return PathDetail.getHdfsHeadSymbol();
	}
	
	/** 在输入的文件名前添加的HDFS的头<br>
	 * <b>务必输入绝对路径，也就是要以"/"开头</b>
	 * @param path
	 * @return
	 */
	public static String addHdfsHeadSymbol(String path) {
		return PathDetail.addHdfsHeadSymbol(path);
	}
	
	/** hadoop实际的hdfs前缀，末尾没有"/" */
	public static String getHdfsHeadPath() {
		return PathDetail.getHdfsHeadPath();
	}
	public static String convertToMaprPath(String hdfsPath){
		if (hdfsPath.length() < 6) {
			
		}else if (HdfsBase.isHdfs(hdfsPath) || HdfsBase.isHdfs(hdfsPath.substring(1, hdfsPath.length()-2))) {
			hdfsPath = hdfsPath.replace(getHdfsHeadSymbol(), getHdfsHeadPath());
		}
		return hdfsPath;
	}
	/** 
	* 把hdfs的路径转换成本地路径，前提是hdfs已经挂载至本地，并且是带有hdfs头的类型
	*/
	public static String convertToLocalPath(String hdfsPath) {
		if (hdfsPath.length() < 6) {
			
		}else if (HdfsBase.isHdfs(hdfsPath) || HdfsBase.isHdfs(hdfsPath.substring(1, hdfsPath.length()-2))) {
			String parentPath = PathDetail.getHdfsLocalPath();
			hdfsPath = hdfsPath.replace(getHdfsHeadSymbol(), parentPath);
		}
		return hdfsPath;
	}
}


