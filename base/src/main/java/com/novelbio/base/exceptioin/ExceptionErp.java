package com.novelbio.base.exceptioin;

import com.novelbio.base.StringOperate;

/**
 * Erp系统异常超类
 * 
 * @author renyx
 *
 */
public class ExceptionErp extends RuntimeException {

	private static final long serialVersionUID = 1L;
	

	private String errorCode;

	public ExceptionErp() {
		super();
	}

	public ExceptionErp(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ExceptionErp(String message, Throwable cause) {
		super(message, cause);
	}

	public ExceptionErp(String message) {
		super(message);
	}

	public ExceptionErp(Throwable cause) {
		super(cause);
	}
	
	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

}
