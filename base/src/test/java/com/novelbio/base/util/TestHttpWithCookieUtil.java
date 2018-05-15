package com.novelbio.base.util;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.novelbio.base.security.Crypter;

/**
 * 
 * @author novelbio liqi
 * @date 2018年5月14日 下午1:50:24
 */
public class TestHttpWithCookieUtil {

	public static final Logger logger = LoggerFactory.getLogger(TestHttpWithCookieUtil.class);
	static HttpWithCookieUtil util = null;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setBeferClass() throws Exception {
		util = new HttpWithCookieUtil();
		// 登陆
		String loginUrl = "http://cloud.test.com/login";
		HashMap<String, Object> mapLogin2Val = new HashMap<>();
		mapLogin2Val.put("username", "admin");
		String passwd = Crypter.signByMD5("admin123");
		mapLogin2Val.put("password", passwd);
		String webContext = util.doPost(loginUrl, mapLogin2Val, null);
		logger.info(webContext);
		JSONObject json = JSON.parseObject(webContext);
		assertTrue((boolean) json.get("state"));
	}

	@Test
	public void testDoGet() {
		// 判断是否登陆
		String isLoginUrl = "http://cloud.test.com/isLogin";
		String webContext = util.doGet(isLoginUrl, null);
		logger.info(webContext);
		JSONObject json = JSON.parseObject(webContext);
		assertTrue((boolean) json.get("state"));
	}

	@Test
	public void testDoPost() {
		// 获取authCode--一个action
		String url = "http://cloud.test.com/getOAuth2";
		Map<String, Object> mapPost2Test = new HashMap<>();
		mapPost2Test.put("action", "api/v201801/getSerivceTime"); // 需要数据库中的权限项
		String webContext = util.doPost(url, mapPost2Test, null);
		logger.info(webContext);
		JSONObject json = JSON.parseObject(webContext);
		assertTrue((boolean) json.get("state"));

		// 获取authCode--多个action
		 url = "http://cloud.test.com/getOAuth2";
		mapPost2Test = new HashMap<>();
		mapPost2Test.put("action", "api/v201801/getSerivceTime"); // 需要数据库中的权限项
		mapPost2Test.put("action", "api/v201801/getDataValues");
		mapPost2Test.put("action", "api/v201801/getDataDictionary");
		 webContext = util.doPost(url, mapPost2Test, null);
		logger.info(webContext);
		 json = JSON.parseObject(webContext);
		assertTrue((boolean) json.get("state"));
	}

}
