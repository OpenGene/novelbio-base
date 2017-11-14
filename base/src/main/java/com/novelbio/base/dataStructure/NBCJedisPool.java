/**
 *
 * @author novelbio fans.fan
 * @date 2017年11月14日
 */
package com.novelbio.base.dataStructure;

import com.novelbio.base.PathDetail;
import com.novelbio.base.StringOperate;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 *
 * @author novelbio fans.fan
 */
public class NBCJedisPool extends JedisPool {

	/** session使用的redis库的下标号 */
	public static final int DB_SESSION_INDEX = 0;
	
	private static String host = PathDetail.getRedisServerIp();
	private static int port = 6379;
	//timeout for jedis try to connect to redis server, not expire time! In milliseconds
	private static int timeout = 10000;
	private static String password = "";
	
	/** redis默认有db0到db15共16个库,session默认使用的是第一个库.所以其他地方要使用需指定库index */
	private int dbIndex = 0;

	/**
	 * @param expire	
	 * @param dbIndex
	 */
	public NBCJedisPool(int dbIndex) {
		super(new JedisPoolConfig(), host, port, timeout, StringOperate.isRealNull(password) ? null : password);
		this.dbIndex = dbIndex;
	}
	
	/* (non-Javadoc)
	 * @see redis.clients.jedis.JedisPool#getResource()
	 */
	@Override
	public Jedis getResource() {
		Jedis jedis = super.getResource();
		jedis.select(dbIndex);
		return jedis;
	}
}
