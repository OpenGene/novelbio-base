package com.novelbio.base;

import java.math.BigDecimal;

/**
 * 
 * @author novelbio liqi
 * @date 2018年11月30日 下午2:24:06
 */
public class TestMyBeanUtilModel2 extends AbstractModel {
	private String a;
	private BigDecimal b;
	private String virtualA;

	public BigDecimal getB() {
		return b;
	}

	public void setB(BigDecimal b) {
		this.b = b;
	}

	public String getA() {
		return a;
	}

	public void setA(String a) {
		this.a = a;
	}

	/* (non-Javadoc)
	 * @see com.novelbio.base.AbstractModel#getVirtualA()
	 */
	@Override
	public String getVirtualA() {
		return virtualA;
	}

	/* (non-Javadoc)
	 * @see com.novelbio.base.AbstractModel#setVirtualA(java.lang.String)
	 */
	@Override
	public void setVirtualA(String virtualA) {
		this.virtualA = virtualA;
	}
}
