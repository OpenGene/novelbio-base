package com.novelbio.base.dataStructure.listOperate;

import org.apache.log4j.Logger;

public abstract class ListAbsSearch <E extends ListDetailAbs, T extends ListCodAbs<E>, K extends ListCodAbsDu<E, T>> extends ListAbs<E>  implements Cloneable{
	private static Logger logger = Logger.getLogger(ListAbsSearch.class);
	private static final long serialVersionUID = 4583552188474447935L;

	/**
	 * 获得的每一个信息都是实际的而没有clone
	 * 输入PeakNum，和单条Chr的list信息 返回该PeakNum的所在LOCID，和具体位置
	 * 采用clone的方法获得信息
	 * 没找到就返回null
	 */
	public T searchLocation(int Coordinate) {
		CoordLocationInfo coordLocationInfo = LocPosition(Coordinate);
		if (coordLocationInfo == null) {
			return null;
		}
		T gffCod = creatGffCod(listName, Coordinate);
		if (coordLocationInfo.isInsideElement()) {
			gffCod.setGffDetailThis( get(coordLocationInfo.getElementNumThisElementFrom0() ) ); 
			gffCod.booFindCod = true;
			gffCod.ChrHashListNumThis = coordLocationInfo.getElementNumThisElementFrom0();
			gffCod.insideLOC = true;
		}
		if (coordLocationInfo.getElementNumLastElementFrom0() >= 0) {
			gffCod.setGffDetailUp( get(coordLocationInfo.getElementNumLastElementFrom0()) );
			gffCod.ChrHashListNumUp = coordLocationInfo.getElementNumLastElementFrom0();
		}
		if (coordLocationInfo.getElementNumNextElementFrom0() >= 0) {
			gffCod.setGffDetailDown(get(coordLocationInfo.getElementNumNextElementFrom0()));
			gffCod.ChrHashListNumDown = coordLocationInfo.getElementNumNextElementFrom0();
		}
		return gffCod;
	}
	
	/**
	 * 返回双坐标查询的结果，内部自动判断 cod1 和 cod2的大小
	 * 如果cod1 和cod2 有一个小于0，那么坐标不存在，则返回null
	 * @param chrID 内部自动小写
	 * @param cod1 必须大于0
	 * @param cod2 必须大于0
	 * @return
	 */
	public K searchLocationDu(int cod1, int cod2) {
		if (cod1 < 0 && cod2 < 0) {
			return null;
		}
		T gffCod1 = searchLocation(cod1);
		T gffCod2 = searchLocation(cod2);
		if (gffCod1 == null) {
			System.out.println("error");
		}
		K lsAbsDu = creatGffCodDu(gffCod1, gffCod2);
		
		if (lsAbsDu.getGffCod1().getItemNumDown() >= 0) {
			for (int i = lsAbsDu.getGffCod1().getItemNumDown(); i <= lsAbsDu.getGffCod2().getItemNumUp(); i++) {
				lsAbsDu.getLsGffDetailMid().add(get(i));
			}
		}
		return lsAbsDu;
	}
	/**
	 * 生成一个全新的GffCod类
	 * @param listName
	 * @param Coordinate
	 * @return
	 */
	protected abstract T creatGffCod(String listName, int Coordinate);
	/**
	 * 生成一个全新的GffCod类
	 * @param listName
	 * @param Coordinate
	 * @return
	 */
	protected abstract K creatGffCodDu(T gffCod1, T gffCod2);
	/**
	 * 已测试，能用
	 */
	@SuppressWarnings("unchecked")
	public ListAbsSearch<E, T, K> clone() {
		ListAbsSearch<E, T, K> result = null;
		result = (ListAbsSearch<E, T, K>) super.clone();
		return result;
	}
}
