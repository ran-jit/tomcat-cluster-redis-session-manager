package tomcat.request.session.data.cache.impl.redis;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/** author: Ranjith Manickam @ 12 Jul' 2018 */
class RedisUtil extends AbstractRedisUtil {

    private static final long FAILIURE_WAIT_TIME = 2000L;

    RedisUtil(String host,
              int port,
              String password,
              int database,
              int timeout,
              JedisPoolConfig poolConfig) {
        super(new JedisPool(poolConfig, host, port, timeout, password, database), FAILIURE_WAIT_TIME);
    }

}
