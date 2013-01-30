package com.novelbio.base.dataStructure.listOperate;

import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.commons.math.stat.descriptive.moment.ThirdMoment;
import org.apache.ibatis.migration.commands.NewCommand;

import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;

/**
 * 双坐标
 * @author zong0jie
 *
 */
public class ListCodAbsDu<T extends ListDetailAbs, K extends ListCodAbs<T>>  {
	//这两个都会在具体的类中新建
	/** peak与左端Item交集时，交集在左端Item中所占的比例 */
	protected double opLeftInItem = -1;
	/** peak与左端Item交集时，交集在peak中所占的比例，如果本值为100，说明该peak在一个Item内  */
	protected double opLeftInCod = -1;
	/**  peak与左端Item交集时，实际交集的bp数  */
	protected int opLeftBp = -1;
	/**  peak与右端Item交集时，交集在左端Item中所占的比例 */
	protected double opRightInItem = -1;
	/**  peak与右端Item交集时，交集在peak中所占的比例，如果本值为100，说明该peak在一个Item内  */
	protected double opRightInCod = -1;
	/** peak与右端Item交集时，实际交集的bp数 */
	protected int opRightBp = -1;
	
	public ListCodAbsDu(ArrayList<T> lsgffDetail, K gffCod1, K gffCod2)
	{
		this.lsgffDetailsMid = lsgffDetail;
		this.gffCod1 = gffCod1;
		this.gffCod2 = gffCod2;
		calInfo();
	}
	public ListCodAbsDu(K gffCod1, K gffCod2)
	{
		this.gffCod1 = gffCod1;
		this.gffCod2 = gffCod2;
		calInfo();
	}

	protected K gffCod1 = null;
	public K getGffCod1() {
		return gffCod1;
	}
	public K getGffCod2() {
		return gffCod2;
	}
	protected K gffCod2 = null;	
	
	//两个端点之间的gffdetail
	protected ArrayList<T> lsgffDetailsMid = new ArrayList<T>();
	public void setLsgffDetailsMid(ArrayList<T> lsgffDetailsMid) {
		this.lsgffDetailsMid = lsgffDetailsMid;
	}
	/**
	 *  peak与左端Item交集时，交集在左端Item中所占的比例
	 * @return
	 */
	public double getOpLeftInItem() {
		return opLeftInItem;
	}
	/**
	 * peak与右端Item交集时，交集在左端Item中所占的比例
	 * @return
	 */
	public double getOpRightInItem() {
		return opRightInItem;
	}
	/**
	 *  peak与左端Item交集时，交集在peak中所占的比例，如果本值为100，说明该peak在一个Item内
	 * @return
	 */
	public double getOpLeftInCod() {
		return opLeftInCod;
	}
	/**
	 *  peak与右端Item交集时，交集在peak中所占的比例，如果本值为100，说明该peak在一个Item内
	 * @return
	 */
	public double getOpRightInCod() {
		return opRightInCod;
	}
	/**
	 *  peak与左端Item交集时，实际交集的bp数
	 * @return
	 */
	public int getOpLeftBp() {
		return opLeftBp;
	}
	/**
	 *  peak与右端Item交集时，实际交集的bp数
	 * @return
	 */
	public int getOpRightBp() {
		return opRightBp;
	}
	/**
	 * 返回左端的GffCod，覆盖成相应的GffCod类
	 * @return
	 */
	public K getGffCodLeft()
	{
		return gffCod1;
	}
	/**
	 * 返回右端的GffCod，覆盖成相应的GffCod类
	 * @return
	 */
	public K getGffCodRight()
	{
		return gffCod2;
	}
	/**
	 * 返回两个坐标中间夹着的的GffDetail，覆盖成相应的GffDetail类
	 * @return
	 */
	public ArrayList<T> getLsGffDetailMid()
	{
		return lsgffDetailsMid;
	}
	/**
	 * 返回全部包含的gffDetail信息
	 * @return
	 * 空的则返回一个size为0的list
	 */
	public ArrayList<T> getAllGffDetail() {
		ArrayList<T> lsGffDetailAll = new ArrayList<T>();
		if (getGffCodLeft() != null && getGffCodLeft().isInsideLoc()) {
			if (getGffCodLeft().isInsideUp())
				lsGffDetailAll.add(getGffCodLeft().getGffDetailUp());
			lsGffDetailAll.add(getGffCodLeft().getGffDetailThis());
		}
		if (lsgffDetailsMid != null) {
			for (T t : lsgffDetailsMid) {
				lsGffDetailAll.add(t);
			}
		}
		if (getGffCodRight() != null && getGffCodRight().isInsideLoc()) {
			if (getGffCodRight().isInsideDown())
				lsGffDetailAll.add(getGffCodRight().getGffDetailDown());
			lsGffDetailAll.add(getGffCodRight().getGffDetailThis());
		}
		return lsGffDetailAll;
	}
	/**
	 * 双坐标查找 输入相关的GffHash类，然后填充相关信息<br>
	 */
	private void calInfo() {
		T gffDetail1 = gffCod1.getGffDetailThis();
		T gffDetail2 = gffCod2.getGffDetailThis();
		int leftItemLength = 0; int leftoverlap = 0;
		int rightItemLength = 0; int rightoverlap = 0;
		int peakLength = gffCod2.getCoord() - gffCod1.getCoord();
		if (gffDetail1 != null) {
			leftItemLength = gffDetail1.numberend - gffDetail1.numberstart;
			leftoverlap = gffDetail1.numberend - gffCod1.getCoord();
		}
		if (gffDetail2 != null) {
			rightItemLength = gffDetail2.numberend - gffDetail2.numberstart;
			rightoverlap = gffCod2.getCoord() - gffDetail2.numberstart;
		}
		/**
		 * 如果peak两个端点都在在同一条目之内
		 */
		if (gffCod1.insideLOC && gffCod2.insideLOC && 
				gffDetail1.equals(gffDetail2)
		)
		{
			opLeftInItem = 100 * (double) peakLength / leftItemLength;
			opLeftInCod = 100;
			opLeftBp = peakLength; opRightInItem = opLeftInItem; opRightInCod = 100; opRightBp = opLeftBp;
		}
		// 如果peak左端点在一个条目内，右端点在另一个条目内
		else if (gffCod1.insideLOC
				&& gffCod2.insideLOC
				&& !gffCod1.equals(gffCod2) )
		{
			opLeftInItem = 100 * (double) leftoverlap / leftItemLength;
			opLeftInCod = 100 * (double) leftoverlap / peakLength;
			opLeftBp = leftoverlap;
			opRightInItem = 100 * (double) rightoverlap / rightItemLength;
			opRightInCod = 100 * (double) rightoverlap / peakLength;
			opRightBp = rightoverlap;
		}
		// peak只有左端点在条目内
		else if (gffCod1.insideLOC && !gffCod2.insideLOC) {
			opLeftInItem = 100 * (double) leftoverlap / leftItemLength;
			opLeftInCod = 100 * (double) leftoverlap / peakLength;
			opLeftBp = leftoverlap;
			opRightInItem = 0; opRightInCod = 0; opRightBp = 0;
		}
		// peak只有右端点在条目内
		else if (!gffCod1.insideLOC && gffCod2.insideLOC) {
			opLeftInItem = 0; opLeftInCod = 0; opLeftBp = 0;
			opRightInItem = 100 * (double) rightoverlap / rightItemLength;
			opRightInCod = 100 * (double) rightoverlap / peakLength;
			opRightBp = rightoverlap;
		}else {
			opLeftInItem = 0; opLeftInCod = 0; opLeftBp = 0;
			opRightInItem = 0;
			opRightInCod = 0;
			opRightBp = 0;
		}
	}
	
}
