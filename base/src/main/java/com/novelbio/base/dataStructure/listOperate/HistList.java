package com.novelbio.base.dataStructure.listOperate;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.novelbio.base.dataStructure.MathComput;
import com.novelbio.base.plot.BarStyle;
import com.novelbio.base.plot.DotStyle;
import com.novelbio.base.plot.PlotScatter;
import com.novelbio.base.plot.PlotBox.BoxInfo;

public abstract class HistList extends ListAbsSearch<HistBin, ListCodAbs<HistBin>, ListCodAbsDu<HistBin,ListCodAbs<HistBin>>> {
	private static final Logger logger = Logger.getLogger(HistList.class);
	private static final long serialVersionUID = 1481673037539688125L;
	
	/** 总共多少数字 */
	long allNum = 0;
	HistBinType histBinType = HistBinType.LopenRclose;
	
	PlotScatter plotScatter;
	
	/**
	 * 默认是左开右闭
	 * @param histBinType
	 */
	public void setHistBinType(HistBinType histBinType) {
		this.histBinType = histBinType;
	}
	
	@Override
	protected ListCodAbs<HistBin> creatGffCod(String listName, int Coordinate) {
		ListCodAbs<HistBin> lsAbs = new ListCodAbs<HistBin>(listName, Coordinate);
		return lsAbs;
	}

	@Override
	protected ListCodAbsDu<HistBin, ListCodAbs<HistBin>> creatGffCodDu(
			ListCodAbs<HistBin> gffCod1, ListCodAbs<HistBin> gffCod2) {
		ListCodAbsDu<HistBin, ListCodAbs<HistBin>> lsResult= new ListCodAbsDu<HistBin, ListCodAbs<HistBin>>(gffCod1, gffCod2);
		return lsResult;
	}
	
	/**
	 * 获得的每一个信息都是实际的而没有clone
	 * 输入PeakNum，和单条Chr的list信息 返回该PeakNum的所在LOCID，和具体位置
	 * 采用clone的方法获得信息
	 * 没找到就返回null
	 */
	@Deprecated
	public ListCodAbs<HistBin> searchLocation(int Coordinate) {
		return super.searchLocation(Coordinate);
	}
	@Deprecated
	public boolean add(HistBin e) {
		// TODO Auto-generated method stub
		return super.add(e);
	}
	/**
	 * 返回双坐标查询的结果，内部自动判断 cod1 和 cod2的大小
	 * 如果cod1 和cod2 有一个小于0，那么坐标不存在，则返回null
	 * @param chrID 内部自动小写
	 * @param cod1 必须大于0
	 * @param cod2 必须大于0
	 * @return
	 */
	@Deprecated
	public ListCodAbsDu<HistBin, ListCodAbs<HistBin>> searchLocationDu(int cod1, int cod2) {
		return super.searchLocationDu(cod1, cod2);
	}
	
	public void setPlotScatter(PlotScatter plotScatter) {
		this.plotScatter = plotScatter;
	}
	
	/**
	 * 自动设置histlist的bin，每隔interval设置一位，名字就起interval
	 * @param histList
	 * @param binNum bin的个数
	 * @param interval 间隔
	 * @param maxSize 最大值，如果最后一位bin都没到最大值，接下来一个bin就和最大值合并
	 */
	public void setBinAndInterval(int binNum, int interval,int maxSize) {
		clear();
		setStartBin(interval, interval + "", 0, interval);
		int binNext = interval*2;
		for (int i = 1; i < binNum; i++) {
			addHistBin(binNext, binNext + "", binNext);
			binNext = binNext + interval;
		}
		if (binNext < maxSize) {
			addHistBin(binNext, binNext + "", maxSize);
		}
	}
	
	/**
	 * 设置起点
	 * @param number 本bin所代表的数值，null就用终点和起点的平均值
	 * @param name
	 * @param start
	 * @param end
	 */
	public void setStartBin(Integer number, String name, int start, int end) {
		setStartBin(number.doubleValue(), name, start, end);
	}
	/**
	 * 设置起点
	 * @param number 本bin所代表的数值，null就用终点和起点的平均值
	 * @param name
	 * @param start
	 * @param end
	 */
	public void setStartBin(Double number, String name, int start, int end) {
		HistBin histBinThis = new HistBin(number);
		histBinThis.setStartCis(start);
		histBinThis.setEndCis(end);
		histBinThis.addItemName(name);
		add(histBinThis);
	}

	/**
	 * 在此之前必须先设定起点{@link #setStartBin}
	 * 添加hist区间，必须是紧挨着设定，
	 * 意思本区间为上一个num和本num之间
	 * @param number 本bin所代表的数值，null就用终点和起点的平均值
	 * @param name 填写的话，就用该名字做坐标名字
	 * @param thisNum
	 */
	public void addHistBin(Integer number, String name, int thisNum) {
		addHistBin(number.doubleValue(), name, thisNum);
	}
	/**
	 * 在此之前必须先设定起点{@link #setStartBin}
	 * 添加hist区间，必须是紧挨着设定，
	 * 意思本区间为上一个num和本num之间
	 * @param number 本bin所代表的数值，null就用终点和起点的平均值
	 * @param name
	 * @param thisNum
	 */
	public void addHistBin(Double number, String name, int thisNum) {
		HistBin histBinLast = get(size() - 1);
		histBinLast.getEndCis();
		HistBin histBinThis = new HistBin(number);
		histBinThis.addItemName(name);
		histBinThis.setStartCis(histBinLast.getEndCis());
		histBinThis.setEndCis(thisNum);
		add(histBinThis);
	}
	
	/**
	 * 查找 coordinate，根据 HistBinType 返回相应的histbin
	 * @param coordinate
	 * @return
	 */
	public abstract HistBin searchHistBin(int coordinate);
	/**
	 * 给定number，把相应的hist加上1
	 * @param coordinate
	 */
	public void addNum(int coordinate) {
		addNum(coordinate, 1);
	}
	/**
	 * 给定number，把相应的hist加上addNumber的数量
	 * @param coordinate
	 */
	public void addNum(int coordinate, int addNumber) {
		HistBin histBin = searchHistBin(coordinate);
		histBin.addNumber(addNumber);
		allNum = allNum + addNumber;
	}
	
	/**
	 * 返回BoxInfo<br>
	 * @return
	 */
	public BoxInfo getBoxInfo() {
		BoxInfo boxInfo = new BoxInfo(getName());
		boxInfo.setInfo25And75(getPercentInfo(25).getThisNumber(), getPercentInfo(75).getThisNumber());
		boxInfo.setInfoMedian(getPercentInfo(50).getThisNumber());
		boxInfo.setInfoMinAndMax(getPercentInfo(1).getThisNumber(), getPercentInfo(99).getThisNumber());
		boxInfo.setInfo5And95(getPercentInfo(5).getThisNumber(), getPercentInfo(95).getThisNumber());
		return boxInfo;
	}
	/** 指定percentage乘以100
	 * 返回该比例所对应的值
	 */
	private HistBin getPercentInfo(int percentage) {
		long thisNumThreshold = (long) ((double)percentage/100 * allNum);
		long thisNum = 0;
		
		for (HistBin histBin : this) {
			thisNum = thisNum + histBin.getCountNumber();
			if (thisNum >= thisNumThreshold) {
				return histBin;
			}
		}
		//全找了一遍没找到么说明数字太大了那就返回最后一位的HistBin吧
		return get(size() - 1);		
	}
	
	/**
	 * 根据统计画直方图
	 * @param dotStyle
	 * @param fontSize 字体大小
	 * @return
	 */
	public PlotScatter getPlotHistBar(BarStyle dotStyle) {
		double[] Ycount = getYnumber(0);
		double[] Xrange = getX();
		String[] xName = getRangeX();
		HashMap<Double, String> mapX2Name = new HashMap<Double, String>();
		for (int i = 0; i < xName.length; i++) {
			HistBin histBin = get(i);
			if (histBin.getNameSingle() == null || histBin.getNameSingle().trim().equals("")) {
				mapX2Name.put(Xrange[i], xName[i]);
			} else {
				mapX2Name.put(Xrange[i], histBin.getNameSingle());
			}
		}
		if (plotScatter == null) {
			plotScatter = new PlotScatter(PlotScatter.PLOT_TYPE_BARPLOT);
		}
		double minY = MathComput.min(Ycount);
		double maxY = MathComput.max(Ycount);

		if (dotStyle.getBarWidth() == 0 && Xrange.length > 1) {
			dotStyle.setBarAndStrokeWidth(Xrange[1] - Xrange[0]);
		}
		
		plotScatter.setAxisX(Xrange[0] - 1, Xrange[Xrange.length - 1] + 1);
		plotScatter.setAxisY(minY, maxY * 1.2);
		plotScatter.addXY(Xrange, Ycount, dotStyle);
//		plotScatter.setAxisTicksXMap(mapX2Name);
		return plotScatter;
	}
	/**
	 * 返回x的数值，从0开始
	 * @return
	 */
	private double[] getX() {
		double[] lengthX = new double[size()];
		for (int j = 0; j < lengthX.length; j++) {
			lengthX[j] = j;
		}
		return lengthX;
	}
	/**
	 * 返回y的数值，注意初始的HistBin必须为等分，否则会出错
	 * @binNum 分割的份数，小于等于0表示分割为histlist的份数
	 * @return
	 */
	private double[] getYnumber(int binNum) {
		if (binNum <= 0) {
			binNum = size();
		}
		
		double[] numberY = new double[size()];
		int i = 0;
		for (HistBin histBin : this) {
			numberY[i] = histBin.getCountNumber();;
			i++;
		}
		
		if (binNum != size()) {
			numberY = MathComput.mySpline(numberY, binNum, 0, 0, 0);
		}
		
		return numberY;
	}
	/**
	 * 返回x的区间的名字
	 * @return
	 */
	private String[] getRangeX() {
		String[] rangeX = new String[size()];
		int i = 0;
		for (HistBin histBin : this) {
			rangeX[i] = histBin.getStartCis() + "_" + histBin.getEndCis();
			i++;
		}
		return rangeX;
	}
	
	public PlotScatter getIntegralPlot(boolean cis, DotStyle dotStyle) {
		ArrayList<double[]> lsXY = getIntegral(cis);
		PlotScatter plotScatter = null;
		if (dotStyle.getStyle() == DotStyle.STYLE_BAR || dotStyle.getStyle() == DotStyle.STYLE_BOX) {
			plotScatter = new PlotScatter(PlotScatter.PLOT_TYPE_BARPLOT);
		} else {
			plotScatter = new PlotScatter(PlotScatter.PLOT_TYPE_SCATTERPLOT);
		}
		
		plotScatter.addXY(lsXY, dotStyle);
		plotScatter.setAxisX(get(0).getStartAbs(), get(size() - 1).getStartAbs());
		plotScatter.setAxisY(0, 1);
		return plotScatter;
	}
	
	/**
	 * 积分图
	 * @param cis true：从前往后，就是最前面是10%，越往后越高
	 * false：从后往前，就是最前面是100%，越往后越低
	 */
	public ArrayList<double[]> getIntegral(boolean cis) {
		ArrayList<double[]> lsXY = new ArrayList<double[]>();
		double thisNum = 0;
		double[] x = new double[size()];
		double[] y = new double[size()];
		if (cis) {
			for (int count = 0; count < size(); count++) {
				HistBin histBin = get(count);
				thisNum = thisNum + histBin.getCountNumber();
				x[count] = histBin.getThisNumber();
				y[count] = thisNum/allNum;
			}
		} else {
			for (int count = size() - 1; count >= 0; count--) {
				HistBin histBin = get(count);
				thisNum = thisNum + histBin.getCountNumber();
				x[count] = histBin.getThisNumber();
				y[count] = thisNum/allNum;
			}
		}
		for (int i = 0; i < x.length; i++) {
			double[] xy = new double[2];
			xy[0] = x[i];
			xy[1] = y[i];
			lsXY.add(xy);
		}
 		return lsXY;
	}

	/**
	 * @param name hist的名字，务必不能重复，否则hash表会有冲突
	 * @param cisList true 从小到大排序的list。 false 从大到小排序的list
	 * @return
	 */
	public static HistList creatHistList(String name, boolean cisList){
		if (cisList) {
			return new HistListCis(name);
		} else {
			return new HistListTrans(name);
		}
	}
	
	public static enum HistBinType {
		LcloseRopen, LopenRclose
	}
	
}

class HistListCis extends HistList {
	private static final Logger logger = Logger.getLogger(HistListCis.class);
	private static final long serialVersionUID = -4966352009491903291L;
	
	public HistListCis(String histName) {
		setName(histName);
	}
	
	/**
	 * 查找 coordinate，根据 HistBinType 返回相应的histbin
	 * @param coordinate
	 * @return
	 */
	public HistBin searchHistBin(int coordinate) {
		ListCodAbs<HistBin> lsHistBin = searchLocation(coordinate);
		HistBin histThis = lsHistBin.getGffDetailThis();
		HistBin histLast = lsHistBin.getGffDetailUp();
		HistBin histNext = lsHistBin.getGffDetailDown();
		
		HistBin resultBin = histThis;
		
		if (histThis == null) {
			HistBin histbin = null;
			if (histLast != null) {
				histbin = histLast;
			} else if (histNext != null) {
				histbin = histNext;
			}
			return histbin;
		}
		
		if (histBinType == HistBinType.LcloseRopen) {
			if ((coordinate >= histThis.getStartCis() && coordinate < histThis.getEndCis())
					||
				(histLast == null && coordinate < histThis.getStartCis() )
					||
				(histNext == null && coordinate >= histThis.getEndCis() )
			) {
				resultBin = histThis;
			} else if (coordinate < histThis.getStartCis() && coordinate >= histLast.getEndCis()) {
				resultBin = histLast;
			} else if (coordinate >= histThis.getEndCis() && coordinate <= histNext.getStartCis()) {
				resultBin = histNext;
			}
		} else if (histBinType == HistBinType.LopenRclose) {
			if ((coordinate > histThis.getStartCis() && coordinate <= histThis.getEndCis())
				||
			(histLast == null && coordinate <= histThis.getStartCis())
				||
			(histNext == null && coordinate > histThis.getEndCis())	
					) {
				resultBin = histThis;
			} else if (coordinate <= histThis.getStartCis() && coordinate >= histLast.getEndCis()) {
				resultBin = histLast;
			} else if (coordinate > histThis.getEndCis() && coordinate <= histNext.getStartCis()) {
				resultBin = histNext;
			}
		}
		return resultBin;
	}

}

class HistListTrans extends HistList {
	private static final Logger logger = Logger.getLogger(HistListTrans.class);
	private static final long serialVersionUID = -5310222125261004172L;
	
	public HistListTrans(String name) {
		setName(name);
	}
	/**
	 * 查找 coordinate，根据 HistBinType 返回相应的histbin
	 * @param coordinate
	 * @return
	 */
	public HistBin searchHistBin(int coordinate) {
		ListCodAbs<HistBin> lsHistBin = searchLocation(coordinate);
		HistBin histThis = lsHistBin.getGffDetailThis();
		HistBin histLast = lsHistBin.getGffDetailUp();
		HistBin histNext = lsHistBin.getGffDetailDown();
		
		HistBin resultBin = histThis;
		if (histBinType == HistBinType.LcloseRopen) {
			if (coordinate <= histThis.getStartCis() && coordinate > histThis.getEndCis()) {
				resultBin = histThis;
			} else if (coordinate > histThis.getStartCis() && coordinate <= histLast.getEndCis()) {
				resultBin = histLast;
			} else if (coordinate <= histThis.getEndCis() && coordinate >= histNext.getStartCis()) {
				resultBin = histNext;
			}
		} else if (histBinType == HistBinType.LopenRclose) {
			if (coordinate > histThis.getStartCis() && coordinate <= histThis.getEndCis()) {
				resultBin = histThis;
			} else if (coordinate <= histThis.getStartCis() && coordinate >= histLast.getEndCis()) {
				resultBin = histLast;
			} else if (coordinate > histThis.getEndCis() && coordinate <= histNext.getStartCis()) {
				resultBin = histNext;
			}
		}
		return resultBin;
	}
	
}

