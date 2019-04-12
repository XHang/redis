package com.cxh.content;

import com.cxh.redis.JedisUitl;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisPool {
    public static JedisPool jedisPool;
    static {
        jedisPool = JedisUitl.getRedisPool();
    }
    public static Jedis getJedis(){
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }
    public static void closePool(){
        JedisUitl.closePool(jedisPool);
    }
    public static void returnResource(Jedis jedis){
        jedis.close();
    }
}
