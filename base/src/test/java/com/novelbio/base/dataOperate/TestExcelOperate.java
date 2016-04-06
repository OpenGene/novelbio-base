package com.novelbio.base.dataOperate;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.novelbio.base.PathDetail;
import com.novelbio.base.StringOperate;
import com.novelbio.base.dataStructure.ArrayOperate;
import com.novelbio.base.fileOperate.FileOperate;

public class TestExcelOperate {
	
	@Test
	public void testWriteTestExcel() {
		ExcelOperate excelOperate = null;
		String filename = PathDetail.getTmpPathWithSep() + "test1.xls";
		try {
			excelOperate = new ExcelOperate(filename);
			excelOperate.setWriteSheetToTxt(true);
			List<String[]> lsData = new ArrayList<>();
			String[] data = new String[4];
			data[0] = "中国";
			data[1] = "two";
			data[2] = "three";
			data[3] = "four";
			for (int i = 0; i < 11; i++) {
				lsData.add(data);
			}
			int endRowNum = 7;
			ExcelStyle style = ExcelStyle.getThreeLineTable(1, endRowNum);
			excelOperate.writeExcel(1, 1, lsData, style);
			excelOperate.close();
			Assert.assertEquals(true, FileOperate.isFileExist(filename));
			Assert.assertEquals(true, FileOperate.isFileExist(filename));
			
			List<String> lsSheetTxt = ExcelOperate.getLsSheetTxtFiles(filename);
			Assert.assertEquals(1, lsSheetTxt.size());
			String outTxtSheet = PathDetail.getTmpPathWithSep() + ExcelOperate.TMP_TXT_PATH + "/test1@@sheet1.txt";
			Assert.assertEquals(outTxtSheet, lsSheetTxt.get(0));
			Assert.assertTrue(FileOperate.isFileExistAndBigThan0(outTxtSheet));
			
			TxtReadandWrite txtReadOutTxtSheet = new TxtReadandWrite(outTxtSheet);
			List<String> lsDataTxt = txtReadOutTxtSheet.readfileLs();
			txtReadOutTxtSheet.close();
			List<String> lsDataExcel = new ArrayList<>();
			for (String[] content : lsData) {
				lsDataExcel.add(ArrayOperate.cmbString(content, "\t"));
			}
			Assert.assertEquals(lsDataTxt, lsDataExcel);
			FileOperate.DeleteFileFolder(FileOperate.getPath(outTxtSheet));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			excelOperate.close();
		}
	}
	
	@Test
	public void testHadoopExcel() {
		ExcelOperate excelOperate = null;
		String filename = "/hdfs:/nbCloud/test.xls";
		try {
			excelOperate = new ExcelOperate(filename);
			List<String[]> lsData = new ArrayList<>();
			String[] data = new String[4];
			data[0] = "one"; data[1] = "two";
			data[2] = "three"; data[3] = "four";
			for (int i = 0; i < 11; i++) {
				lsData.add(data);
			}
			int endRowNum = 7;
			ExcelStyle style = ExcelStyle.getThreeLineTable(1, endRowNum);
			excelOperate.writeExcel(1, 1, lsData, style);
			excelOperate.close();
			
			Assert.assertEquals(true, FileOperate.isFileExist(filename));
			
			FileOperate.DeleteFileFolder(filename);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void testCreateExecl() {
		//需写入的excel文件所在文件夹必须是存在的.不然就不会写入文件.
		String tempFilePath = "/home/novelbio/tmp/1234.xls";
		ExcelOperate excelOperate= new ExcelOperate(tempFilePath);
		excelOperate.writeExcel(new ArrayList<String[]>());
		excelOperate.close();
		Assert.assertTrue(FileOperate.isFileExist(tempFilePath));
	}

	@Test
	public void testReportExcel() {
		//测试数据是否能够写入文件
		String tempFilePath = "/home/novelbio/tmp/123.xls";
		List<String[]> lsExcelData = new ArrayList<>();
		String[] data = new String[]{"one","two","three","four"};
		lsExcelData.add(data);
		
		ExcelOperate excelOperate = new ExcelOperate(tempFilePath);
		excelOperate.writeExcel(1, 1, lsExcelData);
		excelOperate.writeExcel(2, lsExcelData);
		excelOperate.close();
		
		Assert.assertTrue(FileOperate.getFileSizeLong(tempFilePath) > 0);
	}
	
	@Test
	public void testWriteNull() {
		String tempFilePath = "/home/novelbio/tmp/123-3.xls";		
		ExcelOperate excelOperate = new ExcelOperate(tempFilePath);
		excelOperate.writeExcel(null);
		excelOperate.close();
		
		Assert.assertTrue(FileOperate.isFileExistAndBigThan0(tempFilePath));
	}
	
	@Test
	public void testPath() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-ddhhmmss");
		String tempFileName  = PathDetail.getTmpPathWithSep() +FileOperate.getSepPath()+ dateFormat.format(new Date()) +"resultFileTMP.xls";	
	
		System.out.println("path = " + tempFileName);
		
	}
	
	/**
	 * 测试判断文件是否excel
	 * 2016年3月30日
	 * @throws IOException 
	 */
	@Test
	public void testIsExcel() throws IOException {
		String excel = PathDetail.getTmpPathWithSep() + "test1.xls";
		String txt = PathDetail.getTmpPathWithSep() + "test2.xls";
		ExcelOperate excelOperate = new ExcelOperate(excel);
		List<String[]> lsData = new ArrayList<>();
		String[] data = new String[4];
		data[0] = "中国";
		data[1] = "two";
		data[2] = "three";
		data[3] = "four";
		for (int i = 0; i < 11; i++) {
			lsData.add(data);
		}
		excelOperate.writeExcel(lsData);
		excelOperate.close();
		
		
		TxtReadandWrite txtWrite = new TxtReadandWrite(txt, true);
		txtWrite.ExcelWrite(lsData);
		txtWrite.close();
		
		Assert.assertTrue(ExcelOperate.isExcelSimple(excel));
		Assert.assertFalse(ExcelOperate.isExcelSimple(txt));
		
		FileOperate.DeleteFileFolder(excel);
		FileOperate.DeleteFileFolder(txt);
	}
	
}
