package tomcat.request.session.model;

import redis.clients.jedis.Protocol;
import tomcat.request.session.annotation.Property;

import java.io.Serializable;

import static tomcat.request.session.annotation.Property.PropertyType.BOOLEAN;
import static tomcat.request.session.annotation.Property.PropertyType.INTEGER;

/** author: Ranjith Manickam @ 5 Feb' 2020 */
public class Config implements Serializable {

    public static final String APPLICATION_PROPERTIES_FILE = "redis-data-cache.properties";

    /** Redis config type. */
    public enum RedisConfigType {
        DEFAULT,
        SENTINEL,
        CLUSTER
    }

    @Property(name = "redis.hosts", defaultValue = "127.0.0.1:6379")
    private String redisHosts;

    @Property(name = "redis.cluster.enabled", type = BOOLEAN, defaultValue = "false")
    private Boolean redisClusterEnabled;

    @Property(name = "redis.sentinel.enabled", type = BOOLEAN, defaultValue = "false")
    private Boolean redisSentinelEnabled;

    @Property(name = "lb.sticky-session.enabled", type = BOOLEAN, defaultValue = "false")
    private Boolean lbStickySessionEnabled;

    @Property(name = "redis.max.active", type = INTEGER, defaultValue = "10")
    private Integer redisMaxActive;

    @Property(name = "redis.test.onBorrow", type = BOOLEAN, defaultValue = "true")
    private Boolean redisTestOnBorrow;

    @Property(name = "redis.test.onReturn", type = BOOLEAN, defaultValue = "true")
    private Boolean redisTestOnReturn;

    @Property(name = "redis.max.idle", type = INTEGER, defaultValue = "5")
    private Integer redisMaxIdle;

    @Property(name = "redis.min.idle", type = INTEGER, defaultValue = "1")
    private Integer redisMinIdle;

    @Property(name = "redis.test.whileIdle", type = BOOLEAN, defaultValue = "true")
    private Boolean redisTestWhileIdle;

    @Property(name = "redis.test.numPerEviction", type = INTEGER, defaultValue = "10")
    private Integer redisTestNumPerEviction;

    @Property(name = "redis.time.betweenEviction", type = INTEGER, defaultValue = "60000")
    private Integer redisTimeBetweenEviction;

    @Property(name = "redis.password")
    private String redisPassword;

    @Property(name = "redis.database", type = INTEGER, defaultValue = "0")
    private Integer redisDatabase;

    @Property(name = "redis.timeout", type = INTEGER, defaultValue = "2000")
    private Integer redisTimeout;

    @Property(name = "redis.sentinel.master", defaultValue = "mymaster")
    private String redisSentinelMaster;

    @Property(name = "redis.session.expiry.job.interval", type = INTEGER, defaultValue = "60")
    private Integer redisSessionExpiryJobInterval;

    @Property(name = "redis.session.data-sync.job.interval", type = INTEGER, defaultValue = "10")
    private Integer redisSessionDataSyncJobInterval;

    @Property(name = "session.persistent.policies", defaultValue = "DEFAULT")
    private String sessionPersistentPolicies;

    public Config() {
    }

    public Config(String redisHosts,
                  Boolean redisClusterEnabled,
                  Boolean redisSentinelEnabled,
                  Boolean lbStickySessionEnabled,
                  Integer redisMaxActive,
                  Boolean redisTestOnBorrow,
                  Boolean redisTestOnReturn,
                  Integer redisMaxIdle,
                  Integer redisMinIdle,
                  Boolean redisTestWhileIdle,
                  Integer redisTestNumPerEviction,
                  Integer redisTimeBetweenEviction,
                  String redisPassword,
                  Integer redisDatabase,
                  Integer redisTimeout,
                  String redisSentinelMaster,
                  Integer redisSessionExpiryJobInterval,
                  Integer redisSessionDataSyncJobInterval,
                  String sessionPersistentPolicies) {
        this.redisHosts = redisHosts;
        this.redisClusterEnabled = redisClusterEnabled;
        this.redisSentinelEnabled = redisSentinelEnabled;
        this.lbStickySessionEnabled = lbStickySessionEnabled;
        this.redisMaxActive = redisMaxActive;
        this.redisTestOnBorrow = redisTestOnBorrow;
        this.redisTestOnReturn = redisTestOnReturn;
        this.redisMaxIdle = redisMaxIdle;
        this.redisMinIdle = redisMinIdle;
        this.redisTestWhileIdle = redisTestWhileIdle;
        this.redisTestNumPerEviction = redisTestNumPerEviction;
        this.redisTimeBetweenEviction = redisTimeBetweenEviction;
        this.redisPassword = redisPassword;
        this.redisDatabase = redisDatabase;
        this.redisTimeout = redisTimeout;
        this.redisSentinelMaster = redisSentinelMaster;
        this.redisSessionExpiryJobInterval = redisSessionExpiryJobInterval;
        this.redisSessionDataSyncJobInterval = redisSessionDataSyncJobInterval;
        this.sessionPersistentPolicies = sessionPersistentPolicies;
    }

    /** To get 'redis.hosts' value. */
    public String getRedisHosts() {
        return redisHosts;
    }

    /** To get 'redis.cluster.enabled' value. */
    public Boolean getRedisClusterEnabled() {
        return redisClusterEnabled;
    }

    /** To get 'redis.sentinel.enabled' value. */
    public Boolean getRedisSentinelEnabled() {
        return redisSentinelEnabled;
    }

    /** To get 'lb.sticky-session.enabled' value. */
    public Boolean getLbStickySessionEnabled() {
        return lbStickySessionEnabled;
    }

    /** To get 'redis.max.active' value. */
    public Integer getRedisMaxActive() {
        return redisMaxActive;
    }

    /** To get 'redis.test.onBorrow' value. */
    public Boolean getRedisTestOnBorrow() {
        return redisTestOnBorrow;
    }

    /** To get 'redis.test.onReturn' value. */
    public Boolean getRedisTestOnReturn() {
        return redisTestOnReturn;
    }

    /** To get 'redis.max.idle' value. */
    public Integer getRedisMaxIdle() {
        return redisMaxIdle;
    }

    /** To get 'redis.min.idle' value. */
    public Integer getRedisMinIdle() {
        return redisMinIdle;
    }

    /** To get 'redis.test.whileIdle' value. */
    public Boolean getRedisTestWhileIdle() {
        return redisTestWhileIdle;
    }

    /** To get 'redis.test.numPerEviction' value. */
    public Integer getRedisTestNumPerEviction() {
        return redisTestNumPerEviction;
    }

    /** To get 'redis.time.betweenEviction' value. */
    public Integer getRedisTimeBetweenEviction() {
        return redisTimeBetweenEviction;
    }

    /** To get 'redis.password' value. */
    public String getRedisPassword() {
        return (redisPassword == null || redisPassword.isEmpty()) ? null : redisPassword;
    }

    /** To get 'redis.database' value. */
    public Integer getRedisDatabase() {
        return redisDatabase;
    }

    /** To get 'redis.timeout' value. */
    public Integer getRedisTimeout() {
        return Math.max(redisTimeout, Protocol.DEFAULT_TIMEOUT);
    }

    /** To get 'redis.sentinel.master' value. */
    public String getRedisSentinelMaster() {
        return redisSentinelMaster;
    }

    /** To get 'redis.session.expiry.job.interval' value. */
    public Integer getRedisSessionExpiryJobInterval() {
        return redisSessionExpiryJobInterval;
    }

    /** To get 'redis.session.data-sync.job.interval' value. */
    public Integer getRedisSessionDataSyncJobInterval() {
        return redisSessionDataSyncJobInterval;
    }

    /** To get 'session.persistent.policies' value */
    public String getSessionPersistentPolicies() {
        return sessionPersistentPolicies;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Config{" +
                "redisHosts='" + redisHosts + '\'' +
                ", redisClusterEnabled=" + redisClusterEnabled +
                ", redisSentinelEnabled=" + redisSentinelEnabled +
                ", lbStickySessionEnabled=" + lbStickySessionEnabled +
                ", redisMaxActive=" + redisMaxActive +
                ", redisTestOnBorrow=" + redisTestOnBorrow +
                ", redisTestOnReturn=" + redisTestOnReturn +
                ", redisMaxIdle=" + redisMaxIdle +
                ", redisMinIdle=" + redisMinIdle +
                ", redisTestWhileIdle=" + redisTestWhileIdle +
                ", redisTestNumPerEviction=" + redisTestNumPerEviction +
                ", redisTimeBetweenEviction=" + redisTimeBetweenEviction +
                ", redisPassword='" + redisPassword + '\'' +
                ", redisDatabase=" + redisDatabase +
                ", redisTimeout=" + redisTimeout +
                ", redisSentinelMaster='" + redisSentinelMaster + '\'' +
                ", redisSessionExpiryJobInterval=" + redisSessionExpiryJobInterval +
                ", redisSessionDataSyncJobInterval=" + redisSessionDataSyncJobInterval +
                ", sessionPersistentPolicies='" + sessionPersistentPolicies + '\'' +
                '}';
    }

    /** To get redis config type. */
    public RedisConfigType getRedisConfigType() {
        if (this.getRedisClusterEnabled()) {
            return RedisConfigType.CLUSTER;
        } else if (this.getRedisSentinelEnabled()) {
            return RedisConfigType.SENTINEL;
        }
        return RedisConfigType.DEFAULT;
    }
}
