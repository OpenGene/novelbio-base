package com.novelbio.base.fileOperate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.omg.CosNaming._BindingIteratorImplBase;

import com.novelbio.analysis.tools.compare.runCompSimple;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataStructure.PatternOperate;

public class FileOperate {
	public static void main(String[] args) {
		ArrayList<String> lsFileName = FileOperate.getFoldFileNameLs("/media/winF/NBC/Project/MethyArray_QZL110907/QZL_Fifth/FastQ/result/",
				"mir.+All", "txt");
		for (String string : lsFileName) {
			System.out.println(string);
		}
	}
	private static Logger logger = Logger.getLogger(FileOperate.class);

	/**
	 * 读取文本文件内容
	 * 
	 * @param filePathAndName
	 *            带有完整绝对路径的文件名
	 * @param encoding
	 *            文本文件打开的编码方式
	 * @return 返回文本文件的内容
	 */
	public String readTxt(String filePathAndName, String encoding)
			throws IOException {
		encoding = encoding.trim();
		StringBuffer str = new StringBuffer("");
		String st = "";
		try {
			FileInputStream fs = new FileInputStream(filePathAndName);
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

	/** 文件尾巴加个 "/" */
	public static String getSepPath() {
		return File.separator;
	}

	/**
	 * 给定路径名，返回其上一层路径，带"/" 如给定 /wer/fw4e/sr/frw/s3er.txt 返回 /wer/fw4e/sr/frw/<br>
	 * 如果为相对路径的最上层，譬如给定的是soap 则返回“” 可以给定不存在的路径
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getParentPathName(String fileName) {
		File file = new File(fileName);
		String fileParent = file.getParent();
		if (fileParent == null) {
			return "";
		} else {
			return addSep(fileParent);
		}
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

	/**
	 * 给定路径名，返回其名字 如给定/home/zong0jie/和/home/zong0jie 都返回zong0jie 可以给定不存在的路径
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getFileName(String fileName) {
		File file = new File(fileName);
		return file.getName();
	}

	/**
	 * 返回以K为单位的估计文件的总和，压缩文件就会加倍估计<br>
	 * 不准，只是估计而已
	 * 
	 * @return
	 */
	public static double getFileSizeEvaluateK(Collection<String> colFileName) {
		double allFileSize = 0;
		for (String fileName : colFileName) {
			double size = FileOperate.getFileSize(fileName);
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
	 * <b>未经测试</b> 给定文件路径，返回大小，单位为K
	 * 
	 * @param filePath
	 * @return 没有文件返回0；出错返回-1000000000
	 */
	public static double getFileSize(String filePath) {
		File file = new File(filePath);
		double totalsize = 0;
		if (!file.exists()) {
			return 0;
		}
		if (file.isFile()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				double size = (double) fis.available() / 1024;
				return size;
			} catch (Exception e1) {
				logger.error("IO出错！");
				return -1000000000;
			}
		} else if (file.isDirectory()) {
			ArrayList<String[]> lsFileName = getFoldFileName(filePath);

			for (String[] strings : lsFileName) {
				String fileName = null;
				// 获得文件名
				if (strings[1].equals("")) {
					fileName = addSep(filePath) + strings[0];
				} else {
					fileName = addSep(filePath) + strings[0] + "." + strings[1];
				}
				totalsize = totalsize + getFileSize(fileName);
			}
			return totalsize;
		} else {
			logger.error("出错！");
			return -1000000000;
		}
	}

	/**
	 * <b>未经测试</b> 给定文件路径，返回大小，单位为K
	 * 
	 * @param filePath
	 * @return 没有文件返回0；出错返回-1000000000
	 */
	public static long getFileSizeLong(String filePath) {
		File file = new File(filePath);
		long totalsize = 0;
		if (!file.exists()) {
			return 0;
		}
		if (file.isFile()) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				return fis.available();
			} catch (Exception e1) {
				logger.error("IO出错！");
				return -1000000000;
			}
		} else if (file.isDirectory()) {
			ArrayList<String[]> lsFileName = getFoldFileName(filePath);

			for (String[] strings : lsFileName) {
				String fileName = null;
				// 获得文件名
				if (strings[1].equals("")) {
					fileName = addSep(filePath) + strings[0];
				} else {
					fileName = addSep(filePath) + strings[0] + "." + strings[1];
				}
				totalsize = totalsize + getFileSizeLong(fileName);
			}
			return totalsize;
		} else {
			logger.error("出错！");
			return -1000000000;
		}
	}

	/**
	 * 给定路径名，返回其名字,不带后缀名<br>
	 * 如给定/home/zong0jie.aa.txt/和/home/zong0jie.aa.txt<br>
	 * 都返回zong0jie.aa 和 txt<br>
	 * 可以给定不存在的路径<br>
	 * 
	 * @param fileName
	 * @return string[2] 0:文件名 1:文件后缀
	 */
	public static String[] getFileNameSep(String fileName) {
		String[] result = new String[2];

		File file = new File(fileName);
		String filename = file.getName();
		int endDot = filename.lastIndexOf(".");
		if (endDot > 0) {
			result[0] = (String) filename.subSequence(0, endDot);
			result[1] = (String) filename.subSequence(endDot + 1,
					filename.length());
		} else {
			result[0] = filename;
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
	 */
	public static ArrayList<String[]> getFoldFileName(String filePath) {
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
	 */
	public static ArrayList<String[]> getFoldFileName(String filePath,
			String filename, String suffix) {
		ArrayList<String> lsFileName = getFoldFileNameLs(filePath, filename,
				suffix);
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (String fileName : lsFileName) {
			String[] fileNameSep = getFileNameSep(fileName);
			lsResult.add(fileNameSep);
		}
		return lsResult;
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
	 * @return 返回包含目标文件全名的ArrayList
	 */
	public static ArrayList<String> getFoldFileNameLs(String filePath, String filename, String suffix) {
		filePath = removeSep(filePath);
		if (filename == null || filename.equals("*")) {
			filename = ".*";
		}
		if (suffix.equals("*")) {
			suffix = ".*";
		}
		// ================================================================//
		ArrayList<String> ListFilename = new ArrayList<String>();
		File file = new File(filePath);
		if (!file.exists()) {// 没有文件，则返回空
			return null;
		}
		// 开始判断
		PatternOperate patName = new PatternOperate(filename, false);
		// 开始判断
		PatternOperate patSuffix = new PatternOperate(suffix, false);
		
		// 如果只是文件则返回文件名
		if (!file.isDirectory()) { // 获取文件名与后缀名
			String fileName = file.getName();
			if (isNeedFile(patName, patSuffix, fileName, filename, suffix)) {
				ListFilename.add(fileName);
				return ListFilename;
			}
		}
		// 如果是文件夹
		String[] filenameraw = file.list();
		if (filenameraw.length == 0) {
			System.out.println("stop");
		}
		for (int i = 0; i < filenameraw.length; i++) {
			if (isNeedFile(patName, patSuffix, filenameraw[i], filename, suffix)) {
				ListFilename.add(addSep(filePath) + filenameraw[i]);
			}
		}
		return ListFilename;
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
		if (patName.getPatFirst(fileNameSep[0]) != null && patSuffix.getPatFirst(fileNameSep[1]) != null) {
			return true;
		}
		return false;
	}

	/**
	 * 新建目录,如果新文件夹存在也返回ture
	 * 
	 * @param folderPath
	 *            目录路径,最后不要加\\或/
	 * @return 返回目录创建后的路径
	 */
	private static boolean createFolder(String folderPath) {
		File myFilePath = new File(folderPath);
		if (!myFilePath.exists()) {
			return myFilePath.mkdir();
		}
		return true;
	}

	/**
	 * 多级目录创建
	 * 
	 * @param folderPath
	 *            准备要在本级目录下创建新目录的目录路径 例如 c:myf
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
			if (isFileDirectory(foldUpper)) {
				flag = false;
				break;
			}
			creatPath = getFileName(foldUpper) + File.separator + creatPath;
			foldUpper = getParentPathName(foldUpper);
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
	 * 新建文件
	 * 
	 * @param filePathAndName
	 *            文本文件完整绝对路径及文件名
	 * @param fileContent
	 *            文本文件内容
	 * @return
	 */
	public static void createFile(String filePathAndName, String fileContent) {

		try {
			String filePath = filePathAndName;
			filePath = filePath.toString();
			File myFilePath = new File(filePath);
			if (!myFilePath.exists()) {
				myFilePath.createNewFile();
			}
			FileWriter resultFile = new FileWriter(myFilePath);
			PrintWriter myFile = new PrintWriter(resultFile);
			String strContent = fileContent;
			myFile.println(strContent);
			myFile.close();
			resultFile.close();
		} catch (Exception e) {
			logger.error("创建文件操作出错");
		}
	}

	/**
	 * 有编码方式的文件创建
	 * 
	 * @param filePathAndName
	 *            文本文件完整绝对路径及文件名
	 * @param fileContent
	 *            文本文件内容
	 * @param encoding
	 *            编码方式 例如 GBK 或者 UTF-8
	 * @return
	 */
	public static void createFile(String filePathAndName, String fileContent,
			String encoding) {

		try {
			String filePath = filePathAndName;
			filePath = filePath.toString();
			File myFilePath = new File(filePath);
			if (!myFilePath.exists()) {
				myFilePath.createNewFile();
			}
			PrintWriter myFile = new PrintWriter(myFilePath, encoding);
			String strContent = fileContent;
			myFile.println(strContent);
			myFile.close();
		} catch (Exception e) {
			logger.error("创建文件操作出错");
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
			File myDelFile = new File(filePath);
			if (myDelFile.exists()) {
				myDelFile.delete();
				bea = true;
			} else {
				bea = false;
				// message = (filePathAndName+"删除文件操作出错");
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
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
		try {
			delAllFile(folderPath); // 删除完里面所有内容
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			myFilePath.delete(); // 删除空文件夹
		} catch (Exception e) {
			logger.error("删除文件夹操作出错");
		}
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
		File file = new File(path);
		if (!file.exists()) {
			return bea;
		}
		if (!file.isDirectory()) {
			return bea;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			temp = new File(path + tempList[i]);
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + tempList[i]);// 先删除文件夹里面的文件
				delFolder(path + tempList[i]);// 再删除空文件夹
				bea = true;
			}
		}
		return bea;
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
		if (oldPathFile != null && oldPathFile.equals(newPathFile)) {
			return true;
		}
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(oldPathFile);
			File newfile = new File(newPathFile);

			if (oldfile.exists()) { // 文件存在时
				if (newfile.exists()) {
					if (!cover) {
						return false;
					}
					newfile.delete();
				}
				InputStream inStream = new FileInputStream(oldPathFile); // 读入原文件
				FileOutputStream fs = new FileOutputStream(newPathFile);
				byte[] buffer = new byte[1444];
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // 字节数 文件大小
					// System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("复制单个文件操作出错");
			return false;
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
	public static void copyFolder(String oldPath, String newPath, boolean cover) {
		newPath = addSep(newPath);
		oldPath = addSep(oldPath);
		try {
			new File(newPath).mkdirs(); // 如果文件夹不存在 则建立新文件夹
			File a = new File(oldPath);
			String[] file = a.list();
			File temp = null;
			for (int i = 0; i < file.length; i++) {
				temp = new File(oldPath + file[i]);
				if (temp.isFile()) { // 如果目标文件夹已经存在文件，则跳过
					File targetfile = new File(newPath
							+ (temp.getName()).toString());
					if (targetfile.exists()) {
						if (!cover) {
							continue;
						}
						targetfile.delete();
					}
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath
							+ (temp.getName()).toString());
					byte[] b = new byte[1024 * 5];
					int len;
					while ((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory()) {// 如果是子文件夹
					copyFolder(oldPath + "/" + file[i],
							newPath + "/" + file[i], cover);
				}
			}
		} catch (Exception e) {
			logger.error("复制整个文件夹内容操作出错");
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
	public static void changeFileName(String oldName, String newName) {
		changeFileName(oldName, newName, false);
	}

	/**
	 * 只修输入的文件名，并不直接操作文件 文件添加<b>前缀</b>并改后缀名，如果一样则不修改
	 * 
	 * @param FileName
	 *            原来文件的全名
	 * @param append
	 *            要添加的后缀，譬如_1，_new，如果为null，则不添加
	 * @param suffix
	 *            要添加的后缀名，譬如 txt， jpg ，自动去空格 suffix == null则不改变后缀名，suffix = ""
	 *            则去除后缀名
	 */
	public static String changeFilePrefix(String FileName, String append,
			String suffix) {
		String resultFile = "";
		if (append == null) {
			append = "";
		}
		String parentPath = addSep(getParentPathName(FileName));
		String[] fileName = getFileNameSep(FileName);
		resultFile = parentPath + append + fileName[0];
		if (suffix == null) {
			if (fileName[1] == null || fileName[1].equals("")) {
				return resultFile;
			} else {
				return resultFile + "." + fileName[1];
			}
		} else if (suffix.trim().equals("")) {
			return resultFile;
		} else {
			return resultFile + "." + suffix;
		}
	}

	/**
	 * 直接操作文件 文件添加<b>前缀</b>并改后缀名，如果一样则不修改
	 * 
	 * @param FileName
	 *            原来文件的全名
	 * @param append
	 *            要添加的后缀，譬如_1，_new，如果为null，则不添加
	 * @param suffix
	 *            要添加的后缀名，譬如 txt， jpg ，自动去空格 suffix == null则不改变后缀名，suffix = ""
	 *            则去除后缀名
	 */
	public static String changeFilePrefixReal(String FileName, String append,
			String suffix) {
		String newFile = changeFilePrefix(FileName, append, suffix);
		moveSingleFile(FileName, getParentPathName(newFile),
				getFileName(newFile), true);
		return newFile;
	}

	/**
	 * 只修输入的文件名，并不直接操作文件 文件添加<b>后缀</b>并改后缀名，如果一样则不修改
	 * 
	 * @param FileName
	 *            原来文件的全名
	 * @param append
	 *            要添加的后缀，譬如_1，_new，如果为null，则不添加
	 * @param suffix
	 *            要添加的后缀名，譬如 txt， jpg ，自动去空格 suffix == null则不改变后缀名，suffix = ""
	 *            则去除后缀名
	 */
	public static String changeFileSuffix(String FileName, String append,
			String suffix) {
		String resultFile = "";
		if (append == null) {
			append = "";
		}
		String parentPath = addSep(getParentPathName(FileName));
		String[] fileName = getFileNameSep(FileName);
		resultFile = parentPath + fileName[0] + append;
		if (suffix == null) {
			if (fileName[1] == null || fileName[1].equals("")) {
				return resultFile;
			} else {
				return resultFile + "." + fileName[1];
			}
		} else if (suffix.trim().equals("")) {
			return resultFile;
		} else {
			return resultFile + "." + suffix;
		}
	}

	/**
	 * 直接操作文件 文件添加<b>后缀</b>并改后缀名，如果一样则不修改
	 * 
	 * @param FileName
	 *            原来文件的全名
	 * @param append
	 *            要添加的后缀，譬如_1，_new，如果为null，则不添加
	 * @param suffix
	 *            要添加的后缀名，譬如 txt， jpg ，自动去空格 suffix == null则不改变后缀名，suffix = ""
	 *            则去除后缀名
	 */
	public static String changeFileSuffixReal(String FileName, String append,
			String suffix) {
		String newFile = changeFileSuffix(FileName, append, suffix);
		moveSingleFile(FileName, getParentPathName(newFile),
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
		File oldFile = new File(oldName);
		// 文件新（目标）地址
		File fnew = new File(oldFile.getParentFile() + File.separator + newName);
		if (oldFile.getAbsolutePath().equals(fnew.getAbsolutePath())) {
			return true;
		}
		if (fnew.exists() && !cover) {
			return false;
		} else {
			fnew.delete();
		}
		fnew = new File(oldFile.getParentFile() + File.separator + newName);
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
	 * @param oldPath
	 * @param newPath
	 * @param NewName
	 *            新文件或文件夹名
	 * @param cover
	 * @return
	 */
	public static boolean moveFile(boolean cover, String oldFileName, String newFileName) {
		String newPath = FileOperate.getParentPathName(newFileName);
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
		String newPathName = newPath + NewName;
		if (oldFileName.equals(newPathName)) {
			return true;
		}
		
		if (isFileExist(oldFileName)) {
			okFlag = moveSingleFile(oldFileName, newPath, NewName, cover);
		} else if (isFileDirectory(oldFileName)) {
			newPath = newPath + NewName;
			okFlag = moveFoldFile(oldFileName, newPath, "", cover);
		}
		deleteDirectory(oldFileName);
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
		deleteDirectory(oldFilePath);
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
		File oldFile = new File(oldFileName);
		// 文件新（目标）地址
		// new一个新文件夹
		File fnewpath = new File(newPath);
		if (!oldFile.exists()) {
			return false;
		}
		// 判断文件夹是否存在
		if (!fnewpath.exists())
			fnewpath.mkdirs();// 创建新文件
		// 将文件移到新文件里
		File fnew = new File(newPath + newName);
		if (fnew.exists()) {
			if (!cover) {
				return false;
			}
			fnew.delete();
		}
		if (!oldFile.renameTo(fnew)) {
			if (copyFile(oldFileName, newPath + newName, cover)) {
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

		boolean ok = true;
		File olddir = new File(oldfolderfile);
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
				ok = moveFoldFile(files[i].getPath(),
						newfolderfile + files[i].getName(), prix, cover);
				// 成功，删除原文件
				if (ok) {
					files[i].delete();
				}
				continue;
			}
			File fnew = new File(newfolderfile + prix + files[i].getName());
			// 目标文件夹下存在的话，不变
			if (fnew.exists()) {
				if (!cover) {
					ok = false;
					continue;
				}
				fnew.delete();
			}
			if (!files[i].renameTo(fnew)) {
				if (copyFile(files[i].getAbsolutePath(),
						fnew.getAbsolutePath(), cover))
					files[i].delete();
				else {
					ok = false;
				}
			}
		}
		return ok;
	}

	/**
	 * 创建快捷方式，目前只能在linux下使用 内部会根据linkTo的路径自动创建文件夹
	 * 
	 * @param rawFile
	 * @param linkTo
	 * @param cover 是否覆盖
	 * @return 返回是否创建成功
	 */
	public static boolean linkFile(String rawFile, String linkTo, boolean cover) {
		if (!FileOperate.isFileExist(rawFile)) {
			return false;
		}
		if (!FileOperate.createFolders(FileOperate.getParentPathName(linkTo))) {
			return false;
		}
		if (FileOperate.isFileExist(linkTo) && cover) {
			FileOperate.delFile(linkTo);
		}
		String cmd = "ln -s " + CmdOperate.addQuot(rawFile) + " "
				+ CmdOperate.addQuot(linkTo);
		CmdOperate cmdOperate = new CmdOperate(cmd, "lnSeq");
		cmdOperate.run();
		return true;
	}

	/**
	 * 判断文件是否存在，并且不是文件夹，给的是绝对路径
	 * 
	 * @param fileName
	 *            如果为null, 直接返回false
	 * @return
	 */
	public static boolean isFileExist(String fileName) {
		if (fileName == null) {
			return false;
		}
		File file = new File(fileName);
		if (file.exists() && !file.isDirectory()) {// 没有文件，则返回空
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断文件是否存在，并且有一定的大小而不是空文件
	 * 
	 * @param fileName
	 *            如果为null, 直接返回false
	 * @param size
	 *            大小 K为单位
	 * @return
	 */
	public static boolean isFileExistAndBigThanSize(String fileName, double size) {
		if (isFileExist(fileName) && getFileSize(fileName) >= size) {
			return true;
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
		File file = new File(fileName);
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
		File file = new File(fileName);
		if (file.exists() || file.isDirectory()) {// 没有文件，则返回空
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 删除单个文件
	 * 
	 * @param sPath
	 *            被删除文件的文件名
	 * @return 单个文件删除成功返回true，否则返回false
	 */
	private static boolean deleteFile(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}

	/**
	 * 删除目录（文件夹）以及目录下的文件
	 * 
	 * @param sPath
	 *            被删除目录的文件路径，最后无所谓加不加"/"
	 * @return 目录删除成功返回true，否则返回false
	 */
	private static boolean deleteDirectory(String sPath) {
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		sPath = addSep(sPath);
		File dirFile = new File(sPath);
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
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			} // 删除子目录
			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
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
	 * @return 删除成功返回 true，否则返回 false。
	 */
	public static boolean DeleteFileFolder(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 判断目录或文件是否存在
		if (!file.exists()) { // 不存在返回 false
			return flag;
		} else {
			// 判断是否为文件
			if (file.isFile()) { // 为文件时调用删除文件方法
				return deleteFile(sPath);
			} else { // 为目录时调用删除目录方法
				return deleteDirectory(sPath);
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
			path = path + File.separator;
		}
		return path;
	}

	/**
	 * 删除文件分割符
	 * 
	 * @param path
	 * @return
	 */
	public static String removeSep(String path) {
		path = path.trim();
		if (path.endsWith(File.separator)) {
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}
}
