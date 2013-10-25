package com.novelbio.base;

import com.alibaba.fastjson.JSONObject;

public class JsonNBC {
	/**
	 * 把一个对象转成json字符串
	 * @param object
	 * @return
	 */
	public static Object fromObject(Object object){
		Object obj = null;
		try {
			obj = JSONObject.toJSON(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}
	
}

