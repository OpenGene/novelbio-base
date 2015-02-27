package com.novelbio.base.curator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.EnsurePath;
import org.apache.curator.utils.ZKPaths;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import com.novelbio.base.SepSign;
import com.novelbio.base.SerializeKryo;

/**
 * 从curator中的SimpleDistributedQueue修改而来，主要添加了task的支持<br>
 * <b>假定该队列中的对象只有一种</b>
 * <p>
 *     Drop in replacement for: org.apache.zookeeper.recipes.queue.DistributedQueue that is part of
 *     the ZooKeeper distribution
 * </p>
 *
 * <p>
 *     This class is data compatible with the ZK version. i.e. it uses the same naming scheme so
 *     it can read from an existing queue
 * </p>
 */
public class SimpleDistributedQueue<T> {
    private static final Logger logger = Logger.getLogger(SimpleDistributedQueue.class);
    CuratorFramework client;
    final String path;
    final EnsurePath ensurePath;
    SerializeKryo serializeKryo = new SerializeKryo();
    final String PREFIX = "qn-";
    
    /**
     * @param client the client
     * @param path path to store queue nodes
     */
    public SimpleDistributedQueue(CuratorFramework client, String path) {
        this.client = client;
        this.path = path;
        try {
			if (client.checkExists().forPath(path) == null) {
				client.create().creatingParentsIfNeeded().forPath(path);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
        ensurePath = client.newNamespaceAwareEnsurePath(path);
    }
    
    public void setWatcher(Watcher watcherChildren) throws Exception {
		client.getChildren().usingWatcher(watcherChildren).forPath(path);
	}
    /**
     * Inserts data into queue.
     *
     * @param data the data
     * @param prefix 描述该data信息的前缀
     * @return string if data was successfully added
     */
    public String offer(T data, String prefix) {
    	try {
    		ensurePath.ensure(client.getZookeeperClient());
    		byte[] databy = serializeKryo.write(data);
    		String thisPath = ZKPaths.makePath(path, PREFIX + prefix + SepSign.SEP_INFO);
    		client.create().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(thisPath, databy);
    		return thisPath;
		} catch (Exception e) {
			// TODO: handle exception
		}
        return null;
    }
    /**
     * Inserts data into queue.
     * 不填加随机名字
     *
     * @param data the data
     * @param id 描述该data信息的前缀
     * @return string if data was successfully added
     */
    public String offerNoSequential(T data, String id) {
    	try {
    		ensurePath.ensure(client.getZookeeperClient());
    		byte[] databy = serializeKryo.write(data);
    		String thisPath = ZKPaths.makePath(path, PREFIX + id);
    		client.create().withMode(CreateMode.PERSISTENT).forPath(thisPath, databy);
    		return thisPath;
		} catch (Exception e) {
			// TODO: handle exception
		}
        return null;
    }
	/**
	 * 把任务从等待队列中移除,如果没找到任务,返回false
	 * @param id
	 * @return
	 */
	public boolean removeTaskNoSequential(String id) {
		try {
			String nodeName = ZKPaths.makePath(path, PREFIX + id);
			if (client.checkExists().forPath(nodeName) != null ) {
				client.delete().forPath(nodeName);
				return true;
			}
		} catch (Exception e) {
			logger.error("haven't remove task correctly:" + id, e);
		}
		return false;
	}
    /**
     * Inserts data into queue.
     *
     * @param data the data
     * @return string if data was successfully added
     */
    public String offer(T info) {
    	try {
    		ensurePath.ensure(client.getZookeeperClient());
    		byte[] data = serializeKryo.write(info);
    		String thisPath = ZKPaths.makePath(path, PREFIX + SepSign.SEP_INFO);
    		client.create().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(thisPath, data);
    		return thisPath;
		} catch (Exception e) {
			// TODO: handle exception
		}
        return null;
    }
    
    /** 获得所有lsName的名字，就是{@link #offer(Object, String)} 时候输入的名字 
     * @throws Exception */
    public List<String> getLsNodeName() throws Exception {
    	List<String> lsNodeName = new ArrayList<>();
        List<String> lsNodes = client.getChildren().forPath(path);
        for (String string : lsNodes) {
			String strInfo = string.split(SepSign.SEP_INFO)[0].replace(PREFIX, "");
			lsNodeName.add(strInfo);
		}
        return lsNodeName;
    }
    
    /**
     * Returns the data at the first element of the queue, or null if the queue is empty.
     *
     * @return data at the first element of the queue, or null.
     * @throws Exception errors
     */
    public T peek() {
    	return element();
    }

    /**
     * Retrieves and removes the head of this queue, waiting up to the
     * specified wait time if necessary for an element to become available.
     *
     * @param timeout how long to wait before giving up, in units of
     *        <tt>unit</tt>
     * @param unit a <tt>TimeUnit</tt> determining how to interpret the
     *        <tt>timeout</tt> parameter
     * @return the head of this queue, or <tt>null</tt> if the
     *         specified waiting time elapses before an element is available
     * @throws Exception errors
     */
    public T poll(long timeout, TimeUnit unit) {
    	T info = null;
    	try {
    		info = internalPoll(timeout, unit);
		} catch (Exception e) { }
    	return info;
    }

    /**
     * Return the head of the queue without modifying the queue.
     *  
     * @return the data at the head of the queue.
     */
    public T element() {
    	T info = null;
    	try {
    		info = internalElement(false, null);
		} catch (Exception e) {
		}
     	return info;
    }

    /**
     * Removes the head of the queue and returns it, blocks until it succeeds.
     *
     * @return The former head of the queue
     * @throws Exception errors
     */
    public T take() throws Exception {
        T info = internalPoll(0, null);
        return info;
    }

    /**
     * Attempts to remove the head of the queue and return it. Returns null if the queue is empty.
     *
     * @return Head of the queue or null.
     * @throws Exception errors
     */
    public T poll() {
    	T info = null;
		try {
			info = internalElement(true, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return info;
    }

    private T internalPoll(long timeout, TimeUnit unit) throws Exception {
        ensurePath.ensure(client.getZookeeperClient());

        long            startMs = System.currentTimeMillis();
        boolean         hasTimeout = (unit != null);
        long            maxWaitMs = hasTimeout ? TimeUnit.MILLISECONDS.convert(timeout, unit) : Long.MAX_VALUE;
        for(;;) {
            final CountDownLatch    latch = new CountDownLatch(1);
            Watcher watcher = new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    latch.countDown();
                }
            };
           T info = internalElement(true, watcher);
            if ( info != null ) {
        		return info;
            }

            if ( hasTimeout ) {
                long        elapsedMs = System.currentTimeMillis() - startMs;
                long        thisWaitMs = maxWaitMs - elapsedMs;
                if ( thisWaitMs <= 0 ) {
                    return null;
                }
                latch.await(thisWaitMs, TimeUnit.MILLISECONDS);
            } else {
                latch.await();
            }
        }
    }
    
    private T internalElement(boolean removeIt, Watcher watcher) throws Exception {
        try {
			ensurePath.ensure(client.getZookeeperClient());
		} catch (Exception e) {
			return null;
		}

        List<String> nodes;
        try {
        	nodes = (watcher != null) ? client.getChildren().usingWatcher(watcher).forPath(path) : client.getChildren().forPath(path);
        } catch (Exception e) {
        	return null;
        }
        Collections.sort(nodes, new CompareTask());
        
        for ( String node : nodes ) {
        	 String  thisPath = ZKPaths.makePath(path, node);
            if ( !node.startsWith(PREFIX) ) {
                logger.warn("Foreign node in queue path: " + node);
                client.delete().forPath(thisPath);
                continue;
            }
           
            try {
                byte[] bytes = client.getData().forPath(thisPath);
                if ( removeIt ) {
                    client.delete().forPath(thisPath);
                }
                T result = (T)serializeKryo.read(bytes);
                return result;
            }
            catch ( KeeperException.NoNodeException ignore ) {
                //Another client removed the node first, try next
            }
        }
        return null;
    }
    
    public boolean isEmpty() {
    	List<String> lsNodes;
		try {
			lsNodes = client.getChildren().forPath(path);
	    	if (lsNodes.size() == 0) {
				return true;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return false;
    }
    
    /** 清空队列 
     * @throws Exception */
    public void clear() throws Exception {
    	List<String> lsNode = client.getChildren().forPath(path);
    	for (String node : lsNode) {
    		String pathNode = ZKPaths.makePath(path, node);
			client.delete().forPath(pathNode);
		}
    }
}

class CompareTask implements Comparator<String> {
	@Override
	public int compare(String o1, String o2) {
		String o1sub = o1.split(SepSign.SEP_INFO)[1];
		String o2sub = o2.split(SepSign.SEP_INFO)[1];
		// TODO Auto-generated method stub
		return o1sub.compareTo(o2sub);
	}
	
}
