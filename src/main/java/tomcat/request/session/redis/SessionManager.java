package tomcat.request.session.redis;

import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Valve;
import org.apache.catalina.session.ManagerBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tomcat.request.session.SerializationUtil;
import tomcat.request.session.Session;
import tomcat.request.session.SessionConstants;
import tomcat.request.session.SessionConstants.SessionPolicy;
import tomcat.request.session.SessionContext;
import tomcat.request.session.SessionMetadata;
import tomcat.request.session.data.cache.DataCache;
import tomcat.request.session.data.cache.DataCacheConstants;
import tomcat.request.session.data.cache.DataCacheFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;

/** author: Ranjith Manickam @ 12 Jul' 2018 */
public class SessionManager extends ManagerBase implements Lifecycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionManager.class);

    private DataCache dataCache;
    private SerializationUtil serializer;
    private ThreadLocal<SessionContext> sessionContext = new ThreadLocal<>();
    private Set<SessionPolicy> sessionPolicy = EnumSet.of(SessionPolicy.DEFAULT);

    public boolean getSaveOnChange() {
        return this.sessionPolicy.contains(SessionPolicy.SAVE_ON_CHANGE);
    }

    private boolean getAlwaysSaveAfterRequest() {
        return this.sessionPolicy.contains(SessionPolicy.ALWAYS_SAVE_AFTER_REQUEST);
    }

    /** {@inheritDoc} */
    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        super.addLifecycleListener(listener);
    }

    /** {@inheritDoc} */
    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return super.findLifecycleListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        super.removeLifecycleListener(listener);
    }

    /** {@inheritDoc} */
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        super.startInternal();
        super.setState(LifecycleState.STARTING);

        boolean initializedValve = false;
        Context context = getContextIns();
        for (Valve valve : context.getPipeline().getValves()) {
            if (valve instanceof SessionHandlerValve) {
                SessionHandlerValve handlerValve = (SessionHandlerValve) valve;
                handlerValve.setSessionManager(this);
                initializedValve = true;
                break;
            }
        }

        if (!initializedValve) {
            throw new LifecycleException("Session handling valve is not initialized..");
        }
        initialize();
        LOGGER.info("The sessions will expire after " + (getSessionTimeout(null)) + " seconds.");
        context.setDistributable(true);
    }

    /** {@inheritDoc} */
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        super.setState(LifecycleState.STOPPING);
        super.stopInternal();
    }

    /** {@inheritDoc} */
    @Override
    public Session createSession(String sessionId) {
        if (sessionId != null) {
            sessionId = (this.dataCache.setnx(sessionId, SessionConstants.NULL_SESSION) == 0L) ? null : sessionId;
        } else {
            do {
                sessionId = generateSessionId();
            } while (this.dataCache.setnx(sessionId, SessionConstants.NULL_SESSION) == 0L);
        }

        Session session = (sessionId != null) ? createEmptySession() : null;
        if (session != null) {
            session.setId(sessionId);
            session.setNew(true);
            session.setValid(true);
            session.setCreationTime(System.currentTimeMillis());
            session.setMaxInactiveInterval(getSessionTimeout(session));
            session.tellNew();
        }
        setValues(sessionId, session, false, new SessionMetadata());

        if (session != null) {
            try {
                save(session, true);
            } catch (Exception ex) {
                LOGGER.error("Error occurred while creating session..", ex);
                setValues(null, null);
                session = null;
            }
        }
        return session;
    }

    /** {@inheritDoc} */
    @Override
    public Session createEmptySession() {
        return new Session(this);
    }

    /** {@inheritDoc} */
    @Override
    public void add(org.apache.catalina.Session session) {
        save(session, false);
    }

    /** {@inheritDoc} */
    @Override
    public Session findSession(String sessionId) throws IOException {
        if (sessionId != null && this.sessionContext.get() != null && sessionId.equals(this.sessionContext.get().getId())) {
            return this.sessionContext.get().getSession();
        }

        Session session = null;
        boolean isPersisted = false;
        SessionMetadata metadata = null;

        byte[] data = this.dataCache.get(sessionId);
        if (data == null) {
            sessionId = null;
            isPersisted = false;
        } else {
            if (Arrays.equals(SessionConstants.NULL_SESSION, data)) {
                throw new IOException("NULL session data");
            }
            try {
                metadata = new SessionMetadata();
                Session newSession = createEmptySession();
                this.serializer.deserializeSessionData(data, newSession, metadata);

                newSession.setId(sessionId);
                newSession.access();
                newSession.setNew(false);
                newSession.setValid(true);
                newSession.resetDirtyTracking();
                newSession.setMaxInactiveInterval(getSessionTimeout(newSession));

                session = newSession;
                isPersisted = true;
            } catch (Exception ex) {
                LOGGER.error("Error occurred while de-serializing the session object..", ex);
            }
        }
        setValues(sessionId, session, isPersisted, metadata);
        return session;
    }

    /** {@inheritDoc} */
    @Override
    public void remove(org.apache.catalina.Session session) {
        remove(session, false);
    }

    /** {@inheritDoc} */
    @Override
    public void remove(org.apache.catalina.Session session, boolean update) {
        this.dataCache.delete(session.getId());
    }

    /** {@inheritDoc} */
    @Override
    public void load() {
        // Auto-generated method stub
    }

    /** {@inheritDoc} */
    @Override
    public void unload() {
        // Auto-generated method stub
    }

    /** To initialize the session manager. */
    private void initialize() {
        try {
            Properties properties = getApplicationProperties();
            this.dataCache = new DataCacheFactory(properties, getSessionTimeout(null)).getDataCache();
            this.serializer = new SerializationUtil();

            Context context = getContextIns();
            ClassLoader loader = (context != null && context.getLoader() != null) ? context.getLoader().getClassLoader() : null;
            this.serializer.setClassLoader(loader);

            setSessionPersistentPolicies(properties);
        } catch (Exception ex) {
            LOGGER.error("Error occurred while initializing the session manager..", ex);
            throw ex;
        }
    }

    /** To save session object to data cache. */
    public void save(org.apache.catalina.Session session, boolean forceSave) {
        try {
            Boolean isPersisted;
            Session newSession = (Session) session;
            byte[] hash = (this.sessionContext.get() != null && this.sessionContext.get().getMetadata() != null)
                    ? this.sessionContext.get().getMetadata().getAttributesHash() : null;
            byte[] currentHash = this.serializer.getSessionAttributesHashCode(newSession);

            if (forceSave
                    || newSession.isDirty()
                    || (isPersisted = (this.sessionContext.get() != null) ? this.sessionContext.get().isPersisted() : null) == null
                    || !isPersisted || !Arrays.equals(hash, currentHash)) {

                SessionMetadata metadata = new SessionMetadata();
                metadata.setAttributesHash(currentHash);

                this.dataCache.set(newSession.getId(), this.serializer.serializeSessionData(newSession, metadata));
                newSession.resetDirtyTracking();
                setValues(true, metadata);
            }

            int timeout = getSessionTimeout(newSession);
            this.dataCache.expire(newSession.getId(), timeout);
            LOGGER.debug("Session [" + newSession.getId() + "] expire in [" + timeout + "] seconds.");

        } catch (IOException ex) {
            LOGGER.error("Error occurred while saving the session object in data cache..", ex);
        }
    }

    /** To process post request process. */
    void afterRequest() {
        Session session = null;
        try {
            session = (this.sessionContext.get() != null) ? this.sessionContext.get().getSession() : null;
            if (session != null) {
                if (session.isValid()) {
                    save(session, getAlwaysSaveAfterRequest());
                } else {
                    remove(session);
                }
                LOGGER.debug("Session object " + (session.isValid() ? "saved: " : "removed: ") + session.getId());
            }
        } catch (Exception ex) {
            LOGGER.error("Error occurred while processing post request process..", ex);
        } finally {
            this.sessionContext.remove();
            LOGGER.debug("Session removed from ThreadLocal:" + ((session != null) ? session.getIdInternal() : ""));
        }
    }

    /** To get session timeout. */
    private int getSessionTimeout(Session session) {
        int timeout = getContextIns().getSessionTimeout() * 60;
        int sessionTimeout = (session == null) ? 0 : session.getMaxInactiveInterval();
        return (sessionTimeout < timeout) ? (Math.max(timeout, 1800)) : sessionTimeout;
    }

    /** To set values to session context. */
    private void setValues(String sessionId, Session session) {
        if (this.sessionContext.get() == null) {
            this.sessionContext.set(new SessionContext());
        }
        this.sessionContext.get().setId(sessionId);
        this.sessionContext.get().setSession(session);
    }

    /** To set values to session context. */
    private void setValues(boolean isPersisted, SessionMetadata metadata) {
        if (this.sessionContext.get() == null) {
            this.sessionContext.set(new SessionContext());
        }
        this.sessionContext.get().setMetadata(metadata);
        this.sessionContext.get().setPersisted(isPersisted);
    }

    /** To set values to session context. */
    private void setValues(String sessionId, Session session, boolean isPersisted, SessionMetadata metadata) {
        setValues(sessionId, session);
        setValues(isPersisted, metadata);
    }

    /** To get catalina context instance. */
    private Context getContextIns() {
        try {
            Method method = this.getClass().getSuperclass().getDeclaredMethod("getContext");
            return (Context) method.invoke(this);
        } catch (Exception ex) {
            try {
                Method method = this.getClass().getSuperclass().getDeclaredMethod("getContainer");
                return (Context) method.invoke(this);
            } catch (Exception ex2) {
                // skip
            }
        }
        throw new RuntimeException("Error occurred while creating container instance");
    }

    /** To set session persistent policies */
    private void setSessionPersistentPolicies(Properties properties) {
        String sessionPolicies = properties.getProperty(SessionConstants.SESSION_PERSISTENT_POLICIES);
        if (sessionPolicies == null || sessionPolicies.isEmpty()) {
            return;
        }

        sessionPolicies = sessionPolicies.replaceAll("\\s", "");
        String[] sessionPolicyNames = sessionPolicies.split(",");
        for (String sessionPolicyName : sessionPolicyNames) {
            this.sessionPolicy.add(SessionPolicy.fromName(sessionPolicyName));
        }
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
}
