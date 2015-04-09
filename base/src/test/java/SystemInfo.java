import java.io.File;
import java.net.UnknownHostException;

public class SystemInfo {
	public static void main(String[] args) throws UnknownHostException {
		File file = new File("/hdfs:/nbCloud/testJava/NBCplatform/testDNAmap/bwaResult/resultBWA5_sorted.bam22.bai");
		System.out.println(file.length());
	}


}
