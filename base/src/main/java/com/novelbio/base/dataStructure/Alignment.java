package com.novelbio.base.dataStructure;

import java.util.Comparator;

import com.novelbio.base.StringOperate;

public interface Alignment {
	int getStartAbs();
	int getEndAbs();
	int getStartCis();
	int getEndCis();
	Boolean isCis5to3();
	int getLength();
	String getRefID();
	
	public static class ComparatorAlignment implements Comparator<Alignment> {
		@Override
		public int compare(Alignment o1, Alignment o2) {
			Integer o1start = o1.getStartAbs();
			Integer o2start = o2.getStartAbs();
			if (o1.isCis5to3() != null && o2.isCis5to3() != null && o1.isCis5to3() == o2.isCis5to3()) {
				if (o1.isCis5to3()) {
					return o1start.compareTo(o2start);
				} else {
					return -o1start.compareTo(o2start);
				}
			} else {
				return o1start.compareTo(o2start);
			}
		}
		
	}
	
	/** 判断Align1是否cover Align2 */
	public static boolean isAlignCoverAnother(Alignment align1, Alignment align2) {
		return align1.getStartAbs() <= align2.getStartAbs() && align1.getEndAbs() >= align2.getEndAbs();
	}
	
	/** 判断两个align是否overlap */
	public static boolean isOverlap(Alignment align1, Alignment align2) {
		return align1.getStartAbs() <= align2.getEndAbs() && align1.getEndAbs() >= align2.getStartAbs();
	}
	
	/** 判断两个align是否overlap */
	public static boolean isSiteInAlign(Alignment align, int site) {
		return site >= align.getStartAbs() && site <= align.getEndAbs();
	}

	public static int overlapLen(Alignment align1, Alignment align2) {
		validateRef(align1, align2);
		if (!isOverlap(align1, align2)) {
			return 0;
		}
		int refStart = align1.getStartAbs();
		int refEnd = align1.getEndAbs();
		
		int altStart = align2.getStartAbs();
		int altEnd = align2.getEndAbs();
		
		int len = Math.min(refEnd, altEnd) - Math.max(refStart, altStart);
		return len;
	}
	
	static void validateRef(Alignment align1, Alignment align2) {
		//这个报错应该不会出现
		if (!StringOperate.isEqual(align1.getRefID(), align2.getRefID())) {
			throw new RuntimeException("refId is differ " + align1.getRefID() + " " + align2.getRefID());
		}
	}
}
