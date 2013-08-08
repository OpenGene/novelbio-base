package com.novelbio.base.nbcReport;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 表格数据格式化枚举类
 * @author gaozhu
 *
 */
public enum ExcelDataFormat{
	ProbeName("String","10"),GeneSymbol("String","10" ),Description("String","30"),PValue("BigDecimal","3")
	,LogFC("Double","#0.00"),FDR("Double","#0.00"),Allo("Double","#0.00"),GOID("String","11"),GOTerm("String","10")
	,Enrichment("Double","#0.00"),logP("Double","#0.00"),PathID("String","13"),PathTerm("String","15")
	,Source("String","15"),Target("String","30"),Relation("String","30");
	
	static HashMap<String, ExcelDataFormat> mapDataLength;
	
	/**String类型的长度或者Double的有效数字、BigDecimal的科学计数法格式*/
	String formatType;
	/**数据类型*/
	String dataType;
	
	ExcelDataFormat(String dataType,String formatType){
		this.formatType = formatType;
		this.dataType = dataType;
	}
	
	/** 根据不同类型来格式化数据 */
	private static String formatData(ExcelDataFormat excelDataFormat,String data) {
//		if(excelDataFormat.getDataType().equals("String")) {
//			if(data.length() <= Integer.parseInt(excelDataFormat.getFormatType())) {
//				return data;
//			}
//			return data.substring(0,Integer.parseInt(excelDataFormat.getFormatType())-1);
//		}
		if(excelDataFormat.getDataType().equals("BigDecimal")){
			Double newData = Double.parseDouble(data);
			BigDecimal bDecimal = BigDecimal.valueOf(newData);
			MathContext mcContext = new MathContext(Integer.parseInt(excelDataFormat.getFormatType()));
			return bDecimal.divide(BigDecimal.ONE, mcContext).toString();
		}
		if(excelDataFormat.getDataType().equals("Double")){
			Double newData = Double.parseDouble(data);
			DecimalFormat dFormat = new DecimalFormat(excelDataFormat.getFormatType()); 
			return dFormat.format(newData).toString();
		}
		return data;
	}
	
	

	private String getFormatType() {
		return formatType;
	}

	private String getDataType() {
		return dataType;
	}
	
	/** 把所有枚举放在一个map里 */
	public static Map<String, ExcelDataFormat> getMapData(){
		if(mapDataLength != null){
			return mapDataLength;
		}
		mapDataLength = new HashMap<String,ExcelDataFormat>();
		mapDataLength.put(ProbeName.toString().toLowerCase(),ProbeName);
		mapDataLength.put(GeneSymbol.toString().toLowerCase(), GeneSymbol);
		mapDataLength.put(Description.toString().toLowerCase(), Description);
		mapDataLength.put(PValue.toString().toLowerCase(), PValue);
		mapDataLength.put(LogFC.toString().toLowerCase(), LogFC);
		mapDataLength.put(FDR.toString().toLowerCase(), FDR);
		mapDataLength.put(Allo.toString().toLowerCase(), Allo);
		mapDataLength.put(GOID.toString().toLowerCase(), GOID);
		mapDataLength.put(GOTerm.toString().toLowerCase(), GOTerm);
		mapDataLength.put(Enrichment.toString().toLowerCase(), Enrichment);
		mapDataLength.put(logP.toString().toLowerCase(), logP);
		mapDataLength.put(PathID.toString().toLowerCase(), PathID);
		mapDataLength.put(PathTerm.toString().toLowerCase(), PathTerm);
		mapDataLength.put(Source.toString().toLowerCase(), Source);
		mapDataLength.put(Target.toString().toLowerCase(), Target);
		mapDataLength.put(Relation.toString().toLowerCase(), Relation);
		return mapDataLength;
	}
	
	/** 根据标题数据类型和数据来格式化此数据 */
	public static String format(String dataType,String data) {
		dataType = motifyType(dataType);
		ExcelDataFormat excelDataFormat = getMapData().get(dataType.toLowerCase());
		if(excelDataFormat == null){
			return data;
		}
		return formatData(excelDataFormat,data);
	}
	
	/** 去除标题中的非字母内容 */
	public static String motifyType(String dataType) {
        char[] cs = dataType.toCharArray();
        char[] dest = new char[cs.length];
        int index = 0;
        for(char c : cs) {
            if((c <= 'Z' && c >= 'A') || (c <= 'z' && c >= 'a')) {
                dest[index ++] = c;
            }
        }
        return new String(dest, 0, index);
    }
	
}
