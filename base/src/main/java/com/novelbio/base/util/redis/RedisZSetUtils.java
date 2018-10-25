package com.novelbio.base.util.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.dataStructure.NBCJedisPool;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 
 * @author novelbio liqi
 * @date 2018年10月24日 下午7:18:10
 */
public class RedisZSetUtils {
	private static final Logger logger = LoggerFactory.getLogger(RedisZSetUtils.class);
	private static JedisPool jedisPool = new NBCJedisPool(NBCJedisPool.DB_REDIS_SET_INDEX);

	private RedisZSetUtils() {
	}
	
	/**
	 * 往set中写入元素
	 * 
	 * @param key
	 * @param values
	 */
	public static void zadd(String key, String values) {
		Jedis jedis = jedisPool.getResource();
		double score = (double)System.currentTimeMillis();
		jedis.zadd(key, score, values);
		jedis.close();
	}
	
	/**
	 * 移除set中的元素
	 * 
	 * @param key
	 * @param members
	 */
	public static void zrem(String key, String... members) {
		Jedis jedis = jedisPool.getResource();
		jedis.zrem(key, members);
		jedis.close();
	}
	
	/**
	 * 获取列表的数量
	 * 
	 * @param key
	 * @return
	 */
	public static long size(String key) {
		Jedis jedis = jedisPool.getResource();
		long slong = jedis.zcard(key);
		jedis.close();
		return slong;
	}
	
	/**
	 * 分页获取集合中的数据，默认排序
	 * 
	 * @param key
	 *            存储的key
	 * @param begIndex
	 *            开始位置
	 * @param count
	 *            获取数量
	 * @return
	 */
	public static List<String> lrang(String key, long begIndex, Integer count) {
		long endIndex = begIndex + count;
		Jedis jedis = jedisPool.getResource();
		long slong = jedis.zcard(key);
		if (begIndex == slong) {
			begIndex = slong - 1;
			endIndex = begIndex;
		} else if (begIndex < slong) {
			if (endIndex >= slong) {
				endIndex = slong - 1;
			}
		} else {
			return new ArrayList<>();
		}
		// begIndex基于0,engIndex为包括，即 0-9为前10个元素。
		Set<String> set = jedis.zrange(key, begIndex, endIndex);
		jedis.close();
		List<String> list = new ArrayList<>(set);
		return list;
	}
}
