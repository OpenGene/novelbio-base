package com.novelbio.base.cmd;

import java.io.InputStream;
import java.io.OutputStream;

public interface IntProcess {
	/** 第一个执行 */
	public void exec(String[] cmd) throws Exception;
	
	public int waitFor() throws InterruptedException;
	
	public InputStream getStdErr();
	
	public InputStream getStdOut();
	
	public OutputStream getStdIn();
	
	public boolean isCmdStarted();
	
	/** 关闭本线程 */
	public void stopProcess() throws Exception;

}
