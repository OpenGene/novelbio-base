package com.novelbio.base.dataStructure;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

public class ArrayOperate {
	private static final Logger logger = Logger.getLogger(ArrayOperate.class);
	/**
	 * 合并字符串数组
	 * @param ss
	 * @param sep
	 * @return
	 */
	public static String cmbString(String[] ss, String sep) {
		String result = "";
		if (ss.length < 1) {
			return "";
		}
		result = ss[0];
		for (int i = 1; i < ss.length; i++) {
			result = result + sep + ss[i];
		}
		return result;
	}
	public static<T> ArrayList<T> converArray2List(T[] array) {
		ArrayList<T> lsResult = new ArrayList<T>();
		for (T t : array) {
			lsResult.add(t);
		}
		return lsResult;
	}
	public static<T> T[] converList2Array(List<T> ls) {
		@SuppressWarnings("unchecked")
		T[]  result = (T[]) Array.newInstance(ls.get(0).getClass(),ls.size());
		int index = 0;
		for (T t : ls) {
			result[index] = t;
			index++;
		}
		return result;
	}
	/**
	 * 给定lsString，将lsString看作ArrayList-String[]，纵向将其合并为String[][]，也就是类似cbind
	 * @param lsStrings
	 * @return
	 */
	public static String[][] combCol(ArrayList<String[][]> lsStrings) {
		int rowNum=lsStrings.get(0).length;
		int columnNum=lsStrings.size();
		String[][] result=new String[rowNum][columnNum];
		for (int i = 0; i < columnNum; i++) 
		{
			for (int j = 0; j < rowNum; j++) 
			{
				result[j][i]=lsStrings.get(i)[j][0];
			}
		}
		return result;
	}
	/**
	 * 二维[][]的合并，给定AT[][]和BT[][]
	 * 将AT[][]和BT[][]合并，可以指定BT插入在AT的哪一列中
	 * <b>注意，AT和BT的类型必须相等，行数必须相等,也就是第一维必须相等</b>
	 * @param AT
	 * @param BT
	 * @param instNum 插入到AT的第几列之后，是AT的实际列,如果instNum<1则仅仅将BT并到AT的后面。
	 * @return
	 * 
	 */
	public static <T> T[][] combArray(T[][] AT,T[][] BT,int instNum) {
		int rowNum=AT.length;
		int colNum=AT[0].length+BT[0].length;
		if (instNum<1) {
			instNum=AT[0].length;
		}
		instNum--;
		//通过反射的方法新建数组
		T[][]  result = (T[][]) Array.newInstance(AT.getClass().getComponentType().getComponentType(),rowNum,colNum);
		for (int i = 0; i < rowNum; i++) 
		{
			for (int j = 0; j < colNum; j++) 
			{
				if (j<=instNum) {
					result[i][j]=AT[i][j];
				}
				else if (j>instNum&&j<=instNum+BT[0].length) {
					result[i][j]=BT[i][j-instNum-1];
				}
				else
				{
					result[i][j]=AT[i][j-BT[0].length];
				}
			}
		}
		return result;
	}
	/**
	 * 二维[][]的合并，给定AT[][]和BT[][]
	 * 将AT[][]和BT[][]合并，可以指定BT插入在AT的哪一列中
	 * <b>注意，AT和BT的类型必须相等，行数必须相等,也就是第一维必须相等</b>
	 * @param AT
	 * @param BT
	 * @param instNum 插入到AT的第几列之后，是AT的实际列,如果instNum<1则仅仅将BT并到AT的后面。
	 * @return
	 * 
	 */
	public static <T> ArrayList<T[]> combArray(List<T[]> lsAT,List<T[]> lsBT,int instNum) {
		int rowNum = lsAT.size();
		int colNum = lsAT.get(0).length+lsBT.get(0).length;
		if (instNum<1) {
			instNum = lsAT.get(0).length;
		}
		instNum--;
		//通过反射的方法新建数组
		ArrayList<T[]> lsResult = new ArrayList<T[]>();
		T[][]  result = (T[][]) Array.newInstance(lsAT.get(0).getClass().getComponentType(),rowNum,colNum);
		for (int i = 0; i < rowNum; i++) 
		{
			for (int j = 0; j < colNum; j++) 
			{
				if (j<=instNum) {
					result[i][j]=lsAT.get(i)[j];
				}
				else if (j>instNum&&j<=instNum+lsBT.get(0).length) {
					result[i][j]=lsBT.get(i)[j-instNum-1];
				}
				else
				{
					result[i][j]=lsAT.get(i)[j-lsBT.get(i).length];
				}
				lsResult.add(result[i]);
			}
		}
		return lsResult;
	}
	
	public static boolean compareString(String str1, String str2) {
		if (str1 == str2) {
			return true;
		}
		else if (str1 == null && str2 != null) {
			return false;
		}
		else if (!str1.equals(str2)) {
			return false;
		}
		return true;
	}
	
	/**
	 * @deprecated
	 * 采用{@link combArray}取代
	 * String[][]的合并，给定Astring[][]和Bstring[][]
	 * 将Aobject[][]和Bobject[][]合并，可以指定Bobject插入在Aobject的哪一列后
	 * <b>注意，Aobject和Bobject的类型必须相等，行数必须相等,也就是第一维必须相等</b>
	 * @param Aobject
	 * @param Bobject
	 * @param instNum 插入到Aobject的第几列之后，是Aobject的实际列,如果instNum<1则仅仅将Bobject并到Aobject的后面。
	 * @return
	 */
	public static String[][] combStrArray(String[][] Astring,String[][] Bstring,int instNum) {
		int rowNum=Astring.length;
		int colNum=Astring[0].length+Bstring[0].length;
		if (instNum<1) {
			instNum=Astring[0].length;
		}
		instNum--;
		
		String[][] result=new String[rowNum][colNum];
		for (int i = 0; i < rowNum; i++) 
		{
			for (int j = 0; j < colNum; j++) 
			{
				if (j<=instNum) {
					result[i][j]=Astring[i][j];
				}
				else if (j>instNum&&j<=instNum+Bstring[0].length) {
					result[i][j]=Bstring[i][j-instNum-1];
				}
				else
				{
					result[i][j]=Astring[i][j-Bstring[0].length];
				}
			}
		}
		return result;
	}
	public static<T> LinkedHashSet<T> removeDuplicate(Collection<T> colToverrideHashCode) {
		LinkedHashSet<T> setRemoveDuplicate = new LinkedHashSet<T>();
		for (T t : setRemoveDuplicate) {
			setRemoveDuplicate.add(t);
		}
		return setRemoveDuplicate;
	}
	
	/**
	 * 将数组中小于smallNum的项目全部删除
	 * @param column
	 * @param smallNum
	 * @return
	 */
	public static int[] removeSmallValue(int[] column, int smallNum) {
		 ArrayList<Integer> lsReadColNum = new ArrayList<Integer>();
		 for (int i : column) {
			if (i < smallNum) {
				continue;
			}
			lsReadColNum.add(i);
		 }
		 int[] column2 = new int[lsReadColNum.size()];
		 for (int i = 0; i < lsReadColNum.size(); i++) {
			 column2[i] = lsReadColNum.get(i);
		 }
		 return column2;
	}
	
	/**
	 * String[]的合并，给定Astring[]和Bstring[]
	 * 将Astring[]和Bstring[]合并，可以指定Bstring插入在Astring的哪一列后
	 * <b>注意，Astring和Bstring的类型必须相等</b>
	 * @param Astring
	 * @param Bstring
	 * @param instNum 插入到Astring的第几列之后，是Astring的实际列,如果instNum<1则仅仅将Bobject并到Aobject的后面。
	 * @return
	 */
	public static<T> T[] combArray(T[] Aarray,T[] Barray,int instNum) {
		if (instNum<1) {
			instNum=Aarray.length;
		}
		instNum--;
		T[] result=(T[]) Array.newInstance(Aarray.getClass().getComponentType(), Aarray.length + Barray.length);//new T[Astring.length+Bstring.length];
		for (int i = 0; i < result.length; i++) {
			if (i<=instNum) {
				result[i]=Aarray[i];
			}
			else if (i>instNum&&i<=instNum+Barray.length) {
				result[i]=Barray[i-instNum-1];
			}
			else {
				result[i]=Aarray[i-Barray.length];
			}
		}
		return result;
	}
	
	/**
	 * 删除数组中的一些项目
	 * @param <T>
	 * @param Aarray 数组
	 * @param deletNum 需要删除哪几项，从0开始计算，如果超出数组项，则忽略
	 * @return
	 */
	public static<T> T[] deletElement(T[] Aarray,int[] deletNum) {
		TreeSet<Integer> treeRemove = new TreeSet<Integer>();
		for (int i : deletNum) {
			if (i < 0 || i >= Aarray.length) {
				continue;
			}
			treeRemove.add(i);
		}
		
		T[] result=(T[]) Array.newInstance(Aarray.getClass().getComponentType(), Aarray.length - treeRemove.size());//new T[Astring.length+Bstring.length];
		int resultNum = 0;
		for (int i = 0; i < Aarray.length; i++) {
			if (treeRemove.contains(i)) {
				continue;
			}
			result[resultNum] = Aarray[i];
			resultNum++ ;
		}
		return result;
	}
	/**
	 * <b>没有添加范围检测功能</b>
	 * <b>同一列最多只能添加删除各一次</b><br>
	 * 添加或者删除数组中的一些项目
	 * @param <T>
	 * @param Aarray 数组
	 * @param lsIndelInfo 不要出现小于0项。需要删除哪几项，从0开始计算，<br>
	 * 	<b>0：</b> 添加或删除哪一项<br>
	 * 正数：添加，<b>添加在指定位置的前面</b>，<br>
	 * 负数：删除，如果超出数组项，则忽略<br>
	 * <b>1：</b>添加几个元素，或删除该元素<br>
	 * <b>正数为添加，添加可以多个，当为最后一项+1时，只能添加不能删除<br>负数为删除，只能删除一个</b>
	 * @param filling 默认填充的元素
	 * @return
	 */
	public static<T> T[] indelElement(T[] Aarray,ArrayList<int[]> lsIndelInfo, T filling) {
		// 0：修正第几位，1.负数删除，正数在前面添加一位
		HashMap<Integer, TreeSet<Integer>> hashIndelInfo = new HashMap<Integer, TreeSet<Integer>>();
		for (int[] i : lsIndelInfo) {
			if (i[0] > Aarray.length || i[0] < 0)
				continue;
			if (hashIndelInfo.containsKey(Math.abs(i[0]))) {
				TreeSet<Integer> lsDetail = hashIndelInfo.get(Math.abs(i[0]));
				lsDetail.add(i[1]);
			}
			else {
				TreeSet<Integer> lsDetail = new TreeSet<Integer>();
				lsDetail.add(i[1]);
				hashIndelInfo.put(Math.abs(i[0]), lsDetail);
			}
		}
		///////// 计算最终数组长度 //////////////////
		int finalLen = Aarray.length;
		for (TreeSet<Integer> treeSet : hashIndelInfo.values()) {
			for (Integer integer : treeSet) {
				if (integer < 0)
					finalLen --;//负数表示仅将该位点删除
				else
					finalLen = finalLen + integer;//正数表示在该位点之前添加若干个空位
			}
		}
		T[] result=(T[]) Array.newInstance(Aarray.getClass().getComponentType(), finalLen);//new T[Astring.length+Bstring.length];
		int resultNum = 0;//输出array的坐标
		for (int i = 0; i < Aarray.length; i++) {
			boolean flagDel = false;//是否跳过该ID
			if (hashIndelInfo.containsKey(i)) {
				//反向排列，也就是从大到小排序
				NavigableSet<Integer> treeIndelInfo = hashIndelInfo.get(i).descendingSet();
				for (Integer integer : treeIndelInfo) {
					if (integer > 0) {
						resultNum = resultNum + integer;
					}
					else {
						flagDel = true;
					}
				}
			}
			//如果没有跳过
			if (!flagDel) {
				result[resultNum] = Aarray[i];
				resultNum++ ;
			}
		}
		for (int i = 0; i < result.length; i++) {
			if (result[i] == null) {
				result[i] = filling;
			}
		}
		return result;
	}
	/**
	 * 用hash的方法来合并两个List<br>
	 * 给定lsA、lsB<br>
	 * 其中lsA的第AcolNum列没有重复（从0开始计算）<br>
	 * 将lsA的第AcolNum（从0开始计算）和lsB的第BcolNum（从0开始计算）进行比较<br>
	 * 如果相同，则将AcolNum全部添加到lsB后面，最后返回添加好的lsA<br>
	 * @return
	 */
	public static ArrayList<String[]> combArrayListHash(List<String[]> lsA ,List<String[]> lsB, int AcolNum, int BcolNum) 
	{
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		Hashtable<String, String[]> hashLsA = new Hashtable<String, String[]>();
		for (String[] strings : lsA) {
			String tmpKey = strings[AcolNum];
			hashLsA.put(tmpKey.trim(), strings);
		}
		for (String[] strings : lsB) {
			String tmpKeyB = strings[BcolNum];
			String[] tmpA = hashLsA.get(tmpKeyB.trim());
			if (tmpA == null) {
				logger.error("no lsA element equals lsB: "+tmpKeyB);
				continue;
			}
			String[] tmpResult = combArray(strings, tmpA, 0);
			lsResult.add(tmpResult);
		}
		return lsResult;
	}
	

	/**
	 * 用之前要看清楚指定的column是否在ls内 <br>
	 * 给定List，获得其中指定的某几列,获得的某几列按照指定的列进行排列,<b>从0开始计数</b><br>
	 * 或者去除指定的某几列<br>
	 * 用一个boolean参数来指定<br>
	 * @return
	 */
	public static ArrayList<String[]> listCope(ArrayList<String[]> ls, int[] colNum, boolean include) 
	{
		if (include) {
			return listCope(ls, colNum);
		}
		else {
			HashSet<Integer> hashCol = new HashSet<Integer>();
			for (int i = 0; i < colNum.length; i++) {
				hashCol.add(colNum[i]);
			}
			int[] colNumResult = new int[ls.get(0).length - colNum.length]; 
			int k=0;//给最后结果计数，也就是需要哪几列
			//遍历所有列数
			for (int i = 0; i < ls.get(0).length; i++) {
				//如果该列在去除项中，则跳过
				if (hashCol.contains(i))
				{
					continue;
				}
				colNumResult[k] = i; k++;
			}
			return listCope(ls, colNumResult);
		}
	}
	/**
	 * 给定List，获得其中指定的某几列,获得的某几列按照指定的列进行排列，从0开始记数<br>
	 * @param ls
	 * @param colNum
	 * @return
	 */
	private static ArrayList<String[]> listCope(ArrayList<String[]> ls, int[] colNum) {
		ArrayList<String[]> lsResult = new ArrayList<String[]>();
		for (String[] strings : ls) {
			String[] tmpString = new String[colNum.length];
			for (int i = 0; i < tmpString.length; i++) {
				tmpString[i] = strings[colNum[i]];
			}
			lsResult.add(tmpString);
		}
		return lsResult;
	}
	
	
	/**
	 * 两个list取交集,注意lsA和lsB里面不要有重复项
	 * @return
	 */
	public static ArrayList<String> getCoLs(List<String>  lsA, List<String> lsB) {
		ArrayList<String> lsResult = new ArrayList<String>();
		HashSet<String> hashA = new HashSet<String>();
		for (String string : lsA) {
			hashA.add(string);
		}
		for (String string : lsB) {
			if (hashA.contains(string)) {
				lsResult.add(string);
			}
		}
		return lsResult;
	}
	
	/**
	 * 给定一个数组，以及它的中点坐标，和上游下游坐标，切割或者扩充该数组
	 * @param array 数组
	 * @param center 中心位置，譬如2的话，就是该数组的第二位
	 * @param up 中心上面的元素个数
	 * @param down 中心下面的元素个数
	 * @param thisdefault 默认值，就是没有的地方用什么填充
	 * @return
	 * 最后返回长度为 up+1+down的array
	 */
	public static double[] cuttArray(double[] array, int center, int up, int down, double thisdefault) {
		center--;
		double[] result = new double[up + down +1];
		for (int i = 0; i < result.length; i++) {
			result[i] = thisdefault;
		}
		int resultCenter = up;
		for (int i = center; i >= 0; i--) {
			if (resultCenter < 0) {
				break;
			}
			result[resultCenter] = array[i];
			resultCenter--;
		}
		resultCenter = up + 1;
		for (int i = center + 1; i < array.length; i++) {
			if (resultCenter - up > down) {
				break;
			}
			result[resultCenter] = array[i];
			resultCenter++;
		}
		return result;
	}
	
	/**
	 * 颠倒数组，直接性将传入的数组倒置，不返回东西
	 * @param array
	 */
	public static void convertArray(int[] array) {
		int tmpValue=0;
		int arrayLength=array.length;
		for (int i = 0; i < arrayLength/2; i++) {
			tmpValue=array[arrayLength-1-i];
			array[arrayLength-1-i]=array[i];
			array[i]=tmpValue;
		}
	}
	
	/**
	 * 颠倒数组，直接性将传入的数组倒置，不返回东西
	 * @param array
	 */
	public static void convertArray(double[] array) {
		double tmpValue=0;
		int arrayLength=array.length;
		for (int i = 0; i < arrayLength/2; i++) {
			tmpValue=array[arrayLength-1-i];
			array[arrayLength-1-i]=array[i];
			array[i]=tmpValue;
		}
	}
	
	/**
	 * 颠倒数组，直接性将传入的数组倒置，不返回东西
	 * @param array
	 */
	public static<T> void convertArray(T[] array) {
		T tmpValue=null;
		int arrayLength=array.length;
		for (int i = 0; i < arrayLength/2; i++) {
			tmpValue=array[arrayLength-1-i];
			array[arrayLength-1-i]=array[i];
			array[i]=tmpValue;
		}
	}
	/**
	 * 将hashmap的key提取出来
	 * @param <K> key
	 * @param <V> value
	 * @param hashMap
	 * 	 * 没有返回null
	 */
	public static<K,V> ArrayList<K> getArrayListKey(Map<K, V> hashMap) {
		if (hashMap == null || hashMap.size() == 0) {
			return null;
		}
		ArrayList<K> lsResult = new ArrayList<K>();
		Set<K> keys = hashMap.keySet();
		for(K key:keys)
		{
			lsResult.add(key);
		}
		return lsResult;
	}
	/**
	 * 将hashmap的value提取出来
	 * @param <K> key
	 * @param <V> value
	 * @param hashMap
	 * 	 * 没有返回 空的 list
	 */
	public static<K,V> ArrayList<V> getArrayListValue(Map<K, V> hashMap) {
		if (hashMap == null || hashMap.size() == 0) {
			return new ArrayList<V>();
		}
		ArrayList<V> lsResult = new ArrayList<V>();
		Collection<V> values = hashMap.values();
		for(V value:values) {
			lsResult.add(value);
		}
		return lsResult;
	}
	
	/**
	 * 将hashset的value提取出来
	 * @param <K> key
	 * @param hashset
	 * 没有返回一个空的arraylist
	 */
	public static<K> ArrayList<K> getArrayListValue(Set<K> hashset) {
		if (hashset == null || hashset.size() == 0) {
			return new ArrayList<K>();
		}
		
		ArrayList<K> lsResult = new ArrayList<K>();
		for(K value:hashset)
		{
			lsResult.add(value);
		}
		return lsResult;
	}
	
	/**
	 * 复制数组
	 * @param <T>
	 * @param array
	 * @return
	 */
	public static<T> T[] copyArray(T[] array) {
		return copyArray(array, array.length);
	}
	/**
	 * using {@link #indelElement(Object[], int[])} replace<br>
	 * 复制数组
	 * @param <T>
	 * @param array
	 * @param Length 将array的Length位复制给结果array，如果length > array.length，则延长结果array
	 * @return
	 * 最后生成Length长度的array
	 */
	public static<T> T[] copyArray(T[] array, int Length) {
		T[] result=(T[]) Array.newInstance(array.getClass().getComponentType(), Length);
		for (int i = 0; i < array.length; i++) {
			if (i >= Length) {
				continue;
			}
			result[i] = array[i];
		}
		return result;
	}
	/**
	 * 复制数组
	 * @param <T>
	 * @param array
	 * @param Length 将array的Length位复制给结果array，如果Length > array.length，则延长结果array
	 * @param 最后复制靠前还是靠后，靠前 infoXXX，靠后XXXinfo
	 * @return
	 * 最后生成Length长度的array
	 */
	public static<T> T[] copyArray(T[] array, int Length,boolean start) {
		T[] result=(T[]) Array.newInstance(array.getClass().getComponentType(), Length);
		if (start) {
			for (int i = 0; i < array.length; i++) {
				if (i >= Length) {
					continue;
				}
				result[i] = array[i];
			}
		}
		else
		{
			int j = 0;
			for (int i = array.length - 1; i >= 0; i--) {
				j ++;
				if (j >= Length) {
					continue;
				}
				result[i] = array[i];
			}
		}
		return result;
	}
	/**
	 * 比较两个区域之间的overlap的数值和比例
	 * 数组必须只有两个值，并且是闭区间
		 * 结果信息：
		 * 0：位置情况,总共6种情况， 0：一致 1：数组1在前  2：数组2在前 <br>
		 * 1：overlap的bp<br>
		 * 2：overlap占1的比值<br>
		 * 3：overlap占2的比值<br>
		 */
	public static double[] cmpArray(double[] region1, double[] region2) {
		/**
		 * 结果信息：
		 * 0：位置情况,总共6种情况， 0：一致 1：数组1在前  2：数组2在前
		 * 1：overlap的bp
		 * 2：overlap占1的比值
		 * 3：overlap占2的比值
		 */
		double[] result = new double[4];

		double[] region1m = new double[2];
		region1m[0] = Math.min(region1[0], region1[1]);
		region1m[1] = Math.max(region1[0], region1[1]);
		double lenReg1 = region1m[1] - region1m[0] + 1;
		
		
		double[] region2m = new double[2];
		region2m[0] = Math.min(region2[0], region2[1]);
		region2m[1] = Math.max(region2[0], region2[1]);
		double lenReg2 = region2m[1] - region2m[0] + 1;
		//equal
		//   |--------|
		//   |--------|
		if (region1m[0] == region2m[0] && region1m[1] == region2m[1]) {
			result[0] = 0;
			result[1] = region1m[1] - region2m[0] + 1;
			result[2] = 1;
			result[3] = 1;
		}
		//overlap
		else if (region1m[0] <= region2m[0] && region1m[1] > region2m[0]) {
			//      0---------1   region2m               2
			//  0-------1         region1m 
			if (region1m[1] <= region2m[1]) {
				result[0] = 2;
				result[1] = region1m[1] - region2m[0] + 1;
				result[2] = result[1]/lenReg1;
				result[3] = result[1]/lenReg2;
			}
			//     |----------|       region2m            4
			//  |-----------------|   region1m
			else {
				result[0] = 4;
				result[1] = lenReg2;
				result[2] = lenReg2/lenReg1;
				result[3] = 1;
			}
		}
		else if (region1m[0] > region2m[0] && region1m[0] < region2m[1]) {
			//   |---------------|   region2m               3
			//        |-------|      region1m
			if (region1m[1] <= region2m[1]) {
				result[0] = 3;
				result[1] = lenReg1;
				result[2] = 1;
				result[3] = lenReg1/lenReg2;
			}
			//   0---------1           region2m            5
			//        0----------1     region1m
			else {
				result[0] = 5;
				result[1] = region2m[1] - region1m[0] + 1;
				result[2] = result[1]/lenReg1;
				result[3] = result[1]/lenReg2;
			}
		}
		//before
		//                   |------|   region2m             1
		//         |------|             region1m
		else if (region1m[1] <= region2m[0]) {
			result[0] = 1;
			result[1] = 0;
			result[2] = 0;
			result[3] = 0;
		}
		//after 
		//       |------|             region2m             6
		//                 |------|   region1m
		else if (region1m[0] >= region2m[1] ) {
			result[0] = 6;
			result[1] = 0;
			result[2] = 0;
			result[3] = 0;
		}
		else {
			logger.error("出现未知错误，不可能存在的region特征："+ region1m[0] + " " +region1m[1 ] + "     " + region2m[0] + " "+ region2m[1]);
			result[0] = -1;
			result[1] = -1;
			result[2] = -1;
			result[3] = -1;
		}
	
		return result;
	}

	/**
	 * 比较的内容和cmpArray一模一样，只不过比较的时候将大小反过来而已
	 * 大的在前小的在后
	 * 比较两个区域之间的overlap的数值和比例
	 * 数组必须只有两个值
	 * 
	 */
	public static double[] cmpArrayTrans(double[] region1, double[] region2) {
		/**
		 * 结果信息：
		 * 0：位置情况， 0：一致 1：数组1在前  2：数组2在前
		 * 1：overlap的bp
		 * 2：overlap占1的比值
		 * 3：overlap占2的比值
		 * 4：
		 */
		double[] result = new double[3];

		double[] region1m = new double[2];
		region1m[0] = Math.max(region1[0], region1[1]);
		region1m[1] = Math.min(region1[0], region1[1]);
		double lenReg1 = region1m[0] - region1m[1] + 1;
		double[] region2m = new double[2];
		region2m[0] = Math.max(region2[0], region2[1]);
		region2m[1] = Math.min(region2[0], region2[1]);
		double lenReg2 = region2m[0] - region2m[1] + 1;
		//equal
		//   |--------|
		//   |--------|
		if (region1m[0] == region2m[0] && region1m[1] == region2m[1]) {
			result[0] = 0;
		}
		//overlap
		else if (region1m[1] <= region2m[0] && region1m[0] > region2m[0]) {
			//  1----------0             region2m
			//         1--------0        region1m 
			if (region1m[1] >= region2m[1]) {
				result[0] = 2;
				result[1] = region2m[0] - region1m[1] + 1;
				result[2] = result[1]/lenReg1;
				result[3] = result[1]/lenReg2;
			}
			//     1----------0       region2m
			//  1------------------0   region1m
			else {
				result[0] = 4;
				result[1] = lenReg2;
				result[2] = lenReg2/lenReg1;
				result[3] = 1;
			}
		}
		else if (region1m[0] < region2m[0] && region1m[0] > region2m[1]) {
			//   1---------------0  region2m
			//        1-------0     region1m
			if (region1m[1] >= region2m[1]) {
				result[0] = 3;
				result[1] = lenReg1;
				result[2] = 1;
				result[3] = lenReg1/lenReg2;
			}
			//            1---------0  region2m
			//       1----------0     region1m
			else {
				result[0] = 5;
				result[1] = region1m[0] - region2m[1] + 1;
				result[2] = result[1]/lenReg1;
				result[3] = result[1]/lenReg2;
			}
		}
		//before
		//     1------0              region2m
		//                1------0    region1m
		else if (region1m[1] >= region2m[0]) {
			result[0] = 1;
			result[1] = 0;
			result[2] = 0;
			result[3] = 0;
		}
		//after
		//                  1------0       region2m
		//        1------0                 region1m
		else if (region1m[0] <= region2m[1] ) {
			result[0] = 6;
			result[1] = 0;
			result[2] = 0;
			result[3] = 0;
		}
		else {
			logger.error("出现未知错误，不可能存在的region特征："+ region1m[0] + " " +region1m[1 ] + "     " + region2m[0] + " "+ region2m[1]);
			result[0] = -1;
			result[1] = -1;
			result[2] = -1;
			result[3] = -1;
		}
		return result;
	}
}
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

