package com.novelbio.base.fileOperate;

public class ExceptionFile extends RuntimeException {
	public ExceptionFile(String msg) {
		super(msg);
	}
	
	public ExceptionFile(String msg, Throwable e) {
		super(msg, e);
	}
	
	public ExceptionFile(Throwable e) {
		super(e);
	}
}
