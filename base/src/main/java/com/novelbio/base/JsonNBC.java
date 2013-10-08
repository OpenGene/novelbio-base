package com.novelbio.base;

import com.alibaba.fastjson.JSONObject;

public class JsonNBC {
	/**
	 * 把一个对象转成json字符串
	 * @param object
	 * @return
	 */
	public static Object fromObject(Object object){
		return JSONObject.toJSON(object);
	}
	
}

