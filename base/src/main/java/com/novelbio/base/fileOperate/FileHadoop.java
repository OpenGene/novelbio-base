package com.novelbio.base.fileOperate;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;

import com.novelbio.base.PathDetail;
import com.novelbio.base.StringOperate;

@Deprecated
public class FileHadoop extends File {
	private static final long serialVersionUID = 8341313247682247317L;
	public static final String hdfsSymbol = "hdfs:";
	private String fileName;
	Path path;
	
	/**
	 * initial a FileHadoop object from the path<br>
	 * @param hdfsFilePath like "/hdfs:/your/path/htsjdk.jar"  <br>
	 *  "/hdfs:" is the "hdfsHeadSymbol" in configure file src/java/hdfs/config.properties 
	 * @throws IOException 
	 */
	public FileHadoop(Path path) {
		super(FileOperate.removeSplashHead("/" +path.toString(), true));
		this.fileName = FileOperate.removeSplashHead("/" +path.toString(), true);
		this.path = path;
	}
	
	/**
	 * initial a FileHadoop object from the path<br>
	 * @param hdfsFilePath like "/hdfs:/your/path/htsjdk.jar"  <br>
	 *  "/hdfs:" is the "hdfsHeadSymbol" in configure file src/java/hdfs/config.properties 
	 * @throws IOException 
	 */
	public FileHadoop(String hdfsFilePath) {
		super(hdfsFilePath = copeToHdfsHeadSymbol(hdfsFilePath));
		this.fileName = hdfsFilePath;
		this.path = FileOperate.getPath(fileName);
	}
	
	private static String copeToHdfsHeadSymbol(String hdfsFilePath) {
		if (!hdfsFilePath.startsWith(PathDetail.getHdpHdfsHeadSymbol()) && !hdfsFilePath.startsWith(hdfsSymbol)) {
			hdfsFilePath = FileHadoop.addHdfsHeadSymbol(hdfsFilePath);
		} else if (hdfsFilePath.startsWith(PathDetail.getHdpHdfsHeadSymbol())) {
			hdfsFilePath = hdfsFilePath.replaceFirst(PathDetail.getHdpHdfsHeadSymbol(), hdfsSymbol);
		}
		return hdfsFilePath;
	}
	
    public FileHadoop getParentFile() {
        Path p = path.getParent();
        if (p == null) return null;
        return new FileHadoop(p);
    }
	
	@Override
	public String getParent() {
		try {
			return super.getParent();
		} catch (NullPointerException e) {
			return null;
		}
	}
	

	@Override
	public boolean isDirectory() {
		return Files.isDirectory(path);
	}

	@Override
	public boolean exists() {
		return Files.exists(path);
	}
	public String getAbsolutePath() {
		return copeToHdfsHeadSymbol(fileName);
	}
	
    public FileHadoop getAbsoluteFile() {
        String absPath = getAbsolutePath();
        return new FileHadoop(absPath);
    }
	
	@Deprecated
	public FileHadoop getCanonicalFile() throws IOException {
		return new FileHadoop(getCanonicalPath());
	}
	
	@Override
	public String[] list() {
		if (!Files.isDirectory(path)) {
			return null;
		}
		List<String> lsFileName = FileOperate.getLsFoldFileName(path);
		String[] ss = new String[lsFileName.size()];
		for (int i = 0; i < ss.length; i++) {
			ss[i] = FileOperate.getFileName(lsFileName.get(i));
		}
		return ss;
	}

	@Override
	public boolean isFile() {
		return Files.exists(path) && !Files.isDirectory(path);
	}
	
	@Override
	public long length() {
		try {
			return Files.readAttributes(path, BasicFileAttributes.class).size();
		} catch (IOException e) {
			return 0L;
		}
	}

	@Override
	@Deprecated
	public URL toURL() throws MalformedURLException {
		return path.toUri().toURL();
	}
	
	@Deprecated
	public boolean isHidden() {
		 throw new ExceptionNbcFile("No support method");
	}
	
	/** if error, return 0 */
	@Override
	public long lastModified() {
		try {
			return Files.readAttributes(path, BasicFileAttributes.class).lastModifiedTime().toMillis();
		} catch (IOException e) {
			return 0;
		}
	}

	@Override
	public boolean createNewFile() throws IOException {
		Files.createFile(path);
		return true;
	}
	
	@Override
	public boolean delete() {
		try {
			Files.delete(path);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public void deleteOnExit() {
		FileOperate.deleteFileFolder(path);
	}
	
	@Override
	public FileHadoop[] listFiles() {
		List<Path> lsSub = FileOperate.getLsFoldPath(path);
		FileHadoop[] fileHadoop2s = new FileHadoop[lsSub.size()];
		for (int i = 0; i < fileHadoop2s.length; i++) {
			fileHadoop2s[i] = new FileHadoop(lsSub.get(i));
		}
		return fileHadoop2s;
	}
	
	public boolean canWrite() {
		return Files.isReadable(path);
	}
	
	public boolean canRead() {
		return Files.isWritable(path);
	}

	public FileHadoop[] listFiles(FilenameFilter filter) {
        FileHadoop ss[] = listFiles();
        if (ss == null) return null;
        ArrayList<FileHadoop> files = new ArrayList<>();
        for (FileHadoop s : ss)
            if ((filter == null) || filter.accept(this, s.getName()))
                files.add(s);
        return files.toArray(new FileHadoop[files.size()]);
    }
    
    public FileHadoop[] listFiles(FileFilter filter) {
    	FileHadoop ss[] = listFiles();
        if (ss == null) return null;
        ArrayList<FileHadoop> files = new ArrayList<>();
        for (FileHadoop s : ss) {
            if ((filter == null) || filter.accept(s))
                files.add(s);
        }
        return files.toArray(new FileHadoop[files.size()]);
    }
    
	@Override
	public boolean mkdir() {
		try {
			Files.createDirectory(path);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean mkdirs() {
		try {
			Files.createDirectories(path);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	@Deprecated
    public boolean setLastModified(long time) {
        if (time < 0) throw new IllegalArgumentException("Negative time");
        try {
        	Files.setLastModifiedTime(path, FileTime.fromMillis(time));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
        return true;
    }
    @Deprecated
    public boolean setReadOnly() {
    	 throw new ExceptionNbcFile("No support method");
    }
    @Deprecated
    public boolean setWritable(boolean writable, boolean ownerOnly) {
    	return true;
    	
//    	 throw new ExceptionFile("No support method");
    }
    @Deprecated
    public boolean setWritable(boolean writable) {
    	 throw new ExceptionNbcFile("No support method");
    }
    @Deprecated
    public boolean setReadable(boolean readable, boolean ownerOnly) {
    	return true;
//    	 throw new ExceptionFile("No support method");
    }
    @Deprecated
    public boolean setReadable(boolean readable) {
    	 throw new ExceptionNbcFile("No support method");
    }
    @Deprecated
    public boolean setExecutable(boolean executable, boolean ownerOnly) {
    	 throw new ExceptionNbcFile("No support method");
    }
    @Deprecated
    public boolean setExecutable(boolean executable) {
    	 throw new ExceptionNbcFile("No support method");
    }
    public boolean canExecute() {
    	 throw new ExceptionNbcFile("No support method");
    }
    
    //TODO need test
	@Override
	public boolean renameTo(File dest) {
		try {
			Files.move(path, FileOperate.getPath(dest));
			return true;
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}
	
	/** 把 /media/nbfs和/hdfs:/这种改成 hdfs:/ 这种可以被hadoop识别的形式 */
	public static String convertToHdfsPath(String hdfsPath) {
		hdfsPath = convertToHadoop(hdfsPath);
		if (hdfsPath.startsWith(PathDetail.getHdpHdfsHeadSymbol())) {
			hdfsPath = hdfsPath.replaceFirst(PathDetail.getHdpHdfsHeadSymbol(), hdfsSymbol);
        }
		return hdfsPath;
	}
	/** 把 /media/nbfs这种改成hdfs:的形式，如果不是/media/hdfs这种，就不要动 */
	private static String convertToHadoop(String hdfsPath) {
		hdfsPath = FileOperate.removeSplashHead(hdfsPath, true);
		if (hdfsPath.toLowerCase().startsWith(getHdfsLocalPathWithoutSep().toLowerCase())) {
			hdfsPath = hdfsPath.replace(getHdfsLocalPathWithoutSep(), getHdfsSymbol());
		}
		return hdfsPath;
	}
	
	/** 把 /hdfs:/nbCloud/public 这种改成 /nbCloud/public 这种，如果不是/hdfs: 或 hdfs: 打头，就跳过 */
	public static String removeHadoopSymbol(String hdfsPath) {
		String hdfsHead = FileHadoop.hdfsSymbol;
		if (hdfsPath.startsWith(PathDetail.getHdpHdfsHeadSymbol())) {
			hdfsHead = PathDetail.getHdpHdfsHeadSymbol();
		}
		return hdfsPath.replaceFirst(hdfsHead, "");
	}
	
	/** 
	* 把hdfs的路径转换成本地路径，前提是hdfs已经挂载至本地，并且是带有hdfs头的类型
	*/
	public static String convertToLocalPath(String hdfsPath) {
		if (hdfsPath.length() < 6) {
			return hdfsPath;
		}else if (FileHadoop.isHdfs(hdfsPath) || FileHadoop.isHdfs(hdfsPath.substring(1, hdfsPath.length()-2))) {
			String parentPath = getHdfsLocalPathWithoutSep();
			if (hdfsPath.startsWith(PathDetail.getHdpHdfsHeadSymbol())) {
				hdfsPath = hdfsPath.replace(PathDetail.getHdpHdfsHeadSymbol(), parentPath);
			} else if (hdfsPath.startsWith(hdfsSymbol)) {
				hdfsPath = hdfsPath.replace(hdfsSymbol, parentPath);
			}
		}
		return hdfsPath;
	}
	
	/** 
	 * 用{@link com.novelbio.base.fileOperate.FileHadoop#getHdfsSymbol()}替换<br>
	 * 文件名前添加的HDFS的头，末尾没有"/" */
	public static String getHdfsSymbol() {
		return hdfsSymbol;
	}
	
	/** 
	 * 用{@link com.novelbio.base.fileOperate.FileHadoop#addHdfsHeadSymbol(path)}替换<br>
	 * 在输入的文件名前添加的HDFS的头<br>
	 * <b>务必输入绝对路径，也就是要以"/"开头</b>
	 * @param path
	 * @return
	 */
	public static String addHdfsHeadSymbol(String path) {
		return getHdfsSymbol() + path;
	}
	
	/** 
	 * 用{@link com.novelbio.base.fileOperate.FileHadoop#getHdfsLocalPathWithoutSep()}替换<br>
	 * hdfs挂载在本地硬盘的路径 */
	public static String getHdfsLocalPathWithoutSep() {
		return FileOperate.removeSplashTail(PathDetail.getHdfsLocalPath(), false);
	}
	/** 
	 * 用{@link com.novelbio.base.fileOperate.FileHadoop#getHdfsLocalPathWithoutSep()}替换<br>
	 * hdfs挂载在本地硬盘的路径 */
	public static String getHdfsLocalPathWithSep() {
		return FileOperate.addSep(PathDetail.getHdfsLocalPath());
	}
	
	public static FileSystem getHadoopFileSystem() {
		return HdfsInitial.getFileSystem();
	}
	
	public static boolean isHdfs(String fileName) {
		if (StringOperate.isRealNull(fileName)) {
			return false;
		}
		fileName = fileName.toLowerCase();
		if (StringOperate.isRealNull(getHdfsSymbol())) {
			return false;
		}
		return (fileName.startsWith(PathDetail.getHdpHdfsHeadSymbol()) || fileName.startsWith(hdfsSymbol));
	}

	@Deprecated
	public static File[] listRoots() {
        return null;
    }

   @Deprecated
    public long getTotalSpace() {
	   throw new ExceptionNbcFile("No support method");
    }

   @Deprecated
    public long getFreeSpace() {
    	throw new ExceptionNbcFile("No support method");
    }

    @Deprecated
    public long getUsableSpace() {
    	 throw new ExceptionNbcFile("No support method");
    }
    
    /* -- Basic infrastructure -- */

    /**
     * Compares two abstract pathnames lexicographically.  The ordering
     * defined by this method depends upon the underlying system.  On UNIX
     * systems, alphabetic case is significant in comparing pathnames; on Microsoft Windows
     * systems it is not.
     *
     * @param   pathname  The abstract pathname to be compared to this abstract
     *                    pathname
     *
     * @return  Zero if the argument is equal to this abstract pathname, a
     *          value less than zero if this abstract pathname is
     *          lexicographically less than the argument, or a value greater
     *          than zero if this abstract pathname is lexicographically
     *          greater than the argument
     *
     * @since   1.2
     */
    public int compareTo(File pathname) {
        return super.compareTo(pathname);
    }

    /**
     * Tests this abstract pathname for equality with the given object.
     * Returns <code>true</code> if and only if the argument is not
     * <code>null</code> and is an abstract pathname that denotes the same file
     * or directory as this abstract pathname.  Whether or not two abstract
     * pathnames are equal depends upon the underlying system.  On UNIX
     * systems, alphabetic case is significant in comparing pathnames; on Microsoft Windows
     * systems it is not.
     *
     * @param   obj   The object to be compared with this abstract pathname
     *
     * @return  <code>true</code> if and only if the objects are the same;
     *          <code>false</code> otherwise
     */
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof File)) {
            return compareTo((File)obj) == 0;
        }
        return false;
    }

    /**
     * Computes a hash code for this abstract pathname.  Because equality of
     * abstract pathnames is inherently system-dependent, so is the computation
     * of their hash codes.  On UNIX systems, the hash code of an abstract
     * pathname is equal to the exclusive <em>or</em> of the hash code
     * of its pathname string and the decimal value
     * <code>1234321</code>.  On Microsoft Windows systems, the hash
     * code is equal to the exclusive <em>or</em> of the hash code of
     * its pathname string converted to lower case and the decimal
     * value <code>1234321</code>.  Locale is not taken into account on
     * lowercasing the pathname string.
     *
     * @return  A hash code for this abstract pathname
     */
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Returns the pathname string of this abstract pathname.  This is just the
     * string returned by the <code>{@link #getPath}</code> method.
     *
     * @return  The string form of this abstract pathname
     */
    public String toString() {
        return fileName;
    }
    
    public Path toPath() {
    	return path;
    }
    
    public org.apache.hadoop.fs.Path getHdpPath() {
    	String hdfsFilePath = removeHadoopSymbol(fileName);
    	org.apache.hadoop.fs.Path dst = new org.apache.hadoop.fs.Path(hdfsFilePath);
    		return dst;
    }
    
	
	public short getReplication() throws IOException {
		org.apache.hadoop.fs.Path path = getHdpPath();
		FileStatus fileStatus =null;
		if (HdfsInitial.getFileSystem().exists(path)) {
			fileStatus = HdfsInitial.getFileSystem().getFileStatus(path);
			return fileStatus.getReplication();
		}
		return 0;
	}
	
	public FSDataInputStream getInputStream() {
		try {
			return HdfsInitial.getFileSystem().open(getHdpPath());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}


