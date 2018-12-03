package tomcat.request.session.data.cache.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tomcat.request.session.data.cache.impl.redis.RedisCache;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/** author: Ranjith Manickam @ 3 Dec' 2018 */
public class StandardDataCache extends RedisCache {

    private Date lastSessionJobRun;
    private final Map<String, SessionData> sessionData;

    private final long sessionExpiryTime;

    public StandardDataCache(Properties properties, long sessionExpiryTime) {
        super(properties);
        this.sessionExpiryTime = (sessionExpiryTime + 60) / 60;
        this.lastSessionJobRun = new Date();
        this.sessionData = new ConcurrentHashMap<>();
    }

    /** {@inheritDoc} */
    @Override
    public byte[] set(String key, byte[] value) {
        triggerSessionExpiry();
        this.sessionData.put(key, new SessionData(value));
        return super.set(key, value);
    }

    /** {@inheritDoc} */
    @Override
    public Long setnx(String key, byte[] value) {
        triggerSessionExpiry();
        Long retValue = super.setnx(key, value);
        if (retValue == 1L) {
            this.sessionData.put(key, new SessionData(value));
        }
        return retValue;
    }

    /** {@inheritDoc} */
    @Override
    public Long expire(String key, int seconds) {
        return super.expire(key, seconds);
    }

    /** {@inheritDoc} */
    @Override
    public byte[] get(String key) {
        triggerSessionExpiry();
        if (this.sessionData.containsKey(key)) {
            return this.sessionData.get(key).getValue();
        }
        return super.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public Long delete(String key) {
        this.sessionData.remove(key);
        return super.delete(key);
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

    /** To trigger session expiry thread. */
    private void triggerSessionExpiry() {
        long diff = new Date().getTime() - this.lastSessionJobRun.getTime();
        long diffMinutes = diff / (60 * 1000) % 60;

        if (diffMinutes > 0L) {
            synchronized (this) {
                new SessionDataExpiryThread(this.sessionData, this.sessionExpiryTime);
                this.lastSessionJobRun = new Date();
            }
        }
    }

    /** Session data expiry thread. This will takes care of the session expired data removal. */
    private class SessionDataExpiryThread implements Runnable {

        private final Log LOGGER = LogFactory.getLog(SessionDataExpiryThread.class);

        private final long sessionExpiryTime;
        private final Map<String, SessionData> sessionData;

        SessionDataExpiryThread(Map<String, SessionData> sessionData, long sessionExpiryTime) {
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
