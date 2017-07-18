package com.novelbio.base.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.fileOperate.FileOperate;

public class HttpUtil {
	private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
	
	private static final String CHARSET = "utf-8";
	
	/**
	 * 发送 post 请求
	 * @param url
	 * @param encode
	 * @param headers
	 * @return
	 */
	public static String httpPost(String url, Map<String, Object> params, Map<String,String> headers){
		String content = null;
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient closeableHttpClient = httpClientBuilder.build();  

        HttpPost httpPost = new HttpPost(url);
       
        //设置 header
        Header headerss[] = buildHeader(headers);
        if(headerss != null && headerss.length > 0){
        	httpPost.setHeaders(headerss);
        }
        List<NameValuePair> pairs = null;
        if (params != null && !params.isEmpty()) {
            pairs = new ArrayList<NameValuePair>(params.size());
            for (String key : params.keySet()) {
                pairs.add(new BasicNameValuePair(key, params.get(key).toString()));
            }
        }
        if (pairs != null && pairs.size() > 0) {
            try {
				httpPost.setEntity(new UrlEncodedFormEntity(pairs, CHARSET));
			} catch (UnsupportedEncodingException e) {
				logger.error("setEntity error.", e);
				throw new RuntimeException(e);
			}
        }
        
        HttpResponse http_response;
		try {
			http_response = closeableHttpClient.execute(httpPost);
			HttpEntity entity = http_response.getEntity();
			content = EntityUtils.toString(entity, CHARSET);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}finally {
            httpPost.releaseConnection();
            FileOperate.close(closeableHttpClient);
        }
        return content;
	}
	
	/**
	 * 组装请求头
	 * @param params
	 * @return
	 */
    public static Header[] buildHeader(Map<String,String> params){
    	Header[] headers = null;
    	if(params != null && params.size() > 0){
    		headers = new BasicHeader[params.size()];
    		int i  = 0;
    		for (Map.Entry<String, String> entry:params.entrySet()) {
    			headers[i] = new BasicHeader(entry.getKey(),entry.getValue());
    			i++;
    		}
    	}
    	return headers;
    }
	
    public static void main(String[] args) {
//    	Session session = SecurityUtils.getSubject().getSession();
    	String sessionId = "5b476ea8-cedc-4853-a273-2280a67114bc";
		Map<String,String> header = new HashMap<String,String>();
		header.put("Cookie", "sid=" + sessionId);
		String res = HttpUtil.httpPost("http://www.test.com/gold_getdaliygold", null, header);
		System.out.println("res=" + res);
	}
    
}
