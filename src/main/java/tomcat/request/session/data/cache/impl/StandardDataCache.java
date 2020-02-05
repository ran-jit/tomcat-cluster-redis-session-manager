package tomcat.request.session.data.cache.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tomcat.request.session.data.cache.DataCache;
import tomcat.request.session.data.cache.impl.redis.RedisCache;
import tomcat.request.session.model.Config;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** author: Ranjith Manickam @ 3 Dec' 2018 */
public class StandardDataCache extends RedisCache {

    private boolean processDataSync;

    private long expiryJob;
    private long dataSyncJob;

    private final int sessionExpiryTime;
    private final long expiryJobTriggerInterval;
    private final long dataSyncJobTriggerInterval;
    private final Map<String, SessionData> sessionData;

    private final Executor expiryJobExecutor;
    private final Executor dataSyncJobExecutor;

    public StandardDataCache(Config config, int sessionExpiryTime) {
        super(config);
        this.sessionExpiryTime = sessionExpiryTime;
        this.sessionData = new ConcurrentHashMap<>();
        this.expiryJob = new Date().getTime();
        this.dataSyncJob = new Date().getTime();
        this.processDataSync = false;
        this.expiryJobTriggerInterval = TimeUnit.MINUTES.toMillis(config.getRedisSessionExpiryJobInterval());
        this.dataSyncJobTriggerInterval = TimeUnit.MINUTES.toMillis(config.getRedisSessionDataSyncJobInterval());
        this.expiryJobExecutor = Executors.newSingleThreadExecutor();
        this.dataSyncJobExecutor = Executors.newSingleThreadExecutor();
    }

    /** {@inheritDoc} */
    @Override
    public byte[] set(String key, byte[] value) {
        this.sessionData.put(key, new SessionData(value));
        try {
            return super.set(key, value);
        } catch (RuntimeException ex) {
            this.processDataSync = true;
        }
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public Long setnx(String key, byte[] value) {
        Long retValue;
        try {
            retValue = super.setnx(key, value);
        } catch (RuntimeException ex) {
            retValue = this.sessionData.containsKey(key) ? 0L : 1L;
            this.processDataSync = true;
        }

        if (retValue == 1L) {
            this.sessionData.put(key, new SessionData(value));
        }
        return retValue;
    }

    /** {@inheritDoc} */
    @Override
    public Long expire(String key, int seconds) {
        try {
            return super.expire(key, seconds);
        } catch (RuntimeException ex) {
            this.processDataSync = true;
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public byte[] get(String key) {
        handleSessionData();
        if (this.sessionData.containsKey(key)) {
            return this.sessionData.get(key).getValue();
        }
        try {
            return super.get(key);
        } catch (RuntimeException ex) {
            this.processDataSync = true;
            throw ex;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Long delete(String key) {
        Object value = this.sessionData.remove(key);
        try {
            return super.delete(key);
        } catch (RuntimeException ex) {
            this.processDataSync = true;
        }
        return (value == null) ? 0L : 1L;
    }

    /** Session data. */
    private static class SessionData implements Serializable {
        private byte[] value;
        private long lastAccessedOn;

        SessionData(byte[] value) {
            this.value = value;
            updatedLastAccessedOn();
        }

        void updatedLastAccessedOn() {
            this.lastAccessedOn = new Date().getTime();
        }

        byte[] getValue() {
            updatedLastAccessedOn();
            return this.value;
        }

        long getLastAccessedOn() {
            return this.lastAccessedOn;
        }
    }

    /** To handle session data. */
    private synchronized void handleSessionData() {
        // redis sync
        long now = new Date().getTime();

        long difference = now - this.dataSyncJob;
        if (this.processDataSync && difference >= this.dataSyncJobTriggerInterval) {
            this.dataSyncJobExecutor.execute(new SessionDataSyncThread(this, this.sessionData, this.sessionExpiryTime));
            this.dataSyncJob = (this.processDataSync) ? new Date().getTime() : this.dataSyncJob;
            this.processDataSync = false;
        }

        // session expiry
        difference = now - this.expiryJob;
        if (difference >= this.expiryJobTriggerInterval) {
            this.expiryJobExecutor.execute(new SessionDataExpiryThread(this.sessionData, this.sessionExpiryTime));
            this.expiryJob = new Date().getTime();
        }
    }

    /** Session data redis sync thread. */
    private static class SessionDataSyncThread implements Runnable {

        private final Logger LOGGER = LoggerFactory.getLogger(SessionDataSyncThread.class);

        private final DataCache dataCache;
        private final int sessionExpiryTime;
        private final Map<String, SessionData> sessionData;

        SessionDataSyncThread(DataCache dataCache, Map<String, SessionData> sessionData, int sessionExpiryTime) {
            this.dataCache = dataCache;
            this.sessionData = sessionData;
            this.sessionExpiryTime = sessionExpiryTime;
        }

        /** {@inheritDoc} */
        @Override
        public void run() {
            try {
                for (String key : this.sessionData.keySet()) {
                    SessionData data = this.sessionData.get(key);
                    if (data == null) {
                        continue;
                    }
                    this.dataCache.set(key, data.getValue());
                    this.dataCache.expire(key, this.sessionExpiryTime);
                }
            } catch (Exception ex) {
                LOGGER.error("Error processing session data expiry thread", ex);
            }
        }
    }

    /** Session data expiry thread. This will takes care of the session expired data removal. */
    private static class SessionDataExpiryThread implements Runnable {

        private final Logger LOGGER = LoggerFactory.getLogger(SessionDataExpiryThread.class);

        private final long expiry;
        private final Map<String, SessionData> sessionData;

        SessionDataExpiryThread(Map<String, SessionData> sessionData, int sessionExpiryTime) {
            this.sessionData = sessionData;
            this.expiry = TimeUnit.SECONDS.toMillis(sessionExpiryTime + 60);
        }

        /** {@inheritDoc} */
        @Override
        public void run() {
            try {
                for (String key : this.sessionData.keySet()) {
                    SessionData data = this.sessionData.get(key);
                    if (data == null) {
                        continue;
                    }

                    long difference = new Date().getTime() - data.getLastAccessedOn();
                    if (difference >= this.expiry) {
                        this.sessionData.remove(key);
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("Error processing session data expiry thread", ex);
            }
        }
    }
}
