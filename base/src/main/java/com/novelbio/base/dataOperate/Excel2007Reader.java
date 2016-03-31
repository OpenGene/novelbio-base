package com.novelbio.base.dataOperate;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.novelbio.base.fileOperate.FileOperate;


/**
 * 读取excel07.
 * 优点:速度快.比excelOperate中的方式要快1到2秒.
 * 缺点:不能按行列读取,只能是全读和分sheet读.
 * 
 * @author novelbio fans.fan
 * @date 2016年3月31日
 */
public class Excel2007Reader extends DefaultHandler {
	private SharedStringsTable sst;
	private String lastContents;
	private boolean nextIsString;

	@SuppressWarnings("unused")
	private int sheetIndex = -1;
	private List<String> rowlist = new ArrayList<String>();
	private int curRow = 0; // 当前行
	private int curCol = 0; // 当前列索引
	private int preCol = 0; // 上一列列索引
	private int titleRow = 0; // 标题行，一般情况下为0
	private int rowsize = 0; // 列数

	private List<List<String>> exceldata = new ArrayList<List<String>>();// 整个excel数据

	public List<List<String>> getExcelData() {
		return exceldata;
	}

	// excel记录行操作方法，以行索引和行元素列表为参数，对一行元素进行操作，元素为String类型
	// public abstract void optRows(int curRow, List<String> rowlist) throws
	// SQLException ;

	// excel记录行操作方法，以sheet索引，行索引和行元素列表为参数，对sheet的一行元素进行操作，元素为String类型
	// public abstract void optRows(int sheetIndex,int curRow, List<String>
	// rowlist) throws SQLException;

	/**
	 * 只遍历一个sheet，其中sheetId为要遍历的sheet索引，从1开始，1-3
	 */
	public void processOneSheet(InputStream is, int sheetId) throws Exception {
		OPCPackage pkg = null;
		InputStream sheet2 = null;
		try {
			pkg = OPCPackage.open(is);
			XSSFReader r = new XSSFReader(pkg);
			SharedStringsTable sst = r.getSharedStringsTable();
			
			XMLReader parser = fetchSheetParser(sst);
			
			PackageRelationship rel = r.getSheets().getRelationship(sheetId);
			
			PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
			PackagePart sheet = pkg.getPart(relName);
			if(sheet == null) {
				throw new IllegalArgumentException("No data found for Sheet with r:id " + rel.getTargetURI());
			}
			sheet2 = sheet.getInputStream();
			sheetIndex++;
			InputSource sheetSource = new InputSource(sheet2);
			parser.parse(sheetSource);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			FileOperate.close(sheet2);
			FileOperate.close(pkg);
		}
	}

	public void process(InputStream is) throws Exception {
		OPCPackage pkg = OPCPackage.open(is);
		XSSFReader r = new XSSFReader(pkg);
		SharedStringsTable sst = r.getSharedStringsTable();

		XMLReader parser = fetchSheetParser(sst);

		Iterator<InputStream> sheets = r.getSheetsData();
		while (sheets.hasNext()) {
			curRow = 0;
			sheetIndex++;
			InputStream sheet = sheets.next();
			InputSource sheetSource = new InputSource(sheet);
			parser.parse(sheetSource);
			sheet.close();
		}
		pkg.close();// add by lfc 20130710,解决文件使用后无法删除的问题
	}

	public XMLReader fetchSheetParser(SharedStringsTable sst) throws SAXException {
		XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		this.sst = sst;
		parser.setContentHandler(this);
		return parser;
	}

	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		// c => 单元格
		if (name.equals("c")) {
			// 如果下一个元素是 SST 的索引，则将nextIsString标记为true
			String cellType = attributes.getValue("t");
			String rowStr = attributes.getValue("r");
			curCol = this.getRowIndex(rowStr);
			if (cellType != null && cellType.equals("s")) {
				nextIsString = true;
			} else {
				nextIsString = false;
			}
		}
		// 置空
		lastContents = "";
	}

	public void endElement(String uri, String localName, String name) throws SAXException {
		// 根据SST的索引值的到单元格的真正要存储的字符串
		// 这时characters()方法可能会被调用多次
		if (nextIsString) {
			try {
				int idx = Integer.parseInt(lastContents);
				lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
			} catch (Exception e) {

			}
		}

		// v => 单元格的值，如果单元格是字符串则v标签的值为该字符串在SST中的索引
		// 将单元格内容加入rowlist中，在这之前先去掉字符串前后的空白符
		if (name.equals("v")) {
			String value = lastContents.trim();
			value = value.equals("") ? " " : value;
			int cols = curCol - preCol;
			if (cols > 1) {
				for (int i = 0; i < cols - 1; i++) {
					rowlist.add(preCol, "");
				}
			}
			preCol = curCol;
			rowlist.add(curCol - 1, value);
		} else {
			// 如果标签名称为 row ，这说明已到行尾，调用 optRows() 方法
			if (name.equals("row")) {
				int tmpCols = rowlist.size();
				if (curRow > this.titleRow && tmpCols < this.rowsize) {
					for (int i = 0; i < this.rowsize - tmpCols; i++) {
						rowlist.add(rowlist.size(), "");
					}
				}
				// add by lfc 20130710,注释掉

				if (curRow == this.titleRow) {
					this.rowsize = rowlist.size();
				}

				List<String> rowlistcopy = new ArrayList<String>();
				for (int i = 0; i < rowlist.size(); i++) {
					rowlistcopy.add(rowlist.get(i));
				}
				exceldata.add(rowlistcopy);

				rowlist.clear();
				curRow++;
				curCol = 0;
				preCol = 0;
			}
		}
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		// 得到单元格内容的值
		lastContents += new String(ch, start, length);
	}

	// 得到列索引，每一列c元素的r属性构成为字母加数字的形式，字母组合为列索引，数字组合为行索引，
	// 如AB45,表示为第（A-A+1）*26+（B-A+1）*26列，45行
	public int getRowIndex(String rowStr) {
		rowStr = rowStr.replaceAll("[^A-Z]", "");
		byte[] rowAbc = rowStr.getBytes();
		int len = rowAbc.length;
		float num = 0;
		for (int i = 0; i < len; i++) {
			num += (rowAbc[i] - 'A' + 1) * Math.pow(26, len - i - 1);
		}
		return (int) num;
	}

	public int getTitleRow() {
		return titleRow;
	}

	public void setTitleRow(int titleRow) {
		this.titleRow = titleRow;
	}
	
	
	public static void main(String[] args) throws Exception {
		
		String filePathAndName = "/home/novelbio/文档/GO-Analysis_BP_peak0_All.xlsx";
//		String filePathAndName = "/home/novelbio/文档/abc.xlsx";
		InputStream is = FileOperate.getInputStream(filePathAndName);
		
		long time1 = System.currentTimeMillis();
		Excel2007Reader excel07 = new Excel2007Reader();
	    excel07.processOneSheet(is, 0);
	    List<List<String>> exceldata = excel07.getExcelData();
	    long time2 = System.currentTimeMillis();
	    System.out.println("time=" + (time2 - time1));
	    System.out.println("total read1=" + exceldata.size());
	    
	    long time3 = System.currentTimeMillis();
	    ExcelOperate excelOperate = new ExcelOperate(filePathAndName);
	    int size1 = excelOperate.readLsExcelSheet(0).size();
//	    int size2 = excelOperate.readLsExcelSheet(2).size();
//	    int size3 = excelOperate.readLsExcelSheet(3).size();
	    long time4 = System.currentTimeMillis();
	    System.out.println("time=" + (time4 - time3));
	    excelOperate.close();
//	    System.out.println("total read2=" + (size1 + size2 + size3));
	    System.out.println("total read2=" + (size1));
	    
	}



}
