package com.cxh.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

public class JedisExample {
	
	/**
	 * 利用redis，设置String的key和value，并取出。
	 */
	@Test
	public void setGetString(){
		Jedis jedis = JedisUitl.getJedis();
		jedis.set("key","value");
		System.out.println("成功存入redis");
		jedis.close();
		System.out.println("已关闭jedis连接");
		jedis = JedisUitl.getJedis();
		System.out.println("再次连接jedis");
		System.out.println("你存入的，key对应的值是"+jedis.get("key"));
		jedis.close();
		System.out.println("已关闭jedis连接");
	}
	
	/**
	 * 其他操作
	 */
	@Test
	public void otherOperating(){
		Jedis jedis = JedisUitl.getJedis();
		//清除所有键
		 jedis.flushAll();
		 
		//请求服务器静默关闭连接
		 jedis.quit();
		 
		 //关闭redis的另一种做法。。更底层点。不知道有什么区别
		 jedis.disconnect();
		 
	}
	/**
	 * redis的管道流技术，可以实现批量保存，批量读取服务器反馈
	 * 减少因为网络延迟而造成的效率低下
	 * 效果拔群，实际测试会比较迷，并不是执行sync就全部把请求发出去。
	 * 当set的量积攒到一定度的时候，才会一次性请求，而非人工控制
	 * 但可以肯定的是，执行sync后所有的请求都必须完成
	 */
	@Test
	public void pipelineExample(){
		Jedis jedis = JedisUitl.getJedis();
		Pipeline  pipeline=jedis.pipelined();
		for(int i=0;i<5000;i++){
			pipeline.set("key"+i, "value"+i);
		}
		pipeline.set("wocao","niniubu");
		pipeline.sync();
		jedis.close();
	}
	
	/**
	 * redis池的使用技能
	 * 目标：建50个线程，每个线程get/set100000次
	 * @throws InterruptedException 
	 */
	@Test
	public void redisPoolExampel() throws InterruptedException{
		JedisUitl.cleanRedis(null);
		final JedisPool pool = new JedisPool(new GenericObjectPoolConfig(), "centos", 6379, 2000, "10086");
		//该对象可以实现原子性的自增，不过，VM优化无效，酌情使用
		final AtomicInteger atomicInteger = new AtomicInteger();
		//保存着50个线程的集合，用于kill
		List<Thread> threads = new ArrayList<Thread>();
		//下面建50个线程，执行并将线程对象存入集合中
		for(int i=0;i<50;i++){
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					//黑科技，循环的另一种不为人知的手法
					for(int i=0;(i=atomicInteger.getAndIncrement())<10000;){
						String key = "foo"+i;
						String value = "bar"+i;
						Jedis jedis = pool.getResource();
						jedis.set(key,value);
						System.out.println("来看看多线程，大循环取出的都是些什么鬼吧"+jedis.get(key));
						//使用完连接要手动归还，没错，close也担任归还连接的重任
						jedis.close();
					}
				}
			});
			threads.add(thread);
			thread.start();
		}
		//主线程挂起，等待50个线程执行完毕
		for(Thread  thread:threads){
			thread.join();
		}
		JedisUitl.closePool(pool);
	}
	/**
	 * 演示redis的协议的用法
	 * 并用这个协议简单的执行set命令，而不是简单的jedis.set
	 * 关于redis的协议的更多信息，请参考readme.md文件
	 */
	public void redisProtocol(){
		
	}
	
	
	
}
