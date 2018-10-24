package com.novelbio.base.util.redis;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.dataStructure.NBCJedisPool;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * redis中set类型的工具类
 * 
 * @author novelbio liqi
 * @date 2018年9月25日 上午10:44:28
 */
public class RedisSetUtil {
	private static final Logger logger = LoggerFactory.getLogger(RedisSetUtil.class);
	private static JedisPool jedisPool = new NBCJedisPool(NBCJedisPool.DB_REDIS_SET_INDEX);

	private RedisSetUtil() {
	}

	/**
	 * 往set中写入元素
	 * 
	 * @param key
	 * @param values
	 */
	public static void sadd(String key, String... values) {
		Jedis jedis = jedisPool.getResource();
		jedis.sadd(key, values);
		jedis.close();
	}

	/**
	 * 移除set中的元素
	 * 
	 * @param key
	 * @param members
	 */
	public static void srem(String key, String... members) {
		Jedis jedis = jedisPool.getResource();
		jedis.srem(key, members);
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
		long slong = jedis.scard(key);
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
		long slong = jedis.scard(key);
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
		List<String> list = jedis.lrange(key, begIndex, endIndex);
		jedis.close();
		return list;
	}
}
