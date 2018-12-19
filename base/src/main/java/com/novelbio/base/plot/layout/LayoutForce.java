package com.novelbio.base.plot.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.HashMultimap;
import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 * 力引导布局的算法。主要用于cytoscape上需要指定边长度时的布局
 * 譬如haploNet会计算获得边的长度
 * @author zong0jie
 * @data 2018年11月16日
 */
public class LayoutForce {
	private int W ; // 画布的宽度
	private int L ;  //画布的长度
	private double C = 0.1;  // 节点距离控制系数
	private double k; //节点之间的距离系数
	private double distanceAvg; //节点之间的距离

	int iteratorNum = 1000;
	private int temperature;
	/**
	 * key: Id
	 * value: node
	 */
	Map<String, Node> mapId2Nodes = new HashMap<>();
	/** key: Id1+ Edge.nodeSep + Id2
	 * value: edge
	 */
	Map<String, Edge> mapIds2Edge = new HashMap<>();
	
	HashMultimap<String, String> mapId2FlankId = HashMultimap.create();
	
	public static void main(String[] args) {
		List<Node> lsNodes = new ArrayList<>();
		List<Edge> lsEdges = new ArrayList<>();
		LayoutForce.initialNodePos(lsNodes);
		LayoutForce layoutForce = new LayoutForce();
		layoutForce.setLsNodesAndEdges(lsNodes, lsEdges);
		lsNodes = layoutForce.springLayout(10000);
	}
	
	public LayoutForce(){}
	/**
	 * 设置长宽
	 * @param width
	 * @param length
	 */
	public LayoutForce(int height, int width) {
		this.L = height;
		this.W = width;
	}
		
	public void setLsNodesAndEdges(List<? extends Node> lsNodes, List<? extends Edge> lsEdges) {
		for (Node node : lsNodes) {
			if (node.getId().contains(Edge.nodeSep)) {
				throw new RuntimeException("node id cannot contain " + Edge.nodeSep);
			}
			if (mapId2Nodes.containsKey(node.getId())) {
				throw new RuntimeException("duplicate node " + node.getId());
			}
			mapId2Nodes.put(node.getId(), node);
		}
		//TODO 暂时不支持环形图
		for (Edge edge : lsEdges) {
			if (mapIds2Edge.containsKey(edge.getId1_Id2_Key())) {
				continue;
			}
			mapIds2Edge.put(edge.getId1_Id2_Key(), edge);
			
			mapId2FlankId.put(edge.getId1(), edge.getId2());
			mapId2FlankId.put(edge.getId2(), edge.getId1());
		}
		setCanvas(lsNodes.size());
		Random random = new Random();
		for (Node node : lsNodes) {
			node.setX(random.nextInt(W));
			node.setY(random.nextInt(L));
		}
	}
	
	/** 设定画布大小 */
	private void setCanvas(int nodeNum) {
		double area = 0;
		if (W > 0 && L > 0) {
			 area = W*L;
		} else {
			area = nodeNum*100*100/Math.pow(nodeNum, 0.2);
		}
		distanceAvg = Math.sqrt(area / (double) nodeNum);
		k = C*distanceAvg;
		if (W <= 0 || L <= 0) {
			W = (int) Math.sqrt(area);
			L = (int) Math.sqrt(area);
		}
		temperature = W / 10; //模拟退火初始温度
	}
	
	public List<Node> springLayout(int iterator) {
		double temp = this.temperature;
		for (int i = 0; i < iterator; i++) {
			springLayoutIt2(temp, temp*3);
//			temp = temp*(1.0 - i / (double) iterator);
			temp = temperature*(1.0 - i / (double) iterator);
		}
		return new ArrayList<>(mapId2Nodes.values());
	}
	/**
	 * 单次坐标调整
	 * @param temperature 退火温度，单次移动最小距离
	 * @return
	 */
	private void springLayoutIt2(double tempNode, double tempEdge) {
		List<Node> lsNodes = new ArrayList<>(mapId2Nodes.values());
		List<Edge> lsEdges = new ArrayList<>(mapIds2Edge.values());
		//2计算每次迭代局部区域内两两节点间的斥力所产生的单位位移（一般为正值）
		if (tempNode > 0) {
			for (int v = 0; v < lsNodes.size(); v++) {
				Node node1 = lsNodes.get(v);
				for (int u = 0; u < lsNodes.size();  u++) {
					if (u == v) {
						continue;
					}
					Node node2 = lsNodes.get(u);
					String edgeKey = Edge.getId1_Id2_key(node1, node2);
					//如果节点之间有关系，并且关系 > 0 ，则受弹簧作用，不考虑电磁排斥
					if (mapIds2Edge.containsKey(edgeKey) && mapIds2Edge.get(edgeKey).getLength() > 0) {
						continue;
					}
					double[] xyMove = Node.getAxisRejectMove(node1, node2, k, distanceAvg);
					addDeltaXY(node1, xyMove, tempNode);
				}
			}
		}
		
//		TxtReadandWrite txtWrite = new TxtReadandWrite("/home/novelbio/git/NBC-OmicsDB/src/test/resources/cyto/data1.json", true);
//		txtWrite.writefileln("{\"lsNodes\":");
//		txtWrite.writefileln();
//		txtWrite.writefileln(JSON.toJSON(lsNodes).toString());
//		txtWrite.writefileln(",\"lsEdges\":");
//		txtWrite.writefileln(JSON.toJSON(lsEdges).toString());
//		txtWrite.writefileln("}");
//		txtWrite.close();
//		System.out.println();

		//3. 计算每次迭代每条边的引力对两端节点所产生的单位位移(一般为负值)
		/**
		 * 考虑连线长度，以及两端节点所关联的节点数
		 * 譬如有 A-------B 两个节点连线，A还连接了5个别的节点，而B仅连接了1个别的节点
		 * 那么我们选择少移动A，多移动B，来让节点更好看
		 * 
		 */
		if (tempEdge > 0) {
			for (Edge edge : lsEdges) {
				String eStartID = edge.getId1();
				String eEndID = edge.getId2();
				Node start = mapId2Nodes.get(eStartID);
				Node end = mapId2Nodes.get(eEndID);
				double[] move = edge.getAxisAttractMove(start, end);
				int[] startEnd = getNodeStartEnd(start, end, (int)edge.getLength());
				int startBefore = startEnd[0];
				int endAfter = startEnd[1];
				//注意求StartProp的时候，分子是endAfter，是因为如果分子是StartBefore
				//那么如果StartBefore很大，则startProp就会很大，那么移动率会变高，所以要用1减去
				double startProp = 1- (double)startBefore/(startBefore+endAfter);
				if (startProp < 0.2) {
					startProp = 0.2;
				}
				double[] moveStart = new double[]{move[0]*startProp, move[1]*startProp};
				minusDeltaXY(start, moveStart, tempEdge);
				
				double endProp = 1-(double)endAfter/(startBefore+endAfter);
				if (endProp < 0.2) {
					endProp = 0.2;
				}
				double[] moveEnd = new double[]{move[0]*endProp, move[1]*endProp};
				addDeltaXY(end, moveEnd, tempEdge);
			}
		}

//		txtWrite = new TxtReadandWrite("/home/novelbio/git/NBC-OmicsDB/src/test/resources/cyto/data2.json", true);
//		txtWrite.writefileln("{\"lsNodes\":");
//		txtWrite.writefileln();
//		txtWrite.writefileln(JSON.toJSON(lsNodes).toString());
//		txtWrite.writefileln(",\"lsEdges\":");
//		txtWrite.writefileln(JSON.toJSON(lsEdges).toString());
//		txtWrite.writefileln("}");
//		txtWrite.close();
//		System.out.println();
//		for (Node node : lsNodes) {
//			//不能出画图版的区域
//			node.setX(Math.min(W / 2, Math.max(-1.0 * W / 2, node.getX())));
//			node.setY(Math.min(L / 2, Math.max(-1.0 * L / 2, node.getY())));
//		}
    }
	
	private void addDeltaXY(Node node, double[] dxy, double temperature) {
		setDeltaXY(node, dxy, temperature, true);
	}

	private void minusDeltaXY(Node node, double[] dxy, double temperature) {
		setDeltaXY(node, dxy, temperature, false);
	}
	
	private void setDeltaXY(Node node, double[] dxy, double temperature, boolean isplus) {
		double dx = dxy[0], dy = dxy[1];
		double dispLength = Math.sqrt(dx * dx + dy * dy);
		double xDisp = 0, yDisp = 0;
		if (dx != 0 || dy != 0) {
			xDisp = dx / dispLength * Math.min(dispLength, temperature);
			yDisp = dy / dispLength * Math.min(dispLength, temperature);
		}
		if (isplus) {
			node.setX(node.getX()+xDisp);
			node.setY(node.getY()+yDisp);
		} else {
			node.setX(node.getX()-xDisp);
			node.setY(node.getY()-yDisp);
		}
	}
	
	/** 如果节点和degree不一致，优先比较节点
	 * 存在这种情况，start侧的节点很多，end侧的连线很多，但是节点少，就是节点之间互相连
	 * 
	 * 未来可以考虑连线和节点加权加和
	 * @param start
	 * @param end
	 * @param length
	 * @return
	 */
	private int[] getNodeStartEnd(Node start, Node end, int length) {
		int[] startBefore = getNodeNumBeforeNode(start, end, length);
		int[] endAfter = getNodeNumBeforeNode(end, start, length);
		
		int[] result = null;
		if (startBefore[0]*1.25 < endAfter[0] && startBefore[1] >= endAfter[1]
				|| startBefore[0] > endAfter[0]*1.25 && startBefore[1] <= endAfter[1]
				) {
			result = new int[]{startBefore[0], endAfter[0]};
		}
		double nodeNumIndex = Math.abs(Math.log((startBefore[0]+1)/(endAfter[0]+1)));
		double degreeNumIndex = Math.abs(Math.log((startBefore[1]+1)/(endAfter[1]+1)));
		if (nodeNumIndex > degreeNumIndex) {
			result = new int[]{startBefore[0], endAfter[0]};
		} else {
			result = new int[]{startBefore[1], endAfter[1]};
		}
		if (result[0] + result[1] == 0) {
			result = new int[]{1,1};
		}
		return result;
	}
	
	/**
	 * 获得与这个node直接或间接相连的，在这个node这一侧的全部node数
	 * 
	 * 网络中两个点相连，在两个点中画一条斜线，然后可以看到两个点所关联的点的密度是会有差别的。
	 * 那么密度较小一侧的点，可以移动的距离大一些，密度较大一侧的点，可以移动的距离小一些
	 * 
	 * @return int[2] 0: 相关的点的数量 1：相关的degree的数量
	 * 外层优先比较点，点差距不大比较degree
	 */
	public int[] getNodeNumBeforeNode(Node node1, Node node2, int length) {
		if (Node.getDistanceReal(node1, node2) < length/10 || length == 0 && Node.getDistanceReal(node1, node2) < 10) {
			return new int[]{1, 1};
		}
		double slope = 0;
		if (Math.abs(node1.getX() - node2.getX()) < 1) {
			slope = 0;
		} else if (Math.abs(node1.getY() - node2.getY()) < 1) {
			slope = 10000;
		} else {
			slope = (node2.getY()-node1.getY())/(node2.getX()-node1.getX());
			slope = -1/slope;
		}
		boolean isSmall = true;
		if (slope == 0) {
			isSmall = node1.getY() < node2.getY();
		} else {
			isSmall = node1.getX() < node2.getX();
		}
		
		Set<String> setIdsNeed = new HashSet<>();
		setIdsNeed.add(node1.getId());
		searchNode(setIdsNeed, node1.getId(), node1, slope, isSmall);
		
		Set<String> setEdge = new HashSet<>();
		for (String id : setIdsNeed) {
			Set<String>setIdsFlank = mapId2FlankId.get(id);
			for (String idFlank : setIdsFlank) {
				setEdge.add(Edge.getId1_Id2_key(id, idFlank));
			}
		}
		int numDegree = setEdge.size() == 0 ? 0 : setEdge.size()-1;
		return new int[]{setIdsNeed.size(), numDegree};
	}
	
	private void searchNode(Set<String> setIdsAll, String nodeId, Node nodeSite, double slope, boolean small) {
		Set<String> setIds = mapId2FlankId.get(nodeId);
		for (String id : setIds) {
			Node node = mapId2Nodes.get(id);
			//防止死循环，就是如果是一个环形图，这个node下一个下一个很可能回到自身
			if (setIdsAll.contains(id)) {
				continue;
			}
			if (isNeedDot(slope, nodeSite, node, small)) {
				setIdsAll.add(node.getId());
				searchNode(setIdsAll, node.getId(), nodeSite, slope, small);
			}
		}
	}
	
	/** 这个位点是否需要
	 * 
	 * @param slope
	 * @param nodeSite 确定直线的位点
	 * @param node 想看的位点，就是想看这个位点是否满足需求
	 * @param small
	 * @return
	 */
	private boolean isNeedDot(double slope, Node nodeSite, Node node, boolean small) {
		if (nodeSite.getId().equals(node.getId())) {
			return false;
		}
		
		if (slope >= 10000 || slope <= -10000) {
			return small ? node.getX() < nodeSite.getX() : node.getX() > nodeSite.getX(); 
		} else if (slope == 0) {
			return small ? node.getY() < nodeSite.getY() : node.getY() > nodeSite.getY(); 
		} else {
			double y = slope*node.getX() - slope*nodeSite.getX() + nodeSite.getY();
			return !(slope > 0 ^ small) ? node.getY() > y : node.getY() < y;
		}
	}
	
    /** 给node设定初始坐标 */
	public static void initialNodePos(List<Node> lsNodes) {
		double area = lsNodes.size()*100*100/Math.pow( lsNodes.size(), 0.2);
		int length = (int)Math.sqrt(area)/2;
		Random random = new Random();
		for (Node node : lsNodes) {
			node.setX(random.nextInt(length));
			node.setY(random.nextInt(length));
		}
	}
	
	/** 设定画布大小 */
	public static int[] getCanvas(int nodeNum) {
		double area = 0;
		area = nodeNum*100*100/Math.pow(nodeNum, 0.2);
		int W = (int) Math.sqrt(area);
		int L = (int) Math.sqrt(area);
		return new int[]{W, L};
	}
	
}

