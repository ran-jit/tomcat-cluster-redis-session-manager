package tomcat.request.session.data.cache;

import tomcat.request.session.data.cache.impl.StandardDataCache;
import tomcat.request.session.data.cache.impl.redis.RedisCache;
import tomcat.request.session.model.Config;

/** author: Ranjith Manickam @ 3 Dec' 2018 */
public class DataCacheFactory {

    private final Config config;
    private final int sessionExpiryTime;

    public DataCacheFactory(Config config, int sessionExpiryTime) {
        this.config = config;
        this.sessionExpiryTime = sessionExpiryTime;
    }

    /** To get data cache. */
    public DataCache getDataCache() {
        if (this.config.getLbStickySessionEnabled()) {
            return new StandardDataCache(this.config, this.sessionExpiryTime);
        }
        return new RedisCache(this.config);
    }
}
