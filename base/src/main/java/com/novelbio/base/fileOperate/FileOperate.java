package com.novelbio.base.fileOperate;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.novelbio.base.PathDetail;
import com.novelbio.base.SerializeKryo;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.jsr203.bos.OssFileSystemProvider;
import com.novelbio.jsr203.bos.OssPath;

import hdfs.jsr203.HadoopFileSystemProvider;
import hdfs.jsr203.HadoopPath;

public class FileOperate {
	private static final Logger logger = Logger.getLogger(FileOperate.class);
	static HadoopFileSystemProvider hdfsProvider = new HadoopFileSystemProvider();
	static OssFileSystemProvider ossProvider = new OssFileSystemProvider();
	static PatternOperate patternOperate = new PatternOperate("^[/\\\\]{0,2}[^/]+\\:[/\\\\]{0,2}");
	static boolean isWindowsOS = false;
	static {
		String osName = System.getProperty("os.name");
		if (osName.toLowerCase().indexOf("windows") > -1) {
			isWindowsOS = true;
		}
	}

	/**
	 * 是否是windows操作系统
	 */
	public static boolean isWindows() {
		return isWindowsOS;
	}

	/**
	 * 根据不同的文件类型得到File
	 * 
	 * @param filePath
	 * @return
	 */
	@Deprecated
	public static File getFile(String filePathParent, String name) {
		if (!StringOperate.isRealNull(filePathParent.trim())) {
			filePathParent = FileOperate.addSep(filePathParent);
		}
		String path = filePathParent + name;
		return getFile(path);
	}

	/**
	 * 根据不同的文件类型得到File
	 * 
	 * @param filePath
	 * @return
	 */
	@Deprecated
	public static File getFile(File fileParent, String name) {
		if (fileParent instanceof FileHadoop) {
			return new FileHadoop(FileOperate.addSep(fileParent.getAbsolutePath()) + name);
		} else {
			return new File(fileParent, name);
		}
	}

	/**
	 * 根据不同的文件类型得到File.<br/>
	 * <br/>
	 * 
	 * @param filePath
	 * @return
	 */
	@Deprecated
	public static File getFile(String filePath) {
		File file = null;
		boolean isHdfs = FileHadoop.isHdfs(filePath);
		if (isHdfs) {
			file = new FileHadoop(filePath);
		} else if (filePath.startsWith("oss:/")) {
			file = new File(getPath(filePath).toString());
		} else {
			file = new File(filePath);
		}
		return file;
	}

	public static Path getPath(String fileName) {
		if (StringOperate.isRealNull(fileName))
			return null;
		if (fileName.startsWith(PathDetail.getHdpHdfsHeadSymbol())) {
			fileName = fileName.replaceFirst(PathDetail.getHdpHdfsHeadSymbol(), FileHadoop.hdfsSymbol);
		}
		try {
			if (fileName.startsWith(FileHadoop.hdfsSymbol)) {
				URI uri = null;
				if (!fileName.contains(" ")) {
					uri = new URI(fileName);
				} else {
					fileName = fileName.replace(FileHadoop.hdfsSymbol, "");
					if (fileName.startsWith(":"))
						fileName.replaceFirst(":", "");
					String host = null, path = "";
					if (fileName.startsWith("//")) {
						String[] ss = fileName.replaceFirst("//", "").split("/", 2);
						host = ss[0];
						path = "/" + ss[1];
					} else {
						path = fileName;
					}
					uri = new URI("hdfs", host, path, null);
				}

				// TODO 不是类没加载，而是META文件没有读取到
				// Paths.get(uri);
				return hdfsProvider.getPath(uri);
			} else if (fileName.startsWith(OssFileSystemProvider.SCHEME)) {
				URI uri = new URI(fileName);
				return ossProvider.getPath(uri);
			} else {
				File file = new File(fileName);
				return file.toPath();
			}
		} catch (Exception e) {
			throw new ExceptionFileError("cannot get path from " + fileName, e);
		}
	}

	public static Path getPath(File file) {
		if (file == null)
			return null;
		return getPath(file.getPath());
	}

	/** 文件尾巴加个 "/" */
	public static String getSepPath() {
		return File.separator;
	}

	/**
	 * 将对象写成文件
	 * 
	 * @param object
	 *            对象
	 * @param pathAndName
	 *            文件路径及名称
	 * @return
	 */
	public static boolean writeObjectToFile(Object object, String fileName) {
		OutputStream fs = null;
		try {
			fs = getOutputStream(fileName);
			SerializeKryo kryo = new SerializeKryo();
			fs.write(kryo.write(object));
			fs.flush();
			return true;
		} catch (Exception e) {
			return false;// TODO: handle exception
		} finally {
			close(fs);
		}
	}

	public static Object readFileAsObject(String fileName) {
		InputStream fs = null;
		try {
			fs = getInputStream(fileName);
			SerializeKryo kryo = new SerializeKryo();
			return kryo.read(IOUtils.toByteArray(fs));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(fs);
		}

		return null;// TODO: handle exception
	}

	/**
	 * 给定路径名，返回其上一层路径，带"/" 如给定 /wer/fw4e/sr/frw/s3er.txt 返回 /wer/fw4e/sr/frw/
	 * <br>
	 * 如果为相对路径的最上层，譬如给定的是soap 则返回“” 可以给定不存在的路径
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static String getParentPathNameWithSep(String fileName) {
		if (fileName == null)
			return null;

		if (fileName.equals("/") || fileName.equals("\\")) {
			return fileName;
		}

		if (fileName.startsWith("oss://")) {
			try {
				URI uri = new URI(fileName);
				String parentPath = new OssFileSystemProvider().getPath(uri).getParent().toString();
				return parentPath.endsWith("/") ? parentPath : parentPath + "/";
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("getParentPathNameWithSep error.filename=" + fileName, e);
				return fileName;
			}
		} else {
			File file = new File(fileName);
			String fileParent = file.getParent();
			String head = patternOperate.getPatFirst(fileName);
			if (head == null)
				head = "";
			if (fileParent == null)
				fileParent = "";

			if (fileParent.length() < head.length()) {
				return head;
			} else {
				return addSep(fileParent);
			}
		}

	}

	/**
	 * 给定路径名，返回其最近一层路径，带"/" 如给定 /wer/fw4e/sr/frw/s3er.txt 返回 /wer/fw4e/sr/frw/
	 * <br>
	 * 给定/wef/tesw/tre/还是返回/wef/tesw/tre/
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static String getPathName(String fileName) {
		if (fileName == null)
			return null;
		if (fileName.endsWith("/") || fileName.endsWith("\\")) {
			return fileName;
		}
		return getParentPathNameWithSep(fileName);
	}

	/**
	 * 给定路径名，返回其最近一层路径，带"/" 如给定 /wer/fw4e/sr/frw/s3er.txt 返回 /wer/fw4e/sr/frw/
	 * <br>
	 * 给定/wef/tesw/tre/还是返回/wef/tesw/tre/
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static String getPathName(Path path) {
		String name = getAbsolutePath(path);
		if (isFileDirectory(path)) {
			name = addSep(name);
		}
		return getPathName(name);
	}

	/**
	 * 给定文件名，加上后缀
	 * 
	 * @param fileName
	 *            可以包含路径，如果包含路径，则返回全部路径名和后缀。 如果已有后缀，则不添加。
	 * @suffix 待添加的后缀名，如果为""，则不添加
	 * @return
	 */
	public static String addSuffix(String fileName, String suffix) {
		if (suffix == null || suffix.trim().equals("")) {
			return fileName;
		}
		String[] thisFileName = getFileNameSep(fileName);
		if (thisFileName[1].equals(suffix)) {
			return fileName;
		}
		if (fileName.endsWith(".")) {
			return fileName + suffix;
		}
		return fileName + "." + suffix;
	}

	/** 返回日期格式的最后修改时间 */
	public static String getTimeLastModifyStr(String fileName) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(getTimeLastModify(fileName));
		DateFormat formatter = DateFormat.getDateTimeInstance();
		String data = formatter.format(cal.getTime());
		return data;
	}

	public static long getTimeLastModify(String fileName) {
		Path path = getPath(fileName);
		return getTimeLastModify(path);
	}

	public static long getTimeLastModify(Path path) {
		if (path == null) {
			throw new ExceptionFileError("cannot get file " + path);
		}
		try {
			FileTime time = Files.getLastModifiedTime(path);
			return time.toMillis();
		} catch (IOException e) {
			throw new ExceptionFileError("cannot get last modify time " + path);
		}
	}

	@Deprecated
	public static long getTimeLastModify(File file) {
		Path path = getPath(file);
		return getTimeLastModify(path);
	}

	/**
	 * 给定路径名，返回上一级文件夹的名字，可以给定不存在的路径。 如给定 /home/novelbio/zongjie/ 和
	 * /home/novelbio/zongjie 均返回 novelbio
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getLastPathName(String fileName) {
		String ParentPath = getParentPathNameWithSep(fileName);
		return FileOperate.getFileName(ParentPath);
	}

	/**
	 * 给定路径名，返回其名字 如给定/home/zong0jie/和/home/zong0jie 都返回zong0jie 可以给定不存在的路径
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFileName(String fileName) {
		if (StringOperate.isRealNull(fileName)) {
			return "";
		}
		File file = new File(fileName);
		return file.getName();
	}

	/**
	 * 给定路径名，返回其名字 如给定/home/zong0jie/和/home/zong0jie 都返回zong0jie 可以给定不存在的路径
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFileName(Path path) {
		return path == null ? "" : path.getFileName().toString();
	}

	/**
	 * 返回以K为单位的估计文件的总和，压缩文件就会加倍估计<br>
	 * 不准，只是估计而已
	 * 
	 * @return
	 * @throws IOException
	 */
	public static double getFileSizeEvaluateK(Collection<String> colFileName) {
		double allFileSize = 0;
		for (String fileName : colFileName) {
			double size = (double) FileOperate.getFileSizeLong(fileName) / 1024;
			// 如果是压缩文件就假设源文件为6倍大 */
			String suffix = getFileNameSep(fileName)[1].toLowerCase();
			if (suffix.equals("gz") || suffix.equals("zip") || suffix.equals("rar"))
				size = size * 10;
			else
				size = size * 1.2;

			allFileSize = allFileSize + size;
		}
		return allFileSize;
	}

	/**
	 * 给定文件路径，返回大小，单位为byte，如果有链接，则返回链接的大小<br/>
	 * 该方法返回的实际文件大小,不是文件占用空间大小.<br/>
	 * 如:测试实际占用185G.该方法返回172G,实际占用26G,返回24G
	 * 
	 * @param filePath
	 *            如果是文件夹，则递归返回本文件夹下全体文件的大小
	 * @return 没有文件返回-1
	 * @throws IOException
	 */
	public static long getFileSizeLong(String filePath) {
		return getFileSizeLong(getPath(filePath));
	}
	
	/**
	 * 给定文件路径，返回大小，单位为byte，如果有链接，则返回链接的大小<br/>
	 * 该方法返回的实际文件大小,不是文件占用空间大小.<br/>
	 * 如:测试实际占用185G.该方法返回172G,实际占用26G,返回24G
	 * 
	 * @param filePath
	 *            如果是文件夹，则递归返回本文件夹下全体文件的大小
	 * @return 没有文件返回-1
	 * @throws IOException
	 */
	@Deprecated
	public static long getFileSizeLong(File file) {
		return getFileSizeLong(getPath(file));
	}

	/**
	 * 给定文件路径，返回大小，单位为byte，如果有链接，则返回链接的大小<br/>
	 * 该方法返回的实际文件大小,不是文件占用空间大小.<br/>
	 * 如:测试实际占用185G.该方法返回172G,实际占用26G,返回24G
	 * 
	 * @param filePath
	 *            如果是文件夹，则递归返回本文件夹下全体文件的大小
	 * @return 没有文件返回-1
	 * @throws IOException
	 */
	// TODO 测试
	public static long getFileSizeLong(Path path) {
		if (path == null || !Files.exists(path)) {
			return -1;
		}
		if (!isFileDirectory(path)) {
			return getFileAttribute(path).size();
		} else if (isFileDirectory(path)) {
			try {
				long[] size = new long[1];
				Files.list(path).forEach((file) -> {
					size[0] += getFileSizeLong(file);
				});
				return size[0];
			} catch (IOException e) {
				throw new ExceptionFileError("get file size error " + path.toString(), e);
			}
		} else {
			throw new ExceptionFileError("cannot find file " + path.toString());
		}
	}

	public static BasicFileAttributes getFileAttribute(String filePath) {
		return getFileAttribute(getPath(filePath));
	}

	public static BasicFileAttributes getFileAttribute(Path filePath) {
		try {
			return Files.readAttributes(filePath, BasicFileAttributes.class);
		} catch (IOException e) {
			throw new ExceptionFileError("cannot get attributes of file " + filePath);
		}
	}

	/**
	 * 给定路径名，返回其名字,不带后缀名<br>
	 * 如给定/home/zong0jie.aa.txt<br>
	 * 则返回zong0jie.aa 和 txt<br>
	 * <br>
	 * 给定/home/zong0jie.aa.txt/，则返回""和""<br>
	 * 可以给定不存在的路径<br>
	 * 
	 * @param fileName
	 * @return string[2] 0:文件名 1:文件后缀
	 */
	public static String[] getFileNameSep(String fileName) {
		if (fileName.endsWith("/") || fileName.endsWith("\\")) {
			return new String[] { "", "" };
		}
		File file = new File(fileName);
		String filename = file.getName();
		return getFileNameSepWithoutPath(filename);
	}

	/**
	 * 获取文件名，不包括后缀
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFileNameWithoutSuffix(String fileName) {
		return getFileNameSep(fileName)[0];
	}

	/**
	 * 获取文件名后缀 2016年3月30日 novelbio fans.fan
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFileSuffix(String fileName) {
		return getFileNameSep(fileName)[1];
	}

	/**
	 * 给定文件的相对路径名，返回文件名字
	 * 
	 * @param fileNameWithoutPath
	 * @return string[2] 0:文件名 1:文件后缀
	 */
	private static String[] getFileNameSepWithoutPath(String fileNameWithoutPath) {
		fileNameWithoutPath = getFileName(fileNameWithoutPath);
		String[] result = new String[2];
		int endDot = fileNameWithoutPath.lastIndexOf(".");
		if (endDot > 0) {
			result[0] = (String) fileNameWithoutPath.subSequence(0, endDot);
			result[1] = (String) fileNameWithoutPath.subSequence(endDot + 1, fileNameWithoutPath.length());
		} else {
			result[0] = fileNameWithoutPath;
			result[1] = "";
		}
		return result;
	}

	/**
	 * 将文件开头的"//"这种多个的去除
	 * 
	 * @param fileName
	 * @param keepOne
	 *            是否保留一个“/”
	 * @return
	 */
	public static String removeSplashHead(String fileName, boolean keepOne) {
		String head = "//";
		if (!keepOne) {
			head = "/";
		}
		String fileNameThis = fileName;
		while (true) {
			if (fileNameThis.startsWith(head)) {
				fileNameThis = fileNameThis.substring(1);
			} else {
				break;
			}
		}
		return fileNameThis;
	}

	/**
	 * 将文件结尾的"//"这种多个的去除
	 * 
	 * @param fileName
	 * @param keepOne
	 *            是否保留一个“/”
	 * @return
	 */
	public static String removeSplashTail(String fileName, boolean keepOne) {
		String tail = "//";
		if (!keepOne) {
			tail = "/";
		}
		String fileNameThis = fileName;
		while (true) {
			if (fileNameThis.endsWith(tail)) {
				fileNameThis = fileNameThis.substring(0, fileNameThis.length() - 1);
			} else {
				break;
			}
		}
		return fileNameThis;
	}

	/** 用于取代path的tostring方法，因为hdfs的path.toString() 默认返回 hdfs:/，而我们要的是 /hdfs:/ */
	public static String getFilePathName(Path path) {
		boolean isAddSplashHead = false;
		String filePathName = path.toString();
		if (filePathName.startsWith(FileHadoop.hdfsSymbol)) {
			if (!filePathName.startsWith("/")) {
				filePathName = "/" + filePathName;
				isAddSplashHead = true;
			}
		}
		if (isAddSplashHead) {
			filePathName = removeSplashHead(filePathName, true);
		}
		return filePathName;
	}

	public static String getAbsolutePath(String fileName) {
		boolean isAddSplashHead = false;
		if (fileName.startsWith(FileHadoop.hdfsSymbol)) {
			if (!fileName.startsWith("/")) {
				fileName = "/" + fileName;
				isAddSplashHead = true;
			}
		}
		File file = new File(fileName);
		String absolutePath = file.getAbsolutePath();
		if (isAddSplashHead) {
			absolutePath = removeSplashHead(absolutePath, true);
		}
		return absolutePath;
	}

	public static String getAbsolutePath(Path path) {
		String name = getCanonicalPath(path.toString());
		if (path instanceof HadoopPath) {
			if (name.startsWith(FileHadoop.hdfsSymbol)) {
				name = "/" + name;
			} else if (!name.startsWith(FileHadoop.getHdfsSymbol())) {
				name = "/hdfs:" + name;
			}
		} else if (path instanceof OssPath) {
			name = path.toString();
		}
		return name;
	}

	public static String getCanonicalPath(Path path) {
		String name = getCanonicalPath(path.toString());
		if (path instanceof HadoopPath) {
			if (name.startsWith(FileHadoop.hdfsSymbol)) {
				name = "/" + name;
			} else if (!name.startsWith(FileHadoop.getHdfsSymbol())) {
				name = "/hdfs:" + name;
			}
		} else if (path instanceof OssPath) {
			name = path.toString();
		}
		return name;
	}

	public static String getCanonicalPath(String fileName) {
		boolean isAddSplashHead = false;
		if (fileName.startsWith(FileHadoop.hdfsSymbol)) {
			if (!fileName.startsWith("/")) {
				fileName = "/" + fileName;
				isAddSplashHead = true;
			}
		} else if (fileName.startsWith("file://")) {
			fileName = fileName.replaceFirst("file://", "");
		}
		File file = new File(fileName);
		try {
			String canonicalPath = file.getCanonicalPath();
			if (isAddSplashHead) {
				canonicalPath = removeSplashHead(canonicalPath, true);
			}
			return canonicalPath;
		} catch (IOException e) {
			throw new ExceptionFileError("cannot getCanonicalPath " + fileName, e);
		}
	}

	/**
	 * 获取文件夹下包含指定文件名与后缀的所有文件名,等待增加功能子文件夹下的文件。也就是循环获得文件<br>
	 * 如果文件不存在则返回空的list<br>
	 * 如果不是文件夹，则返回该文件名<br>
	 * 
	 * @param filePath
	 *            目录路径
	 * @param filename
	 *            指定包含的文件名，是正则表达式 ，如 "*",正则表达式无视大小<br>
	 *            null 表示不指定
	 * @param suffix
	 *            指定包含的后缀名，是正则表达式<br>
	 *            文件 wfese.fse.fe认作 "wfese.fse"和"fe"<br>
	 *            文件 wfese.fse.认作 "wfese.fse."和""<br>
	 *            文件 wfese 认作 "wfese"和""<br>
	 *            null 表示不指定
	 * @return 返回包含目标文件全名的ArrayList
	 * @throws IOException
	 */
	@Deprecated
	public static List<File> getFoldFileLs(String filePath, String filename, String suffix) {
		filePath = removeSep(filePath);
		File file = getFile(filePath);
		if (filePath.equals("")) {
			filePath = file.getAbsolutePath();
			file = getFile(filePath);
		}
		return getFoldFileLs(file, filename, suffix);
	}

	/**
	 * 获取文件夹下包含指定文件名与后缀的所有文件名，仅找第一层，不递归<br>
	 * 如果文件不存在则返回空的list<br>
	 * 如果不是文件夹，则返回该文件名<br>
	 * 
	 * @param filePath
	 *            目录路径
	 * @param filename
	 *            指定包含的文件名，是正则表达式 ，如 "*",正则表达式无视大小<br>
	 *            null 表示不指定
	 * @param suffix
	 *            指定包含的后缀名，是正则表达式<br>
	 *            文件 wfese.fse.fe认作 "wfese.fse"和"fe"<br>
	 *            文件 wfese.fse.认作 "wfese.fse."和""<br>
	 *            文件 wfese 认作 "wfese"和""<br>
	 *            null 表示不指定
	 * @return 返回包含目标文件全名的ArrayList
	 * @throws IOException
	 */
	public static List<Path> getLsFoldPath(String filePath, String filename, String suffix) {
		return getLsFoldPath(getPath(filePath), filename, suffix);
	}

	/**
	 * 获取文件夹下全部文件名
	 * 
	 * @return 返回包含目标文件全名的List
	 * @throws IOException
	 */
	@Deprecated
	public static List<File> getFoldFileLs(File file) {
		return getFoldFileLs(file, "*", "*");
	}

	/**
	 * 获取文件夹下全部文件名
	 * 
	 * @return 返回包含目标文件全名的List
	 * @throws IOException
	 */
	@Deprecated
	public static List<File> getFoldFileLs(String file) {
		return getFoldFileLs(file, "*", "*");
	}

	/**
	 * 获取文件夹下包含指定文件名与后缀的所有文件名,等待增加功能子文件夹下的文件。也就是循环获得文件<br>
	 * 如果文件不存在则返回空的list<br>
	 * 如果不是文件夹，则返回该文件名<br>
	 * 
	 * @param filePath
	 *            目录路径
	 * @param filename
	 *            指定包含的文件名，是正则表达式 ，如 "*",正则表达式无视大小<br>
	 *            null 表示不指定
	 * @param suffix
	 *            指定包含的后缀名，是正则表达式<br>
	 *            文件 wfese.fse.fe认作 "wfese.fse"和"fe"<br>
	 *            文件 wfese.fse.认作 "wfese.fse."和""<br>
	 *            文件 wfese 认作 "wfese"和""<br>
	 *            null 表示不指定
	 * @return 返回包含目标文件全名的ArrayList
	 * @throws IOException
	 */
	@Deprecated
	public static List<File> getFoldFileLs(File file, String filename, String suffix) {
		if (filename == null || filename.equals("*")) {
			filename = ".*";
		}
		if (suffix == null || suffix.equals("*")) {
			suffix = ".*";
		}
		FileFilterNBC fileFilterNBC = new FileFilterNBC(filename, suffix);
		List<File> lsFilenames = new ArrayList<>();

		if (!file.exists()) {// 没有文件，则返回空
			return new ArrayList<>();
		}
		// 如果只是文件则返回文件名
		if (!file.isDirectory()) { // 获取文件名与后缀名
			if (fileFilterNBC.accept(file)) {
				lsFilenames.add(file);
				return lsFilenames;
			}
		}

		File[] result = file.listFiles(fileFilterNBC);

		for (File file2 : result) {
			lsFilenames.add(file2);
		}
		return lsFilenames;
	}

	public static class FileFilterNBC implements FileFilter {
		PatternOperate patName;
		PatternOperate patSuffix;

		public FileFilterNBC(String fileNameRegex, String suffixRegex) {
			patName = new PatternOperate(fileNameRegex, false);
			// 开始判断
			patSuffix = new PatternOperate(suffixRegex, false);
		}

		@Override
		public boolean accept(File pathname) {
			String[] fileNameSep = getFileNameSepWithoutPath(pathname.getName());
			if (patName.getPatFirst(fileNameSep[0]) != null && patSuffix.getPatFirst(fileNameSep[1]) != null) {
				return true;
			}
			return false;
		}

	}

	/**
	 * 获取文件夹下包含指定文件名与后缀的所有文件名，仅找第一层，不递归<br>
	 * 如果文件不存在则返回空的list<br>
	 * 如果不是文件夹，则返回该文件名<br>
	 * 
	 * @param fileName
	 *            目录路径
	 * @return 返回包含目标文件全名的ArrayList
	 * @throws IOException
	 */
	public static List<Path> getLsFoldPath(String fileName) {
		return getLsFoldPath(getPath(fileName), "*", "*");
	}

	/**
	 * 获取文件夹下包含指定文件名与后缀的所有文件名，仅找第一层，不递归<br>
	 * 如果文件不存在则返回空的list<br>
	 * 如果不是文件夹，则返回该文件名<br>
	 * 
	 * @param file
	 *            目录路径
	 * @return 返回包含目标文件全名的ArrayList
	 * @throws IOException
	 */
	public static List<Path> getLsFoldPath(Path file) {
		return getLsFoldPath(file, "*", "*");
	}

	/**
	 * 获取文件夹下包含指定文件名与后缀的所有文件名<b>递归查找</b><br>
	 * 如果文件不存在则返回空的list<br>
	 * 如果不是文件夹，则返回该文件名<br>
	 * 
	 * @param file
	 *            目录路径
	 * @param isNeedFolder
	 *            是否需要把文件夹也记录下来
	 * @return 返回包含目标文件全名的ArrayList
	 * @throws IOException
	 */
	public static List<Path> getLsFoldPathRecur(String file, boolean isNeedFolder) {
		return getLsFoldPathRecur(FileOperate.getPath(file), "*", "*", isNeedFolder);
	}
	/**
	 * 获取文件夹下包含指定文件名与后缀的所有文件名<b>递归查找</b><br>
	 * 如果文件不存在则返回空的list<br>
	 * 如果不是文件夹，则返回该文件名<br>
	 * 
	 * @param file
	 *            目录路径
	 * @param isNeedFolder
	 *            是否需要把文件夹也记录下来
	 * @return 返回包含目标文件全名的ArrayList
	 * @throws IOException
	 */
	public static List<String> getLsFoldPathRecurStr(String file, boolean isNeedFolder) {
		List<Path> lsPaths = getLsFoldPathRecur(FileOperate.getPath(file), "*", "*", isNeedFolder);
		List<String> lsResult = new ArrayList<>();
		lsPaths.forEach((path) -> {
			lsResult.add(getAbsolutePath(path));
		});
		return lsResult;
	}
	/**
	 * 获取文件夹下包含指定文件名与后缀的所有文件名<b>递归查找</b><br>
	 * 如果文件不存在则返回空的list<br>
	 * 如果不是文件夹，则返回该文件名<br>
	 * 
	 * @param file
	 *            目录路径
	 * @param isNeedFolder
	 *            是否需要把文件夹也记录下来
	 * @return 返回包含目标文件全名的ArrayList
	 * @throws IOException
	 */
	public static List<Path> getLsFoldPathRecur(Path file, boolean isNeedFolder) {
		return getLsFoldPathRecur(file, "*", "*", isNeedFolder);
	}

	/**
	 * 获取文件夹下包含指定文件名与后缀的所有文件名，<b>递归查找</b><br>
	 * 如果文件不存在则返回空的list<br>
	 * 如果不是文件夹，则返回该文件名<br>
	 * 
	 * @param file
	 *            目录路径
	 * @param filename
	 *            指定包含的文件名，是正则表达式 ，如 "*",正则表达式无视大小<br>
	 *            null 表示不指定
	 * @param boolean
	 *            指定包含的后缀名，是正则表达式<br>
	 *            文件 wfese.fse.fe认作 "wfese.fse"和"fe"<br>
	 *            文件 wfese.fse.认作 "wfese.fse."和""<br>
	 *            文件 wfese 认作 "wfese"和""<br>
	 *            null 表示不指定
	 * @param isNeedFolder
	 *            是否需要把文件夹也记录下来
	 * @return 返回包含目标Path 的ArrayList
	 * @throws IOException
	 */
	public static List<Path> getLsFoldPathRecur(Path file, String filename, String suffix, boolean isNeedFolder) {
		List<Path> lsPath = new ArrayList<>();

		int noNeedReg = 0;
		if (filename == null || filename.equals("*")) {
			filename = ".*";
			noNeedReg++;
		}
		if (suffix == null || suffix.equals("*")) {
			suffix = ".*";
			noNeedReg++;
		}
		if (file == null || !Files.exists(file)) {
			return new ArrayList<>();
		}
		
		PredicateFileName predicateFileName = null;
		if (noNeedReg != 2) {
			//等于2就是匹配所有，不需要正则了.
			predicateFileName = new PredicateFileName(filename, suffix);
			predicateFileName.setFilterFolder(false);
		}

		// 如果只是文件则返回文件名
		if (!Files.isDirectory(file)) { // 获取文件名与后缀名
			if (predicateFileName == null) {
				lsPath.add(file);
			} else if (predicateFileName != null && predicateFileName.test(file)) {
				lsPath.add(file);
			}
			return lsPath;
		}
		try {
			Stream<Path> streamPath = Files.list(file);
			List<Path> lsPathTmp = null;
			if (predicateFileName != null) {
				lsPathTmp = streamPath.filter(predicateFileName).collect(Collectors.toList());
			} else {
				lsPathTmp = streamPath.collect(Collectors.toList());
			}
			streamPath.close();
			for (Path path : lsPathTmp) {
				if ((path instanceof OssPath && path.toString().endsWith("/")) || (Files.isDirectory(path))) {
					// 是ossPath，只要是以/结尾的.就是文件夹。不是osspath,则需查找判断一下
					lsPath.addAll(getLsFoldPathRecur(path, filename, suffix, isNeedFolder));
					if (isNeedFolder && !lsPath.contains(path)) {
						lsPath.add(path);
					}
				} else {
					lsPath.add(path);
				}
			}
			return lsPath;
		} catch (IOException e) {
			throw new ExceptionFileError("cannot get sub files of " + file.toString());
		}
	}

	/**
	 * 获取文件夹下包含指定文件名与后缀的所有文件名，<b>仅找第一层，不递归</b><br>
	 * 如果文件不存在则返回空的list<br>
	 * 如果不是文件夹，则返回该文件名<br>
	 * 
	 * @param file
	 *            目录路径
	 * @param filename
	 *            指定包含的文件名，是正则表达式 ，如 "*",正则表达式无视大小<br>
	 *            null 表示不指定
	 * @param suffix
	 *            指定包含的后缀名，是正则表达式<br>
	 *            文件 wfese.fse.fe认作 "wfese.fse"和"fe"<br>
	 *            文件 wfese.fse.认作 "wfese.fse."和""<br>
	 *            文件 wfese 认作 "wfese"和""<br>
	 *            null 表示不指定
	 * @return 返回包含目标文件全名的ArrayList
	 * @throws IOException
	 */
	public static List<Path> getLsFoldPath(Path file, String filename, String suffix) {
		List<Path> lsFilenames = new ArrayList<>();
		
		int noNeedReg = 0;
		if (filename == null || filename.equals("*")) {
			filename = ".*";
		}
		if (suffix == null || suffix.equals("*")) {
			suffix = ".*";
		}
		if (file == null || !Files.exists(file)) {
			return new ArrayList<>();
		}
		
		PredicateFileName predicateFileName = null;
		if (noNeedReg != 2) {
			//等于2就是匹配所有，不需要正则了.
			predicateFileName = new PredicateFileName(filename, suffix);
		}
		// 如果只是文件则返回文件名
		if (!Files.isDirectory(file)) { // 获取文件名与后缀名
			if (predicateFileName == null) {
				lsFilenames.add(file);
			} else if (predicateFileName != null && predicateFileName.test(file)) {
				lsFilenames.add(file);
			}
			return lsFilenames;
		}
		try {
			Stream<Path> streamPath = Files.list(file);
			if (predicateFileName != null) {
				lsFilenames = streamPath.filter(predicateFileName).collect(Collectors.toList());
			} else {
				lsFilenames = streamPath.collect(Collectors.toList());
			}
			streamPath.close();
			return lsFilenames;
		} catch (IOException e) {
			throw new ExceptionFileError("cannot get sub files of " + file.toString());
		}
	}

	private static class PredicateFileName implements Predicate<Path> {
		PatternOperate patName;
		PatternOperate patSuffix;

		boolean isFilterFolder = true;

		public PredicateFileName(String fileNameRegex, String suffixRegex) {
			if (!StringOperate.isRealNull(fileNameRegex) && !fileNameRegex.equalsIgnoreCase("*")) {
				patName = new PatternOperate(fileNameRegex, false);
			}
			if (!StringOperate.isRealNull(suffixRegex) && !suffixRegex.equalsIgnoreCase("*")) {
				patSuffix = new PatternOperate(suffixRegex, false);
			}
		}

		/** 是否用正则表达式过滤文件夹 */
		public void setFilterFolder(boolean isFilterFolder) {
			this.isFilterFolder = isFilterFolder;
		}

		@Override
		public boolean test(Path t) {
			if (!isFilterFolder && Files.isDirectory(t)) {
				return true;
			}
			String fileName = getFileName(t);
			String[] fileNameSep = getFileNameSepWithoutPath(t.toString());
			if (patSuffix == null && patName.getPat(fileName) != null) {
				return true;
			}

			boolean isNameOk = (patName == null || patName.getPatFirst(fileNameSep[0]) != null);
			boolean isSuffixOk = (patSuffix == null || patSuffix.getPatFirst(fileNameSep[1]) != null);
			return isNameOk && isSuffixOk;
		}
	}

	/**
	 * 获取文件夹下包含指定文件名与后缀的所有文件名，仅找第一层，不递归<br>
	 * 如果文件不存在则返回null<br>
	 * 如果不是文件夹，则返回该文件名<br>
	 * 
	 * @param filePath
	 * @return
	 */
	public static ArrayList<String> getLsFoldFileName(String filePath) {
		List<Path> lsPaths = getLsFoldPath(getPath(filePath), "*", "*");
		ArrayList<String> lsResult = new ArrayList<>();
		lsPaths.forEach((path) -> {
			lsResult.add(getAbsolutePath(path));
		});
		return lsResult;
	}

	/**
	 * 获取文件夹下包含指定文件名与后缀的所有文件名，仅找第一层，不递归<br>
	 * 如果文件不存在则返回null<br>
	 * 如果不是文件夹，则返回该文件名<br>
	 * 
	 * @param filePath
	 * @param deepth
	 *            指定深度
	 * @return
	 */
	public static ArrayList<String> getLsFoldFileName(String filePath, String filename, String suffix) {
		List<Path> lsPaths = getLsFoldPath(getPath(filePath), filename, suffix);
		ArrayList<String> lsResult = new ArrayList<>();
		lsPaths.forEach((path) -> {
			lsResult.add(getAbsolutePath(path));
		});
		return lsResult;
	}

	/**
	 * 获取文件夹下包含指定文件名与后缀的所有文件名，仅找第一层，不递归<br>
	 * 如果文件不存在则返回null<br>
	 * 如果不是文件夹，则返回该文件名<br>
	 * 
	 * @param filePath
	 * @param deepth
	 *            指定深度
	 * @return
	 */
	public static ArrayList<String> getLsFoldFileName(Path filePath, String filename, String suffix) {
		List<Path> lsPaths = getLsFoldPath(filePath, filename, suffix);
		ArrayList<String> lsResult = new ArrayList<>();
		lsPaths.forEach((path) -> {
			lsResult.add(getAbsolutePath(path));
		});
		return lsResult;
	}

	/**
	 * 获取文件夹下包含指定文件名与后缀的所有文件名，仅找第一层，不递归<br>
	 * 如果文件不存在则返回null<br>
	 * 如果不是文件夹，则返回该文件名<br>
	 * 
	 * @param filePath
	 * @param deepth
	 *            指定深度
	 * @return
	 */
	public static ArrayList<String> getLsFoldFileName(Path filePath) {
		List<Path> lsPaths = getLsFoldPath(filePath, "*", "*");
		ArrayList<String> lsResult = new ArrayList<>();
		lsPaths.forEach((path) -> {
			lsResult.add(getAbsolutePath(path));
		});
		return lsResult;
	}

	/**
	 * 给定一个文件名，返回该文件名上有几个文件夹，不包括本文件夹 如 给定 /home/novelbio/test 和
	 * /home/novelbio/test/ 都返回2<br>
	 * 此外 如 mypath/novelbio/test 和 mypath/novelbio/test/ 也都返回2
	 * 
	 * @param fileName
	 * @return
	 */
	public static int getFolderParentNumber(String fileName) {
		if (StringOperate.isRealNull(fileName))
			return 0;
		fileName = fileName.replace("\\", "/").replace("//", "/");
		fileName = removeSplashHead(fileName, false);
		fileName = removeSplashTail(fileName, false);
		return fileName.split("/").length - 1;
	}

	/**
	 * 新建目录,如果新文件夹存在也返回ture
	 * 
	 * @param folderPath
	 *            目录路径,最后不要加\\或/
	 * @return 返回目录创建后的路径
	 */
	public static void createFolders(String folderPath) {
		createFolders(getPath(folderPath));
	}

	public static void createFolders(Path path) {
		try {
			if (path == null) {
				return;
			}
			if (isFileDirectory(path))
				return;
			// 这里再判定一次，因为有可能别的程序在这时候新建了一个文件夹
			if (isFileExistAndNotDir(path))
				throw new ExceptionFileError("folderPath is an exist file " + path);

			Files.createDirectories(path);
		} catch (IOException e) {
			throw new ExceptionFileError("cannot creat path " + path, e);
		}
	}

	public static SeekableByteChannel getSeekableByteChannel(Path path) {
		try {
			return Files.newByteChannel(path);
		} catch (IOException e) {
			throw new ExceptionFileError("cannot get channel on file " + path, e);
		}
	}

	public static SeekablePathInputStream getInputStreamSeekable(String filePath) throws FileNotFoundException {
		return new SeekablePathInputStream(getPath(filePath));
	}

	public static InputStream getInputStream(String filePath) throws IOException {
		return getInputStream(getPath(filePath));
	}

	public static InputStream getInputStream(File filePath) throws IOException {
		return getInputStream(getPath(filePath));
	}

	public static InputStream getInputStream(Path file) throws IOException {
		return Files.newInputStream(file);
	}

	public static SeekablePathInputStream getSeekablePathInputStream(Path path) {
		return new SeekablePathInputStream(path);
	}

	public static OutputStream getOutputStream(String filePath) throws IOException {
		return getOutputStream(filePath, false);
	}

	public static OutputStream getOutputStream(String filePath, boolean append) throws IOException {
		return getOutputStream(getPath(filePath), append);
	}

	public static OutputStream getOutputStream(File filePath, boolean append) throws IOException {
		return getOutputStream(getPath(filePath), append);
	}

	public static OutputStream getOutputStream(File file) throws IOException {
		return getOutputStream(file, false);
	}

	public static OutputStream getOutputStream(Path file) throws IOException {
		return getOutputStream(file, false);
	}

	public static OutputStream getOutputStream(Path file, boolean append) throws IOException {
		StandardOpenOption openOption = append ? StandardOpenOption.APPEND : StandardOpenOption.CREATE;
		if (!FileOperate.isFileFolderExist(file)) {
			openOption = StandardOpenOption.CREATE;
		}
		if (append == false && FileOperate.isFileExistAndBigThan0(file)) {
			if (FileOperate.isFileDirectory(file)) {
				throw new ExceptionNbcFile("cannot create outputstream on folder " + file.toString());
			}
			FileOperate.deleteFileFolder(file);
		}
		return Files.newOutputStream(file, openOption);
	}

	public static void copyFileFolder(String oldPathFile, String newPathFile, boolean cover) {
		boolean isFolder = FileOperate.isFileDirectory(oldPathFile);
		Path oldfile = getPath(oldPathFile);
		if (isFolder) {
			copyFolder(oldfile, newPathFile, cover);
		} else {
			copyFile(oldfile, newPathFile, cover);
		}
	}

	public static void copyFileFolder(Path oldfile, String newPathFile, boolean cover) {
		boolean isFolder = FileOperate.isFileDirectory(oldfile);
		if (isFolder) {
			copyFolder(oldfile, newPathFile, cover);
		} else {
			copyFile(oldfile, newPathFile, cover);
		}
	}

	/**
	 * 复制整个文件夹的内容
	 * 
	 * @param oldPath
	 *            准备拷贝的目录，最后都无所谓加不加"/"
	 * @param newPath
	 *            指定绝对路径的新目录
	 * @return
	 */
	public static boolean copyFolder(Path oldFilePath, String newPath, boolean cover) {
		final String newPathSep = addSep(newPath);
		if (!FileOperate.isFileFolderExist(oldFilePath)) {
			logger.error(oldFilePath + " is not exist");
		}
		if (!FileOperate.isFileDirectory(oldFilePath)) {
			logger.error(oldFilePath + " is not a folder");
			return false;
		}
		try {
			createFolders(newPathSep);
			Files.list(oldFilePath).forEach((pathOld) -> {
				if (isFileDirectory(pathOld)) {
					copyFolder(pathOld, newPathSep + pathOld.getFileName(), cover);
				} else {
					copyFile(pathOld, newPathSep + pathOld.getFileName(), cover);
				}
			});
		} catch (Exception e) {
			throw new ExceptionNbcFile("copy fold error", e);
		}
		return true;
	}

	/**
	 * 复制单个文件
	 * 
	 * @param oldPathFile
	 *            准备复制的文件源
	 * @param newPathFile
	 *            拷贝到新绝对路径带文件名
	 * @param cover
	 *            是否覆盖
	 * @return
	 */
	public static void copyFile(String oldPathFile, String newPathFile, boolean cover) {
		Path oldfile = getPath(oldPathFile);
		copyFile(oldfile, newPathFile, cover);
	}

	/**
	 * 复制单个文件
	 * 
	 * @param oldPathFile
	 *            准备复制的文件源
	 * @param newPathFile
	 *            拷贝到新绝对路径带文件名
	 * @param cover
	 *            是否覆盖
	 * @return
	 */
	public static void copyFile(Path oldfile, String newfile, boolean cover) {
		Path pathNew = getPath(newfile);
		copyFile(oldfile, pathNew, cover);
	}

	/**
	 * 复制单个文件
	 * 
	 * @param oldPathFile
	 *            准备复制的文件源
	 * @param newPathFile
	 *            拷贝到新绝对路径带文件名
	 * @param cover
	 *            是否覆盖
	 * @return
	 */
	public static void copyFile(Path oldfile, Path pathNew, boolean cover) {
		if (!isFileExistAndNotDir(oldfile)) {
			throw new ExceptionNbcFile("no file exist: " + oldfile);
		}
		if (oldfile != null && isFilePathSame(getAbsolutePath(oldfile), getAbsolutePath(pathNew)))
			return;
		if (!cover && Files.exists(pathNew))
			return;
		if (cover && isFileDirectory(pathNew)) {
			throw new ExceptionFileError("cannot cover directory " + pathNew);
		}
		Path pathNewTmp = getPath(FileOperate.changeFileSuffix(getAbsolutePath(pathNew), "_tmp", null));
		try {
			Files.deleteIfExists(pathNew);
			Files.deleteIfExists(pathNewTmp);
			createFolders(FileOperate.getPathName(pathNew));
			Files.copy(oldfile, pathNewTmp, StandardCopyOption.REPLACE_EXISTING);
			Files.move(pathNewTmp, pathNew, StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			throw new ExceptionNbcFile("copy file from " + oldfile + " to " + pathNew + " error", e);
		}
	}

	/**
	 * 文件改名,如果已有同名文件存在，则不改名并返回
	 * 
	 * @param oldName
	 *            包含全部路径的文件名
	 * @param newName
	 *            要修改的文件名,不包含路径
	 * @return
	 */
	@Deprecated
	public static void changeFileName(String oldName, String newName) {
		changeFileName(oldName, newName, false);
	}

	/**
	 * 文件改名,如果已有同名文件存在，则不改名并返回
	 * 
	 * @param oldName
	 *            包含全部路径的文件名
	 * @param newName
	 *            要修改的文件名,不包含路径
	 * @return
	 */
	@Deprecated
	public static void changeFileName(String oldName, String newName, boolean cover) {
		// 文件原地址
		Path oldFile = getPath(oldName);
		// 文件新（目标）地址
		Path fnew = getPath(oldFile.getParent() + File.separator + newName);
		if (isFilePathSame(oldFile.toString(), fnew.toString())) {
			return;
		}
		if (Files.exists(fnew) && !cover) {
			return;
		}
		deleteFileFolder(fnew);
		fnew = getPath(oldFile.getParent() + File.separator + newName);
		moveFile(cover, oldFile, fnew);
	}

	/**
	 * 只修输入的文件名，并不直接操作文件 文件添加<b>前缀</b>并改后缀名，如果一样则不修改 如果文件以“/”结尾，则直接添加后缀
	 * 
	 * @param FileName
	 *            原来文件的全名
	 * @param append
	 *            要添加的后缀，譬如_1，_new，如果为null，则不添加
	 * @param suffix
	 *            要添加的后缀名，譬如 txt， jpg ，自动去空格 suffix == null则不改变后缀名，suffix = ""
	 *            则去除后缀名
	 */
	public static String changeFilePrefix(String fileName, String append, String suffix) {
		if (append == null) {
			append = "";
		}
		suffix = getSuffixChange(fileName, suffix);
		int indexSep = Math.max(fileName.lastIndexOf("/"), fileName.lastIndexOf("\\"));
		String parentPath = "";
		if (indexSep >= 0) {
			parentPath = fileName.substring(0, indexSep + 1);
		}

		String fileNameNoSuffix = getFileNameSep(fileName)[0];

		return parentPath + append + fileNameNoSuffix + suffix;
	}

	/**
	 * 直接操作文件 文件添加<b>前缀</b>并改后缀名，如果一样则不修改
	 * 
	 * @param fileName
	 *            原来文件的全名
	 * @param append
	 *            要添加的后缀，譬如_1，_new，如果为null，则不添加
	 * @param suffix
	 *            要添加的后缀名，譬如 txt， jpg ，自动去空格 suffix == null则不改变后缀名，suffix = ""
	 *            则去除后缀名
	 */
	// TODO 待测试
	public static String changeFilePrefixReal(String fileName, String append, String suffix) {
		String newFile = changeFilePrefix(fileName, append, suffix);
		moveFile(true, fileName, newFile);
		return newFile;
	}

	/**
	 * 只修输入的文件名，并不直接操作文件 文件添加<b>后缀</b>并改后缀名，如果一样则不修改<br>
	 * 可以修改输入的uri
	 * 
	 * @param FileName
	 *            原来文件的全名
	 * @param append
	 *            要添加的后缀，譬如_1，_new，如果为null，则不添加
	 * @param suffix
	 *            要添加的后缀名，譬如 txt， jpg ，自动去空格 suffix == null则不改变后缀名，suffix = ""
	 *            则去除后缀名
	 */
	public static String changeFileSuffix(String fileName, String append, String suffix) {
		if (append == null) {
			append = "";
		}
		suffix = getSuffixChange(fileName, suffix);

		int endDot = fileName.lastIndexOf(".");
		int indexSep = Math.max(fileName.lastIndexOf("/"), fileName.lastIndexOf("\\"));
		String result;
		if (endDot > indexSep) {
			result = fileName.substring(0, endDot);
		} else {
			result = fileName;
		}
		return result + append + suffix;
	}

	/**
	 * 
	 * 只修输入的文件名，并不直接操作文件 文件添加<b>后缀</b>并改后缀名，如果一样则不修改<br>
	 * 可以修改输入的uri
	 * 
	 * @param fileName
	 *            原来文件的全名
	 * @param append
	 *            要添加的后缀，譬如_1，_new，如果为null，则不添加
	 * @param suffixOld
	 *            以前的后缀名，可以是txt，txt.gz，fq.gz等多个连在一起的名字，也可以实际上是bed.gz，但只写bed<br>
	 *            如果可能存在不确定的后缀，可以用竖线隔开，如 fq|fastq <b>无所谓大小写</b>
	 * @param suffixNew
	 *            新的后缀全名， suffix == null则不改变后缀名，suffix = ""表示删除后缀
	 * @return
	 */
	public static String changeFileSuffix(String fileName, String append, String suffixOld, String suffixNew) {
		if (append == null) {
			append = "";
		}
		if (suffixOld == null) {
			suffixOld = "";
		}
		String[] suffixOlds = suffixOld.split("\\|");
		for (int i = 0; i < suffixOlds.length; i++) {
			if (!suffixOlds[i].startsWith(".")) {
				suffixOlds[i] = "." + suffixOlds[i];
			}
		}
		int maxEndDot = -1;
		for (String suf : suffixOlds) {
			int endDot = fileName.toLowerCase().lastIndexOf(suf.toLowerCase());
			if (endDot >= 0 && endDot > maxEndDot) {
				maxEndDot = endDot;
			}
		}

		suffixOld = maxEndDot < 0 ? "" : fileName.substring(maxEndDot, fileName.length());

		if (suffixNew == null) {
			suffixNew = suffixOld;
		}
		if (!suffixNew.startsWith(".")) {
			suffixNew = "." + suffixNew;
		}
		int indexSep = Math.max(fileName.lastIndexOf("/"), fileName.lastIndexOf("\\"));
		String result;
		if (maxEndDot > indexSep) {
			result = fileName.substring(0, maxEndDot);
		} else {
			result = fileName;
		}
		return result + append + suffixNew;
	}

	/**
	 * 输入文件名和需要修改的后缀，如果后缀为null则返回原来的后缀，否则返回新的后缀 后缀加上"."
	 * 
	 * @param fileName
	 * @param suffix
	 * @return
	 */
	private static String getSuffixChange(String fileName, String suffix) {
		if (suffix == null && !fileName.endsWith("/") && !fileName.endsWith("\\")) {
			String[] fileNameSep = getFileNameSep(fileName);
			suffix = fileNameSep[1];
		}
		if (suffix == null) {
			suffix = "";
		}
		suffix = suffix.trim();
		if (!suffix.equals("")) {
			suffix = "." + suffix;
		}
		return suffix;
	}

	/**
	 * 直接操作文件 文件添加<b>后缀</b>并改后缀名，如果一样则不修改
	 * 
	 * @param fileName
	 *            原来文件的全名
	 * @param append
	 *            要添加的后缀，譬如_1，_new，如果为null，则不添加
	 * @param suffix
	 *            要添加的后缀名，譬如 txt， jpg ，自动去空格 suffix == null则不改变后缀名，suffix = ""
	 *            则去除后缀名
	 */
	public static String changeFileSuffixReal(String fileName, String append, String suffix) {
		String newFile = changeFileSuffix(fileName, append, suffix);
		moveFile(true, fileName, newFile);
		return newFile;
	}

	/**
	 * 移动文件或文件夹，如果新地址有同名文件，则不移动并返回<br>
	 * 可以创建一级新文件夹<br>
	 * 如果没有文件则返回<br>
	 * 
	 * @param oldPath
	 *            文件路径
	 * @param newPath
	 *            新文件所在的文件夹
	 * @return 是否移动成功
	 */
	public static void moveFile(String oldPath, String newPath, boolean cover) {
		moveFile(cover, oldPath, newPath);
	}

	/**
	 * @param cover
	 *            是否覆盖
	 * @param oldFileName
	 *            老文件全路径
	 * @param newFileName
	 *            新文件全路径
	 * @return
	 */
	public static void moveFile(boolean cover, String oldFileName, String newFileName) {
		moveFile(FileOperate.getPath(oldFileName), newFileName, cover);
	}

	/**
	 * @param cover
	 *            是否覆盖
	 * @param oldFileName
	 *            老文件全路径
	 * @param newFileName
	 *            新文件全路径
	 * @return
	 */
	public static void moveFile(boolean cover, Path oldPath, Path newFileName) {
		String newPath = getAbsolutePath(newFileName);
		moveFile(oldPath, newPath, cover);
	}

	/**
	 * @param oldFileName
	 * @param newPath
	 * @param NewName
	 *            新文件或文件夹名
	 * @param cover
	 * @return
	 */
	// TODO 待测试
	public static void moveFile(String oldFileName, String newPath, String NewName, boolean cover) {
		newPath = addSep(newPath);
		if (NewName == null || NewName.trim().equals("")) {
			NewName = getFileName(oldFileName);
		}
		String newFileName = newPath + NewName;
		Path oldFile = getPath(oldFileName);
		moveFile(oldFile, newFileName, cover);
	}

	/**
	 * @param oldFileName
	 * @param newPath
	 * @param NewName
	 *            新文件或文件夹名
	 * @param cover
	 * @return
	 */
	// TODO 待测试
	private static void moveFile(Path oldFile, String newPathName, boolean cover) {
		Path pathNew = FileOperate.getPath(newPathName);
		if (isFileExistAndNotDir(oldFile)) {
			moveSingleFile(oldFile, pathNew, cover);
		} else if (isFileDirectory(oldFile)) {
			moveFoldFile(oldFile, newPathName, cover);
		}
	}

	/**
	 * 移动文件，如果新地址有同名文件，则不移动并返回<br>
	 * 可以创建一级新文件夹<br>
	 * 如果没有文件则返回<br>
	 * 注意：新文件夹后不要加\\<br>
	 * 
	 * @param oldPath
	 *            文件路径
	 * @param newPath
	 *            新文件名
	 * @param cover
	 *            是否覆盖
	 * @return true 成功 false 失败
	 */
	// TODO 待测试
	private static void moveSingleFile(Path oldPath, Path newPath, boolean cover) {
		if (!isFileExistAndNotDir(oldPath))
			return;
		if (isFilePathSame(oldPath.toUri().toString(), newPath.toUri().toString())) {
			return;
		}

		// 文件新（目标）地址
		// new一个新文件夹
		Path fnewpathParent = newPath.getParent();
		createFolders(fnewpathParent);
		try {
			if (Files.exists(newPath)) {
				if (!cover)
					return;
				Files.deleteIfExists(newPath);
			}
			Files.move(oldPath, newPath);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ExceptionFileError("cannot move file " + oldPath + " to " + newPath, e);
		}
	}

	public static void moveFoldFile(String olddir, String newfolder, boolean cover) {
		moveFoldFile(olddir, newfolder, "", cover);
	}

	/**
	 * 移动指定文件夹内的全部文件，如果目标文件夹下有重名文件，则跳过，同时返回false<br/>
	 * 如果新文件夹不存在，就创建新文件夹，不过似乎只能创建一级文件夹。移动顺利则返回true
	 * 
	 * @param oldfolderfile
	 * @param newfolderfile
	 *            目标文件目录
	 * @param prix
	 *            在文件前加上的前缀
	 * @param cover
	 *            是否覆盖
	 * @throws Exception
	 */
	// TODO 修改文件名，改成movefolder
	// TODO 考虑增加movefile，自动判定单文件还是文件夹
	public static void moveFoldFile(String oldfolderfile, String newfolder, String prix, boolean cover) {
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		oldfolderfile = addSep(oldfolderfile);

		Path olddir = getPath(oldfolderfile);
		moveFoldFile(olddir, newfolder, prix, cover);
	}

	private static void moveFoldFile(Path olddir, String newfolder, boolean cover) {
		moveFoldFile(olddir, newfolder, "", cover);
	}

	/**
	 * 移动指定文件夹内的全部文件，如果目标文件夹下有重名文件，则跳过，同时返回false<br/>
	 * 如果新文件夹不存在，就创建新文件夹，不过似乎只能创建一级文件夹。移动顺利则返回true
	 * 
	 * @param olddir
	 * @param newfolder
	 *            目标文件目录
	 * @param prix
	 *            在文件前加上的前缀
	 * @param cover
	 *            是否覆盖
	 * @throws Exception
	 */
	private static void moveFoldFile(Path olddir, String newfolder, String prix, boolean cover) {
		if (!FileOperate.isFileFolderExist(olddir)) {
			logger.error(olddir + " is not exist");
		}
		if (!FileOperate.isFileDirectory(olddir)) {
			logger.error(olddir + " is not a folder");
			return;
		}

		final String prefix = StringOperate.isRealNull(prix) ? "" : prix;

		final String newPathSep = addSep(newfolder);
		Path pathNew = getPath(newPathSep);

		if (isFileExistAndNotDir(pathNew)) {
			if (cover) {
				try {
					Files.delete(pathNew);
				} catch (Exception e) {
					throw new ExceptionFileError("cannot move file from " + olddir + " to " + newfolder
							+ " because cannot delete file " + pathNew, e);
				}
			} else {
				return;
			}
		}
		String olddirPathTmp = removeSplashHead(olddir.toString(), false);
		String olddirPath = removeSplashTail(olddirPathTmp, false);
		final boolean[] isMakeDirSameAsOld = new boolean[] { false };
		
		if (!FileOperate.isFileFolderExist(pathNew)) {
			try {
				Files.move(olddir, pathNew);
				return;
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
		
		try {
			createFolders(pathNew);
			Files.list(olddir).forEach((pathOld) -> {
				if (isFileDirectory(pathOld)) {
					String newPath = removeSplashHead(newPathSep + pathOld.getFileName(), false);
					newPath = removeSplashTail(newPath, false);
					if (newPath.equals(olddirPath)) {
						isMakeDirSameAsOld[0] = true;
					}
					moveFoldFile(pathOld, newPathSep + pathOld.getFileName(), prefix, cover);
				} else {
					Path newPath = getPath(newPathSep + prefix + pathOld.getFileName());
					moveSingleFile(pathOld, newPath, cover);
				}
			});
		} catch (Exception e) {
			throw new ExceptionNbcFile("move fold error", e);
		}

		if (!isMakeDirSameAsOld[0]) {
			FileOperate.deleteFileFolder(olddir);
		}
	}

	
	protected static boolean isFilePathSame(String oldfile, String newfile) {
		if (StringOperate.isRealNull(oldfile) && StringOperate.isRealNull(newfile)) {
			return true;
		}
		if (StringOperate.isRealNull(oldfile) && !StringOperate.isRealNull(newfile)
				|| !StringOperate.isRealNull(oldfile) && StringOperate.isRealNull(newfile)) {
			return false;
		}
		if (StringOperate.isEqual(oldfile, newfile)) {
			return true;
		}
		String oldFileStr = getCanonicalPath(oldfile);
		String newFileStr = getCanonicalPath(newfile);

		if (oldFileStr != null)
			oldFileStr = oldFileStr.replace("\\", "/");
		if (newFileStr != null)
			newFileStr = newFileStr.replace("\\", "/");

		if (StringOperate.isEqual(oldFileStr, newFileStr)) {
			return true;
		}
		return false;
	}

	/**
	 * 创建快捷方式，目前只能在linux下使用 内部会根据linkTo的路径自动创建文件夹 <br>
	 * HDFS上没有用
	 * 
	 * @param rawFile
	 * @param linkTo
	 * @param cover
	 *            是否覆盖
	 * @return 返回是否创建成功
	 */
	public static boolean linkFile(String rawFile, String linkTo, boolean cover) {
		if (!cover && (FileOperate.isFileFolderExist(linkTo) || FileOperate.isSymbolicLink(linkTo))) {
			return true;
		}
		if (!FileOperate.isFileExist(rawFile)) {
			return false;
		}
		FileOperate.createFolders(FileOperate.getParentPathNameWithSep(linkTo));
		if (FileOperate.isFileExist(linkTo) && cover) {
			FileOperate.delFile(linkTo);
		}

		rawFile = FileHadoop.convertToHadoop(rawFile);
		linkTo = FileHadoop.convertToHadoop(linkTo);
		boolean isRawHdfs = FileHadoop.isHdfs(rawFile);
		boolean isLinkHdfs = FileHadoop.isHdfs(linkTo);
		if (isRawHdfs ^ isLinkHdfs) {
			throw new ExceptionFileNotExist("RawFile And LinkTo File Are Not In Same FileSystem\n, raw File: " + rawFile
					+ "\nLinkTo: " + linkTo);
		}
		if (isRawHdfs) {
			throw new ExceptionNbcFile("could not creat symbolic link on hdfs");
		}

		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("ln");
		lsCmd.add("-s");
		lsCmd.add(rawFile);
		lsCmd.add(linkTo);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setTerminateWriteTo(false);
		cmdOperate.runWithExp();
		return true;
	}

	/**
	 * 判断文件是否存在，并且不是文件夹，给的是绝对路径
	 * 
	 * @param fileName
	 *            如果为null, 直接返回false
	 * @return 文件存在,返回true.否则,返回false
	 */
	// TODO 修改method名字为 isFileExistAndNotDir
	public static boolean isFileExist(String fileName) {
		if (StringOperate.isRealNull(fileName)) {
			return false;
		}
		Path file = getPath(fileName);
		return isFileExistAndNotDir(file);
	}

	/**
	 * 判断文件是否存在，并且不是文件夹，给的是绝对路径
	 * 
	 * @param fileName
	 *            如果为null, 直接返回false
	 * @return 文件存在,返回true.否则,返回false
	 */
	public static boolean isFileExist(Path path) {
		if (path == null) {
			return false;
		}
		return isFileExistAndNotDir(path);
	}

	/**
	 * 判断文件是否存在，并且不是文件夹，给的是绝对路径
	 * 
	 * @param fileName
	 *            如果为null, 直接返回false
	 * @return
	 */
	// TODO 修改method名字为 isFileExistAndNotDir
	public static boolean isFileExistAndNotDir(Path file) {
		return file != null && Files.exists(file) && !Files.isDirectory(file);
	}

	/**
	 * 判断文件是否存在，并且不是文件夹，给的是绝对路径
	 * 
	 * @param fileName
	 *            如果为null, 直接返回false
	 * @return
	 */
	@Deprecated
	public static boolean isFileExistAndNotDir(File file) {
		Path path = getPath(file);
		return isFileExistAndNotDir(path);
	}
	public static boolean isSymbolicLink(String fileName) {
		return Files.isSymbolicLink(FileOperate.getPath(fileName));
	}
	/**
	 * 判断文件是否存在，并且有一定的大小而不是空文件
	 * 
	 * @param fileName
	 *            如果为null, 直接返回false
	 * @param size
	 *            大小 byte为单位
	 * @return
	 */
	public static boolean isFileExistAndBigThanSize(Path path, double size) {
		if (path == null) {
			return false;
		}
		if (size < 0)
			size = -1;
		return FileOperate.getFileSizeLong(path) > size;
	}

	/**
	 * 判断文件是否存在，并且有一定的大小而不是空文件
	 * 
	 * @param fileName
	 *            如果为null, 直接返回false
	 * @param size
	 *            大小 byte为单位
	 * @return
	 */
	public static boolean isFileExistAndBigThanSize(String fileName, double size) {
		if (StringOperate.isRealNull(fileName)) {
			return false;
		}
		if (size < 0)
			size = -1;
		Path path = getPath(fileName);
		return FileOperate.getFileSizeLong(path) > size;
	}

	public static boolean isFileExistAndBigThan0(String fileName) {
		return isFileExistAndBigThanSize(fileName, 0);
	}

	public static boolean isFileExistAndBigThan0(Path path) {
		return isFileExistAndBigThanSize(path, 0);
	}

	public static void validateFileExistAndBigThan0(String fileName) {
		if (!isFileExistAndBigThanSize(fileName, 0)) {
			throw new ExceptionNbcFileInputNotExist(fileName + " is not exist");
		}
	}

	/**
	 * 判断文件是否存在，并且有一定的大小而不是空文件
	 * 
	 * @param fileName
	 *            如果为null, 直接返回false
	 * @param size
	 *            大小 byte为单位
	 * @return
	 * @throws FileNotFoundException
	 */
	public static void checkFileExistAndBigThanSize(String fileName, double size) {
		if (!isFileExistAndBigThanSize(fileName, size)) {
			throw new ExceptionFileNotExist("cannot find file: " + fileName);
		}
	}

	/**
	 * 是否存在并且无损
	 * 
	 * @param filePath
	 * @param realSize
	 * @return
	 */
	@Deprecated
	public static boolean isFileExistAndLossless(String filePath, long realSize) {
		File file = getFile(filePath);
		if (!file.exists()) {
			return false;
		}
		if (file.isFile()) {
			return file.length() == realSize;
		}
		return false;
	}

	/**
	 * 判断文件是否为文件夹,null直接返回false
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean isFileDirectory(String fileName) {
		if (StringOperate.isRealNull(fileName)) {
			return false;
		}
		Path file = getPath(fileName);
		return isFileDirectory(file);
	}

	/**
	 * 如果file是文件夹，并且为空，则返回true，否则返回false
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean isFileDirectoryEmpty(String file) {
		return isFileDirectory(getPath(file));
	}

	/**
	 * 如果file是文件夹，并且为空，则返回true，否则返回false
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean isFileDirectoryEmpty(Path file) {
		if (file == null) {
			return false;
		}
		if (!Files.isDirectory(file)) {// 没有文件，则返回空
			return false;
		}
		return getLsFoldPath(file).isEmpty();
	}

	/**
	 * 判断文件是否为文件夹,null直接返回false
	 * 
	 * @param fileName
	 * @return
	 */
	@Deprecated
	public static boolean isFileDirectory(File file) {
		if (file == null) {
			return false;
		}
		return file.isDirectory();
	}

	/**
	 * 判断文件是否为文件夹,null直接返回false
	 * 
	 * @param fileName
	 * @return true是文件夹，false不是文件夹
	 */
	public static boolean isFileDirectory(Path file) {
		if (file == null) {
			return false;
		}
		if (file instanceof OssPath && file.toString().endsWith("/")) {
			return true;
		}
		return Files.isDirectory(file);
	}

	/**
	 * 判断文件或文件夹是否存在，给的是绝对路径
	 * 
	 * @param fileName
	 *            如果为null, 直接返回false
	 * @return
	 */
	public static boolean isFileFolderExist(String fileName) {
		if (StringOperate.isRealNull(fileName)) {
			return false;
		}
		return isFileFolderExist(getPath(fileName));
	}

	/**
	 * 判断文件或文件夹是否存在，给的是绝对路径
	 * 
	 * @param file
	 *            如果为null, 直接返回false
	 * @return
	 */
	public static boolean isFileFolderExist(File file) {
		if (file == null)
			return false;
		Path path = getPath(file);
		return isFileFolderExist(path);
	}

	/**
	 * <<<<<<< HEAD 判断文件是否存在，给的是绝对路径 ======= 判断文件或文件夹是否存在，给的是绝对路径 >>>>>>> branch
	 * 'master' of https://github.com/NovelBioCloud/base.git
	 * 
	 * @param fileName
	 *            如果为null, 直接返回false
	 * @return
	 */
	public static boolean isFileFolderExist(Path file) {
		return file != null && Files.exists(file);
	}

	/**
	 * 删除文件.文件不存在不会报错.
	 * 
	 * @param filePathAndName
	 *            文本文件完整绝对路径及文件名
	 */
	public static void delFile(String filePathAndName) {
		try {
			Files.deleteIfExists(getPath(filePathAndName));
		} catch (IOException e) {
			throw new ExceptionFileError("cannot delete path " + filePathAndName, e);
		}
	}

	/**
	 * 删除文件
	 * 
	 * @param filePathAndName
	 *            文本文件完整绝对路径及文件名 文件不存在则返回false
	 * @return Boolean 成功删除返回true遭遇异常返回false
	 */
	public static void delFile(Path myDelFile) {
		try {
			Files.deleteIfExists(myDelFile);
		} catch (IOException e) {
			throw new ExceptionFileError("cannot delete path " + myDelFile, e);
		}
	}

	/**
	 * 删除文件夹
	 * 
	 * @param folderPath
	 *            文件夹完整绝对路径
	 * @return
	 */
	public static void delFolder(String folderPath) {
		if (StringOperate.isRealNull(folderPath))
			return;
		try {
			Path folder = getPath(folderPath);
			deleteFolder(folder); // 删除完里面所有内容
		} catch (Exception e) {
			logger.error("删除文件夹操作出错");
		}
	}

	/**
	 * 删除指定文件夹下所有文件,但不删除本文件夹
	 * 
	 * @param path
	 *            文件夹完整绝对路径,最后无所谓加不加\\或/
	 */
	// TODO 待测试
	public static void delAllFile(String path) {
		delAllFile(getPath(path));
	}

	@Deprecated
	public static void deleteOnExit(final File file) {
		final Path path = getPath(file.getPath());
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				FileOperate.deleteFileFolder(path);
			}
		});
	}

	public static void deleteOnExit(final Path path) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				FileOperate.deleteFileFolder(path);
			}
		});
	}

	public static void deleteOnExit(String fileName) {
		final Path path = getPath(fileName);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				FileOperate.deleteFileFolder(path);
			}
		});
	}

	/**
	 * 删除指定文件夹下所有文件,但不删除本文件夹
	 * 
	 * @param path
	 *            必须是文件夹
	 * @return
	 * @return
	 */
	public static void delAllFile(Path path) {
		if (path == null || !Files.exists(path)) {
			return;
		}
		if (!isFileDirectory(path)) {
			throw new ExceptionFileError("path is not directory " + path);
		}

		try {
			Files.list(path).forEach((insidePath) -> {
				if (isFileDirectory(insidePath)) {
					delAllFile(insidePath);
				} else {
					delFile(insidePath);
				}
			});
		} catch (IOException e) {
			throw new ExceptionFileError("cannot delete path " + path, e);
		}
	}

	/**
	 * 删除目录（文件夹）以及目录下的文件，包括本文件夹
	 * 
	 * @param sPath
	 *            被删除目录的文件路径，最后无所谓加不加"/"
	 */
	private static void deleteFolder(Path dirFile) {
		try {
			Files.list(dirFile).forEach((file) -> {
				if (isFileDirectory(file)) {
					deleteFolder(file);
				} else {
					delFile(file);
				}
			});
			Files.deleteIfExists(dirFile);
		} catch (Exception e) {

		}
	}

	/**
	 * 根据路径删除指定的目录或文件，无论存在与否
	 * 
	 * @param sPath
	 *            要删除的目录或文件
	 * @return 删除成功返回 true，否则返回 false 不存在文件也返回true
	 */
	// TODO 需要重命名方法名，把首字母D小写
	public static void deleteFileFolder(String sPath) {
		if (StringOperate.isRealNull(sPath)) {
			return;
		}
		Path file = getPath(sPath);
		deleteFileFolder(file);
	}

	/**
	 * 根据路径删除指定的目录或文件，无论存在与否
	 * 
	 * @param sPath
	 *            要删除的目录或文件
	 * @return 删除成功返回 true，否则返回 false 不存在文件也返回true
	 */
	public static void deleteFileFolder(Path file) {
		if (file == null)
			return;

		if (Files.exists(file)) {
			if (Files.isDirectory(file)) {
				deleteFolder(file);
			} else {
				delFile(file);
			}
		}
	}

	/**
	 * 添加文件分割符
	 * 
	 * @param path
	 * @return
	 */
	public static String addSep(String path) {
		path = path.trim();
		if (!path.endsWith(File.separator)) {
			if (!path.equals("")) {
				path = path + File.separator;
			}
		}
		return path;
	}

	public static void validateFileName(String fileName) {
		if (fileName.contains("\\") || fileName.contains("/") || fileName.contains("*")) {
			throw new ExceptionNbcFile(fileName + " fileName error, cannot contain: \\ / *");
		}
	}

	/**
	 * 删除文件分割符
	 * 
	 * @param path
	 * @return
	 */
	public static String removeSep(String path) {
		path = path.trim();
		if (path.equals("/") || path.equals("\\")) {
			return path;
		}
		while (path.endsWith(File.separator)) {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}

	/**
	 * 
	 * 将输入流保存到指定文件
	 * 
	 * @param inputStream
	 * @param outPath
	 *            输出文件名，不会修改该输出文件名
	 * @param isOutputGzip
	 *            是否将输出流进行压缩
	 * @param uploadFileSize
	 *            上传文件的大小，小于等于0表示不考虑
	 * @return
	 * @throws IOException
	 *             抛出异常，但并不删除已上传的文件
	 */
	public static long uploadFile(InputStream inputStream, String outPath, boolean isOutputGzip, long uploadFileSize)
			throws IOException {
		if (StringOperate.isRealNull(outPath)) {
			return 0;
		}
		FileOperate.createFolders(FileOperate.getParentPathNameWithSep(outPath));
		OutputStream os = FileOperate.getOutputStream(outPath);
		if (isOutputGzip) {
			os = new GZIPOutputStream(os, TxtReadandWrite.bufferLen);
		}

		long outputLen = IOUtils.copyLarge(inputStream, os);
		os.flush();
		inputStream.close();
		os.close();
		if (uploadFileSize > 0 && outputLen != uploadFileSize) {
			throw new IOException("file Length is incorrect! inputLen: " + uploadFileSize + "   realLen: " + outputLen);
		}
		return outputLen;
	}

	/**
	 * 关闭流
	 * 
	 * @date 2015年11月24日
	 * @param stream
	 */
	public static void close(Closeable stream) {
		try {
			if (stream != null) {
				stream.close();
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 检查文件名是否合法.<br/>
	 * 规则: 1.不能有斜线,空格等特殊字符.
	 * 
	 * @date 2016年6月12日
	 * @author novelbio fans.fan
	 * @param fileName
	 * @return
	 */
	public static String convertMessyCodeAndValidateFileName(String fileName) {
		fileName = StringOperate.changeMessyCode(fileName);
		if (fileName == null || fileName.contains("/") || fileName.contains("\\") || fileName.contains("'")) {
			throw new ExceptionNbcFile("fileName is not valide " + fileName);
		}
		if (fileName.contains(" ")) {
			throw new ExceptionNbcFile("fileName is not valide " + fileName);
		}
		if (StringOperate.isContainerSpecialCode(fileName)) {
			throw new ExceptionNbcFile("文件名只允许包含汉字,字母,数字,中划线和下划线.");
		}
		return fileName;
	}

	/**
	 * 获取文件内容<br>
	 * <b>只允许对小于5M的文件读取</b>
	 * 
	 * @date 2016年8月24日
	 * @author novelbio fans.fan
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static String getFileContent(String filePathAndName) throws IOException {
		if (!isFileExistAndBigThan0(filePathAndName)) {
			return null;
		}
		if (getFileSizeLong(filePathAndName) > 5242880) {
			throw new RuntimeException("file size more than 5M");
		}
		StringBuffer stringBuffer = new StringBuffer();
		TxtReadandWrite.readfileLs(filePathAndName).forEach(str -> stringBuffer.append(str).append("\n"));

		return stringBuffer.toString();
	}

	public static class ExceptionFileNotExist extends RuntimeException {
		private static final long serialVersionUID = 8125052068436320509L;

		public ExceptionFileNotExist(String info) {
			super(info);
		}
	}

	public static class ExceptionFileError extends RuntimeException {
		private static final long serialVersionUID = 8125052068436320509L;

		public ExceptionFileError(String info) {
			super(info);
		}

		public ExceptionFileError(String info, Throwable t) {
			super(info, t);
		}
	}

	/**
	 * 返回目录流，该流需要被关闭
	 * 
	 * @param start
	 * @return
	 * @throws IOException
	 */
	public static DirectoryStream<Path> newDirectoryStream(Path start) throws IOException {
		return Files.newDirectoryStream(start);
	}

}
