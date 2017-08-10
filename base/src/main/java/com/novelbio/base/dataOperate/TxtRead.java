package com.novelbio.base.dataOperate;

import hdfs.jsr203.HdfsConfInitiator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.log4j.Logger;

import com.hadoop.compression.lzo.LzopCodec;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite.TXTtype;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.ExceptionNbcFile;
import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.fileOperate.PositionInputStream;

class TxtRead implements Closeable {
	private static final Logger logger = Logger.getLogger(TxtRead.class);
	
	/** 读取本地文件时设定 */
	Path file;
	
	PositionInputStream inputStreamRaw;
	InputStream inputStream;
	BufferedReaderNBC bufread;
	
	/** 抓取文件中特殊的信息 */
	String grepContent = "";
	
	TXTtype txTtype = null;
	boolean isStream = false;
	
	long filesize = 0;
	
	/** 单行最多不能超过这么长，否则就报错 */
	private int maxLineNum = 10000000;
	
	public TxtRead(Path file) {
		txTtype = TXTtype.getTxtType(file.toString());
		this.file = file;
		this.filesize = FileOperate.getFileSizeLong(file); 
		isStream = false;
	}
	
	public TxtRead(InputStream inputStream) {
		this.inputStreamRaw = new PositionInputStream(inputStream);
		txTtype = TXTtype.Txt;
		isStream = true;
	}
	
	public TxtRead(InputStream inputStream, TXTtype txtTtype) {
		this.inputStreamRaw = new PositionInputStream(inputStream);
		txTtype = txtTtype;
		isStream = true;
	}
	
	/** 单行最多不能超过这么长，否则就报错 */
	public void setMaxLineNum(int maxLineNum) {
		this.maxLineNum = maxLineNum;
	}
	
	/** 如果读取的是流，则返回null */
	public String getFileName() {
		if (isStream) {
			return null;
		}
		return file.toString();
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
			result = TxtReadandWrite.ENTER_LINUX;
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
		close();
		char[] mychar = cb.array();
		if (mychar[mychar.length - 2] == 13 && mychar[mychar.length - 1] == 10) {
			return TxtReadandWrite.ENTER_WINDOWS;
		} else {
			return TxtReadandWrite.ENTER_LINUX;
		}
	}
	
	public Iterable<String> readlines() {
		try {
			return readPerlines();
		} catch (ExceptionNbcFile e) {
			throw e;
		}catch (Exception e) {
			String fileName = getFileName();
			if (fileName == null) {
				close();
				throw new ExceptionNbcFile("read stream error", e);
			} else {
				close();
				throw new ExceptionNbcFile("read file " + getFileName() + " error", e);
			}
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
			close();
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
		bufread =  readfile(); 
		return new Iterable<String>() {
			int linNum = 0;
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
						linNum++;
						String line = null;
						try {
							line = bufread.readLine();
						} catch (IOException ioEx) {
							line = null;
						} catch (ExceptionNBCReadLineTooLong e) {
							throw new ExceptionNbcFile("file " + getFileName() + " have a very long line on line " + linNum, e);
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
	 * 效率低
	 * @return
	 */
	public long getTxtLen() {
		int Result = 0;
		for (String content : readlines()) {
			Result = Result + content.trim().length();
		}
		close();
		return Result;
	}

	/** @return 返回 String，读完不用关闭Buffer流 */
	public String readFirstLine() {
		String firstLine = "";
		try {
			firstLine = readlines().iterator().next();
		} finally{
			close();
		}
		return firstLine;
	}
	
	/**
	 * @param Num 读取前几行，实际行。如果文本没有那么多行，那么只读取所有行
	 * @return 返回 String，内部close
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
		close();
		return lsResult;
	}

	/**
	 * 按照excel方法读取文本时使用，用于 获得txt文本的行数，如果最后一行是""，则忽略最后一行
	 * @return
	 * @throws Exception
	 */
	public int ExcelRows() {
		try {
			int rowNum = 0;
			String content2 = "";
			for (String content : readlines()) {
				rowNum++;
				content2 = content;
			}
			if (content2.equals("")) {
				rowNum--;
			}
			close();
			return rowNum;
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
	public int ExcelColumns(){
		int colNum=0;

		int excelRows = 5000; int rowNum = 0;
		for (String tmpstr : readlines()) {
			if (rowNum > excelRows) {
				break;
			}
			rowNum++;
			int TmpColNum = tmpstr.split(TxtReadandWrite.sep).length;
			if (TmpColNum>colNum) {
				colNum=TmpColNum;
			}
		}
		return colNum;
	}

	/**
	 * 按照excel方法读取文本时使用，用于 获得txt文本指定行的列数
	 * @param setRow 读取指定行并判定column
	 * @param sep
	 *            该行的分隔符，为正则表达式，tab为"\t"
	 * @return 返回指定行的列数
	 * @throws Exception
	 */
	public int ExcelColumns(int setRow, String sep) throws Exception {
		int i = 0;
		String thisContent = null;
		for (String content : readlines()) {
			i++;
			thisContent = content;
			if (i == setRow) {
				break;
			}
		}
		if (thisContent == null) {
			return -1;
		}
		String[] tmp = thisContent.split(sep);
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
		BufferedReaderNBC readasexcel = readfile();
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
	 * 设定待抓取本文件中的特定文字
	 * @param grepContent
	 */
	public void setGrepContent(String grepContent) {
		this.grepContent = grepContent;
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
		try {
			return grepInfoExp(range, caseSensitive, isRegx);
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
	private ArrayList<String> grepInfoExp(int range, boolean caseSensitive, boolean isRegx) throws Exception {
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
		BufferedReaderNBC reader = readfile();
		while ((content = reader.readLine()) != null) {
			if (grepInfo(patternOperate, content, caseSensitive, isRegx)) {
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
	private boolean grepInfo(PatternOperate patternOperate, String content, boolean caseSensitive, boolean regx) {
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
	 * 这个内部使用，外部用@readlines代替
	 * 有时间改成private方法
	 * @param path输入文件名
	 * @return 返回BufferedReader，记得读完后要关闭Buffer流
	 * @throws Exception
	 */
	BufferedReaderNBC readfile() {
		try { initialReading(); 	}
		catch (NoSuchFileException e) {
			throw new ExceptionNbcFile("cannot file file: " + getFileName());
		} catch (IOException e) {
			String fileName = getFileName();
			if (StringOperate.isRealNull(fileName) && inputStreamRaw != null) {
				throw	new ExceptionNbcFile("read inputstream error ", e);
			} else {
				throw	new ExceptionNbcFile("read file error " + fileName, e);
			}
		}
		bufread = new BufferedReaderNBC(new InputStreamReader(inputStream));
		bufread.setMaxLineNum(maxLineNum);
		return bufread;
	}
	/**
	 * 初始化读取文本
	 * @throws IOException 
	 * @throws Exception
	 */
	private void initialReading() throws IOException {
			if (bufread != null) {
				bufread.close();
				bufread = null;
			}
			if (!isStream && inputStream != null) {
				inputStream.close();
				inputStream = null;
			}
			
		if (!isStream && inputStreamRaw != null) {
			inputStreamRaw.close();
			inputStreamRaw = null;
		}

		setInStreamExp(txTtype);
	}
	
	private void setInStreamExp(TXTtype txtType) throws IOException {
		if (!isStream) {
			inputStreamRaw = new PositionInputStream(FileOperate.getInputStream(file));
		}

		if (txtType == TXTtype.Txt) {
			inputStream = new BufferedInputStream(inputStreamRaw, TxtReadandWrite.bufferLen);
		} else if (txtType == TXTtype.Zip) {
			ZipArchiveInputStream zipArchiveInputStream = new ZipArchiveInputStream(inputStreamRaw);
			ArchiveEntry zipEntry = null;
			while ((zipEntry = zipArchiveInputStream.getNextEntry()) != null) {
				if (!zipEntry.isDirectory() && zipEntry.getSize() > 0) {
					break;
				}
			}
			inputStream = new BufferedInputStream(zipArchiveInputStream, TxtReadandWrite.bufferLen);
		} else if (txtType == TXTtype.Gzip) {
			inputStream = new BufferedInputStream(new GZIPInputStream(inputStreamRaw, TxtReadandWrite.bufferLen), TxtReadandWrite.bufferLen);
		} else if (txtType == TXTtype.Bzip2) {
			inputStream = new BufferedInputStream(new BZip2CompressorInputStream(inputStreamRaw), TxtReadandWrite.bufferLen);
		} else if (txtType == TXTtype.Lzo) {
			LzopCodec lzo = new LzopCodec();
			lzo.setConf(HdfsConfInitiator.getConf());   
			inputStream = lzo.createInputStream(inputStreamRaw);   
		}
	}
	
	/**
	 * 获得读取的百分比
	 * @return 结果在0-1之间，小于0表示出错
	 */
	public double getReadPercentage() {
		long readByte = getReadByte();
		if (readByte < 0 || filesize < 0) {
			return -1;
		} else {
			return (double)readByte/filesize;
		}
	}
	
	public long getReadByte() {
		return inputStreamRaw.getPos();
	}
	
	/**
	 * 必须关闭
	 * 关闭流文件
	 */
	public void close() {
		FileOperate.close(bufread);
		FileOperate.close(inputStream);
		FileOperate.close(inputStreamRaw);
	}

}
