package com.novelbio.base.dataOperate;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.novelbio.base.SepSign;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.ExceptionNbcFile;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * <b>本类需要实例化才能使用</b><br> 
 * 读取excel文件,注意读取前最好将excel的所有格式全部清除，不然可能会有问题.<br>
 * 清除方法，将excel拷贝入txt文件，再拷贝回来 可以跨平台使用 读取速度可以<br>
 * 本类似乎无法获得最大列的数目.<br>
 * 本代码原始作者 caihua ，
 * Zong Jie修改
 * modify by fans.fan 151203 重构.
 */
public class ExcelOperate implements Closeable {
	
	private static final  Logger logger = LoggerFactory.getLogger(ExcelOperate.class);
	
	/** 是excel2003 */
	public static final int EXCEL2003 = 2003;
	/** 是excel2007 */
	public static final int EXCEL2007 = 2007;
	/** 不是excel */
	public static final int EXCEL_NOT = 100;
	/** 文件不存在 */
	public static final int EXCEL_NO_FILE = 0;
	
	/** 03格式的excel后缀 */
	public static final String EXCEL03_SUFFIX = "xls";
	/** 07格式的excel后缀 */
	public static final String EXCEL07_SUFFIX = "xlsx";
	
	/** 在写入excel的时候，为了方便未来快速读取excel，会把每个sheet的内容再写入一个文本
	 * 那么这个txt有个格式，譬如excel为 /home/novelbio/myexcel.xls
	 * 则会把txt写入为 /home/novelbio/.tmptxt/myexcel@@sheetname.txt
	 */
	@VisibleForTesting
	protected static final String TMP_TXT_PATH = ".tmptxt";
	
	private Workbook wb;
	private Sheet sheet;
	private String filename = "";
	
	private boolean isWriteSheetToTxt = false;
	/** 
	 * excel 2003 或者 excel 2007 .0代表文件不存在.
	 */
	private int version = 0;
	
	/**
	 * 判断是否为excel
	 * <br/>
	 * @return 
	 * TODO 对excel07格式,文件较大时,判断速度比较慢.实测4.1M的excel.判断需4秒左右
	 */
	public static boolean isExcel(String filename) {
		try {
			int fileVersion = isExcelVersion(filename);
			return  fileVersion == EXCEL2003 || fileVersion == EXCEL2007;
		} catch (Exception e) {
		}
		return false;
	}
	
	/**
	 * 简单判断文件是否是excel.不严谨.
	 * <br/>
	 * 判断逻辑: 1.文件后缀名是否是xls或xlsx.
	 * 			2.文件如果以文本形式读取,第一行有asc码在0-9时,就不是普通文本.应该就是excel了.
	 * 2016年3月30日
	 * novelbio fans.fan
	 * @param filename
	 * @return
	 */
	public static boolean isExcelSimple(String filename) {
		String suffix = FileOperate.getFileSuffix(filename);
		if (!EXCEL03_SUFFIX.equals(suffix) && !EXCEL07_SUFFIX.equals(suffix)) {
			return false;
		}
		
		InputStream is = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		try {
			is =  FileOperate.getInputStream(filename);	
			inputStreamReader = new InputStreamReader(is);
			bufferedReader = new BufferedReader(inputStreamReader);
			String str = bufferedReader.readLine();
			byte[] bytes = str.getBytes();
			for (int i = 0; i < bytes.length; i++) {
				if (bytes[i] >= 0 && bytes[i] < 9) {
					// 如果有ascii有0-9的,肯定不是普通文本了
					return true;
				}
			}
		} catch (Exception e) {
		} finally {
			FileOperate.close(is);
			FileOperate.close(inputStreamReader);
			FileOperate.close(bufferedReader);
		}
		return false;
	}
	

	/**
	 * 判断是否为excel2003或2007
	 * 
	 * @return 2003 excel2003<br> 
	 * 			 2007 excel2007<br>
	 * 			 100 不是excel<br> 
	 * 			 0 文件不存在
	 * @throws IOException 
	 */
	private static int isExcelVersion(String filename) throws IOException  {
//		if (!FileOperate.isFileExist(filename))
//			return EXCEL_NO_FILE;
//		
		
		String suffix = FileOperate.getFileSuffix(filename);
		InputStream is =  FileOperate.getInputStream(filename);
		if (EXCEL03_SUFFIX.equals(suffix) && isExcel2003(is)) {
			FileOperate.close(is);
			return EXCEL2003;
		} else if (EXCEL07_SUFFIX.equals(suffix) && isExcel2007(is)) {
			FileOperate.close(is);
			return EXCEL2007;
		}
		
		return EXCEL_NOT;
	}

	private static boolean isExcel2003(InputStream is) {
		try {
			new HSSFWorkbook(is);
			return true;
		} catch (Exception e) {
			FileOperate.close(is);
		}
		return false;
	}

	public static boolean isExcel2007(InputStream is) {
		try {
			new XSSFWorkbook(is);
			return true;
		} catch (Exception e) {
			FileOperate.close(is);
		}
		return false;
	}
	
	/**
	 * 打开excel,没有就新建excel.但指定文件所在的文件夹是必须存在的.<br>
	 * <b>使用完需调用close方法关闭相关对象</b>
	 * @param imputfilename 文件路径和名称
	 */
	public ExcelOperate(String imputfilename) {		
		String suffix = FileOperate.getFileNameSep(imputfilename)[1].toLowerCase();
		version = suffix.equals("xlsx") ? EXCEL2007 : EXCEL2003;
		initialExcel(imputfilename);
	}
	
	/**
	 * 打开excel,没有就新建excel.但指定文件所在的文件夹是必须存在的.<br>
	 * <b>使用完需调用close方法关闭相关对象</b>
	 * @param imputfilename 文件路径和名称
	 * @param isExcel2003
	 */
	public ExcelOperate(String imputfilename, boolean isExcel2003) {		
		version = isExcel2003 ? EXCEL2003 : EXCEL2007;
		initialExcel(imputfilename);
	}

	private void initialExcel(String filePathAndName) {
		filename = filePathAndName;
		if (version != EXCEL2003 && version != EXCEL2007){
			throw new ExceptionNbcExcel("excel version error.please check it. filename=" + filePathAndName);
		}
		
		try {
			if (FileOperate.isFileExist(filePathAndName)) {
				InputStream is = FileOperate.getInputStream(filename);
				if (version == EXCEL2003) {
					wb = new HSSFWorkbook(is);
				} else if (version == EXCEL2007) {
					wb = new XSSFWorkbook(is);
				}
				sheet = wb.getSheetAt(0);
				FileOperate.close(is);
			} else {
				if (version == EXCEL2003) {
					wb = new HSSFWorkbook();
				} else if (version == EXCEL2007) {
					wb = new XSSFWorkbook();
				}
			}
		} catch (Exception e) {
			logger.error("initialExcel error.", e);
			//TODO 这里主要是不想显式的往外声明抛出异常,所以改为RuntimeException,是否合适,待考虑.
			throw new ExceptionNbcExcel("initialExcel error " + filename, e);
		}
	
	}
	
	/**
	 * 是否在写入sheet的时候，同时将信息写入一个新的文本<br>
	 * 可以通过 {@link #getExcelTxtName(String, String)}来获取这个文件的文件名<br>
	 * @param isWriteSheetToTxt
	 */
	public void setWriteSheetToTxt(boolean isWriteSheetToTxt) {
		this.isWriteSheetToTxt = isWriteSheetToTxt;
	}
	
	/**
	 * 返回sheet表数目,为实际sheet数目
	 * @return int
	 */
	public int getSheetCount() {
		return wb.getNumberOfSheets();// 这里获得的是实际的sheet数
	}

	/**
	 * 获得指定sheetNum的rowCount,为实际行数
	 * 
	 * @param sheetNum 	sheet顺序数,从1开始.
	 * @return 				实际行数，如果没有行，则返回1
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
		rowCount = sheet.getLastRowNum() + 1;
		return rowCount;
	}

	/**
	 * 获得列数.<br>
	 * 获得第一个sheetNum的前20行最长的列数
	 * @return 				返回该行列数,如果该行不存在，则返回0
	 */
	public int getColCount() {
		int maxColNum = 0;
		for (int i = 0; i < 20; i++) {
			int tmpColCount = getColCount(1, i);
			if (tmpColCount > maxColNum)
				maxColNum = tmpColCount;
		}
		return maxColNum;
	}

	/**
	 * 获得默认sheetNum的前20行最长的列数
	 * 
	 * @param sheetNum 	指定实际sheet数,从1开始
	 * @return 				返回该行列数,如果该行不存在，则返回0
	 */
	public int getColCountSheet(int sheet) {
		int maxColNum = 0;
		for (int i = 0; i < 20; i++) {
			int tmpColCount = getColCount(sheet, i);
			if (tmpColCount > maxColNum)
				maxColNum = tmpColCount;
		}
		return maxColNum;
	}

	/**
	 * 获得第一个sheetNum的第rowNum行的列数
	 * 
	 * @param rowNum 		指定实际行数,从1开始
	 * @return 				返回该行列数，如果该行不存在，则返回0
	 */
	public int getColCount(int rownum) {
		return getColCount(1, rownum);
	}

	/**
	 * 获得指定sheetNum的rowNum下的列数
	 * 
	 * @param sheetNum 	指定实际sheet数,从1开始
	 * @param rowNum 		指定实际行数,从1开始
	 * @return 				返回该行列数，如果该行不存在，则返回0
	 */
	public int getColCount(int sheetNum, int rowNum) {
		rowNum--;
		sheetNum--;
		if (wb == null)
			return 0;
		Sheet sheet = wb.getSheetAt(sheetNum);
//		if (sheet == null)
//			return 0;
		Row row = sheet.getRow(rowNum);
		if (row == null)
			return 0;

		int ColCount = -1;
		ColCount = row.getLastCellNum();
		return ColCount;
	}
	
	/**
	 * 获取所有页签的名称
	 * 
	 * 2016年3月29日
	 * novelbio fans.fan
	 * @return
	 */
	public List<String> getLsSheetNames(){
		List<String> lsSheetName = new ArrayList<>();
		if (wb == null) {
			return lsSheetName;
		}
		
		int sheetCount = getSheetCount();
		for(int i = 0; i < sheetCount; i++){
			lsSheetName.add(wb.getSheetName(i));
		}
		
		return lsSheetName;
	}
	
	/**
	 * 指定sheet顺序号,读取该sheet全部内容.
	 * 
	 * @param sheetNum			sheet页顺序号,从1开始,
	 * @return
	 */
	public ArrayList<String[]> readLsExcelSheet(int sheetNum) {
		return readLsExcel(sheetNum, 1,  1,  -1, -1);
	}
	
	/**
	 * 指定sheet顺序号,读取该sheet全部内容.
	 * <br/>
	 * <b>注意:该方法是静态方法,不需要new excelOperate对象</b>
	 * <br/>
	 * 2016年3月31日
	 * novelbio fans.fan
	 * @param sheetNum			sheet页顺序号,从0开始.
	 * @return
	 */
	public static List<List<String>> readLsExcel2007SheetFast(String filePathAndName, int sheetNum) {
		InputStream is = null;
		List<List<String>> exceldata = new ArrayList<>();
		try {
			is = FileOperate.getInputStream(filePathAndName);
			
			Excel2007Reader excel07 = new Excel2007Reader();
			excel07.processOneSheet(is, sheetNum);
			exceldata = excel07.getExcelData(); 
		} catch (Exception e) {
			logger.error("readExcel error.filePathAndName=" + filePathAndName + ",sheetNum=" + sheetNum, e);
		} finally {
			FileOperate.close(is);
		}
		
		return exceldata;
	}
	
	/**
	 * 指定sheet顺序号,读取该sheet全部内容.
	 * 
	 * @param sheetNum			sheet页顺序号,从1开始,
	 * @return
	 */
	public ArrayList<String[]> readLsExcel() {
		return readLsExcel(1, 1,  1,  -1, -1);
	}
	
	/**
	 * 读取默认sheet的指定块的内容,如果中间有空行，则一并读取<br>
	 * 
	 * @param rowStartNum 		起点实际行数,从1开始<br>
	 * @param rowEndNum 		终点实际行数，小于等于0则读取到尾部<br>
	 * @param columnEndNum 		终点实际列数，小于等于0则读取到尾部<br>
	 *            						
	 * @return ArrayList<String[]><br>如果行数超过文件实际行数，则多出来的数组设置为null<br>
	 */
	public ArrayList<String[]> readLsExcel(int rowStartNum, int rowEndNum, int[] columnNum) {
		int sheetNum = 0;
		if (sheet != null) {
			sheetNum = wb.getSheetIndex(sheet);
		}
		return readLsExcel(sheetNum + 1, rowStartNum, rowEndNum, columnNum);
	}

	/**
	 * 读取指定块的内容,同时将焦点放到该sheet上,返回arrayList如果中间有空行，则跳过<br>
	 * 
	 * @param sheetName 		待读取sheet名字<br>
	 * @param rowStartNum 		起点实际行数,从1开始计数<br>
	 * @param columnStartNum 	起点实际列数,从1开始计数<br>
	 * @param rowEndNum 		终点实际行数，小于等于0则读取到尾部<br>
	 * @param columnEndNum 		终点实际列数，小于等于0则读取到尾部<br>
	 *            						
	 * @return  如果行数超过文件实际行数，则多出来的数组设置为null<br>
	 */
	public ArrayList<String[]> readLsExcel(String sheetName, int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) {
		int sheetNum = wb.getSheetIndex(sheetName);
		if (sheetNum < 0) {
			sheetNum = 0;
		}
		sheet = wb.getSheetAt(sheetNum);
		return readLsExcel(sheetNum + 1, rowStartNum, columnStartNum, rowEndNum, columnEndNum);
	}

	/**
	 * 读取指定块的内容,同时将焦点放到该sheet上,返回arrayList如果中间有空行，则跳过<br>
	 * 
	 * @param rowStartNum 		起点实际行数，从1开始计数<br>
	 * @param columnStartNum 	起点实际列数，从1开始计数<br>
	 * @param rowEndNum 		终点实际行数，小于等于0则读取到尾部<br>
	 * @param columnEndNum 		终点实际列数，小于等于0则读取到尾部<br>
	 *            						
	 * @return 如果行数超过文件实际行数，则多出来的数组设置为null<br>
	 */
	public ArrayList<String[]> readLsExcel(int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) {
		int sheetNum = 0;
		if (sheet != null) {
			sheetNum = wb.getSheetIndex(sheet);
		}
		return readLsExcel(sheetNum + 1, rowStartNum, columnStartNum, rowEndNum, columnEndNum);
	}

		
	/**
	 * 读取指定块内容,返回arrayList,如果中间有空行，则一并读取<br>
	 * 
	 * @param sheetNum 			实际sheet数,从1开始<br>
	 * @param rowStartNum 		起点实际行数，从1开始计数<br>
	 * @param columnStartNum 	起点实际列数，从1开始计数<br>
	 * @param rowEndNum 		终点实际行数，小于等于0则读取到尾部<br>
	 * @param columnEndNum 		终点实际列数，小于等于0则读取到尾部<br>
	 *            						
	 * @return 如果行数超过文件实际行数，则多出来的数组设置为null<br>
	 */
	// 读取一块excel，每次读一行,循环读
	public ArrayList<String[]> readLsExcel(int sheetNum, int rowStartNum, int rowEndNum, int[] readColNum) {
		// 修正输入的行数和列数的问题
		if (rowEndNum <= 0)
			rowEndNum = getRowCount(sheetNum);

		sheetNum--;
		rowStartNum--;
		rowEndNum--;

		if (sheetNum < 0)
			sheetNum = wb.getSheetIndex(sheet);
		if (rowStartNum < 0)
			rowStartNum = 0;

		int[] readColumn = new int[readColNum.length];
		for (int i = 0; i < readColumn.length; i++) {
			readColumn[i] = readColNum[i] - 1;
		}

		return readLsExcelDetail(sheetNum, rowStartNum, rowEndNum, readColumn);
	}

	/**
	 * 读取指定块内容,返回arrayList,如果中间有空行，则一并读取<br>
	 * 
	 * @param sheetNum 			实际sheet数，从1开始计数<br>
	 * @param rowStartNum 		起点实际行数，从1开始计数<br>
	 * @param columnStartNum 	起点实际列数，从1开始计数<br>
	 * @param rowEndNum 		终点实际行数，小于等于0则读取到尾部<br>
	 * @param columnEndNum 		终点实际列数，小于等于0则读取到尾部<br>
	 *            						如果行数超过文件实际行数，则多出来的数组设置为null<br>
	 * @return String[]
	 */
	// 读取一块excel，每次读一行,循环读
	public ArrayList<String[]> readLsExcel(int sheetNum, int rowStartNum, int columnStartNum, int rowEndNum, int columnEndNum) {
		// 修正输入的行数和列数的问题
		if (sheetNum <= 0) {
			sheetNum = 1;
		}
		
		if (rowEndNum <= 0)
			rowEndNum = getRowCount(sheetNum);
		if (columnEndNum <= 0)
			columnEndNum = getColCountSheet(sheetNum);

		sheetNum--;
		rowStartNum--;
		columnStartNum--;
		rowEndNum--;
		columnEndNum--;
		if (rowStartNum <= 0) {
			rowStartNum = 0;
		}
		if (columnStartNum <= 0) {
			columnStartNum = 0;
		}

		if (sheetNum < 0)
			sheetNum = wb.getSheetIndex(sheet);
		if (rowStartNum < 0)
			rowStartNum = 0;

		int[] readColumn = new int[columnEndNum - columnStartNum + 1];
		for (int readColNum = columnStartNum; readColNum <= columnEndNum; readColNum++) {
			readColumn[readColNum] = readColNum;
		}

		return readLsExcelDetail(sheetNum, rowStartNum, rowEndNum, readColumn);
	}
	
	/**
	 * @param sheetNum			sheet顺序号,从0开始
	 * @param rowStartNum		起始行号,从0开始
	 * @param rowEndNum			结束行号,从0开始
	 * @param readColNum 		如果出现负数的colNum，则跳过
	 * @return
	 */
	private ArrayList<String[]> readLsExcelDetail(int sheetNum, int rowStartNum, int rowEndNum, int[] readColNum) {
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
	

	private String getCellInfo(Cell cellExcel) {
		String result = "";
		if (cellExcel != null) { // add this condition
			switch (cellExcel.getCellType()) {
			case Cell.CELL_TYPE_FORMULA:
				try {
					result = getExcelNumeric(cellExcel.getNumericCellValue());
				} catch (Exception e) {
					result = cellExcel.getCellFormula();
				}
				break;
			case Cell.CELL_TYPE_NUMERIC: // 如果单元格里的数据类型为数据
				result = getExcelNumeric(cellExcel.getNumericCellValue());
				break;
			case Cell.CELL_TYPE_STRING:
				result = cellExcel.getStringCellValue().trim();
				break;
			case Cell.CELL_TYPE_BOOLEAN:// 如果单元格里的数据类型为 Boolean
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

	/** 
	 * 将excel中获得的数字转化为字符串，根据是否有小数点，进行转化 
	 */
	private String getExcelNumeric(double value) {
		if (value == Math.ceil(value)) {
			Long result = (long) value;
			return result + "";
		}
		return value + "";
	}
	
	/**
	 * 直接写一个表格
	 * @param content
	 * @return
	 * @throws IOException 
	 */
	public void writeExcelLs(List<List<String>> content) throws IOException {
		List<String[]> lsStrings = new ArrayList<String[]>();
		if (content == null) {
			content = new ArrayList<>();
		}
		for (List<String> lsList : content) {
			lsStrings.add(lsList.toArray(new String[0]));
		}
		writeExcel(lsStrings);
	}
	
	/**
	 * 从第一个sheet开始写
	 * @throws IOException 
	 */
	public void writeExcel(List<String[]> content) {
		writeExcel(1, content);
	}
	
	/**
	 * 从第一行，第一列开始写
	 * 
	 * @date 2015年12月3日
	 * @param sheetNum	实际sheet顺序号,从1开始
	 * @param content
	 */
	public void writeExcel(int sheetNum,List<String[]> content) {
		writeExcel(sheetNum, 1, 1, content);
	}

	/**
	 * 块文件写入excel文件
	 * 
	 * @param rowNum 		实际行,从1开始计数
	 * @param cellNum 		实际列,从1开始计数
	 * @param content
	 * @throws IOException 
	 */
	public void writeExcel(int rowNum, int cellNum, List<String[]> content) {
		writeExcel(1, rowNum, cellNum, content);
	}

	/**
	 * 块文件写入excel文件
	 * 
	 * @param rowNum 			实际行,从1开始计数
	 * @param cellNum 			实际列,从1开始计数
	 * @param content			内容
	 * @param style				样式
	 * @throws IOException 
	 */
	public void writeExcel(int rowNum, int cellNum, List<String[]> content, ExcelStyle style) {
		writeExcel(1, rowNum, cellNum, content, style);
	}
	
	/**
	 * 块文件写入excel文件.<br>
	 * <b>String[]中的null会自动跳过.<br>
	 * 
	 * @param sheetNum			sheet顺序号,从1开始计数.指定的sheetNum不存在,则自动新建
	 * @param rowNum			行号,从1开始计数
	 * @param colNum			列号,从1开始计数
	 * @param content			写入内容.
	 * @throws IOException 
	 */
	public void writeExcel(int sheetNum, int rowNum, int colNum, List<String[]> content) {
		writeExcel(sheetNum, rowNum, colNum, content, null);
	}
	
	/**
	 * 块文件写入excel文件，并设定sheetName，如果没有该sheetName，那么就新建一个
	 *  String[][]中的null会自动跳过.
	 * 
	 * @param sheetName			sheet名称.指定的sheetName不存在,则自动新建
	 * @param rowNum			行号,从1开始计数
	 * @param colNum			列号,从1开始计数
	 * @param content			写入内容.
	 * @throws IOException 
	 */
	public void writeExcel(String sheetName, int rowNum, int colNum, List<String[]> content) {
		writeExcel(sheetName, rowNum, colNum, content, null);
	}
	
	/**
	 *  块文件写入excel文件
	 *  
	 * @param sheetName			sheet名称.指定的sheetName不存在,则自动新建
	 * @param rowNum			行号,从1开始计数
	 * @param cellNum			列号,从1开始计数
	 * @param content			写入内容.
	 * @param style				样式
	 * @throws IOException
	 */
	public void writeExcel(String sheetName, int rowNum, int cellNum, List<String[]> content, ExcelStyle style) {
		if (StringOperate.isRealNull(sheetName) || rowNum < 0){
			throw new ExceptionNbcExcel("sheetName or rowNum error,please check. sheetName=" + sheetName + ",rowNum=" + rowNum);
		}
		Sheet sheet = getSheet(sheetName);
		writeExcel(sheet, rowNum, cellNum, content, style);
	}
	
	/**
	 * 块文件写入excel文件 
	 * 
	 * @param sheetNum			sheet顺序号,从1开始计数.指定的sheetNum不存在,则自动新建
	 * @param rowNum			行号,从1开始计数
	 * @param cellNum			列号,从1开始计数
	 * @param content			写入内容.
	 * @param style				样式
	 * @throws IOException
	 */
	public void writeExcel(int sheetNum, int rowNum, int cellNum, List<String[]> content, ExcelStyle style) {
		if (sheetNum <= -1 || rowNum < 0){
			throw new ExceptionNbcExcel("rowNum error,please check. rowNum=" + rowNum);
		}
		Sheet sheet = getSheet(sheetNum);
		writeExcel(sheet, rowNum, cellNum, content, style);
	}
	
	private boolean isWrite = false;
	
	/**
	 * 往excel写入数据.
	 * 
	 * @param sheet				sheet页
	 * @param rowNum 			实际行,从1开始
	 * @param cellNum 			实际列,从1开始
	 * @param content			写入内容
	 * @param style				样式
	 * @return
	 * @throws IOException 
	 */
	private void writeExcel(Sheet sheet, int rowNum, int cellNum, List<String[]> content, ExcelStyle style) {
		isWrite = true;
		if (content == null) {
			content = new ArrayList<>();
		}
		if (style != null) {
			style.setWorkbook(wb);
			sheet.createFreezePane(style.getFreezePaneCol(),  style.getFreezenPaneRow());
			//TODO 这里如果rowNum > 1 则需要商榷
			style.setAllLineNum(rowNum + content.size() - 1);
		}
		
		rowNum--;
		cellNum--;// 将sheet和行列都还原为零状态
		if (rowNum < 0) {
			throw new ExceptionNbcExcel("rowNum is error. rowNum=" + rowNum);
		}
		
		if (isWriteSheetToTxt) writeToTxtFile(sheet.getSheetName(), content);
		
		int i = 0;
		for (String[] rowcontent : content) {
			int writerow = i + rowNum;// 写入的行数
			Row row = sheet.getRow(writerow);
			if (row == null) {
				row = sheet.createRow(writerow);
			}
			if (rowcontent == null)
				continue;
			for (int j = 0; j < rowcontent.length; j++) {
				if (rowcontent[j] == null)
					continue; // 跳过空值
				Cell cell = row.getCell((short) (cellNum + j), Row.CREATE_NULL_AS_BLANK);
				try {
					double tmpValue = Double.parseDouble(rowcontent[j]);
					// cell.setCellType(0);
					cell.setCellValue(tmpValue);
				} catch (Exception e) {
					cell.setCellValue(rowcontent[j]);
				}
				if (style != null) {
					style.renderCell(cell, writerow, j);
				}
			}
			i++;
		}
	}
	
	private void writeToTxtFile(String sheetName, List<String[]> lsContent) {
		String txtName =getExcelTxtName(filename, sheetName);
		FileOperate.createFolders(FileOperate.getPathName(txtName));
		TxtReadandWrite txtWrite = new TxtReadandWrite(txtName, true);
		 for (String[] contents : lsContent) {
			txtWrite.writefileln(contents);
		}
		 txtWrite.close();
	}
	
	/**
	 * 将文件写到输出流里.<br>
	 * <b>注意:这个里面使用了gzip压缩.<br/>
	 * 如果OutputStream来自response.需设置	response.setHeader("Content-Encoding", "gzip");</b>
	 * 
	 * @date 2015年12月17日
	 * @param content
	 * @param outputStream
	 */
	public void writeExcel2OutputStream(List<String[]> content, OutputStream outputStream){
		writeExcel(content);
		GZIPOutputStream gzipOutputStream = null;
		try {
			gzipOutputStream = new GZIPOutputStream(outputStream);
			wb.write(gzipOutputStream);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileOperate.close(gzipOutputStream);
		}
	}

	/**
	 * 往excel写入数据.
	 * 
	 * @param sheet				sheet页
	 * @param rowNum 			实际行,从1开始
	 * @param cellNum 			实际列,从1开始
	 * @param content			写入内容
	 * @param style				样式
	 * @return
	 * @throws IOException 
	 */
	public void setRowStyle(String sheetName, int startRow, int endRow, ExcelStyle style) {
		setRowStyle(getSheet(sheetName), startRow, endRow, style);
	}
	/**
	 * 往excel写入数据.
	 * 
	 * @param sheet				sheet页
	 * @param rowNum 			实际行,从1开始
	 * @param cellNum 			实际列,从1开始
	 * @param content			写入内容
	 * @param style				样式
	 * @return
	 * @throws IOException 
	 */
	private void setRowStyle(Sheet sheet, int startRow, int endRow, ExcelStyle style) {
		if (style != null) {
			style.setWorkbook(wb);
			sheet.createFreezePane(style.getFreezePaneCol(),  style.getFreezenPaneRow());
			style.setAllLineNum(endRow - startRow + 1);
		}
		
		startRow--;
		endRow--;// 将sheet和行列都还原为零状态
		if (startRow < 0){
			throw new ExceptionNbcExcel("rowNum is error. rowNum=" + startRow);
		}
		
		for (int i = startRow; i <= endRow; i++) {
			Row row = sheet.getRow(i);
			if (row == null) {
				continue;
			}
			if (style != null) {
				style.renderRow(row, i);
			}
		}
		try {
			save();
		} catch (Exception e) {
			throw new ExceptionNbcFile("cannot save excelfile " + filename);
		}
	}
	/**
	 * 设置写入的sheetName,没有则创建.
	 * 
	 * @param sheetName 	没有设为null
	 * @return
	 */
	private Sheet getSheet(String sheetName) {
		Sheet sheet = null;
		if (sheetName != null) {
			sheet = wb.getSheet(sheetName);
			if (sheet == null) {
				sheet = wb.createSheet(sheetName);
			}
		}
		return sheet;
	}
	
	/**
	 * 根据sheet顺序号获取Sheet.没有则创建.
	 * 
	 * @param sheetNum 	默认从1开始，没有设为小于1
	 * @return
	 */
	private Sheet getSheet(int sheetNum) {
		sheetNum--;
		Sheet sheet = null;
		if (sheetNum >= 0) {
			try {
				sheet = wb.getSheetAt(sheetNum);
			} catch (Exception e) {
				sheet = wb.createSheet("sheet" + (getSheetCount() + 1));// 新建sheet
			}
		}
		return sheet;
	}
	
	/**
	 * 保存excel文件，使用以前的文件名
	 * @throws IOException 
	 */
	private void save() throws IOException {
		if ("".equals(filename)){
			throw new ExceptionNbcExcel("filename is null");
		}
		OutputStream os = null;
		try {
			/*
			 * 这里原来的参数是true.即追加写入.但测试发现.追加写入数据根本出不来.故改为false. modify by fans.fan 151203 
			os = FileOperate.getOutputStream(filename, true);
			 */
			os = FileOperate.getOutputStream(filename);
			//end by fans.fan
			wb.write(os);
		} finally{
			FileOperate.close(os);
		}
	}
	
	public void close() {
		if (isWrite) {
			try {
				save();
			} catch (Exception e) {
				throw new ExceptionNbcFile("cannot save excelfile " + filename);
			}
		}
		
		if (wb != null) {
			try {
//				wb.close();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		if (sheet != null) {
			sheet = null;
		}
	}

	/**
	 * 给定一个excel文件，返回其相关sheet的txt文件
	 * 因为我们会在写入某个sheet的时候把该sheet的内容再写入一个txt文本，
	 * <p>
	 * 譬如exel名字为  /home/novelbio/myexcel.xls
	 * 则txt为 /home/novelbio/.tmptxt/myexcel@@sheetname.txt
	 * 那么就会把这个txt全提取出来
	 * @param excelFile
	 * @return
	 */
	public static List<String> getLsSheetTxtFiles(String excelFile) {
		List<String> lsResult = new ArrayList<>();
		ExcelOperate excelOperate = new ExcelOperate(excelFile);
		List<String> lsSheetNames = excelOperate.getLsSheetNames();
		excelOperate.close();
		for (String sheetName : lsSheetNames) {
			lsResult.add(getExcelTxtName(excelFile, sheetName));
		}
		return lsResult;
	}
	
	public static String getExcelTxtName(String filename, String sheetName) {
		String excelName = FileOperate.getFileNameWithoutSuffix(filename);
		String parentPath = FileOperate.getParentPathNameWithSep(filename);
		String outTmp = parentPath + FileOperate.addSep(TMP_TXT_PATH) + excelName + SepSign.SEP_INFO_SIMPLE;
		return outTmp + sheetName + ".txt";
	}
	
}
