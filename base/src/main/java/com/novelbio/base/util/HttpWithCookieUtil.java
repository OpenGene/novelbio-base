package com.novelbio.base.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;



/**
 * 使用httpClient登陆，获取authCode等操作时使用<br>
 * 每个实例保存对应对应的cookie信息<br>
 * 销毁实例时清空cookie，实例由调用者维护
 * 
 * @author novelbio liqi
 * @date 2018年5月11日 下午4:11:44
 */
public class HttpWithCookieUtil {

	CookieStore cookieStore = null;

	/**
	 * 每个实例持有一个cookieStore。要使用cookie信息，需要在一个实例上处理
	 */
	public HttpWithCookieUtil() {
		cookieStore = new BasicCookieStore();
	}

	/**
	 * post请求
	 * 
	 * @param url
	 *            地址
	 * @param params
	 *            参数
	 * @param headers
	 *            请求header参数
	 * @return
	 */
	public String doPost(String url, Map<String, Object> params, Map<String, String> headers) {
		return HttpUtil.doPost(url, params, headers, cookieStore);
	}

	/**
	 * 发送 GET 请求（HTTP），不带输入数据
	 * 
	 * @param url
	 * @return
	 */
	public String doGet(String url) {
		return doGet(url, new HashMap<String, Object>());
	}

	/**
	 * 发送 GET 请求（HTTP），K-V形式
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public String doGet(String url, Map<String, Object> params) {
		return HttpUtil.doGetWithCookie(url, params, cookieStore);
	}
}
