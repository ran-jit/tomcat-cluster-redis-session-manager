package tomcat.request.session.model;

import org.apache.catalina.Session;
import org.apache.catalina.authenticator.SingleSignOnListener;
import org.apache.catalina.authenticator.SingleSignOnSessionKey;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.Principal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** author: Ranjith Manickam @ 20 Mar' 2020 */
public class SingleSignOnEntry implements Serializable {

    private String authType;
    private String password;
    private Principal principal;
    private String username;
    private boolean canReauthenticate = false;
    private final ConcurrentMap<SingleSignOnSessionKey, SingleSignOnSessionKey> sessionKeys;

    public SingleSignOnEntry() {
        this.sessionKeys = new ConcurrentHashMap<>();
    }

    public SingleSignOnEntry(Principal principal, String authType, String username, String password) {
        this.sessionKeys = new ConcurrentHashMap<>();
        this.updateCredentials(principal, authType, username, password);
    }

    public void addSession(String ssoId, Session session) {
        SingleSignOnSessionKey key = new SingleSignOnSessionKey(session);
        SingleSignOnSessionKey currentKey = this.sessionKeys.putIfAbsent(key, key);
        if (currentKey == null) {
            session.addSessionListener(new SingleSignOnListener(ssoId));
        }
    }

    public void removeSession(Session session) {
        SingleSignOnSessionKey key = new SingleSignOnSessionKey(session);
        this.sessionKeys.remove(key);
    }

    public Set<SingleSignOnSessionKey> findSessions() {
        return this.sessionKeys.keySet();
    }

    public String getAuthType() {
        return this.authType;
    }

    public boolean getCanReauthenticate() {
        return this.canReauthenticate;
    }

    public String getPassword() {
        return this.password;
    }

    public Principal getPrincipal() {
        return this.principal;
    }

    public String getUsername() {
        return this.username;
    }

    public synchronized void updateCredentials(Principal principal, String authType, String username, String password) {
        this.principal = principal;
        this.authType = authType;
        this.username = username;
        this.password = password;
        this.canReauthenticate = "BASIC".equals(authType) || "FORM".equals(authType);
    }

    public void writeObjectData(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeBoolean(true);
        out.writeObject(this.principal);
    }

    public void readObjectData(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        boolean hasPrincipal = in.readBoolean();
        if (hasPrincipal) {
            this.principal = (Principal) in.readObject();
        }
    }
}
