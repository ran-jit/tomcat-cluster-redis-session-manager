package tomcat.request.session.data.cache.impl.redis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import tomcat.request.session.data.cache.DataCache;
import tomcat.request.session.data.cache.DataCacheConstants;
import tomcat.request.session.data.cache.DataCacheConstants.RedisConfigType;
import tomcat.request.session.data.cache.DataCacheFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/** author: Ranjith Manickam @ 12 Jul' 2018 */
public class RedisCache implements DataCache {

    private DataCache dataCache;

    public RedisCache(Properties properties) {
        initialize(properties);
    }

    /** {@inheritDoc} */
    @Override
    public byte[] set(String key, byte[] value) {
        return dataCache.set(key, value);
    }

    /** {@inheritDoc} */
    @Override
    public Long setnx(String key, byte[] value) {
        return dataCache.setnx(key, value);
    }

    /** {@inheritDoc} */
    @Override
    public Long expire(String key, int seconds) {
        return dataCache.expire(key, seconds);
    }

    /** {@inheritDoc} */
    @Override
    public byte[] get(String key) {
        return dataCache.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public Long delete(String key) {
        return dataCache.delete(key);
    }

    private void initialize(Properties properties) {
        RedisConfigType configType;
        if (Boolean.valueOf(DataCacheFactory.getProperty(properties, DataCacheConstants.REDIS_CLUSTER_ENABLED))) {
            configType = RedisConfigType.CLUSTER;
        } else if (Boolean.valueOf(DataCacheFactory.getProperty(properties, DataCacheConstants.REDIS_SENTINEL_ENABLED))) {
            configType = RedisConfigType.SENTINEL;
        } else {
            configType = RedisConfigType.DEFAULT;
        }

        String hosts = DataCacheFactory.getProperty(properties, DataCacheConstants.REDIS_HOSTS, String.format("%s:%s", Protocol.DEFAULT_HOST, Protocol.DEFAULT_PORT));
        Collection<?> nodes = getJedisNodes(hosts, configType);

        String password = DataCacheFactory.getProperty(properties, DataCacheConstants.REDIS_PASSWORD);
        password = (password != null && !password.isEmpty()) ? password : null;

        int database = Integer.parseInt(DataCacheFactory.getProperty(properties, DataCacheConstants.REDIS_DATABASE));

        int timeout = Integer.parseInt(DataCacheFactory.getProperty(properties, DataCacheConstants.REDIS_TIMEOUT));
        timeout = (timeout < Protocol.DEFAULT_TIMEOUT) ? Protocol.DEFAULT_TIMEOUT : timeout;

        JedisPoolConfig poolConfig = getPoolConfig(properties);
        switch (configType) {
            case CLUSTER:
                dataCache = new RedisClusterManager((Set<HostAndPort>) nodes, password, timeout, poolConfig);
                break;
            case SENTINEL:
                String masterName = String.valueOf(DataCacheFactory.getProperty(properties, DataCacheConstants.REDIS_SENTINEL_MASTER));
                dataCache = new RedisSentinelManager((Set<String>) nodes, masterName, password, database, timeout, poolConfig);
                break;
            default:
                dataCache = new RedisStandardManager(((List<String>) nodes).get(0), Integer.parseInt(((List<String>) nodes).get(1)), password, database, timeout, poolConfig);
                break;
        }
    }

    /**
     * To get redis pool config.
     *
     * @param properties - Redis data cache properties.
     * @return - Returns the redis pool config.
     */
    private JedisPoolConfig getPoolConfig(Properties properties) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        int maxActive = Integer.parseInt(DataCacheFactory.getProperty(properties, DataCacheConstants.REDIS_MAX_ACTIVE));
        poolConfig.setMaxTotal(maxActive);

        boolean testOnBorrow = Boolean.parseBoolean(DataCacheFactory.getProperty(properties, DataCacheConstants.REDIS_TEST_ONBORROW));
        poolConfig.setTestOnBorrow(testOnBorrow);

        boolean testOnReturn = Boolean.parseBoolean(DataCacheFactory.getProperty(properties, DataCacheConstants.REDIS_TEST_ONRETURN));
        poolConfig.setTestOnReturn(testOnReturn);

        int maxIdle = Integer.parseInt(DataCacheFactory.getProperty(properties, DataCacheConstants.REDIS_MAX_ACTIVE));
        poolConfig.setMaxIdle(maxIdle);

        int minIdle = Integer.parseInt(DataCacheFactory.getProperty(properties, DataCacheConstants.REDIS_MIN_IDLE));
        poolConfig.setMinIdle(minIdle);

        boolean testWhileIdle = Boolean.parseBoolean(DataCacheFactory.getProperty(properties, DataCacheConstants.REDIS_TEST_WHILEIDLE));
        poolConfig.setTestWhileIdle(testWhileIdle);

        int testNumPerEviction = Integer.parseInt(DataCacheFactory.getProperty(properties, DataCacheConstants.REDIS_TEST_NUMPEREVICTION));
        poolConfig.setNumTestsPerEvictionRun(testNumPerEviction);

        long timeBetweenEviction = Long.parseLong(DataCacheFactory.getProperty(properties, DataCacheConstants.REDIS_TIME_BETWEENEVICTION));
        poolConfig.setTimeBetweenEvictionRunsMillis(timeBetweenEviction);
        return poolConfig;
    }

    /**
     * To get redis data cache nodes.
     *
     * @param hosts      - redis server hosts.
     * @param configType - redis data config type.
     * @return - Returns the redis nodes.
     */
    private Collection<?> getJedisNodes(String hosts, RedisConfigType configType) {
        hosts = hosts.replaceAll("\\s", "");
        String[] hostPorts = hosts.split(",");

        Set<Object> nodes = null;

        for (String hostPort : hostPorts) {
            String[] hostPortArr = hostPort.split(":");

            switch (configType) {
                case CLUSTER:
                    nodes = (nodes == null) ? new HashSet<>() : nodes;
                    nodes.add(new HostAndPort(hostPortArr[0], Integer.valueOf(hostPortArr[1])));
                    break;
                case SENTINEL:
                    nodes = (nodes == null) ? new HashSet<>() : nodes;
                    nodes.add(new HostAndPort(hostPortArr[0], Integer.valueOf(hostPortArr[1])).toString());
                    break;
                default:
                    int port = Integer.valueOf(hostPortArr[1]);
                    if (!hostPortArr[0].isEmpty() && port > 0) {
                        List<String> node = new ArrayList<>();
                        node.add(hostPortArr[0]);
                        node.add(String.valueOf(port));
                        return node;
                    }
            }
        }
        return nodes;
    }
}
