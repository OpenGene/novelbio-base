package com.novelbio.base.dataStructure.listOperate;

import java.util.ArrayList;

import org.apache.ibatis.migration.commands.NewCommand;
import org.apache.log4j.Logger;

/**
 * 需要检查
 * 多条listabs合并在一起
 * 其内部的元素即为
 * @author zong0jie
 *
 */
public abstract class ListComb<T extends ListDetailAbs, M extends ListDetailComb<T>, E extends ListAbs<T>> 
extends ListAbs<M>
{
	private static final long serialVersionUID = -5877021643535236697L;
	private static Logger logger = Logger.getLogger(ListComb.class);
	/** 全体待比较的listabs信息  */
	ArrayList<E> lsAllListAbs = new ArrayList<E>();
	E  lsAllID = null;
	/**
	 * 保存所有exon合并后的边界
	 */
	ArrayList<int[]> lsExonBounder = new ArrayList<int[]>();
	public void addListAbs(E lsListAbs) {
		if (isCis5to3() == null) {
			setCis5to3(lsListAbs.isCis5to3());
		}
		else if (lsListAbs.isCis5to3() != null && isCis5to3() != lsListAbs.isCis5to3()) {
			logger.error("两个方向不同的list不能进行比较");
		}
		lsAllListAbs.add(lsListAbs);
	}

	/** 将输入的多条ListAbs整理成想要的格式，并且按照element进行分段 */
	boolean copelist = false;
	/**
	 * 将输入的多条ListAbs整理成想要的格式，并且按照element进行分段
	 */
	private void copeList()
	{
		if (copelist) {
			return;
		}
		lsAllID = (E) lsAllListAbs.get(0).clone();
		for (int i = 1; i < lsAllListAbs.size(); i++) {
			lsAllID.addAll(lsAllListAbs.get(i));
		}
		lsAllID.sort();
		combExon();
		setExonCluster();
		copelist = true;
	}
	
	/**
	 * 待检查
	 * 将经过排序的exonlist合并，获得几个连续的exon，用于分段
	 */
	private void combExon()
	{
		lsExonBounder.clear();
		T exonOld =  lsAllID.get(0);
		int[] exonBoundOld = new int[]{exonOld.getStartCis(), exonOld.getEndCis()};
		boolean allFinal = false;//最后一个exon是否需要添加入list中
		lsExonBounder.add(exonBoundOld);
		for (int i = 1; i < lsAllID.size(); i++) {
			T ele = lsAllID.get(i);
			int[] exonBound = new int[]{ele.getStartCis(), ele.getEndCis()};
			if (cis5to3 )
			{
				if (exonBound[0] <= exonBoundOld[1]) {
					if (exonBound[1] > exonBoundOld[1]) {
						exonBoundOld[1] = exonBound[1];
					}
				}
				else {
					exonBoundOld = exonBound;
					lsExonBounder.add(exonBoundOld);
				}
			}
			else {
				if (exonBound[0] >= exonBoundOld[1]) {
					if (exonBound[1] < exonBoundOld[1]) {
						exonBoundOld[1] = exonBound[1];
					}
				}
				else {
					exonBoundOld = exonBound;
					lsExonBounder.add(exonBoundOld);
				}
			}
		}
	}
	
	/**
	 * 这里可以优化
	 * 待检查
	 * 按照分组好的边界exon，将每个转录本进行划分
	 */
	private void setExonCluster()
	{
		for (int[] exonBound : lsExonBounder) {
			M elementComb = createListDetailComb();//new ListDetailComb<M>(); //ExonCluster(gffDetailGene.getParentName(), exonBound[0], exonBound[1]);
			for (int m = 0; m < lsAllListAbs.size(); m ++) {
			E lsAbs = lsAllListAbs.get(m);
				if (lsAbs.isCis5to3() != isCis5to3()) {
					logger.error("方向不一致，不能比较");
				}
				ArrayList<T> lsExonClusterTmp = new ArrayList<T>();
				//从1开始计数
				int beforeExonNum = 0;//如果本isoform正好没有落在bounder组中的exon，那么就要记录该isoform的前后两个exon的位置，用于查找跨过和没有跨过的exon
				boolean junc = false;//如果本isoform正好没有落在bounder组中的exon，那么就需要记录跳过的exon的位置，就将这个flag设置为true
				for (int i = 0; i < lsAbs.size(); i++) {
					T ele = lsAbs.get(i);
					if (isCis5to3()) {
						if (ele.getEndCis() < exonBound[0]) {
							junc = true;
							beforeExonNum = i + 1;
							continue;
						}
						else if (ele.getStartCis() >= exonBound[0] && ele.getEndCis() <= exonBound[1]) {
							lsExonClusterTmp.add(ele);
							junc = false;
						}
						else if (ele.getStartCis() > exonBound[1]) {
							//如果起点大于本边界，说明过去了，那么看lsExonClusterTmp有东西没，没东西表示跳过，有东西表示没有跳过
							if (lsExonClusterTmp.size() > 0)
								junc = false;
							else
								junc = true;
							break;
						}
					}
					else {
						if (ele.getEndCis() > exonBound[0]) {
							junc = true;
							beforeExonNum = i + 1;
							continue;
						}
						else if (ele.getStartCis() <= exonBound[0] && ele.getEndCis() >= exonBound[1]) {
							lsExonClusterTmp.add(ele);
							junc = false;
						}
						else if (ele.getStartCis() < exonBound[1]) {
							//如果起点大于本边界，说明过去了，那么看lsExonClusterTmp有东西没，没东西表示跳过，有东西表示没有跳过
							if (lsExonClusterTmp.size() > 0)
								junc = false;
							else
								junc = true;
							break;
						}
					}
				}
				if (lsExonClusterTmp.size() > 0) {
					elementComb.addLsElement(lsAbs.getName(), lsExonClusterTmp, beforeExonNum+1, beforeExonNum + lsExonClusterTmp.size() );
				}
				else if (junc && beforeExonNum < lsAbs.size()) {
					elementComb.addLsElement(lsAbs.getName(), lsExonClusterTmp, beforeExonNum, -beforeExonNum );
				}
			}
			add(elementComb);
		}
	}
	
	/**
	 * 返回有差异的exon系列List
	 * @return
	 */
	public ArrayList<M> getDifExonCluster() {
		copeList();
		ArrayList<M> lsDifExon = new ArrayList<M>();
		for (M elementComb : this) {
			if (!elementComb.isSameEle()) {
				lsDifExon.add(elementComb);
			}
		}
		return lsDifExon;
	}
	/**
	 * 新建一个M，new一个就行了
	 * @return
	 */
	abstract M createListDetailComb();
}
