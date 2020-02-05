package tomcat.request.session.data.cache.impl.redis;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import java.util.Set;

/** author: Ranjith Manickam @ 3 Dec' 2018 */
class RedisSentinelManager extends RedisManager {

    private static final long FAILURE_WAIT_TIME = 2000L;

    RedisSentinelManager(Set<String> nodes,
                         String masterName,
                         String password,
                         int database,
                         int timeout,
                         JedisPoolConfig poolConfig) {
        super(new JedisSentinelPool(masterName, nodes, poolConfig, timeout, password, database), FAILURE_WAIT_TIME);
    }
}
