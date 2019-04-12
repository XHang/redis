package com.cxh.service.impl;

import com.cxh.model.Lock;
import com.cxh.model.Product;
import com.cxh.service.SecKillService;

/**
 * 秒杀系统设计
 */
public class JedisSecKillServiceImpl implements SecKillService {



    /**
     * 秒杀方法
     * @param productId  产品ID
     */
    @Override
    public void secKill(String productId){
        try{
            Lock.lock(productId);
            int count = Product.count;
            if (count>0){
                System.out.println("您"+Thread.currentThread().getId()+"抢到了第"+count+"商品");
                Product.count = count-1;
            }else{
                System.out.println("已"+Thread.currentThread().getId()+"抢光");
            }
        }finally {
            Lock.unlock(productId);
        }

    }

}
