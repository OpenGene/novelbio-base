package com.novelbio.base.dataStructure.listOperate;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.GffGeneIsoInfo;
import com.novelbio.analysis.seq.genome.mappingOperate.Alignment;
import com.novelbio.database.domain.geneanno.SepSign;

/**
 * compare的比较取决于父节点的方向，如果父节点的方向为null，则按照绝对值排序，如果是cis，那么就按cis的排序，如果为trans就按照trans的方式排序
 * 本类重写了equal代码，用于比较两个loc是否一致
 * 重写了hashcode 仅比较ChrID + "//" + numberstart + "//" + numberstart;
 * 存储Gff文件中每个条目的具体信息，直接用于GffPeak文件
 * 包括<br>
 * 条目名 locString<br>
 * 条目起点 numberstart<br>
 * 条目终点 numberend<br>
 * 条目所在染色体编号 ChrID<br>
 * 条目方向 cis5to3
 * @author zong0jie
 *
 */
public class ListDetailAbs implements Alignment, Cloneable, Comparable<ListDetailAbs> {
	/** 父树 */
	protected ListAbs<? extends ListDetailAbs> listAbs;
	
	/** 根据cis在起点的上游多少bp，在此范围内则认为在tss区域  */
	protected int upTss = 0;
	/** 根据cis在起点的下游多少bp，在此范围内则认为在tss区域 */
	protected int downTss = 0;
	/** 根据cis在终点的上游多少bp，在此范围内则认为在tes区域 */
	protected int upGeneEnd3UTR = 0;
	/** 根据cis在终点的下游多少bp，在此范围内则认为在tes区域 */
	protected int downGeneEnd3UTR = 0;
	/**
	 * LOCID，<br>
	 * 水稻：LOC_Os01g01110<br>
	 * 拟南芥：AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG：107_chr1_CpG_36568608: 27 其中107是CpG gff文件中的索引,36568608是该CpG在染色体上的起点
	 * peak: peak起点_peak终点
	 */
	protected ArrayList<String> lsItemName = new ArrayList<String>(); //loc name
	/**  染色体编号，都小写 */
	protected String parentName="";
	/** 转录方向，假设同一基因不管多少转录本都同一转录方向 */
	protected Boolean cis5to3 = null;
	/** 本区域内有多少条reads */
	int readsInElementNumber = 0;
	
	/** 本条目起点,起点位置总是小于终点，无视基因方向 */
	protected int numberstart = ListCodAbs.LOC_ORIGINAL; // loc start number 
	/** 本条目终点，终点位置总是大于起点，无视基因方向 */
	protected int numberend = ListCodAbs.LOC_ORIGINAL; //loc end number
	/** 本基因起点到上一个基因边界的距离 */
	protected int tss2UpGene = ListCodAbs.LOC_ORIGINAL;
	/** 本基因终点到下一个基因边界的距离 */
	protected int tes2DownGene = ListCodAbs.LOC_ORIGINAL;
	/** 该条目在List-GffDetail中的具体位置 */
	protected int itemNum = ListCodAbs.LOC_ORIGINAL;
	
	public ListDetailAbs() {}
	/**
	 * 没有就设定为""或null
	 * @param chrID 染色体编号，自动变成小写
	 * @param locString 	 * LOCID，<br>
	 * 水稻：LOC_Os01g01110<br>
	 * 拟南芥：AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG：107_chr1_CpG_36568608: 27 其中107是CpG gff文件中的索引,36568608是该CpG在染色体上的起点
	 * peak: peak起点_peak终点
	 * @param cis5to3 不确定就输入null
	 */
	public ListDetailAbs(String chrID, String ItemName, Boolean cis5to3) {
		if (chrID != null) {
			this.parentName = chrID;
		}
		this.lsItemName.add(ItemName);
		this.cis5to3 = cis5to3;
	}
	/**
	 * 没有就设定为""或null
	 * @param listAbs 父节点的信息
	 * @param ItemName 本节点的名字
	 * @param cis5to3 正反向 不确定就输入null
	 */
	public ListDetailAbs(ListAbs<? extends ListDetailAbs> listAbs, String ItemName, Boolean cis5to3) {
		this.listAbs = listAbs;
		this.parentName = listAbs.getName();
		this.lsItemName.add(ItemName);
		this.cis5to3 = cis5to3;
	}
	public void setParentListAbs(ListAbs<? extends ListDetailAbs> listAbs) {
		this.listAbs = listAbs;
	}
	public ListAbs<? extends ListDetailAbs> getParent() {
		return listAbs;
	}
	/**
	 * 划定Tss范围上游为负数，下游为正数
	 * @param upTss
	 * @param downTss
	 */
	public void setTssRegion(int upTss, int downTss) {
		this.upTss = upTss;
		this.downTss = downTss;
	}
	/**
	 * 划定Tss范围上游为负数，下游为正数
	 * @param upTss
	 * @param downTss
	 */
	public void setTssRegion(int[] Tss) {
		if (Tss != null)
			setTssRegion(Tss[0], Tss[1]);
	}
	
	/**
	 * 划定Tes范围上游为负数，下游为正数
	 * @param upTes
	 * @param downTes
	 */
	public void setTesRegion(int upTes, int downTes) {
		this.upGeneEnd3UTR = upTes;
		this.downGeneEnd3UTR = downTes;
	}
	/**
	 * 划定Tss范围上游为负数，下游为正数
	 * @param upTss
	 * @param downTss
	 */
	public void setTesRegion(int[] Tes) {
		if (Tes != null)
			setTesRegion(Tes[0], Tes[1]);
	}
	/**
	 * 0：uptss
	 * 1：downtss
	 * @return
	 */
	public int[] getTssRegion() {
		return new int[]{upTss, downTss};
	}
	/**
	 * 0：uptes
	 * 1：downtes
	 * @return
	 */
	public int[] getTesRegion() {
		return new int[]{upGeneEnd3UTR, downGeneEnd3UTR};
	}
	private static Logger logger = Logger.getLogger(ListDetailAbs.class);
	
	/** 计数加一 */
	public void addReadsInElementNum() {
		readsInElementNumber++;
	}
	/**
	 * 本区域内出现多少的元素，必须前面调用addNumber添加
	 * @return
	 */
	public int getReadsInElementNum() {
		return readsInElementNumber;
	}
	/**
	 * 从0开始，位于list的第几个位置
	 * @param itemNum
	 */
	public void setItemNum(int itemNum) {
		this.itemNum = itemNum;
	}
	/** 
	 * <b>从0开始计算</b>
	 * 该条目在List-GffDetail中的具体位置 */
	public int getItemNum() {
		return getParent().indexOf(this);
	}
    /**
     * Item的名字，返回第一个
 	 * LOCID，<br>
	 * 水稻：LOC_Os01g01110<br>
	 * 拟南芥：AT1G01110<br>
	 * UCSC:XM_0101010<br>
	 * CpG：107_chr1_CpG_36568608: 27 其中107是CpG gff文件中的索引,36568608是该CpG在染色体上的起点
	 * peak: peak起点_peak终点
     */
	public String getNameSingle() {
		if (lsItemName.size() == 0) {
			return null;
		}
		return this.lsItemName.get(0);
	}
	/** 全体item的名字 */
	public ArrayList<String > getName() {
		return this.lsItemName;
	}
    /**
 	 * LOCID，<br>
	 * 水稻：LOC_Os01g01110<br>
	 * 拟南芥：AT1G01110<br>
	 * UCSC:XM_0101010/XM_032020<br>
	 * CpG：107_chr1_CpG_36568608: 27 其中107是CpG gff文件中的索引,36568608是该CpG在染色体上的起点
	 * peak: peak起点_peak终点
     */
	public void addItemName(String itemName) {
		this.lsItemName.add(itemName);
	}
	/**
	 * 染色体编号等信息，父ID
	 * @param parentName
	 */
	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	/** 本基因起点到上一个基因边界的距离  */
	public void setTss2UpGene(int tss2UpGene) {
		this.tss2UpGene = tss2UpGene;
	}
	/** 本基因终点到下一个基因边界的距离 */
	public void setTes2DownGene(int tes2DownGene) {
		this.tes2DownGene = tes2DownGene;
	}
	/** 本基因终点到下一个基因边界的距离 */
	public int getTes2DownGene() {
		return tes2DownGene;
	}
	/** 本基因起点到上一个基因边界的距离 */
	public int getTss2UpGene() {
		return tss2UpGene;
	}
	/**
	 * @GffHashGene
	 * 本基因终点，终点位置总是大于起点，无视基因方向
	 * @GffHashItem
	 * 条目终点，终点位置总是大于起点，无视条目方向
	 */
	public int getEndAbs() {
		return numberend;
	}
	/**
	 * @GffHashGene
	 * 本基因起点,起点位置总是小于终点，无视基因方向
	 * @GffHashItem
	 * 条目起点,起点位置总是小于终点，无视条目方向
	 */
	public int getStartAbs() {
		return numberstart;
	}
	/**
	 * @param numberend 条目终点,终点位置总是大于起点，无视基因方向
	 */
	public void setEndAbs(int numberend) {
		this.numberend = numberend;
	}
	/**
	 * @param numberstart 条目起点,起点位置总是小于终点，无视条目方向
	 */
	public void setStartAbs(int numberstart) {
		this.numberstart = numberstart;
	}
	/**
	 * @param numberend 条目终点,根据基因方向确定,从1开始记数
	 */
	public void setEndCis(int numberend) {
		if (isCis5to3() == null || isCis5to3()) {
			this.numberend = numberend;
		}
		else {
			this.numberstart = numberend;
		}
	}
	/**
	 * @param numberstart 条目起点,根据基因方向确定,从1开始记数
	 */
	public void setStartCis(int numberstart) {
		if (isCis5to3() == null || isCis5to3()) {
			this.numberstart = numberstart;
		}
		else {
			this.numberend = numberstart;
		}
	}
	/**
	 * 坐标是否在基因的内部，包括Tss和GeneEnd的拓展区域
	 */
	public boolean isCodInGeneExtend(int coord) {
		return isCodInGene(coord) || isCodInPromoter(coord) || isCodInGenEnd(coord);
	}
	
	/**
	 * 是否在所谓的Tss内,既可以在内也可以在
	 * 所以如果需要只在基因外的tss，需要同时加上isCodInside==false判断
	 * @return
	 */
	public boolean isCodInPromoter(int coord) {
		if (getCod2Start(coord) == null) {
			return false;
		}
		int cod2start = getCod2Start(coord);
		if (cod2start >= upTss && cod2start <= downTss) {
			return true;
		}
		return false;
	}
	
	/**
	 * 是否在所谓的GeneEnd内,既可以在内也可以在外
	 * 所以如果需要只在基因外的geneEnd，需要同时加上isCodInside==false判断
	 * 也就是尾部点，左右扩展geneEnd3UTR长度的bp
	 * @return
	 */
	public boolean isCodInGenEnd(int coord) {
		if (getCod2End(coord) == null) {
			return false;
		}
		int cod2end = getCod2End(coord);
		if (cod2end >= upGeneEnd3UTR && cod2end <= downGeneEnd3UTR ) {
			return true;
		}
		return false;
	}
	/**
	 * 是否在基因内，不拓展
	 * @return
	 */
	public boolean isCodInGene(int coord) {
		if (coord >= numberstart && coord <= numberend) {
			return true;
		}
		return false;
	}
	/**
	 * 所属listAbs编号，都小写
	 */
	public String getRefID() {
		return this.parentName;
	}

	/**
	 * 转录方向，假设同一基因不管多少转录本都同一转录方向
	 * 一个转录本里面既有正向也有反向，选择方向最多的那个
	 */
	public Boolean isCis5to3() {
		return this.cis5to3;
	}
	public void setCis5to3(Boolean cis5to3) {
		this.cis5to3 = cis5to3;
	}
	/**
	 * 获得坐标到该ItemEnd的距离
	 * 用之前先设定coord
	 * 考虑item的正反
	 * 坐标到条目终点的位置，考虑正反向<br/>
	 * 将该基因按照 >--------5start>--------->3end------->方向走
	 * 如果坐标在end的5方向，则为负数
	 * 如果坐标在end的3方向，则为正数
	 * @return
	 */
	public Integer getCod2End(int coord) {
		if (cis5to3 == null) {
			logger.error("不能确定该Item的方向");
			return null;
		}
		if (cis5to3) {
			return coord -numberend;
		}
		else {
			return numberstart- coord;
		}
	}
	/**
	 * 获得坐标到该ItemStart的距离,如果coord小于0说明有问题，则返回null
	 * 用之前先设定coord
	 * 考虑item的正反
	 * 坐标到条目终点的位置，考虑正反向<br/>
	 * 将该基因按照 >--------5start>--------->3end------->方向走
	 * 如果坐标在start的5方向，则为负数
	 * 如果坐标在start的3方向，则为正数
	 * @return
	 */
	public Integer getCod2Start(int coord) {
		if (cis5to3 == null) {
			logger.error("不能确定该Item的方向");
			return null;
		}
		if (cis5to3) {
			return coord -numberstart;
		}
		else {
			return numberend - coord;
		}
	}
	/** 坐标是否在基因内
	 */
	public boolean isCodInSide(int coord) {
		if (coord >= numberstart && coord <=  numberend) {
			return true;
		}
		return false;
	}
	
/////////////////////////////  重写equals等  ////////////////////////////////////
	/**
	 * 只比较numberstart、numberend、ChrID、cis5to3
	 * 不比较coord
	 * 	@Override
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		
		ListDetailAbs otherObj = (ListDetailAbs)obj;
		
		return
		numberend == otherObj.numberend && 
		numberstart == otherObj.numberstart &&
		parentName.equals(otherObj.parentName) &&
//		getItemNum() == otherObj.getItemNum() &&
		cis5to3 == otherObj.cis5to3;
	}
	/** 重写hashcode */
	public int hashCode(){
		String hash = "";
		hash = parentName + SepSign.SEP_ID + numberstart + SepSign.SEP_ID + numberstart;
		return hash.hashCode();
	}
	/** 没有方向则返回startAbs */
	public int getStartCis() {
		if (isCis5to3() == null || isCis5to3()) {
			return numberstart;
		}
		return numberend;
	}
	/** 没有方向则返回endAbs */
	public int getEndCis() {
		if (isCis5to3() == null || isCis5to3()) {
			return numberend;
		}
		return numberstart;
	}
	
	public int Length() {
		return Math.abs(numberend-numberstart) + 1;
	}
	
	public ListDetailAbs clone() {
		ListDetailAbs result = null;
		try {
			result = (ListDetailAbs) super.clone();
			result.cis5to3 = cis5to3;
			result.downGeneEnd3UTR = downGeneEnd3UTR;
			result.downTss = downTss;
			result.lsItemName = (ArrayList<String>) lsItemName.clone();
//			result.itemNum = itemNum;
			result.readsInElementNumber = readsInElementNumber;
			result.numberend = numberend;
			result.numberstart = numberstart;
			result.parentName = parentName;
			result.tes2DownGene = tes2DownGene;
			result.tss2UpGene = tss2UpGene;
			result.upGeneEnd3UTR = upGeneEnd3UTR;
			result.upTss = upTss;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return result;
	}
	@Override
	public int compareTo(ListDetailAbs o) {
		Integer o1startCis = getStartCis(); Integer o1endCis = getEndCis();
		Integer o2startCis = o.getStartCis(); Integer o2endCis = o.getEndCis();
		
		Integer o1startAbs = getStartAbs(); Integer o1endAbs = getEndAbs();
		Integer o2startAbs = o.getStartAbs(); Integer o2endAbs = o.getEndAbs();
		
		if (listAbs == null || listAbs.isCis5to3() == null) {
			int result = o1startAbs.compareTo(o2startAbs);
			if (result == 0) {
				return o1endAbs.compareTo(o2endAbs);
			}
			return result;
		}
		
		else if (listAbs.isCis5to3()) {
			int result = o1startCis.compareTo(o2startCis);
			if (result == 0) {
				return o1endCis.compareTo(o2endCis);
			}
			return result;
		}
		else {
				int result = - o1startCis.compareTo(o2startCis);
				if (result == 0) {
					return - o1endCis.compareTo(o2endCis);
				}
				return result;
			}
	}
	
}
