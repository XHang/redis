package com.cxh.redisUitl;

import redis.clients.jedis.Jedis;

public class SaveObject {
	
	/**
	 * 通过hash来存入一个自定义对象，不需要实现序列化接口
	 * @param jedis
	 * @param object
	 */
	public void SaveByHash(Jedis jedis,Object object){
		
	}
}
