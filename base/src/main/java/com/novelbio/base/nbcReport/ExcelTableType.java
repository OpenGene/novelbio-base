package com.novelbio.base.nbcReport;



public enum ExcelTableType {

	GOAnalysis1("GO-Analysis_DownGO_Result", new XdocTable( "100,100,60,60,60,60,60,60,80,80", "15,15,15,15,15,15,15,15,15,15,15,15,15,15,15")), 
	GOAnalysis2("GO-Analysis_DownGene2GO", new XdocTable( "120,120,150,80,80,50,50", "15,15,15,15,15,15,15,15,15,15,15,15,15,15,15")), 
	GOAnalysis3("GO-Analysis_DownGO2Gene", new XdocTable("80,80,120,120,150,50,50", "15,15,15,15,15,15,15,15,15,15,15,15,15,15,15")), 
	GOAnalysis4("GO-Analysis_UpGO_Result", new XdocTable( "100,100,60,60,60,60,60,60,80,80", "15,15,15,15,15,15,15,15,15,15,15,15,15,15,15")), 
	GOAnalysis5("GO-Analysis_UpGene2GO", new XdocTable("120,120,150,80,80,50,50", "15,15,15,15,15,15,15,15,15,15,15,15,15,15,15")), 
	GOAnalysis6("GO-Analysis_UpGO2Gene", new XdocTable("80,80,120,120,150,50,50", "15,15,15,15,15,15,15,15,15,15,15,15,15,15,15")), 
	GOAnalysis7("GO-Analysis_AllGO_Result", new XdocTable( "100,100,60,60,60,60,60,60,80,80", "15,15,15,15,15,15,15,15,15,15,15,15,15,15,15")), 
	GOAnalysis8("GO-Analysis_AllGene2GO", new XdocTable( "120,120,150,80,80,50,50", "15,15,15,15,15,15,15,15,15,15,15,15,15,15,15")), 
	GOAnalysis9("GO-Analysis_AllGO2Gene", new XdocTable( "80,80,120,120,150,50,50", "15,15,15,15,15,15,15,15,15,15,15,15,15,15,15")), 
	PathwayAnalysis1("Pathway-Analysis_DownPathway_Result", new XdocTable("100,100,60,60,60,60,60,60,80,80", "15,15,15,15,15,15,15,15,15,15,15,15,15,15,15")), 
	PathwayAnalysis2("Pathway-Analysis_DownPathway2Gene", new XdocTable("80,80,120,120,150,50,50", "15,15,15,15,15,15,15,15,15,15,15,15,15,15,15")), 
	PathwayAnalysis3("Pathway-Analysis_UpPathway_Result", new XdocTable("100,100,60,60,60,60,60,60,80,80", "15,15,15,15,15,15,15,15,15,15,15,15,15,15,15")), 
	PathwayAnalysis4("Pathway-Analysis_UpPathway2Gene", new XdocTable("80,80,120,120,150,50,50", "15,15,15,15,15,15,15,15,15,15,15,15,15,15,15")), 
	PathwayAnalysis5("Pathway-Analysis_AllPathway_Result", new XdocTable("100,100,60,60,60,60,60,60,80,80", "15,15,15,15,15,15,15,15,15,15,15,15,15,15,15")), 
	PathwayAnalysis6("Pathway-Analysis_AllPathway2Gene", new XdocTable("80,80,120,120,150,50,50", "15,15,15,15,15,15,15,15,15,15,15,15,15,15,15")), 
	DifGene("Dif-Gene", new XdocTable( "120,80,220,40,40,60,60,50",  "15,15,15,15,15,15,15,15,15,15,15,15,15,15,15"));

	private String name;
	private XdocTable xdocTable;

	ExcelTableType(String name, XdocTable xdocTable) {
		this.name = name;
		this.xdocTable = xdocTable;
	}
	
	/**
	 * 根据模板名，取得默认表对象
	 * @param name 如：GO-Analysis、GO-Analysis_DownGO2Gene等
	 * @return
	 */
	public static XdocTable getTable(String name) {
		ExcelTableType[] values = ExcelTableType.values();
		for (ExcelTableType object : values) {
			if (object.name.toLowerCase().equals(name.toLowerCase())) {
				return object.xdocTable.getClone();
			}
		}
		return null;
	}
}
