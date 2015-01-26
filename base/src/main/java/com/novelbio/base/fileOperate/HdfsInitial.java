package com.novelbio.base.fileOperate;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.novelbio.base.PathDetail;

public class HdfsInitial {
	private static final long serialVersionUID = 1L;
		
	static FileSystem fsHDFS;
	static Configuration conf;
	static {
		initial();
	}
	
	private static void initial() {		
		IntHdfsBaseHolder hdfsBase = new HdfsBaseHolderHadoop2();
		((HdfsBaseHolderHadoop2)hdfsBase).setCorexml(PathDetail.getHdpCoreXml());
		((HdfsBaseHolderHadoop2)hdfsBase).setHdfsxml(PathDetail.getHdpHdfsXml());
		conf = hdfsBase.getConf();
		try {
			fsHDFS = FileSystem.get(conf);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getSymbol() {
		return PathDetail.getHdpHdfsHeadSymbol();
	}
	
	/**
	 * 返回设定的文件系统
	 * @return
	 */
	public static FileSystem getFileSystem() {
		return fsHDFS;
	}

	public static String getHdfsLocalPath() {
		return PathDetail.getHdfsLocalPath();
	}
	
	
	static interface IntHdfsBaseHolder {
		Configuration getConf();
	}
	
	static class HdfsBaseHolderMapr implements IntHdfsBaseHolder {
		static Configuration conf;
		static {
			conf = new Configuration();
			conf.set("dfs.permissions", "false");
		}
		@Override
		public Configuration getConf() {
			// TODO Auto-generated method stub
			return conf;
		}
	}
	
	static class HdfsBaseHolderHadoop2 implements IntHdfsBaseHolder {
		String hdfsxml;
		String corexml;
		Configuration conf;
		
		public void setCorexml(String corexml) {
			this.corexml = corexml;
		}
		public void setHdfsxml(String hdfsxml) {
			this.hdfsxml = hdfsxml;
		}
		
		public synchronized Configuration getConf() {
			conf = new Configuration();
			conf.addResource(new Path(corexml));
			conf.addResource(new Path(hdfsxml));


			conf.set("dfs.permissions.enabled", "false");
		    conf.set("dfs.client.block.write.replace-datanode-on-failure.policy", "NEVER"); 
		    conf.set("dfs.client.block.write.replace-datanode-on-failure.enable", "true"); 
//			try {
//				readXml();
//			} catch (DocumentException e) {
//				e.printStackTrace();
//				return null;
//			}
			return conf;
		}
		
		private void readXml() throws DocumentException {
			Document document = new SAXReader().read(FileOperate.getFile(corexml));
			List<Element> lsElements = document.selectNodes("//configuration/property");
			for (Element ele : lsElements) {
				Element eleName = (Element) ele.selectNodes("name").get(0);
				String name = eleName.getData().toString();
				String value = ((Element)ele.selectNodes("value").get(0)).getData().toString();
				if (name.equals("fs.defaultFS")) {
					conf.set(name, value);
					break;
				}
			}
			
			document = new SAXReader().read(FileOperate.getFile(hdfsxml));
			lsElements = document.selectNodes("//configuration/property");
			for (Element ele : lsElements) {
				Element eleName = (Element) ele.selectNodes("name").get(0);
				String name = eleName.getData().toString();
				String value = ((Element)ele.selectNodes("value").get(0)).getData().toString();
				if (name.equals("dfs.nameservices")
						|| name.startsWith("dfs.ha.namenodes")
						|| name.startsWith("dfs.namenode.rpc-address")
						|| name.startsWith("dfs.client.failover.proxy.provider")						
						) {
					conf.set(name, value);
				}
			}
		}

	}

}
