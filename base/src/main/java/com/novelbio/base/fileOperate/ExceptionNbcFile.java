package com.novelbio.base.fileOperate;

public class ExceptionNbcFile extends RuntimeException {
	public ExceptionNbcFile(String msg) {
		super(msg);
	}
	
	public ExceptionNbcFile(String msg, Throwable e) {
		super(msg, e);
	}
	
	public ExceptionNbcFile(Throwable e) {
		super(e);
	}
}
