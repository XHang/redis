package com.cxh.service.impl;

import com.cxh.model.Product;
import com.cxh.service.SecKillService;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RedissonSecKillServiceImpl implements SecKillService {
    private RedissonClient client;
    public RedissonSecKillServiceImpl(RedissonClient client){
        this.client = client;
    }
    @Override
    public void secKill(String productId) {
        RLock lock = client.getLock(Product.productId);
        try{
            lock.lock();
            int count = Product.count;
            if (count>0){
                System.out.println("您抢到了第"+count+"个商品");
                Product.count = count-1;
            }else{
                System.out.println("已抢光，当前库存量为"+count);
            }

        }finally {
            lock.unlock();
        }



    }
}
