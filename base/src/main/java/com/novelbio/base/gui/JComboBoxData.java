package com.novelbio.base.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 * JComboBox的扩展，方便操作<br>
 * T: 选择的Item所对应的类
 * @author zong0jie
 *
 */
public class JComboBoxData<T> extends JComboBox{
	private static final long serialVersionUID = -1651148386751801706L;
	/**
	 * 保存key和value的map
	 */
	HashMap<String, T> mapString2Value = null;
	/**
	 * 保存key和value的map
	 */
	HashMap<T, String> mapValue2String = null;
	/** 安顺序排列的key值 */
	ArrayList<String> lsInfo = new ArrayList<String>();
	/**
	 * null不排序
	 * true：正序
	 * false：倒序
	 */
	Boolean resultSort = null;
	/**
	 * <b>注意要在setMapItem方法之前设定</b>
	 * null不排序
	 * true：正序
	 * false：倒序
	 */
	public void sortValue(Boolean resultSort) {
		this.resultSort = resultSort;
	}
	/**
	 * 装载hash表
	 * @param hashInfo
	 */
	public void setMapItem(HashMap<String, T> mapString2Value) {
		this.mapString2Value = mapString2Value;
		mapValue2String = new HashMap<T, String>();
		for (Entry<String, T> entry : mapString2Value.entrySet()) {
			mapValue2String.put(entry.getValue(), entry.getKey());
		}
		setCombBox();
	}
	
	private void setCombBox() {
		lsInfo = new ArrayList<String>();
		for (String string : mapString2Value.keySet()) {
			if (string != null) {
				lsInfo.add(string);
			}
		}
		//排序///////////////////
		sortList(lsInfo);
		/////////////////////////////////////////////////////
		String[] speciesarray = new String[lsInfo.size()];
		int i = 0;
		for(String string:lsInfo) {
			speciesarray[i] = string; i++;
		}
		ComboBoxModel jCobTaxSelectModel = new DefaultComboBoxModel(speciesarray);
		setModel(jCobTaxSelectModel);
	}
	/**
	 * 排序，根据给定的方式，作正序倒序或者不排序
	 * @param lsInfo
	 */
	private void sortList(ArrayList<String> lsInfo) {
		if (resultSort != null) {
			if (resultSort) {
				Collections.sort(lsInfo);
			}
			else {
				Collections.sort(lsInfo, new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						return -o1.compareTo(o2);
					}
				} );
			}
		}
	}
	/** 设定展示的值 */
	public void setSelectVaule(String key) {
		int index = lsInfo.indexOf(key);
		setSelectedIndex(index);
	}
	/** 设定展示的值 */
	public void setSelectVaule(T key) {
		String keyString = mapValue2String.get(key);
		setSelectVaule(keyString);
	}
	public T getSelectedValue() {
		String key = (String) getSelectedItem();
		if (mapString2Value == null || mapString2Value.get(key) == null) {
			return null;
		}
		else return mapString2Value.get(key);
	}
}
