package com.novelbio.base;

public class ExceptionNbcBean extends RuntimeException {
	public ExceptionNbcBean(String msg) {
		super(msg);
	}
	
	public ExceptionNbcBean(String msg, Throwable e) {
		super(msg, e);
	}
}
