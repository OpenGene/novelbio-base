package com.novelbio.base.multithread;
/**
 * <b>需要配合 RunGetInfo</b><br>
 * T: 本次running打算输出的中间信息
 * 进度条多线程，需要以下操作 <br>
 * 1. 在循环中添加 suspendCheck()  来挂起线程<br>
 * 2. 在循环中检查 flagRun 来终止循环<br>
 * 3: 在循环中添加 setRunInfo() 方法来获取运行时出现的信息
 * @author zong0jie
 *
 */
public abstract class RunProcess<T> implements Runnable {
	protected RunGetInfo runGetInfo;
	
	protected boolean flagStop = true;
	protected boolean suspendFlag = false;
	/** 是否结束 */
	boolean flagFinish = false;
	
	/** 给定运行中需要修改的信息 */
	public void setRunGetInfo(RunGetInfo runGetInfo) {
		this.runGetInfo = runGetInfo;
	}
	/** 程序暂停 */
	public void threadSuspend() {
		this.suspendFlag = true;
	}
	/** 进程恢复 */
	public void threadResume() {
		if (suspendFlag == false) {
			return;
		}
		this.suspendFlag = false;
		if (runGetInfo != null) {
			runGetInfo.threadResumed(this);
		}
		notify();
	}
	/** 终止线程，需要在循环中添加<br>
	 * if (!flagRun)<br>
	*			break; */
	public void threadStop() {
		threadResume();
		flagStop = true;		
		if (runGetInfo != null) {
			runGetInfo.threadStop(this);
		}
	}
	/**
	 * 放在循环中，检查是否终止线程
	 */
	protected void suspendCheck() {
		byte[] lock = new byte[0];
		synchronized (lock) {
			while (suspendFlag){
				if (runGetInfo != null) {
					runGetInfo.threadSuspended(this);
				}
				try {wait();} catch (InterruptedException e) {}
			}
		}
	}
	@Override
	public void run() {
		flagStop = false;
		try {
			running();
		} catch (Exception e) {
			//TODO 看是否能在runGetInfo中添加一个运行失败的处理方法
			flagFinish = true;
			flagStop = true;
		}
		
		flagFinish = true;
		flagStop = true;
		if (runGetInfo != null) {
			runGetInfo.done(this);
		}
	}
	/** 给run方法调用。运行模块写在这个里面，这样结束后自动会将flagFinish设定为true */
	protected abstract void running();
	/**
	 * 设定输入的信息，内部回调
	 * @param runInfo
	 */
	protected void setRunInfo(T runInfo) {
		if (runGetInfo != null) {
			runGetInfo.setRunningInfo(runInfo);
		}
	}
	public boolean isRunning() {
		return !flagStop;
	}
	
	public boolean isFinished() {
		return flagFinish;
	}
}
