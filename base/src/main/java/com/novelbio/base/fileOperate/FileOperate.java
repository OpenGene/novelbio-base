package com.novelbio.base.fileOperate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.log4j.Logger;

import com.novelbio.base.SerializeKryo;
import com.novelbio.base.StringOperate;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.dataStructure.PatternOperate.PatternUnit;
import com.novelbio.htsjdk.samtools.seekablestream.ISeekableStreamFactory;
import com.novelbio.htsjdk.samtools.seekablestream.SeekableStream;
import com.novelbio.htsjdk.samtools.seekablestream.SeekableStreamFactory;

public class FileOperate {
	private static final Logger logger = Logger.getLogger(FileOperate.class);
	static boolean isWindowsOS = false;
	static{
		    String osName = System.getProperty("os.name");
		    if(osName.toLowerCase().indexOf("windows")>-1){
		    	isWindowsOS = true;
		    }
	}
	
	/**
	 * 是否是windows操作系统
	 */
	public static boolean isWindows(){
	    return isWindowsOS;
	}
	/**
	 * 读取文本文件内容
	 * 
	 * @param filePathAndName
	 *            带有完整绝对路径的文件名
	 * @param encoding
	 *            文本文件打开的编码方式
	 * @return 返回文本文件的内容
	 */
	public String readTxt(String filePathAndName, String encoding){
		encoding = encoding.trim();
		StringBuffer str = new StringBuffer("");
		String st = "";
		try {
			InputStream fs = getInputStream(filePathAndName);
			
			InputStreamReader isr;
			if (encoding.equals("")) {
				isr = new InputStreamReader(fs);
			} else {
				isr = new InputStreamReader(fs, encoding);
			}
			BufferedReader br = new BufferedReader(isr);
			try {
				String data = "";
				while ((data = br.readLine()) != null) {
					str.append(data + " ");
				}
			} catch (Exception e) {
				str.append(e.toString());
			}
			st = str.toString();
		} catch (IOException es) {
			st = "";
		}
		return st;
	}
	
	/**
	 * 根据不同的文件类型得到File
	 * @param filePath
	 * @return
	 */
	public static File getFile(String filePathParent, String name) {
		if (!StringOperate.isRealNull(filePathParent.trim())) {
			filePathParent = FileOperate.addSep(filePathParent);
		}
		String path = filePathParent + name;
		return getFile(path);
	}
	
	/**
	 * 根据不同的文件类型得到File
	 * @param filePath
	 * @return
	 */
	public static File getFile(File fileParent, String name) {
		if (fileParent instanceof FileHadoop) {
			return new FileHadoop(FileOperate.addSep(fileParent.getAbsolutePath()) + name);
		} else {
			return new File(fileParent, name);
		}
	}
	
	/**
	 * 根据不同的文件类型得到File.<br/><br/>
	 * @param filePath
	 * @return
	 */
	public static File getFile(String filePath) {
		File file = null;
		boolean isHdfs = FileHadoop.isHdfs(filePath);
		if (isHdfs) {
			file = new FileHadoop(filePath);
		}else {
			file = new File(filePath);
		}
		return file;
	}
	
	/** 文件尾巴加个 "/" */
	public static String getSepPath() {
		return File.separator;
	}
	
	/**
	 * 将对象写成文件
	 * @param object 对象
	 * @param pathAndName 文件路径及名称
	 * @return
	 */
	public static boolean writeObjectToFile(Object object,String fileName) {
		OutputStream fs = getOutputStream(fileName);
		try {
			SerializeKryo kryo =new SerializeKryo();
			fs.write(kryo.write(object));
			fs.flush();
			return true;
		} catch (Exception e) {
			return false;// TODO: handle exception
		} finally{
			closeOs(fs);
		}
	}
	
	public static Object readFileAsObject(String fileName) {
		InputStream fs = null;
		try {
			fs = getInputStream(fileName);
			SerializeKryo kryo =new SerializeKryo();
			return kryo.read(IOUtils.toByteArray(fs));
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			closeIs(fs);
		}
		
		return null;// TODO: handle exception
	}
	
	/**
	 * 给定路径名，返回其上一层路径，带"/" 如给定 /wer/fw4e/sr/frw/s3er.txt 返回 /wer/fw4e/sr/frw/<br>
	 * 如果为相对路径的最上层，譬如给定的是soap 则返回“” 可以给定不存在的路径
	 * 
	 * @param fileName
	 * @return
	 * @throws IOException 
	 */
	public static String getParentPathNameWithSep(String fileName) {
		if (fileName == null) return null;
		File file = new File(fileName);
		String fileParent = file.getParent();
		if (fileParent == null) {
			return "";
		} else {
			return addSep(fileParent);
		}
	}
	/**
	 * 给定路径名，返回其最近一层路径，带"/" 如给定 /wer/fw4e/sr/frw/s3er.txt 返回 /wer/fw4e/sr/frw/<br>
	 * 给定/wef/tesw/tre/还是返回/wef/tesw/tre/
	 * @param fileName
	 * @return
	 * @throws IOException 
	 */
	public static String getPathName(String fileName){
		if (fileName == null) return null;
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
	
	/** 返回日期格式的最后修改时间 */
	public static String getTimeLastModifyStr(String fileName) {
        File f = getFile(fileName);
        Calendar cal = Calendar.getInstance();  
        long time = f.lastModified();  
        cal.setTimeInMillis(time);    
        DateFormat formatter = DateFormat.getDateTimeInstance();
        String data = formatter.format(cal.getTime());
        return data;
	}
	
	public static long getTimeLastModify(String fileName) {
        File f = getFile(fileName);
        return f.lastModified();
	}
	public static long getTimeLastModify(File file) {
		if (!file.exists()) {
			return 0;
		}
        return file.lastModified();
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
	 * 返回以K为单位的估计文件的总和，压缩文件就会加倍估计<br>
	 * 不准，只是估计而已
	 * 
	 * @return
	 * @throws IOException 
	 */
	public static double getFileSizeEvaluateK(Collection<String> colFileName){
		double allFileSize = 0;
		for (String fileName : colFileName) {
			double size = (double)FileOperate.getFileSizeLong(fileName)/1024;
			// 如果是压缩文件就假设源文件为6倍大 */
			String suffix = getFileNameSep(fileName)[1].toLowerCase();
			if (suffix.equals("gz") || suffix.equals("zip")
					|| suffix.equals("rar"))
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
	 * @return 没有文件返回0；出错返回-1000000000
	 * @throws IOException 
	 */
	public static long getFileSizeLong(String filePath) {
		File file = getFile(filePath);
		long totalsize = 0;
		if (!file.exists()) {
			return 0;
		}
		if (file.isFile()) {
			long size = file.length();
//			if (size == 0 && file instanceof FileHadoop) {
//				return getFileSizeLong(FileHadoop.convertToLocalPath(filePath));
//			}
			return size;
		} else if (file.isDirectory()) {
			List<File> lsFileChild = getFoldFileLs(file, "*", "*");
			for (File fileChild : lsFileChild) {
				totalsize = totalsize + getFileSizeLong(fileChild);
			}
			return totalsize;
		} else {
			logger.error("出错！");
			return -1000000000;
		}
	}
	/**
	 * <b>未经测试</b> 给定文件路径，返回大小，单位为byte
	 * @param filePath
	 * @return 没有文件返回0；出错返回-1000000000
	 * @throws IOException 
	 */
	public static long getFileSizeLong(File file) {
		long totalsize = 0;
		if (!file.exists()) {
			return 0;
		}
		if (file.isFile()) {
			long size = file.length();
//			if (size == 0 && file instanceof FileHadoop) {
//				return getFileSizeLong(FileHadoop.convertToLocalPath( file.getAbsolutePath()));
//			}
			return size;
		} else if (file.isDirectory()) {
			List<File> lsFile = getFoldFileLs(file);
			for (File fileSub : lsFile) {
				totalsize = totalsize + getFileSizeLong(fileSub);
			}
			return totalsize;
		} else {
			logger.error("出错！");
			return -1000000000;
		}
	}
	/**
	 * 给定路径名，返回其名字,不带后缀名<br>
	 * 如给定/home/zong0jie.aa.txt<br>
	 * 则返回zong0jie.aa 和 txt<br><br>
	 * 给定/home/zong0jie.aa.txt/，则返回""和""<br>
	 * 可以给定不存在的路径<br>
	 * 
	 * @param fileName
	 * @return string[2] 0:文件名 1:文件后缀
	 */
	public static String[] getFileNameSep(String fileName) {
		if (fileName.endsWith("/") || fileName.endsWith("\\")) {
			return new String[]{"", ""};
		}
		File file = getFile(fileName);
		String filename = file.getName();
		return getFileNameSepWithoutPath(filename);
	}
	
	/** 给定文件的相对路径名，返回文件名字
	 * 
	 * @param fileNameWithoutPath
	 * @return string[2] 0:文件名 1:文件后缀
	 */
	private static String[] getFileNameSepWithoutPath(String fileNameWithoutPath) {
		String[] result = new String[2];
		int endDot = fileNameWithoutPath.lastIndexOf(".");
		if (endDot > 0) {
			result[0] = (String) fileNameWithoutPath.subSequence(0, endDot);
			result[1] = (String) fileNameWithoutPath.subSequence(endDot + 1,
					fileNameWithoutPath.length());
		} else {
			result[0] = fileNameWithoutPath;
			result[1] = "";
		}
		return result;
	}
	
	/**
	 * 获取文件夹下所有文件名与后缀,不包含路径 * 如果文件不存在则返回null<br>
	 * 
	 * @param filePath
	 *            目录路径,最后不要加\\或/
	 * @return arraylist 里面是string[2] 1:文件名 2：后缀 文件 wfese.fse.fe认作
	 *         "wfese.fse"和"fe"<br>
	 *         文件 wfese.fse.认作 "wfese.fse."和""<br>
	 *         文件 wfese 认作 "wfese"和""<br>
	 * @throws IOException 
	 */
	public static ArrayList<String[]> getFoldFileName(String filePath){
		return getFoldFileName(filePath, "*", "*");
	}

	/**
	 * 获取文件夹下包含指定文件名与后缀的所有文件名,等待增加功能子文件夹下的文件。也就是循环获得文件<br>
	 * 如果文件不存在则返回null<br>
	 * 如果不是文件夹，则返回该文件名<br>
	 * 
	 * @param filePath
	 *            目录路径,最后不要加\\或/
	 * @param filename
	 *            指定包含的文件名，是正则表达式 ，如 "*",正则表达式无视大小写
	 * @param suffix
	 *            指定包含的后缀名，是正则表达式<br>
	 *            文件 wfese.fse.fe认作 "wfese.fse"和"fe"<br>
	 *            文件 wfese.fse.认作 "wfese.fse."和""<br>
	 *            文件 wfese 认作 "wfese"和""<br>
	 * @return 返回包含目标文件名的ArrayList。里面是string[2] 1:文件名 2：后缀
	 * @throws IOException 
	 */
	public static ArrayList<String[]> getFoldFileName(String filePath, String filename, String suffix){
		ArrayList<String> lsFileName = getFoldFileNameLs(filePath, filename,
				suffix);
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (String fileName : lsFileName) {
			String[] fileNameSep = getFileNameSep(fileName);
			lsResult.add(fileNameSep);
		}
		return lsResult;
	}
	
	/** 将文件开头的"//"这种多个的去除
	 * @param fileName
	 * @param keepOne 是否保留一个“/”
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
	/** 将文件结尾的"//"这种多个的去除
	 * @param fileName
	 * @param keepOne 是否保留一个“/”
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
				fileNameThis = fileNameThis.substring(0,fileNameThis.length() - 1);
			} else {
				break;
			}
		}
		return fileNameThis;
	}
	
	/**
	 * 获取文件夹下包含指定文件名与后缀的所有文件名,等待增加功能子文件夹下的文件。也就是循环获得文件<br>
	 * 如果文件不存在则返回空的list<br>
	 * 如果不是文件夹，则返回该文件名<br>
	 * 
	 * @param filePath 目录路径
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
	 * 获取文件夹下全部文件名
	 * @return 返回包含目标文件全名的List
	 * @throws IOException 
	 */
	public static List<File> getFoldFileLs(File file) {
		return getFoldFileLs(file, "*", "*");
	}
	/**
	 * 获取文件夹下全部文件名
	 * @return 返回包含目标文件全名的List
	 * @throws IOException 
	 */
	public static List<File> getFoldFileLs(String file) {
		return getFoldFileLs(file, "*", "*");
	}
	
	/**
	 * 获取文件夹下包含指定文件名与后缀的所有文件名,等待增加功能子文件夹下的文件。也就是循环获得文件<br>
	 * 如果文件不存在则返回空的list<br>
	 * 如果不是文件夹，则返回该文件名<br>
	 * 
	 * @param filePath 目录路径
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
	 * 获取文件夹下包含指定文件名与后缀的所有文件名,等待增加功能子文件夹下的文件。也就是循环获得文件<br>
	 * 如果文件不存在则返回null<br>
	 * 如果不是文件夹，则返回该文件名<br>
	 * 
	 * @param filePath
	 *            目录路径,最后不要加\\或/
	 * @return 返回包含目标文件全名的ArrayList
	 * @throws IOException 
	 */
	public static ArrayList<String> getFoldFileNameLs(String filePath) {
		List<File> lsFile = getFoldFileLs(filePath);
		ArrayList<String> lsFileName = new ArrayList<String>();
		for (File file : lsFile) {
			lsFileName.add(file.getAbsolutePath());
		}
		return lsFileName;
	}
	/**
	 * 获取文件夹下包含指定文件名与后缀的所有文件名,等待增加功能子文件夹下的文件。也就是循环获得文件<br>
	 * 如果文件不存在则返回null<br>
	 * 如果不是文件夹，则返回该文件名<br>
	 * 
	 * @param filePath
	 *            目录路径,最后不要加\\或/
	 * @param filename
	 *            指定包含的文件名，是正则表达式 ，如 "*",正则表达式无视大小<br>
	 *            null 表示不指定
	 * @param suffix
	 *            指定包含的后缀名，是正则表达式<br>
	 *            文件 wfese.fse.fe认作 "wfese.fse"和"fe"<br>
	 *            文件 wfese.fse.认作 "wfese.fse."和""<br>
	 *            文件 wfese 认作 "wfese"和""<br>
	 *            null 表示不指定
	 *            如果要指定多个后缀，可以用fastq|fq.gz|fq 这种形式 不需要\\|
	 * @return 返回包含目标文件全名的ArrayList
	 * @throws IOException 
	 */
	public static ArrayList<String> getFoldFileNameLs(String filePath, String filename, String suffix) {
		List<File> lsFile = getFoldFileLs(filePath, filename, suffix);
		ArrayList<String> lsFileName = new ArrayList<String>();
		for (File file : lsFile) {
			lsFileName.add(file.getAbsolutePath());
		}
		return lsFileName;
	}

	/**
	 * 是符合条件的文件名
	 * 
	 * @param pattern
	 *            用来分割文件名的正则 Pattern pattern = Pattern.compile("(.*)\\.(\\w*)",
	 *            Pattern.CASE_INSENSITIVE);
	 * @param matcher
	 * @param fileName
	 *            输入的文件名
	 * @param regxFilename
	 *            指定包含的文件名，是正则表达式 ，如 "*",正则表达式无视大小写
	 * @param regxSuffix
	 *            指定包含的后缀名，是正则表达式<br>
	 *            文件 wfese.fse.fe认作 "wfese.fse"和"fe"<br>
	 *            文件 wfese.fse.认作 "wfese.fse."和""<br>
	 *            文件 wfese 认作 "wfese"和""<br>
	 * @return
	 */
	private static boolean isNeedFile(PatternOperate patName, PatternOperate patSuffix, 
			String fileName, String regxFilename, String regxSuffix) {
		String[] fileNameSep = getFileNameSep(fileName);
		if (patName != null && patName.getPatFirst(fileNameSep[0]) == null) {
			return false;
		}
		if (patSuffix == null) {
			return true;
		}
		List<PatternUnit> lsPat = patSuffix.searchStr(fileName);
		if (lsPat.isEmpty()) {
			return false;
		}
		for (int i = lsPat.size() - 1; i >= 0; i--) {
			PatternUnit patUnit = lsPat.get(i);
			if (patUnit.getEndLoc() == 1) {
				return true;
			}
		}
		return true;
	}

	/**
	 * 新建目录,如果新文件夹存在也返回ture
	 * 
	 * @param folderPath
	 *            目录路径,最后不要加\\或/
	 * @return 返回目录创建后的路径
	 */
	private static boolean createFolder(String folderPath) {
		File myFilePath = getFile(folderPath);
		if (!myFilePath.exists()) {
			return myFilePath.mkdir();
		}
		return true;
	}

	/**
	 * 多级目录创建
	 * 
	 * @param folderPath
	 *            准备要在本级目录下创建新目录的目录路径 例如 /home/novelbio/myf，最后可以不加"/"
	 * @param paths
	 *            无限级目录参数，各级目录以/或\\区分 例如 a/b/c
	 * @return 返回创建文件后的路径 例如 c:/myf/a/b/c
	 */
	public static boolean createFolders(String folderPath) {
		if (isFileExist(folderPath))
			return false;
		if (isFileDirectory(folderPath))
			return true;

		String foldUpper = folderPath;
		String creatPath = "";
		boolean flag = true;
		while (flag) {
			if (foldUpper.equals("")) {
				return false;
			}
			if (isFileDirectory(foldUpper)) {
				flag = false;
				break;
			}
			creatPath = getFileName(foldUpper) + File.separator + creatPath;
			foldUpper = getParentPathNameWithSep(foldUpper);
		}
		foldUpper = addSep(foldUpper);
		String subFold = "";
		String[] sepID = creatPath.split(getSep());
		String firstPath = foldUpper + sepID[0];
		for (int i = 0; i < sepID.length; i++) {
			subFold = subFold + sepID[i] + File.separator;
			if (!createFolder(foldUpper + subFold)) {
				logger.error("创建目录操作出错！" + foldUpper + subFold);
				DeleteFileFolder(firstPath);
				return false;
			}
		}
		return true;
	}

	private static String getSep() {
		if (File.separator.equals("\\")) {
			return "\\\\";
		} else {
			return File.separator;
		}
	}

	/**
	 * 删除文件
	 * 
	 * @param filePathAndName
	 *            文本文件完整绝对路径及文件名 文件不存在则返回false
	 * @return Boolean 成功删除返回true遭遇异常返回false
	 */
	public static boolean delFile(String filePathAndName) {
		boolean bea = false;
		try {
			String filePath = filePathAndName;
			File myDelFile = getFile(filePath);
			if (myDelFile.exists()) {
				bea = myDelFile.delete();
			} else {
				bea = false;
				// message = (filePathAndName+"删除文件操作出错");
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
		//XXX 不明白为什么要sleep,待确定.
		try { Thread.sleep(500); } catch (InterruptedException e) { }
		return bea;
	}
	/**
	 * 删除文件
	 * 
	 * @param filePathAndName
	 *            文本文件完整绝对路径及文件名 文件不存在则返回false
	 * @return Boolean 成功删除返回true遭遇异常返回false
	 */
	public static boolean delFile(File myDelFile) {
		boolean bea = false;
		try {
			if (myDelFile.exists()) {
				bea = myDelFile.delete();
			} else {
				bea = false;
				// message = (filePathAndName+"删除文件操作出错");
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
		try { Thread.sleep(500); } catch (InterruptedException e) { }
		return bea;
	}
	/**
	 * 删除文件夹
	 * 
	 * @param folderPath
	 *            文件夹完整绝对路径
	 * @return
	 */
	public static void delFolder(String folderPath) {
		if(StringOperate.isRealNull(folderPath))
			return;
		try {
			delAllFile(folderPath); // 删除完里面所有内容
			String filePath = folderPath;
			filePath = filePath.toString();
			File myFilePath = getFile(filePath);
			myFilePath.delete(); // 删除空文件夹
		} catch (Exception e) {
			logger.error("删除文件夹操作出错");
		}
		try { Thread.sleep(500); } catch (InterruptedException e) { }
	}

	/**
	 * 删除指定文件夹下所有文件,
	 * 
	 * @param path
	 *            文件夹完整绝对路径,最后无所谓加不加\\或/
	 * @return
	 * @return
	 */
	public static boolean delAllFile(String path) {
		path = addSep(path);
		boolean bea = false;
		File file = getFile(path);
		if (!file.exists()) {
			return bea;
		}
		if (!file.isDirectory()) {
			return bea;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			temp = getFile(path + tempList[i]);
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + tempList[i]);// 先删除文件夹里面的文件
				delFolder(path + tempList[i]);// 再删除空文件夹
				bea = true;
			}
		}
		try { Thread.sleep(500); } catch (InterruptedException e) { }
		return bea;
	}
	
	public static SeekableStream getInputStreamSeekable(String filePath) throws FileNotFoundException {
		ISeekableStreamFactory seekableStreamFactory = SeekableStreamFactory.getInstance();
		try {
			return seekableStreamFactory.getStreamFor(filePath);
		} catch (Exception e) {
			throw new FileNotFoundException("cannot file file: " + filePath);
		}
	}
	
	public static InputStream getInputStream(String filePath) throws IOException {
		 if (FileHadoop.isHdfs(filePath)) {
        	return new FileHadoop(filePath).getInputStream();
        } else {
        	return new FileInputStream(new File(filePath));
        }
	}
	
	public static InputStream getInputStream(File file) throws FileNotFoundException {
		 if (file instanceof FileHadoop) {
			 return ((FileHadoop)file).getInputStream();
       } else {
       	return new FileInputStream(file);
       }
	}
	
	public static OutputStream getOutputStream(String filePath) {
		return getOutputStream(filePath, false);
	}
	
	public static OutputStream getOutputStream(String filePath, boolean append) {
		try {
			File file =  getFile(filePath);
			OutputStream fs = null;
			if (file instanceof FileHadoop) {
				FileHadoop fileHadoop = (FileHadoop) file;
				FSDataOutputStream fsHdfs = fileHadoop.getOutputStreamNew(!append);
				fs = new OutputStreamHdfs(fsHdfs);
			} else {
				fs = new FileOutputStream(file, append);
			}

			return fs;
		} catch(Exception e) {
			logger.error("get output stream error: " + filePath + "   is append: " + append, e);
			e.printStackTrace();
			return null;
		}
	}

	public static OutputStream getOutputStream(File file) throws FileNotFoundException {
		return getOutputStream(file, false);
	}
	
	public static OutputStream getOutputStream(File file, boolean append) throws FileNotFoundException {
		boolean cover = !append;
		OutputStream fs = null;
		if (file instanceof FileHadoop) {
			FileHadoop fileHadoop = (FileHadoop) file;
			FSDataOutputStream fsHdfs = fileHadoop.getOutputStreamNew(cover);
			fs = new OutputStreamHdfs(fsHdfs);
		}else {
			fs = new FileOutputStream(file, append);
		}

		return fs;
	}
	
	public static boolean copyFileFolder(String oldPathFile, String newPathFile, boolean cover) {
		boolean isFolder = FileOperate.isFileDirectory(oldPathFile);
		if (isFolder) {
			return copyFolder(oldPathFile, newPathFile, cover);
		} else {
			return copyFile(oldPathFile, newPathFile, cover);
		}
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
	public static boolean copyFile(String oldPathFile, String newPathFile, boolean cover) {
		File oldfile = getFile(oldPathFile);
		if (!FileOperate.isFileExist(oldfile)) {
			throw new ExceptionNbcFile("no file exist: " + oldfile);
		}
		String newPathTmp = FileOperate.changeFileSuffix(newPathFile, "_tmp", null);
		File newfile = getFile(newPathTmp);
		boolean isSucess = copyFile(oldfile, newfile, cover);
		if (isSucess) {
			return moveFile(true, newPathTmp, newPathFile);
		}
		return false;
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
	private static boolean copyFile(File oldfile,File newfile, boolean cover) {
		if (oldfile != null && isFilePathSame(oldfile, newfile)) {
			return true;
		}
		try {
			if (oldfile.exists()) { // 文件存在时
				if (newfile.exists()) {
					if (!cover) {
						return false;
					}
					newfile.delete();
				}
				InputStream inStream = FileOperate.getInputStream(oldfile);// 读入原文件
				OutputStream fs = FileOperate.getOutputStream(newfile);
				IOUtils.copy(inStream, fs);
				inStream.close();
				fs.flush();
				fs.close();
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			throw new ExceptionNbcFile("copy file from " + oldfile + " to " + newfile + " error" , e);
		}
	}
	/**
	 * 复制整个文件夹的内容,如果要文件已经存在，则跳过
	 * 
	 * @param oldPath
	 *            准备拷贝的目录，最后都无所谓加不加"/"
	 * @param newPath
	 *            指定绝对路径的新目录
	 * @return
	 */
	public static boolean copyFolder(String oldPath, String newPath, boolean cover) {
		oldPath = addSep(oldPath);
		File a = getFile(oldPath);
		return copyFolder(a, newPath, cover);
	}
	/**
	 * 复制整个文件夹的内容,如果要文件已经存在，则跳过
	 * 
	 * @param oldPath
	 *            准备拷贝的目录，最后都无所谓加不加"/"
	 * @param newPath
	 *            指定绝对路径的新目录
	 * @return
	 */
	public static boolean copyFolder(File oldFilePath, String newPath, boolean cover) {
		newPath = addSep(newPath);
		try {
			getFile(newPath).mkdirs(); // 如果文件夹不存在 则建立新文件夹
			File[] file = oldFilePath.listFiles();
			File temp = null;
			for (int i = 0; i < file.length; i++) {
				temp = file[i];
				if (temp.isFile()) { // 如果目标文件夹已经存在文件，则跳过
					File targetfile = getFile(newPath + (temp.getName()).toString());
					if (targetfile.exists()) {
						if (!cover) {
							continue;
						}
						targetfile.delete();
					}
					
					InputStream input = FileOperate.getInputStream(temp);// 读入原文件
					OutputStream output = FileOperate.getOutputStream(targetfile, !cover);
					IOUtils.copy(input, output);
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory()) {// 如果是子文件夹
					copyFolder(file[i],
							newPath + "/" + file[i].getName(), cover);
				}
			}
		} catch (Exception e) {
			throw new ExceptionNbcFile("copy fold error", e);
		}
		return true;
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
	public static void changeFileName(String oldName, String newName) {
		changeFileName(oldName, newName, false);
	}

	/**
	 * 只修输入的文件名，并不直接操作文件 文件添加<b>前缀</b>并改后缀名，如果一样则不修改
	 * 如果文件以“/”结尾，则直接添加后缀
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
	public static String changeFilePrefixReal(String fileName, String append, String suffix) {
		String newFile = changeFilePrefix(fileName, append, suffix);
		moveSingleFile(fileName, getParentPathNameWithSep(newFile),
				getFileName(newFile), true);
		return newFile;
	}

	/**
	 * 只修输入的文件名，并不直接操作文件 文件添加<b>后缀</b>并改后缀名，如果一样则不修改<br>
	 * 可以修改输入的uri
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
	 * @param fileName
	 *            原来文件的全名
	 * @param append 要添加的后缀，譬如_1，_new，如果为null，则不添加
	 * @param suffixOld 以前的后缀名，可以是txt，txt.gz，fq.gz等多个连在一起的名字，也可以实际上是bed.gz，但只写bed<br>
	 * 如果可能存在不确定的后缀，可以用竖线隔开，如 fq|fastq
	 * <b>无所谓大小写</b>
	 * @param suffixNew 新的后缀全名， suffix == null则不改变后缀名，suffix = ""表示删除后缀
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

		suffixOld = fileName.substring(maxEndDot, fileName.length());
		
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
	 * 输入文件名和需要修改的后缀，如果后缀为null则返回原来的后缀，否则返回新的后缀
	 * 后缀加上"."
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
		moveSingleFile(fileName, getParentPathNameWithSep(newFile),
				getFileName(newFile), true);
		return newFile;
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
	public static boolean changeFileName(String oldName, String newName,
			boolean cover) {
		// 文件原地址
		File oldFile = getFile(oldName);
		// 文件新（目标）地址
		File fnew = getFile(oldFile.getParentFile() + File.separator + newName);
		if (isFilePathSame(oldFile, fnew)) {
			return true;
		}
		if (fnew.exists() && !cover) {
			return false;
		} else {
			fnew.delete();
		}
		fnew = getFile(oldFile.getParentFile() + File.separator + newName);
		return oldFile.renameTo(fnew);
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
	public static boolean moveFile(String oldPath, String newPath, boolean cover) {
		return moveFile(oldPath, newPath, cover, "");
	}
	/**
	 * @param cover 是否覆盖
	 * @param oldFileName 老文件全路径
	 * @param newFileName 新文件全路径
	 * @return
	 */
	public static boolean moveFile(boolean cover, String oldFileName, String newFileName) {
		String newPath = FileOperate.getParentPathNameWithSep(newFileName);
		String newName = FileOperate.getFileName(newFileName);
		return moveFile(oldFileName, newPath, newName, cover);
		
	}

	/**
	 * @param oldFileName
	 * @param newPath
	 * @param NewName
	 *            新文件或文件夹名
	 * @param cover
	 * @return
	 */
	public static boolean moveFile(String oldFileName, String newPath,
			String NewName, boolean cover) {
		newPath = addSep(newPath);
		boolean okFlag = false;
		
		if (NewName == null || NewName.trim().equals("")) {
			NewName = getFileName(oldFileName);
		}
		File oldFile = getFile(oldFileName);
 		if (isFileExist(oldFile)) {
			okFlag = moveSingleFile(oldFile, newPath, NewName, cover);
		} else if (isFileDirectory(oldFile)) {
			newPath = newPath + NewName;
			okFlag = moveFoldFile(oldFile, newPath, "", cover);
		}
		deleteDirectory(oldFile);
		return okFlag;
	}

	/**
	 * 移动文件
	 * 
	 * @param oldPath
	 *            老文件的路径
	 * @param newPath 新文件夹
	 * @param cover
	 *            是否覆盖
	 * @param NewNameOrPrefix
	 *            如果移动的是文件，则为新文件名。如果移动的是文件夹，则为新文件夹的前缀
	 * @return
	 */
	public static boolean moveFile(String oldFilePath, String newPath,
			boolean cover, String NewNameOrPrefix) {
		if (oldFilePath == null) {
			return false;
		}
		File fileOldFilePath = getFile(oldFilePath);
		newPath = addSep(newPath);
		boolean okFlag = false;
		
		if (isFileExist(oldFilePath)) {
			if (NewNameOrPrefix == null || NewNameOrPrefix.trim().equals("")) {
				NewNameOrPrefix = FileOperate.getFileName(oldFilePath);
			}
			okFlag = moveSingleFile(oldFilePath, newPath, NewNameOrPrefix, cover);
		} else if (isFileDirectory(oldFilePath)) {
			newPath = newPath + getFileName(oldFilePath);
			okFlag = moveFoldFile(oldFilePath, newPath, NewNameOrPrefix, cover);
		}
		deleteDirectory(fileOldFilePath);
		return okFlag;
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
	 *            新文件所在的文件夹
	 * @param newName
	 *            新文件的文件名
	 * @param cover
	 *            是否覆盖
	 * @return true 成功 false 失败
	 */
	private static boolean moveSingleFile(String oldFileName, String newPath,
			String newName, boolean cover) {
		newPath = addSep(newPath);
		// 文件原地址
		File oldFile = getFile(oldFileName);
		return moveSingleFile(oldFile, newPath, newName, cover);
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
	 *            新文件所在的文件夹
	 * @param newName
	 *            新文件的文件名
	 * @param cover
	 *            是否覆盖
	 * @return true 成功 false 失败
	 */
	private static boolean moveSingleFile(File oldFile, String newPath,
			String newName, boolean cover) {
		newPath = addSep(newPath);
		File fnew = getFile(newPath + newName);

		// 文件新（目标）地址
		// new一个新文件夹
		File fnewpath = getFile(newPath);
		
		if (isFilePathSame(oldFile, fnew)) {
			return true;
        }
		
		if (!oldFile.exists()) {
			return false;
		}
		// 判断文件夹是否存在
		if (!fnewpath.exists())
			fnewpath.mkdirs();// 创建新文件
		// 将文件移到新文件里
		if (fnew.exists()) {
			if (!cover) {
				return false;
			}
			fnew.delete();
		}
		if (!oldFile.renameTo(fnew)) {
			if (copyFile(oldFile, fnew , cover)) {
				oldFile.delete();
				return true;
			}
			return false;
		}
		return true;
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
	public static boolean moveFoldFile(String oldfolderfile,
			String newfolderfile, String prix, boolean cover) {
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		oldfolderfile = addSep(oldfolderfile);
		newfolderfile = addSep(newfolderfile);

		File olddir = getFile(oldfolderfile);
		return moveFoldFile(olddir, newfolderfile, prix, cover);
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
	public static boolean moveFoldFile(File olddir,
			String newfolderfile, String prix, boolean cover) {
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		newfolderfile = addSep(newfolderfile);
		
		File fileNew = FileOperate.getFile(newfolderfile);
		if (isFilePathSame(olddir, fileNew)) {
			return true;
        }
		
		boolean ok = true;
		File[] files = olddir.listFiles(); // 文件一览
		if (files == null)
			return false;

		if (!isFileDirectory(newfolderfile)) {
			if (!createFolders(newfolderfile)) {
				return false;
			}
		}
		// 文件移动
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) // 如果子文件是文件夹，则递归调用本函数，精彩的用法！！
			{
				ok = moveFoldFile(files[i], newfolderfile + files[i].getName(), prix, cover);
				// 成功，删除原文件
				if (ok) {
					files[i].delete();
				}
				continue;
			}
			File fnew = getFile(newfolderfile + prix + files[i].getName());
			// 目标文件夹下存在的话，不变
			if (fnew.exists()) {
				if (!cover) {
					ok = false;
					continue;
				}
				fnew.delete();
			}
			if (!files[i].renameTo(fnew)) {
				if (copyFile(files[i].getAbsolutePath(), fnew.getAbsolutePath(), cover))
					files[i].delete();
				else {
					ok = false;
				}
			}
		}
		return ok;
	}
	
	public static boolean isFilePathSame(File oldFile, File newFile) {
		if (oldFile == null && newFile != null
				||
				oldFile != null && newFile == null
				) {
			return false;
        }
		if (oldFile == null && newFile == null) {
			return true;
        }
		
		try {
			String oldFileStr = oldFile.getCanonicalPath();
			String newFileStr = newFile.getCanonicalPath();
			
			if (oldFileStr != null) oldFileStr = oldFileStr.replace("\\", "/");
			if (newFileStr != null) newFileStr = newFileStr.replace("\\", "/");
			
			if (StringOperate.isEqual(oldFileStr, newFileStr)) {
				return true;
			}
			if (oldFileStr == null || newFileStr == null) {
				return false;
            }
			
			
			File fileOld = new File(oldFileStr);
			File fileNew = new File(newFileStr);
			
			if (StringOperate.isEqual(fileOld.getCanonicalPath(), fileNew.getCanonicalPath())) {
				return true;
			}
			return false;
		} catch (IOException e) {
			throw new ExceptionFileError("file name is not correct, old file " + oldFile.getName() + " new file " + newFile.getName() , e);
		}
	}
	
	/**
	 * 创建快捷方式，目前只能在linux下使用 内部会根据linkTo的路径自动创建文件夹
	 * <br>HDFS上没有用
	 * @param rawFile
	 * @param linkTo
	 * @param cover 是否覆盖
	 * @return 返回是否创建成功
	 */
	public static boolean linkFile(String rawFile, String linkTo, boolean cover) {		
		if (!FileOperate.isFileExist(rawFile)) {
			return false;
		}
		if (!FileOperate.createFolders(FileOperate.getParentPathNameWithSep(linkTo))) {
			return false;
		}
		if (FileOperate.isFileExist(linkTo) && cover) {
			FileOperate.delFile(linkTo);
		}
		rawFile = FileHadoop.convertToHadoop(rawFile);
		linkTo = FileHadoop.convertToHadoop(linkTo);
		boolean isRawHdfs = FileHadoop.isHdfs(rawFile);
		boolean isLinkHdfs = FileHadoop.isHdfs(linkTo);
		if (isRawHdfs ^ isLinkHdfs) {
			throw new ExceptionFileNotExist("RawFile And LinkTo File Are Not In Same FileSystem\n, raw File: " 
					+ rawFile  + "\nLinkTo: " + linkTo);
		}
		if (isRawHdfs) {
			try {
				HdfsInitial.getFileContext().createSymlink(FileHadoop.getPath(rawFile), FileHadoop.getPath(linkTo), false);
			} catch (Exception e) {
				throw new ExceptionNbcFile("could not creat symbolic link on hdfs");
			}
		}
		
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("ln"); lsCmd.add("-s");
		lsCmd.add(rawFile); lsCmd.add(linkTo);
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setTerminateWriteTo(false);
		cmdOperate.run();
		return true;
	}

	/**
	 * 判断文件是否存在，并且不是文件夹，给的是绝对路径
	 * 
	 * @param fileName
	 *            如果为null, 直接返回false
	 * @return 文件存在,返回true.否则,返回false
	 */
	public static boolean isFileExist(String fileName) {
		if (fileName == null || fileName.trim().equals("")) {
			return false;
		}
		
		File file = getFile(fileName);
		/**
		 * modify by fans.fan 150709 一句话的逻辑,写的好复杂.修改一下.
		if (file.exists() && !file.isDirectory()) {// 没有文件，则返回空
			return true;
		} else {
			return false;
		}
		 */
		return file.exists() && !file.isDirectory();
		//end by fans.fan
	}
	
	/**
	 * 判断文件是否存在，并且不是文件夹，给的是绝对路径
	 * 
	 * @param fileName
	 *            如果为null, 直接返回false
	 * @return
	 */
	public static boolean isFileOrDirectoryExist(String fileName) {
		if (fileName == null || fileName.trim().equals("")) {
			return false;
		}
		
		File file = getFile(fileName);
		if (file.exists()) {// 没有文件，则返回空
			return true;
		} else {
			return false;
		}
	}
	/**
	 * 判断文件是否存在，并且不是文件夹，给的是绝对路径
	 * 
	 * @param fileName
	 *            如果为null, 直接返回false
	 * @return
	 */
	public static boolean isFileOrDirectoryExist(File file) {
		return file.exists();
	}
	
	/**
	 * 判断文件是否存在，并且不是文件夹，给的是绝对路径
	 * 
	 * @param fileName
	 *            如果为null, 直接返回false
	 * @return
	 */
	public static boolean isFileExist(File file) {
		if (file.exists() && !file.isDirectory()) {// 没有文件，则返回空
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isFileExistAndBigThan0(String fileName) {
		return isFileExistAndBigThanSize(fileName, 0);
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
		if (fileName == null || fileName.trim().equals("")) {
			return false;
		}
		File file = getFile(fileName);
		if (file.exists() && !file.isDirectory()) {// 没有文件，则返回空
			if(FileOperate.getFileSizeLong(file) > size) {
				return true;
			}
		} else {
			return false;
		}
		return false;
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
	 * @param filePath
	 * @param realSize
	 * @return
	 */
	public static boolean isFileExistAndLossless(String filePath,long realSize) {
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
		if (fileName == null) {
			return false;
		}
		File file = getFile(fileName);
		if (file.isDirectory()) {// 没有文件，则返回空
			return true;
		} else {
			return false;
		}
	}
	/**
	 * 判断文件是否为文件夹,null直接返回false
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean isFileDirectory(File file) {
		if (file == null) {
			return false;
		}
		if (file.isDirectory()) {// 没有文件，则返回空
			return true;
		} else {
			return false;
		}
	}
	public static boolean isFileFoldExist(String fileName) {
		if (fileName == null) {
			return false;
		}
		File file = getFile(fileName);
		if (file.exists() || file.isDirectory()) {// 没有文件，则返回空
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 删除目录（文件夹）以及目录下的文件
	 * 
	 * @param sPath
	 *            被删除目录的文件路径，最后无所谓加不加"/"
	 * @return 目录删除成功返回true，否则返回false
	 */
	private static boolean deleteDirectory(File dirFile) {
		if (dirFile instanceof FileHadoop) {
			return dirFile.delete();
		}
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		boolean flag = true;
		// 删除文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// 删除子文件
			if (files[i].isFile()) {
				flag = files[i].delete();;
				if (!flag)
					break;
			} // 删除子目录
			else {
				flag = deleteDirectory(files[i]);
				if (!flag)
					break;
			}
		}
		if (!flag)
			return false;
		// 删除当前目录
		if (dirFile.delete()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 根据路径删除指定的目录或文件，无论存在与否
	 * 
	 * @param sPath
	 *            要删除的目录或文件
	 * @return 删除成功返回 true，否则返回 false
	 * 不存在文件也返回true
	 */
	public static boolean DeleteFileFolder(String sPath) {
		if (StringOperate.isRealNull(sPath)) {
			return true;
		}
		File file = getFile(sPath);
		// 判断目录或文件是否存在
		if (file.exists()) {
			if (file.isDirectory()) {
				return deleteDirectory(file);
			} else {
				return file.delete();
			}
		}
		return true;
	}
	/**
	 * 根据路径删除指定的目录或文件，无论存在与否
	 * 
	 * @param sPath
	 *            要删除的目录或文件
	 * @return 删除成功返回 true，否则返回 false
	 * 不存在文件也返回true
	 */
	public static boolean DeleteFileFolder(File file) {
		// 判断目录或文件是否存在
		if (file.exists()) {
			if (file.isDirectory()) {
				return deleteDirectory(file);
			} else {
				return file.delete();
			}
		}
		return true;
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
			if (path.equals("")) {
	            	path = ".";
            }
			path = path + File.separator;
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
	 * @param inputStream
	 * @param outPath 输出文件名，不会修改该输出文件名
	 * @param isOutputGzip 是否将输出流进行压缩
	 * @param uploadFileSize 上传文件的大小，小于等于0表示不考虑
	 * @return
	 * @throws IOException 抛出异常，但并不删除已上传的文件
	 */
	public static long uploadFile(InputStream inputStream, String outPath, boolean isOutputGzip, long uploadFileSize) throws IOException {
		if (StringOperate.isRealNull(outPath)) {
			return 0;
		}
		
		OutputStream os = FileOperate.getOutputStream(outPath);
		if (isOutputGzip) {
			os =  new GZIPOutputStream(os, TxtReadandWrite.bufferLen);
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
	 * 关闭输入流
	 * @date 2015年11月24日
	 * @param is
	 */
	public static void closeIs(InputStream is){
		try {
			if (is != null) {
				is.close();
			}
		} catch (Exception e) {
		}
	}
	
	/**
	 * 关闭输出流
	 * @date 2015年11月24日
	 * @param os
	 */
	public static void closeOs(OutputStream os){
		try {
			if (os != null) {
				os.close();
			}
		} catch (Exception e) {
		}
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
	
}
