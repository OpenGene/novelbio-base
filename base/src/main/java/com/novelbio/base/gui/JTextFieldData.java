package com.novelbio.base.gui;

import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
/**
 * JTextField 的扩展，方便设定输出字符等等
 * @author zong0jie
 *
 */
public class JTextFieldData extends JTextField {
	/**
	 * 
	 */
	private static final long serialVersionUID = -721264059920193811L;
	public void setNumOnly() {
		setDocument(new NumberOnlyDoc());
	}
	 /**
	  * @param maxLen
	  *            int 最大长度(含小数位)
	  * @param decLen
	  *            int 小数位长度
	  */
	public void setNumOnly(int maxLen, int decLen) {
		setDocument(new NumberOnlyDoc(maxLen, decLen));
	}
	 /**
	  * @param decLen
	  *            int 小数位长度
	  */
	public void setNumOnly(int decLen) {
		setDocument(new NumberOnlyDoc(decLen));
	}
	 /**
	  * @param maxLen
	  *            int 最大长度(含小数位) 默认20
	  * @param decLen
	  *            int 小数位长度 默认为0
	  * @param minRange
	  *            double 最小值 默认无限小
	  * @param maxRange 
	  *            double 最大值 默认无限大
	  */
	public void setNumOnly(int maxLen, int decLen, double minRange, double maxRange) {
		setDocument(new NumberOnlyDoc(maxLen, decLen, minRange, maxRange));
	}
	
	
}
/**
 * jtxtFeild只能输入小数点，负号和数字
 * 方法
 * 首先jtxtFeild.setDocument(new DoubleOnlyDoc());<br>
 * @author zong0jie
 *
 */
class NumberOnlyDoc extends PlainDocument {
	private static final long serialVersionUID = 84564523057L;
	/**
	 * 最大长度 
	 */
	int maxLength = 20;// 默认的是20
	 /**
	  * 小数位数
	  */
	 int decLength = 0;

	 double minRange = -Double.MAX_VALUE;

	 double maxRange = Double.MAX_VALUE;
	 /**
	  * @param maxLen
	  *            int 最大长度(含小数位) 默认20
	  * @param decLen
	  *            int 小数位长度 默认为0
	  */
	 public NumberOnlyDoc(int maxLen, int decLen) {
	  maxLength = maxLen;
	  decLength = decLen;
	 }

	 /**
	  * @param maxLen
	  *            int 最大长度(含小数位) 默认20
	  * @param decLen
	  *            int 小数位长度 默认为0
	  * @param minRange
	  *            double 最小值 默认无限小
	  * @param maxRange 
	  *            double 最大值 默认无限大
	  */
	 public NumberOnlyDoc(int maxLen, int decLen, double minRange, double maxRange) {
	  this(maxLen, decLen);
	  this.minRange = minRange;
	  this.maxRange = maxRange;
	 }
	 /**
	  * 小数位数，默认为0
	  * @param decLen
	  */
	 public NumberOnlyDoc(int decLen) {
	  decLength = decLen;
	 }

	public NumberOnlyDoc() {
	}

	public void insertString(int offset, String s, AttributeSet a)
			throws BadLocationException {
		String str = getText(0, getLength());
		if (str.startsWith("-") && s.equals("-")) {
			return;
		}
		if (
		// 不能为f,F,d,D
		s.equals("F")
				|| s.equals("f")
				|| s.equals("D")
				|| s.equals("d")
				// 第一位是0时,第二位只能为小数点
				|| (str.trim().equals("0") && !s.substring(0, 1).equals(".") && offset != 0)
				// 整数模式不能输入小数点
				|| (s.equals(".") && decLength == 0)) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		String strIntPart = "";
		String strDecPart = "";
		String strNew = str.substring(0, offset) + s + str.substring(offset, getLength());
		//-号必须在第一位出现
		if (!strNew.startsWith("-") && strNew.contains("-")) {
			return;
		}
		strNew = strNew.replaceFirst("-", ""); // 控制能输入负数
		int decPos = strNew.indexOf(".");
		if (decPos > -1) {
			strIntPart = strNew.substring(0, decPos);
			strDecPart = strNew.substring(decPos + 1);
		} else {
			strIntPart = strNew;
		}
		if (strIntPart.length() > (maxLength - decLength)
				|| strDecPart.length() > decLength
				|| (strNew.length() > 1 && strNew.substring(0, 1).equals("0") && !strNew.substring(1, 2).equals("."))) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		try {
			if (!strNew.equals("") && !strNew.equals("-")) {// 控制能输入负数
				double d = Double.parseDouble(strNew);
				if (d < minRange || d > maxRange) {
					throw new Exception();
				}
			}
		} catch (Exception e) {
			Toolkit.getDefaultToolkit().beep();
			return;
		}
		super.insertString(offset, s, a);
	}
	
	 /**
	  * @param decLen
	  *            int 小数位长度
	  * @param maxLen
	  *            int 最大长度(含小数位)
	  * @param minRange
	  *            double 最小值
	  * @param maxRange
	  *            double 最大值
	  */

}

/**
 * jtxt只能输入数字
 * 方法
 *  jtxtFeild.setDocument(new NumOnlyDoc());<br>
 * @author zong0jie
 *
 */	
class NumOnlyDoc extends PlainDocument{
	/**
	 * 
	 */
	private static final long serialVersionUID = 14563457L;

	public void insertString(int offset, String s, AttributeSet attrSet)
			throws BadLocationException {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException ex) {
			return;
		}
		super.insertString(offset, s, attrSet);
	}
}
