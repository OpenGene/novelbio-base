package com.novelbio.base.fileOperate;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.DateUtil;
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
		this.fsHDFS = HdfsInitial.getFileSystem();
		hdfsFilePath = hdfsFilePath.replace(FileHadoop.getHdfsSymbol(), HdfsInitial.getHEAD());
		//TODO 以后就应该是 hdfsFilePath = hdfsFilePath.replace(FileHadoop.getHdfsHeadSymbol(), "");
		dst = new Path(hdfsFilePath);
	}
	/** 初始化 */
	private void init() {
		try{
			if(fileStatus != null)
				return;
			if (fsHDFS.exists(dst)) {
				fileStatus = fsHDFS.getFileStatus(dst);
			}
		} catch(Exception e) { }
	}
	
	private static String copeToHdfsHeadSymbol(String hdfsFilePath) {
		if (hdfsFilePath.startsWith(HdfsInitial.getHEAD())) {
			hdfsFilePath = hdfsFilePath.replace(HdfsInitial.getHEAD(), FileHadoop.getHdfsSymbol());
		}
		if (!hdfsFilePath.startsWith(FileHadoop.getHdfsSymbol())) {
			hdfsFilePath = FileHadoop.addHdfsHeadSymbol(hdfsFilePath);
		}
		return hdfsFilePath;
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
	@Deprecated
	public URL toURL() throws MalformedURLException {
		// TODO Auto-generated method stub
		return super.toURL();
	}

	/** 出错返回 -1000 */
	@Override
	public long lastModified() {
		init();
		try {
			return fileStatus.getModificationTime();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean createNewFile() throws IOException {
		return fsHDFS.createNewFile(dst);
	}
	
	@Override
	public boolean delete() {
		try {
			return fsHDFS.delete(dst, true);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public File[] listFiles() {
		String[] files = list();
		List<File> lsFiles = new ArrayList<>();
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

	public static String convertToMaprPath(String hdfsPath){
		if (hdfsPath.length() < 6) {
			
		}else if (FileHadoop.isHdfs(hdfsPath) || FileHadoop.isHdfs(hdfsPath.substring(1, hdfsPath.length()-2))) {
			hdfsPath = hdfsPath.replace(HdfsInitial.getSymbol(), HdfsInitial.getHEAD());
		}
		return hdfsPath;
	}
	/** 
	* 把hdfs的路径转换成本地路径，前提是hdfs已经挂载至本地，并且是带有hdfs头的类型
	*/
	public static String convertToLocalPath(String hdfsPath) {
		if (hdfsPath.length() < 6) {
			
		}else if (FileHadoop.isHdfs(hdfsPath) || FileHadoop.isHdfs(hdfsPath.substring(1, hdfsPath.length()-2))) {
			String parentPath = getHdfsLocalPath();
			hdfsPath = hdfsPath.replace(getHdfsSymbol(), parentPath);
		}
		return hdfsPath;
	}
	
	/** 
	 * 用{@link com.novelbio.base.fileOperate.FileHadoop#getHdfsSymbol()}替换<br>
	 * 文件名前添加的HDFS的头，末尾没有"/" */
	public static String getHdfsSymbol() {
		return HdfsInitial.getSymbol();
	}
	
	/** 
	 * 用{@link com.novelbio.base.fileOperate.FileHadoop#addHdfsHeadSymbol(path)}替换<br>
	 * 在输入的文件名前添加的HDFS的头<br>
	 * <b>务必输入绝对路径，也就是要以"/"开头</b>
	 * @param path
	 * @return
	 */
	public static String addHdfsHeadSymbol(String path) {
		return HdfsInitial.getSymbol() + path;
	}
	
	/** 
	 * 用{@link com.novelbio.base.fileOperate.FileHadoop#getHdfsLocalPath()}替换<br>
	 * hdfs挂载在本地硬盘的路径 */
	public static String getHdfsLocalPath() {
		return HdfsInitial.getHdfsLocalPath();
	}
	
	public static boolean isHdfs(String fileName) {
		if (fileName == null || fileName.equals("")) {
			return false;
		}
		fileName = fileName.toLowerCase();
		if (!StringOperate.isRealNull(HdfsInitial.getHEAD())) {
			return fileName.startsWith(HdfsInitial.getSymbol()) ? true : false;
		}
		return false;
	}

	
//	/**
//	 * @return 返回文件总结信息
//	 * 通过该summary可以获得文件长度等信息
//	 */
//	@Deprecated
//	public ContentSummary getContentSummary() {
//		try {
//			return fsHDFS.getContentSummary(dst);
//		} catch (IOException e) {
//			return null;
//		}
//	}
}


