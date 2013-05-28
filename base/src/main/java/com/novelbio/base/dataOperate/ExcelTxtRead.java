package com.novelbio.base.dataOperate;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.ArrayOperate;


/**
 * 本类为实现一个小功能<br>
 * 安列读取exce或txt文件，如指定需要的某不连续几列<br>
 * 然后将这几列合并后以String[][]的形式返回
 * @author zong0jie
 *
 */
public class ExcelTxtRead {
	private static final Logger logger = Logger.getLogger(ExcelTxtRead.class);
	
	/**
	 * 指定excel/txt文件，以及需要读取的列和行
	 *  不将第一列空位或者null的行删除
	 * @param excelFile 待读取的excel文件
	 * @param columnID 待读取的列，int[]中间是读取的第几列，读取结果会按照指定的列的顺序给出
	 * @param rowStart
	 * @param rowEnd 如果rowEnd<1，则一直读到sheet1文件结尾
	 * @return 
	 */
	public static ArrayList<String[]> readLsExcelTxt(String excelFile,int[] columnID,int rowStart,int rowEnd) {
		return readLsExcelTxt(excelFile, columnID, rowStart, rowEnd,false);
	}
	/**
	 * 
	 * 指定excel/txt文件，以及需要读取的列和行
	 * @param excelFile 待读取的excel文件
	 * @param columnID 待读取的列，int[]中间是读取的第几列，读取结果会按照指定的列的顺序给出
	 * @param rowStart
	 * @param rowEnd 如果rowEnd<1，则一直读到sheet1文件结尾
	 * @param DelFirst 是否将第一列空位或者null的行删除
	 * @return
	 */
	public static ArrayList<String[]> readLsExcelTxt(String excelFile,int[] columnID,int rowStart,int rowEnd, boolean DelFirst) {
		ArrayList<String[]> lsResultTmp = new ArrayList<String[]>();

		if (ExcelOperate.isExcel(excelFile)) {
			ExcelOperate excelOperate = new ExcelOperate(excelFile);
			lsResultTmp = excelOperate.ReadLsExcel(rowStart, rowEnd, columnID);//(rowStartNum, columnStartNum, rowEndNum, columnEndNum);//readExcel(excelFile, columnID, rowStart, rowEnd);
		}
		else {
			TxtReadandWrite txtRead = new TxtReadandWrite(excelFile, false);
			lsResultTmp = txtRead.ExcelRead(rowStart, rowEnd, columnID, -1);
		}
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (String[] strings : lsResultTmp) {
			if (DelFirst && (strings[0] == null || strings[0].trim().equals(""))) {
				continue;
			}
			lsResult.add(strings);
		}
		return lsResult;
	}
	
	/**
	 * 读文件返回一个List<List<String>>
	 * @param excelFile 读入文件
	 * @param firstlinels1 第几行开始读
	 * @return
	 */
	public static List<List<String>> readLsExcelTxtls(String excelFile, int firstlinels1) {
		return readLsExcelTxtls(excelFile, 0, firstlinels1);
	}
	/**
	 * 读文件返回一个List<List<String>>
	 * @param excelFile 读入文件
	 * @param sheetNum 实际sheet数
	 * @param firstlinels1 第几行开始读
	 * @return
	 */
	public static List<List<String>> readLsExcelTxtls(String excelFile, int sheetNum, int firstlinels1) {
		List<List<String>> lsls = new ArrayList<List<String>>();
		ArrayList<String[]> lsStrs = readLsExcelTxt( excelFile, sheetNum, firstlinels1);
		for (String[] strings : lsStrs) {
			List<String> oneLineList = new ArrayList<String>();
			for (String string : strings) {
				oneLineList.add(string);
			}
			lsls.add(oneLineList);
		}
		return lsls;
	}
	/**
	 * 读文件返回一个List<List<String>>
	 * @param excelFile 读入文件
	 * @param firstlinels1 第几行开始读
	 * @return
	 */
	public static List<List<String>> readLsExcelTxtls(String excelFile, int sheetNum, int firstlinels1, int lastlins) {
		List<List<String>> lsls = new ArrayList<List<String>>();
		ArrayList<String[]> lsStrs = readLsExcelTxtFile(excelFile, sheetNum, firstlinels1, 0, lastlins, 0);
		for (String[] strings : lsStrs) {
			List<String> oneLineList = new ArrayList<String>();
			for (String string : strings) {
				oneLineList.add(string);
			}
			lsls.add(oneLineList);
		}
		return lsls;
	}
	/**
	 * 读文件返回一个List<List<String>>
	 * @param excelFile 读入文件
	 * @param firstlinels1 第几行开始读
	 * @return
	 */
	public static List<List<String>> readLsExcelTxtls(String excelFile, String sheetName, int firstlinels1, int lastlins) {
		List<List<String>> lsls = new ArrayList<List<String>>();
		ArrayList<String[]> lsStrs = readLsExcelTxtFile(excelFile, sheetName, firstlinels1, 1, lastlins, 0);
		for (String[] strings : lsStrs) {
			List<String> oneLineList = new ArrayList<String>();
			for (String string : strings) {
				oneLineList.add(string);
			}
			lsls.add(oneLineList);
		}
		return lsls;
	}
	/**
	 * 内部close
	 * 给定文件，xls2003/2007/txt，获得它们的信息，用arraylist-string[]保存
	 * @param File 文件名
	 * @param firstlinels1 从第几行开始读去，实际行数
	 * @param sep 如果是txt的话，间隔是什么
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<String[]> readLsExcelTxt(String excelFile,int firstlinels1) {
		return readLsExcelTxt(excelFile, 0, firstlinels1);
	}
	
	/**
	 * 内部close
	 * 给定文件，xls2003/2007/txt，获得它们的信息，用arraylist-string[]保存
	 * @param File 文件名
	 * @param firstlinels1 从第几行开始读去，实际行数
	 * @param sep 如果是txt的话，间隔是什么
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<String[]> readLsExcelTxt(String excelFile, int sheetNum, int firstlinels1) {
		ArrayList<String[]> ls1=null;
		if (ExcelOperate.isExcel(excelFile)) {
			ExcelOperate excel = new ExcelOperate(excelFile);
			ls1 = excel.ReadLsExcel(sheetNum, firstlinels1, 1, excel.getRowCount(), excel.getColCount());
			excel.Close();
			return ls1;
		}
		TxtReadandWrite txt = new TxtReadandWrite(excelFile);
		int txtRowNum = txt.ExcelRows();
		ls1 = txt.ExcelRead(firstlinels1, 1, txtRowNum , -1, 0);//从目标行读取
		txt.close();
		return ls1;
	}

	/**
	 * 用readLsExcelTxtFile代替
	 * 给定文件，xls2003/2007/txt，获得它们的信息，用arraylist-string[]保存
	 * @param excelFile
	 * @param rowStart 
	 * @param rowEnd 值小于等于0时，读取全部行
	 * @param colStart 
	 * @param colEnd 值小于等于0时，读取全部列
	 * @return
	 * @throws Exception
	 */
	@Deprecated 
	public static ArrayList<String[]> readLsExcelTxt(String excelFile,int rowStart, int rowEnd, int colStart, int colEnd) {
		ArrayList<String[]> ls1=null;
		if (ExcelOperate.isExcel(excelFile)) {
			ExcelOperate excel = new ExcelOperate(excelFile);
			ls1 = excel.ReadLsExcel(rowStart, colStart, rowEnd, colEnd);
			excel.Close();
			return ls1;
		}
		TxtReadandWrite txt = new TxtReadandWrite(excelFile, false);
		ls1=txt.ExcelRead(rowStart, colStart,rowEnd , colEnd, 0);//从目标行读取
		txt.close();
		return ls1;
	}
	/**
	 * 给定文件，xls2003/2007/txt，获得它们的信息，用arraylist-string[]保存
	 * @param excelFile
	 * @param rowStart
	 * @param colStart
	 * @param rowEnd 值小于等于0时，读取全部行
	 * @param colEnd 值小于等于0时，读取全部列
	 * @return
	 */
	public static ArrayList<String[]> readLsExcelTxtFile(String excelFile,int rowStart, int colStart, int rowEnd, int colEnd) {
		return readLsExcelTxtFile(excelFile, 1, rowStart, colStart, rowEnd, colEnd);
	}
	/**
	 * 给定文件，xls2003/2007/txt，获得它们的信息，用arraylist-string[]保存
	 * @param excelFile
	 * @param sheetNum
	 * @param rowStart
	 * @param colStart
	 * @param rowEnd 值小于等于0时，读取全部行
	 * @param colEnd 值小于等于0时，读取全部列
	 * @return
	 */
	public static ArrayList<String[]> readLsExcelTxtFile(String excelFile, int sheetNum, int rowStart, int colStart, int rowEnd, int colEnd) {
		ArrayList<String[]> ls1=null;
		if (ExcelOperate.isExcel(excelFile)) {
			ExcelOperate excel = new ExcelOperate(excelFile);
			ls1 = excel.ReadLsExcel(sheetNum, rowStart, colStart, rowEnd, colEnd);
			excel.Close();
			return ls1;
		}
		TxtReadandWrite txt = new TxtReadandWrite(excelFile, false);
		ls1=txt.ExcelRead(rowStart, colStart,rowEnd , colEnd, 0);//从目标行读取
		txt.close();
		return ls1;
	}
	/**
	 * 给定文件，xls2003/2007/txt，获得它们的信息，用arraylist-string[]保存
	 * @param excelFile
	 * @param sheetName
	 * @param rowStart
	 * @param colStart 实际起点，从1开始计算
	 * @param rowEnd 值小于等于0时，读取全部行
	 * @param colEnd 值小于等于0时，读取全部列
	 * @return
	 */
	public static ArrayList<String[]> readLsExcelTxtFile(String excelFile, String sheetName, int rowStart, int colStart, int rowEnd, int colEnd) {
		ArrayList<String[]> ls1=null;
		if (ExcelOperate.isExcel(excelFile)) {
			ExcelOperate excel = new ExcelOperate(excelFile);
			ls1 = excel.ReadLsExcel(sheetName, rowStart, colStart, rowEnd, colEnd);
			excel.Close();
			return ls1;
		}
		TxtReadandWrite txt = new TxtReadandWrite(excelFile, false);
		ls1=txt.ExcelRead(rowStart, colStart,rowEnd , colEnd, 0);//从目标行读取
		txt.close();
		return ls1;
	}
	/**
	 * 
	 * 给定文件，xls2003/2007/txt，获得它们的信息，用arraylist-string[]保存
	 * @param excelFile 写入已知文档，不过会将写入的sheet覆盖掉，txt的话会新建一个文档
	 * @param rowStart 
	 * @param rowEnd 值小于等于0时，读取全部行
	 * @param colStart 
	 * @param colEnd 值小于等于0时，读取全部列
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<String[]> writeLsExcelTxt(String excelTxtFile, List<String[]> lsContent, int rowStart, int colStart, int rowEnd, int colEnd) {
		ArrayList<String[]> ls1=null;
		if (ExcelOperate.isExcel(excelTxtFile)) {
			ExcelOperate excel = new ExcelOperate(excelTxtFile);
			excel.WriteExcel(1, 1, lsContent);
			excel.Close();
			return ls1;
		}
		TxtReadandWrite txt = new TxtReadandWrite(excelTxtFile, true);
		txt.ExcelWrite(lsContent);
		txt.close();
		return ls1;
	}
	/**
	 * 给定一个文本，指定某几列，然后将这几列所有相邻且重复的行全部删除，只保留重复的第一行
	 * 这个其实是shell命令uniq的一个补充
	 * @param inputFIle 输入文件
	 * @param sep 分隔符一般为\t
	 * @param column 第几列，实际列
	 * @param outPut 输出文件
	 * @throws Exception 
	 */
    public static void uniq(String inputFIle,String sep, int column, String outPut) throws Exception {
    	TxtReadandWrite txtInputFile=new TxtReadandWrite(inputFIle);
    	TxtReadandWrite txtOutput = new TxtReadandWrite(outPut, true);
    	String tmp="";
    	for (String content : txtInputFile.readlines()) {
    		String tmp2=content.split(sep)[column-1].trim();
			if (tmp.equals(tmp2)) {
				continue;
			}
			tmp=tmp2;
			txtOutput.writefile(content+"\n",false);
		}
    	txtInputFile.close();
    	txtOutput.close();
	}
    
}
