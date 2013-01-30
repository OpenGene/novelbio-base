package com.novelbio.base.dataStructure.listOperate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.collections.functors.IfClosure;
import org.apache.log4j.Logger;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler.referenceInsertExecutor;

import com.novelbio.analysis.seq.genome.gffOperate.ExonInfo;
import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.database.domain.geneanno.SepSign;
import com.novelbio.database.model.modgeneid.GeneID;
/**
 * 考虑将其拆分成为三个不同的list，一个cis，一个trans，一个null
 * @author zong0jie
 *
 * @param <E>
 */
public class ListAbs <E extends ListDetailAbs> extends ArrayList<E>  implements Cloneable {
	private static final long serialVersionUID = -3356076601369239937L;
	private static Logger logger = Logger.getLogger(ListAbs.class);
	/**保存某个坐标到所在的内含子/外显子起点的距离 */
	HashMap<Integer, Integer> hashLocExInStart;
	/** 保存某个坐标到所在的内含子/外显子终点的距离 */
	HashMap<Integer, Integer> hashLocExInEnd;
	/** 本条目的名字 */
	protected String listName;
	/** 方向 */
	Boolean cis5to3 = null;
	
	public void setName(String listName) {
		this.listName = listName;
	}
	public String getName() {
		if (listName == null) {
			if (size() > 0) {
				listName = get(0).getRefID();
			}
			else {
				listName = "";
			}
		}
		return listName;
	}
	/**
	 * 没有方向则返回null
	 * @return
	 */
	public Boolean isCis5to3() {
		return cis5to3;
	}
	public void setCis5to3(boolean cis5to3) {
		this.cis5to3 = cis5to3;
 	}
	/**
	 * 返回实际第num个element间区的长度
	 * @param num 实际数目
	 * @return
	 */
	public int getInterGenic(int num) {
		if (cis5to3 == null) {
			return get(num).getStartAbs() - get(num - 1).getEndAbs();
		}
		else {
			return Math.abs(get(num).getStartCis() - get(num - 1).getEndCis());
		}
	}
	/**
	 * 返回实际第num个element的长度
	 * @param num 实际数目
	 * @return
	 */
	public int getEleLen(int num) {
		return get(num-1).Length();
	}
	
	public int getLen() {
		if (cis5to3 != null) {
			return Math.abs(get(0).getStartCis() - get(size()-1).getEndCis()) + 1;
 		}
		else {
			if (size() == 1) {
				return get(0).Length();
			} else {
				if (get(0).getStartAbs() < get(1).getStartAbs()) {
					return get(size()-1).getEndAbs() - get(0).getStartAbs();
				}
				else {
					return get(0).getEndAbs() - get(size()-1).getStartAbs();
				}
			}
		}
	}
	/**
	 * 在下游返回正数，上游返回负数
	 * @param loc
	 * @return
	 */
	protected int getLoc2Start(int loc) {
		if (isLocDownStart(loc)) {
			return Math.abs(loc - getStart());
		} else {
			return -Math.abs(loc - getStart());
		}
	}
	/**
	 * 在下游返回正数，上游返回负数
	 * @param loc
	 * @return
	 */
	protected int getLoc2End(int loc) {
		if (isLocDownEnd(loc)) {
			return Math.abs(loc - getEnd());
		} else {
			return -Math.abs(loc - getEnd());
		}
	}
	/**
	 * 输入的loc是否在本list的范围外
	 * @return
	 */
	protected boolean isLocInside(int loc) {
		if (loc >= Math.max(getStart(), getEnd()) || loc <= Math.min(getStart(), getEnd())) {
			return false;
		}
		return true;
	}
	/**
	 * 输入的loc是否在Start的下游
	 * @return
	 */
	protected boolean isLocDownStart(int loc) {
		if (isCis5to3() && loc >= getStart()
		||
		!isCis5to3() && loc <= getStart()
		) {
			return true;
		}
		return false;
	}
	/**
	 * 输入的loc是否在Start的下游
	 * @return
	 */
	protected boolean isLocDownEnd(int loc) {
		if (isCis5to3() && loc >= getEnd()
		||
		!isCis5to3() && loc <= getEnd()
		) {
			return true;
		}
		return false;
	}
	/** 根据方向返回 */
	public int getStart() {
		if (cis5to3 != null) {
			return get(0).getStartCis();
		}
		return get(0).getStartAbs();
	}
	/** 根据方向返回 */
	public int getEnd() {
		if (cis5to3 != null) {
			return get(size() - 1).getEndCis();
		}
		return get(size() - 1).getEndAbs();
	}
	
	/**
	 * 两个坐标之间的距离，仅仅计算他们在mRNA层面的距离，也就是只计算ele上的距离。
	 * 当两者重叠时，返回0
	 * 当loc1在loc2上游时，返回正数，当loc1在loc2下游时，返回负数
	 * 要求这两个坐标都在exon上.如果不符合，则返回GffCodAbs.LOC_ORIGINAL
	 * @param loc1 第一个坐标
	 * @param loc2 第二个坐标
	 */
	public int getLocDistmRNA(int loc1, int loc2) {
		int locSmall = 0; int locBig = 0;
		if (isCis5to3()) {
			locSmall = Math.min(loc1, loc2);  locBig = Math.max(loc1, loc2);
		}
		else {
			locSmall = Math.max(loc1, loc2);  locBig = Math.min(loc1, loc2);
		}
		int locSmallExInNum = getNumCodInEle(locSmall); 
		int locBigExInNum = getNumCodInEle(locBig);
		
		int distance = ListCodAbs.LOC_ORIGINAL;
		
		if (locSmallExInNum <= 0 || locBigExInNum <= 0) 
			return distance;
		
		locSmallExInNum--; locBigExInNum--;
		if (locSmallExInNum == locBigExInNum) {
			distance = locBig - locSmall;
		} else {
			distance = getCod2ExInEnd(locSmall) + getCod2ExInStart(locBig) + 1;
			for (int i = locSmallExInNum + 1; i <= locBigExInNum - 1; i++) {
				distance = distance + get(i).Length();
			}
		}
		
		if ((isCis5to3() && loc1 < loc2) || (!isCis5to3() && loc1 > loc2)) {
			return Math.abs(distance);
		}
		return -Math.abs(distance);
	}
	/**
	 * 必须首先设定ListAbs的方向，并且该方向和其内部的element的方向要一致
	 * 坐标到element 起点距离，如果重叠则为0
	 * @param location 坐标
	 */
	public int getCod2ExInStart(int location) {
		if (hashLocExInStart == null) {
			hashLocExInStart = new HashMap<Integer, Integer>();
		}
		else if (hashLocExInStart.containsKey(location)) {
			return hashLocExInStart.get(location);
		}
		int loc2ExInStart = -1000000000;
		int exIntronNum = getNumCodInEle(location);
		int NumExon = Math.abs(exIntronNum) - 1; //实际数量减去1，方法内用该变量运算
		if (exIntronNum > 0) {
			if (cis5to3 != null)
				loc2ExInStart = Math.abs(location - get(NumExon).getStartCis());//距离本外显子起始 nnnnnnnnC
			else
				loc2ExInStart = Math.abs(location - get(NumExon).getStartAbs());//距离本外显子起始 nnnnnnnnC
		}
		else if(exIntronNum < 0) 
		{   //0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
			if (cis5to3 != null) 
				loc2ExInStart = Math.abs(location - get(NumExon).getEndCis()) -1;// 距前一个外显子 NnnnCnnnn
			else
				loc2ExInStart = Math.abs(location - get(NumExon).getEndAbs()) -1;// 距前一个外显子 NnnnCnnnn
		}
		hashLocExInStart.put(location, loc2ExInStart);
		return loc2ExInStart;
	}

	/**
	 * 坐标到element 终点距离，当重叠时，为0
	 * @param location 坐标
	 */
	public int getCod2ExInEnd(int location) {
		if (hashLocExInEnd == null) {
			hashLocExInEnd = new HashMap<Integer, Integer>();
		}
		else if (hashLocExInEnd.containsKey(location)) {
			return hashLocExInEnd.get(location);
		}
		int loc2ExInEnd = -1000000000;
		int exIntronNum = getNumCodInEle(location);
		int NumExon = Math.abs(exIntronNum) - 1; //实际数量减去1，方法内用该变量运算
		if (exIntronNum > 0) {
			if (cis5to3 != null) {
				loc2ExInEnd = Math.abs(get(NumExon).getEndCis() - location);//距离本外显子终止  Cnnnnnnn
			} else {
				loc2ExInEnd = Math.abs(get(NumExon).getEndAbs() - location);//距离本外显子终止  Cnnnnnnn
			}
		}
		//0-0 0-1        1-0 1-1          2-0 2-1            3-0  3-1   cood     4-0      4-1               5
		else if(exIntronNum < 0) {
			if (cis5to3 != null) {
				 loc2ExInEnd = Math.abs(get(NumExon+1).getStartCis() - location) - 1;// 距后一个外显子 nnCnnnnN
			} else {
				 loc2ExInEnd = Math.abs(get(NumExon+1).getStartAbs() - location) - 1;// 距后一个外显子 nnCnnnnN
			}
		}
		hashLocExInEnd.put(location, loc2ExInEnd);
		return loc2ExInEnd;
	}
	/**
	 * 获得所有element的长度之和
	 */
	public int getListLen() {
		int isoLen = 0;
		for (E exons : this) {
			isoLen = isoLen + exons.Length();
		}
		return isoLen;
	}
	/**
	 * 返回每个ID对应的具体element的编号
	 * key都是小写
	 * @return
	 */
	public HashMap<String,Integer> getMapName2DetailAbsNum() {
		HashMap<String, Integer> hashID2Num = new HashMap<String, Integer>();
		for (int i = 0; i < size(); i++) {
			E lsDetail = get(i);
			ArrayList<String> ss = lsDetail.getName();
			for (String string : ss) {
				hashID2Num.put(string.toLowerCase(), i);
			}
		}
		return hashID2Num;
	}
	/**
	 * 返回每个ID对应的具体element
	 * 输入一个hashmap，在里面填充信息
	 * key都是小写
	 * @return
	 */
	public HashMap<String, E> getMapName2DetailAbs() {
		HashMap<String, E> mapName2DetailAbs = new HashMap<String, E>();
		for (E ele : this) {
			if (ele.getRefID().equals("chr10") && Math.abs(ele.getStartAbs() - 695888) < 50000) {
				System.out.println("stop");
			}
			ArrayList<String> ss = ele.getName();
			for (String string : ss) {
				mapName2DetailAbs.put(string.toLowerCase(), ele);
				mapName2DetailAbs.put(GeneID.removeDot(string.toLowerCase()), ele);
			}
		}
		return mapName2DetailAbs;
	}
	/**
	 * 返回本ListAbs中的所有string名字
	 * 如果两个Item是重叠的，取全部ID
	 * @return
	 */
	public ArrayList<String> getLsNameAll() {
		ArrayList<String> lsLocID = new ArrayList<String>();
		for (E ele : this) {
			lsLocID.addAll(ele.getName());
		}
		return lsLocID;
	}
	/**
	 * 二分法查找location所在的位点,也是static的。已经考虑了在第一个Item之前的情况，还没考虑在最后一个Item后的情况<br>
	 * 返回一个int[3]数组，<br>
	 * 0: 1-基因内 2-基因外<br>
	 * 1：本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因<br>
	 * 2：下个基因的序号 -1表示后面没有基因<br>
	 * 3：该点在外显子中为正数，在内含子中为负数
	 * 不在为0
	 * 为实际数目
	 */
	protected CoordLocationInfo LocPosition( int Coordinate) {
		if (cis5to3 == null) {
			return BinarySearch.LocPositionAbs(this, Coordinate);
		}
		else if (cis5to3) {
			return BinarySearch.LocPositionCis(this, Coordinate);
		}
		else {
			return BinarySearch.LocPositionTran(this, Coordinate);
		}
	}
	
	/**
	 * 该点在外显子中为正数，在内含子中为负数
	 * 不在为0
	 * 为实际数目，从1开始计数
	 * @return
	 */
	public int getNumCodInEle(int location) {
		return LocPosition(location).getElementNumThisAbs();
	}

	/**
	 * TO BE CHECKED
	 * 返回距离loc有num Bp的坐标，在mRNA层面，在loc上游时num 为负数
	 * 在loc下游时num为正数
	 * 如果num Bp外就没有基因了，则返回-1；
	 * @param mRNAnum
	 * NnnnLoc 为-4位，当N与Loc重合时为0
	 * LnnnnN为5位
	 */
	public int getLocDistmRNASite(int location, int mRNAnum) {
		if (getNumCodInEle(location) <= 0) {
			return -1;
		}
		if (mRNAnum < 0) {
			if (Math.abs(mRNAnum) <= getCod2ExInStart(location)) {
				if (isCis5to3()) {
					return location + mRNAnum;
				}
				else
					return  location + Math.abs(mRNAnum);
			}
			else {
				int exonNum = getNumCodInEle(location) - 1;
				int remain = Math.abs(mRNAnum) - getCod2ExInStart(location);
				for (int i = exonNum - 1; i >= 0; i--) {
					E tmpExon = get(i);
					// 一个一个外显子的向前遍历
					if (remain - tmpExon.Length() > 0) {
						remain = remain - tmpExon.Length();
						continue;
					}
					else {
						if (isCis5to3()) {
							return tmpExon.getEndCis() - remain + 1;
						}
						else {
							return tmpExon.getEndCis() + remain - 1;
						}
					}
				}
				return -1;
			}
		}
		else {
			if (mRNAnum <= getCod2ExInEnd(location)) {
				if (isCis5to3()) {
					return location + mRNAnum;
				}
				else {
					return location - mRNAnum;
				}
			} 
			else {
				int exonNum = getNumCodInEle(location) - 1;
				int remain = mRNAnum - getCod2ExInEnd(location);
				for (int i = exonNum + 1; i < size(); i++) {
					E tmpExon = get(i);
					// 一个一个外显子的向前遍历
					if (remain - tmpExon.Length() > 0) {
						remain = remain - tmpExon.Length();
						continue;
					}
					else {
						if (isCis5to3()) {
							return tmpExon.getStartCis() + remain - 1;
						}
						else {
							return tmpExon.getStartCis() - remain + 1;
						}
					}
				}
				return -1;
			}
		}
	}
	/**
	 * 依次比较两个list中的元素是否一致。内部调用每个元素的equals方法
	 * 不比较name，如果需要比较name，那么就用equal
	 * 暂时还没重写equal
	 * 外显子比较如果一模一样则返回true；
	 * @param lsOtherExon
	 * @return
	 */
	public boolean equalsIso(ListAbs<E> lsOther) {
		if (lsOther.size() != size() ) {
			return false;
		}
		for (int i = 0; i < lsOther.size(); i++) {
			E otherT = lsOther.get(i);
			E thisT = get(i);
			if (!otherT.equals(thisT)) {
				return false;
			}
		}
		return true;
	}
	/**
	 * 将list中的元素进行排序，如果反向，那么就从大到小排序
	 * 如果正向，那么就从小到大排序
	 * 内部有flag，排完后就不会再排第二次了
	 */
	public void sort() {
		if (cis5to3 == null) {
			Collections.sort(this, new CompS2MAbs());
		}
		else if (cis5to3) {
			Collections.sort(this, new CompS2M());
		}
		else {
			Collections.sort(this, new CompM2S());
		}
	}
	/**
	 * 已测试，能用
	 */
	@SuppressWarnings("unchecked")
	public ListAbs<E> clone() {
		ListAbs<E> result = null;
		result = (ListAbs<E>) super.clone();
		result.cis5to3 = cis5to3;
		result.hashLocExInEnd = hashLocExInEnd;
		result.hashLocExInStart = hashLocExInStart;
		result.listName = listName;
		result.clear();
		for (E ele : this) {
			result.add((E) ele.clone());
		}
		return result;
	}
	/**
	 * 给定一系列ListElement，以及一个方向。
	 * 将相同方向的ListElement提取出来，然后合并，然后找出这些element的共同边界
	 * @param cis5to3 null,不考虑方向
	 * @param lsIso
	 * @param sepSingle 遇到这种情况怎么分割：<br>
	 * 	 * ---m-m-------------a--a---------b--b------------n-n----<br>
	 *    ---m-m---------------------------------------------n-n----<br>
	 *    true aa 和 bb 分开
	 *    false aa 和 bb合在一起
	 * @return
	 * 返回一个list，按照cis5to3排序，如果cis5to3为true，从小到大排列
	 * 如果cis5to3为false，从大到小排列
	 * 内部的int[] 0: startAbs 1: endAbs
	 */
	public static ArrayList<int[]> getCombSep(Boolean cis5to3, ArrayList<? extends ListAbs<? extends ListDetailAbs>> lsIso, boolean sepSingle) {
		ArrayList<? extends ListDetailAbs> lsAllelement = combListAbs(cis5to3, lsIso);
		ArrayList<int[]> lsSep = null;
		if (sepSingle) {
			lsSep = getLsElementSep(cis5to3, lsAllelement);
		} else {
			lsSep = getLsElementSepComb(cis5to3, lsAllelement);
		}
		return lsSep;
	}
	/**
	 * 
	 * 将一个List中的Iso全部合并起来。
	 * @param cis5to3 null,不考虑方向
	 * @param lsIso
	 * @return
	 */
	private static ArrayList<? extends ListDetailAbs> combListAbs(Boolean cis5to3, ArrayList<? extends ListAbs<? extends ListDetailAbs>> lsIso) {
		ArrayList<ListDetailAbs> lsAll = new ArrayList<ListDetailAbs>();
		//将全部的exon放在一个list里面并且排序
		for (ListAbs<? extends ListDetailAbs> listAbs : lsIso) {
			if (cis5to3 != null && listAbs.isCis5to3() != cis5to3) {
				continue;
			}
			lsAll.addAll(listAbs);
		}
		Collections.sort(lsAll);
		return lsAll;
	}
	/** 将经过排序的exonlist合并，获得几个连续的exon，用于分段
	 * 返回的int[] 0: startAbs    1: endAbs
	 *  
	 *  */
	private static ArrayList<int[]> getLsElementSep(Boolean cis5to3, ArrayList<? extends ListDetailAbs> lsAll) {
		ArrayList<int[]> lsExonBounder = new ArrayList<int[]>();
		int[] exonOld = new int[]{lsAll.get(0).getStartAbs(), lsAll.get(0).getEndAbs()};
		lsExonBounder.add(exonOld);
		for (int i = 1; i < lsAll.size(); i++) {
			int[] exon = new int[]{lsAll.get(i).getStartAbs(), lsAll.get(i).getEndAbs()};
			if (cis5to3 == null || cis5to3) {
				if (exon[0] <= exonOld[1]) {
					if (exon[1] > exonOld[1]) {
						exonOld[1] = exon[1];
					}
				} else {
					exonOld = exon.clone();
					lsExonBounder.add(exonOld);
				}
			} else {
				if (exon[1] >= exonOld[0]) {
					if (exon[0] < exonOld[0]) {
						exonOld[0] = exon[0];
					}
				} else {
					exonOld = exon.clone();
					lsExonBounder.add(exonOld);
				}
			}
		}
		return lsExonBounder;
	}
	
	/** 将经过排序的exonlist合并，获得几个连续的exon，用于分段<br>
	 * 如果有两个exon连续并且单独出现，类似<br>
	 * ---m-m-------------a--a---------b--b------------n-n----<br>
	 * ---m-m---------------------------------------------n-n----<br>
	 * <br>
	 * 那么a-a和b-b放在一起<br>
	 *  */
	private static ArrayList<int[]> getLsElementSepComb(Boolean cis5to3, ArrayList<? extends ListDetailAbs> lsAll) {
		ArrayList<int[]> lsExonBounder = new ArrayList<int[]>();
		int[] exonOld = new int[]{lsAll.get(0).getStartAbs(), lsAll.get(0).getEndAbs()};
		lsExonBounder.add(exonOld);
		//一堆flag标签
		
		// 上一个exon的父类，判断是否为同一个父类基因
		ListAbs lastExonParent = lsAll.get(0).getParent(); 
		
		//上一个exon是否来自于单一父类，就是说没有跟来自另一个父类的exon混合，以下mm和kk是混合的，aa是单独的
		//* -------m-----------m-------------a--a---------b--b------------n-n----<br>
		 //* ---k---------k--------------------------------------n-n----<br>
		boolean lastParentIsSingle = true; 
		
		for (int i = 1; i < lsAll.size(); i++) {
			ListDetailAbs listDetailAbs = lsAll.get(i);
			ListDetailAbs listDetailAbsNext = null;
			if (i < lsAll.size() - 1) {
				listDetailAbsNext = lsAll.get(i+1);
			}
			
			int[] exon = new int[]{listDetailAbs.getStartAbs(), listDetailAbs.getEndAbs()};
			if (cis5to3 == null || cis5to3) {
				if (exon[0] <= exonOld[1]) {
					lastParentIsSingle = false;
					if (exon[1] > exonOld[1]) {
						exonOld[1] = exon[1];
					}
				} else {
					//如果是这种情况：
					//* ---m-m-------------a--a---------b--b------------n-n----<br>
					//* ---m-m---------------------------------------------n-n----<br>
					if (lastParentIsSingle == true && lastExonParent == listDetailAbs.getParent() 
							&&
							(i == lsAll.size() - 1 || listDetailAbsNext.getStartAbs() >= listDetailAbs.getEndAbs())
					) {
						exonOld[1] = exon[1];
					} else {
						exonOld = exon.clone();
						lsExonBounder.add(exonOld);
						lastParentIsSingle = true;
						lastExonParent = listDetailAbs.getParent();
					}
				}
			} else {
				if (exon[1] >= exonOld[0]) {
					lastParentIsSingle = false;
					if (exon[0] < exonOld[0]) {
						exonOld[0] = exon[0];
					}
				} else {
					if (lastParentIsSingle == true && lastExonParent == listDetailAbs.getParent() 
							&&
							(i == lsAll.size() - 1 || listDetailAbsNext.getStartCis() <= listDetailAbs.getEndCis())
					) {
						exonOld[0] = exon[0];
					} else {
						exonOld = exon.clone();
						lsExonBounder.add(exonOld);
						lastParentIsSingle = true;
						lastExonParent = lsAll.get(i).getParent();
					}
				}
			}
		}
		return lsExonBounder;
	}
	
}
/**
 * 内建的二分法查找类，专门用于ListAbs查找
 * @author zong0jie
 *
 */
class BinarySearch {
	/**
	 * 二分法查找location所在的位点,也是static的。已经考虑了在第一个Item之前的情况，还没考虑在最后一个Item后的情况<br>
	 * 返回一个int[3]数组，<br>
	 * 0: 1-基因内 2-基因外<br>
	 * 1：本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因<br>
	 * 2：下个基因的序号 -1表示后面没有基因
	 * 3：单独的一个标签，该点在外显子中为正数，在内含子中为负数
	 * 不在为0
	 * 为实际数目
	 */
	protected static CoordLocationInfo LocPositionCis(ArrayList<? extends ListDetailAbs> lsElement, int Coordinate) {
		if (lsElement == null) {
			return null;
		}
		CoordLocationInfo coordLocationInfo = new CoordLocationInfo(lsElement.size());
		int endnum = 0;
		endnum = lsElement.size() - 1;
		int beginnum = 0;
		int number = 0;
		// 在第一个Item之前
		if (Coordinate < lsElement.get(beginnum).getStartCis()){
			coordLocationInfo.setElementInsideOutSideNum(0);
			return coordLocationInfo;
		}
		// 在最后一个Item之后
		else if (Coordinate >= lsElement.get(endnum).getStartCis()) {
			if (Coordinate > lsElement.get(endnum).getEndCis()) {
				coordLocationInfo.setElementInsideOutSideNum(-lsElement.size());
			}
			else {
				coordLocationInfo.setElementInsideOutSideNum(lsElement.size());
			}
			return coordLocationInfo;
		}
		do {
			number = (beginnum + endnum + 1) / 2;// 3/2=1,5/2=2
			if (Coordinate == lsElement.get(number).getStartCis()) {
				beginnum = number;
				endnum = number + 1;
				break;
			}
			else if (Coordinate < lsElement.get(number).getStartCis()
					&& number != 0) {
				endnum = number;
			} else {
				beginnum = number;
			}
		} while ((endnum - beginnum) > 1);
		if (Coordinate <= lsElement.get(beginnum).getEndCis())// 不知道会不会出现PeakNumber比biginnum小的情况
		{ // location在基因内部
			coordLocationInfo.setElementInsideOutSideNum(beginnum + 1);
			return coordLocationInfo;
		}
		else if (Coordinate >= lsElement.get(endnum).getStartCis())// 不知道会不会出现PeakNumber比biginnum小的情况
		{ // location在基因内部
			coordLocationInfo.setElementInsideOutSideNum(endnum + 1);
			return coordLocationInfo;
		}
		// location在基因外部
		coordLocationInfo.setElementInsideOutSideNum(-beginnum - 1);
		return coordLocationInfo;
	}

	/**
	 * 二分法查找location所在的位点,也是static的。已经考虑了在第一个Item之前的情况，还没考虑在最后一个Item后的情况<br>
	 * 返回一个int[3]数组，<br>
	 * 0: 1-基因内 2-基因外<br>
	 * 1：本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因<br>
	 * 2：下个基因的序号 -1表示后面没有基因
	 * 3：单独的一个标签，该点在外显子中为正数，在内含子中为负数
	 * 不在为0
	 * 为实际数目
	 */
	protected static CoordLocationInfo LocPositionTran(ArrayList<? extends ListDetailAbs> lsElement, int Coordinate) {
		if (lsElement == null) {
			return null;
		}
		CoordLocationInfo coordLocationInfo = new CoordLocationInfo(lsElement.size());
		int endnum = 0;
		endnum = lsElement.size() - 1;
		int beginnum = 0;
		int number = 0;
		// 在第一个Item之前
		if (Coordinate > lsElement.get(beginnum).getStartCis()){
			coordLocationInfo.setElementInsideOutSideNum(0);
			return coordLocationInfo;
		}
		// 在最后一个Item之后
		else if (Coordinate <= lsElement.get(endnum).getStartCis()) {
			if (Coordinate < lsElement.get(endnum).getEndCis()) {
				coordLocationInfo.setElementInsideOutSideNum(-lsElement.size());
			}
			else {
				coordLocationInfo.setElementInsideOutSideNum(lsElement.size());
			}
			return coordLocationInfo;
		}
		do {
			number = (beginnum + endnum + 1) / 2;// 3/2=1,5/2=2
			if (Coordinate == lsElement.get(number).getStartCis()) {
				beginnum = number;
				endnum = number + 1;
				break;
			}
			else if (Coordinate > lsElement.get(number).getStartCis()
					&& number != 0) {
				endnum = number;
			} else {
				beginnum = number;
			}
		} while ((endnum - beginnum) > 1);
		if (Coordinate >= lsElement.get(beginnum).getEndCis()) { // location在基因内部
			coordLocationInfo.setElementInsideOutSideNum(beginnum + 1);
			return coordLocationInfo;
		}
		else if (Coordinate <= lsElement.get(endnum).getStartCis()) 
		{// location在基因内部
			coordLocationInfo.setElementInsideOutSideNum(endnum + 1);
			return coordLocationInfo;
		}
		// location在基因外部
		coordLocationInfo.setElementInsideOutSideNum(-beginnum - 1);
		return coordLocationInfo;
	}
	/**
	 * 二分法查找location所在的位点,也是static的。已经考虑了在第一个Item之前的情况，还没考虑在最后一个Item后的情况<br>
	 * 返回一个int[3]数组，<br>
	 * 0: 1-基因内 2-基因外<br>
	 * 1：本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因<br>
	 * 2：下个基因的序号 -1表示后面没有基因
	 * 3：单独的一个标签，该点在外显子中为正数，在内含子中为负数
	 * 不在为0
	 * 为实际数目
	 */
	protected static CoordLocationInfo LocPositionAbs(ArrayList<? extends ListDetailAbs> lsElement, int Coordinate) {
		if (lsElement == null) {
			return null;
		}
		CoordLocationInfo coordLocationInfo = new CoordLocationInfo(lsElement.size());
		int endnum = 0;
		endnum = lsElement.size() - 1;
		int beginnum = 0;
		int number = 0;
		// 在第一个Item之前
		if (Coordinate < lsElement.get(beginnum).getStartAbs()){
			coordLocationInfo.setElementInsideOutSideNum(0);
			return coordLocationInfo;
		}
		// 在最后一个Item之后
		else if (Coordinate >= lsElement.get(endnum).getStartAbs()) {
			if (Coordinate > lsElement.get(endnum).getEndAbs()) {
				coordLocationInfo.setElementInsideOutSideNum(-lsElement.size());
			}
			else {
				coordLocationInfo.setElementInsideOutSideNum(lsElement.size());
			}
			return coordLocationInfo;
		}
		do {
			number = (beginnum + endnum + 1) / 2;// 3/2=1,5/2=2
			if (Coordinate == lsElement.get(number).getStartAbs()) {
				beginnum = number;
				endnum = number + 1;
				break;
			}
			else if (Coordinate < lsElement.get(number).getStartAbs()
					&& number != 0) {
				endnum = number;
			} else {
				beginnum = number;
			}
		} while ((endnum - beginnum) > 1);
		if (Coordinate <= lsElement.get(beginnum).getEndAbs()) {
			coordLocationInfo.setElementInsideOutSideNum(beginnum + 1);
			return coordLocationInfo;
		}
		coordLocationInfo.setElementInsideOutSideNum(-beginnum-1);
		return coordLocationInfo;
	}
	
}

/**
 * 从小到大排序
 * @author zong0jie
 */
class CompS2M implements Comparator<ListDetailAbs> {
	@Override
	public int compare(ListDetailAbs o1, ListDetailAbs o2) {
		Integer o1start = o1.getStartCis();
		Integer o2start = o2.getStartCis();
		int comp = o1start.compareTo(o2start);
		if (comp == 0) {
			Integer o1end = o1.getEndCis();
			Integer o2end = o2.getEndCis();
			return o1end.compareTo(o2end);
		}
		return comp;
	}
}

/**
 * 从小到大排序，用绝对坐标值排序
 * @author zong0jie
 */
class CompS2MAbs implements Comparator<ListDetailAbs> {
	@Override
	public int compare(ListDetailAbs o1, ListDetailAbs o2) {
		Integer o1start = o1.getStartAbs();
		Integer o2start = o2.getStartAbs();
		int comp = o1start.compareTo(o2start);
		if (comp == 0) {
			Integer o1end = o1.getEndAbs();
			Integer o2end = o2.getEndAbs();
			return o1end.compareTo(o2end);
		}
		return comp;
	}
}

/**
 * 从大到小排序
 * @author zong0jie
 */
class CompM2S implements Comparator<ListDetailAbs> {
	@Override
	public int compare(ListDetailAbs o1, ListDetailAbs o2) {
		Integer o1start = o1.getStartCis();
		Integer o2start = o2.getStartCis();
		int comp = o1start.compareTo(o2start);
		if (comp == 0) {
			Integer o1end = o1.getEndCis();
			Integer o2end = o2.getEndCis();
			return -o1end.compareTo(o2end);
		}
		return -comp;
	}
}
/**
 * 前提，第一个element的起点就是list的起点，最后一个element的终点就是list的终点
 * 否则就要<b>重写getElementNumThisAbs() 方法</b>
 * 
 * 二分法查找location所在的位点所保存的信息
 * 返回一个int[3]数组，<br>
 * 0: 1-基因内 2-基因外<br>
 * 1：本基因序号（定位在基因内） / 上个基因的序号(定位在基因外) -1表示前面没有基因<br>
 * 2：下个基因的序号 -1表示后面没有基因
 * 3：单独的一个标签，该点在外显子中为正数，在内含子中为负数
 * 不在为0
 * 从0开始的数目，可以直接用get(i)提取
 */

class CoordLocationInfo {
	/**待查找的list的元素个数 */
	int listSize;
	/** 表示该点在第几个元素中<br>
	 * 正数表示在第几个元素中，譬如在第几个exon中或第几个基因中，实际数目<br>
	 * 负数表示在第几个intron中或第几个间隔中，实际数目。
	 * 如果在list最前面，则为0。如果在list最后面，则为负数的list.size()
	 */
	int elementInsideOutSideNumAbs = 0;
	
	public CoordLocationInfo(int listSize) {
		this.listSize = listSize;
	}
	/** 表示该点在第几个元素中，<b>实际数目</b><br>
	 * 正数表示在第几个元素中，譬如在第几个exon中或第几个基因中，实际数目<br>
	 * 负数表示在第几个intron中或第几个间隔中。
	 * 如果在list最前面，则为0。如果在list最后面，则为负数的list.size()
	 */
	public void setElementInsideOutSideNum(int elementInsideOutSideNumAbs) {
		this.elementInsideOutSideNumAbs = elementInsideOutSideNumAbs;
	}
	
	public boolean isInsideElement() {
		if (elementInsideOutSideNumAbs > 0) {
			return true;
		}
		return false;
	}
	/**
	 * 返回该点所在的元素，一直返回正数。如果在list外，返回-1<br>
	 * 计数从0开始<br>
	 * <b>-1表示前面没有基因</b>
	 * @return
	 */
	public int getElementNumLastElementFrom0() {
		if (elementInsideOutSideNumAbs > 0) {
			return elementInsideOutSideNumAbs - 2;
		}
		else if (elementInsideOutSideNumAbs < 0) {
			return Math.abs(elementInsideOutSideNumAbs) - 1;
		}
		else {
			return -1;
		}
	}
	/**
	 * 返回该点所在的Element，一直返回正数。如果在element之外，返回-1
	 * 计数从0开始
	 * @return
	 */
	public int getElementNumThisElementFrom0() {
		if (elementInsideOutSideNumAbs > 0) {
			return elementInsideOutSideNumAbs - 1;
		}
		else {
			return -1;
		}
	}
	/**
	 * 返回该点所在的元素，一直返回正数。如果在list外，返回-1<br>
	 * 计数从0开始<br>
	 * <b>-1表示后面没有基因</b>
	 * @return
	 */
	public int getElementNumNextElementFrom0() {
		if (elementInsideOutSideNumAbs >= 0 && elementInsideOutSideNumAbs < listSize) {
			return elementInsideOutSideNumAbs;
		}
		else if (elementInsideOutSideNumAbs < 0 && Math.abs(elementInsideOutSideNumAbs) < listSize) {
			return Math.abs(elementInsideOutSideNumAbs);
		}
		else {
			return -1;
		}
	}
	/**
	 * 前提，第一个element的起点就是list的起点，最后一个element的终点就是list的终点<br>
	 * 返回该点所在的元素，从1开始，<br>
	 * 正数表示在第几个元素中，譬如在第几个exon中或第几个基因中，实际数目<br>
	 * 负数表示在第几个intron中或第几个间隔中。<br>
	 * 如果<b>在list最前面或最后面，则为0</b>。
	 */
	public int getElementNumThisAbs() {
		if (elementInsideOutSideNumAbs == -listSize) {
			return 0;
		}
		return elementInsideOutSideNumAbs;
	}
}

