package com.novelbio.base.multithread.txtreadcopewrite;

import java.util.ArrayList;

import com.novelbio.base.multithread.RunGetInfo;
import com.novelbio.base.multithread.RunProcess;

public abstract class MTmulitCopeInfo<T extends MTrecordCoper<K>, K extends MTRecordCope> implements RunGetInfo<K> {
	int threadStopNum = 0;
	Boolean isFinished = false;
	/** 一个线程读取 */
	MTRecoreReader mtOneThreadReader;
	protected ArrayList<T> lsCopeRecorders = new ArrayList<T>();
	
	/** 将read对象保存起来 */
	public void setReader(MTRecoreReader mtOneThreadReadFile) {
		this.mtOneThreadReader = mtOneThreadReadFile;
	}
	/**添加线程，当然也可以在外面包装一个 方法新建线程 */
	public void addMTcopedRecord(T mTcopeRecorder) {
		lsCopeRecorders.add(mTcopeRecorder);
	}
	/**持续等到本工作结束
	 * @param time 毫秒的时间，每隔这段时间检查是否结束
	 * @return false 程序终端，没有完成，true：程序成功结束
	 */
	public boolean isFinished(int time) {
		while (true) {
			if (isFinished == null ) {
				return false;
			}
			else if (isFinished == true) {
				return true;
			}
			try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); }
		}
	}
	@Override
	public void done(RunProcess<K> runProcess) {
		synchronized (this) {
			doneOneThread(runProcess);
			threadStopNum ++;
			if (threadStopNum == lsCopeRecorders.size()) {
				mtOneThreadReader.close();
				doneAllThread();
				isFinished = true;
			}
		}
	}
	public void execute() {
		threadStopNum = 0;
		isFinished = false;
		beforeExecute();
		startThread();
	}
	
	@Override
	public void setRunningInfo(K info) {
		synchronized (this) {
			copeReadInfo(info);
		}
	}
	/** 设定想要做的工作，不需要加锁 
	 * 想写入的文本可以在这里写入
	 * */
	protected abstract void copeReadInfo(K info);
	
	public void suspendThread() {
		mtOneThreadReader.threadSuspend();
		for (T copeRecorder : lsCopeRecorders) {
			copeRecorder.threadSuspend();
		}
	}

	public void resumeThread() {
		mtOneThreadReader.threadResume();
		for (T copeRecorder : lsCopeRecorders) {
			copeRecorder.threadResume();
		}
	}

	public void stopThread() {
		mtOneThreadReader.threadStop();
		for (T copeRecorder : lsCopeRecorders) {
			copeRecorder.threadStop();
		}
		isFinished = null;
	}
	
	@Override
	public void threadSuspended(RunProcess<K> runProcess) {}

	@Override
	public void threadResumed(RunProcess<K> runProcess) {}

	@Override
	public void threadStop(RunProcess<K> runProcess) {}
	
	/** 在启动前做的准备工作 */
	protected abstract void beforeExecute();		
	
	protected void startThread() {
		isFinished = false;
		mtOneThreadReader.setLsCopedThread(lsCopeRecorders);
		Thread thread = new Thread(mtOneThreadReader);
		thread.start();
		for (T copeRecorder : lsCopeRecorders) {
			copeRecorder.setRunGetInfo(this);
			thread = new Thread(copeRecorder);
			thread.start();
		}
	}
	
	/** 某个线程完成时的工作，不需要synchronized */
	protected abstract void doneOneThread(RunProcess<K> runProcess);
	
	/** 全部线程完成时的工作，不需要synchronized, 不需要关闭reader */
	protected abstract void doneAllThread();
	
}
