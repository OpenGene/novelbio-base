package com.novelbio.base.dataOperate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.novelbio.base.dataOperate.ExcelStyle.EnumXlsCellBorder;
import com.novelbio.base.fileOperate.FileOperate;

import junit.framework.TestCase;

public class TestExcelOperate extends TestCase {
	
	public void testWriteTestExcel() {
		ExcelOperate excelOperate = new ExcelOperate();
		excelOperate.openExcel("/home/novelbio/jpx/test/test.xls", true);
		List<String[]> lsData = new ArrayList<>();
		String[] data = new String[4];
		data[0] = "one";
		data[1] = "two";
		data[2] = "three";
		data[3] = "four";
		lsData.add(data);
		lsData.add(data);
		lsData.add(data);
		lsData.add(data);
		lsData.add(data);
		lsData.add(data);
		lsData.add(data);
		lsData.add(data);
		lsData.add(data);
//		excelOperate.WriteExcel(1, 1, lsData);
		int endRowNum = 7;
		ExcelStyle style = ExcelStyle.getThreeLineTable(1, endRowNum);
		excelOperate.WriteExcel(1, 1, lsData, style);
		excelOperate.close();
		assertEquals(true, FileOperate.isFileExist("/home/novelbio/jpx/test/test.xls"));
	}
	
	public void testCreateExecl(){
		//需写入的excel文件所在文件夹必须是存在的.不然就不会写入文件.
		String tempFilePath = "/home/novelbio/jpx/test/abc/1234.xls";
		ExcelOperate excelOperate= new ExcelOperate();
		excelOperate.openExcel(tempFilePath);
		File file = new File(tempFilePath);
		assertFalse(FileOperate.isFileExist(tempFilePath));
	}

	public void testReportExcel(){
		//测试数据是否能够写入文件
		String tempFilePath = "/home/novelbio/jpx/test/123.xls";
		List<String[]> lsExcelData = new ArrayList<>();
		String[] data = new String[]{"one","two","three","four"};
		lsExcelData.add(data);
		
		ExcelOperate excelOperate = null;
		try {
			excelOperate = new ExcelOperate();
			excelOperate.openExcel(tempFilePath);
			excelOperate.setNBCExcel(true);
			// excelOperate.setNewFile(true);
			excelOperate.WriteExcel(1, 1, lsExcelData);

		} finally {
			if (excelOperate != null) {
				excelOperate.close();
			}
		}
		
		assertTrue(FileOperate.getFileSizeLong(tempFilePath) > 0);

	}
	
}
