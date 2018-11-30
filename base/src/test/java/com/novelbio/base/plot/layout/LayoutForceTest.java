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

		List<Node> lsNodes = getMockNodes();
		List<Edge> lsEdges = getMockEdges();
		// LayoutForce.initialNodePos(lsNodes);

		LayoutForce layoutForce = new LayoutForce();
		layoutForce.setLsNodesAndEdges(lsNodes, lsEdges);
		lsNodes = layoutForce.springLayout(10000);

		System.out.println(JSON.toJSON(lsNodes));
		System.out.println(JSON.toJSON(lsEdges));
	}

	private List<Node> getMockNodes() {
		List<Node> lsNodes = new ArrayList<>();
		int[][] nodeArray = new int[][] { { 1, 1, 1, 1 }, { 3, 3, 3, 1 }, { 9, 9, 9, 1 } };
		for (int i = 0; i < nodeArray.length; i++) {
			Node node = new Node(String.valueOf(nodeArray[i][0]), nodeArray[i][1], nodeArray[i][2]);
			node.setR(nodeArray[i][3]);
			lsNodes.add(node);
		}

		return lsNodes;
	}

	private List<Edge> getMockEdges() {
		List<Edge> lsEdges = new ArrayList<>();
		int[][] edgeArray = new int[][] { { 1, 3, 1 }, { 3, 9, 7 } };
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
