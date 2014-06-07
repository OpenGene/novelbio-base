package com.novelbio.base.dataOperate;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLHandshakeException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParamBean;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
//import com.sun.xml.internal.xsom.impl.WildcardImpl.Other;

/**
 * <b>单个HttpFetch用完后务必调用方法{@link #close()} 来释放连接，但是不能使用close</b><br>
 * 一次只能选择一项，要么post，要么get
 * 
 * 还没有设定获得网页的信息，譬如404或者200等
 * 
 * 首先query()，返回一个是否成功的标签。
 * 如果通过了则调用readResponse()或者download()
 * @author zongjie
 *
 */
public class HttpFetch implements Closeable {
	private static Logger logger = Logger.getLogger(HttpFetch.class);
	
	public static final int HTTPTYPE_POST = 2;
	public static final int HTTPTYPE_GET = 4;
	public static final int HTTPTYPE_HEAD = 12;
	/**
	 * 下载缓冲
	 */
	private final static int BUFFER = 1024;
	
	static PoolingClientConnectionManager cm;
	
	ArrayList<BasicHeader> lsHeaders = new ArrayList<BasicHeader>();
	
	URI uri;
	DefaultHttpClient httpclient;
	
	HttpRequestBase httpRequest;
	/** post提交的参数 */
	UrlEncodedFormEntity postEntity;
	
	/** 好像httpclient会自动保存cookie */
	CookieStore cookieStore;

	InputStream instream;
	
	boolean querySucess;
	
	int methodType = HTTPTYPE_GET;
	private Charset charset;
	
	/**
	 * 不是单例，实际使用的时候get一次就可<br>
	 * 实际使用的时候如果get多个实例，可能会耗尽连接池
	 * @return
	 */
	public static HttpFetch getInstance() {
		return new HttpFetch();
	}
	/** 返回共用一个连接池的webFetch
	 * @param num 值必须大于等于1
	 */
	public static ArrayList<HttpFetch> getInstanceLs(int num) {
		ArrayList<HttpFetch> lsResult = new ArrayList<HttpFetch>();
		HttpFetch webFetch = new HttpFetch();
		lsResult.add(webFetch);
		for (int i = 0; i < num - 1; i++) {
			HttpFetch webFetch2 = new HttpFetch(webFetch.httpclient);
			lsResult.add(webFetch2);
		}
		return lsResult;
	}
	/** 返回与输入的webFetch共用同一个连接池的webFetch */
	public static HttpFetch getInstance(HttpFetch webFetch) {
		if (webFetch == null) {
			return new HttpFetch();
		}
		return new HttpFetch(webFetch.httpclient);
	}
	private HttpFetch() {
		initial(null);
		setHeader();
	}
	private HttpFetch(DefaultHttpClient httpClient) {
		initial(httpClient);
		setHeader();
	}
	private void initial(DefaultHttpClient httpClient) {
		if (httpClient != null) {
			this.httpclient = httpClient;
			return;
		}
		initialCM();
		httpclient = new DefaultHttpClient(cm);
		//设定重试
		httpclient.setHttpRequestRetryHandler(new MyRetryHandler());
		//设定http query的参数等
		HttpParams httpParams = new BasicHttpParams();
		HttpProtocolParamBean paramsBean = new HttpProtocolParamBean(httpParams); 
		paramsBean.setVersion(HttpVersion.HTTP_1_1);
		paramsBean.setContentCharset("UTF8");
		paramsBean.setUseExpectContinue(true);
		httpclient.setParams(httpParams);
		httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
		httpclient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, true);
		//重定向的策略，遇到301或者302也继续重定向
		 httpclient.setRedirectStrategy(new DefaultRedirectStrategy() {                
			 public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)  {
				 boolean isRedirect=false;
				 try {
					 isRedirect = super.isRedirected(request, response, context);
				 } catch (ProtocolException e) {
					 // TODO Auto-generated catch block
					 e.printStackTrace();
				 }
				 if (!isRedirect) {
					 int responseCode = response.getStatusLine().getStatusCode();
					 if (responseCode == 301 || responseCode == 302) {
						 return true;
					 }
				 }
				 return isRedirect;
			 }
		 });
	}
	private static PoolingClientConnectionManager initialCM(){
		if (cm == null) {
			SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
			schemeRegistry.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
			cm = new PoolingClientConnectionManager(schemeRegistry);
			// Increase max total connection to 200
			cm.setMaxTotal(20);//setMaxTotalConnections(200);
			// Increase default max connection per route to 20
			cm.setDefaultMaxPerRoute(10);
			// Increase max connections for localhost:80 to 50
			HttpHost localhost = new HttpHost("locahost", 80);
			cm.setMaxPerRoute(new HttpRoute(localhost), 50);
		}
		return cm;
	
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
	public void setUri(String uri) {
		if (uri == null) {
			return;
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
		querySucess = false;
	}
	public void setUri(URI uri) {
		if (uri == null) {
			return;
		}
		this.uri = uri;
		querySucess = false;
	}
	/** 设定post提交的参数，设定后默认改为post method */
	public void setPostParam(List<String[]> lsKey2Value) {
		Map<String, String> mapKey2Value = new HashMap<String, String>();
		for (String[] strings : lsKey2Value) {
			mapKey2Value.put(strings[0], strings[1]);
		}
		setPostParam(mapKey2Value);
	}
	/** 设定post提交的参数，设定后默认改为post method */
	public void setPostParam(Map<String, String> mapKey2Value) {
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
		methodType = HTTPTYPE_POST;
	}
	public void setCookies(CookieStore cookieStore) {
		httpclient.setCookieStore(cookieStore);
	}
	/** 运行之后获得cookies */
	public CookieStore getCookies() {
		return cookieStore;
	}
	/** 是否成功query */
	public boolean isQuerySucess() {
		return querySucess;
	}
	/** 读取的网页的string格式，读取出错则返回null */
	public String getResponse() {
		String result = "";
		if (!querySucess) {
			return null;
		}
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
		if (!querySucess) {
			return null;
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(instream, charset));
		return reader;
	}
	public boolean download(String fileName) {
		try {
			return downloadExp(fileName);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	/** 是否成功下载 
	 * @throws IOException 
	 * @throws ClientProtocolException */
	private boolean downloadExp(String fileName) throws ClientProtocolException, IOException {
		if (!querySucess) {
			return false;
		}
		File file = new File(fileName);
		FileOutputStream out = new FileOutputStream(file);
		byte[] b = new byte[BUFFER];
		int len = 0;
		while ((len = instream.read(b)) != -1) {
			out.write(b, 0, len);
		}
		instream.close();
		out.flush();
		out.close();
		out = null;
		return true;
	}
	/** 默认重试2次的query */
	public boolean query() {
		return query(2);
	}
	/** 重试若干次,在0-100之间 */
	public boolean query(int retryNum) {
		if (retryNum <= 0 || retryNum > 100) {
			retryNum = 2;
		}
		try {
			//重试好多次
			int queryNum = 0;
			while (!querySucess) {
				getResponseExp();
				queryNum ++;
				if (queryNum > retryNum) {
					break;
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return querySucess;
	}
	
	/** 重试若干次,在0-100之间，如果没成功则抛出runtime异常 */
	public void queryExp(int retryNum) {
		if (retryNum <= 0 || retryNum > 100) {
			retryNum = 2;
		}
		Exception exp = null;
		try {
			//重试好多次
			int queryNum = 0;
			while (!querySucess) {
				getResponseExp();
				queryNum ++;
				if (queryNum > retryNum) {
					break;
				}
			}
		} catch (ClientProtocolException e) {
			exp = e;
		} catch (IOException e) {
			exp = e;
		}
		if(!querySucess) {
			throw new RuntimeException("query error:" + uri, exp);
		}
	}
	
	/**
	 * 返回null 表示没有成功
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private void getResponseExp() throws ClientProtocolException, IOException {
		querySucess = false;
		closeStream();
		instream = null;
		HttpResponse httpResponse = null;
		try {
			httpResponse = httpclient.execute(getQuery());
		} catch (Exception e) {
			logger.error("query出错：" + uri);
			return;
		}
		int httpStatusCode = httpResponse.getStatusLine().getStatusCode();
		
		if (httpStatusCode/100 == 4 || httpStatusCode/100 == 5) {
			querySucess = false;
		}
		synchronized (this) {
			cookieStore = httpclient.getCookieStore();
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
		querySucess = true;
	}
	
	private HttpUriRequest getQuery() {
		if (methodType == HTTPTYPE_GET) {
			httpRequest = new HttpGet(uri);
		} else if (methodType == HTTPTYPE_POST) {
			httpRequest = new HttpPost(uri);
			((HttpPost)httpRequest).setEntity(postEntity);
			methodType = HTTPTYPE_GET;
			postEntity = null;
		} else if (methodType == HTTPTYPE_HEAD) {
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
		httpclient = null;
	}
	public static void ressetCM() {
		if (cm != null) {
			cm.shutdown();
		}
		cm = null;
	}
	/** html解码还很薄弱 */
	public static String decode(String inputUrl) {
		String result = "";
		try {
			result = URLDecoder.decode(inputUrl, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			logger.error("解码出错：" + inputUrl);
		}
		result = result.replace("&amp;", "&");
		result = result.replace("&nbsp;", " ");
		return result;
	}
}
/**
 * 请求重试处理
 * @author zong0jie
 *
 */
class MyRetryHandler implements HttpRequestRetryHandler {

	
	public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
		   if (executionCount >= 5) {
			      // 如果超过最大重试次数,那么就不要继续了
			      return false;
			   }
			   if (exception instanceof NoHttpResponseException) {
			      // 如果服务器丢掉了连接,那么就重试
			      return true;
			   }
			   if (exception instanceof SSLHandshakeException) {
			      // 不要重试SSL握手异常
			      return false;
			   }
			   HttpRequest request = (HttpRequest) context.getAttribute(
			ExecutionContext.HTTP_REQUEST);
			   boolean idempotent = !(request instanceof
			   HttpEntityEnclosingRequest);
			   if (idempotent) {
			      // 如果请求被认为是幂等的,那么就重试
			      return true;
			   }

		return false;
	}
	
}

class WebFetchIdleConnectionMonitorThread extends Thread {
	private final ClientConnectionManager connMgr;
	private volatile boolean shutdown;

	public WebFetchIdleConnectionMonitorThread(ClientConnectionManager connMgr) {
		super();
		this.connMgr = connMgr;
	}

	@Override
	public void run() {
		try {
			while (!shutdown) {
				synchronized (this) {
					wait(5000);
					// 关闭过期连接
					connMgr.closeExpiredConnections();
					// 可选地，关闭空闲超过30秒的连接
					connMgr.closeIdleConnections(300, TimeUnit.SECONDS);
				}
			}
		} catch (InterruptedException ex) {
			// 终止
		}
	}

	public void shutdown() {
		shutdown = true;
		synchronized (this) {
			notifyAll();
		}
	}
}
