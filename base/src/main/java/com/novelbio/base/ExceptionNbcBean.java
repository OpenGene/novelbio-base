package com.novelbio.base;

@SuppressWarnings("serial")
public class ExceptionNbcBean extends RuntimeException {
	public ExceptionNbcBean(String msg) {
		super(msg);
	}
	
	public ExceptionNbcBean(String msg, Throwable e) {
		super(msg, e);
	}
	
	public ExceptionNbcBean(Exception e) {
		super(e);
	}
}
