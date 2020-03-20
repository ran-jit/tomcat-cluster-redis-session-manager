package tomcat.request.session.data.cache.impl.redis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisRedirectionException;

import java.util.Set;

/** author: Ranjith Manickam @ 12 Jul' 2018 */
class RedisClusterManager extends RedisManager {

    private final JedisCluster cluster;

    private static final int NUM_RETRIES = 30;
    private static final int DEFAULT_MAX_RE_DIRECTIONS = 5;
    private static final long FAILURE_WAIT_TIME = 4000L;

    RedisClusterManager(Set<HostAndPort> nodes,
                        String password,
                        int timeout,
                        JedisPoolConfig poolConfig) {
        super(null, FAILURE_WAIT_TIME);
        this.cluster = new JedisCluster(nodes, timeout, Protocol.DEFAULT_TIMEOUT, DEFAULT_MAX_RE_DIRECTIONS, password, poolConfig);
    }

    /** {@inheritDoc} */
    @Override
    public byte[] set(String key, byte[] value) {
        int tries = 0;
        boolean retry = true;
        String retVal = null;
        do {
            tries++;
            try {
                retVal = this.cluster.set(key.getBytes(), value);
                retry = false;
            } catch (JedisRedirectionException | JedisConnectionException ex) {
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
            try {
                retVal = this.cluster.setnx(key.getBytes(), value);
                retry = false;
            } catch (JedisRedirectionException | JedisConnectionException ex) {
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
            try {
                retVal = this.cluster.expire(key, seconds);
                retry = false;
            } catch (JedisRedirectionException | JedisConnectionException ex) {
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
            try {
                retVal = this.cluster.get(key.getBytes());
                retry = false;
            } catch (JedisRedirectionException | JedisConnectionException ex) {
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
            try {
                retVal = this.cluster.del(key);
                retry = false;
            } catch (JedisRedirectionException | JedisConnectionException ex) {
                handleException(tries, ex);
            }
        } while (retry && tries <= NUM_RETRIES);
        return retVal;
    }

    /** {@inheritDoc} */
    @Override
    public Boolean exists(String key) {
        int tries = 0;
        boolean retry = true;
        Boolean retVal = null;
        do {
            tries++;
            try {
                retVal = this.cluster.exists(key);
                retry = false;
            } catch (JedisRedirectionException | JedisConnectionException ex) {
                handleException(tries, ex);
            }
        } while (retry && tries <= NUM_RETRIES);
        return retVal;
    }
}
