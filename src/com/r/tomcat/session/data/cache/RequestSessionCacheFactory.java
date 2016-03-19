package com.r.tomcat.session.data.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.r.tomcat.session.data.cache.constants.RedisConstants;

/**
 * Tomcat clustering implementation
 * 
 * This factory is uses to get the request session data cache
 *
 * @author Ranjith Manickam
 * @since 1.0
 */
public class RequestSessionCacheFactory
{
	private static Log log = LogFactory.getLog(RequestSessionCacheFactory.class);

	protected static IRequestSessionCacheUtils requestCache;

	public static synchronized IRequestSessionCacheUtils getInstance() {
		try {
			if (requestCache == null) {
				Properties properties = getRedisProperties();
				if (!Boolean.valueOf(properties.getProperty(RedisConstants.IS_CLUSTER_ENABLED, RedisConstants.DEFAULT_IS_CLUSTER_ENABLED))) {
					requestCache = new RedisCacheUtil(properties);
				} else {
					requestCache = new RedisClusterCacheUtil(properties);
				}
			}
		} catch (Exception e) {
			log.error("Error occurred initializing redis", e);
		}
		return requestCache;
	}

	private static Properties getRedisProperties() throws Exception {
		Properties properties = null;
		try {
			if (properties == null || properties.isEmpty()) {
				InputStream resourceStream = null;
				try {
					resourceStream = null;
					properties = new Properties();
					File file = new File(System.getProperty("catalina.base").concat(File.separator).concat("conf").concat(File.separator).concat(RedisConstants.REDIS_DATA_CACHE_PROPERTIES_FILE));
					if (file.exists()) {
						resourceStream = new FileInputStream(file);
					}
					if (resourceStream == null) {
						ClassLoader loader = Thread.currentThread().getContextClassLoader();
						resourceStream = loader.getResourceAsStream(RedisConstants.REDIS_DATA_CACHE_PROPERTIES_FILE);
					}
					properties.load(resourceStream);
				} finally {
					resourceStream.close();
				}
			}
		} catch (IOException e) {
			log.error("Error occurred fetching redis informations", e);
		}
		return properties;
	}
}