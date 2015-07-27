package com.novelbio.base.fileOperate;

public class ExceptionFileInputError extends RuntimeException {
	public ExceptionFileInputError(String msg) {
		super(msg);
	}
	
	public ExceptionFileInputError(String msg, Throwable e) {
		super(msg, e);
	}
	
	public ExceptionFileInputError(Throwable e) {
		super(e);
	}
}
