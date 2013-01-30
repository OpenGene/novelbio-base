package com.novelbio.base.dataOperate;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 本类需要实例化才能使用
 * 读取excel文件,注意读取前最好将excel的所有格式全部清除，不然可能会有问题。
 * 清除方法，将excel拷贝入txt文件，再拷贝回来
 * 可以跨平台使用
 * 读取速度可以	
 * 读取块返回的是一个二维数组，然后这个二维数组的起点是[0][0]，不同于C#中的[1,1]
 * 本类似乎无法获得最大列的数目,这里可以考虑采用一维数目不同的二维数组，这个实现可以考虑用foreach来遍历
 * 本代码原始作者 caihua ，Zong Jie修改
 */
public class ExcelOperate {   
	public static final int EXCEL2003 = 2003;
	public static final int EXCEL2007 = 2007;
	public static final int EXCEL_NOT = 100;
	public static final int EXCEL_NO_FILE = 0;

	 private Workbook wb;
	 private Sheet sheet;
	 private int sheetNum = 0; // 第sheetnum个工作表
     private String filename="";
     /** excel 2003 或者 excel 2007 */
	 int versionXls = 0;
	 public ExcelOperate() {}
	 /**
	  * 打开excel，没有就新建excel2003
	  * @param imputfilename
	  */
	 public ExcelOperate(String imputfilename) {
		 openExcel(imputfilename, false);
	 }
	 public ExcelOperate(String imputfilename, boolean excel2003) {
		 boolean excel2007 = !excel2003;
		 openExcel(imputfilename,excel2007);
	 }
	 
	 /**
	  * 读取excel文件获得HSSFWorkbook对象,默认新建2003
	  * 这个使用的时候要用try块包围
	  * 能读取返回true，不然返回false
	  * @param imputfilename
	  */
	 public boolean openExcel(String imputfilename) {  
		 return openExcel(imputfilename,false);
	 }
	 /**
	  * 判断是否为excel2003或2007
	  * @return
	  * EXCEL2003 EXCEL2007 EXCEL_NOT EXCEL_NO_FILE
	  */
	 public static boolean isExcel(String filename) {
		 try {
			 if (isExcelVersion(filename) == EXCEL2003 || isExcelVersion(filename) == EXCEL2007 )
				 return true;
		 } catch (Exception e) {}
		return false;
	 }
	 /**
	  * 判断是否为excel2003或2007
	  * @return
	  * EXCEL2003 EXCEL2007 EXCEL_NOT EXCEL_NO_FILE
	 * @throws FileNotFoundException 
	  */
	 private static int isExcelVersion(String filename) throws Exception {
		 if (!FileOperate.isFileExist(filename))
			return EXCEL_NO_FILE;
		 File f = new File(filename);
		 FileInputStream fos = null;
		try {
			fos = new FileInputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return EXCEL_NO_FILE;
		}
		fos = new FileInputStream(f);
		 if (isExcel2003(fos)) {
			 try { fos.close(); } catch (Exception e) { }
			 return EXCEL2003;
		 }
		 fos = new FileInputStream(f);
		 if (isExcel2007(fos)) {
			 try { fos.close(); } catch (Exception e) { }
			 return EXCEL2007;
		 }
		return EXCEL_NOT;
	 }
	  private static boolean isExcel2003(FileInputStream fos) {
		  Workbook wb = null;
		  try {
			  wb = new HSSFWorkbook(fos);
		  } catch (Exception e) {  
			  try {
				fos.close();
			} catch (IOException e1) { }
		  }
		  if (wb != null) 
			  return true;
		  return false;
	 }
	 public static boolean isExcel2007(FileInputStream fos) {
		 Workbook wb = null;
		try {
			wb = new XSSFWorkbook(fos);
		} catch (Exception e) { 
			try {
				fos.close();
			} catch (IOException e1) { }
		}
		if (wb != null) 
			 return true;
		return false;
	 }
	 /**
	  * 读取excel文件获得Workbook对象,默认聚焦在第一个sheet上
	  * 这个使用的时候要用try块包围
	  * 能读取返回true，不然返回false
	  * @param imputfilename
	  * @param excel2007 是否是2007版excel，true：是
	 * @throws FileNotFoundException 
	  */
	 public boolean openExcel(String imputfilename,boolean excel2007) {
		 filename=imputfilename;
		 if (!FileOperate.isFileExist(imputfilename)) {
			 return newExcelOpen(imputfilename, excel2007);
		 }
		 try {
			versionXls = isExcelVersion(filename);
		} catch (Exception e) {
			versionXls = EXCEL_NO_FILE;
		}
		 return initialExcel();
	 }
	 
	 private boolean initialExcel() {
		try {
			return resetExcelExp();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	 }
	 
	 private boolean resetExcelExp() throws Exception {
		 if (versionXls != EXCEL2003 && versionXls != EXCEL2007)
			 return false; 
		 
		 File f = new File(filename);
		 if (!FileOperate.isFileExist(filename)) {
			return false;
		}
		 FileInputStream fos = new FileInputStream(f);
		 if (versionXls == EXCEL2003)
			  wb= new HSSFWorkbook(fos);
		 else if (versionXls == EXCEL2007)
			  wb= new XSSFWorkbook(fos);
		 
		  sheet = wb.getSheetAt(0);
		  sheetNum = 0;
	 	  fos.close();
    	  return true;
	 }
	 /**
	  * 默认新建03版excel
	  * @param filenameinput
	  * @return
	  */
	 public boolean newExcelOpen(String filenameinput) {
		 return newExcelOpen(filenameinput,false);
	 }
	 
	 public boolean newExcelOpen(String filenameinput, boolean excel2007) {
		filename = filenameinput;
		if (!excel2007) {
			wb = new HSSFWorkbook();
			versionXls = EXCEL2003;
		} else {
			wb = new XSSFWorkbook();
			versionXls = EXCEL2007;
		}
		return true;
	}

	// ///////////////////////excel的各个属性，包括sheet数目，某sheet下的行数///////////////////////
	 /**
	  * 返回sheet表数目,为实际sheet数目
	  * @return int
	  */
	 public int getSheetCount() {
		 int sheetCount = -1;
		 sheetCount =  wb.getNumberOfSheets();//这里获得的是实际的sheet数
		 return sheetCount;
	 }

	 /**
	  * 获得默认sheetNum下的记录行数,为实际行数
	  * @return int 实际行数，如果没有行，则返回1
	  */
	 public int getRowCount() {
		 return getRowCount(this.sheetNum+1);//这里获得的row数比实际少一，所以补上
	 }

	 /**
	  * 获得指定sheetNum的rowCount,为实际行数
	  * @param sheetNum,sheet数，为实际sheet数
	  * @return 实际行数，如果没有行，则返回1
	  */
	 public int getRowCount(int sheetNum) {
		 sheetNum--;
		 if (wb == null)
			 return 0;
		
		 Sheet sheet = wb.getSheetAt(sheetNum);
		 if (sheet == null) {
			 return 0;
		 }
		 int rowCount = -1;
		 rowCount = sheet.getLastRowNum()+1;
		 return rowCount;
	 }
	 /**
	  * 获得默认sheetNum的前20行最长的列数
	  * @param sheetNum 指定实际sheet数
	  * @param rowNum 指定实际行数
	  * @return 返回该行列数,如果该行不存在，则返回0
	  */
	 public int getColCount() {
		 int maxColNum = 0;
		 for (int i = 0; i < 20; i++) {
			 int tmpColCount = getColCount(this.sheetNum+1,i);
			if (tmpColCount > maxColNum) 
				maxColNum = tmpColCount;
		}
		return maxColNum;
	 }
	 
	 /**
	  * 获得默认sheetNum的前20行最长的列数
	  * @param sheetNum 指定实际sheet数
	  * @param rowNum 指定实际行数
	  * @return 返回该行列数,如果该行不存在，则返回0
	  */
	 public int getColCountSheet(int sheet) {
		 int maxColNum = 0;
		 for (int i = 0; i < 20; i++) {
			 int tmpColCount = getColCount(sheet,i);
			if (tmpColCount > maxColNum) 
				maxColNum = tmpColCount;
		}
		return maxColNum;
	 }
	 /**
	  * 获得第一个sheetNum的第rowNum行的列数
	  * @param sheetNum 指定实际sheet数
	  * @param rowNum 指定实际行数
	  * @return 返回该行列数，如果该行不存在，则返回0
	  */
	 public int getColCount(int rownum) {    
		 return getColCount(1,rownum);
	 }
	 /**
	  * 获得指定sheetNum的rowNum下的列数
	  * @param sheetNum 指定实际sheet数
	  * @param rowNum 指定实际行数
	  * @return 返回该行列数，如果该行不存在，则返回0
	  */
	 public int getColCount(int sheetNum,int rowNum) {
		 rowNum--; sheetNum--;
	   if (wb == null)
		 return 0;
	   Sheet sheet = wb.getSheetAt(sheetNum);
	   if (sheet == null)
		   return 0;
	   Row row=sheet.getRow(rowNum);
	   if (row == null)
		   return 0;
	   
	   int ColCount = -1;
	   ColCount = row.getLastCellNum();
	   return ColCount;
	 }
	 /**
	  * 读取默认sheet的指定块的内容,如果中间有空行，则一并读取<br/>
	  *直接指定标准的行数和列数，从1开始计数，不用从0起<br/>
	  *但是最后获得的数组计数是从0开始的，不同于C#<br/>
	  * @param rowStartNum：起点实际行数<br/> 
	  * @param columnStartNum：起点实际列数<br/> 
	  * @param rowEndNum：终点实际行数，小于等于0则读取到尾部<br/> 
	  * @param columnEndNum：终点实际列数，小于等于0则读取到尾部<br/>
	  * 如果行数超过文件实际行数，则多出来的数组设置为null<br/>
	  * @return String[][]<br/>
	  */
	 public ArrayList<String[]> ReadLsExcel(int rowStartNum, int rowEndNum, int[] columnNum) {
		 if (sheet != null) {
			 sheetNum = wb.getSheetIndex(sheet);
		 }
		 return ReadLsExcel(this.sheetNum+1, rowStartNum, rowEndNum, columnNum);
	 }
	 /**
	  * 读取指定块的内容,同时将焦点放到该sheet上,返回arrayList如果中间有空行，则跳过<br/>
	  *指定待读取sheet名称，标准的行数和列数，从1开始计数，不用从0起<br/>
	  *但是最后获得的数组计数是从0开始的，不同于C#<br/>
	  * @param sheetName：待读取sheet名字<br/> 
	  * @param rowStartNum：起点实际行数<br/> 
	  * @param columnStartNum：起点实际列数<br/> 
	  * @param rowEndNum：终点实际行数，小于等于0则读取到尾部<br/> 
	  * @param columnEndNum：终点实际列数，小于等于0则读取到尾部<br/>
	  * 如果行数超过文件实际行数，则多出来的数组设置为null<br/>
	  * @return String[][]<br/>
	  */
	 public ArrayList<String[]> ReadLsExcel(String sheetName, int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) {
		 sheetNum=wb.getSheetIndex(sheetName);
		 if (sheetNum < 0) {
			 sheetNum = 0;
		 }
		 sheet = wb.getSheetAt(sheetNum);
		 return  ReadLsExcel(sheetNum+1,  rowStartNum,  columnStartNum,  rowEndNum,  columnEndNum);
	 }
	 
	 
	 /**
	  * 读取指定块内容,返回arrayList,如果中间有空行，则一并读取<br/>
	  * 指定工作表，起始的行数，列数，终止行数，列数<br/>
	  * 直接指定标准的sheet数，行数和列数，从1开始计数，不用从0起<br/>
	  * 但是最后获得的数组计数是从0开始的，不同于C#<br/>
	  * @param sheetNum：实际sheet数<br/>
	  * @param rowStartNum：起点实际行数<br/> 
	  * @param columnStartNum：起点实际列数<br/> 
	  * @param rowEndNum：终点实际行数，小于等于0则读取到尾部<br/> 
	  * @param columnEndNum：终点实际列数，小于等于0则读取到尾部<br/>
	  * 如果行数超过文件实际行数，则多出来的数组设置为null<br/>
	  * @return String[]
	  */
	//读取一块excel，每次读一行,循环读
	 public ArrayList<String[]> ReadLsExcel(int sheetNum, int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) {
		 //修正输入的行数和列数的问题
		 if (rowEndNum <= 0)
			 rowEndNum = getRowCount(sheetNum);
		 if (columnEndNum <= 0)
			columnEndNum = getColCountSheet(sheetNum);
		 
		sheetNum--;rowStartNum--;columnStartNum--;rowEndNum--;columnEndNum--;
		
		if (sheetNum < 0)
			sheetNum = wb.getSheetIndex(sheet);
		if (rowStartNum < 0)
			rowStartNum = 0;
		
		int[] readColumn = new int[columnEndNum - columnStartNum + 1];
		for (int readColNum = columnStartNum; readColNum <= columnEndNum; readColNum++) {
			readColumn[readColNum] = readColNum;
		}
		
		return ReadLsExcelDetail(sheetNum, rowStartNum, rowEndNum, readColumn);
	 }
	 /**
	  * 读取指定块的内容,同时将焦点放到该sheet上,返回arrayList如果中间有空行，则跳过<br/>
	  *指定待读取sheet名称，标准的行数和列数，从1开始计数，不用从0起<br/>
	  *但是最后获得的数组计数是从0开始的，不同于C#<br/>
	  * @param rowStartNum 起点实际行数<br/> 
	  * @param columnStartNum 起点实际列数<br/> 
	  * @param rowEndNum 终点实际行数，小于等于0则读取到尾部<br/> 
	  * @param columnEndNum 终点实际列数，小于等于0则读取到尾部<br/>
	  * 如果行数超过文件实际行数，则多出来的数组设置为null<br/>
	  * @return String[][]<br/>
	  */
	 public ArrayList<String[]> ReadLsExcel(int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) {
		 if (sheet != null) {
			 sheetNum = wb.getSheetIndex(sheet);
		 }
		 return  ReadLsExcel(sheetNum+1,  rowStartNum,  columnStartNum,  rowEndNum,  columnEndNum);
	 }
	 /**
	  * 读取指定块内容,返回arrayList,如果中间有空行，则一并读取<br/>
	  * 指定工作表，起始的行数，列数，终止行数，列数<br/>
	  * 直接指定标准的sheet数，行数和列数，从1开始计数，不用从0起<br/>
	  * 但是最后获得的数组计数是从0开始的，不同于C#<br/>
	  * @param sheetNum：实际sheet数<br/>
	  * @param rowStartNum：起点实际行数<br/> 
	  * @param columnStartNum：起点实际列数<br/> 
	  * @param rowEndNum：终点实际行数，小于等于0则读取到尾部<br/> 
	  * @param columnEndNum：终点实际列数，小于等于0则读取到尾部<br/>
	  * 如果行数超过文件实际行数，则多出来的数组设置为null<br/>
	  * @return String[]
	  */
	//读取一块excel，每次读一行,循环读
	 public ArrayList<String[]> ReadLsExcel(int sheetNum, int rowStartNum, int rowEndNum, int[] readColNum) {
		 //修正输入的行数和列数的问题
		 if (rowEndNum <= 0)
			 rowEndNum = getRowCount(sheetNum);
		 
		sheetNum--; rowStartNum--; rowEndNum--;
		
		if (sheetNum < 0)
			sheetNum = wb.getSheetIndex(sheet);
		if (rowStartNum < 0)
			rowStartNum = 0;
	
		int[] readColumn = new int[readColNum.length];
		for (int i = 0; i < readColumn.length; i++) {
			readColumn[i] = readColNum[i] - 1;
		}
		
		return ReadLsExcelDetail(sheetNum, rowStartNum, rowEndNum, readColumn);
	 }
	 /**
	  * @param sheetNum
	  * @param rowStartNum
	  * @param rowEndNum
	  * @param readColNum 如果出现负数的colNum，则跳过
	  * @return
	  */
	 private ArrayList<String[]> ReadLsExcelDetail(int sheetNum, int rowStartNum, int rowEndNum, int[] readColNum) {
		 ArrayList<String[]> LsExcelLine = new ArrayList<String[]>();
		 sheet = wb.getSheetAt(sheetNum);
		 Row row = sheet.getRow(rowStartNum);
		 
		 readColNum = ArrayOperate.removeSmallValue(readColNum, 0);
		 
		 for (int readLines = rowStartNum; readLines <= rowEndNum; readLines++) {
			 row = sheet.getRow(readLines);
			 String[] tmpLine = new String[readColNum.length];
			 if (row == null) {
				 for (int j = 0; j < tmpLine.length; j++) {
					 tmpLine[j] = "";
				 }
				 LsExcelLine.add(tmpLine);
				 continue;
			 }
			 for (int j = 0; j < readColNum.length; j++) {
				 Cell cell = row.getCell((readColNum[j]));
				 tmpLine[j] = getCellInfo(cell);
			 }
			 LsExcelLine.add(tmpLine);
		 }
		 return LsExcelLine;
	 }
	 /**
	  *  读取单个内容，默认工作表，指定行、列。
	  *  所有指定的行号和列号都只要真实编号，不需要减去1
	  * @param rowNum
	  * @param cellNum
	  * @return String
	  */
	 public String ReadExcel(int rowNum, int cellNum) {
		 return  ReadExcel(this.sheetNum+1, rowNum, cellNum);
	 }
	 /**
	  * 读取单个内容，指定工作表sheetNum、行、列。
	  * 所有指定的Sheet编号，行号和列号都只要真实编号，不需要减去1
	  * @param sheetNum
	  * @param rowNum
	  * @param cellNum
	  * @return String
	  */
	 public String ReadExcel(int sheetNum, int rowNum, int cellNum) {
		 sheetNum--;rowNum--;cellNum--;
		 if (sheetNum < 0) {
			 sheetNum = wb.getSheetIndex(sheet);
		 }
		 if (rowNum < 0)
			 return "";
		 sheet = wb.getSheetAt(sheetNum);
		 Row row = sheet.getRow(rowNum);
		 Cell cell = row.getCell(cellNum);
		 return getCellInfo(cell);
	 }
	 
	 private String getCellInfo(Cell cellExcel) {
		 String result = "";
		 if (cellExcel != null) { // add this condition
			 switch (cellExcel.getCellType()) {
			 case Cell.CELL_TYPE_FORMULA:
				 result = getExcelNumeric(cellExcel.getNumericCellValue());
				 break;
			 case Cell.CELL_TYPE_NUMERIC:  //如果单元格里的数据类型为数据  
				 result = getExcelNumeric(cellExcel.getNumericCellValue());
				 break;
			 case Cell.CELL_TYPE_STRING:
				 result = cellExcel.getStringCellValue().trim();
				 break;
			 case Cell.CELL_TYPE_BOOLEAN://如果单元格里的数据类型为 Boolean                     
				 result = String.valueOf(cellExcel.getBooleanCellValue()).trim();
				 break;
			 case Cell.CELL_TYPE_BLANK:
				 result = "";
				 break;
			 default:
				 result = "error";
				 break;
			 }
		 }
		 return result;
	 }
	 /** 将excel中获得的数字转化为字符串，根据是否有小数点，进行转化 */
	 private String getExcelNumeric(double value) {
		 if (value == Math.ceil(value)) {
			 Long result = (long) value;
			 return result + "";
		 }
		 return value + "";
	 }
	 /**
	  * 默认保存
	  * 单个数值写入单个excel文件,默认写入sheet1,写入其他sheet不改变exceloperate焦点
	  * 设置写入行数，列数和内容，写入的内容默认为String
	  * 其中行数，列数，都为实际数目，不用减去1
	  * @param rowNum
	  * @param cellNum
	  * @param content
	  */
	 public boolean WriteExcel(int rowNum, int cellNum,String content) {
		 return WriteExcel(null, 1,rowNum, cellNum,content);
	 }
 
	 /**
	     * 块文件写入excel文件，并设定sheetName，如果没有该sheetName，那么就新建一个
	     * 设置写入的sheet名字，行数，列数和内容，写入的内容默认为String[][]
	     * String[][]中的null会自动跳过
	     * 其中sheet数，行数，列数，都为实际数目，不用减去1
	     * 当sheetNum设置超出已存在sheet数目时，则为新建sheet写入
	     * @param sheetName
	     * @param rowNum
	     * @param cellNum
	     * @param content
		 */
	 public boolean WriteExcel(String sheetName, int rowNum, int cellNum,List<String[]> content) {
		 return WriteExcel(sheetName, -1, rowNum, cellNum, content);
	 }
	 /**
	  * 块文件写入excel文件
	  * 设置写入的sheet数，行数，列数和内容，写入的内容默认为List<String[]>,其中String[]为行，list.get(i)为列
	  * String[]中的null会自动跳过
	  * 其中sheet数，行数，列数，都为实际数目，不用减去1
	  * 当sheetNum设置超出已存在sheet数目时，则为新建sheet写入
	  * @param rowNum 实际行
	  * @param cellNum 实际列
	  * @param content
	  */
	 public boolean WriteExcel(int rowNum, int cellNum, List<String[]> content) {
		return WriteExcel(null, 1, rowNum, cellNum, content);
	}
	 /**
	  * 块文件写入excel文件
	  * 设置写入的sheet数，行数，列数和内容，写入的内容默认为List<String[]>,其中String[]为行，list.get(i)为列
	  * String[]中的null会自动跳过
	  * 其中sheet数，行数，列数，都为实际数目，不用减去1
	  * 当sheetNum设置超出已存在sheet数目时，则为新建sheet写入
	  * @param sheetNum
	  * @param rowNum
	  * @param cellNum
	  * @param content
	  */
	 public boolean WriteExcel(int sheetNum, int rowNum, int cellNum, List<String[]> content) {
		return WriteExcel(null, sheetNum, rowNum, cellNum, content);
	}
	 /**
	  * 单个元素写入excel文件
	  * 设置写入的sheet数或sheetName，两个只要设置一个，默认先设定sheetName
	  * 行数，列数和内容，写入的内容默认为List<String[]>,其中String[]为行，list.get(i)为列
	  * String[]中的null会自动跳过
	  * 其中sheet数，行数，列数，都为实际数目，不用减去1
	  * 当sheetNum设置超出已存在sheet数目时，则为新建sheet写入
	  * @param sheetNum
	  * @param sheetName
	  * @param rowNum 实际行
	  * @param cellNum 实际列
	  * @param content
	  * @return
	  */
	 private boolean WriteExcel(String sheetName ,int sheetNum, int rowNum, int cellNum, String content) {
		initialExcel(); 
		if ((sheetNum <= -1 && sheetName == null) || rowNum < 0)
			return false;

		Sheet sheet = getSheet(sheetName, sheetNum);
		writeExcel(sheet, rowNum, cellNum, content);
		if (filename != "")
			Save();
		return true;
	}
	 /**
	  * 块文件写入excel文件
	  * 设置写入的sheet数或sheetName，两个只要设置一个，默认先设定sheetName
	  * 行数，列数和内容，写入的内容默认为List<String[]>,其中String[]为行，list.get(i)为列
	  * String[]中的null会自动跳过
	  * 其中sheet数，行数，列数，都为实际数目，不用减去1
	  * 当sheetNum设置超出已存在sheet数目时，则为新建sheet写入
	  * @param sheetNum
	  * @param sheetName
	  * @param rowNum 实际行
	  * @param cellNum 实际列
	  * @param content
	  * @return
	  */
	 private boolean WriteExcel(String sheetName ,int sheetNum, int rowNum, int cellNum, List<String[]> content) {
		initialExcel();
		if ((sheetNum <= -1 && sheetName == null) || rowNum < 0)
			return false;

		Sheet sheet = getSheet(sheetName, sheetNum);
		writeExcel(sheet, rowNum, cellNum, content);
		if (filename != "")
			Save();
		return true;
	}
	 /**
	  * 设置写入的sheet数或sheetName，两个只要设置一个，默认先设定sheetName
	  * @param sheetName 没有设为null
	  * @param sheetNum 没有设为小于1
	  * @return
	  */
	private Sheet getSheet(String sheetName, int sheetNum) {
		sheetNum--;
		Sheet sheet = null;
		if (sheetName != null) {
			sheet = wb.getSheet(sheetName);
			if (sheet == null) {
				sheet = wb.createSheet(sheetName);
				sheetNum = wb.getSheetIndex(sheetName);
			}
		} else if (sheetNum >= 0) {
			try {
				sheet = wb.getSheetAt(sheetNum);
			} catch (Exception e) {
				// TODO: handle exception
			}
			if (sheet == null) {
				sheet = wb.createSheet("sheet" + (getSheetCount() + 1));// 新建sheet
			}
		}
		return sheet;
	}
	/**
	 * 写入单个元素
	 * @param sheet
	 * @param rowNum 实际行
	 * @param cellNum 实际列
	 * @param content
	 * @return
	 */
	private boolean writeExcel(Sheet sheet, int rowNum, int cellNum, String content) {
		rowNum--;
		cellNum--;// 将sheet和行列都还原为零状态
		if (rowNum < 0)
			return false;
		try {
			// row = sheet.createRow(rowNum);
			Row row = sheet.getRow(rowNum);
			if (row == null) {
				row = sheet.createRow(rowNum);
			}
			Cell cell = row.createCell((short) cellNum);
			try {
				double tmpValue = Double.parseDouble(content);
				// cell.setCellType(0);
				cell.setCellValue(tmpValue);
			} catch (Exception e) {
				cell.setCellValue(content);
			}
			if (filename != "")
				Save();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	 /**
	  * 写入list等
	  * @param sheet
	  * @param rowNum 实际行
	  * @param cellNum 实际列
	  * @param content
	  * @return
	  */
	private boolean writeExcel(Sheet sheet, int rowNum, int cellNum, Iterable<String[]> content) {
		rowNum--;
		cellNum--;// 将sheet和行列都还原为零状态
		if (rowNum < 0)
			return false;
		try {
			int i = 0;
			for (String[] rowcontent : content) {
				int writerow = i + rowNum;// 写入的行数
				Row row = sheet.getRow(writerow);
				if (row == null) {
					row = sheet.createRow(writerow);
				}
				if (rowcontent == null)
					continue;
				for (int j = 0; j < rowcontent.length; j++) // 写入
				{
					if (rowcontent[j] == null)
						continue; // 跳过空值
					Cell cell = row.createCell((short) (cellNum + j));
					try {
						double tmpValue = Double.parseDouble(rowcontent[j]);
						// cell.setCellType(0);
						cell.setCellValue(tmpValue);
					} catch (Exception e) {
						cell.setCellValue(rowcontent[j]);
					}
				}
				i ++;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	 /**
	     * 条文件写入excel文件，
	     * 设置写入的sheet数，行数，列数和内容，写入的内容默认为String[],设定写入行/列
	     * String[]中的null会自动跳过
	     * 其中sheet数，行数，列数，都为实际数目，不用减去1
	     * 当sheetNum设置超出已存在sheet数目时，则为新建sheet写入
	     * @param sheetNum
	     * @param rowNum
	     * @param cellNum
	     * @param content
	     * @param raw true为写入某一行，设定false为写入某一列
		 */
	 public boolean WriteExcel(boolean save,int sheetNum, int rowNum, int cellNum, String[] content, boolean raw) {
		 initialExcel();
		 	sheetNum--;rowNum--;cellNum--;//将sheet和行列都还原为零状态
		 	int writeNumber=content.length;//这个就是数组第一维的数量
	    	 if (sheetNum < -1 || rowNum < 0)
	    		   return false;
	    	 try {
	    		 try {
	    			 sheet=wb.getSheetAt(sheetNum);  						 
	    		 } 
	    		 catch (Exception e) {
	    			 sheet=wb.createSheet("sheet"+(getSheetCount()+1));//新建sheet					 
	    		 }
	    		  
	    		 if(raw==true) {//横着写入一行
	    			 Row row=sheet.getRow(rowNum);
	    			 if(row==null) {
	    				 row=sheet.createRow(rowNum); 
	    			 }
	    			 for(int i=0;i<writeNumber;i++) {
	    				 String WriteContent=content[i];
	    				 if(WriteContent==null) continue;
	    				 Cell cell=row.createCell((short)(cellNum+i));
	     				 try {
								double tmpValue = Double.parseDouble(WriteContent);
								//cell.setCellType(0);
								cell.setCellValue(tmpValue);
						} catch (Exception e) {
							cell.setCellValue(WriteContent);
						}
	    			 }
	    		 }
	    		 else {
	    			 for(int i=0;i<writeNumber;i++) {
	    				 Row row=sheet.getRow(rowNum+i);
	    				 if(row==null) {
	    					 row=sheet.createRow(rowNum+i); 
	    				 }
	    				 String WriteContent=content[i];
	    				 if(WriteContent==null) continue;
	    				 Cell cell=row.createCell((short)(cellNum));
	     				 try {
								double tmpValue = Double.parseDouble(WriteContent);
								//cell.setCellType(0);
								cell.setCellValue(tmpValue);
						} catch (Exception e) {
							cell.setCellValue(WriteContent);
						}
	    			 }
	    		 }
	    		 if(filename!=""&&save)	Save();
	    		 return true;
	    	 }
	    	 catch (Exception e) {
	    		 e.printStackTrace();
	    		 return false;
	    	 }
	 }
	 
	 /**
	     * 条文件写入excel文件，
	     * 设置写入的sheet数，行数，列数和内容，写入的内容默认为List<String>,设定写入行/列
	     * String[]中的null会自动跳过
	     * 其中sheet数，行数，列数，都为实际数目，不用减去1
	     * 当sheetNum设置超出已存在sheet数目时，则为新建sheet写入
	     * @param sheetNum
	     * @param rowNum
	     * @param cellNum
	     * @param content
	     * @param raw true为写入某一行，设定false为写入某一列
		 */
	 public boolean WriteExcel(boolean save,int sheetNum, int rowNum, int cellNum, List<String> content, boolean raw) {
		 initialExcel();
		 	sheetNum--;rowNum--;cellNum--;//将sheet和行列都还原为零状态
		 	int writeNumber=content.size();//这个就是数组第一维的数量
		 	boolean flag;
	    	 if (sheetNum < -1 || rowNum < 0)
	    		   return false;
	    	 try {
	    		 try {
	    			 sheet=wb.getSheetAt(sheetNum);  						 
	    		 } 
	    		 catch (Exception e) {
	    			 sheet=wb.createSheet("sheet"+(getSheetCount()+1));//新建sheet					 
	    		 }
	    		  
	    		 if(raw==true)//横着写入一行
	    		 {	
	    			 Row row=sheet.getRow(rowNum);
	    			 if(row==null) {
	    				 row=sheet.createRow(rowNum); 
	    			 }
	    			 for(int i=0;i<writeNumber;i++) {
	    				 String WriteContent=content.get(i);
	    				 if(WriteContent==null) continue;
	    				 Cell cell=row.createCell((cellNum+i));
	    				 try {
								double tmpValue = Double.parseDouble(WriteContent);
								//cell.setCellType(0);
								cell.setCellValue(tmpValue);
						} catch (Exception e) {
							cell.setCellValue(WriteContent);
						}
	    			 }
	    		 }
	    		 else {
	    			 for(int i=0;i<writeNumber;i++) {
	    				 Row row=sheet.getRow(rowNum+i);
	    				 if(row==null) {
	    					 row=sheet.createRow(rowNum+i); 
	    				 }
	    				 String WriteContent=content.get(i);
	    				 if(WriteContent==null) continue;
	    				 Cell cell=row.createCell((cellNum));
	     				 try {
								double tmpValue = Double.parseDouble(WriteContent);
								//cell.setCellType(0);
								cell.setCellValue(tmpValue);
						} catch (Exception e) {
							cell.setCellValue(WriteContent);
						}
	    			 }
	    		 }
	    		 if(filename!=""&&save)	Save();
	    		 return true;
	    	 }
	    	 catch (Exception e) {
	    		 e.printStackTrace();
	    		 return false;
	    	 }
	 }
	 
///////////////////////保存文件方法/////////////////////////////////////	 
	    /**
	     * 保存excel文件，使用以前的文件名。有重载
		 */
	 public boolean Save() {
		 if(filename=="") return false;
		 try {
			 FileOutputStream out = new FileOutputStream(filename);
			 wb.write(out);
			 out.close();
			 return true;
		 } catch (Exception e) {
			 // TODO: handle exception
			 e.printStackTrace();
			 return false;
		}
	}
	     /**
	      * 输入文件名
	     * 保存excel文件，另存为
		 */
	 public boolean Save(String newfilename) {
		 try {
			 FileOutputStream out = new FileOutputStream(newfilename);
			    wb.write(out);
			    out.close();
			    return true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return false;
		}
	}
///////////////////关闭对象////////////////////////////////
	 /**
	  * 暂时没功能
	  */
	 public void Close() {//暂时不会
		 wb = null;// book [includes sheet]
		 sheet = null;
	 }
}

	 
 
