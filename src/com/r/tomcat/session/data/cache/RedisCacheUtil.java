package com.r.tomcat.session.data.cache;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Tomcat clustering implementation
 * 
 * This cache util is uses to store and retrieve the session object in Redis data cache non cluster
 *
 * @author Ranjith Manickam
 * @since 1.0
 */
public class RedisCacheUtil implements IRequestSessionCacheUtils
{
	private Log log = LogFactory.getLog(RedisCacheUtil.class);

	public boolean available = false;

	private static int numRetries = 3;

	private RedisManager manager = null;

	RedisCacheUtil(Properties properties) throws Exception {
		try {
			manager = RedisManager.createInstance(properties);
		} catch (Exception e) {
			this.available = false;
			log.error("Exception initializing Redis: ", e);
		}
		this.available = true;
	}

	@Override
	public boolean isAvailable() {
		return available;
	}

	@Override
	public void setByteArray(String key, byte[] value) {
		int tries = 0;
		boolean sucess = false;
		do {
			tries++;
			try {
				if (key != null && value != null) {
					Jedis jedis = manager.getJedis();
					jedis.set(key.getBytes(), value);
					jedis.close();
				}
				sucess = true;
			} catch (JedisConnectionException e) {
				log.error("Jedis connection failed, retrying..." + tries);
				if (tries == numRetries) {
					throw e;
				}
			}
		} while (!sucess && tries <= numRetries);
	}

	@Override
	public Long setStringIfKeyNotExists(byte[] key, byte[] value) {
		int tries = 0;
		Long retVal = null;
		boolean sucess = false;
		do {
			tries++;
			try {
				if (key != null && value != null) {
					Jedis jedis = manager.getJedis();
					retVal = jedis.setnx(key, value);
					jedis.close();
				}
				sucess = true;
			} catch (JedisConnectionException e) {
				log.error("Jedis connection failed, retrying..." + tries);
				if (tries == numRetries) {
					throw e;
				}
			}
		} while (!sucess && tries <= numRetries);
		return retVal;
	}

	@Override
	public void expire(String key, int ttl) {
		int tries = 0;
		boolean sucess = false;
		do {
			tries++;
			try {
				Jedis jedis = manager.getJedis();
				jedis.expire(key, ttl);
				jedis.close();
				sucess = true;
			} catch (JedisConnectionException e) {
				log.error("Jedis connection failed, retrying..." + tries);
				if (tries == numRetries) {
					throw e;
				}
			}
		} while (!sucess && tries <= numRetries);
	}

	@Override
	public byte[] getByteArray(String key) {
		int tries = 0;
		boolean sucess = false;
		byte[] array = new byte[1];
		do {
			tries++;
			try {
				if (key != null) {
					Jedis jedis = manager.getJedis();
					array = jedis.get(key.getBytes());
					jedis.close();
				}
				sucess = true;
			} catch (JedisConnectionException e) {
				log.error("Jedis connection failed, retrying..." + tries);
				if (tries == numRetries) {
					throw e;
				}
			}
		} while (!sucess && tries <= numRetries);
		return array;
	}

	@Override
	public void deleteKey(String key) {
		int tries = 0;
		boolean sucess = false;
		do {
			tries++;
			try {
				if (key != null) {
					Jedis jedis = manager.getJedis();
					jedis.del(key);
					jedis.close();
				}
				sucess = true;
			} catch (JedisConnectionException e) {
				log.error("Jedis connection failed, retrying..." + tries);
				if (tries == numRetries) {
					throw e;
				}
			}
		} while (!sucess && tries <= numRetries);
	}
}