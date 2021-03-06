package com.novelbio.base.dataOperate;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.ExceptionNbcFile;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.fileOperate.RandomFileInt;
import com.novelbio.base.fileOperate.RandomFileInt.RandomFileFactory;

/**
 * 新建read没关系
 * 新建write就要关闭
 * 
 * 使用完毕后调用close关闭流
 * 
 * @author zong0jie
 * 
 */
public class TxtReadandWrite implements Closeable {
	private static final Logger logger = Logger.getLogger(TxtReadandWrite.class);
	
	protected static enum PlatForm {
		pc, hadoop, stream
	}
	
	public static enum TXTtype{
		BGzip, Gzip, Bzip2, Zip, Txt;
		
		/**
		 * 根据文件后缀判断文件的类型，是gz还是txt等
		 * @return
		 */
		public static TXTtype getTxtType(String fileName) {
			TXTtype txtTtype = null;
			fileName = fileName.toLowerCase().trim();
			if (fileName.endsWith(".gz") || fileName.endsWith("gzip")) {
				txtTtype = TXTtype.Gzip;
			} else if (fileName.endsWith(".bz2") || fileName.endsWith("bzip2")) {
				txtTtype = TXTtype.Bzip2;
			} else if (fileName.endsWith("zip")) {
				txtTtype = TXTtype.Zip;
			} else if (fileName.endsWith("bgz")) {//TODO 这种后缀名igv不认，igv只认gz后缀
				txtTtype = TXTtype.BGzip;
			} else {
				txtTtype = TXTtype.Txt;
			}
			return txtTtype;
		}
	}

	public final static String ENTER_LINUX = "\n";
	public final static String ENTER_WINDOWS = "\r\n";
	
	/** inputStream的缓冲长度 */
	public static int bufferLen = 100000;

	static String sep = "\t";

	TxtRead txtRead;
	TxtWrite txtWrite;
	
	boolean read = true;
	
	public TxtReadandWrite(File file) {
		this(file, false);
	}
	
	public TxtReadandWrite(File file, boolean isNeedWriteFile) {
		this(file, isNeedWriteFile, false);
	}
	
	public TxtReadandWrite(String fileName) {
		this(fileName, false);
	}
	
	public TxtReadandWrite(String fileName, boolean isNeedWriteFile) {
		this(fileName, isNeedWriteFile, false);
	}
	
	public TxtReadandWrite(Path path, boolean isNeedWriteFile) {
		this(path, isNeedWriteFile, false);
	}
	
	public TxtReadandWrite(Path path) {
		this(path, false);
	}
	
	public TxtReadandWrite(String fileName, boolean isNeedWriteFile, boolean isAppend) {
		this(FileOperate.getPath(fileName), isNeedWriteFile, isAppend);
	}
	
	public TxtReadandWrite(File file, boolean isNeedWriteFile, boolean isAppend) {
		this(FileOperate.getPath(file), isNeedWriteFile, isAppend);
	}
	
	public TxtReadandWrite(Path path, boolean isNeedWriteFile, boolean isAppend) {
		if (isNeedWriteFile) {
			txtWrite = new TxtWrite(path);
			txtWrite.setAppend(isAppend);
			try {
				txtWrite.createFile(); 
			} catch (NoSuchFileException e) {
				throw new ExceptionNbcFile("cannot creat file, because the directory is not exist " + path.toString());
			} catch (Exception e) {
				throw new ExceptionNbcFile("cannot creat file " + path.toString(), e);
			}
			read = false;
		} else {
			txtRead = new TxtRead(path);
			read = true;
		}
	}
	/** 写入模式 */
	public TxtReadandWrite(OutputStream outputStream) {
		txtWrite = new TxtWrite(outputStream);
		read = false;
	}
	
	/** 读取模式 */
	public TxtReadandWrite(InputStream inputStream) {
		txtRead = new TxtRead(inputStream);
		read = true;
		
	}
	public TxtReadandWrite(InputStream inputStream, TXTtype txtTtype) {
		txtRead = new TxtRead(inputStream, txtTtype);
		read = true;
	}
	
	/** 读取文本的单行最多不能超过这么长，否则就报错 */
	public void setReadMaxLineNum(int maxLineNum) {
		if (txtRead != null) {
			txtRead.setMaxLineNum(maxLineNum);
		}
	}

	public String getFileName() {
		if (read) {
			return txtRead.getFileName();
		} else {
			return txtWrite.getFileName();
		}
	}
	/**
	 * 返回该文本的回车方式
	 * @return
	 */
	public String getEnterType() {
		return txtRead.getEnterType();
	}
	
	/** 读取的具体长度，出错返回 -1 */
	public long getReadByte() {
		if (txtRead != null) {
			return txtRead.getReadByte();
		}
		return -1;
	}
	
	/**
	 * 获得读取的百分比
	 * @return 结果在0-1之间，小于0表示出错
	 */
	public double getReadPercentage() {
		if (txtRead != null) {
			return txtRead.getReadPercentage();
		}
		return -1;
	}
	
	public Iterable<String> readlines() {
		return txtRead.readlines();
	}
	
	/** 只有当为true的时候才会返回<br>
	 * 可以用{@link #close()} 关闭该流
	 *  */
	public OutputStream getOutputStream() {
		return txtWrite.getOutputStream();
	}
	
	/**
	 * 从第几行开始读，是实际行
	 * @param lines 如果lines小于等于1，则从头开始读取
	 * @return
	 */
	public Iterable<String> readlines(int lines) {
		return txtRead.readlines(lines);
	}
	
	/** 获得txtRead.bufread<p>
	 * 注意调用readlines时会重置该BufferedReader<br>
	 * 但是先调用 readlines，再获得该bufread，不会重置
	 * @return
	 */
	public BufferedReader getBufferedReader() {
		return txtRead.bufread;
	}
	
	/**
	 * 	@Deprecated 用 {@link #readfileLs(String)} 代替
	 * @param path输入文件名
	 * @return 返回List<String>，读完关闭
	 * @throws Exception
	 */
	public ArrayList<String> readfileLs() {
		return txtRead.readfileLs();
	}
	
	/**
	 * 去除空格后文件的字符长度，不是文件大小，而是含有多少文字
	 * 效率低
	 * @return
	 */
	public long getTxtLen() {
		return txtRead.getTxtLen();
	}

	/** @return 返回 String，读完自动close */
	public String readFirstLine() {
		return txtRead.readFirstLine();
	}
	
	/**
	 * @param Num 读取前几行，实际行。如果文本没有那么多行，那么只读取所有行
	 * @return 返回 String，内部close
	 * @throws Exception
	 */
	public ArrayList<String> readFirstLines(int Num) {
		return txtRead.readFirstLines(Num);
	}
	
	/**
	 * 把所有的内容读成一个字符串
	 * @return
	 */
	@Deprecated
	public String readAllAsString() {
		StringBuffer content = new StringBuffer();
		for (String line : readlines()) {
			content.append(line);
		}
		return content.toString();
	}
	/**
	 * 按照excel方法读取文本时使用，用于 获得txt文本的行数，如果最后一行是""，则忽略最后一行
	 * @return
	 * @throws Exception
	 */
	public int ExcelRows() {
		return txtRead.ExcelRows();
	}

	/**
	 * 按照excel方法读取文本时使用，用于 获得文本中前5000行中最长行的列数
	 * @param sep
	 *            该行的分隔符，为正则表达式，tab为"\t"
	 * @return 返回指定行的列数
	 * @throws Exception
	 */
	public int ExcelColumns() {
		return txtRead.ExcelColumns();
	}

	/**
	 * 按照excel方法读取文本时使用，用于 获得txt文本指定行的列数
	 * @param setRow 读取指定行并判定column
	 * @param sep
	 *            该行的分隔符，为正则表达式，tab为"\t"
	 * @return 返回指定行的列数
	 * @throws Exception
	 */
	public int ExcelColumns(int setRow, String sep) {
		try {
			return txtRead.ExcelColumns(setRow, sep);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
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
	public String ExcelRead(String sep, int rowNum, int columnNum) {
		try {
			return txtRead.ExcelRead(sep, rowNum, columnNum);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
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
		if (columnStartNum <= 0) {
			columnStartNum = 1;
		}
		if (columnEndNum <= 0) {
			columnEndNum = ExcelColumns();
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
				
		for (String content : txtRead.readlines(rowStartNum)) {
			if (rowEndNum > 0 && countRows > readlines)
				break;
			
			if (content.trim().equals("")) {
				continue;
			}
			String[] tmp = content.split(TxtReadandWrite.sep);
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
		txtRead.close();
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
		return txtRead.getKey2Value(sep, keyCase);
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
		return txtRead.getKey2ValueDouble(sep, keyCase);
	}

	/**
	 * 设定待抓取本文件中的特定文字
	 * @param grepContent
	 */
	public void setGrepContent(String grepContent) {
		txtRead.setGrepContent(grepContent);
	}
	/**
	 * 获取抓取信息以及其前后几行的信息
	 * @param range
	 * @param caseSensitive 大小写敏感
	 * @param isRegx 是否是正则表达式，如果是正则表达式那么速度会慢
	 * @return
	 * @throws Exception 
	 */
	public ArrayList<String> grepInfo(int range, boolean caseSensitive, boolean isRegx) {
		return txtRead.grepInfo(range, caseSensitive, isRegx);
	}

	/**
	 * 写入并换行，没有flush
	 * @param content
	 *            ，要写入文件内容
	 * @throws Exception
	 */
	public void flush() {
		if (txtWrite != null) {
			txtWrite.flush();
		}
	}
	
	/**
	 * 写完自动flush
	 * @param content 要写入文件内容
	 * @throws Exception
	 */
	public void writefile(String content) {
		txtWrite.writefile(content);
	}
	/**
	 * 写完自动flush
	 * @param content 要写入文件内容
	 * @throws Exception
	 */
	public void writefile(char content) {
		txtWrite.writefile(content);
	}
	/**
	 * 将流写入文件
	 * 
	 * @param is
	 */
	public void writefile(InputStream is) {
		txtWrite.writefile(is);
	}
	public void writefile(byte[] bytes) {
		txtWrite.writefile(bytes);
	}
	public void writefile(byte b) {
		txtWrite.writefile(b);
	}
	/**
	 * @param content
	 *            ，要写入文件内容,并考虑是否刷新--也就是直接写入文件而不是进入缓存
	 * @throws Exception
	 */
	public void writefile(String content, boolean flush) {
		txtWrite.writefile(content, flush);
	}
	
	/**
	 * 写入并换行，没有flush
	 * @param content
	 *            ，要写入文件内容
	 * @throws Exception
	 */
	public void writefileln(String content) {
		txtWrite.writefileln(content);
	}
	/**
	 * 写入并换行，没有flush
	 * @param content
	 *            ，要写入文件内容
	 * @throws Exception
	 */
	public void writefileln(String content, boolean flash) {
		txtWrite.writefileln(content, flash);
	}
	/**
	 * 写入一行数组并换行，用sep隔开
	 * @param content
	 *            ，要写入文件内容
	 * @throws Exception
	 */
	public void writefileln(String[] content) {
		txtWrite.writefileln(content);
	}
	
	/**
	 * 写入一行list，用\t隔开
	 * @param content
	 */
	public void writefileln(List<String> content) {
		txtWrite.writefileln(content);
	}
	
	/**
	 * @deprecated
	 * 用{@link #writefilelnls(List)} 替代 <br>
	 * 直接写入一个表格ArrayList<ArrayList<String>>;
	 * @param lsls
	 */
	public void writefileln(ArrayList<ArrayList<String>> lsls) {
		txtWrite.writefileln(lsls);
	}
	/**
	 * 直接写入一个表格List<List<String>>;
	 * @param lsls
	 */
	public void writefilelnls(List<List<String>> lsls) {
		for (List<String> list : lsls) {
			txtWrite.writefileln(list);
		}
	}
	public void writefile(byte[] content, boolean flush) {
		txtWrite.writefile(content, flush);
	}
	/**
	 * 写入并换行
	 * @param content 要写入文件内容
	 * @throws Exception
	 */
	public void writefileln() {
		txtWrite.writefileln();
	}
	/**
	 * 写入并将写入的序列换行，目前只能写入ascII文本
	 * @param content 输入的string，是没有换行的那种
	 * @param length 每多少行进行换行
	 * @throws Exception
	 */
	public void writefilePerLine(String content, int length) {
		txtWrite.writefilePerLine(content, length);
	}
	
	/**
	 * 给定内容，写入文本，这个写入的东西可以给R语言用scan读取,默认每行20个元素，用空格隔开
	 * 
	 * @param content
	 */
	public void Rwritefile(double[] content) {
		txtWrite.Rwritefile(content);
	}
	/**
	 * 给定内容，写入文本，这个写入的东西可以给R语言用scan读取,默认每行20个元素，用空格隔开
	 * 
	 * @param content
	 */
	public void Rwritefile(int[] content) {
		txtWrite.Rwritefile(content);
	}
	/**
	 * 给定内容，写入文本，这个写入的东西可以给R语言用scan读取,默认每行20个元素，用空格隔开
	 * 内部close
	 * @param content
	 */
	public void Rwritefile(String[] content) {
		txtWrite.Rwritefile(content);
	}
	
	/**
	 * @param lsContent-T 注意T只能是string interge等简单的能转化为string的类
	 *            ，要写入List--String文件内容,自动在每一行添加换行符huiche;
	 *            内部close 流
	 * @throws Exception
	 */
	public<T> void writefile(List<T> lsContent) {
		txtWrite.writefile(lsContent);
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
		txtWrite.ExcelWrite(content);
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
	public void ExcelWrite(List<String[]> content, int[] column, boolean include) {
		try {
			txtWrite.ExcelWrite(content, column, include);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 关闭，关闭后依然可以读文件
	 */
	public void close() {
		if (txtRead != null) {
			txtRead.close();
		}
		if (txtWrite != null) {
			txtWrite.close();
		}
		
		if (!read) {
			try {
				txtRead = new TxtRead(FileOperate.getPath(txtWrite.getFileName()));
				read = true;
			} catch (Exception e) {}
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
		BufferedInputStream bis = null;
		try {
			boolean checked = false;
			bis = new BufferedInputStream(
					FileOperate.getInputStream(file.toPath()));
			bis.mark(0);
			int read = bis.read(first3Bytes, 0, 3);
			if (read == -1)
				return charset;
			if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
				charset = "UTF-16LE";
				checked = true;
			} else if (first3Bytes[0] == (byte) 0xFE
					&& first3Bytes[1] == (byte) 0xFF) {
				charset = "UTF-16BE";
				checked = true;
			} else if (first3Bytes[0] == (byte) 0xEF
					&& first3Bytes[1] == (byte) 0xBB
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
					// 单独出现BF以下的，也算是GBK
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
				System.out.println();
				System.out.println(loc + " " + Integer.toHexString(read));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			FileOperate.close(bis);
		}
		return charset;
	}

	public BufferedReader readfile() {
		return txtRead.readfile();
	}

	public void unZipFile(String tmpTxt) {
		String fileName = getFileName();
		TXTtype txTtype = TXTtype.getTxtType(fileName);
		if (txTtype == TXTtype.Txt) {
			FileOperate.moveFile(fileName, FileOperate.getParentPathNameWithSep(tmpTxt), FileOperate.getFileName(tmpTxt), true);
			return;
		}
		TxtReadandWrite txtOut = new TxtReadandWrite(tmpTxt, true);
		for (String string : readlines()) {
			txtOut.writefileln(string);
		}
		close();
		txtOut.close();
	}

	/**
     * 从文件末尾开始读取文件，并返回list
     * @param filename    file path
     * @param charset character
     */
    public static List<String> readReverse(String filename, int num) {
        RandomFileInt rf = null;
        LinkedList<String> lsResult = new LinkedList<>();
        try {
            rf = RandomFileFactory.createInstance(filename);
            long fileLength = rf.length();
            long start = rf.getFilePointer();// 返回此文件中的当前偏移量
            long readIndex = start + fileLength -1;
            String line;
            rf.seek(readIndex);// 设置偏移量为文件末尾
            int c = -1;
            
            int readLineNum = 0;
            while (readIndex > start) {
                c = rf.read();
                if (c == '\n' || c == '\r') {
                    line = rf.readLine();
                    if (line != null) {
//                    	Charset cs = Charset.forName("UTF-8");
                    	line = new String(line.getBytes("ISO-8859-1"), "UTF-8");
                    	lsResult.addFirst(line);
                    } else {
                    	lsResult.addFirst("");
                    }
                    readIndex--;
                    readLineNum++;
                    if (readLineNum >= num) {
						break;
					}
                }
                readIndex--;
                rf.seek(readIndex);
                if (readIndex == 0) {// 当文件指针退至文件开始处，输出第一行
                	line = rf.readLine();
                	line = new String(line.getBytes("ISO-8859-1"), Charset.defaultCharset());

                	lsResult.addFirst(line);
                	break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rf != null)
                    rf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lsResult;
    }

	public static String readFirstLine(String txtFile) {
		return readFirstLine(FileOperate.getPath(txtFile));
	}
	
	/** 内部会关闭流 */
	public static String readInputStream(InputStream is) {
		TxtReadandWrite txtRead = new TxtReadandWrite(is);
		StringBuffer sBuffer = new StringBuffer();
		int i = 0;
		for (String content : txtRead.readlines()) {
			if (i++ == 0) {
				sBuffer.append(content);
			} else {
				sBuffer.append("\n").append(content);
			}
		}
		txtRead.close();
		return sBuffer.toString();
	}
	
	public static String readFirstLine(File txtFile) {
		return readFirstLine(FileOperate.getPath(txtFile));
	}
	
	public static String readFirstLine(Path txtFile) {
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(txtFile);
		String firstLine = txtReadandWrite.readFirstLine();
		txtReadandWrite.close();
		return firstLine;
	}
	
	public static List<String> readfileLs(String txtFile) {
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(txtFile);
		List<String> lsResult = new ArrayList<>();
		for (String string : txtReadandWrite.readlines()) {
			lsResult.add(string);
		}
		txtReadandWrite.close();
		return lsResult;
	}

	public static List<String> readfileLs(File txtFile) {
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(txtFile);
		List<String> lsResult = new ArrayList<>();
		for (String string : txtReadandWrite.readlines()) {
			lsResult.add(string);
		}
		txtReadandWrite.close();
		return lsResult;
	}
	
	public static List<String> readfileLs(Path txtFile) {
		TxtReadandWrite txtReadandWrite = new TxtReadandWrite(txtFile);
		List<String> lsResult = new ArrayList<>();
		for (String string : txtReadandWrite.readlines()) {
			lsResult.add(string);
		}
		txtReadandWrite.close();
		return lsResult;
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
	public static String getFileContent(String filePathAndName) {
		return getFileContent(FileOperate.getPath(filePathAndName));
	}
	
	/**
	 *  获取文件内容<br>
	 * <b>只允许对小于5M的文件读取</b>
	 * 
	 * @author novelbio fans.fan
	 * @date 2017年11月30日
	 * @param filePathAndName
	 * @return
	 */
	public static String getFileContent(Path filePathAndName) {
		if (!FileOperate.isFileExistAndBigThan0(filePathAndName)) {
			return null;
		}
		if (FileOperate.getFileSizeLong(filePathAndName) > 5242880) {
			throw new RuntimeException("file size more than 5M.path=" + filePathAndName);
		}
		StringBuffer stringBuffer = new StringBuffer();
		TxtReadandWrite.readfileLs(filePathAndName).forEach(str -> stringBuffer.append(str).append("\n"));

		return stringBuffer.toString();
	}
}
