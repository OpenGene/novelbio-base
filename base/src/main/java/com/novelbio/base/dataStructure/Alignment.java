package com.novelbio.base.dataStructure;

public interface Alignment {
	int getStartAbs();
	int getEndAbs();
	int getStartCis();
	int getEndCis();
	Boolean isCis5to3();
	int getLength();
	String getRefID();
}
