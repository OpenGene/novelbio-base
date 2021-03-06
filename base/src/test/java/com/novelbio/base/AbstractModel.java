package com.novelbio.base;

/**
 * 
 * @author novelbio liqi
 * @date 2018年12月6日 上午10:33:33
 */
public abstract class AbstractModel {
	private String sName;
	private boolean isFirst;
	private boolean isSecend;
	private Boolean deleted;

	public abstract String getVirtualA();

	public abstract void setVirtualA(String virtualA);

	public String getsName() {
		return sName;
	}

	public void setsName(String sName) {
		this.sName = sName;
	}

	public boolean isFirst() {
		return isFirst;
	}

	public void setIsFirst(boolean isFirst) {
		this.isFirst = isFirst;
	}

	public boolean isSecend() {
		return isSecend;
	}

	public void setSecend(boolean isSecend) {
		this.isSecend = isSecend;
	}

	public Boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
}
