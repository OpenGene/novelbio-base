package com.novelbio.base.fileOperate;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.novelbio.base.PathDetail;

public class HdfsInitial {
	private static final long serialVersionUID = 1L;
	@Deprecated
	private static String HEAD;	
	private static String symbol;
	/** hdfs挂载在本地哪个盘下面 */
	private static String hdfsLocalPath;
	
	static FileSystem fsHDFS;
	static Configuration conf;
	static {
		initial();
	}
	
	private static void initial() {
		String configPath = FileOperate.isWindows() ? "configWindows.properties" : "config.properties";
		InputStream in = PathDetail.class.getClassLoader().getResourceAsStream(configPath);
		Properties properties = new Properties();
		try {
			properties.load(in);
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally{
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		HEAD = properties.getProperty("hdfsHead");
		symbol = properties.getProperty("hdfsHeadSymbol");
		hdfsLocalPath = properties.getProperty("hdfsLocalPath");
		IntHdfsBaseHolder hdfsBase = null;
		if (properties.containsKey("hdfs-core-xml")) {
			hdfsBase = new HdfsBaseHolderHadoop2();
			((HdfsBaseHolderHadoop2)hdfsBase).setCorexml(properties.getProperty("hdfs-core-xml"));
			((HdfsBaseHolderHadoop2)hdfsBase).setHdfsxml(properties.getProperty("hdfs-xml"));
			HEAD = null;
		} else {
			hdfsBase = new HdfsBaseHolderMapr();
		}
		conf = hdfsBase.getConf();
		try {
			if (properties.containsKey("hdfs-core-xml")) {
				fsHDFS = FileSystem.get(conf);
			} else {
				fsHDFS = FileSystem.get(URI.create(HEAD), conf);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Deprecated
	public static String getHEAD() {
		if (HEAD != null) {
			return HEAD;
		}
		return "";
	}
	public static String getSymbol() {
		return symbol;
	}
	
	/**
	 * 返回设定的文件系统
	 * @return
	 */
	public static FileSystem getFileSystem() {
		return fsHDFS;
	}

	public static String getHdfsLocalPath() {
		return hdfsLocalPath;
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
//			conf.set("fs.defaultFS", "hdfs://cluster1");
//			conf.set("dfs.nameservices", "cluster1");
//			conf.set("dfs.ha.namenodes.cluster1", "nn1,nn2");
//			conf.set("dfs.namenode.rpc-address.cluster1.nn1", "192.168.0.180:8020");
//			conf.set("dfs.namenode.rpc-address.cluster1.nn2", "192.168.0.181:8020");
//			conf.set("dfs.client.failover.proxy.provider.cluster1", "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
			conf.set("dfs.permissions.enabled", "false");
			try {
				readXml();
			} catch (DocumentException e) {
				e.printStackTrace();
				return null;
			}
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
