package com.novelbio.base.dataStructure;

public interface StatisticsTest {

    public double getRightTailedP(int a, int b, int c, int d);

    public double getLeftTailedP(int a, int b, int c, int d);

    public double getTwoTailedP(int a, int b, int c, int d);
    
    public static enum StatisticsPvalueType {
    	TwoTail, LeftTail, RightTail
    }
}
