package tomcat.request.session.data.cache.impl.redis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import tomcat.request.session.SessionConstants;
import tomcat.request.session.data.cache.DataCache;
import tomcat.request.session.data.cache.impl.redis.RedisConstants.RedisConfigType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/** author: Ranjith Manickam @ 12 Jul' 2018 */
public class RedisCache implements DataCache {

    private static DataCache dataCache;

    private static final Log LOGGER = LogFactory.getLog(RedisCache.class);

    public RedisCache() {
        initialize();
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

    /** To initialize the redis data cache. */
    private void initialize() {
        if (dataCache != null) {
            return;
        }
        Properties properties = getProperties();

        RedisConfigType configType;
        if (Boolean.valueOf(properties.getProperty(RedisConstants.CLUSTER_ENABLED))) {
            configType = RedisConfigType.CLUSTER;
        } else if (Boolean.valueOf(properties.getProperty(RedisConstants.SENTINEL_ENABLED))) {
            configType = RedisConfigType.SENTINEL;
        } else {
            configType = RedisConfigType.DEFAULT;
        }

        String hosts = properties.getProperty(RedisConstants.HOSTS, Protocol.DEFAULT_HOST.concat(":").concat(String.valueOf(Protocol.DEFAULT_PORT)));
        Collection<?> nodes = getJedisNodes(hosts, configType);

        String password = properties.getProperty(RedisConstants.PASSWORD);
        password = (password != null && !password.isEmpty()) ? password : null;

        int database = Integer.parseInt(properties.getProperty(RedisConstants.DATABASE, String.valueOf(Protocol.DEFAULT_DATABASE)));

        int timeout = Integer.parseInt(properties.getProperty(RedisConstants.TIMEOUT, String.valueOf(Protocol.DEFAULT_TIMEOUT)));
        timeout = (timeout < Protocol.DEFAULT_TIMEOUT) ? Protocol.DEFAULT_TIMEOUT : timeout;

        JedisPoolConfig poolConfig = getPoolConfig(properties);
        switch (configType) {
            case CLUSTER:
                dataCache = new RedisClusterUtil((Set<HostAndPort>) nodes, password, timeout, poolConfig);
                break;
            case SENTINEL:
                String masterName = String.valueOf(properties.getProperty(RedisConstants.SENTINEL_MASTER, RedisConstants.DEFAULT_SENTINEL_MASTER));
                dataCache = new RedisSentinelUtil((Set<String>) nodes, masterName, password, database, timeout, poolConfig);
                break;
            default:
                dataCache = new RedisUtil(((List<String>) nodes).get(0), Integer.parseInt(((List<String>) nodes).get(1)), password, database, timeout, poolConfig);
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
        int maxActive = Integer.parseInt(properties.getProperty(RedisConstants.MAX_ACTIVE, RedisConstants.DEFAULT_MAX_ACTIVE_VALUE));
        poolConfig.setMaxTotal(maxActive);

        boolean testOnBorrow = Boolean.parseBoolean(properties.getProperty(RedisConstants.TEST_ONBORROW, RedisConstants.DEFAULT_TEST_ONBORROW_VALUE));
        poolConfig.setTestOnBorrow(testOnBorrow);

        boolean testOnReturn = Boolean.parseBoolean(properties.getProperty(RedisConstants.TEST_ONRETURN, RedisConstants.DEFAULT_TEST_ONRETURN_VALUE));
        poolConfig.setTestOnReturn(testOnReturn);

        int maxIdle = Integer.parseInt(properties.getProperty(RedisConstants.MAX_ACTIVE, RedisConstants.DEFAULT_MAX_ACTIVE_VALUE));
        poolConfig.setMaxIdle(maxIdle);

        int minIdle = Integer.parseInt(properties.getProperty(RedisConstants.MIN_IDLE, RedisConstants.DEFAULT_MIN_IDLE_VALUE));
        poolConfig.setMinIdle(minIdle);

        boolean testWhileIdle = Boolean.parseBoolean(properties.getProperty(RedisConstants.TEST_WHILEIDLE, RedisConstants.DEFAULT_TEST_WHILEIDLE_VALUE));
        poolConfig.setTestWhileIdle(testWhileIdle);

        int testNumPerEviction = Integer.parseInt(properties.getProperty(RedisConstants.TEST_NUMPEREVICTION, RedisConstants.DEFAULT_TEST_NUMPEREVICTION_VALUE));
        poolConfig.setNumTestsPerEvictionRun(testNumPerEviction);

        long timeBetweenEviction = Long.parseLong(properties.getProperty(RedisConstants.TIME_BETWEENEVICTION, RedisConstants.DEFAULT_TIME_BETWEENEVICTION_VALUE));
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

    /** To get redis data cache properties. */
    private Properties getProperties() {
        Properties properties = new Properties();
        try {
            String filePath = System.getProperty(SessionConstants.CATALINA_BASE).concat(File.separator)
                    .concat(SessionConstants.CONF).concat(File.separator)
                    .concat(RedisConstants.PROPERTIES_FILE);

            InputStream resourceStream = null;
            try {
                resourceStream = (!filePath.isEmpty() && new File(filePath).exists()) ? new FileInputStream(filePath) : null;

                if (resourceStream == null) {
                    ClassLoader loader = Thread.currentThread().getContextClassLoader();
                    resourceStream = loader.getResourceAsStream(RedisConstants.PROPERTIES_FILE);
                }
                properties.load(resourceStream);
            } finally {
                if (resourceStream != null) {
                    resourceStream.close();
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Error while loading task scheduler properties", ex);
        }
        return properties;
    }

}
