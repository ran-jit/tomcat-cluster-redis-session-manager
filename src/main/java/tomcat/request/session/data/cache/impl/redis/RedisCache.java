package tomcat.request.session.data.cache.impl.redis;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;
import tomcat.request.session.data.cache.DataCache;
import tomcat.request.session.model.Config;
import tomcat.request.session.model.Config.RedisConfigType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** author: Ranjith Manickam @ 12 Jul' 2018 */
public class RedisCache implements DataCache {

    private DataCache dataCache;

    public RedisCache(Config config) {
        initialize(config);
    }

    /** {@inheritDoc} */
    @Override
    public byte[] set(String key, byte[] value) {
        return this.dataCache.set(key, value);
    }

    /** {@inheritDoc} */
    @Override
    public Long setnx(String key, byte[] value) {
        return this.dataCache.setnx(key, value);
    }

    /** {@inheritDoc} */
    @Override
    public Long expire(String key, int seconds) {
        return this.dataCache.expire(key, seconds);
    }

    /** {@inheritDoc} */
    @Override
    public byte[] get(String key) {
        return this.dataCache.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public Long delete(String key) {
        return this.dataCache.delete(key);
    }

    /** {@inheritDoc} */
    @Override
    public Boolean exists(String key) {
        return this.dataCache.exists(key);
    }

    private void initialize(Config config) {
        Collection<?> nodes = getJedisNodes(config.getRedisHosts(), config.getRedisConfigType());
        JedisPoolConfig poolConfig = getPoolConfig(config);
        switch (config.getRedisConfigType()) {
            case CLUSTER:
                this.dataCache = new RedisClusterManager((Set<HostAndPort>) nodes,
                        config.getRedisPassword(),
                        config.getRedisTimeout(),
                        poolConfig);
                break;
            case SENTINEL:
                this.dataCache = new RedisSentinelManager((Set<String>) nodes,
                        config.getRedisSentinelMaster(),
                        config.getRedisPassword(),
                        config.getRedisDatabase(),
                        config.getRedisTimeout(),
                        poolConfig);
                break;
            default:
                this.dataCache = new RedisStandardManager(((List<String>) nodes).get(0),
                        Integer.parseInt(((List<String>) nodes).get(1)),
                        config.getRedisPassword(),
                        config.getRedisDatabase(),
                        config.getRedisTimeout(),
                        poolConfig);
                break;
        }
    }

    /**
     * To get redis pool config.
     *
     * @param config - Application config.
     * @return - Returns the redis pool config.
     */
    private JedisPoolConfig getPoolConfig(Config config) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(config.getRedisMaxActive());
        poolConfig.setTestOnBorrow(config.getRedisTestOnBorrow());
        poolConfig.setTestOnReturn(config.getRedisTestOnReturn());
        poolConfig.setMaxIdle(config.getRedisMaxIdle());
        poolConfig.setMinIdle(config.getRedisMinIdle());
        poolConfig.setTestWhileIdle(config.getRedisTestWhileIdle());
        poolConfig.setNumTestsPerEvictionRun(config.getRedisTestNumPerEviction());
        poolConfig.setTimeBetweenEvictionRunsMillis(config.getRedisTimeBetweenEviction());
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
                    nodes.add(new HostAndPort(hostPortArr[0], Integer.parseInt(hostPortArr[1])));
                    break;
                case SENTINEL:
                    nodes = (nodes == null) ? new HashSet<>() : nodes;
                    nodes.add(new HostAndPort(hostPortArr[0], Integer.parseInt(hostPortArr[1])).toString());
                    break;
                default:
                    int port = Integer.parseInt(hostPortArr[1]);
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
