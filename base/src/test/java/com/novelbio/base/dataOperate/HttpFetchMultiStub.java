package com.novelbio.base.dataOperate;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;

public class HttpFetchMultiStub implements IHttpFetch {

	@Override
	public void setRefUri(String refUrl) {		
	}

	@Override
	public void setRefUri(URI refUri) {
	}

	@Override
	public CookieStore getCookies() {
		return null;
	}

	@Override
	public void download(URI uri, Map<String, String> mapPostParam,
			String fileName) throws ClientProtocolException, IOException {		
	}

	@Override
	public void download(String uri, String fileName)
			throws ClientProtocolException, IOException {		
	}

	@Override
	public String queryGetUriStr(String uri) throws ClientProtocolException,
			IOException {
		return null;
	}

	@Override
	public Iterable<String> queryGetUriIt(String uri)
			throws ClientProtocolException, IOException {
		return null;
	}

	@Override
	public String queryPostUriStr(String uri, List<String[]> lsPostParam)
			throws ClientProtocolException, IOException {
		return null;
	}

	@Override
	public Iterable<String> queryPostUriIt(String uri,
			List<String[]> lsPostParam) throws ClientProtocolException,
			IOException {
		return null;
	}

	@Override
	public void close() {		
	}

}
