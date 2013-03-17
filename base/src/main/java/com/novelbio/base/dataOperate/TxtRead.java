package com.novelbio.base.dataOperate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;

public class TxtRead {
	private static final Logger logger = Logger.getLogger(TxtReadandWrite.class);
	public final static String ENTER_LINUX = "\n";
	public final static String ENTER_WINDOWS = "\r\n";
	/** 缓冲 */
	static int bufferLen = 100000;
	/** 默认以制表符分割 */
	static String sep = "\t";
	
	public static enum System{
		pc, hadoop
	}
	
	public static enum TXTtype{
		Gzip, Bzip2, Zip, Txt
	}
	
	String txtfile;
	BufferedInputStream inputStream;
	BufferedReader bufread;
	
	/** 抓取文件中特殊的信息 */
	String grepContent = "";
	
	/**
	 * 设定缓冲长度，默认为10000
	 * @param bufferLen
	 */
	public static void setBufferLen(int bufferLen) {
		TxtReadandWrite.bufferLen = bufferLen;
	}
	
	public String getFileName() {
		return txtfile;
	}
	
	public TxtRead(String fileName) {
		this.txtfile = fileName;
		InputStream inStream = null;
		try {
			inStream = new FileInputStream(txtfile);
			setInStream(getTxtType(fileName), inStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public TxtRead(FileHadoop fileHadoop) {
		InputStream inputStream = fileHadoop.getInputStream();
		try {
			setInStream(getTxtType(fileHadoop.getFileNameHdfs()), inputStream);
		} catch (IOException e) {e.printStackTrace();}
	}
	
	/**
	 * 根据文件后缀判断文件的类型，是gz还是txt等
	 * @return
	 */
	private TXTtype getTxtType(String fileName) {
		TXTtype txtTtype = null;
		fileName = fileName.toLowerCase().trim();
		if (fileName.endsWith(".gz")) {
			txtTtype = TXTtype.Gzip;
		} else if (fileName.endsWith(".bz2")) {
			txtTtype = TXTtype.Bzip2;
		} else if (fileName.endsWith("zip")) {
			txtTtype = TXTtype.Zip;
		} else {
			txtTtype = TXTtype.Txt;
		}
		return txtTtype;
	}
	
	private void setInStream(TXTtype txtType, InputStream inputStreamRaw) throws IOException {
		if (txtType == TXTtype.Txt) {
			inputStream = new BufferedInputStream(inputStreamRaw, bufferLen);
		} else if (txtType == TXTtype.Zip) {
			ZipArchiveInputStream zipArchiveInputStream = new ZipArchiveInputStream(inputStreamRaw);
			ArchiveEntry zipEntry = null;
			while ((zipEntry = zipArchiveInputStream.getNextEntry()) != null) {
				if (!zipEntry.isDirectory() && zipEntry.getSize() > 0) {
					break;
				}
			}
			inputStream = new BufferedInputStream(zipArchiveInputStream, bufferLen);
		} else if (txtType == TXTtype.Gzip) {
			inputStream = new BufferedInputStream(new GZIPInputStream(inputStreamRaw, bufferLen), bufferLen);
		} else if (txtType == TXTtype.Bzip2) {
			inputStream = new BufferedInputStream(new BZip2CompressorInputStream(inputStreamRaw), bufferLen);
		}
	}
	
	/**
	 * 返回该文本的回车方式
	 * @return
	 */
	public String getEnterType() {
		String result = "";
		try {
			result = getEnterTypeExp();
		} catch (Exception e) {
			result = ENTER_LINUX;
		}
		close();
		return result;
	}
	
	private String getEnterTypeExp() throws Exception {
		int firstLineNum = readFirstLine().getBytes().length;
		//获得第一行的btye长度
		byte[] mybyte = new byte[firstLineNum + 2];
		initialReading();
		inputStream.read(mybyte);
		Charset cs = Charset.forName("UTF-8");
		ByteBuffer bb = ByteBuffer.allocate (mybyte.length);
		bb.put(mybyte);
		bb.flip();
		CharBuffer cb = cs.decode (bb);
	  
		char[] mychar = cb.array();
		if (mychar[mychar.length - 2] == 13 && mychar[mychar.length - 1] == 10) {
			return ENTER_WINDOWS;
		} else {
			return ENTER_LINUX;
		}
	}
	
	/**
	 * 这个内部使用，外部用@readlines代替
	 * 有时间改成private方法
	 * @param path输入文件名
	 * @return 返回BufferedReader，记得读完后要关闭Buffer流
	 * @throws Exception
	 */
	@Deprecated
	public BufferedReader readfile() throws Exception {
		initialReading();
		bufread = new BufferedReader(new   InputStreamReader(inputStream));
		return bufread;
	}
	/**
	 * 初始化读取文本
	 * @throws Exception
	 */
	private void initialReading() throws Exception {
		if (inputStream != null) {
			inputStream.close();
		}
		if (bufread != null) {
			bufread.close();
		}
		setReadFile(filetype);
	}
	
	private void setReadFile(String fileType) throws Exception {
		inputStream = new BufferedInputStream(new FileInputStream(txtfile), bufferLen);
		if (fileType.equals(TXT)) {
			return;
		}
		if (fileType.equals(ZIP)) {
			ZipArchiveInputStream zipArchiveInputStream = new ZipArchiveInputStream(inputStream);
			ArchiveEntry zipEntry = null;
			while ((zipEntry = zipArchiveInputStream.getNextEntry()) != null) {
				if (!zipEntry.isDirectory() && zipEntry.getSize() > 0) {
					break;
				}
			}
			inputStream = new BufferedInputStream(zipArchiveInputStream, bufferLen);
		}
		else if (fileType.equals(GZIP))
			inputStream = new BufferedInputStream(new GZIPInputStream(inputStream, bufferLen), bufferLen);
		else if (fileType.equals(BZIP2))
			inputStream = new BufferedInputStream(new BZip2CompressorInputStream(inputStream), bufferLen);
	}
	
	public Iterable<String> readlines() {
		try {
			return readPerlines();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 从第几行开始读，是实际行
	 * @param lines 如果lines小于1，则从头开始读取
	 * @return
	 */
	public Iterable<String> readlines(int lines) {
		lines = lines - 1;
		try {
			Iterable<String> itContent = readPerlines();
			if (lines > 0) {
				for (int i = 0; i < lines; i++) {
					itContent.iterator().hasNext();
				}
			}
			return itContent;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 迭代读取文件
	 * @param filename
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 */
	private Iterable<String> readPerlines() throws Exception {
		 final BufferedReader bufread =  readfile(); 
		return new Iterable<String>() {
			public Iterator<String> iterator() {
				return new Iterator<String>() {
					public boolean hasNext() {
						return line != null;
					}
					public String next() {
						String retval = line;
						line = getLine();
						return retval;
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
					String getLine() {
						String line = null;
						try {
							line = bufread.readLine();
						} catch (IOException ioEx) {
							line = null;
						}
						if (line == null) {
							close();
						}
						return line;
					}
					String line = getLine();
				};
			}
		};
	}

	/**
	 * @param path输入文件名
	 * @return 返回List<String>，读完关闭
	 * @throws Exception
	 */
	public ArrayList<String> readfileLs() {
		ArrayList<String> lsResult = new ArrayList<String>();
		for (String string : readlines()) {
			lsResult.add(string);
		}
		close();
		return lsResult;
	}
	
	/**
	 * 去除空格后文件的字符长度，不是文件大小，而是含有多少文字
	 * @return
	 */
	public long getTxtLen() {
		int Result = 0;
		for (String content : readlines()) {
			Result = Result + content.trim().length();
		}
		return Result;
	}

	/**
	 * @return 返回 String，读完不用关闭Buffer流
	 */
	public String readFirstLine() {
		String firstLine = "";
		try {
			firstLine = readlines().iterator().next();
		} catch (Exception e) { }
		return firstLine;
	}
	
	/**
	 * @param Num 读取前几列，实际列。如果文本没有那么多列，那么只读取所有列
	 * @return 返回 String，读完不用关闭Buffer流
	 * @throws Exception
	 */
	public ArrayList<String> readFirstLines(int Num) {
		ArrayList<String> lsResult = new ArrayList<String>();
		int rowNum = 1;
		for (String string : readlines()) {
			if (rowNum > Num ) {
				break;
			}
			lsResult.add(string);
			rowNum ++;
		}
		return lsResult;
	}
	/**
	 * 写入并换行，没有flush
	 * @param content
	 *            ，要写入文件内容
	 * @throws Exception
	 */
	public void flash() {
		try {
			outputStream.flush();
		} catch (Exception e) { }
	}
	/**
	 * 写完自动flush
	 * @param content 要写入文件内容
	 * @throws Exception
	 */
	public void writefile(String content) {
		writefile(content, true);
	}
	/**
	 * @param content
	 *            ，要写入文件内容,并考虑是否刷新--也就是直接写入文件而不是进入缓存
	 * @throws Exception
	 */
	public void writefile(String content, boolean flush) {
		try {
			outputStream.write(content.getBytes());
			if (flush) {
				outputStream.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 写入并换行，没有flush
	 * @param content
	 *            ，要写入文件内容
	 * @throws Exception
	 */
	public void writefileln(String content) {
		try {
			outputStream.write((content+ENTER_LINUX).getBytes());
		} catch (Exception e) { }
	}
	/**
	 * 写入一行数组并换行，用sep隔开
	 * @param content
	 *            ，要写入文件内容
	 * @throws Exception
	 */
	public void writefileln(String[] content) {
		String content2 = content[0];
		for (int i = 1; i < content.length; i++) {
			content2 = content2 + sep + content[i];
		}
		try {
			outputStream.write((content2+ENTER_LINUX).getBytes());
		} catch (Exception e) {
		}
	}
	
	/**
	 * 写入一行list，用sep隔开
	 * @param content
	 */
	public void writefileln(List<String> content) {
		String[] strings = new String[content.size()]; 
		for (int i = 0; i < content.size(); i++) {
			strings[i] =  content.get(i);
			}
		writefileln(strings);
	}
	
	/**
	 * 直接写入一个表格ArrayList<ArrayList<String>>;
	 * @param lsls
	 */
	public void writefileln(ArrayList<ArrayList<String>> lsls) {
		for (List<String> list : lsls) {
			writefileln(list);
		}
	}
	
	
	/**
	 * 写入并换行
	 * @param content 要写入文件内容
	 * @throws Exception
	 */
	public void writefileln() {
		try {
			outputStream.write(ENTER_LINUX.getBytes());
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	/**
	 * 写入并将写入的序列换行，目前只能写入ascII文本
	 * @param content 输入的string，是没有换行的那种
	 * @param length 每多少行进行换行
	 * @throws Exception
	 */
	public void writefilePerLine(String content, int length) {
		try {
			char[] mychar = content.toCharArray();
			for (int i = 0; i < mychar.length; i++) {
				if (i>0 && i%length == 0) {
					outputStream.write(ENTER_LINUX.getBytes());
				}
				outputStream.write(mychar[i]);
			}
			outputStream.flush();
		} catch (Exception e) {
		}
	}
	
	/**
	 * 指定正则表达式，将文本中含有该正则表达式的行全部删除
	 * @param regx
	 */
	public void delLines(String regx, boolean isregx) {
		String tmpFileName = txtfile.getAbsolutePath() + DateTime.getDateAndRandom();
		TxtReadandWrite txtNewFile = new TxtReadandWrite(tmpFileName,true);
		PatternOperate patternOperate = new PatternOperate(regx, false);
		for (String content : readlines()) {
			if (isregx) {
				String result = patternOperate.getPatFirst(content);
				if (result != null)
					continue;
			}
			else {
				if (content.contains(regx)) {
					continue;
				}
			}
			txtNewFile.writefileln(content);
		}
		txtNewFile.close();
		FileOperate.delFile(txtfile.getAbsolutePath());
		FileOperate.changeFileName(tmpFileName, FileOperate.getFileName(txtfile.getAbsolutePath()),true);	
		close();
	}
	/**
	 * 给定内容，写入文本，这个写入的东西可以给R语言用scan读取,默认每行20个元素，用空格隔开
	 * 
	 * @param content
	 */
	public void Rwritefile(double[] content) {
		try {
			Rwritefile(content, 20, " ");
		} catch (Exception e) { }
	}
	/**
	 * 给定内容，写入文本，这个写入的东西可以给R语言用scan读取,默认每行20个元素，用空格隔开
	 * 
	 * @param content
	 */
	public void Rwritefile(int[] content) {
		Rwritefile(content, 20, " ");
	}
	/**
	 * 给定内容，写入文本，这个写入的东西可以给R语言用scan读取,默认每行20个元素，用空格隔开
	 * 内部close
	 * @param content
	 */
	public void Rwritefile(String[] content) {
		Rwritefile(content, 20, " ");
	}
	/**
	 * 给定内容，写入文本，这个写入的东西可以给R语言用scan读取
	 * @param content
	 * @param colLen
	 * @param sep
	 * @throws Exception
	 */
	private void Rwritefile(int[] content, int colLen, String sep) {
		try {
			for (int i = 0; i < content.length; i++) {
				outputStream.write((content[i] + "" + sep).getBytes());
				if ((i + 1) % colLen == 0) {
					outputStream.write(ENTER_LINUX.getBytes());
				}
			}
			outputStream.flush();
		} catch (Exception e) {
			logger.error("file error: "+ getFileName());
		}
		close();
	}
	/**
	 * 给定内容，写入文本，这个写入的东西可以给R语言用scan读取
	 * 内部close
	 * @param content
	 * @param colLen
	 * @param sep
	 * @throws Exception
	 */
	private void Rwritefile(String[] content, int colLen, String sep) {
		try {
			for (int i = 0; i < content.length; i++) {
				outputStream.write((content[i] + "" + sep).getBytes());
				if ((i + 1) % colLen == 0) {
					outputStream.write(ENTER_LINUX.getBytes());
				}
			}

			outputStream.flush();
		} catch (Exception e) {
			logger.error("file error: "+getFileName());
		}
		close();
	}
	/**
	 * 给定内容，写入文本，这个写入的东西可以给R语言用scan读取
	 * @param content
	 * @param colLen 每行写几个
	 * @param sep 分隔符是什么
	 * @throws Exception
	 */
	private void Rwritefile(double[] content, int colLen, String sep) throws Exception {
		for (int i = 0; i < content.length; i++) {
			outputStream.write((content[i] + "" + sep).getBytes());
			if ((i + 1) % colLen == 0) {
				outputStream.write(ENTER_LINUX.getBytes());
			}
		}
		outputStream.flush();
		close();
	}
	/**
	 * @param lsContent-T 注意T只能是string interge等简单的能转化为string的类
	 *            ，要写入List--String文件内容,自动在每一行添加换行符huiche;
	 *            内部close 流
	 * @throws Exception
	 */
	public<T> void writefile(List<T> lsContent){
		try {
			for (int i = 0; i < lsContent.size(); i++) {
				outputStream.write(lsContent.get(i).toString().getBytes());
				outputStream.write(ENTER_LINUX.getBytes());
			}
			outputStream.flush();
		} catch (Exception e) {
			// TODO: handle exception
		}
		close();
	}

	/**
	 * 按照excel方法读取文本时使用，用于 获得txt文本的行数，如果最后一行是""，则忽略最后一行
	 * 
	 * @return
	 * @throws Exception
	 */
	public int ExcelRows() {
		try {
			int rowNum = 0;
			BufferedReader readasexcel = readfile();
			String content = "";
			String content2 = "";
			while ((content = readasexcel.readLine()) != null) {
				rowNum++;
				content2 = content;
			}
			
			if (content2.equals("")) {
				close();
				return rowNum - 1;
			}
			else {
				close();
				return rowNum;
			}
		} catch (Exception e) {
			logger.error("excelRows error: "+ getFileName());
			close();
			return -1;
		}
	}

	/**
	 * 按照excel方法读取文本时使用，用于 获得文本中前5000行中最长行的列数
	 * @param sep
	 *            该行的分隔符，为正则表达式，tab为"\t"
	 * @return 返回指定行的列数
	 * @throws Exception
	 */
	public int ExcelColumns(String sep){
		int colNum=0;

		int excelRows = 5000; int rowNum = 0;
		for (String tmpstr : readlines()) {
			if (rowNum > excelRows) {
				break;
			}
			rowNum++;
			int TmpColNum = tmpstr.split(sep).length;
			if (TmpColNum>colNum) {
				colNum=TmpColNum;
			}
		}
		return colNum;
	}

	/**
	 * 按照excel方法读取文本时使用，用于 获得txt文本指定行的列数
	 * 
	 * @param setRow
	 *            指定行数，为实际行数，如果指定行超过文本最大行，则将指定行设为最大行。
	 * @param sep
	 *            该行的分隔符，为正则表达式，tab为"\t"
	 * @return 返回指定行的列数
	 * @throws Exception
	 */
	public int ExcelColumns(int setRow, String sep) throws Exception {
		int excelRows = ExcelRows();
		if (setRow > excelRows) {
			setRow = excelRows;
		}
		BufferedReader readasexcel = readfile();
		for (int i = 0; i < setRow - 1; i++) {
			readasexcel.readLine();
		}
		String tmpstr = readasexcel.readLine();
		String[] tmp = tmpstr.split(sep);
		return tmp.length;
	}

	/**
	 * 将规则的txt文本按照excel的方法读取
	 * 
	 * @param sep
	 *            txt文本的分割符
	 * @param rowNum
	 *            实际读取行
	 * @param columnNum
	 *            实际读取列
	 * @return 返回string,单个值,如果值为null则返回""
	 * @throws Exception
	 */
	public String ExcelRead(String sep, int rowNum, int columnNum)
			throws Exception {
		BufferedReader readasexcel = readfile();
		// 先跳过前面的好多行
		for (int i = 0; i < rowNum - 1; i++) {
			if (readasexcel.readLine() == null) {
				return "";
			}
		}
		// 正式读取
		String content = "";
		String[] tmp;// 两个临时变量
		content = readasexcel.readLine();
		tmp = content.split(sep);
		
		close();
		
		if (tmp.length < columnNum)
			return "";
		
		return tmp[columnNum - 1];
	}

	/**
	 * 将规则的txt文本按照excel的方法读取
	 * 最后一行即使没东西也会用""表示
	 * @param sep
	 *            txt文本的分割符,为正则表达式，tab是"\t"
	 * @param rowStartNum
	 *            实际读取起始行
	 * @param columnStartNum
	 *            实际读取起始列
	 * @param rowEndNum
	 *            实际读取终止行
	 * @param columnEndNum
	 *            实际读取终止列
	 * @return 返回string[] 数组,数组中null项用""替换
	 * @throws Exception
	 */
	public String[][] ExcelRead(String sep, int rowStartNum,
			int columnStartNum, int rowEndNum, int columnEndNum)
			throws Exception {
		if (rowEndNum <= 0) {
			rowEndNum = ExcelRows();
		}
		if (rowStartNum < 0) {
			rowStartNum = 1;
		}
		int readlines = rowEndNum - rowStartNum + 1;
		int readcolumns = columnEndNum - columnStartNum + 1;
		// System.out.println(readlines);
		// System.out.println(readcolumns);
		String[][] result = new String[readlines][readcolumns];
		BufferedReader readasexcel = readfile();

		// 先跳过前面的好多行
		for (int i = 0; i < rowStartNum - 1; i++) {
			if (readasexcel.readLine() == null)// 如果文本中没有那么多行
			{
				return null;
			}
		}
		// 正式读取
		String content = "";
		String[] tmp;// 两个临时变量
		for (int i = 0; i < readlines; i++) {
			if ((content = readasexcel.readLine()) == null)// 读完了
			{
				break;
			}
			tmp = content.split(sep);
			for (int j = 0; j < readcolumns; j++) {
				if (tmp.length >= columnStartNum + j) {
					result[i][j] = tmp[columnStartNum - 1 + j];
				}
			}
		}
		for (int i = 0; i < result.length; i++)// 将所有为null的项通通赋值为""
		{
			for (int j = 0; j < result[0].length; j++) {
				if (result[i][j] == null)
					result[i][j] = "";
			}
		}
		close();
		return result;
	}

	/**
	 * 内部close
	 * 将规则的txt文本按照excel的方法读取,自动跳过空行
	 * 最后一行为空行的话会保留
	 * @param sep
	 *            txt文本的分割符,为正则表达式，tab是"\t"
	 * @param rowStartNum
	 *            实际读取起始行
	 * @param columnStartNum
	 *            实际读取起始列
	 * @param rowEndNum 
	 *            实际读取终止行 ,当该项=-1时，读取所有行
	 * @param columnEndNum
	 *            实际读取终止列,当该项=-1时，读取所有列，反正是ArrayList--String[]嘛<br>
	 *            如果该项大于最大列，那么就把本行都读取了
	 * @param colNotNone
	 *            主键列，该列不能为""，否则把该列为""的行删除，如果本项<=0，则不考虑
	 * @return 返回ArrayList<String[]> 数组,数组中null项用""替换
	 * @throws Exception
	 */
	public ArrayList<String[]> ExcelRead(int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum, int colNotNone) {
		if (columnEndNum <= 0) {
			columnEndNum = ExcelColumns(sep);
		}
		int[] colRead = new int[columnEndNum - columnStartNum + 1];
		for (int i = 0; i < colRead.length; i++) {
			colRead[i] = columnStartNum + i;
		}
		return ExcelRead(rowStartNum, rowEndNum, colRead, colNotNone);
	}
	/**
	 * 内部close
	 * 将规则的txt文本按照excel的方法读取,自动跳过空行
	 * 最后一行为空行的话会保留
	 * @param sep txt文本的分割符,为正则表达式，tab是"\t"
	 * @param rowStartNum 实际读取起始行
	 * @param rowEndNum 实际读取终止行 ,当该项=-1时，读取所有行
	 * @param column 实际读取的列
	 * @param colNotNone 主键列，该列不能为""，否则把该列为""的行删除，如果本项<=0，则不考虑
	 * @return 返回ArrayList<String[]> 数组,数组中null项用""替换
	 * @throws Exception
	 */
	public ArrayList<String[]> ExcelRead(int rowStartNum, int rowEndNum, int[] column, int colNotNone) {
		colNotNone--;
		if (rowEndNum <= 0)
			rowEndNum = ExcelRows();
		
		ArrayList<String[]> result = new ArrayList<String[]>();
		int readlines = rowEndNum - rowStartNum + 1;
		int countRows = 1;
				
		for (String content : readlines(rowStartNum)) {
			if (rowEndNum > 0 && countRows > readlines)
				break;
			
			if (content.trim().equals("")) {
				continue;
			}
			String[] tmp = content.split(sep);
			if (colNotNone > 0 && (tmp.length < colNotNone + 1 || tmp[colNotNone] == null || tmp[colNotNone].trim().equals(""))) {
				continue;
			}
			
			column = ArrayOperate.removeSmallValue(column, 0);
			 
			 String[] tmpResult = new String[column.length];
			 for (int i = 0; i < tmpResult.length; i++) {
				 tmpResult[i] = "";
			 }
			 for (int i = 0; i < column.length; i++) {
				 int colNum = column[i] - 1;
				 if (tmp.length <= colNum || tmp[colNum] == null) {
					 tmpResult[i] = "";
				 } else {
					 tmpResult[i] = tmp[colNum];
				 }
			 }
			 result.add(tmpResult);
			 countRows ++;
		}
		close();
		return result;
	}
	/**
	 * 给定一个两列文件，将其中的结果按照Key-value导出
	 * 如果一列为空，如为很多空格，则跳过，如果有重复列，选择后出现的列
	 * @param chrLenFile
	 * @param keyCase key的大小写。 null 不改变大小写，false 小写，true大写
	 * @return
	 * 没东西则返回null
	 */
	public LinkedHashMap<String, String> getKey2Value(String sep, Boolean keyCase) {
		LinkedHashMap<String, String> lkhashResult = new LinkedHashMap<String, String>();
		ArrayList<String> lstmp = readfileLs();
		for (String string : lstmp) {
			if (string == null || string.trim().equals("")) {
				continue;
			}
			String[] ss = string.trim().split("\t");
			if (keyCase != null) {
				 ss[0] = keyCase == true ? ss[0].toUpperCase():ss[0].toLowerCase();  
			}
			if (ss.length < 2) {
				lkhashResult.put(ss[0], "");
			}
			else {
				lkhashResult.put(ss[0], ss[1]);
			}
		}
		if (lkhashResult.size() == 0) {
			return null;
		}
		return lkhashResult;
	}
	/**
	 * 给定一个两列文件，将其中的结果按照Key-value导出,value为double类型
	 * 如果一列为空，如为很多空格，则跳过，如果有重复列，选择后出现的列
	 * @param chrLenFile
	 * @param keyCase key的大小写。 null 不改变大小写，false 小写，true大写
	 * @return
	 * 没东西则返回null
	 */
	public LinkedHashMap<String, Double> getKey2ValueDouble(String sep, Boolean keyCase) {
		LinkedHashMap<String, Double> lkhashResult = new LinkedHashMap<String, Double>();
		ArrayList<String> lstmp = readfileLs();
		for (String string : lstmp) {
			if (string == null || string.trim().equals("")) {
				continue;
			}
			String[] ss = string.trim().split("\t");
			if (keyCase != null) {
				 ss[0] = keyCase == true ? ss[0].toUpperCase():ss[0].toLowerCase();  
			}
			if (ss.length < 2) {
				lkhashResult.put(ss[0], 0.0);
			}
			else {
				lkhashResult.put(ss[0], Double.parseDouble(ss[1]));
			}
		}
		if (lkhashResult.size() == 0) {
			return null;
		}
		return lkhashResult;
	}
	/**
	 * 将数据按照excel的方法写入string[][],null和""都不写入，最后写入一个换行
	 * 
	 * @param sep
	 *            txt文本的分割符,为正则表达式，tab是"\t"
	 * @throws Exception
	 */
	public<T> void ExcelWrite(T[][] content) throws Exception {
		String tmp = "";
		for (int i = 0; i < content.length; i++) {
			for (int j = 0; j < content[0].length; j++) {
				if (content[i][j] == null)
					tmp = "";
				else {
					tmp = content[i][j].toString();
				}
				if (j < (content[0].length - 1)) {
					outputStream.write((tmp + sep).getBytes());
				} else {
					outputStream.write(tmp.getBytes());
				}
			}
			outputStream.write(ENTER_LINUX.getBytes());// 换行
		}
		outputStream.flush();// 写入文本
	}

	/**
	 * 将数据按照excel的方法写入string[][],null和""都不写入，最后写入一个换行
	 * 
	 * @param sep
	 *            txt文本的分割符,为正则表达式，tab是"\t"
	 * @throws Exception
	 */
	public void ExcelWrite(String[][] content, int rowStart, int colStart) throws Exception {

		for (int i = 0; i < content.length; i++) {
			for (int j = 0; j < content[0].length; j++) {
				if (content[i][j] == null)
					content[i][j] = "";
				if (j < (content[0].length - 1)) {
					outputStream.write((content[i][j] + sep).getBytes());
				} else {
					outputStream.write(content[i][j].getBytes());
				}
			}
			outputStream.write(ENTER_LINUX.getBytes());// 换行
		}
		outputStream.flush();// 写入文本
	}

	/**
	 * 将数据按照excel的方法写入string[],null和""都不写入,最后写入一个换行
	 * 
	 * @param sep
	 *            txt文本的分割符,为正则表达式，tab是"\t"
	 * @param row
	 *            true时按行写入
	 * @throws Exception
	 */
	public void ExcelWrite(String[] content, boolean row) throws Exception {
		if (row == true)// 横着写入
		{
			for (int i = 0; i < content.length; i++) {
				if (content[i] == null)
					content[i] = "";
				if (i < (content.length - 1)) {
					outputStream.write((content[i] + sep).getBytes());
				} else {
					outputStream.write(content[i].getBytes());
				}
			}
			outputStream.write(ENTER_LINUX.getBytes());
		} else// 竖着写入
		{
			for (int i = 0; i < content.length; i++) {
				outputStream.write((content[i] + ENTER_LINUX).getBytes());
			}
		}
		outputStream.flush();// 写入文本
	}
	/**
	 * 效率太低，待修正
	 * 将数据按照excel的方法写入List<string[]>,null和""都不写入，最后写入一个换行
	 * 内部close()
	 * @param sep
	 *            txt文本的分割符,为正则表达式，tab是"\t"
	 * @param rowStartNum
	 *            实际写入起始行
	 * @param columnStartNum
	 *            实际写入起始列
	 * @throws Exception
	 */
	public void ExcelWrite(List<String[]> content) {
		ExcelWrite(content, 1, 1);
	}
	/**
	 * 效率太低，待修正
	 * 将数据按照excel的方法写入List<string[]>,null和""都不写入，最后写入一个换行
	 * 内部close()
	 * @param sep
	 *            txt文本的分割符,为正则表达式，tab是"\t"
	 * @param rowStartNum
	 *            实际写入起始行
	 * @param columnStartNum
	 *            实际写入起始列
	 * @throws Exception
	 */
	public void ExcelWrite(List<String[]> content, int rowStartNum, int columnStartNum) {
		if (content == null || content.size() == 0) {
			return;
		}
		try {
			for (int i = 0; i < content.size(); i++) {
				for (int j = 0; j < content.get(i).length; j++) {
					if (content.get(i)[j] == null)
						content.get(i)[j] = "";
					if (j < (content.get(i).length - 1)) {
						outputStream.write((content.get(i)[j] + sep).getBytes());
					} else {
						outputStream.write(content.get(i)[j].getBytes());
					}
				}
				outputStream.write(ENTER_LINUX.getBytes());// 换行
			}
			outputStream.flush();// 写入文本
		} catch (Exception e) {
			logger.error("write list data error:"+getFileName());
		}
		close();
	}

	/**
	 * 将数据按照excel的方法写入List<string[]>,null和""都写为""，最后写入一个换行
	 * 
	 * @param sep
	 *            txt文本的分割符,为正则表达式，tab是"\t"
	 * @param column
	 *            要写入content的哪几列，从0开始记数
	 * @param include
	 *            设置column，如果为true，仅仅写column的哪几列，如果为false，则将column的那几列去除
	 * @param rowStartNum
	 *            实际写入起始行
	 * @param columnStartNum
	 *            实际写入起始列
	 * @throws Exception
	 */
	public void ExcelWrite(List<String[]> content, int[] column, boolean include, int rowStartNum, int columnStartNum)
			throws Exception {
		if (include) {
			for (int i = 0; i < content.size(); i++) {
				for (int j = 0; j < column.length; j++) {
					if (content.get(i)[column[j]] == null)
						content.get(i)[column[j]] = "";
					if (j < (column.length - 1)) {
						outputStream.write((content.get(i)[column[j]] + sep).getBytes());
					} else {
						outputStream.write(content.get(i)[column[j]].getBytes());
					}
				}
				outputStream.write(ENTER_LINUX.getBytes());// 换行
			}
			outputStream.flush();// 写入文本
		} else {
			ArrayList<Integer> lscolumn = new ArrayList<Integer>();
			for (int i = 0; i < column.length; i++) {
				lscolumn.add(column[i]);
			}

			for (int i = 0; i < content.size(); i++) {
				for (int j = 0; j < content.get(i).length; j++) {
					if (lscolumn.contains(j)) // 当读取到column中的某一列时，跳过
						continue;
					if (content.get(i)[j] == null)
						content.get(i)[j] = "";
					if (j < (content.get(i).length - 1)) {
						outputStream.write((content.get(i)[j] + sep).getBytes());
					} else {
						outputStream.write(content.get(i)[j].getBytes());
					}
				}
				outputStream.write(ENTER_LINUX.getBytes());// 换行
			}
			outputStream.flush();// 写入文本
		}
	}
	/**
	 * 获得txt的文本，如果没压缩，则将文件改名，如果压缩了，则返回OutTxt的解压缩文件
	 * @param OutTxt
	 */
	public void unZipFile(String OutTxt)
	{
		if (this.filetype.equals(TXT)) {
			FileOperate.moveFile(txtfile.getAbsolutePath(), FileOperate.getParentPathName(OutTxt), FileOperate.getFileName(OutTxt), true);
			return;
		}
		TxtReadandWrite txtOut = new TxtReadandWrite(OutTxt, true);
		for (String string : readlines()) {
			txtOut.writefileln(string);
		}
		close();
		txtOut.close();
	}

	/**
	 * 设定待抓取本文件中的特定文字
	 * @param grepContent
	 */
	public void setGrepContent(String grepContent) {
		this.grepContent = grepContent;
	}
	
	public ArrayList<String> grepInfo(int range, boolean caseSensitive, boolean regx) {
		try {
			return grepInfoExp(range, caseSensitive, regx);
		} catch (Exception e) {
			return new ArrayList<String>();
		}
	}
	/**
	 * 获取抓取信息以及其前后几行的信息
	 * @param txtFile
	 * @param zipType
	 * @param grepContent 可以是正则表达式
	 * @param range
	 * @param regx 是否是正则表达式，如果是正则表达式那么速度会慢
	 * @return
	 * @throws Exception 
	 */
	private ArrayList<String> grepInfoExp(int range, boolean caseSensitive, boolean regx) throws Exception {
		PatternOperate patternOperate = new PatternOperate(this.grepContent, caseSensitive);
		/**
		 * 存储获得string上面的string 本来想用list存储的，但是考虑效率问题，所以用string数组来存储
		 * 依次保存上面的几行信息，循环保存
		 */
		String[] tmpContent = new String[range];
		int i = 0;
		// 保存最后的结果
		ArrayList<String> lsResult = new ArrayList<String>();
		String content = "";
		BufferedReader reader = readfile();
		while ((content = reader.readLine()) != null) {
			if (grepInfo(patternOperate, content, caseSensitive, regx)) {
				int num = 0;// 计数器，将前面的几行全部加入list
				// 加入前面保存的文字
				while (num < range) {
					if (i >= range) {
						i = 0;
					}
					lsResult.add(tmpContent[i]);
					num++;
					i++;
				}
				// 加入本行文字
				lsResult.add(content);
				// 将后几行加入list，然后结束
				int rest = 0;
				while ((content = reader.readLine()) != null) {
					if (rest >= range) {
						close();
						return lsResult;
					}
					lsResult.add(content);
					rest++;
				}
				close();
				return lsResult;
			}
			tmpContent[i] = content;
			i++;
			if (i >= range) {
				i = 0;
			}
		}
		close();
		return null;
	}
	/**
	 * 给定文字，以及是否大小写，然后看是不是含有需要的文字
	 * @param content
	 * @param caseSensitive
	 * @param regx
	 * @return
	 */
	private boolean grepInfo(PatternOperate patternOperate, String content, boolean caseSensitive, boolean regx)
	{
		if (!regx) {
			if (!caseSensitive)
				if (content.toLowerCase().contains(grepContent))
					return true;
			else
				if (content.contains(grepContent)) 
					return true;
		}
		else {
			String getStr = patternOperate.getPatFirst(content);
			if (getStr != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 必须关闭
	 * 关闭流文件
	 */
	public void close() {
		try { outputStream.flush(); } catch (Exception e) {}
		try { bufread.close(); } catch (Exception e) {}
		try { bufwriter.close(); } catch (Exception e) {}
		try { inputStream.close(); } catch (Exception e) {}
		try { outputStream.close(); } catch (Exception e) {}
		try {
			if (filetype.equals(ZIP)) {
				zipOutputStream.closeArchiveEntry();
			}
		} catch (Exception e) { }
	}
	   protected void finalize() {
         close();
         try {
			super.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

       }
	   
       
       
       /**
        * 根据文件得到该文件中文本内容的编码
        * 
        * @param file 要分析的文件
        */
       public static String getCharset(File file) {
               String charset = "GBK"; // 默认编码
               byte[] first3Bytes = new byte[3];
               try {
                   boolean checked = false;
                   BufferedInputStream bis = new BufferedInputStream(
                         new FileInputStream(file));
                   bis.mark(0);
                   int read = bis.read(first3Bytes, 0, 3);
                   if (read == -1)
                       return charset;
                   if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                       charset = "UTF-16LE";
                       checked = true;
                   } else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1]
                       == (byte) 0xFF) {
                       charset = "UTF-16BE";
                       checked = true;
                   } else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1]
                           == (byte) 0xBB
                           && first3Bytes[2] == (byte) 0xBF) {
                       charset = "UTF-8";
                       checked = true;
                   }
                   bis.reset();
                   if (!checked) {
                       int loc = 0;
                       while ((read = bis.read()) != -1) {
                           loc++;
                           if (read >= 0xF0)
                               break;
                           //单独出现BF以下的，也算是GBK
                           if (0x80 <= read && read <= 0xBF)
                               break;
                           if (0xC0 <= read && read <= 0xDF) {
                               read = bis.read();
                               if (0x80 <= read && read <= 0xBF)// 双字节 (0xC0 - 0xDF)
                                   // (0x80 -
                                   // 0xBF),也可能在GB编码内
                                   continue;
                               else
                                   break;
                            // 也有可能出错，但是几率较小
                           } else if (0xE0 <= read && read <= 0xEF) {
                               read = bis.read();
                               if (0x80 <= read && read <= 0xBF) {
                                   read = bis.read();
                                   if (0x80 <= read && read <= 0xBF) {
                                       charset = "UTF-8";
                                       break;
                                   } else
                                       break;
                               } else
                                   break;
                           }
                       }
                       System.out.println(loc + " " + Integer.toHexString(read));
                   }
                   bis.close();
               } catch (Exception e) {
                   e.printStackTrace();
               }
               return charset;
       }

}
