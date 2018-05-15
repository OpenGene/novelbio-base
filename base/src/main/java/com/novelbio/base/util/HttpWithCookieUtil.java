package com.novelbio.base.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpUtils;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.novelbio.base.fileOperate.FileOperate;
import com.novelbio.base.security.Crypter;

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

	private static final int MAX_TIMEOUT = 120_000;
	private static RequestConfig requestConfig;
	static {
		RequestConfig.Builder configBuilder = RequestConfig.custom();
		// 设置连接超时
		configBuilder.setConnectTimeout(MAX_TIMEOUT);
		// 设置读取超时
		configBuilder.setSocketTimeout(MAX_TIMEOUT);
		// 设置从连接池获取连接实例的超时
		configBuilder.setConnectionRequestTimeout(MAX_TIMEOUT);
		requestConfig = configBuilder.build();
	}

	/**
	 * 实例化的对象一定要关闭
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

		String httpStr = null;
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;

		try {
			HttpPost httpPost = new HttpPost(url);
			// 设置 header
			Header headerss[] = HttpUtil.buildHeader(headers);
			if (headerss != null && headerss.length > 0) {
				httpPost.setHeaders(headerss);
			}
			httpPost.setConfig(requestConfig);
			if (params != null) {
				List<NameValuePair> pairList = new ArrayList<>(params.size());
				for (Map.Entry<String, Object> entry : params.entrySet()) {
					NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue().toString());
					pairList.add(pair);
				}
				httpPost.setEntity(new UrlEncodedFormEntity(pairList, Charset.forName("UTF-8")));
			}
			if (url.startsWith("https")) {
				httpClient = SeeSSLCloseableHttpClient.getCloseableHttpClient(cookieStore);
			} else {
				httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
			}

			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			httpStr = EntityUtils.toString(entity, "UTF-8");
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			FileOperate.close(httpClient);
			HttpUtil.closeResponse(response);
		}
		return httpStr;
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
		String apiUrl = url;
		StringBuffer param = new StringBuffer();
		int i = 0;
		if (params == null) {
			params = new HashMap<>();
		}
		
		for (String key : params.keySet()) {
			if (i == 0)
				param.append("?");
			else
				param.append("&");
			param.append(key).append("=").append(params.get(key));
			i++;
		}
		apiUrl += param;
		String result = null;
		CloseableHttpClient httpClient = null;
		HttpResponse response = null;
		try {
			if (url.startsWith("https")) {
				httpClient = SeeSSLCloseableHttpClient.getCloseableHttpClient(cookieStore);
			} else {
				httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
			}
			HttpGet httpGet = new HttpGet(apiUrl);
			response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				result = IOUtils.toString(instream, "UTF-8");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			FileOperate.close(httpClient);
			HttpUtil.closeResponse(response);
		}
		return result;
	}
}
