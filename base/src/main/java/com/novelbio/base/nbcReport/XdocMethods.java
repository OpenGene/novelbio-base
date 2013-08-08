package com.novelbio.base.nbcReport;

import java.util.LinkedHashSet;
import java.util.Set;

public class XdocMethods {
	private Set<String> setMethod = new LinkedHashSet<String>();
	
	public Set<String> addMethod(String method){
		setMethod.add(method);
		return this.setMethod;
	}

	public Set<String> getSetMethod() {
		return this.setMethod;
	}
	
}
