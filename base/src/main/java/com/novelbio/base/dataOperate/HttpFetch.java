package com.novelbio.base.dataOperate;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
import org.apache.http.client.methods.HttpUriRequest;
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
 * 用{@link HttpFetchMultiThread}取代
 * 如果是做网页爬虫，推荐使用
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
@Deprecated
public class HttpFetch implements Closeable {
	private static Logger logger = Logger.getLogger(HttpFetch.class);

	/**
	 * 下载缓冲
	 */
	private final static int BUFFER = 1024;
	
	PoolingHttpClientConnectionManager cm;
//	static {
//		try {
//			initialCM();
//		} catch (KeyManagementException | NoSuchAlgorithmException
//				| KeyStoreException | CertificateException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
		
	ArrayList<BasicHeader> lsHeaders = new ArrayList<BasicHeader>();
	
	URI uri;
	CloseableHttpClient httpclient;
	
	HttpRequestBase httpRequest;
	/** post提交的参数 */
	UrlEncodedFormEntity postEntity;
	
	/** 好像httpclient会自动保存cookie */
	CookieStore cookieStore;

	InputStream instream;
		
	int methodType = HttpFetchMultiThread.HTTPTYPE_GET;
	private Charset charset;
	
	/**
	 * 不是单例，实际使用的时候get一次就可<br>
	 * 实际使用的时候如果get多个实例，可能会耗尽连接池
	 * @return
	 * @throws IOException 
	 * @throws CertificateException 
	 * @throws KeyStoreException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyManagementException 
	 */
	public static HttpFetch getInstance() {
		try {
			return new HttpFetch();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/** 返回与输入的webFetch共用同一个连接池的webFetch 
	 * @throws IOException 
	 * @throws CertificateException 
	 * @throws KeyStoreException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyManagementException */
	public static HttpFetch getInstance(CookieStore cookieStore) {
		try {
			return new HttpFetch();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private HttpFetch() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
		initialCM();
		initial(cookieStore);
		setHeader();
	}
	private HttpFetch(CookieStore cookieStore) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
		initialCM();
		this.cookieStore = cookieStore;
		initial(cookieStore);
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
	
	private void initialCM() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException{
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
		cm.setMaxTotal(1000);
		// Increase default max connection per route to 20
		cm.setDefaultMaxPerRoute(100);
		
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
	/** 应该不需要设置，内部会自动判断 */
	public void setHttpType(int httpType) {
		this.methodType = httpType;
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
	
	/** 输入网址，开头可以不加http:// */
	private void setUri(String uri) {
		if (StringOperate.isRealNull(uri)) {
			throw new ExceptionNbcParamError("uri cannot be null");
		}
		uri = uri.trim().toLowerCase();
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
			this.uri = new URI(uri);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void setUri(URI uri) {
		if (uri == null) {
			throw new ExceptionNbcParamError("uri cannot be null");
		}
		this.uri = uri;
	}
	
	public void setUriGet(String uri) {
		setUri(uri);
		this.methodType = HttpFetchMultiThread.HTTPTYPE_GET; 
	}
	public void setUriGet(URI uri) {
		setUri(uri);
		this.methodType = HttpFetchMultiThread.HTTPTYPE_GET; 
	}
	public void setUriPost(String uri, List<String[]> lsPostKey2Value) {
		setUri(uri);
		setPostParam(lsPostKey2Value);
		this.methodType = HttpFetchMultiThread.HTTPTYPE_POST;
	}
	public void setUriPost(String uri, Map<String, String> mapKey2Value) {
		setUri(uri);
		setPostParam(mapKey2Value);
		this.methodType = HttpFetchMultiThread.HTTPTYPE_POST;
	}
	/** 设定post提交的参数，设定后默认改为post method */
	private void setPostParam(List<String[]> lsKey2Value) {
		Map<String, String> mapKey2Value = new HashMap<String, String>();
		for (String[] strings : lsKey2Value) {
			mapKey2Value.put(strings[0], strings[1]);
		}
		setPostParam(mapKey2Value);
	}
	/** 设定post提交的参数，设定后默认改为post method */
	private void setPostParam(Map<String, String> mapKey2Value) {
		try {
			setPostParamExp(mapKey2Value);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	private void setPostParamExp(Map<String, String> mapKey2Value) throws UnsupportedEncodingException {
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		for (Entry<String, String> key2value : mapKey2Value.entrySet()) {
			nvps.add(new BasicNameValuePair(key2value.getKey(), key2value.getValue()));
		}
		postEntity = new UrlEncodedFormEntity(nvps);
		methodType = HttpFetchMultiThread.HTTPTYPE_POST;
	}
	/** 运行之后获得cookies */
	public CookieStore getCookies() {
		return cookieStore;
	}

	/** 读取的网页的string格式，读取出错则返回null */
	public String getResponse() {
		String result = "";
		for (String content : readResponse()) {
			result = result + content + "\n";
		}
		closeStream();
		return result;
	}
	/** 最好能先判断一下是否为null
	 * 如果为null表示没有读取成功
	 * @return
	 */
	public Iterable<String> readResponse() {
		try {
			return readResponseExp();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 迭代读取返回的结果
	 * @param filename
	 * @return
	 * @throws Exception 
	 * @throws IOException
	 */
	private Iterable<String> readResponseExp() throws Exception {
		 final BufferedReader bufread =  getResponseReader();
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
	/** 获得返回的bufferReader类
	 * 貌似会自动重定向，如果不会的话，可以解析HttpResponse的头文件，获得重定向的url，然后再次get或者post
	 *  */	
	private BufferedReader getResponseReader() throws ClientProtocolException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(instream, charset));
		return reader;
	}
	public void download(String fileName) throws ClientProtocolException, IOException {
		downloadExp(fileName);
	}
	/** 是否成功下载 
	 * @throws IOException 
	 * @throws ClientProtocolException */
	private void downloadExp(String fileName) throws ClientProtocolException, IOException {
		String tmp = FileOperate.changeFileSuffix(fileName, "_tmp", null);
		OutputStream out = FileOperate.getOutputStream(tmp);
		byte[] b = new byte[BUFFER];
		int len = 0;
		while ((len = instream.read(b)) != -1) {
			out.write(b, 0, len);
		}
		instream.close();
		out.flush();
		out.close();
		out = null;
		FileOperate.moveFile(true, tmp, fileName);
	}
	/** 默认重试2次的query */
	@Deprecated
	public boolean query() {
		return query(2);
	}
	
	/** 重试若干次,在0-100之间 */
	@Deprecated
	public boolean query(int retryNum) {
		try {
			queryExp();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (retryNum <= 0 || retryNum > 100) {
			retryNum = 2;
		}
		return false;
	}
	
	/** 重试若干次,在0-100之间，如果没成功则抛出runtime异常 */
	public void queryExp() throws ClientProtocolException, UnknownHostException, ConnectException, IOException {
		queryExp(2);
	}
	
	/** 重试若干次,在0-100之间，如果没成功则抛出runtime异常 */
	@Deprecated
	public void queryExp(int retryNum) throws ClientProtocolException, UnknownHostException, ConnectException, IOException {
		if (retryNum <= 0 || retryNum > 100) {
			retryNum = 2;
		}
		IOException exp = null;
		//重试好多次
		boolean isSucess = false;
		for (int i = 0; i < retryNum; i++) {
			try {
				getResponseExp();
				isSucess = true;
				break;
			} catch (IOException e) {
				exp = e;
			}
		}
		if(!isSucess && exp != null) {
			throw exp;
		}
	}
	
	/**
	 * 返回null 表示没有成功
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private void getResponseExp() throws ClientProtocolException, HttpResponseException, IOException {
		closeStream();
		instream = null;
		HttpResponse httpResponse = null;
		httpResponse = httpclient.execute(getQuery());

		int httpStatusCode = httpResponse.getStatusLine().getStatusCode();
		
		if (httpStatusCode/100 == 4 || httpStatusCode/100 == 5) {
			throw new HttpResponseException(httpStatusCode, "response is not correct, http status code: " + httpStatusCode);
		}
		HttpEntity entity = httpResponse.getEntity();
		ContentType contentType = ContentType.getOrDefault(entity);
		charset = contentType.getCharset();
		if (charset == null) {
			charset = Charset.defaultCharset();
		}
		if (entity != null) {
			instream = entity.getContent();
		}
	}
	
	private HttpUriRequest getQuery() {
		if (methodType == HttpFetchMultiThread.HTTPTYPE_GET) {
			httpRequest = new HttpGet(uri);
		} else if (methodType == HttpFetchMultiThread.HTTPTYPE_POST) {
			httpRequest = new HttpPost(uri);
			((HttpPost)httpRequest).setEntity(postEntity);
			methodType = HttpFetchMultiThread.HTTPTYPE_GET;
			postEntity = null;
		} else if (methodType == HttpFetchMultiThread.HTTPTYPE_HEAD) {
			httpRequest = new HttpHead(uri);
		}
		
		httpRequest.setHeaders(lsHeaders.toArray(new BasicHeader[1]));
		return httpRequest;
	}
	
	/** 除了httpclient 其他都关掉 */
	private void closeStream() {
		try { instream = null; } catch (Exception e) { }
		try { httpRequest.releaseConnection(); } catch (Exception e) { }
		try { httpRequest.abort(); } catch (Exception e) { }
	}
	
	public void close() {
		closeStream();
		try {
			httpclient.close();
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
	 
//	public static void ressetCM() {
//		if (cm != null) {
//			cm.shutdown();
//		}
//		cm = null;
//	}

}

