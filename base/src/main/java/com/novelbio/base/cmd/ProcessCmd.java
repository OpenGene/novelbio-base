package com.novelbio.base.cmd;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novelbio.base.StringOperate;

/** 本地版的cmd进程 */
public class ProcessCmd implements IntProcess {
	private static final Logger logger = Logger.getLogger(ProcessCmd.class);
	File runPath;
	Process process;
	
	public ProcessCmd() {	}
	
	public ProcessCmd(String runPath) {
		if (!StringOperate.isRealNull(runPath)) {
			this.runPath = new File(runPath);
		}
	}
	
	public void setRunPath(File runPath) {
		this.runPath = runPath;
	}
	
	/** 第一个执行 */
	public void exec(String[] cmd) throws Exception {
		Runtime runtime = Runtime.getRuntime();
		process = runtime.exec(cmd, null, runPath);
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
			List<ProcessInfo> lsProc = ProcessInfo.getLsPid(pid, true);
			//倒着关闭，因为后面是子进程，前面是父进程，先关闭子进程
			for (int i = lsProc.size() - 1; i >= 0; i--) {
				ProcessInfo processInfo = lsProc.get(i);
				logger.info("kill pid " + processInfo.getPid() + " program " + processInfo.getCmdName());
				Runtime.getRuntime().exec("kill -9 " + processInfo.getPid()).waitFor();
			}
		//	process = null;
		}
		
		//在西安测试可以杀死bwa的cmd命令 20150511
		//	process.destroy();// 无法杀死线程
	}
	
	@Override
	/** 获得本进程以及其子进程的pid和运行情况 */
	public List<ProcessInfo> getLsProcInfo() throws Exception {
		int pid = getUnixPID();
		if (pid > 0) {
			return ProcessInfo.getLsPid(pid, true);
		}
		return new ArrayList<ProcessInfo>();
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
			throw new ExceptionCmd("Needs to be a UNIXProcess");
		}
	}
	
}
