package com.novelbio.base.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;

/** 本地版的cmd进程 */
public class ProcessCmd implements IntProcess {
	Process process;
	
	/** 第一个执行 */
	public void exec(String[] cmd) throws Exception {
		Runtime runtime = Runtime.getRuntime();
		process = runtime.exec(cmd);
	}
	
	public int waitFor() throws InterruptedException {
		return process.waitFor();
	}
	
	public InputStream getStdErr() {
		return process.getErrorStream();
	}
	
	public InputStream getStdOut() {
		return process.getInputStream();
	}
	
	public OutputStream getStdIn() {
		return process.getOutputStream();
	}
	
	public boolean isCmdStarted() {
		return process != null;
	}

	/** 关闭本线程 */
	public void stopProcess() throws Exception {
		int pid = getUnixPID();
		if (pid > 0) {
			Runtime.getRuntime().exec("kill -9 " + pid).waitFor();
		//	process.destroy();// 无法杀死线程
		//	process = null;
		}
	}
	
	private int getUnixPID() throws Exception {
		// System.out.println(process.getClass().getName());
		if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
			Class cl = process.getClass();
			Field field = cl.getDeclaredField("pid");
			field.setAccessible(true);
			Object pidObject = field.get(process);
			return (Integer) pidObject;
		} else {
			throw new IllegalArgumentException("Needs to be a UNIXProcess");
		}
	}
	
}
