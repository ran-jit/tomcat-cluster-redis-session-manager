package tomcat.request.session.data.cache.impl.redis;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/** author: Ranjith Manickam @ 12 Jul' 2018 */
class RedisStandardManager extends RedisManager {

    private static final long FAILURE_WAIT_TIME = 2000L;

    RedisStandardManager(String host,
                         int port,
                         String password,
                         int database,
                         int timeout,
                         JedisPoolConfig poolConfig) {
        super(new JedisPool(poolConfig, host, port, timeout, password, database), FAILURE_WAIT_TIME);
    }
}
