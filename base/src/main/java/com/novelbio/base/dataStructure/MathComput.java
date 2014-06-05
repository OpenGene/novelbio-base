package com.novelbio.base.dataStructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
//import org.apache.ibatis.annotations.Insert;


public class MathComput {
	private static Logger logger = Logger.getLogger(MathComput.class);
	
	
	/**
	 * 输入数据，获得平均数
	 * 用于获得每10个bp的tag堆积数的中位数
	 * @return
	 */
	public static double mean(int[] unsortNum)
	{
		int length=unsortNum.length;
		int sum=0;
		for(int i=0;i<length;i++)
		{
			sum=sum+unsortNum[i];
		}
		double avg= (double)sum/length;
		return avg;
	}
	
	/**
	 * 输入数据，获得平均数
	 * 用于获得每10个bp的tag堆积数的中位数
	 * @return
	 */
	public static double mean(int[]... unsortNum)
	{
		long sum = 0;
		long num = 0;
		for (int i = 0; i < unsortNum.length; i++) {
			for (int j = 0; j < unsortNum[i].length; j++) {
				sum = sum + unsortNum[i][j];
				num ++ ;
			}
		}
		double avg=(double) sum/num;
		return avg;
	}
	/**
	 * 输入数据，获得平均数
	 * 用于获得每10个bp的tag堆积数的中位数
	 * @return
	 */
	public static double mean(Iterable<int[]> unsortNum)
	{
		long sum = 0;
		long num = 0;
		for (int[] is : unsortNum) {
			for (int i : is) {
				sum = sum + i;
				num ++;
			}
		}
		double avg=(double) sum/num;
		return avg;
	}
	/**
	 * 输入数据，获得平均数
	 * 用于获得每10个bp的tag堆积数的中位数
	 * @return
	 */
	public static long mean(long[] unsortNum)
	{
		int length=unsortNum.length;
		long sum=0;
		for(int i=0;i<length;i++)
		{
			sum=sum+unsortNum[i];
		}
		long avg=sum/length;
		return avg;
	}
	
	public static double mean(List<? extends Number> lsNumbers) {
		double length=lsNumbers.size();
		double sum=0;
		for(int i=0;i<length;i++)
		{
			sum=sum+lsNumbers.get(i).doubleValue();
		}
		double avg=sum/length;
		return avg;
	}
	
	public static double mean(Set<? extends Number> lsNumbers) {
		double length=lsNumbers.size();
		double sum=0;
		for (Number number : lsNumbers) {
			sum = sum + number.doubleValue();;
		}
		double avg=sum/length;
		return avg;
	}
	
	/**
	 * 输入数据，获得平均数
	 * 采用插入排序法，据说对于小规模数据效率还不错
	 * 用于获得每10个bp的tag堆积数的中位数
	 * @return
	 */
	public static double mean(double[] unsortNum)
	{
		double length=unsortNum.length;
		double sum=0;
		for(int i=0;i<length;i++) {
			sum=sum+unsortNum[i];
		}
		double avg=sum/length;
		return avg;
	}
	/**
	 * 按照ID，获取一个矩阵的中位数
	 * 每列表示不同的信息，每行表示一个基因
	 * 可能存在重复基因，所以要对重复行(也就是重复基因)，取中位数
	 * @param lsIn
	 * @param colAccID 实际列，从1开始计数
	 * @param colNum
	 * @return
	 */
	public static ArrayList<String[]> getMedian(List<String[]> lsIn, int colAccID, List<Integer> colNum) {
		/** 每个ID一个基因 */
		ArrayListMultimap<String, String[]> mapAccID2Info = ArrayListMultimap.create();
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		colAccID--;
		ArrayList<Integer> lsColNum = new ArrayList<Integer>();
		for (int i = 0; i < colNum.size(); i++) {
			lsColNum.add(colNum.get(i) - 1);
		}
		for (String[] strings : lsIn) {
			mapAccID2Info.put(strings[colAccID].trim(), strings);
		}
		
		for (String accID : mapAccID2Info.keySet()) {
			List<String[]> value = mapAccID2Info.get(accID);
			try {
				lsResult.add(getMediaInfo(value, lsColNum));
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
		return lsResult;
	}
	/**
	 * @param lsInfo 指定几行信息
	 * @param col 指定所在的列
	 * @return 返回所在列的中位数
	 * 列名采用第一行list的名字
	 */
	public static String[] getMediaInfo(List<String[]> lsInfo, List<Integer> col) {
		
		if (lsInfo.size() == 1) {
			return lsInfo.get(0);
		}
		String[] result = lsInfo.get(0);
		
		for (int i = 0; i < col.size(); i++) {
			double[] info = new double[lsInfo.size()];
			for (int m = 0; m < lsInfo.size(); m++) {
				try {
					info[m] = Double.parseDouble(lsInfo.get(m)[col.get(i)].trim());
				} catch (Exception e) {
					info[m] = 0;
				}
				
			}
			double infoNew = median(info);
			result[col.get(i)] = infoNew + "";
		}
		return result;
	}
	/**
	 * 输入数据，获得和
	 * @param Num
	 * @return
	 */
	public static	double sum(double[] Num) {
		double sum = 0;
		for (double d : Num) {
			sum = sum + d;
		}
		return sum;
	}
	/**
	 * 输入数据，获得和
	 * @param Num
	 * @return
	 */
	public static	double sum(List<? extends Number> lsNum) {
		double sum = 0;
		for (Number d : lsNum) {
			sum = sum + d.doubleValue();
		}
		return sum;
	}
	/**
	 * 输入数据，获得和
	 * @param Num
	 * @return
	 */
	public static	int sum(int[] Num) {
		int sum = 0;
		for (int d : Num) {
			sum = sum + d;
		}
		return sum;
	}
	/**
	 * 输入数据，获得中位数, 用于10
	 * 采用插入排序法，据说对于小规模数据效率还不错
	 * 用于获得每10个bp的tag堆积数的中位数
	 * @return
	 */
	public static int median(int[] unsortNum) {
		double[] value = new double[unsortNum.length];
		for (int i = 0; i < value.length; i++) {
			value[i] = unsortNum[i];
		}
		return (int)median(value);
	}
	/**
	 * 输入数据，获得中位数, 用于10
	 * 采用插入排序法，据说对于小规模数据效率还不错
	 * @return
	 */
	public static double median(List<? extends Number> lsNumbers) {
		double[] mydouble = new double[lsNumbers.size()];
		for (int i = 0; i < mydouble.length; i++) {
			mydouble[i] = lsNumbers.get(i).doubleValue();
		}
		return median(mydouble);
	}
	/**
	 * 输入数据，获得中位数, 用于10
	 * 采用插入排序法，据说对于小规模数据效率还不错
	 * @param lsNumbers
	 * @param percentage 乘以100的值
	 * @return
	 */
	public static double median(List<? extends Number> lsNumbers, int percentage) {
		double[] mydouble = new double[lsNumbers.size()];
		for (int i = 0; i < mydouble.length; i++) {
			mydouble[i] = lsNumbers.get(i).doubleValue();
		}
//		return StatUtils.percentile(mydouble, percentage);

		return median(mydouble, percentage);
	}
	
	/**
	 * 输入数据，获得中位数, 用于10
	 * 采用插入排序法，据说对于小规模数据效率还不错
	 * 不会对输入数据排序
	 * @return
	 */
	public static double median(double[] unsortNum) {
		return median(unsortNum, 50);
	}
	/**
	 * 
	 * 输入数据，获得中位数, 用于10
	 * 采用插入排序法，据说对于小规模数据效率还不错
	 * @param unsortNum 输入数据，不会对其排序
	 * @param percentage 乘以100的值
	 * @return
	 */
	public static double median(double[] unsortNum, int percentage) {
		double result = StatUtils.percentile(unsortNum, percentage);
		return result;
	}
	/**
	 * copy an array
	 * @param array
	 * @return a new array which is been copied
	 */
	private static double[] copyArray(double[] array)
	{
		double[] arrayResult = new double[array.length];
		for (int i = 0; i < array.length; i++) {
			arrayResult[i] = array[i];
		}
		return arrayResult;
	}
	/**
	 * copy an array
	 * @param array
	 * @return a new array which is been copied
	 */
	private static int[] copyArray(int[] array)
	{
		int[] arrayResult = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			arrayResult[i] = array[i];
		}
		return arrayResult;
	}
	/**
	 * 输入数据，获得最接近中位数的那个数, 用于10
	 * 采用插入排序法，据说对于小规模数据效率还不错
	 * @return
	 */
	public static double medianLike(double[] unsortNum)
	{
		double med=-100;
		int length=unsortNum.length;
		double[] unsortNew = copyArray(unsortNum);
		sort(unsortNew, true);

		if (length%2==0){
			med=(unsortNew[length/2-1]+unsortNew[length/2])/2;
			if (Math.abs(unsortNew[length/2-1] - med) <= Math.abs(unsortNew[length/2] - med)) {
				return unsortNew[length/2-1];
			}
			else {
				return unsortNew[length/2];
			}
		}
		else 
			return unsortNew[length/2];
	}
	
	/**
	 * 输入数据，获得平均数
	 * 采用插入排序法，据说对于小规模数据效率还不错
	 * 用于获得每10个bp的tag堆积数的中位数
	 * @return
	 */
	public static double max(ArrayList<? extends Number> lsNum) {
		double max = lsNum.get(0).doubleValue();
		for (Number number : lsNum) {
			double tmp = number.doubleValue();
			if (tmp > max) {
				max = tmp;
			}
		}
		return max;
	}
	/**
	 * 输入数据，获得平均数
	 * 采用插入排序法，据说对于小规模数据效率还不错
	 * 用于获得每10个bp的tag堆积数的中位数
	 * @return
	 */
	public static double max(double[] num) {
		double max = num[0];
		for (double number : num) {
			double tmp = number;
			if (tmp > max) {
				max = tmp;
			}
		}
		return max;
	}
	/**
	 * 输入数据，获得平均数
	 * 采用插入排序法，据说对于小规模数据效率还不错
	 * 用于获得每10个bp的tag堆积数的中位数
	 * @return
	 */
	public static int max(int[] num) {
		int max = num[0];
		for (int number : num) {
			int tmp = number;
			if (tmp > max) {
				max = tmp;
			}
		}
		return max;
	}
	
	
	
	
	
	/**
	 * 输入数据，获得平均数
	 * 采用插入排序法，据说对于小规模数据效率还不错
	 * 用于获得每10个bp的tag堆积数的中位数
	 * @return
	 */
	public static double min(ArrayList<? extends Number> lsNum) {
		double min = lsNum.get(0).doubleValue();
		for (Number number : lsNum) {
			double tmp = number.doubleValue();
			if (tmp < min) {
				min = tmp;
			}
		}
		return min;
	}
	/**
	 * 输入数据，获得平均数
	 * 采用插入排序法，据说对于小规模数据效率还不错
	 * 用于获得每10个bp的tag堆积数的中位数
	 * @return
	 */
	public static double min(double[] num) {
		double min = num[0];
		for (double number : num) {
			double tmp = number;
			if (tmp < min) {
				min = tmp;
			}
		}
		return min;
	}
	/**
	 * 输入数据，获得平均数
	 * 采用插入排序法，据说对于小规模数据效率还不错
	 * 用于获得每10个bp的tag堆积数的中位数
	 * @return
	 */
	public static int min(int[] num) {
		int min = num[0];
		for (int number : num) {
			int tmp = number;
			if (tmp < min) {
				min = tmp;
			}
		}
		return min;
	}
	
	/**
	 * 输入数据进行排序，
	 * @param unsortNum 待排序的数组
	 * @param smallToBig 是否从小到大排序
	 * @return
	 */
	public static void sort(int[] unsortNum, boolean smallToBig)
	{
		int tmp = -10000;
		int length = unsortNum.length;
		if (smallToBig) {
			for(int i = 1;i<length;i++) {
				tmp = unsortNum[i];
				int j = i;
				for(;j > 0; j --) {
					if(tmp < unsortNum[j-1]) {
						unsortNum[j] = unsortNum[j-1];
					}
					else break;
				}
				unsortNum[j] = tmp;
			}
		}
		else {
			for(int i = 1; i < length; i ++) {
				tmp = unsortNum[i];
				int j = i;
				for(;j > 0;j --) {
					if(tmp > unsortNum[j-1]) {
						unsortNum[j] = unsortNum[j-1];
					}
					else break;
				}
				unsortNum[j] = tmp;
			}
		}
	}
	
	/**
	 * 给定数组，直接排序
	 * @param unsortNum
	 */
	private static void sort(double[] unsortNum, boolean smallToBig)
	{
		double tmp=-10000;
		int length=unsortNum.length;
		if (smallToBig) {
			for(int i=1;i<length;i++) {
				tmp=unsortNum[i];
				int j=i;
				for(;j>0;j--) {
					if(tmp<unsortNum[j-1]) {
						unsortNum[j]=unsortNum[j-1];
					}
					else break;
				}
				unsortNum[j]= tmp;
			}
		}
		else {
			for(int i=1;i<length;i++) {
				tmp=unsortNum[i];
				int j=i;
				for(;j>0;j--) {
					if(tmp>unsortNum[j-1]) {
						unsortNum[j]=unsortNum[j-1];
					}
					else break;
				}
				unsortNum[j]= tmp;
			}
		}
	}
	
	
	/**
	 * 给定一组数(有顺序的排列)，根据给定的分割数，指定获得加权平均，最后获得指定分割数量的数组
	 * 譬如现在有int[20]的一组数，我想要把这组数缩小到int[10]里面并且保持其比例大体吻合，这时候我采用加权平均的方法
	 * 检查了一遍，感觉可以
	 * 用于将500或更多份的数缩小到100份内
	 * @param treatNum invNum里面的bp具体值
	 * @param binNum 后面要生成的分割的块数
	 * @param startBias 从起点的多少开始 为 0,1之间的小数，表示从第一个值的几分之几开始
	 * @param endBias 到终点的多少结束  为 0,1之间的小数，表示到 (结束位点到终点的距离/每个单元的长度)
	 * 0-*--|---1---------2--------3----------4----------5---------6----|--*-7
	 * 星号标记的地方
	 * @param type 0：加权平均 1：取最高值，2：加权但不平均--也就是加和
	 * @return
	 */
	public static double[] mySpline(int[] treatNum, int binNum,double startBias,double endBias,int type)
	{
		double rawlength=treatNum.length-startBias-endBias;
		double binlength=rawlength/binNum; //将每一个分隔的长度标准化为一个比值，基准为invNum为1 
		double[] resultBinValue=new double[binNum];
		for (int i = 1; i <= binNum; i++) 
		{
			//某区域内treatNum最靠左边的一个值(包含边界)的下标+1，因为数组都是从0开始的
			int leftTreatNum=(int) Math.ceil(binlength*(i-1)+startBias);
			//最左边值的权重
			double leftweight=leftTreatNum-binlength*(i-1)-startBias;
			////某区域内treatNum最右边的一个值(不包含边界)的下标+1，因为数组都是从0开始的
			int rightTreatNum=(int) Math.ceil(binlength*i+startBias);
			//最右边值的权重
			int rightfloorNum=(int)Math.floor(binlength*i+startBias);
			double rightweight=binlength*(i)+startBias-rightfloorNum;
			
			//////////////////////如果左右端点都在一个区域内，那么加权平均，最大值，加和都等于该区域的值/////////////////////////////////////////
			if (leftTreatNum>rightfloorNum) {
				resultBinValue[i-1]=treatNum[rightfloorNum];
				//////////看是否会错，可删//////////////////////
				if(leftTreatNum-rightfloorNum!=1)
					System.out.print("mySpline error");
				////////////////////////////////////////////////////////////
				continue;
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////
			//中间有几个值
			int middleNum=rightfloorNum-leftTreatNum;
			
			double leftBinlength=-100000;
			if (leftTreatNum<1) 
				leftBinlength=0;
			else 
				leftBinlength=leftweight*treatNum[leftTreatNum-1];
			
			double rightBinlength=-100000;
			if (rightTreatNum>treatNum.length) 
				rightBinlength=0;
			else 
				rightBinlength=rightweight*treatNum[rightTreatNum-1];
			
			
			double treatNumInbinAll=leftBinlength+rightBinlength;
			double max=Math.max(leftBinlength, rightBinlength);

			for (int j = leftTreatNum; j < rightfloorNum; j++) {
				treatNumInbinAll=treatNumInbinAll+treatNum[j];
				max=Math.max(max,treatNum[j]);
			}
			//////////////////根据条件选择加权平均或最大值或加和////////////////////////////////////////////////////////////////
			double tmpValue;
			if (type==0)//加权平均
				tmpValue=treatNumInbinAll/(leftweight+rightweight+middleNum);
			else if (type==1) //最大值
				tmpValue=max;
			else if (type==2)
				tmpValue=treatNumInbinAll;
			else //默认加权平均
				tmpValue=treatNumInbinAll/(leftweight+rightweight+middleNum);
			//////////////////////////////////////////////////////////////////////////////////
			resultBinValue[i-1]=tmpValue;
		}
		return resultBinValue;
	}
	
	/**
	 * 给定一组数(有顺序的排列)，根据给定的分割数，指定获得加权平均，最后获得指定分割数量的数组
	 * 譬如现在有int[20]的一组数，我想要把这组数缩小到int[10]里面并且保持其比例大体吻合，这时候我采用加权平均的方法
	 * 检查了一遍，感觉可以
	 * 用于将500或更多份的基因中tag累计数缩小到100份内
	 * @param treatNum invNum里面的bp具体值
	 * @param binNum 后面要生成的分割的块数
	 * @param startBias 从起点的多少开始 最左边分隔到起点的距离比值
	 * @param endBias 到终点的多少结束 最右边分隔到终点的距离比值
	 * @param type 0：加权平均 1：取最高值，2：加和
	 * @return
	 */
	public static double[] mySpline(double[] treatNum, int binNum,double startBias,double endBias,int type) {
		if (startBias == 0 && endBias == 0 && treatNum.length == binNum) {
			return treatNum;
		}
		double rawlength=treatNum.length - startBias - endBias;
		double binlength=rawlength/binNum; //将每一个分隔的长度标准化为一个比值，基准为invNum为1 
		double[] resultBinValue=new double[binNum];
		for (int i = 1; i <= binNum; i++) 
		{
			//某区域内treatNum最靠左边的一个值(包含边界)的下标+1，因为数组都是从0开始的
			int leftTreatNum=(int) Math.ceil(binlength*(i-1)+startBias);
			//最左边值的权重
			double leftweight=leftTreatNum-binlength*(i-1)-startBias;
			////某区域内treatNum最右边的一个值(不包含边界)的下标+1，因为数组都是从0开始的
			int rightTreatNum=(int) Math.ceil(binlength*i+startBias);
			//最右边值的权重
			int rightfloorNum=(int)Math.floor(binlength*i+startBias);
			double rightweight=binlength*(i)+startBias-rightfloorNum;
			
			//////////////////////如果左右端点都在一个区域内，那么加权平均，最大值，加和都等于该区域的值/////////////////////////////////////////
			if (leftTreatNum>rightfloorNum) {
				resultBinValue[i-1]=treatNum[rightfloorNum];
				//////////看是否会错，可删//////////////////////
				if(leftTreatNum-rightfloorNum!=1)
					System.out.print("mySpline error");
				////////////////////////////////////////////////////////////
				continue;
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////
			//中间有几个值
			int middleNum=rightfloorNum-leftTreatNum;
			
			double leftBinlength=-100000;
			if (leftTreatNum<1) 
				leftBinlength=0;
			else 
				leftBinlength=leftweight*treatNum[leftTreatNum-1];
			
			double rightBinlength=-100000;
			if (rightTreatNum>treatNum.length) 
				rightBinlength=0;
			else 
				rightBinlength=rightweight*treatNum[rightTreatNum-1];
			
			
			double treatNumInbinAll=leftBinlength+rightBinlength;
			double max=Math.max(leftBinlength, rightBinlength);

			for (int j = leftTreatNum; j < rightfloorNum; j++) {
				treatNumInbinAll=treatNumInbinAll+treatNum[j];
				max=Math.max(max,treatNum[j]);
			}
			//////////////////根据条件选择加权平均或最大值或加和////////////////////////////////////////////////////////////////
			double tmpValue;
			if (type==0)//加权平均
				tmpValue=treatNumInbinAll/(leftweight+rightweight+middleNum);
			else if (type==1) //最大值
				tmpValue=max;
			else if (type==2)
				tmpValue=treatNumInbinAll;
			else //默认加权平均
				tmpValue=treatNumInbinAll/(leftweight+rightweight+middleNum);
			//////////////////////////////////////////////////////////////////////////////////
			resultBinValue[i-1]=tmpValue;
		}
		return resultBinValue;
	}

	
	/**
	 * 这个暂时是为韩燕做的
	 * 给定一组数(有顺序的排列)，根据给定的分割数，指定进行合并，最后获得指定分割数量的数组
	 * 用于将500或更多份的基因中tag累计数缩小到100份内
	 * @param treatNum invNum里面的bp具体值
	 * @param invBpNum 每一块里面的bp数，比方韩燕的要求是3个bp一个coding这么划分
	 * @param startBp 从起点的第几个Bp开始，实际起点，从1开始记数。因为序列不一定是3的倍数，那么我们指定从起点的第几个bp开始，从该Bp(<b>包括该Bp</b>)进行划分
	 * ，这个值最好小于invBpNum
	 * @param Num 选择该invBp中，也就是3个bp中第几个作为最后的结果，从1开始计数。那么韩燕的话，应该选择最后一个--也就是第三个bp的结果作为划分的结果
	 * 也就是说韩燕的设置应该为3
	 * @return
	 */
	public static double[] mySpline(double[] treatNum, int invBpNum,int startBp,int Num)
	{
		//四舍五入获得长度
		int length = (int)((double)(treatNum.length - startBp + 1)/invBpNum + 0.5);
		double[] result = new double[length]; int k = 0; int m = 0;
		//startBp - 2 startbp是实际位置，向前退一位是从0开始的本位点，在向前退一位是前一位点，然后加上Num偏移
		for (int i = startBp - 2 + Num; i < treatNum.length; i++) {
			if (m%invBpNum == 0) {
				result[k] = treatNum[i];
				k++;
			}
			m++;
		}
		return result;
	}
	
	/**
	 * 这个是为韩燕做的
	 * 给定一个数组，指定ATG所在的位置(实际位置)，然后从该位置向前(开区间)，向后(闭区间)，根据给定的分割数，指定进行合并，最后获得指定分割数量的数组
	 * 用于将500或更多份的基因中tag累计数缩小到100份内
	 * @param treatNum invNum里面的bp具体值
	 * @param invBpNum 每一块里面的bp数，比方韩燕的要求是3个bp一个coding这么划分
	 * @param ATGsite 从起点的第几个Bp开始(实际位点)，因为序列不一定是3的倍数，那么我们指定从起点的第几个bp开始，从该Bp(<b>也就是ATG的实际位点，包括该Bp</b>)进行划分
	 * ，这个值最好小于invBpNum
	 * @param Num 选择该invBp中，也就是3个bp中第几个作为最后的结果，从1开始计数。那么韩燕的话，应该选择最后一个--也就是第三个bp的结果作为划分的结果
	 * 也就是说韩燕的设置应该为3
	 * @return
	 */
	public static double[] mySplineHY(double[] treatNum, int invBpNum,int ATGsite,int Num)
	{
		//四舍五入获得长度
		int lengthDown = (int)Math.ceil((double)(treatNum.length - ATGsite + 1)/invBpNum);
		int lengthUp = (int)Math.ceil((double)(ATGsite -  1)/invBpNum);
		double[] result = new double[lengthDown + lengthUp];
		
		int k = lengthUp;
		//后半部分
		for (int i = ATGsite - 1; i < treatNum.length + 1 - Num;  i = i + invBpNum) {
			result[k] = treatNum[i + Num - 1];
			k++;
		}
		//前半部分
		k = lengthUp - 1;
		for (int i = ATGsite - 1 - invBpNum; i >= 1-Num; i = i - invBpNum) {
			result[k] = treatNum[i + Num - 1];
			k--;
		}
		return result;
	}
	
	
	/**
	 * 将 aArray与bArray相加，最后结果保存在aArray中
	 * 如果bArray==null，则直接返回aArray，但是会system.out.println报错
	 * @param aArray
	 * @param bArray
	 * @return
	 */
	public static double[] addArray(double[] aArray,double[] bArray) 
	{
		if(bArray==null)
		{
			System.out.println("addArray Error: bArray==null");
			return aArray;
		}
		
		for (int i = 0; i < aArray.length; i++) {
			aArray[i]=aArray[i]+bArray[i];
		}
		return aArray;
	}
	
	
	/**
	 * 将 aArray与bArray整合在一起并计算a和b每个部分分别的比例。aArray和bArray的长度必须一致
	 * @param aArray 实验组数据
	 * @param bArray 对照组数据，也就是背景
	 * @return
	 */
	public static String[][] batStatistic(long[] aArray,long[] bArray,String[] item,String aName,String bName) 
	{
		String[][] result=new String[aArray.length+1][5];
		long sumaArray=0;long sumbArray=0;
		for (int i = 0; i < aArray.length; i++) {
			sumaArray=sumaArray+aArray[i];
		}
		for (int i = 0; i < bArray.length; i++) {
			sumbArray=sumbArray+bArray[i];
		}
		result[0][0]="item";
		result[0][1]=aName;
		result[0][2]=aName+" proportion";
		result[0][3]=bName;
		result[0][4]=bName+" proportion";
		
		
		for (int i = 1; i < result.length; i++) {
			result[i][0]=item[i-1];
			result[i][1]=aArray[i-1]+"";
			result[i][2]=(double)aArray[i-1]/sumaArray+"";
			result[i][3]=bArray[i-1]+"";
			result[i][4]=(double)bArray[i-1]/sumbArray+"";
		}
		return result;
	}
	
	/**
	 * 将List中的数字按照行取中位数，也就是每一个Number[]取一个中位数
	 * 所以不要求Numbers[]的长度相等。
	 * 排序数量不要太大，别超过了内存限制
	 * @return
	 */
	public static double[] getMediaByRow(List<? extends Number []> lsNum) {
		double[] result = new double[lsNum.size()];
		for (int i = 0; i< lsNum.size() ; i++) {
			Number[] numbers = lsNum.get(i);
			
			int length = numbers.length;
			double[] tmp = new double[length];
			result[i] = StatUtils.percentile(tmp, 50);
		}
		return result;
	}
	
	
	/**
	 * 将List中的数字按照行取中位数，也就是每一个double[]取一个中位数
	 * 所以不要求doubles[]的长度相等。
	 * 排序数量不要太大，别超过了内存限制
	 * @return
	 */
	public static double[] getMediaByRowdou(List<double[]> lsNum) {
		double[] result = new double[lsNum.size()];
		for (int i = 0; i< lsNum.size() ; i++) {
			double[] numbers = lsNum.get(i);
			result[i] = StatUtils.percentile(numbers, 50);
		}
		return result;
	}
	
	/**
	 * 将List中的数字按照列，也就是依次每一行的Number取一个值，算该列的平均数
	 * 所以要求Numbers[]的长度相等。
	 * 排序数量不要太大，别超过了内存限制
	 * @return
	 */
	public static double[] getMediaByCol(List<? extends Number []> lsNum) {
		int length = lsNum.get(0).length;
		double[] result = new double[length];
		for (int i = 0; i< length ; i++) {
			double[] tmpMedia = new double[lsNum.size()];
			for (int j = 0; j < lsNum.size(); j++) {
				Number[] number = lsNum.get(j);
				tmpMedia[j] = (Double) number[i];			
			}
			result[i] = StatUtils.percentile(tmpMedia, 50);
		}
		return result;
	}
	
	
	/**
	 * 将List中的数字按照列，也就是依次每一行的Number取一个值，算该列的平均数
	 * 所以要求doubles[]的长度相等。
	 * 排序数量不要太大，别超过了内存限制
	 * @return
	 */
	public static double[] getMediaByColdou(List<double[]> lsNum) 
	{
		int length = lsNum.get(0).length;
		double[] result = new double[length];
		for (int i = 0; i< length ; i++) {
			double[] tmpMedia = new double[lsNum.size()];
			for (int j = 0; j < lsNum.size(); j++)
			{
				tmpMedia[j] = lsNum.get(j)[i];			
			}
			result[i] = StatUtils.percentile(tmpMedia, 50);
		}
		return result;
	}
	
	/**
	 * 将List中的数字按照列，也就是依次每一行的Number取一个值，算该列的平均数
	 * 所以要求doubles[]的长度相等。
	 * 排序数量不要太大，别超过了内存限制
	 * @return
	 */
	public static double[] getMeanByColdou(List<double[]> lsNum) 
	{
		int length = lsNum.get(0).length;
		double[] result = new double[length];
		for (int i = 0; i< length ; i++) {
			double[] tmpMedia = new double[lsNum.size()];
			for (int j = 0; j < lsNum.size(); j++)
			{
				tmpMedia[j] = lsNum.get(j)[i];			
			}
			result[i] = StatUtils.mean(tmpMedia);
		}
		return result;
	}
	
	/**
	 * 给定一组区域，首先排序，然后将区域挨的很近--小于distance--的区域合并为一个，最后返回合并后的区域list
	 * 排序只按照第一个区间进行排序，从小到大排列
	 * @param lsNum 区域list，每个为double[0]起点坐标。double[1] 终点坐标 必须起点小于终点
	 * @param distance，小于0就默认为0，即只合并重叠的区域
	 * @return
	 */
	public static ArrayList<double[]>  combInterval(List<double[]> lsNum, double distance) 
	{
		Collections.sort(lsNum, new Comparator<double[]>() {
			//从小到大排序
			@Override
			public int compare(double[] o1, double[] o2) {
				if (o1[0] < o2 [0]) 
					return -1;
				else if (o1[0] > o2[0]) 
					return 1;
				else {
					if (o1[1] < o2[1]) 
						return -1;
					else if (o1[1] > o2[1]) 
						return 1;
					else 
						return 0;
				}
			}
		});
		ArrayList<double[]> lsResult = new ArrayList<double[]>();
		int i = 1;double[] tmpResult = lsNum.get(0);
		while (i < lsNum.size()) {
			double[] loc = lsNum.get(i);
			if (tmpResult[1] >= loc[0] - distance) {
				tmpResult[1] = loc[1];
				i++;
			}
			else {
				lsResult.add(tmpResult);
				tmpResult  = loc;
				i++;
			}
		}
		lsResult.add(tmpResult);
		return lsResult;
	}
	
	
	/**
	 * 
	 * 给定一组数，将两个接近距离小于distance的数合并，保留权重大的那一个
	 * 最后返回按照位置进行排序的结果
	 * @param lsNum double[2] 0:数字 1:权重
	 * @param distance 数字的距离，不能小于该值
	 * @param max true 选择权重最大的，min选择权重最小的
	 * @return
	 */
	public static ArrayList<double[]>  combLs(List<double[]> lsNum, double distance, boolean max) {
		Collections.sort(lsNum, new Comparator<double[]>() {
			//从小到大排序
			public int compare(double[] o1, double[] o2) {
				if (o1[0] == o2 [0]) return 0;
				return o1[0] < o2[0] ? -1:1;
			}});
		//最大的一个数
		double bigNum = lsNum.get(lsNum.size() -1)[0];
		double binNum =  (bigNum - lsNum.get(0)[0])/distance;//最后能切割成多少份
		
		int lastInsertNum = 0;
		int insertNum = 0;
		ArrayList<double[]> lsResult = new ArrayList<double[]>();
		for (int i = 0; i < binNum; i++) {
			double[] binNum2 = new double[2]; binNum2[0] = binNum; binNum2[1] = 0;
			lastInsertNum = insertNum;
			insertNum = Collections.binarySearch(lsNum, binNum2,new Comparator<double[]>() {
				public int compare(double[] o1, double[] o2) {
					if (o1[0] == o2 [0]) return 0;
					return o1[0] < o2[0] ? -1:1;
				} });
			//截取相应的区域
			List<double[]> lsTmp = null;
			if (insertNum >= 0) {
				insertNum ++;
				lsTmp = lsNum.subList(lastInsertNum, insertNum);
				
			}
			else {
				insertNum = -insertNum - 1;
				lsTmp = lsNum.subList(lastInsertNum, insertNum);
			}
			double[] tmpResult = getBigestWeight(lsTmp,max);
			if (tmpResult != null) {
				lsResult.add(tmpResult);
			}
		}
		return lsResult;
	}
	
	
	/**
	 * 找到这个序列中权重最大的一项，没有则返回null
	 * @param lsNum 0:数字 1:权重
	 * @param max true 选择权重最大的，min选择权重最小的
	 * @return
	 */
	private static double[] getBigestWeight(List<double[]> lsNum, boolean max)
	{
		double[] result = null;
		if (max) {
			double big = Double.MIN_VALUE;
			for (double[] ds : lsNum) {
				if (ds[1] > big) {
					result = ds;
					big = ds[1];
				}
			}
		}
		else {
			double small = Double.MAX_VALUE;
			for (double[] ds : lsNum) {
				if (ds[1] < small) {
					result = ds;
					small = ds[1];
				}
			}
		}
		return result;
	}
	
//////////////////////////// java 版的 fdr 计算， BH 方法 //////////////////////////////////////////////////////////////////////////////////
	/**
	 * 给定pvaule，获得相应的fdr，用java来计算的<br>
	 * @param lsPvalue
	 * @return 返回跟输入一致顺序的fdrlist
	 * @throws Exception 
	 */
	public static ArrayList<Double> pvalue2Fdr(Collection<Double> lsPvalue) {
		ArrayList<Double[]> lsPvalueInfo = new ArrayList<Double[]>();
		int i = 0;
		for (Double doubles : lsPvalue) {
			Double[] dou = new Double[2];
			dou[0] = (double) i;
			dou[1] = doubles;
			lsPvalueInfo.add(dou);
			i ++;
		}
		
		HashMap<Integer, Double> hashResult = getFDR(lsPvalueInfo);
		ArrayList<Double> lsResult = new ArrayList<Double>();
		int resultSize = lsPvalue.size();
		for (int m = 0; m < resultSize; m++) {
			lsResult.add(hashResult.get(m));
		}
		return lsResult;
	}
	
	
	private static HashMap<Integer, Double> getFDR(ArrayList<Double[]> lsPvalue) {
		// ordening the pvalues.
		Collections.sort(lsPvalue, new Comparator<Double[]>() {
			@Override
			public int compare(Double[] o1, Double[] o2) {
				if (o1 == null && o2 == null) {
					return 0;
				}
				 if (o1== null)return 1;
			 	 if (o2 == null)return -1;
	
				if (o1[1] < o2[1])
					return -1;
				else if (o1[1] == o2[1])
					return 0;
				else
					return 1;
			}
		});
		double[] ordenedPvalues = new double[lsPvalue.size()];
		double[] adjustedPvalues = new double[lsPvalue.size()];
		for (int i = 0; i < ordenedPvalues.length; i++) {
			ordenedPvalues[i] = lsPvalue.get(i)[1];
		}
		
		HashMap<Integer, Double> hashResult = new HashMap<Integer, Double>();
		// calculating adjusted p-values.
		double min = 1;
		double mkprk;
		for (int i = ordenedPvalues.length; i > 0; i--) {
			mkprk = ordenedPvalues.length * ordenedPvalues[i - 1] / i;
			if (mkprk < min) {
				min = mkprk;
			}
			adjustedPvalues[i - 1] = min;
		}
		for (int i = 0; i < adjustedPvalues.length; i++) {
			hashResult.put(lsPvalue.get(i)[0].intValue(), adjustedPvalues[i]);
		}
		return hashResult;
	}
	
}
