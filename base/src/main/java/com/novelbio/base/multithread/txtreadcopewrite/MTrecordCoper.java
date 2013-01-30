package com.novelbio.base.multithread.txtreadcopewrite;

import java.util.AbstractQueue;

import com.novelbio.base.multithread.RunProcess;

public abstract class MTrecordCoper<T extends MTRecordCope> extends RunProcess<MTRecordCope>{
	
	protected MTRecoreReader<?, ? extends MTRecordRead> mtOneThreadReader;
	/** 读取得到的内容就保存在这里面 */
	protected AbstractQueue<? extends MTRecordRead> absQueue;
	
	/** 主要是看读取是否完毕 */
	public void setReader(MTRecoreReader<?, ? extends MTRecordRead> mtOneThreadReader) {
		this.mtOneThreadReader = mtOneThreadReader;
	}
	/** 设定待读取的list，几个线程共用一个list */
	public void setLsRecords(AbstractQueue<? extends MTRecordRead> absQueue) {
		this.absQueue = absQueue;
	}
	@Override
	protected void running() {
		copeBeforeRun();
		try {
			copeLsReads();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	protected boolean isReadingFinished() throws InterruptedException {
		while (absQueue.isEmpty()) {
			if (mtOneThreadReader.isFinished()) {
				return true;
			}
			Thread.sleep(20);
		}
		return false;
	}
	/** 需要在running之前处理的信息，譬如计数、参数设定等，不需要的话留空 */
	protected abstract void copeBeforeRun();
	
	/** 处理序列 */
	protected abstract void copeLsReads() throws InterruptedException;
}
