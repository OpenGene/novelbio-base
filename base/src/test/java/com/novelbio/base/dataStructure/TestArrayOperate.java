package com.novelbio.base.dataStructure;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.novelbio.base.dataStructure.ArrayOperate;

import junit.framework.TestCase;

public class TestArrayOperate extends TestCase {
	double[] aa = null;double[] bb = null;double[] cc = null;
	Integer[] aaa = null;Integer[] bbb = null;Integer[] ccc = null;
	
	
	@Test
	public void testCuttArray() {
		aa = new double[]{1,2,3,4,5,6,7,8,9};
		bb = ArrayOperate.cuttArray(aa, 4, 3, 2, -1);
		cc = new double[]{1,2,3,4,5,6};
		assertEquals(cc.length, bb.length);
		for (int i = 0; i < bb.length; i++) {
			assertEquals(cc[i], bb[i]);
		}
		
		aa = new double[]{1,2,3,4,5,6,7,8,9};
		bb = ArrayOperate.cuttArray(aa, 4, 5, 2, -1);
		cc = new double[]{-1,-1,1,2,3,4,5,6};
		assertEquals(cc.length, bb.length);
		for (int i = 0; i < bb.length; i++) {
			assertEquals(cc[i], bb[i]);
		}
		
		aa = new double[]{1,2,3,4,5,6,7,8,9};
		bb = ArrayOperate.cuttArray(aa, 4, 5, 6, -1);
		cc = new double[]{-1,-1,1,2,3,4,5,6,7,8,9,-1};
		assertEquals(cc.length, bb.length);
		for (int i = 0; i < bb.length; i++) {
			assertEquals(cc[i], bb[i]);
		}
		
		aa = new double[]{1,2,3,4,5,6,7,8,9};
		bb = ArrayOperate.cuttArray(aa, 6, 3, 3, -1);
		cc = new double[]{3,4,5,6,7,8,9};
		assertEquals(cc.length, bb.length);
		for (int i = 0; i < bb.length; i++) {
			assertEquals(cc[i], bb[i]);
		}
		
		aa = new double[]{1,2,3,4,5,6,7,8,9};
		bb = ArrayOperate.cuttArray(aa, 6, 3, 4, -1);
		cc = new double[]{3,4,5,6,7,8,9,-1};
		assertEquals(cc.length, bb.length);
		for (int i = 0; i < bb.length; i++) {
			assertEquals(cc[i], bb[i]);
		}
		
		aa = new double[]{1,2,3,4,5,6,7,8,9};
		bb = ArrayOperate.cuttArray(aa, 6, 3, 2, -1);
		cc = new double[]{3,4,5,6,7,8};
		assertEquals(cc.length, bb.length);
		for (int i = 0; i < bb.length; i++) {
			assertEquals(cc[i], bb[i]);
		}
		
		aa = new double[]{1,2,3,4,5,6,7,8,9};
		bb = ArrayOperate.cuttArray(aa, 6, 6, 2, -1);
		cc = new double[]{-1,1,2,3,4,5,6,7,8};
		assertEquals(cc.length, bb.length);
		for (int i = 0; i < bb.length; i++) {
			assertEquals(cc[i], bb[i]);
		}
		
	}
	
	@Test
	public void testIndeltArray() {
		ArrayList<int[]> lsIndelInfo = new ArrayList<int[]>();
		lsIndelInfo.clear();
		aaa = new Integer[]{1,2,3,4,5,6,7,8,9};
		lsIndelInfo.add(new int[]{0,-1});
		lsIndelInfo.add(new int[]{0,2});
		bbb = ArrayOperate.indelElement(aaa, lsIndelInfo,null);
		ccc = new Integer[]{null,null,2,3,4,5,6,7,8,9};
		assertEquals(ccc.length, bbb.length);
		for (int i = 0; i < bbb.length; i++) {
			assertEquals(ccc[i], bbb[i]);
		}
		
		lsIndelInfo.clear();
		aaa = new Integer[]{1,2,3,4,5,6,7,8,9};
		lsIndelInfo.add(new int[]{0,-1});
		lsIndelInfo.add(new int[]{0,2});
		lsIndelInfo.add(new int[]{1,2});
		lsIndelInfo.add(new int[]{2,-1});
		lsIndelInfo.add(new int[]{4,2});
		lsIndelInfo.add(new int[]{5,-1});
		lsIndelInfo.add(new int[]{8,-1});
		lsIndelInfo.add(new int[]{9,2});
		bbb = ArrayOperate.indelElement(aaa, lsIndelInfo,0);
		ccc = new Integer[]{0,0,0,0,2,4,0,0,5,7,8,0,0};
		assertEquals(ccc.length, bbb.length);
		for (int i = 0; i < bbb.length; i++) {
			assertEquals(ccc[i], bbb[i]);
		}
		
		lsIndelInfo.clear();
		aaa = new Integer[]{1,2,3,4,5,6,7,8,9};
		lsIndelInfo.add(new int[]{0,2});
		lsIndelInfo.add(new int[]{2,2});
		lsIndelInfo.add(new int[]{4,2});
		lsIndelInfo.add(new int[]{5,2});
		lsIndelInfo.add(new int[]{9,2});
		bbb = ArrayOperate.indelElement(aaa, lsIndelInfo,0);
		ccc = new Integer[]{0,0,1,2,0,0,3,4,0,0,5,0,0,6,7,8,9,0,0};
		assertEquals(ccc.length, bbb.length);
		for (int i = 0; i < bbb.length; i++) {
			assertEquals(ccc[i], bbb[i]);
		}
	}
	
	@Test
	public void testAddList() {
		List<Integer> lsSummary = Lists.newArrayList(1,2,3,4,5);
		List<Integer> lsAdd = Lists.newArrayList(2,3,4,5,6);
		ArrayOperate.addListToFirst(lsSummary, lsAdd);
		assertEquals( Lists.newArrayList(3,5,7,9,11), lsSummary);
		
		lsSummary = Lists.newArrayList(1,2,3,4);
		lsAdd = Lists.newArrayList(2,3,4,5,6);
		ArrayOperate.addListToFirst(lsSummary, lsAdd);
		assertEquals( Lists.newArrayList(3,5,7,9,6), lsSummary);
		
		lsSummary = Lists.newArrayList(1,2,3,4,5);
		lsAdd = Lists.newArrayList(2,3,4,5);
		ArrayOperate.addListToFirst(lsSummary, lsAdd);
		assertEquals( Lists.newArrayList(3,5,7,9,5), lsSummary);
		//========
		List<Integer> ls1 = Lists.newArrayList(1,2,3,4,5);
		List<Integer> ls2 = Lists.newArrayList(2,3,4,5,6);
		List<Integer> lsSum = ArrayOperate.addList(ls1, ls2);
		assertEquals( Lists.newArrayList(3,5,7,9,11), lsSum);
		
		ls1 = Lists.newArrayList(1,2,3,4);
		ls2 = Lists.newArrayList(2,3,4,5,6);
		lsSum = ArrayOperate.addList(ls1, ls2);
		assertEquals( Lists.newArrayList(3,5,7,9,6), lsSum);
		
		ls1 = Lists.newArrayList(1,2,3,4,5);
		ls2 = Lists.newArrayList(2,3,4,5);
		lsSum = ArrayOperate.addList(ls1, ls2);
		assertEquals( Lists.newArrayList(3,5,7,9,5), lsSum);
	}
}
