package com.novelbio.base.dataOperate;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.NoRouteToHostException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import com.novelbio.base.ExceptionNbcParamError;
import com.novelbio.base.StringOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 本类适合高并发的连接使用，如并发访问某个client<br>
 * 如果只是普通的爬虫，推荐使用
 * htmlUnit的{@link com.gargoylesoftware.htmlunit.WebClient}，那个更简单易用<br>
 * <b>单个HttpFetch用完后务必调用方法{@link #close()} 来释放连接</b><br>
 * 一次只能选择一项，要么post，要么get
 * 
 * 还没有设定获得网页的信息，譬如404或者200等
 * 
 * 首先query()，返回一个是否成功的标签。
 * 如果通过了则调用readResponse()或者download()
 * @author zongjie
 */
public class HttpFetchMultiThread implements IHttpFetch, Closeable {
	private static Logger logger = Logger.getLogger(HttpFetchMultiThread.class);
	
	public static final int HTTPTYPE_POST = 2;
	public static final int HTTPTYPE_GET = 4;
	public static final int HTTPTYPE_HEAD = 12;
	
	PoolingHttpClientConnectionManager cm;
		
	ArrayList<BasicHeader> lsHeaders = new ArrayList<BasicHeader>();
	
	CloseableHttpClient httpclient;

	/** 好像httpclient会自动保存cookie */
	CookieStore cookieStore;
	
	/** http连接超时 */
	int connectTimeout = -1;
	/** 数据传输超时 */
	int socketTimeout = -1;
	
	/** 不是单例，实际使用的时候get一次就可 */
	public static HttpFetchMultiThread getInstance() {
		return getInstance(200, 10, null);
	}
	/** 不是单例，实际使用的时候get一次就可 */
	public static HttpFetchMultiThread getInstance(int maxConnect, int maxConnectPerRoute) {
		return getInstance(200, 10, null);
	}
	/** 不是单例，实际使用的时候get一次就可
	 * 默认最大连接数 1000
	 * @param cookieStore
	 * @return
	 */
	public static HttpFetchMultiThread getInstance(CookieStore cookieStore) {
		return getInstance(200, 10, cookieStore);
	}
	/** 不是单例，实际使用的时候get一次就可
	 * @param maxConnect 最大连接数
	 * @param maxConnectPerRoute 最大单个网站的连接数
	 * @param cookieStore
	 * @return
	 */
	public static HttpFetchMultiThread getInstance(int maxConnect, int maxConnectPerRoute, CookieStore cookieStore) {
		try {
			return new HttpFetchMultiThread(maxConnect,maxConnectPerRoute, cookieStore);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setCookieStore(CookieStore cookieStore) {
		this.cookieStore = cookieStore;
	}
	/**
	 * 一次http请求，必定会有三个阶段，一：建立连接；二：数据传送；三，断开连接
	 * 当建立连接在规定的时间内（ConnectionTimeOut ）没有完成，那么此次连接就结束了。
	 * 后续的SocketTimeOutException就一定不会发生。只有当连接建立起来后，也就是没有
	 * 发生ConnectionTimeOutException ，才会开始传输数据，如果数据在
	 * 规定的时间内(SocketTimeOut)传输完毕,则断开连接。
	 * 否则，触发SocketTimeOutException
	 */
	public void setTimeoutConnect(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	/**
	 * 一次http请求，必定会有三个阶段，一：建立连接；二：数据传送；三，断开连接
	 * 当建立连接在规定的时间内（ConnectionTimeOut ）没有完成，那么此次连接就结束了。
	 * 后续的SocketTimeOutException就一定不会发生。只有当连接建立起来后，也就是没有
	 * 发生ConnectionTimeOutException ，才会开始传输数据，如果数据在
	 * 规定的时间内(SocketTimeOut)传输完毕,则断开连接。
	 * 否则，触发SocketTimeOutException
	 */
	public void setTimeoutSocket(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}
	
	private HttpFetchMultiThread(int maxConnect, int maxConnectPerRoute, CookieStore cookieStore) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
		initialCM(maxConnect,maxConnectPerRoute);
		this.cookieStore = cookieStore == null ? new BasicCookieStore() : cookieStore;
		initial(this.cookieStore);
		setHeader();
	}
	
	private void initial(CookieStore cookieStore) {
		// Use custom cookie store if necessary.
		// Use custom credentials provider if necessary.
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		// Create global request configuration
		RequestConfig defaultRequestConfig = RequestConfig.custom()
				.setCookieSpec(CookieSpecs.DEFAULT)
				.setExpectContinueEnabled(true)
				.setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
				.setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
				.setRedirectsEnabled(true)
				.build();
		
		httpclient = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultCookieStore(cookieStore)
                .setDefaultCredentialsProvider(credentialsProvider)
                .setDefaultRequestConfig(defaultRequestConfig)
                .setRetryHandler(new MyRetryHandler())
                .setRedirectStrategy(new DefaultRedirectStrategy())
                .build();
	}
	
	/**
	 * @param maxConnect 连接池的最大连接数
	 * @param maxConnectPerRoute 对于单个网址的最大连接数
	 */
	private void initialCM(int maxConnect, int maxConnectPerRoute) throws KeyManagementException, NoSuchAlgorithmException {
		// Trust own CA and all self-signed certs
		SSLContext sslcontext = getSSLContextTrustAll();//SSLContexts.custom().loadTrustMaterial(new TrustSelfSignedStrategy()).build();
//		 SSLContext sslcontext = SSLContexts.createSystemDefault();
		// Allow TLSv1 protocol only
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
				sslcontext);
		
		Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create()
		        .register("http", PlainConnectionSocketFactory.INSTANCE)
		        .register("https", sslsf)
		        .build();
		
		cm = new PoolingHttpClientConnectionManager(r);
		// Increase max total connection to 200
		cm.setMaxTotal(maxConnect);
		// Increase default max connection per route to 20
		cm.setDefaultMaxPerRoute(maxConnectPerRoute);
		
		ConnectionConfig connectionConfig = ConnectionConfig.custom()
				.setMalformedInputAction(CodingErrorAction.IGNORE)
				.setUnmappableInputAction(CodingErrorAction.IGNORE)
				.setCharset(Consts.UTF_8)
				.build();
		cm.setDefaultConnectionConfig(connectionConfig);	
	}
	
	private void setHeader() {
		lsHeaders.clear();
		lsHeaders.add(new BasicHeader("ContentType", "application/x-www-form-urlencoded"));
		lsHeaders.add(new BasicHeader("Accept-Language", "zh-cn,zh;q=0.5"));
		lsHeaders.add(new BasicHeader("Connection", "Keep-Alive"));
		lsHeaders.add(new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:15.0) Gecko/20100101 Firefox/15.0.1"));
		lsHeaders.add(new BasicHeader("Accept", "text/html, Accept:image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, application/x-shockwave-flash, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, application/x-silverlight, */* "));
		lsHeaders.add(new BasicHeader("Accept-Charset", "gb2312,utf-8,ISO-8859-1;q=0.7,*;q=0.7"));
		lsHeaders.add(new BasicHeader("UA-CPU", "x86"));
	}
	
	/** 有些网站譬如pixiv，在下载图片时需要浏览器提供最近访问的链接，而且必须是其指定的链接才能下载 */
	public void setRefUri(String refUrl) {
		if (refUrl == null) {
			return;
		}
		lsHeaders.add(new BasicHeader("Referer", refUrl));
	}	
	/** 有些网站譬如pixiv，在下载图片时需要浏览器提供最近访问的链接，而且必须是其指定的链接才能下载 */
	public void setRefUri(URI refUri) {
		if (refUri == null) {
			return;
		}
		lsHeaders.add(new BasicHeader("Referer", refUri.toString()));
	}
	
	/** 运行之后获得cookies */
	public CookieStore getCookies() {
		return cookieStore;
	}

	public void download(URI uri, Map<String, String> mapPostParam, String fileName) throws ClientProtocolException, IOException {
		HttpRequestAndResponse httpRequestAndResponse = getResponseExp(uri, getLsParam(mapPostParam));
		httpRequestAndResponse.downloadExp(fileName);
	}
	
	public void download(String uri, String fileName) throws ClientProtocolException, IOException {
		HttpRequestAndResponse httpRequestAndResponse = getResponseExp(getUri(uri));
		httpRequestAndResponse.downloadExp(fileName);
	}
	public void download(URI uri, String fileName) throws ClientProtocolException, IOException {
		HttpRequestAndResponse httpRequestAndResponse = getResponseExp(uri);
		httpRequestAndResponse.downloadExp(fileName);
	}
	public String queryGetUriStr(String uri) throws ClientProtocolException, IOException {
		HttpRequestAndResponse httpRequestAndResponse = getResponseExp(getUri(uri), null);
		return httpRequestAndResponse.getResponse();
	}
	
	public Iterable<String> queryGetUriIt(String uri) throws ClientProtocolException, IOException {
		HttpRequestAndResponse httpRequestAndResponse = getResponseExp(getUri(uri), null);
		return httpRequestAndResponse.readResponse();
	}
	
	public String queryPostUriStr(String uri, List<String[]> lsPostParam) throws ClientProtocolException, IOException {
		HttpRequestAndResponse httpRequestAndResponse = getResponseExp(getUri(uri), lsPostParam);
		return httpRequestAndResponse.getResponse();
	}
	public Iterable<String> queryPostUriIt(String uri, List<String[]> lsPostParam) throws ClientProtocolException, IOException {
		HttpRequestAndResponse httpRequestAndResponse = getResponseExp(getUri(uri), lsPostParam);
		return httpRequestAndResponse.readResponse();
	}
	
	/**
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private HttpRequestAndResponse getResponseExp(URI uri) throws NoRouteToHostException, HttpResponseException, ClientProtocolException, IOException {
		HttpRequestAndResponse httpRequestAndResponse = new HttpRequestAndResponse();
		httpRequestAndResponse.setLsHeader(lsHeaders);
		httpRequestAndResponse.setUri(uri);
		
		//设置http超时
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout).build();//设置请求和传输超时时间
		HttpRequestBase httpRequest = httpRequestAndResponse.getHttpRequest();
		httpRequest.setConfig(requestConfig);
		
		HttpResponse httpResponse = httpclient.execute(httpRequest);
		
		httpRequestAndResponse.setHttpResponse(httpResponse);
		return httpRequestAndResponse;
	}
	
	/**
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private HttpRequestAndResponse getResponseExp(URI uri, List<String[]> lsPostParam) throws NoRouteToHostException, HttpResponseException, ClientProtocolException, IOException {
		HttpRequestAndResponse httpRequestAndResponse = new HttpRequestAndResponse();
		httpRequestAndResponse.setLsHeader(lsHeaders);
		httpRequestAndResponse.setPostParam(lsPostParam);
		httpRequestAndResponse.setUri(uri);
		
		//设置http超时
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout).build();//设置请求和传输超时时间
		HttpRequestBase httpRequest = httpRequestAndResponse.getHttpRequest();
		httpRequest.setConfig(requestConfig);

		HttpResponse httpResponse = httpclient.execute(httpRequest);
		httpRequestAndResponse.setHttpResponse(httpResponse);
		return httpRequestAndResponse;
	}
	
	/** 输入网址，开头可以不加http:// */
	private URI getUri(String uri) {
		if (StringOperate.isRealNull(uri)) {
			throw new ExceptionNbcParamError("uri cannot be null");
		}
		uri = uri.trim();
		if (uri.startsWith("//")) {
			uri = "http:" + uri;
		} else if (uri.startsWith("/")) {
			uri = "http:/" + uri;
		} else if (uri.startsWith("http") || uri.startsWith("ftp")) {
			if (uri.contains("http:/") && !uri.contains("http://")) {
				uri = uri.replace("http:/", "http://");
			}
		} else {
			uri = "http://" + uri;
		}
		try {
			return new URI(uri);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	private List<String[]> getLsParam(Map<String, String> mapPostParam) {
		List<String[]> lsPostParam = new ArrayList<>();
		for (String key : mapPostParam.keySet()) {
			lsPostParam.add(new String[]{key, mapPostParam.get(key)});
		}
		return lsPostParam;
	}
	
	public void close() {
		try {
			httpclient.close();
			cm.shutdown();
			cm.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Trust every server - dont check for any certificate
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyManagementException 
	 */
	private static SSLContext getSSLContextTrustAll() throws NoSuchAlgorithmException, KeyManagementException {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}
		} };
		SSLContext sc = SSLContext.getInstance("TLS");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		return sc;
	}

}

class HttpRequestAndResponse {
	HttpRequestBase httpRequest;
	HttpResponse httpResponse;
	UrlEncodedFormEntity urlEncodedFormEntity;
	int method = HttpFetchMultiThread.HTTPTYPE_GET;
	List<BasicHeader> lsHeaders;
	
	URI uri;
	Charset charset;
	
	InputStream is;
	private final static int BUFFER = 1024;

	public void setUri(URI uri) {
		if (uri == null) {
			throw new ExceptionNbcParamError("uri cannot be null");
		}
		this.uri = uri;
	}
	
	public void setLsHeader(List<BasicHeader> lsHeader) {
		this.lsHeaders = lsHeader;
	}
	
	public void setPostParam(List<String[]> lsPostParam) throws UnsupportedEncodingException {
		if (CollectionUtils.isEmpty(lsPostParam)) {
			return;
		}
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		for (String[] key2Value : lsPostParam) {
			nvps.add(new BasicNameValuePair(key2Value[0], key2Value[1]));
		}
		urlEncodedFormEntity= new UrlEncodedFormEntity(nvps);
	}
	
	public HttpRequestBase getHttpRequest() {
		if (urlEncodedFormEntity == null) {
			method = HttpFetchMultiThread.HTTPTYPE_GET;
		} else {
			method = HttpFetchMultiThread.HTTPTYPE_POST;
		}
		
		if (method == HttpFetchMultiThread.HTTPTYPE_GET) {
			httpRequest = new HttpGet(uri);
		} else if (method == HttpFetchMultiThread.HTTPTYPE_POST) {
			httpRequest = new HttpPost(uri);
			((HttpPost)httpRequest).setEntity(urlEncodedFormEntity);
			method = HttpFetchMultiThread.HTTPTYPE_GET;
		} else if (method == HttpFetchMultiThread.HTTPTYPE_HEAD) {
			httpRequest = new HttpHead(uri);
		}
		
		httpRequest.setHeaders(lsHeaders.toArray(new BasicHeader[1]));
		return httpRequest;
	}
	
	public void setHttpResponse(HttpResponse httpResponse) throws UnsupportedOperationException, IOException {
		this.httpResponse = httpResponse;
		int httpStatusCode = httpResponse.getStatusLine().getStatusCode();
		
		if (httpStatusCode/100 == 4 || httpStatusCode/100 == 5) {
			throw new HttpResponseException(httpStatusCode, "response is not correct, http status code: " + httpStatusCode + " url " + uri.toString());
		}
		HttpEntity entity = httpResponse.getEntity();
		ContentType contentType = ContentType.getOrDefault(entity);
		charset = contentType.getCharset();
		if (charset == null) {
			charset = Charset.defaultCharset();
		}
		is = entity.getContent();
	}
	
	/**
	 * 迭代读取返回的结果
	 * @param filename
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 */
	public Iterable<String> readResponse() {
		 final BufferedReader bufread = new BufferedReader(new InputStreamReader(is, charset));

		return new Iterable<String>() {
			public Iterator<String> iterator() {
				return new Iterator<String>() {
					public boolean hasNext() {
						return line != null;
					}
					public String next() {
						String retval = line;
						line = getLine();
						return retval;
					}
					public void remove() {
						throw new UnsupportedOperationException();
					}
					String getLine() {
						String line = null;
						try {
							line = bufread.readLine();
						} catch (IOException ioEx) {
							line = null;
						}
						if (line == null) {
							closeStream();
						}
						return line;
					}
					String line = getLine();
				};
			}
		};
	}
	
	/** 读取的网页的string格式，读取出错则返回null 
	 * @throws Exception */
	public String getResponse() {
		String result = "";
		for (String content : readResponse()) {
			result = result + content + "\n";
		}
		closeStream();
		return result;
	}
	
	/** 是否成功下载 
	 * @throws IOException 
	 * @throws ClientProtocolException */
	public void downloadExp(String fileName) throws ClientProtocolException, IOException {
		String tmp = FileOperate.changeFileSuffix(fileName, "_tmp", null);
		OutputStream out = FileOperate.getOutputStream(tmp);
		byte[] b = new byte[BUFFER];
		int len = 0;
		while ((len = is.read(b)) != -1) {
			out.write(b, 0, len);
		}
		out.flush();
		out.close();
		closeStream();
		out = null;
		FileOperate.moveFile(true, tmp, fileName);
	}
	
	/** 除了httpclient 其他都关掉 */
	private void closeStream() {
		try {
			is.close();
			is = null;
		} catch (Exception e) { }
		try { httpRequest.releaseConnection(); } catch (Exception e) { }
		try { httpRequest.abort(); } catch (Exception e) { }
	}
}

/**
 * 请求重试处理
 * @author zong0jie
 *
 */
class MyRetryHandler implements HttpRequestRetryHandler {
	public boolean retryRequest(IOException exception,  int executionCount, HttpContext context) {
        if (executionCount >= 5) {
            // Do not retry if over max retry count
            return false;
        }
        if (exception instanceof InterruptedIOException) {
            // Timeout
            return false;
        }
        if (exception instanceof UnknownHostException) {
            // Unknown host
            return false;
        }
        if (exception instanceof ConnectTimeoutException) {
            // Connection refused
            return false;
        }
        if (exception instanceof SSLException) {
            // SSL handshake exception
            return false;
        }
        HttpClientContext clientContext = HttpClientContext.adapt(context);
        HttpRequest request = clientContext.getRequest();
        boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
        if (idempotent) {
            // Retry if the request is considered idempotent
            return true;
        }
        return false;
    }
}

/** 关闭过期的连接 */
class IdleConnectionMonitorThread extends Thread {
   
    private final HttpClientConnectionManager connMgr;
    private volatile boolean shutdown;
    
    public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
        super();
        this.connMgr = connMgr;
    }

    @Override
    public void run() {
        try {
            while (!shutdown) {
                synchronized (this) {
                    wait(5000);
                    // Close expired connections
                    connMgr.closeExpiredConnections();
                    // Optionally, close connections
                    // that have been idle longer than 30 sec
                    connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                }
            }
        } catch (InterruptedException ex) {
            // terminate
        }
    }
    
    public void shutdown() {
        shutdown = true;
        synchronized (this) {
            notifyAll();
        }
    }
    
}

