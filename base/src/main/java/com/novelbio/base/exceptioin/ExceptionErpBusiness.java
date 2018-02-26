package com.novelbio.base.exceptioin;

/**
 * 业务异常
 * 
 * @author renyx
 *
 */
public class ExceptionErpBusiness extends ExceptionErp {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExceptionErpBusiness() {
		super();
	}

	public ExceptionErpBusiness(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ExceptionErpBusiness(String message, Throwable cause) {
		super(message, cause);
	}

	public ExceptionErpBusiness(String message) {
		super(message);
	}

	public ExceptionErpBusiness(Throwable cause) {
		super(cause);
	}

	public ExceptionErpBusiness(String errorCode, String message) {
		super(message);
		this.setErrorCode(errorCode);
	}

}
