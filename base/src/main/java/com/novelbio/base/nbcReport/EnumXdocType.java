package com.novelbio.base.nbcReport;


public enum EnumXdocType {
	Excel("EXCEL"), Picture("PICTURE");
	
	String type = "";

	EnumXdocType(String type) {
		this.type = type;
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
