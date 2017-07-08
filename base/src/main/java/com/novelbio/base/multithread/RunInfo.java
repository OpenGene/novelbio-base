package com.novelbio.base.multithread;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RunInfo {
	String name;
	String description;
	double process;
	
	Map<String, List<String>> mapParam2LsInfo = new LinkedHashMap<>();
	Map<String, List<Double>> mapParam2LsProcess = new LinkedHashMap<>();
	
	public void setName(String name) {
		this.name = name;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setProcess(double process) {
		this.process = process;
	}
	
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	public double getProcess() {
		return process;
	}
	
	public synchronized void addParam2Info(String param, String info) {
		List<String> lsInfo = mapParam2LsInfo.get(param);
		if (lsInfo == null) {
			lsInfo = new ArrayList<>();
			mapParam2LsInfo.put(param, lsInfo);
		}
		lsInfo.add(info);
	}
	public synchronized void addParam2Process(String param, double process) {
		List<Double> lsInfo = mapParam2LsProcess.get(param);
		if (lsInfo == null) {
			lsInfo = new ArrayList<>();
			mapParam2LsProcess.put(param, lsInfo);
		}
		lsInfo.add(process);
	}
	
	public List<String> getLsInfo(String param) {
		return mapParam2LsInfo.get(param);
	}
	
	public List<Double> getLsProcess(String param) {
		return mapParam2LsProcess.get(param);
	}
	
}
