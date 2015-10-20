package com.novelbio.base.fileOperate;

public class ExceptionHdfsFileConnection extends RuntimeException {
	public ExceptionHdfsFileConnection(String msg) {
		super(msg);
	}
	
	public ExceptionHdfsFileConnection(String msg, Throwable e) {
		super(msg, e);
	}
	
	public ExceptionHdfsFileConnection(Throwable e) {
		super(e);
	}
}
