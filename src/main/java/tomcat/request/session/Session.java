package tomcat.request.session;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardSession;

import tomcat.request.session.redis.SessionManager;

/**
 * Tomcat clustering with Redis data-cache implementation.
 * 
 * This class is uses to store and retrieve the HTTP request session objects.
 *
 * @author Ranjith Manickam
 * @since 2.0
 */
public class Session extends StandardSession {

	private static final long serialVersionUID = -6056744304016869278L;

	protected Boolean dirty;

	protected Map<String, Object> changedAttributes;

	protected static Boolean manualDirtyTrackingSupportEnabled = false;

	protected static String manualDirtyTrackingAttributeKey = "__changed__";

	public Session(Manager manager) {
		super(manager);
		resetDirtyTracking();
	}

	public void resetDirtyTracking() {
		this.changedAttributes = new HashMap<>();
		this.dirty = false;
	}

	public static void setManualDirtyTrackingSupportEnabled(boolean enabled) {
		manualDirtyTrackingSupportEnabled = enabled;
	}

	public static void setManualDirtyTrackingAttributeKey(String key) {
		manualDirtyTrackingAttributeKey = key;
	}

	public Boolean isDirty() {
		return this.dirty || !this.changedAttributes.isEmpty();
	}

	public Map<String, Object> getChangedAttributes() {
		return this.changedAttributes;
	}

	/** {@inheritDoc} */
	@Override
	public void setId(String id) {
		this.id = id;
	}

	/** {@inheritDoc} */
	@Override
	public void setAttribute(String key, Object value) {
		if (manualDirtyTrackingSupportEnabled && manualDirtyTrackingAttributeKey.equals(key)) {
			this.dirty = true;
			return;
		}

		Object oldValue = getAttribute(key);
		super.setAttribute(key, value);

		if ((value != null || oldValue != null)
				&& (value == null && oldValue != null || oldValue == null && value != null
						|| !value.getClass().isInstance(oldValue) || !value.equals(oldValue))) {
			if (this.manager instanceof SessionManager && ((SessionManager) this.manager).getSaveOnChange()) {
				((SessionManager) this.manager).save(this, true);
			} else {
				this.changedAttributes.put(key, value);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public Object getAttribute(String name) {
		return super.getAttribute(name);
	}

	/** {@inheritDoc} */
	@Override
	public Enumeration<String> getAttributeNames() {
		return super.getAttributeNames();
	}

	/** {@inheritDoc} */
	@Override
	public void removeAttribute(String name) {
		super.removeAttribute(name);
		if (this.manager instanceof SessionManager && ((SessionManager) this.manager).getSaveOnChange()) {
			((SessionManager) this.manager).save(this, true);
		} else {
			this.dirty = true;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setPrincipal(Principal principal) {
		super.setPrincipal(principal);
		this.dirty = true;
	}

	/** {@inheritDoc} */
	@Override
	public void writeObjectData(ObjectOutputStream out) throws IOException {
		super.writeObjectData(out);
		out.writeLong(this.getCreationTime());
	}

	/** {@inheritDoc} */
	@Override
	public void readObjectData(ObjectInputStream in) throws IOException, ClassNotFoundException {
		super.readObjectData(in);
		this.setCreationTime(in.readLong());
	}

	/** {@inheritDoc} */
	@Override
	public void invalidate() {
		super.invalidate();
	}
}