package com.novelbio.base.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.novelbio.base.ExceptionNbcApiStateError;
import com.novelbio.base.ResultJson;
import com.novelbio.base.security.Crypter;

/**
 * 
 * 简单封装返回值为Json格式的http.POST请求(内部系统接口调用)<br>
 * 使用{@link HttpUtil}类进行网络调用,
 * 
 * @author novelbio
 *
 */
public class HttpJsonUtil {

	/** 开放模式，不加密 **/
	public static final int ENCRYTION_TYPE_NONE = 0;
	/** 公钥私钥加密(基于RSA加密) **/
	public static final int ENCRYTION_TYPE_RSA = 1;

	/**
	 * 返回值为{@link ResultJson}，且{@linkplain ResultJson#getResult()}为列表时使用<br>
	 * 当返回{@link ResultJson}格式且{@linkplain ResultJson#isState()}为true时返回列表<br>
	 * 当返回{@link ResultJson}格式{@linkplain ResultJson#isState()}为false时抛出异常，异常信息为{@linkplain ResultJson#getMessage()}<br>
	 * 其他情况抛出对应的异常
	 * 
	 * @param url
	 *            请求url
	 * @param params
	 *            请求参数
	 * @param encryptionType
	 *            加密类型
	 * @param clazz
	 * @return
	 */
	public static <T> List<T> post4ResultJsonList(String url, Map<String, Object> params, int encryptionType,
			Class<T> clazz) {
		JSONObject jsonObject = JSON.parseObject(post4String(url, params, encryptionType));
		List<T> lsResult = new ArrayList<>();
		if (jsonObject.getBooleanValue("state")) {
			JSONArray array = jsonObject.getJSONArray("result");
			for (int i = 0; i < array.size(); i++) {
				JSONObject obj = array.getJSONObject(i);
				T tObj = parseToJava(obj, clazz);
				lsResult.add(tObj);
			}
		} else {
			throw new ExceptionNbcApiStateError("ResultJson中state不等于true!");
		}
		return lsResult;
	}

	/**
	 * 返回值为{@link ResultJson}，且{@linkplain ResultJson#getResult()}为对象时使用<br>
	 * 当返回{@link ResultJson}格式且{@linkplain ResultJson#isState()}为true时返回对象T<br>
	 * 当返回{@link ResultJson}格式{@linkplain ResultJson#isState()}为false时抛出异常，异常信息为{@linkplain ResultJson#getMessage()}<br>
	 * 其他情况抛出对应的异常
	 * 
	 * @param url
	 *            请求url
	 * @param params
	 *            请求参数
	 * @param encryptionType
	 *            加密类型
	 * @param clazz
	 * @return
	 */
	public static <T> T post4ResultJsonOne(String url, Map<String, Object> params, int encryptionType, Class<T> clazz) {
		JSONObject jsonObject = JSON.parseObject(post4String(url, params, encryptionType));
		T result = null;
		if (jsonObject.getBooleanValue("state")) {
			JSONObject obj = jsonObject.getJSONObject("result");
			result = parseToJava(obj, clazz);
		} else {
			throw new ExceptionNbcApiStateError("ResultJson中state不等于true!");
		}
		return result;
	}

	/**
	 * 返回值为普通Json格式的列表时调用<br>
	 * 如: [{a:"a"},{b:"b"},{c:"c"}]<br>
	 * 如果列表中对象没有对应的java类，请使用{@link JSONObject}对象
	 * 
	 * @param url
	 *            请求url
	 * @param params
	 *            请求参数
	 * @param encryptionType
	 *            加密类型
	 * @param clazz
	 * @return
	 */
	public static <T> List<T> post4JsonList(String url, Map<String, Object> params, int encryptionType,
			Class<T> clazz) {
		JSONArray array = JSON.parseArray(post4String(url, params, encryptionType));
		List<T> lsResult = new ArrayList<>();

		for (int i = 0; i < array.size(); i++) {
			JSONObject obj = array.getJSONObject(i);
			T tObj =  parseToJava(obj, clazz);
			lsResult.add(tObj);
		}
		return lsResult;
	}
	
	public static ResultJson postEncryp(String requestUrl, Map<String, Object> params, String authCode) {
		Map<String, Object> paramsEn = Crypter.encryptHttpParams(requestUrl.substring(requestUrl.lastIndexOf("/") + 1),
				JSON.toJSONString(params));
		try {
			// paramsEn已经加密，HttpJsonUtil不需要二次处理
			paramsEn.put("authCode", authCode); // 添加authCode
			return HttpJsonUtil.post4JsonOne(requestUrl, paramsEn, HttpJsonUtil.ENCRYTION_TYPE_NONE, ResultJson.class);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * 返回值为普通Json格式时(非列表)调用<br>
	 * 如: {a:"a", b:[1,2,3], c:{name:"im c"}}<br>
	 * 如果列表中对象没有对应的java类，请使用{@link JSONObject}对象
	 * 
	 * @param url
	 *            请求url
	 * @param params
	 *            请求参数
	 * @param encryptionType
	 *            加密类型
	 * @param clazz
	 * @return
	 */
	public static <T> T post4JsonOne(String url, Map<String, Object> params, int encryptionType, Class<T> clazz) {
		JSONObject jsonObject = JSON.parseObject(post4String(url, params, encryptionType));
		T result =  parseToJava(jsonObject, clazz);
		return result;
	}

	/**
	 * 获取String格式的json返回值
	 * 
	 * @param url
	 * @param params
	 * @param encryptionType
	 * @return
	 */
	private static String post4String(String url, Map<String, Object> params, int encryptionType) {
		Map<String, Object> paramsEn = null;
		switch (encryptionType) {
		case ENCRYTION_TYPE_RSA:
			if (params == null) {
				params = new HashMap<>();
			}
			if (params.isEmpty()) {
				params.put("stub", "0");
			}
			paramsEn = Crypter.encryptHttpParams(url.substring(url.lastIndexOf("/") + 1), JSON.toJSONString(params));
			break;
		case ENCRYTION_TYPE_NONE:
		default:
			paramsEn = params;
			break;
		}
		return HttpUtil.doPost(url, paramsEn, null);
	}
	
	/**
	 * 将json对象转换成java对象，如果clazz等于JSONObject.class强转类型
	 * @param jsonObj
	 * @param clazz
	 * @return
	 */
	private static <T> T parseToJava(JSONObject jsonObj, Class<T> clazz){
		T obj = null;
		if(JSONObject.class.equals(clazz)) {
			obj = (T) jsonObj;
		}else {
			obj = jsonObj.toJavaObject(clazz);
		}
		return obj;
	}

}
