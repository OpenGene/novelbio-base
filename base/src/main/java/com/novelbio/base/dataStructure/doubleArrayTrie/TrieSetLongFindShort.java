package com.novelbio.base.dataStructure.doubleArrayTrie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
public class TrieSetLongFindShort {
	List<String> lsKeys = new ArrayList<>();
	DoubleArrayTrieDart doubleArrayTrieDart = new DoubleArrayTrieDart();
	Set<String> setKkey;
	
	public TrieSetLongFindShort(Set<String> setKkey) {
		if (ArrayOperate.isEmpty(setKkey)) {
			return;
		}
		this.setKkey = setKkey;
		lsKeys = new ArrayList<>(setKkey);
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
		if (ArrayOperate.isEmpty(setKkey)) {
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
	/** 输入 /home/novelbio/mytest/
	 * 可以把 /home/novelbio/ 所对应的值找出来
	 * 注意如果找出多个value，则从长到短排序
	 */
	public boolean contains(String searchInfo) {
		try {
			if (ArrayOperate.isEmpty(setKkey)) {
				return false;
			}
			List<Integer> lsIndex = doubleArrayTrieDart.commonPrefixSearch(searchInfo);
			return !lsIndex.isEmpty();
		} catch (Exception e) {
			if (ArrayOperate.isEmpty(setKkey)) {
				return false;
			}
			List<Integer> lsIndex = doubleArrayTrieDart.commonPrefixSearch(searchInfo);
			return !lsIndex.isEmpty();
		}

	}
	
}
