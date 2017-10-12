package com.cxh.jedisCluster;

import org.junit.Test;
import com.cxh.redis.JedisUitl;
import redis.clients.jedis.JedisCluster;

public class JedisClusterTest {
	
	@Test
	public void setJedisClusterValue(){
		JedisCluster  cluster = JedisUitl.getRedisClusterConnection();
		System.out.println("集群内部共有节点数"+cluster.getClusterNodes().size());
		cluster.set("ClusterKey", "ClusterValue");
		JedisUitl.closeClusterConnection(cluster);
	}
}
