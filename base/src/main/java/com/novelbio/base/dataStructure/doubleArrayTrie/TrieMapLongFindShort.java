package com.novelbio.base.dataStructure.doubleArrayTrie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.novelbio.base.dataStructure.ArrayOperate;

/**
 * 字典树，key为短的，用长的输入参数可以查找头部一致的短的值
 * 
 * 譬如 key为 /home/novelbio/
 * 那么输入 /home/novelbio/mytest/
 * 就可以把 /home/novelbio/ 所对应的值找出来
 * @author zong0jie
 * @data 2018年8月1日
 */
public class TrieMapLongFindShort<T> {
	List<String> lsKeys = new ArrayList<>();
	DoubleArrayTrieDart doubleArrayTrieDart = new DoubleArrayTrieDart();
	Map<String, T> mapKey2Value;
	
	public TrieMapLongFindShort(Map<String, T> mapKey2Value) {
		if (ArrayOperate.isEmpty(mapKey2Value)) {
			return;
		}
		
		this.mapKey2Value = mapKey2Value;
		lsKeys = new ArrayList<>(mapKey2Value.keySet());
		Collections.sort(lsKeys, (info1, info2) -> {
			Integer l1 = info1.length();
			Integer l2 = info2.length();
			return -l1.compareTo(l2);
		});
		doubleArrayTrieDart.build(lsKeys);
	}
	
	/** 输入 /home/novelbio/mytest/
	 * 可以把 /home/novelbio/ 所对应的值找出来
	 * 注意如果找出多个value，则从长到短排序
	 */
	public List<String> getLsKeys(String searchInfo) {
		if (ArrayOperate.isEmpty(mapKey2Value)) {
			return new ArrayList<>();
		}
		
		List<Integer> lsIndex = doubleArrayTrieDart.commonPrefixSearch(searchInfo);
		List<String> lsResult = new ArrayList<>();
		for (Integer index : lsIndex) {
			lsResult.add(lsKeys.get(index));
		}
		return lsResult;
	}
	
	/** 输入 /home/novelbio/mytest/
	 * 可以把 /home/novelbio/ 所对应的值找出来
	 * 注意如果找出多个value，则从长到短排序
	 */
	public String getKeyFirst(String searchInfo) {
		List<String> lsResult = getLsKeys(searchInfo);
		if (!lsResult.isEmpty()) {
			return lsResult.get(0);
		}
		return null;
	}
	
	/**
	 * 输入 /home/novelbio/mytest/
	 * 把其对应的Value找出来
	 * 首先先找到输入所对应的keylist
	 * 然后把keylist对应的value找出来
	 * 
	 * 注意如果找出多个value，则按照key的长到短排序
	 */
	public List<T> getLsValues(String searchInfo) {
		List<String> lsKeys = getLsKeys(searchInfo);
		List<T> lsResult = new ArrayList<>();
		for (String key : lsKeys) {
			lsResult.add(mapKey2Value.get(key));
		}
		return lsResult;
	}
	
	/** 输入 /home/novelbio/mytest/
	 * 把其对应的Value找出来
	 * 首先先找到输入所对应的keylist
	 * 然后把keylist对应的value找出来
	 * 
	 * 注意如果找出多个value，则按照key的长到短排序
	 */
	public T getValueFirst(String searchInfo) {
		List<T> lsResult = getLsValues(searchInfo);
		if (!lsResult.isEmpty()) {
			return lsResult.get(0);
		}
		return null;
	}
	
	/**
	 *  输入 /home/novelbio/mytest/
	 * 可以把 /home/novelbio/ 所对应的值找出来
	 * 注意如果找出多个value，则从长到短排序
	 */
	public boolean contains(String searchInfo) {
		if (ArrayOperate.isEmpty(mapKey2Value)) {
			return false;
		}
		
		List<Integer> lsIndex = doubleArrayTrieDart.commonPrefixSearch(searchInfo);
		return !lsIndex.isEmpty();
	}
	
}
