import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.hyperic.sigar.Cpu;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.Swap;

public class SystemInfo {
	public static void main(String[] args) throws UnknownHostException {
		Sigar sigar = new Sigar();
		Mem mem = null;
		try {
			mem = sigar.getMem();
			CpuInfo  info = sigar.getCpuInfoList()[0];    
			CpuPerc perc = sigar.getCpuPerc();    
			Cpu timer = sigar.getCpu();    
			
			System.out.println(info.getTotalCores());
			System.out.println(perc.toString());
			System.out.println(timer.toString());
			System.out.println((int)perc.getCombined());
			
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
		// 内存总量
		System.out.println("内存总量:    " + mem.getTotal() / 1024L + "K av");
		// 当前内存使用量
		System.out.println("当前内存使用量:    " + mem.getActualUsed() / 1024L + "K used");
		System.out.println("当前内存空闲量:    " + mem.getActualFree() / 1024L + "K used");
		System.out.println("当前内存空闲量:    " + Runtime.getRuntime().totalMemory() + "K used");
		// 当前内存剩余量
//		Swap swap = sigar.getSwap();
//		// 交换区总量
//		System.out.println("交换区总量:    " + swap.getTotal() / 1024L + "K av");
//		// 当前交换区使用量
//		System.out.println("当前交换区使用量:    " + swap.getUsed() / 1024L + "K used");
//		// 当前交换区剩余量
//		System.out.println("当前交换区剩余量:    " + swap.getFree() / 1024L + "K free");
		
        Map<String, String> map = System.getenv();  
        String userName = map.get("USERNAME");// 获取用户名  
        String computerName = map.get("COMPUTERNAME");// 获取计算机名  
        String userDomain = map.get("USERDOMAIN");// 获取计算机域名 
        System.out.println(userName);
        System.out.println(computerName);
        System.out.println(userDomain);
        System.out.println(InetAddress.getLocalHost().getHostName());
	}


}
