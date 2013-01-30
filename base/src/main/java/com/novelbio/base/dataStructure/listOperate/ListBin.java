package com.novelbio.base.dataStructure.listOperate;
/**
 * 一般的序列查找就用这个就行
 * @author zong0jie
 *
 */
public class ListBin<T extends ListDetailAbs> extends ListAbsSearch<T, ListCodAbs<T>, ListCodAbsDu<T,ListCodAbs<T>>>{
	private static final long serialVersionUID = 8632727637919902406L;
	String description = "";
	/**
	 * 根据需要设定描述
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * 获得设定的描述
	 * @param description
	 */
	public String getDescription() {
		return description;
	}
	@Override
	protected ListCodAbs<T> creatGffCod(String listName, int Coordinate) {
		ListCodAbs<T> result = new ListCodAbs<T>(listName, Coordinate);
		return result;
	}

	@Override
	protected ListCodAbsDu<T, ListCodAbs<T>> creatGffCodDu(
			ListCodAbs<T> gffCod1, ListCodAbs<T> gffCod2) {
		ListCodAbsDu<T, ListCodAbs<T>> result = new ListCodAbsDu<T, ListCodAbs<T>>(gffCod1, gffCod2);
		return result;
	}
	
}
