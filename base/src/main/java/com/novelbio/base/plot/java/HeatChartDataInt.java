package com.novelbio.base.plot.java;
/**
 * 专门用于画图的接口
 * @author zong0jie
 *
 */
public interface HeatChartDataInt {
	/**
	 * 返回double信息
	 * @return
	 */
	public double[] getDouble();
	/**
	 * 返回文件的title
	 */
	public String getName();
}
