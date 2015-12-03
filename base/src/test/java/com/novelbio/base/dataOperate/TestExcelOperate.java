package com.novelbio.base.dataOperate;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import com.novelbio.base.PathDetail;
import com.novelbio.base.fileOperate.FileOperate;

public class TestExcelOperate extends TestCase {
	
	public void testWriteTestExcel() {
		ExcelOperate excelOperate = null;
		String filename = "/home/novelbio/tmp/test1.xls";
		try {
			excelOperate = new ExcelOperate(filename);
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
			lsData.add(data);
			lsData.add(data);
			int endRowNum = 7;
			ExcelStyle style = ExcelStyle.getThreeLineTable(1, endRowNum);
			excelOperate.writeExcel(1, 1, lsData, style);
			
			assertEquals(true, FileOperate.isFileExist(filename));
			
			ExcelOperate readExcelOperate = null;
			readExcelOperate = new ExcelOperate(filename);
			ArrayList<String[]> lsList = readExcelOperate.readLsExcelSheet(1);
			readExcelOperate.close();
//			assertTrue(lsList.size() == 10);
			
			data = new String[4];
			data[0] = "1";
			data[1] = "2";
			data[2] = "3";
			data[3] = "4";
			lsData.clear();
			lsData.add(data);
			lsData.add(data);
			excelOperate.writeExcel(1, 1, lsData, null);
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			excelOperate.close();
		}
	
		
	}
	
	public void testCreateExecl(){
		//需写入的excel文件所在文件夹必须是存在的.不然就不会写入文件.
		String tempFilePath = "/home/novelbio/tmp/1234.xls";
		ExcelOperate excelOperate= new ExcelOperate(tempFilePath);
		excelOperate.writeExcel(new ArrayList<String[]>());
		excelOperate.close();
		assertTrue(FileOperate.isFileExist(tempFilePath));
	}

	public void testReportExcel(){
		//测试数据是否能够写入文件
		String tempFilePath = "/home/novelbio/tmp/123.xls";
		List<String[]> lsExcelData = new ArrayList<>();
		String[] data = new String[]{"one","two","three","four"};
		lsExcelData.add(data);
		
		ExcelOperate excelOperate = new ExcelOperate(tempFilePath);
		excelOperate.writeExcel(1, 1, lsExcelData);
		excelOperate.writeExcel(2, lsExcelData);
		excelOperate.close();
		
		assertTrue(FileOperate.getFileSizeLong(tempFilePath) > 0);
	}
	
	public void testPath(){
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-ddhhmmss");
		String tempFileName  = PathDetail.getTmpPathWithSep() +FileOperate.getSepPath()+ dateFormat.format(new Date()) +"resultFileTMP.xls";	
	
		System.out.println("path = " + tempFileName);
		
	}
	
}
