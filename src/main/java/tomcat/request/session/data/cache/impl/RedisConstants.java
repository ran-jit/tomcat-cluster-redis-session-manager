package tomcat.request.session.data.cache.impl;

/**
 * Tomcat clustering with Redis data-cache implementation.
 * 
 * Redis data-cache constants.
 * 
 * @author Ranjith Manickam
 * @since 2.0
 */
interface RedisConstants {

	// redis properties file name
	final String PROPERTIES_FILE = "redis-data-cache.properties";

	// redis properties
	final String HOSTS = "redis.hosts";
	final String CLUSTER_ENABLED = "redis.cluster.enabled";

	final String MAX_ACTIVE = "redis.max.active";
	final String TEST_ONBORROW = "redis.test.onBorrow";
	final String TEST_ONRETURN = "redis.test.onReturn";
	final String MAX_IDLE = "redis.max.idle";
	final String MIN_IDLE = "redis.min.idle";
	final String TEST_WHILEIDLE = "redis.test.whileIdle";
	final String TEST_NUMPEREVICTION = "redis.test.numPerEviction";
	final String TIME_BETWEENEVICTION = "redis.time.betweenEviction";

	final String PASSWORD = "redis.password";
	final String DATABASE = "redis.database";
	final String TIMEOUT = "redis.timeout";

	// redis property default values
	final String DEFAULT_MAX_ACTIVE_VALUE = "10";
	final String DEFAULT_TEST_ONBORROW_VALUE = "true";
	final String DEFAULT_TEST_ONRETURN_VALUE = "true";
	final String DEFAULT_MAX_IDLE_VALUE = "5";
	final String DEFAULT_MIN_IDLE_VALUE = "1";
	final String DEFAULT_TEST_WHILEIDLE_VALUE = "true";
	final String DEFAULT_TEST_NUMPEREVICTION_VALUE = "10";
	final String DEFAULT_TIME_BETWEENEVICTION_VALUE = "60000";
	final String DEFAULT_CLUSTER_ENABLED = "false";

	final String CONN_FAILED_RETRY_MSG = "Jedis connection failed, retrying...";
}