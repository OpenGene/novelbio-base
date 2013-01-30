package com.novelbio.base.dataStructure.listOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * 内建排序方法
 * 将多条ListAbs合并后，其每个元素的信息
 * 合并方法为首先两条listAbs合在一起，然后排序。
 * 如果两个element有交集，就将这两个element放在一起，作为一个element
 * 如果只有单独一个element，就放该element
 * 所以不能有空的ElementComb
 * @author zong0jie
 *
 * @param <T>
 */
public class ListDetailComb<T extends ListDetailAbs> extends ListDetailAbs {
	public ListDetailComb() {
		super("", "", null);
	}
	/**
	 * 基因名--int[2]:
	 * 0: 该ele所在的起点
	 * 1: 该ele所在的终点。如果1<0，表示该ele是跳过的，那么0表示的是前一个ele的位置
	 */
	HashMap<String, int[]> hashList2Num = new HashMap<String, int[]>();
	/**
	 * 保存每个listabs中的element
	 */
	ArrayList<ArrayList<T>> lsElement = new ArrayList<ArrayList<T>>();
	ArrayList<T> lsSortEle = new ArrayList<T>();
	String SEP = "\\";
	boolean sorted = false;
	/**
	 * 给定一个exon的list，以及该list所占原始list的起点位和终点位的element
	 * 譬如，给定了一个list--含有一个element，起点第2位，终点第2位
	 * 给定了一个空list或者没给list，含有0个element，起点第2位，终点第-2位--当终点为负数时表示该element是跳过的
	 * @param element
	 * @param numStart
	 * @param numEnd
	 */
	protected void addLsElement(String lsName, ArrayList<T> element, int numStart, int numEnd) {
		hashList2Num.put(lsName, new int[]{numStart, numEnd});
		if (element == null || element.size() == 0) {
			return;
		}
		lsElement.add(element);
		/// 这个可以不设置 ////////////////
		for (T t : element) {
			this.parentName = t.getRefID();
			break;
		}
		//如果比较的element里面有相反的cis，那么就设定为null
		for (T t : element) {
			if (isCis5to3() == null) {
				setCis5to3(t.isCis5to3());
			}
			else if (t.isCis5to3() != null || t.isCis5to3() != isCis5to3()) {
				setCis5to3(null);
				break;
			}
		}
	}
	
	protected void sort() {
		if (sorted) {
			return;
		}
		for (ArrayList<T> lsele : lsElement) {
			for (T t : lsele) {
				lsSortEle.add(t);
			}
		}
		/**
		 * 将list中的元素进行排序，如果element里面 start > end，那么就从大到小排序
		 * 如果element里面start < end，那么就从小到大排序
		 */
		if ( isCis5to3() == null) {
			Collections.sort(lsSortEle, new CompS2MAbs());
		}
		if ( isCis5to3()) {
			Collections.sort(lsSortEle, new CompS2M());
		}
		else {
			Collections.sort(lsSortEle, new CompM2S());
		}
		sorted = true;
	}
	@Override
	public Boolean isCis5to3() {
		return lsSortEle.get(0).isCis5to3();
	}
	/**
	 * 待验证
	 */
	@Override
	public int getStartCis() {
		sort();
		return lsSortEle.get(0).getStartCis();
	}
	@Override
	public int getStartAbs() {
		sort();
		return Math.min(lsSortEle.get(0).getStartAbs(), lsSortEle.get(lsSortEle.size() - 1).getStartAbs());
	}
	/**
	 * 待验证
	 */
	@Override
	public int getEndCis() {
		sort();
		return lsSortEle.get(lsSortEle.size() - 1).getEndCis();
	}
	/**
	 * 待验证
	 */
	@Override
	public int getEndAbs() {
		sort();
		return Math.min(lsSortEle.get(0).getEndAbs(), lsSortEle.get(lsSortEle.size() - 1).getEndAbs());
	}
	
	@Override
	public int Length() {
		return Math.abs(getStartAbs()- getEndAbs());
	}

	@Override
	public ArrayList<String> getName() {
		ArrayList<String> lsName = lsElement.get(0).get(0).getName();
		for (int i = 1; i < lsElement.size(); i++) {
			lsName.addAll(lsElement.get(i).get(0).getName());
		}
		return lsName;
	}
	/** 假定几个转录本的来源一致 */
	@Override
	public String getRefID() {
		String name = lsElement.get(0).get(0).getRefID();
		return name;
	}
	@Override
	public String getNameSingle() {
		return lsElement.get(0).get(0).getName().get(0);
	}
	/**
	 * 输入的几个exon是不是一样的
	 * @return
	 */
	public boolean isSameEle() {
		if (lsElement.size() != hashList2Num.size()) {
			return false;
		}
		if (lsElement.get(0).size() > 1) {
			return false;
		}
		ArrayList<T> ele = lsElement.get(0);
		HashSet<String> hashEle = new HashSet<String>();
		hashEle.add(ele.get(0).getStartAbs() + "_ " +ele.get(0).getEndAbs());
		for (int i = 1; i < lsElement.size(); i++) {
			if (lsElement.get(i).size() > 1) {
				return false;
			}
			else if (!hashEle.contains(lsElement.get(i).get(0).getStartAbs() + "_ " +lsElement.get(i).get(0).getEndAbs())) {
				return false;
			}
		}
		return true;
	}
}
