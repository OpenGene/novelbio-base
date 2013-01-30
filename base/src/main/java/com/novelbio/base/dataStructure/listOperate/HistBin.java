package com.novelbio.base.dataStructure.listOperate;

import com.novelbio.analysis.seq.genome.gffOperate.ListDetailBin;

/**
 * 频率直方图的每个bin
 * @author zongjie
 */
public class HistBin extends ListDetailAbs {
	/** 计数器 */
	long countNumber = 0;
	double binNum = -Double.MAX_VALUE;
	
	
	protected HistBin() {
		super("", "", true);
	}
	protected HistBin(Double binNum) {
		super("", "", true);
		if (binNum != null) {
			this.binNum = binNum;
		}
	}
	/**
	 * 设定分数，根据需要保存double值
	 * @param score
	 */
	public void setNumber(long number) {
		this.countNumber = number;
	}
	public void addNumber() {
		this.countNumber++;
	}
	public void addNumber(int addNum) {
		this.countNumber = this.countNumber + addNum;
	}
	/**
	 * 如果设定了binNum则返回binNum，没设定的话bin的头减去尾，所以注意是否要四舍五入
	 * @return
	 */
	public double getThisNumber() {
		if (binNum > -Double.MAX_VALUE) {
			return binNum;
		}
		return (getEndAbs() + getStartAbs())/2;
	}
	
	/**
	 * 获得分数，根据需要保存的计数值
	 * @return
	 */
	public long getCountNumber() {
		return countNumber;
	}
	public HistBin clone() {
		HistBin result = (HistBin) super.clone();
		result.countNumber = countNumber;
		return result;
	}

}
