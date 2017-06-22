package com.novelbio.base;

public class ExceptionNbcParamError extends RuntimeException {
	public ExceptionNbcParamError(String msg) {
		super(msg);
	}
	public ExceptionNbcParamError(String msg, Throwable t) {
		super(msg, t);
	}
}
