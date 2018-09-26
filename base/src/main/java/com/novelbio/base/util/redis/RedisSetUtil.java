package com.novelbio.base.util.redis;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.novelbio.base.dataStructure.NBCJedisPool;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanResult;

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

	public static List<String> scan(String key, String cursor) {
		Jedis jedis = jedisPool.getResource();
		ScanResult<String> ret = jedis.sscan(key, cursor);
		jedis.close();
		return ret.getResult();
	}
}
