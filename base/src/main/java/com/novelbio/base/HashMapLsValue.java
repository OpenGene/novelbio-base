package com.novelbio.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;
/**
 * 包装了一个hashmap < K, arraylist < V > >
 * @author zong0jie
 *
 * @param <K>
 * @param <V>
 */
public class HashMapLsValue<K, V> {
	LinkedHashMap<K, ArrayList<V>> mapK2V = new LinkedHashMap<K, ArrayList<V>>();
	private static final long serialVersionUID = -6427548618773494477L;
	/**
	 * 如果含有该Key，就在list后面添加，如果不含有该key，就新建一个list然后加入
	 * @param key
	 * @param value
	 */
	public void put(K key, V value) {
		ArrayList<V> lsValue = null;
		if (mapK2V.containsKey(key)) {
			lsValue = mapK2V.get(key);
		}
		else {
			lsValue = new ArrayList<V>();
			mapK2V.put(key, lsValue);
		}
		lsValue.add(value);
	}
	
	public boolean containsKey(K key) {
		return mapK2V.containsKey(key);
	}
	
	public ArrayList<V> get(K key) {
		return mapK2V.get(key);
	}
	public ArrayList<V> remove(K key) {
		return mapK2V.remove(key);
	}
	public void clear() {
		mapK2V.clear();
	}
	public Set<Entry<K, ArrayList<V>>> entrySet() {
		return mapK2V.entrySet();
	}
	public Set<K> keySet() {
		return mapK2V.keySet();
	}
	public Collection<ArrayList<V>> values() {
		return mapK2V.values();
	}
	public boolean isEmpty() {
		return mapK2V.isEmpty();
	}

}
