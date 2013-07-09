package com.novelbio.base.dataOperate;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.TxtReadandWrite.PlatForm;
import com.novelbio.base.dataOperate.TxtReadandWrite.TXTtype;
import com.novelbio.base.fileOperate.FileHadoop;
import com.novelbio.base.fileOperate.FileOperate;

class TxtWrite implements Closeable {
	private static Logger logger = Logger.getLogger(TxtReadandWrite.class);
		
	String txtfile;
	FileHadoop fileHadoop;
	
	BufferedOutputStream outputStream;
	boolean append = true;
	
	PlatForm platform = PlatForm.pc;
	
	/**
	 * 仅仅为了最后关闭zip用
	 */
	ArchiveOutputStream zipOutputStream;
	
	public TxtWrite(String fileName) {
		if (fileName.startsWith("hdfs://")) {
			fileHadoop = new FileHadoop(fileName);
			platform = PlatForm.hadoop;
		}
		this.txtfile = fileName;
	}
	
	@Deprecated
	public TxtWrite(FileHadoop fileHadoop) {
		this.fileHadoop = fileHadoop;
		platform = PlatForm.hadoop;
	}
	
	public void setAppend(boolean append) {
		this.append = append;
	}
	
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
	
	protected void createFile() throws Exception {
		String fileName = "";
		OutputStream outputStreamRaw = null;
		if (platform == PlatForm.pc) {
			outputStreamRaw = new FileOutputStream(txtfile, append);
		}
		else if (platform == PlatForm.hadoop) {
			if (append) {
				outputStreamRaw = fileHadoop.getOutputStreamAppend();
			} else {
				outputStreamRaw = fileHadoop.getOutputStreamNew(true);
			}
		}
		TXTtype txtTtype = TXTtype.getTxtType(fileName);

		if (txtTtype == TXTtype.Txt) {
			outputStream = new BufferedOutputStream(outputStreamRaw, TxtReadandWrite.bufferLen);
		} else if (txtTtype == TXTtype.Gzip) {
			outputStream = new BufferedOutputStream(new GZIPOutputStream(outputStream, TxtReadandWrite.bufferLen), TxtReadandWrite.bufferLen);
		} else if (txtTtype == TXTtype.Bzip2) {
			outputStream = new BufferedOutputStream(new BZip2CompressorOutputStream(outputStream, TxtReadandWrite.bufferLen), TxtReadandWrite.bufferLen);
		} else if (txtTtype == TXTtype.Zip) {
			zipOutputStream = new ZipArchiveOutputStream(outputStream);
			ZipArchiveEntry entry = new ZipArchiveEntry(FileOperate.getFileNameSep(fileName)[0]);
			zipOutputStream.putArchiveEntry(entry);
			outputStream = new BufferedOutputStream(zipOutputStream, TxtReadandWrite.bufferLen);
		}
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
			outputStream.write((content + TxtReadandWrite.ENTER_LINUX).getBytes());
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
			content2 = content2 + TxtReadandWrite.sep + content[i];
		}
		try {
			outputStream.write((content2 + TxtReadandWrite.ENTER_LINUX).getBytes());
		} catch (Exception e) { }
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
			outputStream.write(TxtReadandWrite.ENTER_LINUX.getBytes());
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
					outputStream.write(TxtReadandWrite.ENTER_LINUX.getBytes());
				}
				outputStream.write(mychar[i]);
			}
			outputStream.flush();
		} catch (Exception e) {
		}
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
					outputStream.write(TxtReadandWrite.ENTER_LINUX.getBytes());
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
					outputStream.write(TxtReadandWrite.ENTER_LINUX.getBytes());
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
				outputStream.write(TxtReadandWrite.ENTER_LINUX.getBytes());
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
	public<T> void writefile(List<T> lsContent) {
		if (lsContent == null) {
			return;
		}
		try {
			for (T t : lsContent) {
				outputStream.write(t.toString().getBytes());
				outputStream.write(TxtReadandWrite.ENTER_LINUX.getBytes());
			}
			outputStream.flush();
		} catch (Exception e) {
			// TODO: handle exception
		}
		close();
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
		if (content == null || content.size() == 0) {
			return;
		}
		try {
			for (int i = 0; i < content.size(); i++) {
				for (int j = 0; j < content.get(i).length; j++) {
					if (content.get(i)[j] == null)
						content.get(i)[j] = "";
					if (j < (content.get(i).length - 1)) {
						outputStream.write((content.get(i)[j] + TxtReadandWrite.sep).getBytes());
					} else {
						outputStream.write(content.get(i)[j].getBytes());
					}
				}
				outputStream.write(TxtReadandWrite.ENTER_LINUX.getBytes());// 换行
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
	public void ExcelWrite(List<String[]> content, int[] column, boolean include)
			throws Exception {
		if (include) {
			for (int i = 0; i < content.size(); i++) {
				for (int j = 0; j < column.length; j++) {
					if (content.get(i)[column[j]] == null)
						content.get(i)[column[j]] = "";
					if (j < (column.length - 1)) {
						outputStream.write((content.get(i)[column[j]] + TxtReadandWrite.sep).getBytes());
					} else {
						outputStream.write(content.get(i)[column[j]].getBytes());
					}
				}
				outputStream.write(TxtReadandWrite.ENTER_LINUX.getBytes());// 换行
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
						outputStream.write((content.get(i)[j] + TxtReadandWrite.sep).getBytes());
					} else {
						outputStream.write(content.get(i)[j].getBytes());
					}
				}
				outputStream.write(TxtReadandWrite.ENTER_LINUX.getBytes());// 换行
			}
			outputStream.flush();// 写入文本
		}
	}

	/**
	 * 必须关闭
	 * 关闭流文件
	 */
	public void close() {
		try { outputStream.flush(); } catch (Exception e) {}
		try { outputStream.close(); } catch (Exception e) {}
		try { zipOutputStream.closeArchiveEntry(); } catch (Exception e) { }
	}
	
}
