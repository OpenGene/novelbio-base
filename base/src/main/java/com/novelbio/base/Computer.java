package com.novelbio.base;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataStructure.PatternOperate;
import com.novelbio.base.fileOperate.FileOperate;

/**
 * 计算机的实体类
 * 
 * @author novelbio
 * 
 */
public class Computer implements Serializable {
	private static final Logger logger = Logger.getLogger(Computer.class);
	private static final long serialVersionUID = 1943457765445L;
	static final int minRunTaskMem = 10;//单位为MB 
	static transient Sigar sigar;
	protected transient static int memToBeUsed = 5000;
	protected transient static int cpuToBeUsed = 2;
	
	/** ip地址 */
	protected String ip;
	/** 计算机的名字 如:slaver1、master1.. */
	protected String name;
	protected int cpuCoreNum;
	/** 总内存数，以MB为单位 */
	protected int memoryAll;

	
	/** 数据库里记载的空闲cpu数量 */
	protected int cpuCoreFreeInDB;
	/** 实际空间cpu数量 */
	protected int cpuCoreFreeReal;
	/** 数据库里记载的空闲内存数量 */
	protected int memoryFreeDB;
	/** 实际空闲内存数量 */
	protected int memoryFreeReal;
	
	/** 虽然是单例，不过序列化之后依然可以产生多例 */
	private static Computer computer;
	
	protected Computer() {}
	
	public void initial() {
		if (sigar == null) {
			sigar = new Sigar();
		}
		try {
			Mem mem = sigar.getMem();
			memoryAll = (int) (mem.getTotal() / (1024L*1024L));
			cpuCoreNum = sigar.getCpuInfoList()[0].getTotalCores();
			memoryFreeDB = (int) (mem.getActualFree() / (1024L*1024L)) - memToBeUsed;
			cpuCoreFreeInDB = cpuCoreNum - cpuToBeUsed;
		} catch (SigarException e) {
			e.printStackTrace();
		}
		try {
			InetAddress[] IP = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
			for (InetAddress inetAddress : IP) {
				if (inetAddress instanceof Inet4Address && !inetAddress.getHostAddress().startsWith("127") ) {
					name = inetAddress.getHostName();
				}
			}
			if (name == null) {
				name = InetAddress.getLocalHost().getHostName();
			}
		} catch (Exception e) {
			name = "nbcNoName";
		}
		
		if (FileOperate.isWindows()) {
			ip = getIpWindows();
		} else {
			ip = getIpLinux();
		}
	}
	
	private String getIpWindows() {
		String ip = "";
		try {
			InetAddress[] IP = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
			for (InetAddress inetAddress : IP) {
				if (inetAddress instanceof Inet4Address && !inetAddress.getHostAddress().startsWith("127") ) {
					ip = inetAddress.getHostAddress();
				}
			}
			if (ip == null) {
				ip = InetAddress.getLocalHost().getHostAddress();
			}
		} catch (Exception e) {
			logger.error("cannot get this server's ip");
			throw new RuntimeException("cannot get this server's ip");
		}
		return ip;
	}
	
	private String getIpLinux() {
		String ip = "";
		List<String> lsIp = new ArrayList<String>();
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("ifconfig");
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setGetLsStdOut();
		cmdOperate.run();
		PatternOperate patternOperate = new PatternOperate("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}", false);
		for (String content : cmdOperate.getLsStdOut()) {
			content = content.trim();
			if (content.startsWith("inet") && !content.startsWith("inet6")) {
				String ipTmp = patternOperate.getPatFirst(content);
				if (!ipTmp.equals("127.0.0.1")) {
					lsIp.add(ipTmp);
				}
			}
		}
		
		if (lsIp.size() == 1) {
			ip = lsIp.get(0);
		} else {
			for (String string : lsIp) {
				if (string.startsWith("192")) {
					ip = string;
					break;
				}
			}
		}
		if (StringOperate.isRealNull(ip)) {
			logger.error("cannot get this server's ip");
			throw new RuntimeException("cannot get this server's ip");
		}
		return ip;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	/** 获得当前cpu和内存的信息 */
	public void setCpuMemInfo() {
		try {
			Mem mem = sigar.getMem();
			memoryFreeReal = (int) (mem.getActualFree() / (1024L*1024L));
		} catch (SigarException e) {
			e.printStackTrace();
			memoryFreeReal = -1;
		}
		try {
			cpuCoreFreeReal = (int) (sigar.getCpuPerc().getIdle()*cpuCoreNum);
		} catch (SigarException e) {
			e.printStackTrace();
			cpuCoreFreeReal = -1;
		}
	}
	
	public long getMemoryAll() {
		return memoryAll;
	}
	
	/**
	 * <b>用之前先调用{@link #getCpuMemInfo()}</b><br>
	 * 实际内存使用
	 * MB为单位<p>
	 * 返回负数表示出错
	 */
	public int getMemoryFreeReal() {
		return memoryFreeReal;
	}
	
	public String getIp() {
		return ip;
	}
	
	/** 计算机的名字 如:slaver1、master1.. */
	public String getName() {
		return name;
	}
	/** 计算机的名字加上IP，意思就是独一无二的ID */
	public String getNameIP() {
		String ip = getIp();
		return name + SepSign.SEP_INFO_SIMPLE + ip.replace(".", "_");
	}

	/** 设定数据库记录的空闲内存大小，每个task有消耗的内存数，根据该数据减去内存大小，单位是MB */
	public void setMemoryFreeDB(int memoryFreeDB) {
		this.memoryFreeDB = memoryFreeDB;
	}
	/** 获得数据库的内存大小，单位是MB */
	public long getMemoryFreeDB() {
		return memoryFreeDB;
	}
	/** 设定数据库记录的空闲cpu核心数，每个task有消耗的cpu核心数，根据该数据减去cpu核心数量 */
	public void setCpuCoreFreeInDB(int cpuCoreFreeInDB) {
		this.cpuCoreFreeInDB = cpuCoreFreeInDB;
	}
	/** 本机在数据库，也就是task消耗下，理论CPU空闲数 */
	public int getCpuCoreFreeInDB() {
		return cpuCoreFreeInDB;
	}
	/** 
	 * <b>用之前先调用{@link #getCpuMemInfo()}</b><br>
	 * 本机实际有多少CPU核心空闲<br>
	 * 小于0则报错
	 * @return
	 */
	public int getCpuCoreFreeReal() {
		return cpuCoreFreeReal;
	}
	/** 本机有多少CPU核心 */
	public int getCpuCoreNum() {
		return cpuCoreNum;
	}

	/**
	 * cpu使用百分比
	 */
	public double getPerCpu(){
		return  100 -Math.rint(cpuCoreFreeReal*100/cpuCoreNum );
	}
	
	/**
	 * cpuDB使用百分比
	 */
	public double getPerCpuDb(){
		return  100 -Math.rint(cpuCoreFreeInDB*100/cpuCoreNum );
	}
	
	/**
	 * 内存使用百分比
	 */
	public double getPerMemery() {
	return 100 - Math.rint(memoryFreeReal*100/memoryAll);
	}
	
	/**
	 * 内存使用百分比(DB)
	 */
	public double getPerMemeryDb() {
	return 100 - Math.rint(memoryFreeDB*100/memoryAll);
	}
		
	private String getComputerInfo() {
		return getIp() + SepSign.SEP_ID + getName() + SepSign.SEP_ID + getCpuCoreNum();
	}
	
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		
		if (getClass() != obj.getClass()) return false;
		Computer otherObj = (Computer)obj;		
		return getComputerInfo().equals(otherObj.getComputerInfo());		
	}
	
	@Override
	public int hashCode() {
		return getComputerInfo().hashCode();
	}
	
	private static class ComputerHolder {
		static Computer computer = new Computer();
		static{
			computer.initial();
		}
	}
	
	public static Computer getInstance() {
		return ComputerHolder.computer;
	}

}
