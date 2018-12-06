package com.novelbio.base;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author novelbio liqi
 * @date 2018年5月23日 下午5:36:49
 */
public class TestMyBeanUtilModel extends AbstractModel {

	private String a;
	private Date b;
	private List<String> lsStr;
	private Set<Date> setDate;
	private Map<String, Object> mapKey2Obj;
	private TestMyBeanUtilModel innerModel;
	private String virtualA;

	public String getVirtualName() {
		return "virtual name";
	}

	public String getA() {
		return a;
	}

	public void setA(String a) {
		this.a = a;
	}

	public Date getB() {
		return b;
	}

	public void setB(Date b) {
		this.b = b;
	}

	public List<String> getLsStr() {
		return lsStr;
	}

	public void setLsStr(List<String> lsStr) {
		this.lsStr = lsStr;
	}

	public Set<Date> getSetDate() {
		return setDate;
	}

	public void setSetDate(Set<Date> setDate) {
		this.setDate = setDate;
	}

	public Map<String, Object> getMapKey2Obj() {
		return mapKey2Obj;
	}

	public void setMapKey2Obj(Map<String, Object> mapKey2Obj) {
		this.mapKey2Obj = mapKey2Obj;
	}

	public TestMyBeanUtilModel getInnerModel() {
		return innerModel;
	}

	public void setInnerModel(TestMyBeanUtilModel innerModel) {
		this.innerModel = innerModel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.novelbio.base.AbstractModel#getVirtualA()
	 */
	@Override
	public String getVirtualA() {
		return virtualA;
	}

	@Override
	public void setVirtualA(String virtualA) {
		this.virtualA = virtualA;
	}

}
