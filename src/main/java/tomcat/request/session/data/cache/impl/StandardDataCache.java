package tomcat.request.session.data.cache.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tomcat.request.session.data.cache.DataCache;
import tomcat.request.session.data.cache.DataCacheConstants;
import tomcat.request.session.data.cache.DataCacheFactory;
import tomcat.request.session.data.cache.impl.redis.RedisCache;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/** author: Ranjith Manickam @ 3 Dec' 2018 */
public class StandardDataCache extends RedisCache {

    private boolean triggerDataSync;

    private Date expiryJobTriggered;
    private Date dataSyncJobTriggered;

    private final int expiryJobTriggerInterval;
    private final int dataSyncJobTriggerInterval;

    private final int sessionExpiryTime;
    private final Map<String, SessionData> sessionData;

    public StandardDataCache(Properties properties, int sessionExpiryTime) {
        super(properties);
        this.sessionExpiryTime = sessionExpiryTime;
        this.sessionData = new ConcurrentHashMap<>();
        this.expiryJobTriggered = new Date();
        this.dataSyncJobTriggered = new Date();
        this.triggerDataSync = false;
        this.expiryJobTriggerInterval = Integer.parseInt(DataCacheFactory.getProperty(properties, DataCacheConstants.SESSION_EXPIRY_JOB_INTERVAL));
        this.dataSyncJobTriggerInterval = Integer.parseInt(DataCacheFactory.getProperty(properties, DataCacheConstants.SESSION_DATA_SYNC_JOB_INTERVAL));
    }

    /** {@inheritDoc} */
    @Override
    public byte[] set(String key, byte[] value) {
        this.sessionData.put(key, new SessionData(value));
        try {
            return super.set(key, value);
        } catch (RuntimeException ex) {
            this.triggerDataSync = true;
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
            this.triggerDataSync = true;
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
            this.triggerDataSync = true;
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
            this.triggerDataSync = true;
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
            this.triggerDataSync = true;
        }
        return (value == null) ? 0L : 1L;
    }

    /** Session data. */
    private class SessionData implements Serializable {
        private byte[] value;
        private Date lastAccessedOn;

        SessionData(byte[] value) {
            this.value = value;
            updatedLastAccessedOn();
        }

        void updatedLastAccessedOn() {
            this.lastAccessedOn = new Date();
        }

        byte[] getValue() {
            updatedLastAccessedOn();
            return this.value;
        }

        Date getLastAccessedOn() {
            return this.lastAccessedOn;
        }
    }

    /** To handle session data. */
    private synchronized void handleSessionData() {
        // redis sync
        if (this.triggerDataSync) {
            long difference = new Date().getTime() - this.dataSyncJobTriggered.getTime();

            if (difference >= TimeUnit.MINUTES.toMillis(this.dataSyncJobTriggerInterval)) {
                new SessionDataSyncThread(this, this.sessionData, this.sessionExpiryTime);
                this.triggerDataSync = false;
                this.dataSyncJobTriggered = new Date();
            }
        }

        // session expiry
        long difference = new Date().getTime() - this.expiryJobTriggered.getTime();
        if (difference >= TimeUnit.MINUTES.toMillis(this.expiryJobTriggerInterval)) {
            new SessionDataExpiryThread(this.sessionData, this.sessionExpiryTime);
            this.expiryJobTriggered = new Date();
        }
    }

    /** Session data redis sync thread. */
    private class SessionDataSyncThread implements Runnable {

        private final Log LOGGER = LogFactory.getLog(SessionDataSyncThread.class);

        private final DataCache dataCache;
        private final int sessionExpiryTime;
        private final Map<String, SessionData> sessionData;

        SessionDataSyncThread(DataCache dataCache, Map<String, SessionData> sessionData, int sessionExpiryTime) {
            this.dataCache = dataCache;
            this.sessionData = sessionData;
            this.sessionExpiryTime = sessionExpiryTime;
            new Thread(this).start();
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
    private class SessionDataExpiryThread implements Runnable {

        private final Log LOGGER = LogFactory.getLog(SessionDataExpiryThread.class);

        private final int sessionExpiryTime;
        private final Map<String, SessionData> sessionData;

        SessionDataExpiryThread(Map<String, SessionData> sessionData, int sessionExpiryTime) {
            this.sessionData = sessionData;
            this.sessionExpiryTime = (sessionExpiryTime + 60) / 60;
            new Thread(this).start();
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

                    long diff = new Date().getTime() - data.getLastAccessedOn().getTime();
                    long diffMinutes = diff / (60 * 1000) % 60;

                    if (diffMinutes > this.sessionExpiryTime) {
                        this.sessionData.remove(key);
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("Error processing session data expiry thread", ex);
            }
        }
    }
}
