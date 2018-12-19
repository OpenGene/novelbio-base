package com.novelbio.base.plot.layout;

import java.util.List;

import com.novelbio.base.dataStructure.MathComput;

/**
 * 根据半径或距离返回重新设定半径
 * 系统自己设定的半径可能会差距太大，譬如最短 10，最长100000
 * 这样在图片上很难展示好看，因此需要重型设定一下
 * 
 * 这里是做线性调整的策略，以后也可以考虑对数调整
 * @author zongjie
 *
 */
public class Resize {
	
	/** 在图上的中位数 */
	int mediaPic = 40;
	/** 在图上的最大值 */
	int maxPic = 180;
	/** 在图上的最小值 */
	int minPic = 5;
	
	/** 半径中位数 */
	double mediaIn;
	/** 半径最小值 */
	double minIn;
	/** 半径最大值 */
	double maxIn;
	
	int mediaOut;
	int minOut;
	int maxOut;
	
	/**
	 * 默认大小为
	 * mediaPic = 40;
	 * maxPic = 180;
	 * minPic = 5;
	 */
	public Resize() {}
	
	public Resize(int nodeNum, int width, int height) {
		double length = Math.sqrt(width*height);
		mediaPic = (int) (length/nodeNum*1.2);
		maxPic = mediaPic*5;
		minPic = Math.max(5,(int) (mediaPic/8.0));
	}
//	/**
//	 * 在图片上面的像素最大最小值
//	 * 譬如在图片上，node最长180,最短5,中位数40
//	 * @param media
//	 * @param max
//	 * @param min
//	 */
//	public Resize(int media, int max, int min) {
//		this.mediaPic = media;
//		this.maxPic = max;
//		this.minPic = min;
//	}
	
	public void setLsSize(List<Integer> lsRsize) {
		mediaIn = MathComput.median(lsRsize);
		minIn = Double.MAX_VALUE;
		maxIn = 0;
		for (Integer rsize : lsRsize) {
			minIn = Math.min(minIn, rsize);
			maxIn = Math.max(maxIn, rsize);
		}
		minOut = minPic;
		maxOut = maxPic;
		mediaOut = mediaPic;
		double foldMax = maxIn/mediaIn;
		if (foldMax <= (double)maxPic/mediaPic) {
			maxOut = (int) (foldMax*mediaOut);
		}
		
		double foldMin = minIn/mediaIn;
		if (foldMin >= (double)(minPic/mediaPic)) {
			minOut = (int) (foldMin * mediaOut);
		}
	}
	
	/** 给定r的长度，获取经过矫正的R 的长度 */
	public int getReSize(double rSize) {
		int rLength = 0;
		if (rSize == mediaIn) {
			rLength = mediaOut;
		} else if (rSize > mediaIn) {
			double folder = (double)(rSize-mediaIn)/(maxIn-mediaIn);
			rLength = (int) (folder * (maxOut-mediaOut) + mediaOut);
		} else if (rSize < mediaIn) {
			double folder = (double)(rSize-minIn)/(mediaIn-minIn);
			rLength = (int)(folder * (mediaOut - minOut) + minOut);
		}
		return rLength;
	}
}
