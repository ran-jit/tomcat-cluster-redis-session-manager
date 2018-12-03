package tomcat.request.session.data.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tomcat.request.session.SessionConstants;
import tomcat.request.session.data.cache.impl.StandardDataCache;
import tomcat.request.session.data.cache.impl.redis.RedisCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** author: Ranjith Manickam @ 3 Dec' 2018 */
public class DataCacheFactory {

    private static final Log LOGGER = LogFactory.getLog(DataCacheFactory.class);

    private final long sessionExpiryTime;

    public DataCacheFactory(long sessionExpiryTime) {
        this.sessionExpiryTime = sessionExpiryTime;
    }

    /** To get data cache. */
    public DataCache getDataCache() {
        Properties properties = getApplicationProperties();

        if (Boolean.valueOf(getProperty(properties, DataCacheConstants.REDIS_LB_STICKY_SESSION_ENABLED))) {
            return new StandardDataCache(properties, this.sessionExpiryTime);
        }

        return new RedisCache(properties);
    }

    /** To get redis data cache properties. */
    private Properties getApplicationProperties() {
        Properties properties = new Properties();
        try {
            String filePath = System.getProperty(SessionConstants.CATALINA_BASE).concat(File.separator)
                    .concat(SessionConstants.CONF).concat(File.separator)
                    .concat(DataCacheConstants.APPLICATION_PROPERTIES_FILE);

            InputStream resourceStream = null;
            try {
                resourceStream = (!filePath.isEmpty() && new File(filePath).exists()) ? new FileInputStream(filePath) : null;
                if (resourceStream == null) {
                    ClassLoader loader = Thread.currentThread().getContextClassLoader();
                    resourceStream = loader.getResourceAsStream(DataCacheConstants.APPLICATION_PROPERTIES_FILE);
                }
                properties.load(resourceStream);
            } finally {
                if (resourceStream != null) {
                    resourceStream.close();
                }
            }
        } catch (IOException ex) {
            LOGGER.error("Error while retrieving application properties", ex);
        }
        return properties;
    }

    /**
     * To get property with the specified key in this properties list.
     *
     * @param properties - properties list.
     * @param key        - search key.
     * @return - Returns the property value.
     */
    public static String getProperty(Properties properties, String key) {
        return getProperty(properties, key, null);
    }

    /**
     * To get property with the specified key in this properties list.
     *
     * @param properties   - properties list.
     * @param key          - search key.
     * @param defaultValue - default value.
     * @return - - Returns the property value.
     */
    public static String getProperty(Properties properties, String key, String defaultValue) {
        String[] keyValue = key.split(":");
        return properties.getProperty(keyValue[0], (keyValue.length > 1 && defaultValue == null) ? keyValue[1] : defaultValue);
    }
}
