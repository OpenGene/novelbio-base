package com.novelbio.base.dataOperate;

import java.io.IOException;

public class ExceptionNBCServerConnect extends IOException {
	
	public ExceptionNBCServerConnect(String msg) {
		super(msg);
	}
	
	public ExceptionNBCServerConnect(String msg, Throwable e) {
		super(msg, e);
	}
}
