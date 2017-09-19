package com.cxh.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class JedisUitl {
	
	/**
	 * redis主机名
	 */
	public static final String REDIS_HOST = "centos";
	/**
	 * redis端口
	 */
	public static final int REDIS_PORT = 6379;
	/**
	 * redis认证密码
	 */
	public static final String PASSWORD = "10086";
	/***
	 * 连接池超时毫秒
	 */
	public static final int POOL_TIMEOUT = 2000;
	
	
	/**
	 * 获取jedis连接对象
	 * @return
	 */
	public static Jedis getJedis(){
		Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);
		jedis.auth(PASSWORD);
		return jedis;
	}
	/**
	 * 关闭redis连接
	 */
	public static void closeRedis(Jedis jedis){
		if(jedis!=null){
			jedis.quit();
			jedis.close();
		}
	}
	
	/**
	 * 清空redis的所有数据，如果没有传jedis，将自己创建一个
	 * 有传jedis的，方法不会关掉你的jedis
	 * @param jedis
	 */
	public static void cleanRedis(Jedis jedis){
		if(jedis == null){
			jedis=getJedis();
			jedis.flushAll();
			jedis.quit();
			jedis.disconnect();
		}else{
			jedis.flushAll();
		}
		
	}
	
	/**
	 * 获取jedis连接池对象
	 * @return
	 */
	public static JedisPool   getRedisPool(){
		JedisPool pool = new JedisPool(new GenericObjectPoolConfig(), REDIS_HOST, REDIS_PORT, POOL_TIMEOUT,PASSWORD );
		return pool;
	}
	/**
	 * 关闭redis连接池
	 * @param pool redis连接池对象
	 */
	public static void closePool(JedisPool pool){
		if(pool!=null){
			pool.destroy();
		}
	}
}
