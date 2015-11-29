package com.novelbio.base.cmd;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.collect.ArrayListMultimap;
import com.novelbio.base.dataStructure.ArrayOperate;

/**
 * 从cmd的top命令获得的进程信息
 * @author novelbio
 *
 */
public class ProcessInfo {
	private static final Logger logger = Logger.getLogger(ProcessInfo.class);
	/** cpu使用百分比 */
	double cpuUsage;
	/** 内存使用百分比 */
	double memUsage;
	/** 命令名称 */
	String cmdName;
	/** 用户名 */
	String userName;
	/** id */
	int pid;
	/** parentId */
	int ppid;
	/** 进程状态 */
	EnumProcessStatus status;
	String processStatus;
	/** 运行时间，单位为毫秒 */
	long runtime;

	/**
	 * @param infoLine 指定的列
	 * @param mapColNum2Title 列数 对应该列的 名字<br>
	 * 列数从0开始计算
	 */
	public ProcessInfo(String infoLine, Map<Integer, String> mapColNum2Title) {
		String[] ss = infoLine.split(" ");
		int i = 0;
		for (String info : ss) {
			info = info.trim();
			if (info.equals("")) continue;
			
			String title = mapColNum2Title.get(i);
			if (title == null) continue;
			
			if (title.equals("PID")) {
				pid = Integer.parseInt(info);
			} else if (title.equals("USER")) {
				userName = info;
			} else if (title.equals("%CPU")) {
				cpuUsage = Double.parseDouble(info);
			} else if (title.equals("%MEM")) {
				memUsage = Double.parseDouble(info);
			} else if (title.equals("S")) {
				processStatus = info;
				try {
					status = EnumProcessStatus.valueOf(info);
				} catch (Exception e) {
					logger.error("find unknown process status: " + status);
					status = EnumProcessStatus.unKnown;
				}				
			} else if (title.equals("COMMAND")) {
				cmdName = info;
			} else if (title.equals("PPID")) {
				ppid = Integer.parseInt(info);
			} else if (title.equals("TIME+")) {
				setRunTime(info);
			}
			i++;
		}
	}
	
	/** 设定运行时间，输入类似
	 * 623:09.64
	 * @param rumtime
	 */
	private void setRunTime(String rumtime) {
		String[] ss = rumtime.split(":");
		double sec = Double.parseDouble(ss[1]);
		double runTimeSec = Integer.parseInt(ss[0]) * 60 + sec;
		if (sec > 60) {
			throw new RuntimeException("second cannot bigger then 60");
		}
		this.runtime = (long)runTimeSec* 1000;
	}

	
	public double getCpuUsage() {
		return cpuUsage;
	}
	public double getMemUsage() {
		return memUsage;
	}
	public String getCmdName() {
		return cmdName;
	}
	public String getUserName() {
		return userName;
	}
	/** 进程号 */
	public int getPid() {
		return pid;
	}
	/** 父进程号 */
	public int getPpid() {
		return ppid;
	}
	
	public EnumProcessStatus getStatus() {
		return status;
	}
	/** 运行时间，秒为单位 */
	public int getRuntimeSec() {
		return (int)(runtime/1000);
	}
	/** 运行时间，分为单位 */
	public String getRuntimeMin() {
		int secAll = (int)(runtime/1000);
		int min = secAll/60;
		int sec = secAll%60;

		return min + ":" + sec;
	}
	
	public void killProc() {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("kill"); lsCmd.add("-9"); lsCmd.add(pid + "");
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.run();
	}
	
	public static String getTitle() {
		List<String> lsResult = new ArrayList<String>();
		lsResult.add("PID");
		lsResult.add("PPID");
		lsResult.add("CpuUsage");
		lsResult.add("MemUsage");
		lsResult.add("Status");
		lsResult.add("Runtime");
		lsResult.add("CmdName");
		return ArrayOperate.cmbString(lsResult.toArray(new String[0]), "\t");
	}
	
	public String toString() {
		List<String> lsResult = new ArrayList<String>();
		lsResult.add(pid + "");
		lsResult.add(ppid + "");
		lsResult.add(cpuUsage + "");
		lsResult.add(memUsage + "");
		if (status == EnumProcessStatus.unKnown) {
			lsResult.add(processStatus + "needToCheck");
		} else {
			lsResult.add(status + "");
		}
		lsResult.add(runtime + "");
		lsResult.add(cmdName);
		return ArrayOperate.cmbString(lsResult.toArray(new String[0]), "\t");
	}
	
	public static List<ProcessInfo> getLsSubPid(int pid) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("ps"); 
		lsCmd.add("--ppid");lsCmd.add(pid + "");
		lsCmd.add("-o");
		lsCmd.add("pid,ppid,user,%cpu,%mem,s,vsize,comm,user,time");
//		ps -eo pid,ppid,user,%cpu,%mem,s,vsize,comm,user,time

		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setTerminateWriteTo(false);
		cmdOperate.setGetLsStdOut();
		cmdOperate.run();
		List<String> lsStd = cmdOperate.getLsStdOut();
		return getLsPid(lsStd);
	}
	
	/** 给定ps出来的结果，返回具体的信息 */
	private static List<ProcessInfo> getLsPid(List<String> lsTopInfo) {
		List<ProcessInfo> lsResult = new ArrayList<ProcessInfo>();
		boolean startGetInfo = false;
		Map<Integer, String> mapColNum2Title = null;
		for (String string : lsTopInfo) {
			if ((!startGetInfo && string.contains(":")) || string.trim().equals("")) continue;
			
			if (string.trim().contains("PID")) {
				startGetInfo = true;
				mapColNum2Title = getMapColNum2Title(string);
				continue;
			}

			ProcessInfo processInfo = new ProcessInfo(string, mapColNum2Title);
			if (processInfo.getPpid() == 0) continue;
			lsResult.add(processInfo);
		}
		return lsResult;
	}
	
	/** 获得当前正在运行的java程序的pid */
	public static int getPidThis() {
		String name = ManagementFactory.getRuntimeMXBean().getName();  
		// get pid  
		String pid = name.split("@")[0];  
		return Integer.parseInt(pid);
	}
	
	/** 获得当前正在运行的java程序，以及其子pid
	 * 考虑用于container获取每个java程序所使用的资源
	 */
	public static List<ProcessInfo> getLsPid(boolean isGetChild) {
		return getLsPid(getPidThis(), isGetChild);
	}
	
	public static List<ProcessInfo> getLsPid(int pid, boolean isGetChild) {
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("ps"); lsCmd.add("-eo");
		lsCmd.add("pid,ppid,user,%cpu,%mem,s,vsize,comm,user,time");
//		ps -eo pid,ppid,user,%cpu,%mem,s,vsize,comm,user,time

		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setGetLsStdOut();
		cmdOperate.setNeedLog(false);
		cmdOperate.run();
		List<String> lsStd = cmdOperate.getLsStdOut();
		return getLsPid(lsStd, pid, isGetChild);
	}
	
	/**
	 * 给定top出来的结果，指定pid，提取相关的进程
	 * @param lsTopInfo
	 * @param pid 指定的进程
	 * @param isGetChild 是否提取该进程的子进程(第归提取)
	 * @return
	 */
	private static List<ProcessInfo> getLsPid(List<String> lsTopInfo, int pid, boolean isGetChild) {
		List<ProcessInfo> lsResult = new ArrayList<ProcessInfo>();
		ArrayListMultimap<Integer, ProcessInfo> mapPid2ProcInfo = ArrayListMultimap.create();
		boolean startGetInfo = false;
		Map<Integer, String> mapColNum2Title = null;
		for (String string : lsTopInfo) {
			if ((!startGetInfo && string.contains(":")) || string.trim().equals("")) continue;
			
			if (string.trim().contains("PID")) {
				startGetInfo = true;
				mapColNum2Title = getMapColNum2Title(string);
				continue;
			}

			ProcessInfo processInfo = new ProcessInfo(string, mapColNum2Title);
			if (processInfo.getPpid() == 0) continue;
			if (processInfo.getPid() == pid) {
				lsResult.add(processInfo);
				continue;
			}
			mapPid2ProcInfo.put(processInfo.getPpid(), processInfo);
		}
		if (isGetChild) {
			lsResult.addAll(getLsChildPid(lsResult, mapPid2ProcInfo));
		}
		return lsResult;
	}
	
	private static List<ProcessInfo> getLsChildPid(List<ProcessInfo> lsParent, ArrayListMultimap<Integer, ProcessInfo> mapPid2ProcInfo) {
		List<ProcessInfo> lsChildProc = new ArrayList<ProcessInfo>();
		for (ProcessInfo proc : lsParent) {
			lsChildProc.addAll(mapPid2ProcInfo.get(proc.getPid()));
		}
		if (!lsChildProc.isEmpty()) {
			lsChildProc.addAll(getLsChildPid(lsChildProc, mapPid2ProcInfo));
		}
		return lsChildProc;
	}
	
	
	private static Map<Integer, String> getMapColNum2Title(String title) {
		Map<Integer, String> mapColNum2Title = new HashMap<>();
		title = title.trim();
		String[] ss = title.split(" ");
		int i = 0;
		for (String info : ss) {
			info = info.trim();
			if (info.equals("")) continue;
			mapColNum2Title.put(i, info);
			i++;
		}
		return mapColNum2Title;
	}
}
