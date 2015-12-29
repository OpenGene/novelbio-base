package com.novelbio.base.fileOperate;

public class ExceptionNbcHdfsFileConnection extends RuntimeException {
    private static final long serialVersionUID = -5867840752501025711L;

	public ExceptionNbcHdfsFileConnection(String msg) {
		super(msg);
	}
	
	public ExceptionNbcHdfsFileConnection(String msg, Throwable e) {
		super(msg, e);
	}
	
	public ExceptionNbcHdfsFileConnection(Throwable e) {
		super(e);
	}
}
