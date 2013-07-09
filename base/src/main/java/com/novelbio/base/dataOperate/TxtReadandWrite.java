package com.novelbio.base.dataOperate;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;

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
	private static Logger logger = Logger.getLogger(TxtReadandWrite.class);
	
	protected static enum PlatForm {
		pc, hadoop
	}
	
	public static enum TXTtype{
		Gzip, Bzip2, Zip, Txt;
		/**
		 * 根据文件后缀判断文件的类型，是gz还是txt等
		 * @return
		 */
		public static TXTtype getTxtType(String fileName) {
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
	}

	public final static String ENTER_LINUX = "\n";
	public final static String ENTER_WINDOWS = "\r\n";
	
	static int bufferLen = 100000;

	static String sep = "\t";

	TxtRead txtRead;
	TxtWrite txtWrite;
	
	boolean read = true;
	@Deprecated
	public TxtReadandWrite(FileHadoop fileHadoop) {
		this(fileHadoop, false);
	}
	
	public TxtReadandWrite(String fileName) {
		this(fileName, false);
	}
	
	public TxtReadandWrite(String fileName, boolean creatFile) {
		this(fileName, creatFile, false);
	}
	@Deprecated
	public TxtReadandWrite(FileHadoop fileHadoop, boolean creatFile) {
		this(fileHadoop, creatFile, false);
	}
	
	public TxtReadandWrite(String fileName, boolean writeFile, boolean append) {
		if (writeFile) {
			txtWrite = new TxtWrite(fileName);
			txtWrite.setAppend(append);
			try { txtWrite.createFile(); } catch (Exception e) { e.printStackTrace(); }
			read = false;
		} else {
			txtRead = new TxtRead(fileName);
			read = true;
		}
	}
	@Deprecated
	public TxtReadandWrite(FileHadoop fileHadoop, boolean writeFile, boolean append) {
		if (writeFile) {
			txtWrite = new TxtWrite(fileHadoop);
			txtWrite.setAppend(append);
			try { txtWrite.createFile(); } catch (Exception e) { e.printStackTrace(); }
			read = false;
		} else {
			txtRead = new TxtRead(fileHadoop);
			read = true;
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
	/**
	 * 从第几行开始读，是实际行
	 * @param lines 如果lines小于1，则从头开始读取
	 * @return
	 */
	public Iterable<String> readlines(int lines) {
		return txtRead.readlines(lines);
	}
	
	/**
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
	 * @param Num 读取前几列，实际列。如果文本没有那么多列，那么只读取所有列
	 * @return 返回 String，内部close
	 * @throws Exception
	 */
	public ArrayList<String> readFirstLines(int Num) {
		return txtRead.readFirstLines(Num);
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
	public void flash() {
		if (txtWrite != null) {
			txtWrite.flash();
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
	 * 写入一行数组并换行，用sep隔开
	 * @param content
	 *            ，要写入文件内容
	 * @throws Exception
	 */
	public void writefileln(String[] content) {
		txtWrite.writefileln(content);
	}
	
	/**
	 * 写入一行list，用sep隔开
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
		try { txtRead.close(); } catch (Exception e) { }
		try { txtWrite.close(); } catch (Exception e) { }
		if (!read) {
			try {
				if (txtWrite.platform == PlatForm.pc) {
					txtRead = new TxtRead(txtWrite.getFileName());
				} else if (txtWrite.platform == PlatForm.hadoop) {
					txtRead = new TxtRead(txtWrite.fileHadoop);
				}
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
			bis.close();
		} catch (Exception e) {
			e.printStackTrace();
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
			FileOperate.moveFile(fileName, FileOperate.getParentPathName(tmpTxt), FileOperate.getFileName(tmpTxt), true);
			return;
		}
		TxtReadandWrite txtOut = new TxtReadandWrite(tmpTxt, true);
		for (String string : readlines()) {
			txtOut.writefileln(string);
		}
		close();
		txtOut.close();
	}
	
}
////大文件排
 class TestCountWords {  
       public static void main(String[] args) {  
           File wf = new File("words.txt");  
           final CountWords cw1 = new CountWords(wf, 0, wf.length()/2);  
           final CountWords cw2 = new CountWords(wf, wf.length()/2, wf.length());  
           final Thread t1 = new Thread(cw1);  
           final Thread t2 = new Thread(cw2);  
           //开辟两个线程分别处理文件的不同片段  
           t1.start();  
           t2.start();  
           Thread t = new Thread() {  
               public void run() {  
                   while(true) {  
                       //两个线程均运行结束  
                       if(Thread.State.TERMINATED==t1.getState() && Thread.State.TERMINATED==t2.getState()) {  
                           //获取各自处理的结果  
                           HashMap<String, Integer> hMap1 = cw1.getResult();  
                           HashMap<String, Integer> hMap2 = cw2.getResult();  
                           //使用TreeMap保证结果有序  
                           TreeMap<String, Integer> tMap = new TreeMap<String, Integer>();  
                           //对不同线程处理的结果进行整合  
                           tMap.putAll(hMap1);  
                           tMap.putAll(hMap2);  
                           //打印输出，查看结果  
                           for(Entry<String, Integer> entry : tMap.entrySet()) {  
                               String key = entry.getKey();    
                               int value = entry.getValue();    
                               System.out.println(key+":\t"+value);    
                           }  
                           //将结果保存到文件中  
                           mapToFile(tMap, new File("result.txt"));  
                       }  
                       return;  
                   }  
               }  
           };  
           t.start();  
       }  
       //将结果按照 "单词：次数" 格式存在文件中  
       private static void mapToFile(Map<String, Integer> src, File dst) {  
           try {  
               //对将要写入的文件建立通道  
               FileChannel fcout = new FileOutputStream(dst).getChannel();  
               //使用entrySet对结果集进行遍历  
               for(Map.Entry<String,Integer> entry : src.entrySet()) {  
                   String key = entry.getKey();  
                   int value = entry.getValue();  
                   //将结果按照指定格式放到缓冲区中  
                   ByteBuffer bBuf = ByteBuffer.wrap((key+":\t"+value).getBytes());  
                   fcout.write(bBuf);  
                   bBuf.clear();  
               }  
           } catch (FileNotFoundException e) {  
               e.printStackTrace();  
           } catch (IOException e) {  
               e.printStackTrace();  
           }  
       }  
   }
     
   class CountWords implements Runnable {  
         
       private FileChannel fc;  
       private FileLock fl;  
       private MappedByteBuffer mbBuf;  
       private HashMap<String, Integer> hm;  
         
       public CountWords(File src, long start, long end) {  
           try {  
               //得到当前文件的通道  
               fc = new RandomAccessFile(src, "rw").getChannel();  
               //锁定当前文件的部分  
               fl = fc.lock(start, end, false);  
               //对当前文件片段建立内存映射，如果文件过大需要切割成多个片段  
               mbBuf = fc.map(FileChannel.MapMode.READ_ONLY, start, end);  
               //创建HashMap实例存放处理结果  
               hm = new HashMap<String,Integer>();  
           } catch (FileNotFoundException e) {  
               e.printStackTrace();  
           } catch (IOException e) {  
               e.printStackTrace();  
           }  
       }  
       
       public void run() {  
           String str = Charset.forName("UTF-8").decode(mbBuf).toString();  
           //使用StringTokenizer分析单词  
           StringTokenizer token = new StringTokenizer(str);  
           String word;  
           while(token.hasMoreTokens()) {  
               //将处理结果放到一个HashMap中，考虑到存储速度  
               word = token.nextToken();  
               if(null != hm.get(word)) {  
                   hm.put(word, hm.get(word)+1);  
               } else {  
                   hm.put(word, 1);  
               }  
           }  
           try {  
               //释放文件锁  
               fl.release();  
           } catch (IOException e) {  
               e.printStackTrace();  
           }  
           return;  
       }  
         
       //获取当前线程的执行结果  
       public HashMap<String, Integer> getResult() {  
           return hm;  
       }
}
