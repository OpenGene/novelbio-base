package com.novelbio.base.fileOperate;

public class ExceptionNbcFileInputError extends ExceptionNbcFile {
	public ExceptionNbcFileInputError(String msg) {
		super(msg);
	}
	
	public ExceptionNbcFileInputError(String msg, Throwable e) {
		super(msg, e);
	}
	
	public ExceptionNbcFileInputError(Throwable e) {
		super(e);
	}
}
