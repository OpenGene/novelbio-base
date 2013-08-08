package com.novelbio.base.nbcReport;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.novelbio.base.SepSign;
import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * excel读取结果实体类
 * @author gaozhu
 *
 */
public class XdocTmpltExcel extends XdocTemplate{
	
	/** 对应的说明文件名，全名 */
	private String descFile = "";
//	/** 表格的标题 */
//	private String excelTitle = "";
//	/** 表格的注： */
//	private String note = ""; 
//	/** 同类表格的对比说明在这类表格的上方 */
//	private String upCompare = "";
//	/** 同类表格的对比说明在这类表格的下方 */
//	private String downCompare = "";
	private XdocTable xdocTable = null;
	/** excel的全名 */
	String excelName;
	String sheetName;
	
	/** 根据excel路径完成本类的构造
	 * @param filePath
	 * @param excelName
	 */
	public XdocTmpltExcel(String excelNameAndSheetNum) {
		String[] ss = excelNameAndSheetNum.split(SepSign.SEP_INFO_SAMEDB);
		this.excelName = ss[0];
		if (ss.length > 1) {
			this.sheetName = ss[1];
		} else {
			this.sheetName = null;
		}
	
		resolveExcelName(this.excelName, sheetName);
	}
	
	/** 根据excel路径完成本类的构造
	 * @param filePath
	 * @param excelName
	 * @param sheetNum 读取第几个sheet，实际sheet数
	 */
	public XdocTmpltExcel(String excelName, String sheetName) {
		this.excelName = excelName;
		this.sheetName = sheetName;
		resolveExcelName(this.excelName, this.sheetName);
	}
	
	/** 解析文件名
	 * 如果文件名为 /Path/To/Your/GOanalysis_PMvsKO.xlsx<br>
	 * 则：<br>
	 * descFile: /Path/To/Your/GOanalysis_PMvsKO_GOresult_xls.txt<br>
	 * tempName: /Path/To/Xdoc/GOanalysis_GOresult.xdoc
	 * @param excelName
	 * @param sheetName
	 */
	private void resolveExcelName(String excelName, String sheetName) {
		/** 如果文件名为 /Path/To/Your/GOanalysis_PMvsKO.xlsx
		 * 则：
		 * descFile: /Path/To/Your/GOanalysis_PMvsKO_GOresult_xls.txt
		 * tempName: /Path/To/Xdoc/GOanalysis_GOresult.xdoc
		 */
		//去掉后缀名
		String excelNameNoSuffix = FileOperate.getFileNameSep(excelName)[0];
		if (sheetName == null || sheetName.equals("")) {
			this.descFile = excelNameNoSuffix + "_xls" + ".txt";
			super.tempName = excelNameNoSuffix.split("_")[0];
		} else {
			this.descFile = excelNameNoSuffix + "_" + sheetName + "_xls" + ".txt";
			super.tempName = excelNameNoSuffix.split("_")[0] + "_" + sheetName;
		}
		
//		this.excelTitle = "下表为"+excelNameNoSuffix+".xls 中的部分内容";
	}
	
	/** 读取excel的说明文件中的参数（允许不存在）*/
	@Override
	public void readParamAndGenerateXdoc(){
		xdocTable = ExcelTableType.getTable(tempName);
		if (xdocTable == null) return;
		if (FileOperate.isFileExist(descFile)) {
			TxtReadandWrite txtRead = new TxtReadandWrite(descFile, false);
			for (String content : txtRead.readlines()) {
				if (content.trim().equals("")) {
					continue;
				}
				String[] params = content.split(SepSign.SEP_INFO);
				if (params.length == 1) {
					logger.error(descFile+".txt文件书写不规范");
				}
				if(params[0].equals("title")){
					xdocTable.setTitle(params[1]);
				}else if(params[0].equals("note")){
					xdocTable.setNote(params[1]);
				}else if(params[0].equals("upCompare")){
					xdocTable.setUpCompare(params[1]);
				}else if(params[0].equals("downCompare")){
					xdocTable.setDownCompare(params[1]);
				}
			}
			txtRead.close();
		}
		//使用枚举格式化Excel中的数据
		xdocTable.setLsExcelTable(formatDataList(ExcelTxtRead.readLsExcelTxtls(excelName, sheetName, 1, 200)));
		mapParams.put("excel",xdocTable);
	}
	
	/**把所有的表格数据格式化*/
	private List<List<String>> formatDataList(List<List<String>> lsAllDatas) {
		List<List<String>> lsNewDatas = new ArrayList<List<String>>();
		List<String> lsTitles = lsAllDatas.get(0);
		lsNewDatas.add(lsTitles);
		for (int i = 1; i < lsAllDatas.size(); i++) {
			List<String> lsData = new ArrayList<String>();
			for (int j = 0; j < lsTitles.size(); j++) {
				lsData.add(ExcelDataFormat.format(lsTitles.get(j), lsAllDatas.get(i).get(j)));
			}
			lsNewDatas.add(lsData);
		}
		return lsNewDatas;
	}
	public String toString(XdocMethods xdocMethods) {
		return super.toString(xdocMethods);
	}
	/** 输出渲染好的xdoc的toString结果 */
	@Override
	public String toString(){
		/** 把子xdoc的toString方法封装成集合传递给本xdoc */
		if (xdocTable == null) return "";
		try {
			if (!FileOperate.isFileExist(FileOperate.addSep(xdocPath) + "Excel.xdoc")) {
				return "";
			}
			return renderXdoc(xdocPath, "Excel.xdoc", mapParams);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("渲染模板" + tempName + "出错");
			return "";
		}
	}
	
	/** 配置文件的后缀名，譬如文件名为
	 * AvsB_GO-Analysis.xls
	 * 配置文件为
	 * AvsB_GO-Analysis_xls.txt
	 * */
	public static String getSuffix() {
		return "_xls.txt";
	}
	
	/**
	 * 这种类型 EXCEL::value1#/#1;value1#/#2;value2#/#1....读取为excel集合<br>
	 * @param path 文件所在父级路径
	 * @param param value1#/#1;value1#/#2;value2#/#1....读取为excel集合<br>
	 * @param num 读取前几个文件 
	 * @return
	 */
	public static Set<String> getLsFile(String path, String param, int num) {
		Set<String> setResultFileName = new LinkedHashSet<String>();
		path = FileOperate.addSep(path);
		String[] ss = param.split(";");
		int numThis = 0;
		for (String string : ss) {
			numThis++;
			setResultFileName.add(path + string.split(SepSign.SEP_INFO_SAMEDB)[0]);
			if (numThis > num) {
				break;
			}
		}
		return setResultFileName;
	}
}
