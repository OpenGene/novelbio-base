package com.novelbio.base.plot.layout;

import java.util.Random;

public class Node {
	/** 排斥因子 */
	static double ejectfactor = 6;
	
	String id;
	double x;
	double y;
	double r;
	
	public Node(String id) {
		this.id = id;
	}
	
	public Node(String id, double x, double y) {
		this.id = id;
		this.x = x;
		this.y = y;
	}
	public void setR(double r) {
		this.r = r;
	}
	
	public String getId() {
		return id;
	}
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
	public void setX(double x) {
		this.x = x;
	}
	public void setY(double y) {
		this.y = y;
	}
	public double getR() {
		return r;
	}
	
	/** 
	 * 计算node1在node2的作用下的位移情况<br>
	 * 返回 node1 所需要的位移<br>
	 *  double[2] 0: x的位移，1: y的位移<br>
	 * 有正负号的区别
	 * @param node1
	 * @param node2
	 * @param k
	 * @return
	 */
	public static double[] getAxisRejectMove(Node node1, Node node2, double k) {
		double deltaX = node1.getX() - node2.getX();
		double deltaY = node1.getY() - node2.getY();
		double deltaLength = getDistance(node1, node2);
		if (deltaLength == 0) {
			Random random = new Random();
			deltaX = (double)random.nextInt(10)/10;
			deltaY = (double)random.nextInt(10)/10;
			deltaLength = Math.sqrt(deltaX*deltaX + deltaY*deltaY);
		}
		double deltaLengthReal = getDistanceReal(node1, node2);
		//放置出现NaN错误
		if (deltaLengthReal <= 0) {
			deltaLengthReal = 0.28;
		}
		double eject = ejectfactor;
		if (deltaLengthReal < 30) {
			eject = eject * 0.833;
		}
		
		double force = k * k / deltaLengthReal * eject;
		double[] xyMove = new double[] {0,0};
		if (deltaLengthReal < 250) {
			xyMove[0] =  deltaX / deltaLength * force;
			xyMove[1] = deltaY / deltaLength * force;
		}
		return xyMove;
	}
	
	/** 计算两个点之间的距离，有可能会负数，就是两个球overlap的时候 */
	public static double getDistanceReal(Node node1, Node node2) {
		return getDistance(node1, node2) - node1.getR() - node2.getR();
	}
	/** 计算两个点之间的距离，有可能会负数，就是两个球overlap的时候 */
	public static double getDistance(Node node1, Node node2) {
		double diffx = node1.getX() - node2.getX();
		double diffy = node1.getY() - node2.getY();
		return Math.sqrt(diffx * diffx + diffy * diffy);
	}
}
