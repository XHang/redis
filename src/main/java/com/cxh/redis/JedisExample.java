package com.cxh.redis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.util.Hashing;
import redis.clients.util.SafeEncoder;

public class JedisExample {
	
	private static Jedis jedis;
	@After
	public void cleanRedis(){
		System.out.println("清理犯罪现场");
		JedisUitl.cleanRedis(jedis);
		JedisUitl.closeRedis(jedis);
	}
	@Before
	public void init(){
		jedis=JedisUitl.getJedis();
		System.out.println("一屋不扫何以扫天下");
		JedisUitl.cleanRedis(jedis);
		
	}
	
	
	
	/**
	 * 利用redis，设置String的key和value，并取出。
	 */
	@Test
	public void setGetString(){
		 
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
	 * 测试jedis  exists方法，用于判断键值是否存在redis
	 */
	@Test
	public  void existsKey(){
		String key = "Set";
		String value= "Value";
		 
		jedis.set(key,value);
		//该方法有重载，传入多个key，可以判断有多少个key是存在的，返回存在的个数
		boolean exists = jedis.exists(key);
		//这个是静态导入，所以不需要写类名
		 assertTrue(exists);
		JedisUitl.closeRedis(jedis);
	}
	/**
	 * 测试jedis的删除方法
	 */
	@Test
	public  void del(){
		String key1= "key1"; String value1 = "value1";
		String key2 = "key2"; String value2 = "value2";
		String key3 = "key3"; String value3 ="value3";
		 
		jedis.set(key1, value1);
		jedis.set(key2, value2);
		jedis.set(key3, value3);
		//删除传进去的键列表，返回删除成功的个数
		long delCount = jedis.del(key1,key2,key3);
		assertEquals(delCount, 3L);
		JedisUitl.closeRedis(jedis);
	}
	/**
	 * 测试jedis的type方法：传一个key，判断给key的value是什么类型
	 * 类型主要有几种：
	 * none
	 * string
	 * list
	 * set
	 */
	@Test
	public void type(){
		String key = "key";
		String value = "value";
		 
		jedis.set(key, value);
		String type = jedis.type(key);
		assertEquals("string",type);
		JedisUitl.closeRedis(jedis);
	}
	
	/**
	 * 模糊搜索key，把符合条件的key全选出来
	 * 不建议在生产环境使用，可能会降低效率，破坏数据库性能
	 */
	@Test
	public void keys(){
		 
		jedis.set("key", "vue");
		jedis.set("key1", "key2");
		Set<String > result = jedis.keys("key*");
		//预期执行结果为key和key1
		Set<String> expected  = new HashSet<String>();
		expected.add("key");
		expected.add("key1");
		assertEquals(expected, result);
		JedisUitl.closeRedis(jedis);
	}
	
		/**
		 * randomKey从当前的redis随机选一个key
		 */
	  @Test
	  public void randomKey() {
		 Jedis  jedis = JedisUitl.getJedis();
		 //从一无所有的redis取东西，当然就什么都没有了
	    assertEquals(null, jedis.randomKey());

	    jedis.set("foo", "bar");
	    //只有一个键的情况下当然就只取一个咯
	    assertEquals("foo", jedis.randomKey());

	    JedisUitl.closeRedis(jedis);

	}
	  /**
	   * 测试重命名功能，将key重命名，如果重命名的key已存在，那个已存在的key-value会删除，新的key取而代之
	   * 同胞兄弟：renamenx
	   * 如果重命名后的key已存在，返回状态码0，表示重命名失败
	   */
	  @Test
	  public void rename(){
		   
		   jedis.set("key", "value");
		   String status = jedis.rename("key", "newkey");
		  //断言一下看重命名成功了没
		  assertEquals("OK", status);
		 boolean exists =  jedis.exists("newkey");
		 //测试重命名的key在redis是否存在
		 assertTrue(exists);
		  JedisUitl.closeRedis(jedis);
	  }
	  
	  /**
	   * dbSize方法，测试redis数据库里面拥有多少key
	   */
	  @Test
	  public void deSize(){
		   
		  long size = jedis.dbSize();
		    assertEquals(0, size);
		    jedis.set("foo", "bar");
		    size = jedis.dbSize();
		    assertEquals(1, size);
			JedisUitl.closeRedis(jedis);
	  }
	  /**
	   * 测试超时设置,顺便演示下ttl，它用来看key还有多少寿命来着
	   * 孪生兄弟：expireAt 可以指定时间戳作为超时时间，换句话说，你可以指定在某年某月某日过时
	   * @throws InterruptedException
	   */
	  @Test
	  public void expire() throws InterruptedException{
		  //指定一个key300秒后超时，但是现在数据库一无所有，所以执行失败，返回0
		  long status = jedis.expire("NONE", 300);
		  assertEquals(0L, status);
		  
		  jedis.set("key", "value");
		  //断言设置过期成功否
		  status = jedis.expire("key", 10);
		  //ttl如果没有该key或者key永不过期，返回-1
		 long laveTime =  jedis.ttl("key");
		 System.out.println("key的寿命还有"+laveTime+"秒");
		  assertEquals(1L, status);
		  boolean exists =  jedis.exists("key");
		  //立即查看该键存在否
		  assertTrue(exists);
		  Thread.sleep(10000L);
		  //10秒后看看该键删除否
		  exists =  jedis.exists("key");
		  assertFalse(exists);
		 JedisUitl.closeRedis(jedis);
	  }
	  
	  /**
	   * 演示一下redis的分区功能：分区内的数据互不干扰
	   * 默认客户端连的是分区0
	   */
	  @Test
	  public void select(){
		  	jedis.set("foo", "bar");
		    String status = jedis.select(1);
		    //测试分区切换是否成功
		    assertEquals("OK", status);
		    //切换分区1后，分区0的数据不会读取到
		    assertEquals(null, jedis.get("foo"));
		    //再次切换为分区0，读取之前设置的数据
		    status = jedis.select(0);
		    assertEquals("OK", status);
		    assertEquals("bar", jedis.get("foo"));
	  }
	  /**
	   * 虽然没有注释但是一看就知道getDB是获取当前客户端在哪个分区内的
	   */
	  @Test
	  public void getDB() {
		 Assert.assertEquals(new Long(0), jedis.getDB());
	    jedis.select(1);
	    Assert.assertEquals(new Long(1), jedis.getDB());
	  }	  
	  
	  /**
	   * 测试move方法，将指定的key-value迁移到另一个分区内
	   */
	  @Test
	  public void move() {
		 //将一个不存在的key-value迁移到另一个分区，当然失败
	    long status = jedis.move("foo", 1);
	    assertEquals(0, status);
	    
	    //set一个value，并迁移到另一个分区
	    jedis.set("foo", "bar");
	    status = jedis.move("foo", 1);
	    assertEquals(1, status);
	    assertEquals(null, jedis.get("foo"));
	    //客户端访问另一个分区，并读取之前迁移的键值对
	    jedis.select(1);
	    assertEquals("bar", jedis.get("foo"));
	}
	  
}
