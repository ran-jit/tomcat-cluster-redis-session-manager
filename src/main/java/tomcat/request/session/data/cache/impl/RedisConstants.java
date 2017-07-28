package tomcat.request.session.data.cache.impl;

/**
 * Tomcat clustering with Redis data-cache implementation.
 * 
 * Redis data-cache constants.
 * 
 * @author Ranjith Manickam
 * @since 2.0
 */
public class RedisConstants {

	// redis properties file name
	public static final String PROPERTIES_FILE = "redis-data-cache.properties";

	// redis properties
	public static final String HOSTS = "redis.hosts";
	public static final String CLUSTER_ENABLED = "redis.cluster.enabled";

	public static final String MAX_ACTIVE = "redis.max.active";
	public static final String TEST_ONBORROW = "redis.test.onBorrow";
	public static final String TEST_ONRETURN = "redis.test.onReturn";
	public static final String MAX_IDLE = "redis.max.idle";
	public static final String MIN_IDLE = "redis.min.idle";
	public static final String TEST_WHILEIDLE = "redis.test.whileIdle";
	public static final String TEST_NUMPEREVICTION = "redis.test.numPerEviction";
	public static final String TIME_BETWEENEVICTION = "redis.time.betweenEviction";

	public static final String PASSWORD = "redis.password";
	public static final String DATABASE = "redis.database";
	public static final String TIMEOUT = "redis.timeout";

	// redis property default values
	public static final String DEFAULT_MAX_ACTIVE_VALUE = "10";
	public static final String DEFAULT_TEST_ONBORROW_VALUE = "true";
	public static final String DEFAULT_TEST_ONRETURN_VALUE = "true";
	public static final String DEFAULT_MAX_IDLE_VALUE = "5";
	public static final String DEFAULT_MIN_IDLE_VALUE = "1";
	public static final String DEFAULT_TEST_WHILEIDLE_VALUE = "true";
	public static final String DEFAULT_TEST_NUMPEREVICTION_VALUE = "10";
	public static final String DEFAULT_TIME_BETWEENEVICTION_VALUE = "60000";
	public static final String DEFAULT_CLUSTER_ENABLED = "false";

	public static final String CONN_FAILED_RETRY_MSG = "Jedis connection failed, retrying...";
}