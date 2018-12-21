/**
 *
 * @author novelbio fans.fan
 * @date 2018年11月30日
 */
package com.novelbio.base.plot.layout;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.novelbio.base.dataOperate.TxtReadandWrite;

/**
 *
 * @author novelbio fans.fan
 */
public class LayoutForceTest {

	/**
	 * Test method for
	 * {@link com.novelbio.base.plot.layout.LayoutForce#springLayout(int)}.
	 */
	@Test
	public void testSpringLayout() {
		// List<Node> lsNodes = getLsNodes(pegasNode);
		// List<EdgeHaplo> lsEdges = getLsEdges(pegasEdge);
		//
		// LayoutForce layoutForce = new LayoutForce();
		// layoutForce.setLsNodesAndEdges(lsNodes, lsEdges);
		// lsNodes = layoutForce.springLayout(1000);
		
		TxtReadandWrite txtWrite = new TxtReadandWrite("/home/novelbio/git/NBC-OmicsDB/src/test/resources/cyto/data2.json", true);
		
		List<Node> lsNodes = getMockNodes();
		List<Edge> lsEdges = getMockEdges();
		LayoutForce layoutForce = new LayoutForce(1000, 1000);
		layoutForce.setLsNodesAndEdges(lsNodes, lsEdges);
		lsNodes = layoutForce.springLayout(700);
//		for (Node node : lsNodes) {
//			node.setX(node.getX()*10);
//			node.setY(node.getY()*10);
//		}
		txtWrite.writefileln("{\"lsNodes\":");
		txtWrite.writefileln();
		txtWrite.writefileln(JSON.toJSON(lsNodes).toString());
		txtWrite.writefileln(",\"lsEdges\":");
		txtWrite.writefileln(JSON.toJSON(lsEdges).toString());
		txtWrite.writefileln("}");
		txtWrite.close();
	}

	private List<Node> getMockNodes() {
		List<Node> lsNodes = new ArrayList<>();
		int[][] nodeArray = new int[][] { { 1, 1, 1, 1 }, { 2, 1, 1, 1 }, { 3, 1, 1, 1 } , { 4, 1, 1, 1 } , { 5, 1, 1, 1 } };
		for (int i = 0; i < nodeArray.length; i++) {
			Node node = new Node(String.valueOf(nodeArray[i][0]), nodeArray[i][1], nodeArray[i][2]);
			node.setR(nodeArray[i][3]);
			lsNodes.add(node);
		}
		return lsNodes;
	}

	private List<Edge> getMockEdges() {
		List<Edge> lsEdges = new ArrayList<>();
		int[][] edgeArray = new int[][] { { 1, 3, 50 }, { 4, 5, 220 }, { 1, 4, 150 } , { 2, 4, 300 }, { 2, 5, 500 } };
//		int[][] edgeArray = new int[][] { { 4, 5, 220 }, { 1, 4, 150 } , { 2, 4, 300 }, { 2, 5, 500 } };
		for (int i = 0; i < edgeArray.length; i++) {
			Edge edge = new Edge();
			edge.setId1(String.valueOf(edgeArray[i][0]));
			edge.setId2(String.valueOf(edgeArray[i][1]));
			edge.setLength(edgeArray[i][2]);
			lsEdges.add(edge);
		}

		return lsEdges;
	}

}
