import com.novelbio.base.dataOperate.TxtReadandWrite;


public class TestHadoop2Hdfs {
	public static void main(String[] args) {
		TxtReadandWrite txtRead = new TxtReadandWrite("/hdfs:/test/aaabbb");
		for (String string : txtRead.readlines()) {
			System.out.println(string);
		}
	}
}
