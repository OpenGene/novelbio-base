package com.novelbio.base.curator;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;

import com.novelbio.base.PathDetail;
import com.novelbio.base.fileOperate.FileOperate;


/** 只有第一个获得锁的线程(电脑)才会获得锁
 * 每次new一个新的来执行
 * 其他的线程()
 * @author zong0jie
 *
 */
public class CuratorLock {
	CuratorFramework client;
	String path;
	String prefix = "nbclock-";
	String thisNode;
	
	/**
	 * 每次new一个新的来执行
	 * @param client
	 * @param path
	 */
	CuratorLock(CuratorFramework client, String path) {
		this.client = CuratorNBC.getClient();
		this.path = path;
	}
	
	/** 返回是否获得线程锁 */
	public boolean acquire() {
		try {
			String node = ZKPaths.makePath(path, prefix);
			String thisNode = client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(node);
			List<String> lsNodes = client.getChildren().forPath(path);
			Collections.sort(lsNodes, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2) {
					if (o1 == o2) {
						return 0;
					} else if (o1 == null) {
						return 1;
					} else if (o2 == null) {
						return -1;
					}
					Integer int1 = Integer.MAX_VALUE, int2 = Integer.MAX_VALUE;
					try { int1 = Integer.parseInt(o1.replace(prefix, ""));	} catch (Exception e) {}
					try { int2 = Integer.parseInt(o2.replace(prefix, ""));	} catch (Exception e) {}
					// TODO Auto-generated method stub
					return int1.compareTo(int2);
				}
			});
			if (lsNodes.get(0).equals(FileOperate.getFileName(thisNode))) {
				this.thisNode = thisNode; 
				return true;
			} else {
				client.delete().forPath(thisNode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/** 释放锁
	 * 前面获得过锁的对象才有必要释放锁
	 *  */
	public void release() {
		if (thisNode != null) {
			try {
				client.delete().forPath(thisNode);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		thisNode = null;
	}
	

}
