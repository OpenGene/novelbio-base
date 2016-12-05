package com.novelbio.base.dataOperate;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;

public interface IHttpFetch {
	
	/**
	 * 一次http请求，必定会有三个阶段，一：建立连接；二：数据传送；三，断开连接
	 * 当建立连接在规定的时间内（ConnectionTimeOut ）没有完成，那么此次连接就结束了。
	 * 后续的SocketTimeOutException就一定不会发生。只有当连接建立起来后，也就是没有
	 * 发生ConnectionTimeOutException ，才会开始传输数据，如果数据在
	 * 规定的时间内(SocketTimeOut)传输完毕,则断开连接。
	 * 否则，触发SocketTimeOutException
	 */
	public void setTimeoutConnect(int connectTimeout);
	/**
	 * 一次http请求，必定会有三个阶段，一：建立连接；二：数据传送；三，断开连接
	 * 当建立连接在规定的时间内（ConnectionTimeOut ）没有完成，那么此次连接就结束了。
	 * 后续的SocketTimeOutException就一定不会发生。只有当连接建立起来后，也就是没有
	 * 发生ConnectionTimeOutException ，才会开始传输数据，如果数据在
	 * 规定的时间内(SocketTimeOut)传输完毕,则断开连接。
	 * 否则，触发SocketTimeOutException
	 */
	public void setTimeoutSocket(int socketTimeout);
	
	/** 有些网站譬如pixiv，在下载图片时需要浏览器提供最近访问的链接，而且必须是其指定的链接才能下载 */
	public void setRefUri(String refUrl);
	/** 有些网站譬如pixiv，在下载图片时需要浏览器提供最近访问的链接，而且必须是其指定的链接才能下载 */
	public void setRefUri(URI refUri);
	
	/** 运行之后获得cookies */
	public CookieStore getCookies();

	public void download(URI uri, Map<String, String> mapPostParam, String fileName) throws ClientProtocolException, IOException;
	
	public void download(String uri, String fileName) throws ClientProtocolException, IOException;
	
	public String queryGetUriStr(String uri) throws ClientProtocolException, IOException;
	
	public Iterable<String> queryGetUriIt(String uri) throws ClientProtocolException, IOException;
	
	public String queryPostUriStr(String uri, List<String[]> lsPostParam) throws ClientProtocolException, IOException;
	public Iterable<String> queryPostUriIt(String uri, List<String[]> lsPostParam) throws ClientProtocolException, IOException;

	
	public void close();

}
