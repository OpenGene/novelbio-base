package com.novelbio.base.dataOperate;

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
		excelOperate.openExcel("/home/novelbio/jpx/test/test.xlsx", true);
		List<String[]> lsData = new ArrayList<String[]>();
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
		assertEquals(true, FileOperate.isFileExist("/home/novelbio/jpx/test/test.xlsx"));
	}

}
