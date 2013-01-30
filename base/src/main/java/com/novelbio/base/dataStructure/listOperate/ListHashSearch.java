package com.novelbio.base.dataStructure.listOperate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.log4j.Logger;

import com.novelbio.analysis.seq.genome.gffOperate.GffDetailGene;
import com.novelbio.analysis.seq.genome.gffOperate.ListDetailBin;
import com.novelbio.analysis.seq.genome.gffOperate.ListGff;
import com.novelbio.database.domain.geneanno.SepSign;


/**
 * 获得Gff的项目信息<br/>
 * 具体的GffHash需要实现ReadGffarray并通过该方法填满三个表
 * @Chrhash hash（ChrID）--ChrList--GeneInforList(GffDetail类)
 * @locHashtable hash（LOCID）--GeneInforlist
 * @LOCIDList 顺序存储每个基因号或条目号
 */
public abstract class ListHashSearch < T extends ListDetailAbs, E extends ListCodAbs<T>, K extends ListCodAbsDu<T, E>, M extends ListAbsSearch<T, E, K>> {
	Logger logger = Logger.getLogger(ListHashSearch.class);
	/**
	 * 哈希表LOC--LOC细节<br>
	 * 用于快速将LOC编号对应到LOC的细节<br>
	 * hash（LOCID）--GeneInforlist，其中LOCID代表具体的条目编号 <br>
	  * 会有有多个LOCID共用一个区域的情况，所以有多个不同的LOCID指向同一个GffdetailUCSCgene<br>
	 */
	protected LinkedHashMap<String,T> mapName2DetailAbs;
	/**
	 * 哈希表LOC--在arraylist上的Num<br>
	 * 用于快速将LOC编号对应到其对应的chr上的位置<br>
	 */
	protected LinkedHashMap<String,Integer> mapName2DetailNum;
	/**  起点默认为开区间  */
	int startRegion = 1;
	/**  终点默认为闭区间 */
	int endRegion = 0;
	/**
	 * 这个是真正的查找用hash表<br>
	 * 这个哈希表来存储
	 * hash（ChrID）--ChrList--GeneInforList(GffDetail类)<br>
	 * 其中ChrID为小写，
	 * 代表染色体名字，因此用get来获取相应的ChrList的时候要输入小写的ChrID
	 * chr格式，全部小写 chr1,chr2,chr11<br>
	 */
	protected LinkedHashMap<String, M> mapChrID2ListGff;
	/** 保存所有gffDetailGene */
	ArrayList<T> lsGffDetailAll = new ArrayList<T>();
	/** 顺序存储ChrHash中的ID，这个就是ChrHash中实际存储的ID，如果两个Item是重叠的，就取其中的第一个 */
	protected ArrayList<String> lsNameAll;
	/**
	 * 这个List顺序存储每个基因号或条目号，这个打算用于提取随机基因号，实际上是所有条目按顺序放入，但是不考虑转录本(UCSC)或是重复(Peak)
	 * 这个ID与locHash一一对应，但是不能用它来确定某条目的前一个或后一个条目
	 */
	protected ArrayList<String> lsNameNoRedundent;
	
	protected String gfffilename = "";
	
	public String getGffFilename() {
		return gfffilename;
	}
	/**
	 * 起点是否为闭区间，不是则为开区间，<br>
	 * False: 开区间的意思是，24表示从0开始计数的24位，也就是实际的25位<br>
	 * True: 闭区间的意思是，24就代表第24位<br>
	 * UCSC的默认文件的起点是开区间
	 */
	public void setStartRegion(boolean region) {
		if (region) 
			this.startRegion = 0;
		else 
			this.startRegion = 1;
	}
	/**
	 * 起点默认为开区间
	 */
	public int getStartRegion() {
		return startRegion;
	}
	/**
	 * 终点默认为闭区间
	 */
	public int getEndRegion() {
		return endRegion;
	}
	/**
	 * 起点是否为闭区间，不是则为开区间，<br>
	 * False: 开区间的意思是，24表示从0开始计数的24位，也就是实际的25位<br>
	 * True: 闭区间的意思是，24就代表第24位<br>
	 * UCSC的默认文件的终点是闭区间间
	 */
	public void setEndRegion(boolean region) {
		if (region) 
			this.endRegion = 0;
		else 
			this.endRegion = 1;
	}
	/**
	 * 返回哈希表 LOC--LOC细节<br/>
	 * 用于快速将LOC编号对应到LOC的细节
	 * hash（LOCID）--GeneInforlist，其中LOCID代表具体的基因编号 <br/>
	 */
	public HashMap<String,Integer> getMapName2DetailNum() {
		if (mapName2DetailNum != null) {
			return mapName2DetailNum;
		}
		mapName2DetailNum = new LinkedHashMap<String, Integer>();
		for (M listAbs : mapChrID2ListGff.values()) {
			mapName2DetailNum.putAll(listAbs.getMapName2DetailAbsNum());
		}
		return mapName2DetailNum;
	}
	/**
	 * 返回哈希表 LOC--LOC细节<br/>
	 * 用于快速将LOC编号对应到LOC的细节
	 * hash（LOCID）--GeneInforlist，其中LOCID代表具体的基因编号 <br/>
	 */
	public HashMap<String, T> getMapName2Detail() {
		if (mapName2DetailAbs != null) {
			return mapName2DetailAbs;
		}
		mapName2DetailAbs = new LinkedHashMap<String, T>();
		for (M listAbs : mapChrID2ListGff.values()) {
			mapName2DetailAbs.putAll(listAbs.getMapName2DetailAbs());
		}
		return mapName2DetailAbs;
	}
	/**
	 * 给定一个chrID，返回该chrID所对应的ListAbs
	 * @param chrID
	 * @return
	 */
	public M getListDetail(String chrID) {
		chrID = chrID.toLowerCase();
		return mapChrID2ListGff.get(chrID);
	}

	/**
	 * 返回List顺序存储每个基因号或条目号，这个打算用于提取随机基因号。
	 * 不能通过该方法获得某个LOC在基因上的定位
	 * 每个gffDetail返回一个Name
	 */
	public ArrayList<String> getLsNameNoRedundent() {
		if (lsNameNoRedundent == null) {
			lsNameNoRedundent = new ArrayList<String>();
			for (M lsGff : mapChrID2ListGff.values()) {
				for (T gff : lsGff) {
					lsNameNoRedundent.add(gff.getNameSingle());
				}
			}
		}
		return lsNameNoRedundent;
	}
	/** 顺序存储ChrHash中的ID，这个就是ChrHash中实际存储的ID，如果两个Item是重叠的，就全加入 */
	public ArrayList<String> getLsNameAll() {
		if (lsNameAll != null) {
			return lsNameAll;
		}
		lsNameAll = new ArrayList<String>();
		for (M lsGff : mapChrID2ListGff.values()) {
			lsNameAll.addAll(lsGff.getLsNameAll());
		}
		return lsNameAll;
	}

	/**
	 * 返回真正的查找用hash表<br>
	 * 这个哈希表来存储
	 * hash（ChrID）--ChrList--GeneInforList(GffDetail类)<br>
	 * 其中ChrID为小写，
	 * 代表染色体名字，因此用get来获取相应的ChrList的时候要输入小写的ChrID
	 * chr格式，全部小写 chr1,chr2,chr11<br>
	 */
	public HashMap<String, M> getMapChrID2LsGff() {
		if (mapChrID2ListGff == null) {
			mapChrID2ListGff = new LinkedHashMap<String, M>();
		}
		return mapChrID2ListGff;
	}
	/**
	 * 获得的每一个信息都是实际的而没有clone
	 * 输入PeakNum，和单条Chr的list信息 返回该PeakNum的所在LOCID，和具体位置
	 * 采用clone的方法获得信息
	 * 没找到就返回null
	 * @param chrID 内部自动转化为小写
	 * @param cod1 坐标
	 */
	public E searchLocation(String chrID, int cod1) {
		chrID = chrID.toLowerCase();
		M Loclist =  getMapChrID2LsGff().get(chrID);// 某一条染色体的信息
		if (Loclist == null) {
			return null;
		}
		E gffCod1 = Loclist.searchLocation(cod1);//(chrID, Math.min(cod1, cod2));
		return gffCod1;
	}
	/**
	 * 返回双坐标查询的结果，内部自动判断 cod1 和 cod2的大小
	 * 如果cod1 和cod2 有一个小于0，那么坐标不存在，则返回null
	 * @param chrID 内部自动小写
	 * @param cod1 必须大于0
	 * @param cod2 必须大于0
	 * @return
	 */
	public K searchLocation(String chrID, int cod1, int cod2) {
		chrID = chrID.toLowerCase();
		M Loclist =  getMapChrID2LsGff().get(chrID);// 某一条染色体的信息
		if (Loclist == null) {
			return null;
		}
		return Loclist.searchLocationDu(cod1, cod2);
	}
	/**
	 * 给定ID，在其所对应的信息上加一
	 * @param name
	 * @param location
	 */
	public void addNumber(String chrID, int location) {
		ListCodAbs<T> gffCodPeak = searchLocation(chrID, location);
		if (!gffCodPeak.isInsideLoc()) {
			return;
		}
		T gffDetailPeak = gffCodPeak.getGffDetailThis();
		gffDetailPeak.addReadsInElementNum();
	}
	/**
	 * 返回区间以及每个区间的数量，前面必须add过
	 * key：int的区间
	 * value：具体数量
	 * @return
	 */
	public LinkedHashMap<String,LinkedHashMap<int[], Integer>> getFreq() {
		LinkedHashMap<String, LinkedHashMap<int[], Integer>> hashResult = new LinkedHashMap<String, LinkedHashMap<int[],Integer>>();
		Set<String> setChrID = getMapChrID2LsGff().keySet();
		for (String string : setChrID) {
			LinkedHashMap<int[], Integer> hashTmpResult = new LinkedHashMap<int[], Integer>();
			M lsPeak = getListDetail(string);
			for (T gffDetailPeak : lsPeak) {
				int[] interval = new int[2];
				interval[0] = gffDetailPeak.getStartAbs();
				interval[1]= gffDetailPeak.getEndAbs();
				hashTmpResult.put(interval, gffDetailPeak.getReadsInElementNum());
			}
			hashResult.put(string, hashTmpResult);
		}
		return hashResult;
	}
	/**
	 * 在读取文件后如果有什么需要设置的，可以写在setOther();方法里面
	 * @param gfffilename
	 */
	public void ReadGffarray(String gfffilename) {
		if (this.gfffilename.equals(gfffilename)) {
			return;
		}
		this.gfffilename = gfffilename;
		try {
			ReadGffarrayExcep(gfffilename);
			setItemDistance();
			setOther();
			getMapName2DetailNum();
			getMapName2Detail();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * @本方法需要被覆盖
	 * 最底层读取gff的方法<br>
	 * 输入Gff文件，最后获得两个哈希表和一个list表,
	 * 结构如下：<br/>
	 * @1.Chrhash
	 * （ChrID）--ChrList--GeneInforList(GffDetail类)<br/>
	 *   其中ChrID为小写，代表染色体名字，因此用get来获取相应的ChrList的时候要输入小写的ChrID,
	 * chr格式，全部小写 chr1,chr2,chr11<br/>
	 * 
	 * @2.locHashtable
	 * （LOCID）--GeneInforlist，其中LOCID代表具体的条目编号,各个条目定义由相应的GffHash决定 <br/>
	 * 
	 * @3.LOCIDList
	 * （LOCID）--LOCIDList，按顺序保存LOCID,只能用于随机查找基因，不建议通过其获得某基因的序号<br/>
	 * @throws Exception 
	 */
	protected abstract void ReadGffarrayExcep(String gfffilename) throws Exception;
	/**
	 * 需要覆盖
	 * 查找某个特定LOC的信息
	 * {return locHashtable.get(LOCID);}
	 * @param LOCID 给定某LOC的名称，注意名称是一个短的名字，譬如在UCSC基因中，不是locstring那种好几个基因连在一起的名字，而是单个的短的名字
	 * @return 返回该LOCID的具体GffDetail信息，用相应的GffDetail类接收
	 */
	public T searchLOC(String LOCID) {
		return  getMapName2Detail().get(LOCID.toLowerCase());
	}
	/**
	 * 需要覆盖
	 * {return Chrhash.get(chrID).get(LOCNum);}
	 * 给定chrID和该染色体上的位置，返回GffDetail信息
	 * @param chrID 小写
	 * @param LOCNum 该染色体上待查寻LOC的int序号
	 * @return  返回该LOCID的具体GffDetail信息，用相应的GffDetail类接收
	 */
	public T searchLOC(String chrID,int LOCNum) {
		chrID = chrID.toLowerCase();
		return mapChrID2ListGff.get(chrID).get(LOCNum);
	}
	
	/**
	 * 给定某个LOCID，返回该LOC在某条染色体中的位置序号号，第几位<br>
	 * 也就是Chrhash中某个chr下该LOC的位置<br>
	 * 该位置必须大于等于0，否则就是出错<br>
	 * 该比较是首先用单个LOCID从locHashtable获得其GffDetail类，然后用ChrID在Chrhash中获得某条染色体的gffdetail的List，然后比较他们的locString以及基因的起点和终点
	 * 仅仅将GffDetail的equal方法重写。
	 * @param LOCID 输入某基因编号
	 * @return string[2]<br>
	 * 0: 染色体编号，chr1,chr2等，都为小写<br>
	 * 1:该染色体上该LOC的序号，如1467等
	 */
	public String[] getLOCNum(String LOCID) {
		String[] result = new String[2];
		T ele = getMapName2Detail().get(LOCID.toLowerCase());
		result[0] = ele.getRefID();
		result[1] = getMapName2DetailNum().get(LOCID.toLowerCase()) + "";
		return result;
	}
	/**
	 * 设定每个GffDetail的tss2UpGene和tes2DownGene
	 */
	protected void setItemDistance() {
		for (M lsGffDetail : mapChrID2ListGff.values()) {
			for (int i = 0; i < lsGffDetail.size(); i++) {
				T gffDetail = lsGffDetail.get(i);
				T gffDetailUp = null;
				T gffDetailDown = null;
				if (i > 0) {
					gffDetailUp = lsGffDetail.get(i-1);
				}
				if (i < lsGffDetail.size() - 1) {
					gffDetailDown = lsGffDetail.get(i + 1);
				}
				if (gffDetail.isCis5to3()) {
					gffDetail.setTss2UpGene( distance(gffDetail, gffDetailUp, true) );
					gffDetail.setTes2DownGene( distance(gffDetail, gffDetailDown, false) );
				}
				else {
					gffDetail.setTss2UpGene( distance(gffDetail, gffDetailDown, false) );
					gffDetail.setTes2DownGene( distance(gffDetail, gffDetailUp, true) );
				}
			}
		}
	}
	
	private int distance(T gffDetail1, T gffDetail2, boolean Up) {
		if (gffDetail2 == null) {
			return 0;
		}
		else {
			if (Up) {
				return Math.abs(gffDetail1.getStartAbs() - gffDetail2.getEndAbs());
			}
			else {
				return Math.abs(gffDetail1.getEndAbs() - gffDetail2.getStartAbs());
			}
		}
	}
	/**
	 * 在读取文件后如果有什么需要设置的，可以写在setOther();方法里面，本方发为空，直接继承即可
	 */
	protected void setOther()
	{
		
	}
	/**
	 * 返回所有不重复GffDetailGene
	 * @return
	 */
	public ArrayList<T> getGffDetailAll() {
		if (lsGffDetailAll.size() != 0) {
			return lsGffDetailAll;
		}
		for (M lsGffDetailGenes : mapChrID2ListGff.values()) {
			lsGffDetailAll.addAll(lsGffDetailGenes);
		}
		return lsGffDetailAll;
	}
}
