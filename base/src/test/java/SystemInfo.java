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

import com.novelbio.base.dataOperate.TxtReadandWrite;

public class SystemInfo {
	public static void main(String[] args) throws UnknownHostException {
		TxtReadandWrite txtRead = new TxtReadandWrite("/home/novelbio/software/hadoop/配置文件/172/yarn-site.xml");
		for (String string : txtRead.readlines()) {
			System.out.println(string);
		}
		
	}


}
