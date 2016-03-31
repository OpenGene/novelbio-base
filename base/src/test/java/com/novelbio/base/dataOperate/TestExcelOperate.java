package com.novelbio.base.dataOperate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.novelbio.base.PathDetail;
import com.novelbio.base.StringOperate;
import com.novelbio.base.fileOperate.FileOperate;

import junit.framework.TestCase;

public class TestExcelOperate extends TestCase {
	
	public void testWriteTestExcel() {
		ExcelOperate excelOperate = null;
		String filename = "/home/novelbio/tmp/test1.xls";
		try {
			excelOperate = new ExcelOperate(filename);
			List<String[]> lsData = new ArrayList<>();
			String[] data = new String[4];
			data[0] = "中国";
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
	
	public void testHadoopExcel(){
		ExcelOperate excelOperate = null;
		String filename = "/media/nbfs/testdata/test5.xls";
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
			excelOperate.close();
			
			assertEquals(true, FileOperate.isFileExist(filename));
			
			FileOperate.delFile(filename);
		}catch(Exception e){
			e.printStackTrace();
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
	
	public void testWriteNull(){
		String tempFilePath = "/home/novelbio/tmp/123-3.xls";
		List<String[]> lsExcelData = new ArrayList<>();
		
		ExcelOperate excelOperate = new ExcelOperate(tempFilePath);
		excelOperate.writeExcel(null);
		excelOperate.close();
		
		assertTrue(FileOperate.getFileSizeLong(tempFilePath) > 0);
	}
	
	
	public void testPath(){
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-ddhhmmss");
		String tempFileName  = PathDetail.getTmpPathWithSep() +FileOperate.getSepPath()+ dateFormat.format(new Date()) +"resultFileTMP.xls";	
	
		System.out.println("path = " + tempFileName);
		
	}
	
	/**
	 * 测试判断文件是否excel的判断速度.
	 * 
	 * 2016年3月30日
	 * novelbio fans.fan
	 * @throws IOException 
	 */
	public void testIsExcel() throws IOException{
//		String file = "/hdfs:/nbCloud/public/AllProject/project_56a1d075da50acf943e4bd06/task_56a5c3eada50a2b486568e55/GOAnalysis_result/GO-Analysis_BP_peak0_All.xlsx";
//		String file = "/hdfs:/nbCloud/public/AllProject/project_56a1d075da50acf943e4bd06/GO-Analysis_BP_peak0_All.xlsx";
//		String file = "/home/novelbio/文档/GO-Analysis_BP_peak0_All.xlsx";
		String file = "/home/novelbio/文档/GO-Analysis_BP_peak0_All.xls";
//		String file = "/home/novelbio/文档/abc.xlsx";
//		String file = "/hdfs:/nbCloud/public/AllProject/project_56a1d075da50acf943e4bd06/task_56a5c3eada50a2b486568e55/GOAnalysis_result/testRead.xls";
//		String file = "/media/nbfs/testdata/testRead.xls";
		ExcelOperate excelOperate = new ExcelOperate(file);
		long time1 = System.currentTimeMillis();
		System.out.println("isExcel=" + ExcelOperate.isExcel(file));
		long time2 = System.currentTimeMillis();
		System.out.println("time=" + (time2 - time1));
		
	}
	
}
