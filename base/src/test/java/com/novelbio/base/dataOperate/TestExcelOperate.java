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
		data[0] = "一";
		data[1] = "二";
		data[2] = "三";
		data[3] = "四";
		lsData.add(data);
		lsData.add(data);
		lsData.add(data);
		lsData.add(data);
		lsData.add(data);
		lsData.add(data);
		lsData.add(data);
		lsData.add(data);
		lsData.add(data);
		lsData.add(data);
		lsData.add(data);
		excelOperate.WriteTestExcel( 1, 1, lsData);
		excelOperate.close();
		assertEquals(true, FileOperate.isFileExist("/home/novelbio/jpx/test/test.xlsx"));
	}

}
