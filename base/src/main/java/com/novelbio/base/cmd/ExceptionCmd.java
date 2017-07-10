package com.novelbio.base.cmd;

public class ExceptionCmd extends RuntimeException{
	private static final long serialVersionUID = 6016303614042631477L;
	
	public ExceptionCmd(Throwable t) {
		super(t);
	}
	public ExceptionCmd(String info) {
		super(info);
	}
	public ExceptionCmd(String info, Throwable t) {
		super(info, t);
	}

	public ExceptionCmd(String info, CmdOperate cmdOperate) {
		super(info + "\n" + cmdOperate.getCmdExeStrReal() + "\n" + cmdOperate.getErrOut());
	}
	public ExceptionCmd(String info, CmdOperate cmdOperate, Throwable t) {
		super(info + "\n" + cmdOperate.getCmdExeStrReal() + "\n" + cmdOperate.getErrOut(), t);
	}
	public ExceptionCmd(CmdOperate cmdOperate) {
		super(cmdOperate.getCmdExeStrReal() + "\n" + cmdOperate.getErrOut());
	}
	public ExceptionCmd(CmdOperate cmdOperate, Throwable t) {
		super(cmdOperate.getCmdExeStrReal() + "\n" + cmdOperate.getErrOut(), t);
	}
	public ExceptionCmd(String info, Exception e) {
		super(info, e);
	}
}
