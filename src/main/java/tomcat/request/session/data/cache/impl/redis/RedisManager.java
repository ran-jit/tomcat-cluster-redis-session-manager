package tomcat.request.session.data.cache.impl.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.util.Pool;
import tomcat.request.session.data.cache.DataCache;

/** author: Ranjith Manickam @ 12 Jul' 2018 */
abstract class RedisManager implements DataCache {

    private static final int NUM_RETRIES = 3;
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisManager.class);

    private static final String REDIS_CONN_FAILED_RETRY_MSG = "Jedis connection failed, retrying...";

    private final Pool<Jedis> pool;
    private final long failureWaitTime;

    RedisManager(Pool<Jedis> pool, long failureWaitTime) {
        this.pool = pool;
        this.failureWaitTime = failureWaitTime;
    }

    /** {@inheritDoc} */
    @Override
    public byte[] set(String key, byte[] value) {
        int tries = 0;
        boolean retry = true;
        String retVal = null;
        do {
            tries++;
            try (Jedis jedis = this.pool.getResource()) {
                retVal = jedis.set(key.getBytes(), value);
                retry = false;
            } catch (JedisConnectionException ex) {
                handleException(tries, ex);
            }
        } while (retry && tries <= NUM_RETRIES);
        return (retVal != null) ? retVal.getBytes() : null;
    }

    /** {@inheritDoc} */
    @Override
    public Long setnx(String key, byte[] value) {
        int tries = 0;
        boolean retry = true;
        Long retVal = null;
        do {
            tries++;
            try (Jedis jedis = this.pool.getResource()) {
                retVal = jedis.setnx(key.getBytes(), value);
                retry = false;
            } catch (JedisConnectionException ex) {
                handleException(tries, ex);
            }
        } while (retry && tries <= NUM_RETRIES);
        return retVal;
    }

    /** {@inheritDoc} */
    @Override
    public Long expire(String key, int seconds) {
        int tries = 0;
        boolean retry = true;
        Long retVal = null;
        do {
            tries++;
            try (Jedis jedis = this.pool.getResource()) {
                retVal = jedis.expire(key, seconds);
                retry = false;
            } catch (JedisConnectionException ex) {
                handleException(tries, ex);
            }
        } while (retry && tries <= NUM_RETRIES);
        return retVal;
    }

    /** {@inheritDoc} */
    @Override
    public byte[] get(String key) {
        int tries = 0;
        boolean retry = true;
        byte[] retVal = null;
        do {
            tries++;
            try (Jedis jedis = this.pool.getResource()) {
                retVal = jedis.get(key.getBytes());
                retry = false;
            } catch (JedisConnectionException ex) {
                handleException(tries, ex);
            }
        } while (retry && tries <= NUM_RETRIES);
        return retVal;
    }

    /** {@inheritDoc} */
    @Override
    public Long delete(String key) {
        int tries = 0;
        boolean retry = true;
        Long retVal = null;
        do {
            tries++;
            try (Jedis jedis = this.pool.getResource()) {
                retVal = jedis.del(key);
                retry = false;
            } catch (JedisConnectionException ex) {
                handleException(tries, ex);
            }
        } while (retry && tries <= NUM_RETRIES);
        return retVal;
    }

    /**
     * To handle jedis exception.
     *
     * @param tries - exception occurred in tries.
     * @param ex    - jedis exception.
     */
    void handleException(int tries, RuntimeException ex) {
        LOGGER.error(REDIS_CONN_FAILED_RETRY_MSG + tries);
        if (tries == NUM_RETRIES) {
            throw ex;
        }
        try {
            Thread.sleep(this.failureWaitTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
