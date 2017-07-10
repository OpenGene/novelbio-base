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
public abstract class RunProcess extends Thread {
	protected RunGetInfo runGetInfo;
	byte[] lock = new byte[0];

	/** 是否要停止本线程 */
	protected volatile boolean flagStop = false;
	protected volatile RunThreadStat runThreadStat = RunThreadStat.notStart;
	protected boolean suspendFlag = false;
	protected Throwable exception;
	
	/** 给定运行中需要修改的信息 */
	public void setRunGetInfo(RunGetInfo runGetInfo) {
		this.runGetInfo = runGetInfo;
	}
	/** 程序暂停，需要在实现类中根据  suspendFlag 去暂停*/
	public void threadSuspend() {
		this.suspendFlag = true;
		runThreadStat = RunThreadStat.threadSuspend;
	}
	/** 进程恢复，需要在实现类中根据  suspendFlag 去恢复 */
	public void threadResume() {
		threadResume(false);
	}
	/** 进程恢复 */
	private void threadResume(boolean isThreadStop) {
		if (suspendFlag == false) {
			return;
		}
		this.suspendFlag = false;
		if (!isThreadStop) {
			runThreadStat = RunThreadStat.running;
		}
		if (runGetInfo != null) {
			runGetInfo.threadResumed(this);
		}
		lock.notify();
	}
	/** 终止线程，需要在实现类中根据  suspendFlag 去在循环中添加<br>
	 * if (flagStop)<br>
	*			break; */
	public synchronized void threadStop() {
		flagStop = true;
		runThreadStat = RunThreadStat.finishInterrupt;
		if (runGetInfo != null) {
			runGetInfo.threadStop(this);
		}
		threadResume(true);
	}
	/**
	 * 放在实现类的循环中，检查是否终止线程
	 */
	protected void suspendCheck() {
		synchronized (lock) {
			while (suspendFlag && !flagStop){
				if (runGetInfo != null) {
					runGetInfo.threadSuspended(this);
				}
				try {lock.wait();} catch (InterruptedException e) {}
			}
		}
	}
	@Override
	public void run() {
		flagStop = false;
		runThreadStat = RunThreadStat.running;
		
		try {
			running();
			runThreadStat = RunThreadStat.finishNormal;
		} catch (Throwable e) {
			e.printStackTrace();
			exception = e;
			runThreadStat = RunThreadStat.finishAbnormal;
		}
		flagStop = true;
		if (runGetInfo != null) {
			runGetInfo.done(this);
		}
	}
	/** 给run方法调用。运行模块写在这个里面，这样结束后自动会将flagFinish设定为true */
	protected abstract void running();
	/**
	 * 设定输入的信息，内部回调<br>
	 * 只有当 {@link #setRunGetInfo(RunGetInfo)} 设置了参数时，才会把该参数传递给 {@link RunGetInfo}<br>
	 * 这里可以设定任意类，但是注意输入的类必须和 {@link RunGetInfo#setRunningInfo(Object)}保持一致，否则会报类型转换错误<br>
	 * @param runInfo
	 */
	protected<T> void setRunInfo(T runInfo) {
		if (runGetInfo != null) {
			runGetInfo.setRunningInfo(runInfo);
		}
	}
	/**
	 * 是否在运行
	 * @return
	 */
	public boolean isRunning() {
		return !flagStop;
	}
	/**
	 * 是否正常结束，如果isRunning为false，而isFinished也为false，则表示运行出错。
	 * @return
	 */
	public boolean isFinishedNormal() {
		return runThreadStat == RunThreadStat.finishNormal;
	}
	
	/** 返回线程运行时的状态 */
	public RunThreadStat getRunThreadStat() {
		return runThreadStat;
	}
	/** 返回异常 */
	public Throwable getException() {
		return exception;
	}
	public static enum RunThreadStat {
		notStart, running, finishNormal, threadSuspend, finishInterrupt, finishAbnormal
	}
}
