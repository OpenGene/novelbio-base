package com.novelbio.base.multithread.txtreadcopewrite;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

import com.novelbio.analysis.seq.fastq.FastQRecord;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.multithread.RunProcess;

/** 多线程中一个线程读取文件<br>
 * T: 就是实际读取的内容，譬如String或FastQRecord<br>
 * K: 把T包装好，传入absQueue并发list的对象<br>
 * 
 * 多线程，需要以下操作 <br>
 * 1. 在循环中添加 wait_To_Cope_AbsQueue()  来挂起线程，同时防止absQueue队列过大<br>
 * 2. 在循环中检查 flagRun 来终止循环<br>
 * 3: 在循环中添加 setRunInfo() 方法来获取运行时出现的信息
 * @author zong0jie
 *
 */
public abstract class MTRecoreReader <T, K extends MTRecordRead> extends RunProcess<K> {
	long readsNum = 0;
		
	protected int maxNumReadInLs = 5000;
	protected AbstractQueue<K> absQueue = new ArrayBlockingQueue<K>(maxNumReadInLs);
	
	
	/** 在每个MTRecordCope中都设定本读取类，同时将 公共队列absQueue设置给每一个MTRecordCope */
	public void setLsCopedThread(ArrayList<? extends MTrecordCoper<? extends MTRecordCope>> lsCopedRecords) {
		for (MTrecordCoper<? extends MTRecordCope> mtRecordCoper : lsCopedRecords) {
			addFilterReads(mtRecordCoper);
		}
	}
	/** 在每个filterReads中都设定本读取类,同时将 公共队列absQueue设置给每一个MTRecordCope */
	public void addFilterReads(MTrecordCoper<? extends MTRecordCope> mtRecordCoper) {
		mtRecordCoper.setReader(this);
		mtRecordCoper.setLsRecords(absQueue);
	}
	public long getReadsNum() {
		if (readsNum == 0) {
			for (T t : readlines()) {
				readsNum++;
			}
		}
		return readsNum;
	}
	public Iterable<T> readlines() {
		return readlines(0);
	}
	/**
	 * 读取前几行，不影响{@link #readlines()}
	 * @param num
	 * @return
	 */
	public ArrayList<T> readHeadLines(int num) {
		ArrayList<T> lsResult = new ArrayList<T>();
		int i = 0;
		for (T info : readlines()) {
			if (i >= num) {
				break;
			}
			lsResult.add(info);
		}
		return lsResult;
	}
	/** 等待处理线程将AbsQueue队列中的记录处理掉 */
	protected void wait_To_Cope_AbsQueue() {
		suspendCheck();
		while (absQueue.size() == maxNumReadInLs) {
			try { Thread.sleep(50); } catch (InterruptedException e) { }
		}
	}
	/**
	 * 从第几行开始读，是实际行
	 * @param lines 如果lines小于1，则从头开始读取
	 * @return
	 */
	public abstract Iterable<T> readlines(int lines);
	/** 关闭想要关闭的流，记得用try包围 */
	public abstract void close();
	

}
