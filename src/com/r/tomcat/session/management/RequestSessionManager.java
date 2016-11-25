package com.r.tomcat.session.management;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Loader;
import org.apache.catalina.Session;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.session.ManagerBase;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.r.tomcat.session.data.cache.IRequestSessionCacheUtils;
import com.r.tomcat.session.data.cache.RequestSessionCacheFactory;

/**
 * Tomcat clustering implementation
 * 
 * This class is uses to store and retrieve the HTTP request session objects from catalina to data cache
 *
 * @author Ranjith Manickam
 * @since 1.0
 */
public class RequestSessionManager extends ManagerBase implements Lifecycle
{
	private final Log log = LogFactory.getLog(RequestSessionManager.class);

	private IRequestSessionCacheUtils requestSessionCacheUtils;

	protected SessionDataSerializer serializer;

	protected RequestSessionHandlerValve handlerValve;

	protected byte[] NULL_SESSION = "null".getBytes();

	protected LifecycleSupport lifecycle = new LifecycleSupport(this);

	protected ThreadLocal<String> currentSessionId = new ThreadLocal<>();

	protected ThreadLocal<CustomRequestSession> currentSession = new ThreadLocal<>();

	protected ThreadLocal<Boolean> currentSessionIsPersisted = new ThreadLocal<>();

	protected EnumSet<SessionPersistPolicy> sessionPersistPoliciesSet = EnumSet.of(SessionPersistPolicy.DEFAULT);

	protected ThreadLocal<SessionSerializationMetadata> currentSessionSerializationMetadata = new ThreadLocal<>();

	enum SessionPersistPolicy {
		DEFAULT, SAVE_ON_CHANGE, ALWAYS_SAVE_AFTER_REQUEST;

		static SessionPersistPolicy fromName(String name) {
			for (SessionPersistPolicy policy : SessionPersistPolicy.values()) {
				if (policy.name().equalsIgnoreCase(name)) {
					return policy;
				}
			}
			throw new IllegalArgumentException("Invalid session persist policy [" + name + "]. Must be one of " + Arrays.asList(SessionPersistPolicy.values()) + ".");
		}
	}

	public String getSessionPersistPolicies() {
		StringBuilder policies = new StringBuilder();
		for (Iterator<SessionPersistPolicy> iter = this.sessionPersistPoliciesSet.iterator(); iter.hasNext();) {
			SessionPersistPolicy policy = iter.next();
			policies.append(policy.name());
			if (iter.hasNext()) {
				policies.append(",");
			}
		}
		return policies.toString();
	}

	public void setSessionPersistPolicies(String sessionPersistPolicies) {
		String[] policyArray = sessionPersistPolicies.split(",");
		EnumSet<SessionPersistPolicy> policySet = EnumSet.of(SessionPersistPolicy.DEFAULT);
		for (String policyName : policyArray) {
			SessionPersistPolicy policy = SessionPersistPolicy.fromName(policyName);
			policySet.add(policy);
		}
		this.sessionPersistPoliciesSet = policySet;
	}

	public boolean getSaveOnChange() {
		return this.sessionPersistPoliciesSet.contains(SessionPersistPolicy.SAVE_ON_CHANGE);
	}

	public boolean getAlwaysSaveAfterRequest() {
		return this.sessionPersistPoliciesSet.contains(SessionPersistPolicy.ALWAYS_SAVE_AFTER_REQUEST);
	}

	@Override
	public void addLifecycleListener(LifecycleListener listener) {
		lifecycle.addLifecycleListener(listener);
	}

	@Override
	public LifecycleListener[] findLifecycleListeners() {
		return lifecycle.findLifecycleListeners();
	}

	@Override
	public void removeLifecycleListener(LifecycleListener listener) {
		lifecycle.removeLifecycleListener(listener);
	}

	@Override
	protected synchronized void startInternal() throws LifecycleException {
		super.startInternal();
		setState(LifecycleState.STARTING);
		Boolean attachedToValve = false;
		for (Valve valve : getContainer().getPipeline().getValves()) {
			if (valve instanceof RequestSessionHandlerValve) {
				this.handlerValve = (RequestSessionHandlerValve) valve;
				this.handlerValve.setRedisSessionManager(this);
				attachedToValve = true;
				break;
			}
		}
		if (!attachedToValve) {
			throw new LifecycleException("Unable to attach to session handling valve; sessions cannot be saved after the request without the valve starting properly.");
		}
		try {
			initializeSessionSerializer();
			requestSessionCacheUtils = RequestSessionCacheFactory.getInstance();
		} catch (Exception e) {
			log.error("Error while initializing serializer/rediscache", e);
		}
		log.info("The sessions will expire after " + (getContext().getSessionTimeout() * 60) + " seconds");
		getContext().setDistributable(true);
	}

	@Override
	protected synchronized void stopInternal() throws LifecycleException {
		setState(LifecycleState.STOPPING);
		super.stopInternal();
	}

	@Override
	public Session createSession(String requestedSessionId) {
		CustomRequestSession customSession = null;
		String sessionId = null;
		if (requestedSessionId != null) {
			sessionId = requestedSessionId;
			if (requestSessionCacheUtils.setStringIfKeyNotExists(sessionId.getBytes(), NULL_SESSION) == 0L) {
				sessionId = null;
			}
		} else {
			do {
				sessionId = generateSessionId();
			} while (requestSessionCacheUtils.setStringIfKeyNotExists(sessionId.getBytes(), NULL_SESSION) == 0L); // 1 = key set; 0 = key already existed
		}
		if (sessionId != null) {
			customSession = (CustomRequestSession) createEmptySession();
			customSession.setNew(true);
			customSession.setValid(true);
			customSession.setCreationTime(System.currentTimeMillis());
			customSession.setMaxInactiveInterval((getContext().getSessionTimeout() * 60));
			customSession.setId(sessionId);
			customSession.tellNew();
		}
		currentSession.set(customSession);
		currentSessionId.set(sessionId);
		currentSessionIsPersisted.set(false);
		currentSessionSerializationMetadata.set(new SessionSerializationMetadata());
		if (customSession != null) {
			try {
				save(customSession, true);
			} catch (Exception e) {
				log.error("Error saving newly created session", e);
				currentSession.set(null);
				currentSessionId.set(null);
				customSession = null;
			}
		}
		return customSession;
	}

	@Override
	public Session createEmptySession() {
		return new CustomRequestSession(this);
	}

	@Override
	public void add(Session session) {
		save(session, false);
	}

	@Override
	public Session findSession(String sessionId) throws IOException {
		CustomRequestSession customSession = null;
		if (sessionId == null) {
			currentSessionIsPersisted.set(false);
			currentSession.set(null);
			currentSessionSerializationMetadata.set(null);
			currentSessionId.set(null);
		} else if (sessionId.equals(currentSessionId.get())) {
			customSession = currentSession.get();
		} else {
			byte[] data = requestSessionCacheUtils.getByteArray(sessionId);
			if (data != null) {
				DeserializedSessionContainer container = deserializeSessionData(sessionId, data);
				customSession = (CustomRequestSession) container.session;
				currentSession.set(customSession);
				currentSessionSerializationMetadata.set(container.metadata);
				currentSessionIsPersisted.set(true);
				currentSessionId.set(sessionId);
			} else {
				currentSessionIsPersisted.set(false);
				currentSession.set(null);
				currentSessionSerializationMetadata.set(null);
				currentSessionId.set(null);
			}
		}
		return customSession;
	}

	@Override
	public void remove(Session session) {
		remove(session, false);
	}

	@Override
	public void remove(Session session, boolean update) {
		requestSessionCacheUtils.expire(session.getId(), 10);
	}

	@Override
	public void load() throws ClassNotFoundException, IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public void unload() throws IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * method to deserialize session data
	 * 
	 * @param id
	 * @param data
	 * @return
	 * @throws IOException
	 */
	public DeserializedSessionContainer deserializeSessionData(String id, byte[] data) throws IOException {
		if (Arrays.equals(NULL_SESSION, data)) {
			throw new IOException("Serialized session data was equal to NULL_SESSION");
		}
		CustomRequestSession customSession = null;
		SessionSerializationMetadata metadata = null;
		try {
			metadata = new SessionSerializationMetadata();
			customSession = (CustomRequestSession) createEmptySession();
			serializer.deserializeSessionData(data, customSession, metadata);
			customSession.setId(id);
			customSession.setNew(false);
			customSession.setMaxInactiveInterval((getContext().getSessionTimeout() * 60));
			customSession.access();
			customSession.setValid(true);
			customSession.resetDirtyTracking();
		} catch (Exception e) {
			log.error("Unable to deserialize into session", e);
		}
		return new DeserializedSessionContainer(customSession, metadata);
	}

	/**
	 * method to save session data to cache
	 * 
	 * @param session
	 * @param forceSave
	 */
	public void save(Session session, boolean forceSave) {
		Boolean isCurrentSessionPersisted;
		try {
			CustomRequestSession customSession = (CustomRequestSession) session;
			SessionSerializationMetadata sessionSerializationMetadata = currentSessionSerializationMetadata.get();
			byte[] originalSessionAttributesHash = sessionSerializationMetadata.getSessionAttributesHash();
			byte[] sessionAttributesHash = null;
			if (forceSave || customSession.isDirty() || (isCurrentSessionPersisted = this.currentSessionIsPersisted.get()) == null || !isCurrentSessionPersisted || !Arrays.equals(originalSessionAttributesHash, (sessionAttributesHash = serializer.getSessionAttributesHashCode(customSession)))) {
				if (sessionAttributesHash == null) {
					sessionAttributesHash = serializer.getSessionAttributesHashCode(customSession);
				}
				SessionSerializationMetadata updatedSerializationMetadata = new SessionSerializationMetadata();
				updatedSerializationMetadata.setSessionAttributesHash(sessionAttributesHash);
				requestSessionCacheUtils.setByteArray(customSession.getId(), serializer.serializeSessionData(customSession, updatedSerializationMetadata));
				customSession.resetDirtyTracking();
				currentSessionSerializationMetadata.set(updatedSerializationMetadata);
				currentSessionIsPersisted.set(true);
			}
			
			int timeout = getContext().getSessionTimeout() * 60;
			timeout = timeout < 1800 ? 1800 : timeout;
			log.trace("Setting expire timeout on session [" + customSession.getId() + "] to " + timeout);
			requestSessionCacheUtils.expire(customSession.getId(), timeout);
		} catch (IOException e) {
			log.error("Error occured while storing the session object into redis", e);
		}
	}

	public void afterRequest(Request request) {
		CustomRequestSession customSession = currentSession.get();
		if (customSession != null) {
			try {
				if (customSession.isValid()) {
					log.trace("Request with session completed, saving session " + customSession.getId());
					save(customSession, getAlwaysSaveAfterRequest());
				} else {
					log.trace("HTTP Session has been invalidated, removing :" + customSession.getId());
					remove(customSession);
				}
			} catch (Exception e) {
				log.error("Error storing/updating/removing session", e);
			} finally {
				currentSession.remove();
				currentSessionId.remove();
				currentSessionIsPersisted.remove();
				log.trace("Session removed from ThreadLocal :" + customSession.getIdInternal());
			}
		}
	}

	/**
	 * method to initialize custom session serializer
	 * 
	 * @throws Exception
	 */
	private void initializeSessionSerializer() throws Exception {
		serializer = new SessionDataSerializer();
		Loader loader = null;
		if (getContext() != null) {
			loader = getContext().getLoader();
		}
		ClassLoader classLoader = null;
		if (loader != null) {
			classLoader = loader.getClassLoader();
		}
		serializer.setClassLoader(classLoader);
	}
}
