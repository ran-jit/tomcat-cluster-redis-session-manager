package com.r.tomcat.session.management;

import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;

import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Tomcat clustering implementation
 * 
 * This class is uses to store and retrieve the HTTP request session objects from catalina to data cache
 *
 * @author Ranjith Manickam
 * @since 1.0
 */
public class CustomRequestSession extends StandardSession
{
	private static final long serialVersionUID = 8237845843135996014L;

	private final Log log = LogFactory.getLog(CustomRequestSession.class);

	protected Boolean dirty;

	protected HashMap<String, Object> changedAttributes;

	protected static Boolean manualDirtyTrackingSupportEnabled = false;

	protected static String manualDirtyTrackingAttributeKey = "__changed__";

	public static void setManualDirtyTrackingSupportEnabled(Boolean enabled) {
		manualDirtyTrackingSupportEnabled = enabled;
	}

	public static void setManualDirtyTrackingAttributeKey(String key) {
		manualDirtyTrackingAttributeKey = key;
	}

	public CustomRequestSession(Manager manager) {
		super(manager);
		resetDirtyTracking();
	}

	public Boolean isDirty() {
		return dirty || !changedAttributes.isEmpty();
	}

	public HashMap<String, Object> getChangedAttributes() {
		return changedAttributes;
	}

	public void resetDirtyTracking() {
		changedAttributes = new HashMap<>();
		dirty = false;
	}

	@Override
	public void setAttribute(String key, Object value) {
		if (manualDirtyTrackingSupportEnabled && manualDirtyTrackingAttributeKey.equals(key)) {
			dirty = true;
			return;
		}
		Object oldValue = getAttribute(key);
		super.setAttribute(key, value);

		if ((value != null || oldValue != null) && (value == null && oldValue != null || oldValue == null && value != null || !value.getClass().isInstance(oldValue) || !value.equals(oldValue))) {
			if (this.manager instanceof RequestSessionManager && ((RequestSessionManager) this.manager).getSaveOnChange()) {
				try {
					((RequestSessionManager) this.manager).save(this, true);
				} catch (Exception ex) {
					log.error("Error saving session on setAttribute (triggered by saveOnChange=true): " + ex.getMessage());
				}
			} else {
				changedAttributes.put(key, value);
			}
		}
	}

	@Override
	public Object getAttribute(String name) {
		return super.getAttribute(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return super.getAttributeNames();
	}

	@Override
	public void removeAttribute(String name) {
		super.removeAttribute(name);
		if (this.manager instanceof RequestSessionManager && ((RequestSessionManager) this.manager).getSaveOnChange()) {
			try {
				((RequestSessionManager) this.manager).save(this, true);
			} catch (Exception ex) {
				log.error("Error saving session on removeAttribute (triggered by saveOnChange=true): " + ex.getMessage());
			}
		} else {
			dirty = true;
		}
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public void setPrincipal(Principal principal) {
		dirty = true;
		super.setPrincipal(principal);
	}

	@Override
	public void writeObjectData(java.io.ObjectOutputStream out) throws IOException {
		super.writeObjectData(out);
		out.writeLong(this.getCreationTime());
	}

	@Override
	public void readObjectData(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
		super.readObjectData(in);
		this.setCreationTime(in.readLong());
	}
}