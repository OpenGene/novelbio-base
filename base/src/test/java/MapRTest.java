
import java.util.ArrayList;
import java.util.List;

import com.novelbio.base.PathDetail;
import com.novelbio.base.cmd.CmdOperate;
import com.novelbio.base.dataOperate.TxtReadandWrite;
import com.novelbio.base.fileOperate.FileHadoop;

import org.apache.hadoop.yarn.applications.distributedshell.ApplicationMaster;
import org.springframework.util.MultiValueMap;  
/**
 * Assumes mapr installed in /opt/mapr
 * 
 * compilation needs only hadoop jars: javac -cp
 * /opt/mapr/hadoop/hadoop-0.20.2/lib/hadoop-0.20.2-dev-core.jar MapRTest.java
 * 
 * Run: java -Djava.library.path=/opt/mapr/lib -cp
 * /opt/mapr/hadoop/hadoop-0.20.2
 * /conf:/opt/mapr/hadoop/hadoop-0.20.2/lib/hadoop-
 * 0.20.2-dev-core.jar:/opt/mapr/
 * hadoop/hadoop-0.20.2/lib/maprfs-0.1.jar:.:/opt/mapr
 * /hadoop/hadoop-0.20.2/lib/commons
 * -logging-1.0.4.jar:/opt/mapr/hadoop/hadoop-0.20.2/lib/zookeeper-3.3.2.jar
 * MapRTest /test
 */
public class MapRTest {
	public static void main(String args[]) throws Exception {
		TxtReadandWrite txtRead = new TxtReadandWrite("/home/novelbio/software/hadoop/etc/hadoop/core-site.xml");
		TxtReadandWrite txtWrite = new TxtReadandWrite("/hdfs:/nbCloud/testFile", true);
		txtWrite.writefileln("setstgfsaerae");
		for (String content : txtRead.readlines()) {
			txtWrite.writefileln(content);
		}
		txtRead.close();
//		txtWrite.close();
		
		List<String> lsCmd = new ArrayList<String>();
		lsCmd.add("ifconfig");
		
		CmdOperate cmdOperate = new CmdOperate(lsCmd);
		cmdOperate.setGetLsStdOut();
		cmdOperate.run();
		for (String string : cmdOperate.getLsStdOut()) {
			txtWrite.writefileln(string);
		}
		txtWrite.flush();
		txtWrite.close();
	}
}