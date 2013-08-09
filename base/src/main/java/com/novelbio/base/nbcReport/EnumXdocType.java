package com.novelbio.base.nbcReport;


public enum EnumXdocType {
	Excel_Dif("EXCEL"), Picture(""),Catalog("background_catalog.xdoc");
	
	String tempPath = "";

	EnumXdocType(String temp) {
		this.tempPath = tempPath;
	}
	
	/**
	 * 名字和XdocType的对照关系
	 * key为小写
	 * @return
	 */
	public static EnumXdocType get(String type) {
		EnumXdocType[] values = EnumXdocType.values();
		for (EnumXdocType object : values) {
			if (object.type.toLowerCase().equals(type.toLowerCase())) {
				return object;
			}
		}
		return null;
	}

	
}
