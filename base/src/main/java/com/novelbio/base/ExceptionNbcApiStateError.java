package com.novelbio.base;

/**
 * {@link ResultJson}格式返回值state不为true
 * 
 * @author novelbio liqi
 * @date 2018年3月26日 下午6:21:44
 */
public class ExceptionNbcApiStateError extends RuntimeException {

	/** **/
	private static final long serialVersionUID = 1L;

	public ExceptionNbcApiStateError(String msg) {
		super(msg);
	}
}
