package com.novelbio.base.dataOperate;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;

public interface IHttpFetch {
	
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
