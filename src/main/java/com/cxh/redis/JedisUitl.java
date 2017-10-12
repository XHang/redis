package com.cxh.redis;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
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
	
	public static final String NODE_HOST="192.168.2.158";
	
	public static final int NODE_PORT = 7001;
	
	
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
	
	/**
	 * 获取redis集群的连接对象
	 * @return
	 */
	public static JedisCluster getRedisClusterConnection(){
		Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
		//加入一个节点后，Jedis Cluster会自动查找集群节点
		jedisClusterNodes.add(new HostAndPort(NODE_HOST, NODE_PORT));
		JedisCluster jc = new JedisCluster(jedisClusterNodes);
		return jc;
	}
	public static void closeClusterConnection(JedisCluster cluster) {
		try {
			if(cluster !=null){
				cluster.close();
			}
		} catch (IOException e) {
			throw new RuntimeException("关闭集群连接失败！",e);
		}
	
	}
}
