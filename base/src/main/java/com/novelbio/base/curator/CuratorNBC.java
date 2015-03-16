package com.novelbio.base.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.novelbio.base.PathDetail;
import com.novelbio.base.fileOperate.FileOperate;

public class CuratorNBC {
	
	
//	//懒汉模式的单例延迟--超牛逼
	static class ClientHolder {
		static CuratorFramework client = CuratorFrameworkFactory.newClient(PathDetail.getZookeeperServerSite(), new ExponentialBackoffRetry(1000, 3));
//		static CuratorFramework client = CuratorFrameworkFactory.newClient(getZookeeperServerSite(), 
//			4000, 1000, new ExponentialBackoffRetry(1000, 3));
		}
	static double[] lock = new double[0];
	/** 返回已经启动的client */
	public static CuratorFramework getClient() {
		CuratorFramework client = ClientHolder.client;
		synchronized (lock) {
			if (client.getState() != CuratorFrameworkState.STARTED) {
				try {
					client.start();
				} catch (Exception e) {
					try { client.close(); } catch (Exception e2) { }
					client = null;
					RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
					client = CuratorFrameworkFactory.newClient(PathDetail.getZookeeperServerSite(), retryPolicy);
//					client = CuratorFrameworkFactory.newClient(getZookeeperServerSite(), 4000, 1000, retryPolicy);	
					client.start();
				}			
			}
		}
		return client;
	}
	
	public static InterProcessMutex getInterProcessMutex(String path) {
		return new InterProcessMutex(getClient(), FileOperate.addSep(PathDetail.getZookeeperLock()) + path);
	}
	
	public static void deleteInterProcessMutexPath(String path) {
		try {
			getClient().delete().forPath(FileOperate.addSep(PathDetail.getZookeeperLock()) + path);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
