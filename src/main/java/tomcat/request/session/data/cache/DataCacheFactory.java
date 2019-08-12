package tomcat.request.session.data.cache;

import tomcat.request.session.data.cache.impl.StandardDataCache;
import tomcat.request.session.data.cache.impl.redis.RedisCache;

import java.util.Properties;

/** author: Ranjith Manickam @ 3 Dec' 2018 */
public class DataCacheFactory {

    private final Properties properties;
    private final int sessionExpiryTime;

    public DataCacheFactory(Properties properties, int sessionExpiryTime) {
        this.properties = properties;
        this.sessionExpiryTime = sessionExpiryTime;
    }

    /** To get data cache. */
    public DataCache getDataCache() {
        if (Boolean.parseBoolean(getProperty(this.properties, DataCacheConstants.LB_STICKY_SESSION_ENABLED))) {
            return new StandardDataCache(this.properties, this.sessionExpiryTime);
        }
        return new RedisCache(this.properties);
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
