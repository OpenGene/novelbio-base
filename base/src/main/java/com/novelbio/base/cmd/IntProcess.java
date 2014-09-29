package com.novelbio.base.cmd;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

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

	/** 获得本进程以及其子进程的pid和运行情况，目前只有本地版的cmd才能使用 */
	List<ProcessInfo> getLsProcInfo() throws Exception;

}
