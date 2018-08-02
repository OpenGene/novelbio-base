package com.novelbio.base.dataStructure.doubleArrayTrie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
		return !getLsKeys(searchInfo).isEmpty();
	}
	
	/** 输入 /home/novelbio/mytest/
	 * 可以把 /home/novelbio/ 所对应的值找出来
	 * 注意如果找出多个value，则从长到短排序
	 */
	public List<String> getLsKeys(String searchInfo) {
		if (ArrayOperate.isEmpty(setKkey)) {
			return new ArrayList<>();
		}
		
		return lsKeys.stream().filter(key -> searchInfo.startsWith(key)).collect(Collectors.toList());
	}
	
	
	
}
