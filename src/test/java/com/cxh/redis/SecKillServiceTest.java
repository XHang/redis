package com.cxh.redis;

import com.cxh.model.Product;
import com.cxh.service.SecKillService;
import com.cxh.service.impl.JedisSecKillServiceImpl;
import com.cxh.service.impl.RedissonSecKillServiceImpl;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.CountDownLatch;

public class SecKillServiceTest {

    @Test
    public void secKill() throws InterruptedException {
        //1000人秒杀100个商品
        int totalConcurrency = 1000;
        //只有当执行了totalConcurrency个countDown后，被await的线程才会继续执行
        CountDownLatch countDownLatch =new CountDownLatch(totalConcurrency);
        CountDownLatch countBgineLatch =new CountDownLatch(1);
        RedissonClient client = getClient();
        for (int i = 0; i < totalConcurrency; i++) {
            new Thread( ()->{
                try {
                    countBgineLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("客户请求来了");
                //new JedisSecKillServiceImpl().secKill(Product.productId);
                new RedissonSecKillServiceImpl(client).secKill(Product.productId);
                //秒杀任务结束
                countDownLatch.countDown();
            }).start();
        }
        countBgineLatch.countDown();
        System.out.println("已开始秒杀行动");
        countDownLatch.await();
        System.out.println("秒杀行动已经结束");
    }
    public RedissonClient getClient(){
        Config config = new Config();
        // for single server
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379")
                .setPassword("10086");
        RedissonClient client = Redisson.create(config);
        System.out.println("已经创建RedissonClient");
        return client;
    }
}