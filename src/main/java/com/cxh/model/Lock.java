package com.cxh.model;

import com.cxh.content.RedisPool;
import com.cxh.exception.LockTimeOutException;
import com.cxh.redis.JedisUitl;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

import java.math.BigDecimal;

public class Lock {

    public static final String suffix = "_lock";

    public  static final String lockValue = "lock";

    /**
     * 超时10s 可能不够，因为千万级并发可能绝大多数时间，cpu都浪费在轮询上。
     * 没有时间去处理实际的业务，导致占锁时间过长
     */
    public static final long timeout = 10000;

    public static void lock(String productId)  {
        Jedis jedis = RedisPool.getJedis();
        try{
            //锁存在，则轮询
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis()-startTime<timeout){
                //设置锁数据,如果锁数据不存在，则设置锁，并返回1，如果锁数据已存在，则不设置锁，返回0
                if (jedis.setnx(getLockKey(productId),lockValue) == 1){
                    System.out.println("您"+Thread.currentThread().getId()+"已拿到锁");
                    //设置锁有效时间，超过该时间则删除该锁，释放资源
                    jedis.expire(getLockKey(productId), (int) (timeout/1000));
                    return;
                }
                //睡3ms左右再轮询锁
                try {
                    Thread.sleep(3, (new BigDecimal(Math.random()*100+30).intValue()));
                } catch (InterruptedException e) {
                    throw new RuntimeException("获取锁失败",e);
                }
            }
            throw new LockTimeOutException("您"+Thread.currentThread().getId()+"获取锁超时");
        }finally {
            RedisPool.returnResource(jedis);
        }

    }
    private static String getLockKey(String productId){
        return productId+suffix;
    }
    public static void unlock(String productId){
        Jedis jedis = RedisPool.getJedis();
        try {
           System.out.println("解锁操作"+jedis.del(getLockKey(productId))+"解锁人"+Thread.currentThread().getId());
        }finally {
            RedisPool.returnResource(jedis);
        }

    }
}
