package com.novelbio.base.dataOperate;

import java.io.IOException;

public class ExceptionNBCReadLineTooLong extends RuntimeException {
	public ExceptionNBCReadLineTooLong(String msg) {
		super(msg);
	}
	
	public ExceptionNBCReadLineTooLong(String msg, Throwable e) {
		super(msg, e);
	}
}
