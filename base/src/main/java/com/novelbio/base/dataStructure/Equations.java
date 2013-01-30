package com.novelbio.base.dataStructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.log4j.Logger;

import com.novelbio.base.dataOperate.ExcelTxtRead;
import com.novelbio.base.fileOperate.FileOperate;
/**
 * 首先设定一系列的ArrayList<double[]> lsXY
 * 然后按照x排序
 * 然后二分查找x，获得y
 * @author zong0jie
 */
public class Equations {
	private static Logger logger = Logger.getLogger(Equations.class);
	ArrayList<double[]> lsXY = new ArrayList<double[]>();
	double min = Double.MIN_VALUE;
	double max = Double.MAX_VALUE;
	public void setMin(double min){
		this.min = min;
	}
	public void setMax(double max) {
		this.max = max;
	}

	/**
	 * 给定一系列的数据，分为两列，获得一个xy的曲线
	 * @param file 没有文件则直接返回
	 * @param colX x列
	 * @param colY y列
	 * @param rowNum 从第几列开始读去
	 */
	public void setXYFile(String file, int colX, int colY, int rowStart)
	{
		if (!FileOperate.isFileExist(file)) {
			lsXY.clear();
			return;
		}
		ArrayList<String[]> lsInfo = ExcelTxtRead.readLsExcelTxt(file, new int[]{colX, colY}, rowStart, -1);
		for (String[] strings : lsInfo) {
			double x = 0;
			double y = 0;
			try {
				x = Double.parseDouble(strings[0]);
				y = Double.parseDouble(strings[1]);
			} catch (Exception e) {
				continue;
			}
			addXY(x, y);
		}
		//排序
		Collections.sort(lsXY, new CompLsXY());
	}
	/**
	 * 给定一系列的数据，分为两列，获得一个xy的曲线
	 * @param file  没有文件则直接返回  x第一列，y第二；列，从第一行开始读取
	 */
	public void setXYFile(String file)
	{
		setXYFile(file, 1, 2, 1);
	}
	/**
	 * 设定x，y的值
	 * @param x
	 * @param y
	 */
	public void addXY(double x, double y)
	{
		lsXY.add(new double[]{x,y});
	}
	public void setXY(ArrayList<double[]> lsXY)
	{
		this.lsXY = lsXY;
	}
	/**
	 * 返回修正结果
	 * @param x
	 * @return
	 */
	public double[] getYinfo(double[] x)
	{		
		double[] y = new double[x.length];

		if (lsXY.size() < 2) {
			for (int i = 0; i < y.length; i++) {
				y[i] = x[i];
			}
			return y;
		}
		
		for (int i = 0; i < y.length; i++) {
			y[i] = getY(x[i]);
		}
		return y;
	}

	/**
	 * 给定x，获得对应的y
	 * @param X
	 * @return
	 */
	public double getY(double X)
	{
		int num = Collections.binarySearch(lsXY, new double[]{X,0}, new CompLsXY());
		if (num >= 0) {
			return lsXY.get(num)[1];
		}
		else {
			//如果x在最前面
			if (num == -1) {
				return getYinside(lsXY.get(0), lsXY.get(1), X);
			}
			//如果x在最后面
			else if (num == -lsXY.size() - 1) {
				return getYinside(lsXY.get(-num - 3), lsXY.get(-num - 2), X);
			}
			//如果x在中间
			else {
				return getYinside(lsXY.get(-num - 2), lsXY.get( -num - 1), X);
			}
		}
	}
	
	/**
	 * 指定上一个点和下一个点的坐标，给定两点之间的x值，计算该点的Y值
	 * @param upXY
	 * @param downXY
	 * @param X
	 * @return
	 */
	private double getYinside(double[] upXY, double[] downXY, double x)
	{
		double x1 = upXY[0]; double  y1 = upXY[1];
		double x2 = downXY[0]; double y2 = downXY[1];
		if (x1 == x2) {
			logger.error("坐标错误, x1: " + x1 + " y1: " + y1 + " x2: " + x2 + " y2: " + y2 + " x: " + x);
		}
//		if (x > Math.max(x1, x2) || x < Math.min(x1, x2)) {
//			logger.error("坐标错误, x1: " + x1 + " y1: " + y1 + " x2: " + x2 + " y2: " + y2 + " x: " + x);
//		}
		double y = (y2*x1 - y2*x - y1*x2 + y1*x)/(x1 - x2);
		if (y < min) {
			return min;
		}
		if (y > max) {
			return max;
		}
		return y;
	}
}

class CompLsXY implements Comparator<double[]>
{
	@Override
	public int compare(double[] o1, double[] o2) {
		Double x1 = o1[0];
		Double x2 = o2[0];
		return x1.compareTo(x2);
	}
}
