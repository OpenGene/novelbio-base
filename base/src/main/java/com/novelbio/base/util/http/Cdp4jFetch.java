package com.novelbio.base.util.http;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.StringOperate;

import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;

/**
 * TODO-- 使用cdp4j的一个优势就是可以在通过api模拟用户行为，如果读取页面返回数据，仅解析页面，不能做对于动作，有些大材小用
 * 采用cdp4j抓取动态页面，cdp4j使用chrome浏览器的api接口<br>
 * 一个Cdp4jFetch会保持一个sessFactory对象，保证登陆session信息存在<br>
 * 调用完成，手动调用close()释放资源。
 * 
 * 几乎所有chrome可以打开的网站都可以抓取
 * 
 * @author novelbio liqi
 * @date 2018年6月25日 下午4:01:09
 */
public class Cdp4jFetch implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(Cdp4jFetch.class);

	/** 页面加载检测次数，页面通过ajax动态加载，完成时间随机 */
	public static int LOAD_CHECK_TIME = 3;

	// private static final Logger logger =
	// LoggerFactory.getLogger(Cdp4jFetch.class);

	private Launcher launcher;
	private SessionFactory sessionFactory;

	public Cdp4jFetch() {
		launcher = new Launcher();
		sessionFactory = launcher.launch(Arrays.asList("--headless", "--disable-gpu"));
	}

	/**
	 * 创建session，调用者可以自由使用session。使用完毕请调用session.close()方法
	 * 
	 * @return
	 */
	public Session createSession() {
		return sessionFactory.create();
	}

	/**
	 * 直接抓取页面内容，并关闭页面
	 * 
	 * @param url
	 * @return
	 */
	public String fetchContent(String url) {
		// 抓取到的页面内容
		String content = "";
		try (Session session = sessionFactory.create()) {
			session.navigate(url).waitDocumentReady();
			content = session.getContent();
		}
		return content;
	}

	/**
	 * 检测页面是否显示完成<br>
	 * 页面ajax加载数据时间不能确定,需要指定检查元素检查，默认重试3次，等待秒数为 (2*i*i)，即0,2,8<br>
	 * selector对应的元素text不能为空（null，空值等）
	 * 
	 * @param session
	 * @param selector
	 *            css or xpath selector
	 * @return
	 */
	public static boolean waitDynamicPageReady(Session session, String selector) {
		return waitDynamicPageReady(session, selector, LOAD_CHECK_TIME);
	}

	/**
	 * 检测页面是否显示完成<br>
	 * 页面ajax加载数据时间不能确定,需要指定检查元素检查，等待秒数为 (2*i*i)，即0,2,8...<br><br>
	 * selector对应的元素text不能为空（null，空值等）
	 * 
	 * @param session
	 * @param selector
	 *            css or xpath selector
	 * @param reTryCnt
	 *            重试次数
	 * @return
	 */
	public static boolean waitDynamicPageReady(Session session, String selector, int reTryCnt) {
		for (int i = 0; i < reTryCnt; i++) {
			try {
				// 等待
				int waitInt = 2 * i * i * 1000;
				if (waitInt > 0) {
					session.wait(waitInt);
				}

				// 检测页面是否加载
				String content = session.getText(selector);
				// 指定元素text不能为空
				if (!StringOperate.isRealNull(content)) {
					return true;
				}
			} catch (Exception e2) {
				logger.warn("get elemetnt error! url=" + session.getLocation() + "---selector=" + selector);
			}
		}
		return false;
	}

	/**
	 * 实现autoClose接口，在try()方法中调用后自动释放资源<br>
	 * 也可用来手动调用释放资源
	 */
	@Override
	public void close() throws Exception {
		try {
			if (sessionFactory != null) {
				sessionFactory.close();
			}
		} catch (Exception e) {
		}
		try {
			if (launcher != null) {
				launcher.kill();
			}
		} catch (Exception e) {
		}
	}
}
