package com.novelbio.base.dataStructure;

import java.util.Comparator;

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
}
