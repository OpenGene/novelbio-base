package com.novelbio.base.dataOperate;

public class ExceptionNbcExcel extends RuntimeException {
	public ExceptionNbcExcel(String msg) {
		super(msg);
	}
	
	public ExceptionNbcExcel(String msg, Throwable e) {
		super(msg, e);
	}
}
