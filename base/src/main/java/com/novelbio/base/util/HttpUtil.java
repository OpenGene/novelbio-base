package com.novelbio.base.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * HTTP 请求工具类
 *
 */
public class HttpUtil {
	private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
	
	private static PoolingHttpClientConnectionManager connMgr;
	private static RequestConfig requestConfig;
	private static final int MAX_TIMEOUT = 120_000;
	private static SSLConnectionSocketFactory sslsf = null;
	static {
		sslsf = SSLConnectionSocketFactory.getSystemSocketFactory();
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.INSTANCE).register("https", sslsf).build();
		// 设置连接池
		connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		// 设置连接池大小
		connMgr.setMaxTotal(100);
		connMgr.setDefaultMaxPerRoute(connMgr.getMaxTotal());
		// 在提交请求之前 测试连接是否可用
		connMgr.setValidateAfterInactivity(MAX_TIMEOUT);

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
	 * 发送 GET 请求（HTTP），不带输入数据
	 * 
	 * @param url
	 * @return
	 */
	public static String doGet(String url) {
		return doGet(url, new HashMap<String, Object>());
	}

	/**
	 * 发送 GET 请求（HTTP），K-V形式 TODO 网上下的工具类GET请求未使用过
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public static String doGet(String url, Map<String, Object> params) {
		return doGetWithCookie(url, params, null);
	}
	
	public static String doGetWithCookie(String url, Map<String, Object> params, CookieStore cookieStore) {
		String apiUrl = url;
		StringBuffer param = new StringBuffer();
		int i = 0;
		for (String key : params.keySet()) {
			if (i == 0) {
				param.append("?");
			} else {
				param.append("&");
			}
			param.append(key).append("=").append(params.get(key));
			i++;
		}
		apiUrl += param;
		String result = null;
		CloseableHttpClient httpClient = null;
		HttpResponse response = null;
		try {
			httpClient = getHttpClient(apiUrl.startsWith("https"), cookieStore);
			HttpGet httpPost = new HttpGet(apiUrl);
			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream instream = entity.getContent();
				result = IOUtils.toString(instream, "UTF-8");
				IOUtil.close(instream);
			}
		} catch (IOException e) {
			logger.error("", e);
			throw new RuntimeException(e);
		} finally {
			IOUtil.close(httpClient);
			closeResponse(response);
		}
		return result;
	}

	/**
	 * 发送 POST 请求（HTTP），K-V形式.支持https
	 * 
	 * @param apiUrl
	 *            API接口URL
	 * @param params
	 *            参数map
	 * @param headers
	 *            请求头信息
	 * @return
	 */
	public static String doPost(String apiUrl, Map<String, Object> params, Map<String, String> headers) {
		String httpStr = null;
		CloseableHttpResponse response = null;
		try {
			response = doPost2Response(apiUrl, params, headers);
			HttpEntity entity = response.getEntity();
			httpStr = EntityUtils.toString(entity, "UTF-8");
		} catch (IOException e) {
			logger.error("", e);
			throw new RuntimeException(e);
		} finally {
			closeResponse(response);
		}
		return httpStr;
	}
	
	public static String doPost(String apiUrl, Map<String, Object> params, Map<String, String> headers, CookieStore cookieStore) {
		String httpStr = null;
		CloseableHttpResponse response = null;
		try {
			response = doPost2Response(apiUrl, params, headers);
			HttpEntity entity = response.getEntity();
			httpStr = EntityUtils.toString(entity, "UTF-8");
		} catch (IOException e) {
			logger.error("", e);
			throw new RuntimeException(e);
		} finally {
			closeResponse(response);
		}
		return httpStr;
	}
	
	public static CloseableHttpResponse doPost2Response(String apiUrl, Map<String, Object> params, Map<String, String> headers) {
		return doPostWithCookie(apiUrl, params, headers, null);
	}
	
	public static CloseableHttpResponse doPostWithCookie(String apiUrl, Map<String, Object> params, Map<String, String> headers, CookieStore cookieStore) {
		CloseableHttpClient httpClient = null;
		try {
			HttpPost httpPost = new HttpPost(apiUrl);
			// 设置 header
			Header headerss[] = buildHeader(headers);
			if (headerss != null && headerss.length > 0) {
				httpPost.setHeaders(headerss);
			}
			httpPost.setConfig(requestConfig);
			if (params != null) {
				List<NameValuePair> pairList = new ArrayList<>(params.size());
				for (Map.Entry<String, Object> entry : params.entrySet()) {
					NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue() == null ? null : entry.getValue().toString());
					pairList.add(pair);
				}
				httpPost.setEntity(new UrlEncodedFormEntity(pairList, Charset.forName("UTF-8")));
			}
			
			httpClient = getHttpClient(apiUrl.startsWith("https"), cookieStore);
			return httpClient.execute(httpPost);
		} catch (IOException e) {
			logger.error("", e);
			throw new RuntimeException(e);
		} 
	}
	
	private static CloseableHttpClient getHttpClient(boolean isHttps, CookieStore cookieStore) {
		CloseableHttpClient httpClient = null;
		if (isHttps) {
			httpClient = cookieStore == null ? SeeSSLCloseableHttpClient.getCloseableHttpClient() : SeeSSLCloseableHttpClient.getCloseableHttpClient(cookieStore);
		} else {
			httpClient = cookieStore == null ? HttpClients.createDefault() : HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		}
		
		return httpClient;
	}

	/**
	 * 组装请求头
	 * 
	 * @param params
	 * @return
	 */
	protected static Header[] buildHeader(Map<String, String> params) {
		if (params == null || params.size() <= 0) {
			return null;
		}

		Header[] headers = new BasicHeader[params.size()];
		int i = 0;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			headers[i] = new BasicHeader(entry.getKey(), entry.getValue());
			i++;
		}
		return headers;
	}

	public static void closeResponse(HttpResponse response) {
		if (response == null) return;
		try {
			EntityUtils.consume(response.getEntity());
		} catch (IOException e) {
			logger.error("", e);
		}
	}

	/** 注意:这里的cookie只适用单个客户端连接 */
	public static Cache<String, String> Cookie = CacheBuilder.newBuilder().expireAfterAccess(8, TimeUnit.HOURS).build();
	
	public static void saveCookies(HttpResponse response) {
		Header[] headers = response.getHeaders("Set-Cookie");
		if (headers == null) {
			return;
		}

		for (int i = 0; i < headers.length; i++) {
			String cookie = headers[i].getValue();
			String[] cookievalues = cookie.split(";");
			for (int j = 0; j < cookievalues.length; j++) {
				String[] keyPair = cookievalues[j].split("=");
				String key = keyPair[0].trim();
				String value = keyPair.length > 1 ? keyPair[1].trim() : "";
				HttpUtil.Cookie.put(key, value);
			}
		}
		closeResponse(response);
	}
	
	 /**
     * 增加Cookie
     * 
     * @param request
     */
    @SuppressWarnings("rawtypes")
	public static String getCookies() {
		StringBuilder sb = new StringBuilder();
		Iterator iter = Cookie.asMap().entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = entry.getKey().toString();
			String val = entry.getValue().toString();
			sb.append(key);
			sb.append("=");
			sb.append(val);
			sb.append(";");
		}
		return sb.toString();
	}
    
}

/**
 * 保证https请求可以正常访问
 * 
 * @author novelbio
 *
 */
class SeeSSLCloseableHttpClient {

	private static X509TrustManager tm = new X509TrustManager() {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	};

	/**
	 * liqi-修改---方法重构增加参数
	 * 
	 * @return
	 */
	public static CloseableHttpClient getCloseableHttpClient() {
		return getCloseableHttpClient(null);
	}

	/**
	 * liqi--新增--getCloseableHttpClient()方法增加参数CookieStore。<br>
	 * 增加对cookieStore的支持
	 * 
	 * @param cookieStore
	 * @return
	 */
	public static CloseableHttpClient getCloseableHttpClient(CookieStore cookieStore) {
		SSLContext ctx = null;
		try {
			ctx = SSLContext.getInstance("TLS");
			ctx.init(null, new TrustManager[] { tm }, null);
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		HttpClientBuilder builder = HttpClientBuilder.create();
		SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(ctx,
				NoopHostnameVerifier.INSTANCE);
		builder.setSSLSocketFactory(sslConnectionFactory);
		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("https", sslConnectionFactory).build();
		HttpClientConnectionManager ccm1 = new BasicHttpClientConnectionManager(registry);
		builder.setConnectionManager(ccm1);

		// 添加cookieStore
		if (cookieStore != null) {
			builder.setDefaultCookieStore(cookieStore);
		}
		return builder.build();
	}
	
}
