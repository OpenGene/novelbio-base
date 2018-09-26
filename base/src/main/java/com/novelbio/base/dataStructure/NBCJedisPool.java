/**
 *
 * @author novelbio fans.fan
 * @date 2017年11月14日
 */
package com.novelbio.base.dataStructure;

import java.util.Set;

import com.novelbio.base.PathDetail;
import com.novelbio.base.StringOperate;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 对JedisPool简单处理.这样可以让不同的业务使用不同的db.每次操作的都是自己的库
 * @author novelbio fans.fan
 */
public class NBCJedisPool extends JedisPool {

	/** session使用的redis库的下标号:0 */
	public static final int DB_SESSION_INDEX = 0;
	/** 微信小程序session使用的redis库的下标号:1 */
	public static final int WECHAT_INDEX = 1;
	/** 使用redis作为一个简单的消息队列:2 */
	public static final int DB_REDIS_QUEUE_INDEX = 2;
	/** 使用redis作为Set的简单操作:3 */
	public static final int DB_REDIS_SET_INDEX = 3;
	
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

	public static void main(String[] args) {
		NBCJedisPool pool = new NBCJedisPool(1);
		Jedis jedis = pool.getResource();
		jedis.set("abc".getBytes(), "123".getBytes());
		Set<String> setKeys = jedis.keys("*");
		System.out.println(setKeys);
		jedis.close();
		pool.close();
	}
}
