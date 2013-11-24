package com.novelbio.base.cmd;

public class ExceptionCmd extends RuntimeException{
	private static final long serialVersionUID = 6016303614042631477L;

	public ExceptionCmd(String info) {
		super(info);
	}
}
