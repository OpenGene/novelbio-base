package com.novelbio.base.fileOperate;

public class ExceptionNbcFileInputNotExist extends ExceptionNbcFile {
	public ExceptionNbcFileInputNotExist(String msg) {
		super(msg);
	}
	
	public ExceptionNbcFileInputNotExist(String msg, Throwable e) {
		super(msg, e);
	}
	
	public ExceptionNbcFileInputNotExist(Throwable e) {
		super(e);
	}
	
	public static void validateFile(String inputFile, String msg) {
		if (!FileOperate.isFileExistAndBigThanSize(inputFile, 0)) {
			throw new ExceptionNbcFileInputNotExist(msg);
        }
	}
}
