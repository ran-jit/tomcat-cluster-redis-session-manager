package tomcat.request.session.data.cache.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import tomcat.request.session.SessionConstants;
import tomcat.request.session.data.cache.DataCache;

/**
 * Tomcat clustering with Redis data-cache implementation.
 * 
 * Redis data-cache implementation to store/retrieve session objects.
 * 
 * @author Ranjith Manickam
 * @since 2.0
 */
public class RedisDataCache implements DataCache {

	private static DataCache dataCache;

	private Log log = LogFactory.getLog(RedisDataCache.class);

	public RedisDataCache() {
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
		return (key != null) ? dataCache.get(key) : null;
	}

	/** {@inheritDoc} */
	@Override
	public Long delete(String key) {
		return dataCache.delete(key);
	}

	/**
	 * To parse data-cache key
	 * 
	 * @param key
	 * @return
	 */
	public static String parseDataCacheKey(String key) {
		return key.replaceAll("\\s", "_");
	}

	/**
	 * To initialize the data-cache
	 * 
	 * @param properties
	 * @param filePath
	 */
	@SuppressWarnings("unchecked")
	private void initialize() {
		if (dataCache != null) {
			return;
		}
		Properties properties = loadProperties();

		boolean clusterEnabled = Boolean.valueOf(properties.getProperty(RedisConstants.CLUSTER_ENABLED, RedisConstants.DEFAULT_CLUSTER_ENABLED));

		String hosts = properties.getProperty(RedisConstants.HOSTS, Protocol.DEFAULT_HOST.concat(":").concat(String.valueOf(Protocol.DEFAULT_PORT)));
		Collection<? extends Serializable> nodes = getJedisNodes(hosts, clusterEnabled);

		String password = properties.getProperty(RedisConstants.PASSWORD);
		password = (password != null && !password.isEmpty()) ? password : null;

		int database = Integer.parseInt(properties.getProperty(RedisConstants.DATABASE, String.valueOf(Protocol.DEFAULT_DATABASE)));

		int timeout = Integer.parseInt(properties.getProperty(RedisConstants.TIMEOUT, String.valueOf(Protocol.DEFAULT_TIMEOUT)));
		timeout = (timeout < Protocol.DEFAULT_TIMEOUT) ? Protocol.DEFAULT_TIMEOUT : timeout;

		if (clusterEnabled) {
			dataCache = new RedisClusterCacheUtil((Set<HostAndPort>) nodes, password, timeout, getPoolConfig(properties));
		} else {
			dataCache = new RedisCacheUtil(((List<String>) nodes).get(0),
					Integer.parseInt(((List<String>) nodes).get(1)), password, database, timeout, getPoolConfig(properties));
		}
	}

	/**
	 * To get jedis pool configuration
	 * 
	 * @param properties
	 * @return
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
	 * To get jedis nodes
	 * 
	 * @param hosts
	 * @param clusterEnabled
	 * @return
	 */
	private Collection<? extends Serializable> getJedisNodes(String hosts, boolean clusterEnabled) {
		hosts = hosts.replaceAll("\\s", "");
		String[] hostPorts = hosts.split(",");

		List<String> node = null;
		Set<HostAndPort> nodes = null;

		for (String hostPort : hostPorts) {
			String[] hostPortArr = hostPort.split(":");

			if (clusterEnabled) {
				nodes = (nodes == null) ? new HashSet<HostAndPort>() : nodes;
				nodes.add(new HostAndPort(hostPortArr[0], Integer.valueOf(hostPortArr[1])));
			} else {
				int port = Integer.valueOf(hostPortArr[1]);
				if (!hostPortArr[0].isEmpty() && port > 0) {
					node = (node == null) ? new ArrayList<String>() : node;
					node.add(hostPortArr[0]);
					node.add(String.valueOf(port));
					break;
				}
			}
		}
		return clusterEnabled ? nodes : node;
	}

	/**
	 * To load data-cache properties
	 * 
	 * @param filePath
	 * @return
	 */
	private Properties loadProperties() {
		Properties properties = new Properties();
		try {
			String filePath = System.getProperty(SessionConstants.CATALINA_BASE).concat(File.separator)
					.concat(SessionConstants.CONF).concat(File.separator).concat(RedisConstants.PROPERTIES_FILE);

			InputStream resourceStream = null;
			try {
				resourceStream = (filePath != null && !filePath.isEmpty() && new File(filePath).exists())
						? new FileInputStream(filePath) : null;

				if (resourceStream == null) {
					ClassLoader loader = Thread.currentThread().getContextClassLoader();
					resourceStream = loader.getResourceAsStream(RedisConstants.PROPERTIES_FILE);
				}
				properties.load(resourceStream);
			} finally {
				resourceStream.close();
			}
		} catch (IOException ex) {
			log.error("Error while loading task scheduler properties", ex);
		}
		return properties;
	}
}