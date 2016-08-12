package com.novelbio.base;

import org.junit.Assert;
import org.junit.Test;
import org.mortbay.servlet.RestFilter;

public class TestStringOperate {
	@Test
	public void testReplaceSpecialCode() {
		String str = "*adCVs*34_a _09_b5*[/435^*&城池()^$$&*).{}+.|.)%%*(*.中国}34{45[]12.fd'*&999下面.是中..文的-字符￥……{}【】。，；’“‘”？";
		String result = StringOperate.replaceSpecialCode(str);
		Assert.assertEquals("adCVs34_a _09_b5435城池....中国344512.fd999下面.是中文的-字符", result);
	}
	
	@Test
	public void testEscapeHtml() {
		String content = "<div>test</div>";
		String resContent = StringOperate.escapeHtml(content);
		Assert.assertEquals("&lt;div&gt;test&lt;/div&gt;", resContent);
	}
}
