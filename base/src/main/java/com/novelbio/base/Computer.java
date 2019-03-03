package com.novelbio.base;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
//import org.hyperic.sigar.Mem;
//import org.hyperic.sigar.Sigar;
//import org.hyperic.sigar.SigarException;

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
	protected transient static int memToBeUsed = 5000;
	protected transient static int cpuToBeUsed = 2;
	
	/** ip地址 */
	protected String ip;
	/** 计算机的名字 如:slaver1、master1.. */
	protected String name;
//	protected int cpuCoreNum;
//	/** 总内存数，以MB为单位 */
//	protected int memoryAll;
//
//	
//	/** 数据库里记载的空闲cpu数量 */
//	protected int cpuCoreFreeInDB;
//	/** 实际空间cpu数量 */
//	protected int cpuCoreFreeReal;
//	/** 数据库里记载的空闲内存数量 */
//	protected int memoryFreeDB;
//	/** 实际空闲内存数量 */
//	protected int memoryFreeReal;
	
	
	protected Computer() {}
	
	public void initial() {
//		if (sigar == null) {
//			sigar = new Sigar();
//		}
//		try {
//			Mem mem = sigar.getMem();
//			memoryAll = (int) (mem.getTotal() / (1024L*1024L));
//			cpuCoreNum = sigar.getCpuInfoList()[0].getTotalCores();
//			memoryFreeDB = (int) (mem.getActualFree() / (1024L*1024L)) - memToBeUsed;
//			cpuCoreFreeInDB = cpuCoreNum - cpuToBeUsed;
//		} catch (SigarException e) {
//			e.printStackTrace();
//		}
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
		ip = "0.0.0.0";
		if (FileOperate.isWindows()) {
			ip = getIpWindows();
		} else {
			try {
				ip = getIpUbuntu();
			} catch (Exception e) {
			}
			if (StringOperate.isRealNull(ip) || ip.equals("0.0.0.0")) {
				try {
					ip = getIpCentOs();
				} catch (Exception e) {
				}
			}
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
	
	private String getIpUbuntu() {
		return getIpLinux("/sbin/ifconfig");
	}
	private String getIpCentOs() {
		return getIpLinux("/sbin/ifconfig");
	}
	private String getIpLinux(String cmd) {
		String ip = "";
		List<String> lsIp = new ArrayList<>();
		List<String> lsCmd = new ArrayList<>();
		lsCmd.add("/sbin/ifconfig");
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setTerminateWriteTo(false);
		cmdOperate.runWithExp();
		PatternOperate patternOperate = new PatternOperate("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}", false);
		boolean isEth0 = false;
		String ipEth0 = null;
		for (String content : cmdOperate.getLsStdOut()) {
			content = content.trim();
			if (content.startsWith("eth0")) {
				isEth0 = true;
			}
			if (content.startsWith("inet") && !content.startsWith("inet6")) {
				String ipTmp = patternOperate.getPatFirst(content);
				if (!ipTmp.equals("127.0.0.1")) {
					lsIp.add(ipTmp);
				}
				if (isEth0) {
					ipEth0 = ipTmp;
					isEth0 = false;
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
			if (!StringOperate.isRealNull(ipEth0)) {
				ip = ipEth0;
			} else if (!lsIp.isEmpty()) {
				ip = lsIp.get(0);
			} else {
				logger.error("cannot get this server's ip");
				throw new RuntimeException("cannot get this server's ip");
			}
		}
		return ip;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	public void setIp(String ip) {
		this.ip = ip;
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
		
	private String getComputerInfo() {
		return getIp() + SepSign.SEP_ID + getName();
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
