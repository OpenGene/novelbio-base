package com.novelbio.base.nbcReport.Params;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * GOAnalysis参数对象类，记录结果报告所需要的参数
 * 
 * @author novelbio
 * 
 */
public class ReportGOAll extends ReportBase {
	private List<ReportGO> lsReportGOs = new ArrayList<ReportGO>();
	private Map<String, Object> mapKey2Params = new HashMap<String, Object>();
	
	@Override
	public EnumReport getEnumReport() {
		return EnumReport.GOAnalysis;
	}


	@Override
	public Map<String, Object> addParamMap() {
		if (lsReportGOs.size()>0) {
			ReportGO reportGO = lsReportGOs.get(0);
			mapKey2Params.put("testMethod", reportGO.getTestMethod());
			mapKey2Params.put("finderCondition", reportGO.getFinderCondition());
		}
		mapKey2Params.put("lsReportGOs", lsReportGOs);
		return mapKey2Params;
	}
	
	public void setLsReportGOs(List<ReportGO> lsReportGOs) {
		this.lsReportGOs = lsReportGOs;
	}
}
