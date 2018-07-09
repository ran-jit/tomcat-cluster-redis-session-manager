package tomcat.request.session.data.cache.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import tomcat.request.session.data.cache.DataCache;

/**
 * Tomcat clustering with Redis data-cache implementation.
 *
 * Redis stand-alone mode data-cache implementation.
 *
 * @author Ranjith Manickam
 * @since 2.0
 */
class RedisCacheUtil implements DataCache {

  private JedisPool pool;

  private static final int NUM_RETRIES = 3;

  private Log log = LogFactory.getLog(RedisCacheUtil.class);

  RedisCacheUtil(String host, int port, String password, int database, int timeout,
      JedisPoolConfig poolConfig) {
    pool = new JedisPool(poolConfig, host, port, timeout, password, database);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte[] set(String key, byte[] value) {
    int tries = 0;
    boolean sucess = false;
    String retVal = null;
    do {
      tries++;
      try {
        Jedis jedis = pool.getResource();
        retVal = jedis.set(key.getBytes(), value);
        jedis.close();
        sucess = true;
      } catch (JedisConnectionException ex) {
        log.error(RedisConstants.CONN_FAILED_RETRY_MSG + tries);
        if (tries == NUM_RETRIES) {
          throw ex;
        }
      }
    } while (!sucess && tries <= NUM_RETRIES);
    return (retVal != null) ? retVal.getBytes() : null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long setnx(String key, byte[] value) {
    int tries = 0;
    boolean sucess = false;
    Long retVal = null;
    do {
      tries++;
      try {
        Jedis jedis = pool.getResource();
        retVal = jedis.setnx(key.getBytes(), value);
        jedis.close();
        sucess = true;
      } catch (JedisConnectionException ex) {
        log.error(RedisConstants.CONN_FAILED_RETRY_MSG + tries);
        if (tries == NUM_RETRIES) {
          throw ex;
        }
      }
    } while (!sucess && tries <= NUM_RETRIES);
    return retVal;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long expire(String key, int seconds) {
    int tries = 0;
    boolean sucess = false;
    Long retVal = null;
    do {
      tries++;
      try {
        Jedis jedis = pool.getResource();
        retVal = jedis.expire(key, seconds);
        jedis.close();
        sucess = true;
      } catch (JedisConnectionException ex) {
        log.error(RedisConstants.CONN_FAILED_RETRY_MSG + tries);
        if (tries == NUM_RETRIES) {
          throw ex;
        }
      }
    } while (!sucess && tries <= NUM_RETRIES);
    return retVal;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte[] get(String key) {
    int tries = 0;
    boolean sucess = false;
    byte[] retVal = null;
    do {
      tries++;
      try {
        Jedis jedis = pool.getResource();
        retVal = jedis.get(key.getBytes());
        jedis.close();
        sucess = true;
      } catch (JedisConnectionException ex) {
        log.error(RedisConstants.CONN_FAILED_RETRY_MSG + tries);
        if (tries == NUM_RETRIES) {
          throw ex;
        }
      }
    } while (!sucess && tries <= NUM_RETRIES);
    return retVal;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long delete(String key) {
    int tries = 0;
    boolean sucess = false;
    Long retVal = null;
    do {
      tries++;
      try {
        Jedis jedis = pool.getResource();
        retVal = jedis.del(key);
        jedis.close();
        sucess = true;
      } catch (JedisConnectionException ex) {
        log.error(RedisConstants.CONN_FAILED_RETRY_MSG + tries);
        if (tries == NUM_RETRIES) {
          throw ex;
        }
      }
    } while (!sucess && tries <= NUM_RETRIES);
    return retVal;
  }
}