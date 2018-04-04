package com.novelbio.base.fileOperate;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hadoop.compression.lzo.LzopCodec;
import com.novelbio.base.PathDetail;
import com.novelbio.base.SerializeKryo;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdMoveFileAli;
import com.novelbio.base.dataOperate.DateUtil;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataOperate.TxtReadandWrite.TXTtype;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.util.IOUtil;
import com.novelbio.base.util.ServiceEnvUtil;
import com.novelbio.jsr203.objstorage.ObjPath;
import com.novelbio.jsr203.objstorage.PathDetailObjStorage;

import hdfs.jsr203.HadoopFileSystemProvider;
import hdfs.jsr203.HadoopPath;
import hdfs.jsr203.HdfsConfInitiator;

public class FileOperate {
	private static final Logger logger = LoggerFactory.getLogger(FileOperate.class);
	static HadoopFileSystemProvider hdfsProvider = new HadoopFileSystemProvider();
	static FileSystemProvider objProvider = PathDetailObjStorage.generateObjStorageFileSystemProvider();
	
	static PatternOperate patternOperate = new PatternOperate("^[/\\\\]{0,2}[^/]+\\:[/\\\\]{0,2}");
	static boolean isWindowsOS = false;
	static {
		String osName = System.getProperty("os.name");
		if (osName.toLowerCase().indexOf("windows") > -1) {
			isWindowsOS = true;
		}
	}
	/** 是否是windows操作系统 */
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
			filePathParent = addSep(filePathParent);
		}
		String path = filePathParent + name;
		return getFile(path);
	}
	
	/**
	 * 返回当前系统的schema，譬如oss，hdfs等
	 * 注意不带最后的冒号
	 * @return
	 */
	public static String getSchema() {
		if (ServiceEnvUtil.isHadoopEnvRun()) {
			return hdfsProvider.getScheme();
		} else {
			return objProvider.getScheme();
		}
	}
	/**
	 * 是否为绝对路径，也就是以
	 * "/"  "hdfs:"  "cos:" "oss:"
	 * 等开头 
	 * @return
	 */
	public static boolean isAbsolutPath(String path) {
		String head = "/";
		if (ServiceEnvUtil.isHadoopEnvRun()) {
			head = hdfsProvider.getScheme() + ":/";
		} else if (ServiceEnvUtil.isCloudEnv()) {
			head = objProvider.getScheme() + ":/";
		}
		if (StringOperate.isRealNull(path)) {
			return false;
		}
		return path.startsWith(head) || path.startsWith("/");
	}
//	/**
//	 * 根据不同的文件类型得到File
//	 * 
//	 * @param filePath
//	 * @return
//	 */
//	@Deprecated
//	public static File getFile(File fileParent, String name) {
//		if (fileParent instanceof FileHadoop) {
//			return new FileHadoop(FileOperate.addSep(fileParent.getAbsolutePath()) + name);
//		} else {
//			return new File(fileParent, name);
//		}
//	}

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
	public static Path getPath(String first, String... rest) {
		try {
			if (first == null || rest == null || rest.length == 0) {
				throw new IllegalArgumentException("params can not be null");
			}
			if (first.startsWith(HadoopFileSystemProvider.SCHEME + ":/")) {
				return hdfsProvider.getFileSystem(new URI(first)).getPath(new URI(first).getPath(), rest);
			} else if (first.startsWith(objProvider.getScheme() + ":/")) {
				return objProvider.getFileSystem(new URI(first)).getPath(new URI(first).getPath(), rest);
			}  else {
				System.out.println("default Path");
				return Paths.get(first, rest);
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
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
			} else if (fileName.startsWith(objProvider.getScheme())) {
				URI uri = new URI(fileName);
				return objProvider.getPath(uri);
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
	 * 给定路径名，返回其上一层路径，带"/" 如给定 /wer/fw4e/sr/frw/s3er.txt 返回 /wer/fw4e/sr/frw
	 * <br>
	 * 如果为相对路径的最上层，譬如给定的是soap 则返回“” 可以给定不存在的路径
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public static String getParentPathNameWithSep(Path path) {
		if (path == null)
			return null;

		return getParentPathNameWithSep(path.toString());
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

		if (fileName.startsWith(objProvider.getScheme() + "://")) {
			try {
				URI uri = new URI(fileName);
				String parentPath = objProvider.getPath(uri).getParent().toString();
				return parentPath.endsWith("/") ? parentPath : parentPath + "/";
			} catch (Exception e) {
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
	public static String getPathName(Path path) {
		String name = getAbsolutePath(path);
		if (isFileDirectory(path)) {
			name = addSep(name);
		}
		return getPathName(name);
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

//	/** 返回日期格式的最后修改时间 */
//	public static String getTimeLastModifyStr(String fileName) {
//		Calendar cal = Calendar.getInstance();
//		cal.setTimeInMillis(getTimeLastModify(fileName));
//		DateFormat formatter = DateFormat.getDateTimeInstance();
//		String data = formatter.format(cal.getTime());
//		return data;
//	}
	
	@Deprecated
	public static long getTimeLastModify(File file) {
		return getTimeLastModify(getPath(file));
	}

	public static long getTimeLastModify(String fileName) {
		return getTimeLastModify(getPath(fileName));
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


	/**
	 * 给定路径名，返回上一级文件夹的名字，可以给定不存在的路径。 如给定 /home/novelbio/zongjie/ 和
	 * /home/novelbio/zongjie 均返回 novelbio
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getLastPathName(String fileName) {
		String ParentPath = getParentPathNameWithSep(fileName);
		return getFileName(ParentPath);
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
			double size = (double) getFileSizeLong(fileName) / 1024;
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
		if (path == null || !isFileExist(path)) {
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

//	public static BasicFileAttributes getFileAttribute(String filePath) {
//		return getFileAttribute(getPath(filePath));
//	}

	private static BasicFileAttributes getFileAttribute(Path filePath) {
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

	/** 等同path的tostring方法，获得文件全名 */
	public static String getFilePathName(Path path) {
		return path.toString();
	}

	public static String getAbsolutePath(String fileName) {
		return getAbsolutePath(getPath(fileName));
	}

	public static String getAbsolutePath(Path path) {
		String name = path.toAbsolutePath().normalize().toString();
		if (path instanceof HadoopPath) {
			if (name.startsWith(PathDetail.getHdpHdfsHeadSymbol())) {
				name = name.replaceFirst(PathDetail.getHdpHdfsHeadSymbol(), FileHadoop.hdfsSymbol);
			} else if (!name.startsWith(FileHadoop.hdfsSymbol)) {
				name = FileHadoop.hdfsSymbol + name;
			}
		} else if (path instanceof ObjPath) {
			name = path.toString();
		}
		return name;
	}
	
//	/**
//	 * 如果path是软连接，会返回实际的文件路径
//	 * @param path
//	 * @return
//	 */
//	public static String getCanonicalPath(Path path) {
//		String name = getCanonicalPath(path.toString());
//		if (path instanceof HadoopPath) {
//			if (name.startsWith(PathDetail.getHdpHdfsHeadSymbol())) {
//				name = name.replaceFirst(PathDetail.getHdpHdfsHeadSymbol(), FileHadoop.hdfsSymbol);
//			} else if (!name.startsWith(FileHadoop.hdfsSymbol)) {
//				name = FileHadoop.hdfsSymbol + name;
//			}
//		} else if (path instanceof OssPath) {
//			name = path.toString();
//		}
//		return name;
//	}

	public static String getCanonicalPath(String fileName) {
		boolean isAddSplashHead = false;
		if (fileName.startsWith(PathDetail.getHdpHdfsHeadSymbol())) {
			fileName = FileHadoop.convertToHdfsPath(fileName);
		}
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
				canonicalPath = removeSplashHead(canonicalPath, false);
			}
			return canonicalPath;
		} catch (IOException e) {
			throw new ExceptionFileError("cannot getCanonicalPath " + fileName, e);
		}
	}
	public static String convertToHdfs(String file) {
		if (!ServiceEnvUtil.isHadoopEnvRun()) {
			return file;
		}
		return FileHadoop.convertToHdfsPath(file);
	}
	
	public static String convertHdfsOssToLocal(String path) {
		if (FileHadoop.isHdfs(path)) {
			path = FileHadoop.convertToLocalPath(path);
		}
		if (path.startsWith("oss://")) {
			path = CmdMoveFileAli.convertAli2Loc(path, true);
//			try {
//				URI uri = new URI(path);
//				path = "/media/nbfs" + uri.getPath();
//			} catch (URISyntaxException e) {
//				throw new RuntimeException("cannot resolve url " + path);
//			}
		}
		return path;
	}
	
//	/**
//	 * 获取文件夹下包含指定文件名与后缀的所有文件名,等待增加功能子文件夹下的文件。也就是循环获得文件<br>
//	 * 如果文件不存在则返回空的list<br>
//	 * 如果不是文件夹，则返回该文件名<br>
//	 * 
//	 * @param filePath
//	 *            目录路径
//	 * @param filename
//	 *            指定包含的文件名，是正则表达式 ，如 "*",正则表达式无视大小<br>
//	 *            null 表示不指定
//	 * @param suffix
//	 *            指定包含的后缀名，是正则表达式<br>
//	 *            文件 wfese.fse.fe认作 "wfese.fse"和"fe"<br>
//	 *            文件 wfese.fse.认作 "wfese.fse."和""<br>
//	 *            文件 wfese 认作 "wfese"和""<br>
//	 *            null 表示不指定
//	 * @return 返回包含目标文件全名的ArrayList
//	 * @throws IOException
//	 */
//	@Deprecated
//	public static List<File> getFoldFileLs(String filePath, String filename, String suffix) {
//		filePath = removeSep(filePath);
//		File file = getFile(filePath);
//		if (filePath.equals("")) {
//			filePath = file.getAbsolutePath();
//			file = getFile(filePath);
//		}
//		return getFoldFileLs(file, filename, suffix);
//	}

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

//	/**
//	 * 获取文件夹下全部文件名
//	 * 
//	 * @return 返回包含目标文件全名的List
//	 * @throws IOException
//	 */
//	@Deprecated
//	public static List<File> getFoldFileLs(File file) {
//		return getFoldFileLs(file, "*", "*");
//	}

//	/**
//	 * 获取文件夹下全部文件名
//	 * 已过期,建议使用getLsFoldPath
//	 * 
//	 * @return 返回包含目标文件全名的List
//	 * @throws IOException
//	 */
//	@Deprecated
//	public static List<File> getFoldFileLs(String file) {
//		return getFoldFileLs(file, "*", "*");
//	}

//	/**
//	 * 获取文件夹下包含指定文件名与后缀的所有文件名,等待增加功能子文件夹下的文件。也就是循环获得文件<br>
//	 * 如果文件不存在则返回空的list<br>
//	 * 如果不是文件夹，则返回该文件名<br>
//	 * 
//	 * @param filePath
//	 *            目录路径
//	 * @param filename
//	 *            指定包含的文件名，是正则表达式 ，如 "*",正则表达式无视大小<br>
//	 *            null 表示不指定
//	 * @param suffix
//	 *            指定包含的后缀名，是正则表达式<br>
//	 *            文件 wfese.fse.fe认作 "wfese.fse"和"fe"<br>
//	 *            文件 wfese.fse.认作 "wfese.fse."和""<br>
//	 *            文件 wfese 认作 "wfese"和""<br>
//	 *            null 表示不指定
//	 * @return 返回包含目标文件全名的ArrayList
//	 * @throws IOException
//	 */
//	@Deprecated
//	public static List<File> getFoldFileLs(File file, String filename, String suffix) {
//		if (filename == null || filename.equals("*")) {
//			filename = ".*";
//		}
//		if (suffix == null || suffix.equals("*")) {
//			suffix = ".*";
//		}
//		FileFilterNBC fileFilterNBC = new FileFilterNBC(filename, suffix);
//		List<File> lsFilenames = new ArrayList<>();
//
//		if (!file.exists()) {// 没有文件，则返回空
//			return new ArrayList<>();
//		}
//		// 如果只是文件则返回文件名
//		if (!file.isDirectory()) { // 获取文件名与后缀名
//			if (fileFilterNBC.accept(file)) {
//				lsFilenames.add(file);
//				return lsFilenames;
//			}
//		}
//
//		File[] result = file.listFiles(fileFilterNBC);
//
//		for (File file2 : result) {
//			lsFilenames.add(file2);
//		}
//		return lsFilenames;
//	}

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
			noNeedReg++;
		}
		if (suffix == null || suffix.equals("*")) {
			noNeedReg++;
		}
		if (file == null || !isFileExist(file)) {
			return new ArrayList<>();
		}
		
		PredicateFileNameSuffix predicateFileName = null;
		if (noNeedReg != 2) {
			//等于2就是匹配所有，不需要正则了.
			predicateFileName = new PredicateFileNameSuffix(filename, suffix);
		}
		// 如果只是文件则返回文件名
		if (!isFileDirectory(file)) { // 获取文件名与后缀名
			if (predicateFileName == null) {
				lsFilenames.add(file);
			} else if (predicateFileName != null && predicateFileName.test(file)) {
				lsFilenames.add(file);
			}
			return lsFilenames;
		}
		try(Stream<Path> streamPath = Files.list(file)) {
			if (predicateFileName != null) {
				lsFilenames = streamPath.filter(predicateFileName).collect(Collectors.toList());
			} else {
				lsFilenames = streamPath.collect(Collectors.toList());
			}
			streamPath.close();
			return lsFilenames;
		} catch (IOException e) {
			throw new ExceptionFileError("cannot get sub files of " + file.toString(), e);
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
	public static List<Path> getLsFoldPath(Path file, String filename) {
		List<Path> lsFilenames = new ArrayList<>();
		
		boolean needReg = true;
		if (filename == null || filename.equals("*")) {
			needReg = false;
		}
		if (file == null || !isFileExist(file)) {
			return new ArrayList<>();
		}
		
		PredicateFileName predicateFileName = null;
		if (needReg) {
			//等于2就是匹配所有，不需要正则了.
			predicateFileName = new PredicateFileName(filename);
		}
		// 如果只是文件则返回文件名
		if (!isFileDirectory(file)) { // 获取文件名与后缀名
			if (predicateFileName == null) {
				lsFilenames.add(file);
			} else if (predicateFileName != null && predicateFileName.test(file)) {
				lsFilenames.add(file);
			}
			return lsFilenames;
		}
		try(Stream<Path> streamPath = Files.list(file)) {
			if (predicateFileName != null) {
				lsFilenames = streamPath.filter(predicateFileName).collect(Collectors.toList());
			} else {
				lsFilenames = streamPath.collect(Collectors.toList());
			}
			streamPath.close();
			return lsFilenames;
		} catch (IOException e) {
			throw new ExceptionFileError("cannot get sub files of " + file.toString(), e);
		}
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
		return getLsFoldPathRecur(getPath(file), "*", "*", isNeedFolder);
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
		List<Path> lsPaths = getLsFoldPathRecur(getPath(file), "*", "*", isNeedFolder);
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
			noNeedReg++;
		}
		if (suffix == null || suffix.equals("*")) {
			noNeedReg++;
		}
		if (file == null || !isFileExist(file)) {
			return new ArrayList<>();
		}
		
		PredicateFileNameSuffix predicateFileName = null;
		if (noNeedReg != 2) {
			//等于2就是匹配所有，不需要正则了.
			predicateFileName = new PredicateFileNameSuffix(filename, suffix);
			predicateFileName.setFilterFolder(false);
		}

		// 如果只是文件则返回文件名
		if (!isFileDirectory(file)) { // 获取文件名与后缀名
			if (predicateFileName == null) {
				lsPath.add(file);
			} else if (predicateFileName != null && predicateFileName.test(file)) {
				lsPath.add(file);
			}
			return lsPath;
		}
		try(Stream<Path> streamPath = Files.list(file)) {
			 List<Path> lsPathTmp = null;
			 if (predicateFileName != null) {
				 lsPathTmp = streamPath.filter(predicateFileName).collect(Collectors.toList());
			} else {
				lsPathTmp = streamPath.collect(Collectors.toList());
			}
			streamPath.close();
			for (Path path : lsPathTmp) {
				if (isFileDirectory(path)) {
					//是ossPath，只要是以/结尾的.就是文件夹。不是osspath,则需查找判断一下
					lsPath.addAll(getLsFoldPathRecur(path, filename, suffix, isNeedFolder));
					if (isNeedFolder) {
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
	 * 获取文件夹下包含指定文件名与后缀的所有文件名，<b>递归查找</b><br>
	 * 如果文件不存在则返回空的list<br>
	 * 如果不是文件夹，则返回该文件名<br>
	 * 
	 * @param file
	 *            目录路径
	 * @param filename
	 *            指定包含的文件名，是正则表达式 ，如 "*",正则表达式无视大小<br>
	 *            null 表示不指定
	 * @param isNeedFolder
	 *            是否需要把文件夹也记录下来
	 * @return 返回包含目标Path 的ArrayList
	 * @throws IOException
	 */
	public static List<Path> getLsFoldPathRecur(Path file, String filename, boolean isNeedFolder) {
		List<Path> lsPath = new ArrayList<>();

		boolean needReg = true;
		if (filename == null || filename.equals("*")) {
			needReg = false;
		}
		if (file == null || !isFileExist(file)) {
			return new ArrayList<>();
		}
		
		PredicateFileName predicateFileName = null;
		if (needReg) {
			//等于2就是匹配所有，不需要正则了.
			predicateFileName = new PredicateFileName(filename);
			predicateFileName.setFilterFolder(false);
		}

		// 如果只是文件则返回文件名
		if (!isFileDirectory(file)) { // 获取文件名与后缀名
			if (predicateFileName == null) {
				lsPath.add(file);
			} else if (predicateFileName != null && predicateFileName.test(file)) {
				lsPath.add(file);
			}
			return lsPath;
		}
		try(Stream<Path> streamPath = Files.list(file)) {
			 List<Path> lsPathTmp = null;
			 if (predicateFileName != null) {
				 lsPathTmp = streamPath.filter(predicateFileName).collect(Collectors.toList());
			} else {
				lsPathTmp = streamPath.collect(Collectors.toList());
			}
			streamPath.close();
			for (Path path : lsPathTmp) {
				if (isFileDirectory(path)) {
					//是ossPath，只要是以/结尾的.就是文件夹。不是osspath,则需查找判断一下
					lsPath.addAll(getLsFoldPathRecur(path, filename, isNeedFolder));
					if (isNeedFolder) {
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
	
	private static class PredicateFileName implements Predicate<Path> {
		PatternOperate patName;

		boolean isFilterFolder = true;

		public PredicateFileName(String fileNameRegex) {
			if (!StringOperate.isRealNull(fileNameRegex)) {
				fileNameRegex = fileNameRegex.replace(".", "\\.");
				fileNameRegex = fileNameRegex.replace("*", ".*");
				if (!fileNameRegex.startsWith("^")) {
					fileNameRegex = "^" + fileNameRegex;
				}
				if (!fileNameRegex.endsWith("$")) {
					fileNameRegex = fileNameRegex + "$";
				}
			} else {
				fileNameRegex = "*";
			}
			
			if (!fileNameRegex.equalsIgnoreCase("*")) {
				patName = new PatternOperate(fileNameRegex, false);
			}
		}

		/** 是否用正则表达式过滤文件夹 */
		public void setFilterFolder(boolean isFilterFolder) {
			this.isFilterFolder = isFilterFolder;
		}

		@Override
		public boolean test(Path t) {
			if (!isFilterFolder && isFileDirectory(t)) {
				return true;
			}
			String fileName = getFileName(t);
			return patName == null || !patName.getPat(fileName).isEmpty();
		}
	}
	
	private static class PredicateFileNameSuffix implements Predicate<Path> {
		PatternOperate patName;
		PatternOperate patSuffix;

		boolean isFilterFolder = true;

		public PredicateFileNameSuffix(String fileNameRegex, String suffixRegex) {
			if (!StringOperate.isRealNull(fileNameRegex)) {
				fileNameRegex = fileNameRegex.replace(".", "\\.");
				fileNameRegex = fileNameRegex.replace("*", ".*");
			} else {
				fileNameRegex = "*";
			}
			if (!StringOperate.isRealNull(suffixRegex)) {
				suffixRegex = suffixRegex.replace(".", "\\.");
				suffixRegex = suffixRegex.replace("*", ".*");
			} else {
				suffixRegex = "*";
			}
			
			if (!fileNameRegex.equalsIgnoreCase("*")) {
				patName = new PatternOperate(fileNameRegex, false);
			}
			if (!suffixRegex.equalsIgnoreCase("*")) {
				if (!suffixRegex.endsWith("$")) {
					suffixRegex += "$";
				}
				patSuffix = new PatternOperate(suffixRegex, false);
			}
		}

		/** 是否用正则表达式过滤文件夹 */
		public void setFilterFolder(boolean isFilterFolder) {
			this.isFilterFolder = isFilterFolder;
		}

		@Override
		public boolean test(Path t) {
			if (!isFilterFolder && isFileDirectory(t)) {
				return true;
			}
			String fileName = getFileName(t);
			String[] fileNameSep = getFileNameSepWithoutPath(t.toString());
			if (patSuffix == null && !patName.getPat(fileName).isEmpty()) {
				return true;
			}

			boolean isNameOk = (patName == null || patName.getPatFirst(fileNameSep[0]) != null);
			boolean isSuffixOk = (patSuffix == null || patSuffix.getPatFirst(fileName) != null);
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
		return getLsFoldFileName(filePath, "*", "*");
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
		return getLsFoldFileName(getPath(filePath), filename, suffix);
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
	public static ArrayList<String> getLsFoldFileName(String filePath, String filename) {
		return getLsFoldFileName(getPath(filePath), filename);
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
		return getLsFoldFileName(filePath, "*", "*");
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
	public static ArrayList<String> getLsFoldFileName(Path filePath, String filename) {
		List<Path> lsPaths = getLsFoldPath(filePath, filename);
		ArrayList<String> lsResult = new ArrayList<>();
		lsPaths.forEach((path) -> {
			lsResult.add(getAbsolutePath(path));
		});
		return lsResult;
	}
	/**
	 * 给定一个文件名，返回该文件名上有几个文件夹，不包括本文件夹 <br>
	 * 如 给定 /home/novelbio/test 和 /home/novelbio/test/ 都返回2<br>
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

	/**
	 * 如果存在直接返回
	 * 
	 * @param path
	 */
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
	/** 根据给定的后缀，产生相应的流，譬如如果后缀是gz，就包装为gz格式 */
	public static OutputStream getOutputStreamWithSuffix(String filePath) throws IOException {
		OutputStream os = getOutputStream(filePath, false);
		return modifyOutputStream(TXTtype.getTxtType(filePath), os, getFileName(filePath));
	}
	
	public static OutputStream modifyOutputStream(TXTtype txtTtype, OutputStream outputStreamRaw, String zipfileName) throws IOException {
		OutputStream outputStream = null;
		if (txtTtype == TXTtype.Txt) {
			outputStream = new BufferedOutputStream(outputStreamRaw, TxtReadandWrite.bufferLen);
		} else if (txtTtype == TXTtype.Gzip) {
			outputStream = new BufferedOutputStream(new GZIPOutputStream(outputStreamRaw, TxtReadandWrite.bufferLen), TxtReadandWrite.bufferLen);
		} else if (txtTtype == TXTtype.Bzip2) {
			outputStream = new BufferedOutputStream(new BZip2CompressorOutputStream(outputStreamRaw), TxtReadandWrite.bufferLen);
		} else if (txtTtype == TXTtype.Zip) {
			ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(outputStreamRaw);
			ZipArchiveEntry entry = new ZipArchiveEntry(getFileNameSep(zipfileName)[0]);
			zipOutputStream.putArchiveEntry(entry);
			outputStream = new BufferedOutputStream(zipOutputStream, TxtReadandWrite.bufferLen);
		} else if (txtTtype == TXTtype.Lzo) {
			LzopCodec lzo = new LzopCodec();
			lzo.setConf(HdfsConfInitiator.getConf());
			CompressionOutputStream outputStreamCmp =lzo.createOutputStream(outputStreamRaw);
			outputStream = new BufferedOutputStream(outputStreamCmp);
		}
		return outputStream;
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
	
	/**
	 * 获取文件的写入流对象.<br/>
	 *  1.当文件所在的文件夹不存在时,会自动创建父级文件夹.<br/>
	 *  2.append为false.并文件大小大于0时,会删除现有文件.<br/>
	 * 
	 * @param file
	 * @param append
	 * @return
	 * @throws IOException
	 */
	public static OutputStream getOutputStream(Path file, boolean append) throws IOException {
		StandardOpenOption openOption = append ? StandardOpenOption.APPEND : StandardOpenOption.CREATE;
		if (!isFileExist(file)) {
			openOption = StandardOpenOption.CREATE;
		}
		if (append == false && isFileExistAndBigThan0(file)) {
			if (isFileDirectory(file)) {
				throw new ExceptionNbcFile("cannot create outputstream on folder " + file.toString());
			}
			deleteFileFolder(file);
		}
		createFolders(getParentPathNameWithSep(file));
		return Files.newOutputStream(file, openOption);
	}
	
	/**
	 * 拷贝文件
	 * 输入为 /home/novelbio/test
	 * 输出为 hdfs:/nbCloud/result
	 * 则会把 test 中的内容全拷贝到result中。注意<b>不会</b>在result中创建test文件夹。
	 * 
	 * 输入为 /home/novelbio/test
	 * 输出为 hdfs:/nbCloud/result/test
	 * 则会把 test 中的内容全拷贝到result/test中。注意<b>会</b>在result中创建test文件夹。
	 * @param oldPathFile
	 * @param newPathFile
	 * @param cover
	 */
	public static void copyFileFolder(String oldPathFile, String newPathFile, boolean cover) {
//		boolean isFolder = isFileDirectory(oldPathFile);
//		Path oldfile = getPath(oldPathFile);
//		if (isFolder) {
//			copyFolder(oldfile, newPathFile, cover);
//		} else {
//			copyFile(oldfile, newPathFile, cover);
//		}
		copyFileFolder(getPath(oldPathFile), newPathFile, cover);
	}

	public static void copyFileFolder(Path oldfile, String newPathFile, boolean cover) {
		boolean isFolder = isFileDirectory(oldfile);
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
		if (!isFileExist(oldFilePath)) {
			logger.error(oldFilePath + " is not exist");
		}
		if (!isFileDirectory(oldFilePath)) {
			logger.error(oldFilePath + " is not a folder");
			return false;
		}
		try {
			createFolders(newPathSep);
			for (Path pathOld : getLsFoldPath(oldFilePath)) {
				if (isFileDirectory(pathOld)) {
					copyFolder(pathOld, newPathSep + pathOld.getFileName(), cover);
				} else {
					copyFile(pathOld, newPathSep + pathOld.getFileName(), cover);
				}
			}
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
		copyFile(getPath(oldPathFile), newPathFile, cover);
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
		copyFile(oldfile, getPath(newfile), cover);
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
		if (!cover && isFileExist(pathNew))
			return;
		if (cover && isFileDirectory(pathNew)) {
			throw new ExceptionFileError("cannot cover directory " + pathNew);
		}
		Path pathNewTmp = getPath(changeFileSuffix(getAbsolutePath(pathNew), "_tmp", null));
		try {
			Files.deleteIfExists(pathNewTmp);
			createFolders(getPathName(pathNew));
			logger.debug("start copy from {} to {}", oldfile, pathNew);
			//XXX 这里注意.StandardCopyOption的其他两个参数底层不支持.所以这里必须是REPLACE_EXISTING
			Files.copy(oldfile, pathNewTmp, StandardCopyOption.REPLACE_EXISTING);
			Files.deleteIfExists(pathNew);
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
		if (isFileExist(fnew) && !cover) {
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
	
	/** 生成文件临时名字 */
	public static String getFileTmpName(String fileName) {
		return fileName + ".tmp";
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
	 * 既可移动文件夹又可移动单个文件
	 * @param cover 是否覆盖
	 * @param oldFileName 老文件全路径
	 * @param newFileName  新文件全路径
	 * @return
	 */
	public static void moveFile(boolean cover, String oldFileName, String newFileName) {
		moveFile(cover, getPath(oldFileName), newFileName, true);
	}
	/**
	 * 既可移动文件夹又可移动单个文件
	 * @param cover 是否覆盖
	 * @param oldFileName 老文件全路径
	 * @param newFileName  新文件全路径
	 * @param isDeleteFolder 如果是move的文件夹，move结束后是否删除已有的文件夹。
	 * @return
	 */
	public static void moveFile(boolean cover, String oldFileName, String newFileName, boolean isDeleteFolder) {
		moveFile(cover, getPath(oldFileName), newFileName, isDeleteFolder);
	}
	/**
	 * @param cover  是否覆盖
	 * @param oldFileName 老文件全路径
	 * @param newFileName 新文件全路径
	 * @return
	 */
	public static void moveFile(boolean cover, Path oldPath, Path newFileName) {
		moveFile(cover, oldPath, getAbsolutePath(newFileName), true);
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
		moveFile(cover, oldFile, newFileName, true);
	}

	/**
	 * @param oldFileName
	 * @param newPath
	 * @param NewName
	 *            新文件或文件夹名
	 * @param cover
	 * @return
	 */
	private static void moveFile(boolean cover, Path oldFile, String newPathName, boolean isDeleteFolder) {
		if (isFileExistAndNotDir(oldFile)) {
			moveSingleFile(oldFile, newPathName, cover);
		} else if (isFileDirectory(oldFile)) {
			moveFoldFile(oldFile, newPathName, cover, isDeleteFolder);
		}
	}

	/**
	 * 移动文件，如果新地址有同名文件，则不移动并返回<br>
	 * 可以创建一级新文件夹<br>
	 * 如果没有文件则返回<br>
	 * 注意：新文件夹后不要加\\<br>
	 * 
	 * @param oldPath 文件路径
	 * @param newPathStr 新文件名
	 * @param cover 是否覆盖
	 * @return true 成功 false 失败
	 */
	// TODO 待测试
	private static void moveSingleFile(Path oldPath, String newPathStr, boolean cover) {
		if (!isFileExistAndNotDir(oldPath))
			return;
		
		Path newPath = getPath(newPathStr);
		if (isFilePathSame(oldPath.toUri().toString(), newPath.toUri().toString())) {
			return;
		}

		// 文件新（目标）地址
		// new一个新文件夹
		Path fnewpathParent = newPath.getParent();
		createFolders(fnewpathParent);
		try {
			if (isFileExist(newPath)) {
				if (!cover)
					return;
				
				Path pathNewTmp = getPath(newPathStr + ".tmp" + DateUtil.getDateAndRandom());
				Files.deleteIfExists(pathNewTmp);
				Files.move(oldPath, pathNewTmp);
				Files.deleteIfExists(newPath);
				Files.move(pathNewTmp, newPath);
			} else {
				Files.move(oldPath, newPath);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ExceptionFileError("cannot move file " + oldPath + " to " + newPathStr, e);
		}
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
		moveFoldFile(olddir, newfolder, prix, cover, true);
	}

	private static void moveFoldFile(Path olddir, String newfolder, boolean cover, boolean isDeleteFolder) {
		moveFoldFile(olddir, newfolder, "", cover, isDeleteFolder);
	}
	
	/**
	 * 移动文件夹，将oldFolder文件夹下的全部文件以及目录移动到newFolder文件夹下面<br>
	 * 注意：处理完成后，oldFolder文件夹下的全部文件均会被删除，文件夹是否保留由deleteOldFolder参数决定<br>
	 * @param oldFolder 源文件夹，如果该文件夹不存在，则不做处理
	 * @param newFolder 目标文件夹
	 * @param prix 文件名前缀，全部移动后的文件名称均添加该前缀，注意仅文件添加，文件夹不添加，例如prix="mv-"，移动前文件名：log.conf，移动后文件名：mv-log.conf
	 * @param cover 是否覆盖，如果目标文件夹内已经存在同名文件或文件夹，true的时候则先删除后移动，false的时候则跳过不移动
	 * @param deleteOldFolder 是否删除源文件夹，true的时候将删除oldFolder文件夹，fasle的时候则保留oldFolder文件夹及其内部全部的子文件夹
	 * 
	 * @author novelbio luwei
	 * @date 20170928
	 */
	public static void moveFolder(String oldFolder, String newFolder, String prix, boolean cover, boolean deleteOldFolder) {
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		oldFolder = addSep(oldFolder);
		Path olddir = getPath(oldFolder);
		
		moveFoldFile(olddir, newFolder, prix, cover, deleteOldFolder);
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
	 * @param cover 是否覆盖
	 * @param isDeleteFolder 如果是move的文件夹，move结束后是否删除已有的文件夹。
	 * true: 删除olddir文件夹
	 * false: 保留olddir中的全部文件夹
	 * @throws Exception
	 */
	private static void moveFoldFile(Path olddir, String newfolder, String prix, boolean cover, boolean isDeleteFolder) {
		if (!isFileExist(olddir)) {
			logger.error(olddir + " is not exist");
			return;
		}
		if (!isFileDirectory(olddir)) {
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
		
		//如果可以删除文件夹，那么就可以考虑使用Files.move直接全移动过去
		if (isDeleteFolder) {
			if (!isFileExist(pathNew)) {
				try {
					Files.move(olddir, pathNew);
					return;
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}

		try {
			createFolders(pathNew);
			Files.list(olddir).forEach((pathOld) -> {
				if (isFileDirectory(pathOld)) {
					String newPath = removeSplashHead(newPathSep + pathOld.getFileName(), false);
					newPath = removeSplashTail(newPath, false);
					if (StringOperate.isEqual(newPath, olddirPath)) {
						isMakeDirSameAsOld[0] = true;
					}
					moveFoldFile(pathOld, newPathSep + pathOld.getFileName(), prefix, cover, isDeleteFolder);
				} else {
					String newPathStr = newPathSep + prefix + pathOld.getFileName();
					moveSingleFile(pathOld, newPathStr, cover);
				}
			});
		} catch (Exception e) {
			throw new ExceptionNbcFile("move fold error", e);
		}

		if (isDeleteFolder && !isMakeDirSameAsOld[0]) {
			deleteFileFolder(olddir);
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
	public static void linkFile(String rawFile, String linkTo, boolean cover) {
		if (!cover && (isFileExist(linkTo) || isSymbolicLink(linkTo))) {
			return;
		}
		if (!isFileExist(rawFile)) {
			return;
		}
		createFolders(getParentPathNameWithSep(linkTo));
		if (isFileExist(linkTo) && cover) {
			deleteFileFolder(linkTo);
		}

		rawFile = FileHadoop.convertToHdfsPath(rawFile);
		linkTo = FileHadoop.convertToHdfsPath(linkTo);
		boolean isRawHdfs = FileHadoop.isHdfs(rawFile);
		boolean isLinkHdfs = FileHadoop.isHdfs(linkTo);
		if (isRawHdfs ^ isLinkHdfs) {
			throw new ExceptionFileNotExist("RawFile And LinkTo File Are Not In Same FileSystem\n, raw File: " + rawFile
					+ "\nLinkTo: " + linkTo);
		}
		if (isRawHdfs) {
			throw new ExceptionNbcFile("could not creat symbolic link on hdfs from " + rawFile + " to " + linkTo);
		}
		try {
			Files.createSymbolicLink(getPath(linkTo), getPath(rawFile));
		} catch (IOException e) {
			throw new ExceptionNbcFile("could not creat symbolic link from " + rawFile + " to " + linkTo, e);
		}
//		List<String> lsCmd = new ArrayList<>();
//		lsCmd.add("ln");
//		lsCmd.add("-s");
//		lsCmd.add(rawFile);
//		lsCmd.add(linkTo);
//		CmdOperate cmdOperate = new CmdOperate(lsCmd);
//		cmdOperate.setTerminateWriteTo(false);
//		cmdOperate.runWithExp();
//		return true;
	}
	
	/**判断文件是否为文件夹,null直接返回false */
	public static boolean isFileDirectory(String fileName) {
		return isFileDirectory(getPath(fileName));
	}

	/** 如果file是文件夹，并且为空，则返回true，否则返回false */
	public static boolean isFileDirectoryEmpty(String file) {
		return isFileDirectory(getPath(file));
	}

	/** 如果file是文件夹，并且为空，则返回true，否则返回false */
	public static boolean isFileDirectoryEmpty(Path file) {
		if (file == null) {
			return false;
		}
		if (!isFileDirectory(file)) {// 没有文件，则返回空
			return false;
		}
		return getLsFoldPath(file).isEmpty();
	}

	/** 判断文件是否为文件夹,null直接返回false */
	@Deprecated
	public static boolean isFileDirectory(File file) {
//		if (file == null) return false;
//		return file.isDirectory();
		return isFileDirectory(getPath(file));
	}

	/** 判断文件是否为文件夹,null直接返回false */
	public static boolean isFileDirectory(Path file) {
		return file != null && Files.isDirectory(file);
	}

	/**
	 * 判断文件或文件夹是否存在，给的是绝对路径
	 * @param fileName  如果为null, 直接返回false
	 * @return 文件存在,返回true.否则,返回false
	 */
	public static boolean isFileExist(String file) {
		return isFileExist(getPath(file));
	}
	/**
	 * 判断文件或文件夹是否存在，给的是绝对路径
	 * @param fileName 如果为null, 直接返回false
	 * @return 文件存在,返回true.否则,返回false
	 */
	public static boolean isFileExist(Path path) {
		return path != null && Files.exists(path);
	}

	/**
	 * 判断文件是否存在，并且不是文件夹，给的是绝对路径
	 * 
	 * @param fileName
	 *            如果为null, 直接返回false
	 * @return 文件存在,返回true.否则,返回false
	 */
	public static boolean isFileExistAndNotDir(String fileName) {
		return isFileExistAndNotDir(getPath(fileName));
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
		return isFileExistAndNotDir(getPath(file));
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
		return isFileExist(file) && !isFileDirectory(file);
	}
	
	public static boolean isSymbolicLink(String fileName) {
		return isSymbolicLink(getPath(fileName));
	}
	public static boolean isSymbolicLink(Path path) {
		return Files.isSymbolicLink(path);
	}
	
	/**
	 * 判断文件是否存在，并且有一定的大小而不是空文件.可以是文件或文件夹,文件夹返回文件夹里所有文件的大小.
	 * 
	 * @param fileName
	 * @return
	 */
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
	 * 判断文件是否存在，并且有一定的大小而不是空文件.可以是文件或文件夹,文件夹返回文件夹里所有文件的大小.
	 * 
	 * @param fileName
	 *            如果为null, 直接返回false
	 * @param size
	 *            大小 byte为单位
	 * @return
	 */
	public static boolean isFileExistAndBigThanSize(String fileName, double size) {
//		if (StringOperate.isRealNull(fileName)) {
//			return false;
//		}
//		if (size < 0)
//			size = -1;
//		Path path = getPath(fileName);
		return isFileExistAndBigThanSize(getPath(fileName), size);
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
		if (size < 0) size = -1;
		return getFileSizeLong(path) > size;
	}

	/**
	 * 判断文件是否存在，并且有一定的大小而不是空文件
	 * 
	 * @deprecated 可以用isFileExistAndBigThanSize方法代替.两个方法功能重复
	 * 
	 * @param fileName
	 *            如果为null, 直接返回false
	 * @param size
	 *            大小 byte为单位
	 * @return
	 * @throws FileNotFoundException
	 */
	@Deprecated
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
		Path file = getPath(filePath);
		if (isFileExist(file)) {
			return false;
		}
		if (isFileExistAndNotDir(file)) {
			return getFileSizeLong(file) == realSize;
		}
		return false;
	}

	/**
	 * 删除文件
	 * 
	 * @param filePathAndName
	 *            文本文件完整绝对路径及文件名 文件不存在则返回false
	 * @return Boolean 成功删除返回true遭遇异常返回false
	 */
	private static void delFile(Path myDelFile) {
		try {
			Files.deleteIfExists(myDelFile);
		} catch (IOException e) {
			throw new ExceptionFileError("cannot delete path " + myDelFile, e);
		}
	}

	/**
	 * 删除指定文件夹下所有文件,但不删除本文件夹
	 * 
	 * @param path
	 *            文件夹完整绝对路径,最后无所谓加不加\\或/
	 */
	// TODO 待测试
	public static void deleteFolderClean(String path) {
		deleteFolderClean(getPath(path));
	}

//	@Deprecated
//	public static void deleteOnExit(final File file) {
//		final Path path = getPath(file.getPath());
//		Runtime.getRuntime().addShutdownHook(new Thread() {
//			public void run() {
//				deleteFileFolder(path);
//			}
//		});
//	}

	/**
	 * 删除文件或文件夹.<br/>
	 * <b>注意:该方法调用后不会立即删除文件后文件夹.是在jvm退出时,才会执行删除操作.正式代码务必谨慎使用.</b>
	 * @param path
	 */
	public static void deleteOnExit(final Path path) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				deleteFileFolder(path);
			}
		});
	}

	/**
	 * 删除文件或文件夹.<br/>
	 * <b>注意:该方法调用后不会立即删除文件后文件夹.是在jvm退出时,才会执行删除操作.正式代码务必谨慎使用.</b>
	 * @param path
	 */
	public static void deleteOnExit(String fileName) {
		final Path path = getPath(fileName);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				deleteFileFolder(path);
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
	public static void deleteFolderClean(Path path) {
		if (path == null || !isFileExist(path)) {
			return;
		}
		if (!isFileDirectory(path)) {
			throw new ExceptionFileError("path is not directory " + path);
		}

		try {
			Files.list(path).forEach((insidePath) -> {
				if (isFileDirectory(insidePath)) {
					deleteFolder(insidePath);
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
	 */
	private static void deleteFolder(Path dirFile) {
		if (isSymbolicLink(dirFile)) {
			delFile(dirFile);
			return;
		}
		
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
	 * @param sPath 要删除的目录或文件
	 */
	public static void deleteFileFolder(String sPath) {
		deleteFileFolder(getPath(sPath));
	}
	
	/**
	 * 根据路径删除指定的目录或文件，无论存在与否
	 * 
	 * @param sPath
	 *            要删除的目录或文件
	 * @return 删除成功返回 true，否则返回 false 不存在文件也返回true
	 */
	public static void deleteFileFolder(Path file) {
		if (file == null || !isFileExist(file)) return;
		
		if (isFileDirectory(file)) {
			deleteFolder(file);
		} else {
			delFile(file);
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
		createFolders(getParentPathNameWithSep(outPath));
		OutputStream os = getOutputStream(outPath);
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
		IOUtil.closeQuietly(stream);
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
			//这里写中文是因为后面会把这个异常信息贴到前端网页
			throw new ExceptionNbcFile("文件名只允许包含汉字,字母,数字,中划线和下划线.");
			//file name can only contains letters, Chinese, numbers, underline(_), middleline(-)
		}
		return fileName;
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
