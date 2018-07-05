package tomcat.request.session.data.cache.impl;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisClusterMaxRedirectionsException;
import redis.clients.jedis.exceptions.JedisConnectionException;
import tomcat.request.session.data.cache.DataCache;

/**
 * Tomcat clustering with Redis data-cache implementation.
 * 
 * Redis multiple node cluster data-cache implementation.
 * 
 * @author Ranjith Manickam
 * @since 2.0
 */
class RedisClusterCacheUtil implements DataCache {

	private JedisCluster cluster;

	private static final int NUM_RETRIES = 30;
	private static final int DEFAULT_MAX_REDIRECTIONS = 5;

	private Log log = LogFactory.getLog(RedisClusterCacheUtil.class);

	public RedisClusterCacheUtil(Set<HostAndPort> nodes, String password, int timeout, JedisPoolConfig poolConfig) {
		cluster = new JedisCluster(nodes, timeout, Protocol.DEFAULT_TIMEOUT, DEFAULT_MAX_REDIRECTIONS, password, poolConfig);
	}

	/** {@inheritDoc} */
	@Override
	public byte[] set(String key, byte[] value) {
		int tries = 0;
		boolean sucess = false;
		String retVal = null;
		do {
			tries++;
			try {
				retVal = cluster.set(key.getBytes(), value);
				sucess = true;
			} catch (JedisClusterMaxRedirectionsException | JedisConnectionException ex) {
				log.error(RedisConstants.CONN_FAILED_RETRY_MSG + tries);
				if (tries == NUM_RETRIES) {
					throw ex;
				}
				waitforFailover();
			}
		} while (!sucess && tries <= NUM_RETRIES);
		return (retVal != null) ? retVal.getBytes() : null;
	}

	/** {@inheritDoc} */
	@Override
	public Long setnx(String key, byte[] value) {
		int tries = 0;
		boolean sucess = false;
		Long retVal = null;
		do {
			tries++;
			try {
				retVal = cluster.setnx(key.getBytes(), value);
				sucess = true;
			} catch (JedisClusterMaxRedirectionsException | JedisConnectionException ex) {
				log.error(RedisConstants.CONN_FAILED_RETRY_MSG + tries);
				if (tries == NUM_RETRIES) {
					throw ex;
				}
				waitforFailover();
			}
		} while (!sucess && tries <= NUM_RETRIES);
		return retVal;
	}

	/** {@inheritDoc} */
	@Override
	public Long expire(String key, int seconds) {
		int tries = 0;
		boolean sucess = false;
		Long retVal = null;
		do {
			tries++;
			try {
				retVal = cluster.expire(key, seconds);
				sucess = true;
			} catch (JedisClusterMaxRedirectionsException | JedisConnectionException ex) {
				log.error(RedisConstants.CONN_FAILED_RETRY_MSG + tries);
				if (tries == NUM_RETRIES) {
					throw ex;
				}
				waitforFailover();
			}
		} while (!sucess && tries <= NUM_RETRIES);
		return retVal;
	}

	/** {@inheritDoc} */
	@Override
	public byte[] get(String key) {
		int tries = 0;
		boolean sucess = false;
		byte[] retVal = null;
		do {
			tries++;
			try {
				retVal = cluster.get(key.getBytes());
				sucess = true;
			} catch (JedisClusterMaxRedirectionsException | JedisConnectionException ex) {
				log.error(RedisConstants.CONN_FAILED_RETRY_MSG + tries);
				if (tries == NUM_RETRIES) {
					throw ex;
				}
				waitforFailover();
			}
		} while (!sucess && tries <= NUM_RETRIES);
		return retVal;
	}

	/** {@inheritDoc} */
	@Override
	public Long delete(String key) {
		int tries = 0;
		boolean sucess = false;
		Long retVal = null;
		do {
			tries++;
			try {
				retVal = cluster.del(key);
				sucess = true;
			} catch (JedisClusterMaxRedirectionsException | JedisConnectionException ex) {
				log.error(RedisConstants.CONN_FAILED_RETRY_MSG + tries);
				if (tries == NUM_RETRIES) {
					throw ex;
				}
				waitforFailover();
			}
		} while (!sucess && tries <= NUM_RETRIES);
		return retVal;
	}

	/**
	 * To wait for handling redis fail-over
	 */
	private void waitforFailover() {
		try {
			Thread.sleep(4000);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
}
