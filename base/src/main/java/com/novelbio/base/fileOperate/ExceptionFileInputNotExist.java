package com.novelbio.base.fileOperate;

public class ExceptionFileInputNotExist extends RuntimeException {
	public ExceptionFileInputNotExist(String msg) {
		super(msg);
	}
	
	public ExceptionFileInputNotExist(String msg, Throwable e) {
		super(msg, e);
	}
	
	public ExceptionFileInputNotExist(Throwable e) {
		super(e);
	}
	
	public static void validateFile(String inputFile, String msg) {
		if (!FileOperate.isFileExistAndBigThanSize(inputFile, 0)) {
			throw new ExceptionFileInputNotExist(msg);
        }
	}
}
