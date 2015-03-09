
import org.apache.curator.framework.CuratorFramework;

import com.novelbio.base.curator.CuratorNBC;
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
		CuratorFramework client = CuratorNBC.getClient();
		client.create().forPath("/novelbiosss");
	}
}