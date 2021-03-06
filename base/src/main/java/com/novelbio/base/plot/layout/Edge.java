package com.novelbio.base.plot.layout;

import java.util.Random;

import com.novelbio.base.SepSign;

public class Edge {
	static String nodeSep = SepSign.SEP_INFO;
	/** 弹力系数 */
	static int condensefactor = 3;
	String id1;
	String id2;
	
	/** 线的长度，0表示没有长度 */
	double length;
	
	public void setId1(String id1) {
		this.id1 = id1;
	}
	public void setId2(String id2) {
		this.id2 = id2;
	}
	public String getId1() {
		return id1;
	}
	public String getId2() {
		return id2;
	}
	public void setLength(double length) {
		this.length = length;
	}
	/** 0 表示默认长度，按照电磁力弹出去 */
	public double getLength() {
		return length;
	}
	/** 没有方向的可以当key的Id */
	public String getId1_Id2_Key() {
		return getId1_Id2_key(id1, id2);
	}
	protected String getId1_vs_Id2() {
		return id1+"\t"+id2;
	}
	/** 没有方向的可以当key的Id */
	protected static String getId1_Id2_key(Node node1, Node node2) {
		String id1 = node1.getId();
		String id2 = node2.getId();
		return getId1_Id2_key(id1, id2);
	}
	/** 没有方向的可以当key的Id */
	public static String getId1_Id2_key(String id1, String id2) {
		if (id1.compareTo(id2) > 0) {
			String idTmp = id1;
			id1 = id2;
			id2 = idTmp;
		}
		return id1+nodeSep+id2;
	}
	public String getLabel() {
		return length+"";
	}
	/**
	 * 假设第一个点在前面，第二个点在后面
	 * @param node1
	 * @param node2
	 * @return node1 减去这两个坐标，node2加上这两个坐标
	 */
	public double[] getAxisAttractMove(Node node1, Node node2, double step) {
		double deltaX = node1.getX() - node2.getX();
		double deltaY = node1.getY() - node2.getY();
		double deltaLength = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
		if (deltaLength == 0) {
			Random random = new Random();
			deltaX = (double)random.nextInt(10)/10 * randomSign(random);
			deltaY = (double)random.nextInt(10)/10 * randomSign(random);
			deltaLength = Math.sqrt(deltaX*deltaX + deltaY*deltaY);
		}
		
		double deltaLengthReal = deltaLength - node1.getR() - node2.getR();
		double force = Math.abs(Math.pow(deltaLengthReal - length, 1) * condensefactor/2);
		double deltaXResult =Math.abs(deltaX/deltaLength*force);
		double deltaYResult = Math.abs(deltaY/deltaLength*force);
		if (deltaLengthReal > length) {
			deltaXResult = -deltaXResult;
			deltaYResult = -deltaYResult;
		}
		if (node1.getX() > node2.getX()) {
			deltaXResult = - deltaXResult;
		}
		if (node1.getY() > node2.getY()) {
			deltaYResult = -deltaYResult;
		}
 		return new double[] {deltaXResult, deltaYResult};
	}
	
	private static int randomSign(Random random) {
		int num = random.nextInt(9);
		return num <= 4 ? -1:1;
	}
}