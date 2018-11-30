package com.novelbio.base.plot.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.novelbio.base.SepSign;

/**
 * 力引导布局的算法。主要用于cytoscape上需要指定边长度时的布局
 * 譬如haploNet会计算获得边的长度
 * @author zong0jie
 * @data 2018年11月16日
 */
public class LayoutForce {
	private int W ; // 画布的宽度
	private int L ;  //画布的长度
	private double C = 1;  // 节点距离控制系数
	private double k; //节点之间的距离
    
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
	
	public static void main(String[] args) {
		List<Node> lsNodes = new ArrayList<>();
		List<Edge> lsEdges = new ArrayList<>();
		LayoutForce.initialNodePos(lsNodes);
		LayoutForce layoutForce = new LayoutForce();
		layoutForce.setLsNodesAndEdges(lsNodes, lsEdges);
		lsNodes = layoutForce.springLayout(10000);
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
		}
		setCanvas(lsNodes.size());
	}
	
	/** 设定画布大小 */
	private void setCanvas(int nodeNum) {
		double area = 0;
		if (W > 0 && L > 0) {
			 area = W*L;
		} else {
			area = nodeNum*100*100/Math.pow(nodeNum, 0.2);
		}
		k= C* Math.sqrt(area / (double) nodeNum);
		if (W <= 0 || L <= 0) {
			W = (int) Math.sqrt(area);
			L = (int) Math.sqrt(area);
		}
		temperature = W / 10; //模拟退火初始温度
	}
	
	public List<Node> springLayout(int iterator) {
		int temperature = this.temperature; 
		for (int i = 0; i < iterator; i++) {
			springLayoutIt(temperature);
			temperature *= (1.0 - i / (double) iterator);
		}
		return new ArrayList<>(mapId2Nodes.values());
	}
	
	/**
	 * 单次坐标调整
	 * @param temperature 退火温度，单次移动最小距离
	 * @return
	 */
	private void springLayoutIt(int temperature) {
		List<Node> lsNodes = new ArrayList<>(mapId2Nodes.values());
		List<Edge> lsEdges = new ArrayList<>(mapIds2Edge.values());
		//2计算每次迭代局部区域内两两节点间的斥力所产生的单位位移（一般为正值）
		Map<String,Double> dispx = new HashMap<String,Double>();
		Map<String,Double> dispy = new HashMap<String,Double>();
		
		for (int v = 0; v < lsNodes.size(); v++) {
			String id = lsNodes.get(v).getId();
			dispx.put(id, 0.0);
			dispy.put(id, 0.0);
			for (int u = 0; u < lsNodes.size();  u++) {
				if (u == v) {
					continue;
				}
				Node node1 = lsNodes.get(v);
				Node node2 = lsNodes.get(u);
				String edgeKey = Edge.getId1_Id2_key(node1, node2);
				//如果节点之间有关系，并且关系 > 0 ，则受弹簧作用，不考虑电磁排斥
				if (mapIds2Edge.containsKey(edgeKey) && mapIds2Edge.get(edgeKey).getLength() > 0) {
					continue;
				}
				double[] xyMove = Node.getAxisRejectMove(lsNodes.get(v), lsNodes.get(u), k);
				dispx.put(id,dispx.get(id) + xyMove[0]);
				dispy.put(id,dispy.get(id) + xyMove[1]);
			}
		}
		//3. 计算每次迭代每条边的引力对两端节点所产生的单位位移(一般为负值)
		for (Edge edge : lsEdges) {
			String eStartID = edge.getId1();
			String eEndID = edge.getId2();
			Node start = mapId2Nodes.get(eStartID);
			Node end = mapId2Nodes.get(eEndID);
			double[] move = edge.getAxisAttractMove(start, end);
			dispx.put(eStartID, dispx.get(eStartID) - move[0]);
			dispy.put(eStartID, dispy.get(eStartID) - move[1]);
			dispx.put(eEndID, dispx.get(eEndID) + move[0]);
			dispy.put(eEndID, dispy.get(eEndID) + move[1]);
		}
		
		for (Node node : lsNodes) {
			Double dx = dispx.get(node.getId());
			Double dy = dispy.get(node.getId());
			double dispLength = Math.sqrt(dx * dx + dy * dy);
			double xDisp = dx / dispLength * Math.min(dispLength, temperature);
			double yDisp = dy / dispLength * Math.min(dispLength, temperature);

			node.setX(node.getX()+xDisp);
			node.setY(node.getY()+yDisp);
			node.setX(Math.min(W / 2, Math.max(-1.0 * W / 2, node.getX())));
			node.setY(Math.min(L / 2, Math.max(-1.0 * L / 2, node.getY())));
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
	
}

