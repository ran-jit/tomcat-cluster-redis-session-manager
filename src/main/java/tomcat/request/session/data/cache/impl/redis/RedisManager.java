package tomcat.request.session.data.cache.impl.redis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.util.Pool;
import tomcat.request.session.data.cache.DataCache;
import tomcat.request.session.data.cache.DataCacheConstants;

/** author: Ranjith Manickam @ 12 Jul' 2018 */
abstract class RedisManager implements DataCache {

    private static final int NUM_RETRIES = 3;
    private static final Log LOGGER = LogFactory.getLog(RedisManager.class);

    private final Pool<Jedis> pool;
    private final long failiureWaitTime;

    RedisManager(Pool<Jedis> pool, long failiureWaitTime) {
        this.pool = pool;
        this.failiureWaitTime = failiureWaitTime;
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
        LOGGER.error(DataCacheConstants.REDIS_CONN_FAILED_RETRY_MSG + tries);
        if (tries == NUM_RETRIES) {
            throw ex;
        }
        try {
            Thread.sleep(this.failiureWaitTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
