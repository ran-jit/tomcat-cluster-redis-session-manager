package tomcat.request.session.data.cache;

/** author: Ranjith Manickam @ 3 Dec' 2018 */
public interface DataCacheConstants {

    // redis properties file name
    String APPLICATION_PROPERTIES_FILE = "redis-data-cache.properties";

    // redis properties
    String REDIS_HOSTS = "redis.hosts";
    String REDIS_CLUSTER_ENABLED = "redis.cluster.enabled:false";
    String REDIS_SENTINEL_ENABLED = "redis.sentinel.enabled:false";
    String REDIS_LB_STICKY_SESSION_ENABLED = "redis.load-balancer.sticky-session.enabled:false";

    String REDIS_MAX_ACTIVE = "redis.max.active:10";
    String REDIS_TEST_ONBORROW = "redis.test.onBorrow:true";
    String REDIS_TEST_ONRETURN = "redis.test.onReturn:true";
    String REDIS_MAX_IDLE = "redis.max.idle:5";
    String REDIS_MIN_IDLE = "redis.min.idle:1";
    String REDIS_TEST_WHILEIDLE = "redis.test.whileIdle:true";
    String REDIS_TEST_NUMPEREVICTION = "redis.test.numPerEviction:10";
    String REDIS_TIME_BETWEENEVICTION = "redis.time.betweenEviction:60000";

    String REDIS_PASSWORD = "redis.password";
    String REDIS_DATABASE = "redis.database:0";
    String REDIS_TIMEOUT = "redis.timeout:2000";

    String REDIS_SENTINEL_MASTER = "redis.sentinel.master:mymaster";

    String REDIS_CONN_FAILED_RETRY_MSG = "Jedis connection failed, retrying...";

    String SESSION_EXPIRY_JOB_INTERVAL = "redis.session.expiry.job.interval:60";
    String SESSION_DATA_SYNC_JOB_INTERVAL = "redis.session.data-sync.job.interval:10";

    enum RedisConfigType {
        DEFAULT,
        SENTINEL,
        CLUSTER
    }
}
