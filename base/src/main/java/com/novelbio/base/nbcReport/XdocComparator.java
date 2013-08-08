package com.novelbio.base.nbcReport;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XdocComparator implements Comparator<String> {
	static Map<String, Integer> mapFilename2Num = new HashMap<String, Integer>();

	private static Map<String, Integer> getMapFilename2Num() {
		if (mapFilename2Num.size() > 0) {
			return mapFilename2Num;
		}
		List<String> lsFileName = new ArrayList<String>();
		lsFileName.add("Quality-Control_result");
		lsFileName.add("Mapping_result");
		lsFileName.add("Difference Expression_result");
		lsFileName.add("GO-Analysis_result");
		lsFileName.add("GO-Trees_result");
		lsFileName.add("PathWay-Analysis_result");
		lsFileName.add("Gene-Act-Network_result");
		lsFileName.add("Gene-Act-Network_result");
		lsFileName.add("miRNA-Target-Network_result");
		lsFileName.add("Pathway-Act-network_result");
		lsFileName.add("Co-Exp-Net_LncRNA_result");
		int i = 0;
		for (String string : lsFileName) {
			mapFilename2Num.put(string, i);
			i++;
		}
		return mapFilename2Num;
	}
	
	private int getFileNum(String fileName) {
		Integer num = getMapFilename2Num().get(fileName);
		if (num == null) {
			num = Integer.MAX_VALUE;
		}
		return num;
	}
	
	@Override
	public int compare(String o1, String o2) {
		String[] names1 = o1.split("、|,|，", 0);
		String[] names2 = o1.split("、|,|，", 0);
		Integer num1 = null, num2 = null;
		try { num1 = Integer.parseInt(names1[0]);} catch (Exception e) {}
		try { num2 = Integer.parseInt(names2[0]);} catch (Exception e) {}
		if (num1 != null && num2 != null) {
			return num1.compareTo(num2);
		}
		if (num1 == null && num2 !=  null) {
			return 1;
		} else if (num1 != null && num2 == null) {
			return -1;
		} else {
			num1 = getFileNum(o1);
			num2 = getFileNum(o2);
		}
		return num1.compareTo(num2);
	}
	
}
