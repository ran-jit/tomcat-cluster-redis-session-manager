package tomcat.request.session.redis;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Manager;
import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.authenticator.SingleSignOn;
import org.apache.catalina.authenticator.SingleSignOnSessionKey;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tomcat.request.session.exception.BackendException;
import tomcat.request.session.model.SingleSignOnEntry;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.security.Principal;
import java.util.Set;

/** author: Ranjith Manickam @ 20 Mar' 2020 */
public class SingleSignOnValve extends SingleSignOn {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleSignOnValve.class);

    private Engine engine;
    private SessionManager manager;

    /** {@inheritDoc} */
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        Container c;
        for (c = this.getContainer(); c != null && !(c instanceof Engine); c = c.getParent()) {
        }

        if (c instanceof Engine) {
            this.engine = (Engine) c;
        }

        super.startInternal();
    }

    /** {@inheritDoc} */
    @Override
    public void invoke(Request request, Response response) throws BackendException {
        try {
            this.setSessionManager(request.getContext().getManager());

            request.removeNote("org.apache.catalina.request.SSOID");
            LOGGER.debug("singleSignOn.debug.invoke, requestURI: {}", request.getRequestURI());

            if (request.getUserPrincipal() == null) {
                LOGGER.debug("singleSignOn.debug.cookieCheck");
                Cookie cookie = null;
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie value : cookies) {
                        if (Constants.SINGLE_SIGN_ON_COOKIE.equals(value.getName())) {
                            cookie = value;
                            break;
                        }
                    }
                }

                if (cookie == null) {
                    LOGGER.debug("singleSignOn.debug.cookieNotFound");
                } else {
                    LOGGER.debug("singleSignOn.debug.principalCheck, ssoId: {}", cookie.getValue());

                    SingleSignOnEntry entry = this.manager.getSingleSignOnEntry(cookie.getValue());
                    if (entry == null) {
                        LOGGER.debug("singleSignOn.debug.principalNotFound, ssoId: {}", cookie.getValue());
                        cookie.setValue("REMOVE");
                        cookie.setMaxAge(0);
                        cookie.setPath("/");
                        String domain = this.getCookieDomain();
                        if (domain != null) {
                            cookie.setDomain(domain);
                        }

                        cookie.setSecure(request.isSecure());
                        if (request.getServletContext().getSessionCookieConfig().isHttpOnly() || request.getContext().getUseHttpOnly()) {
                            cookie.setHttpOnly(true);
                        }
                        response.addCookie(cookie);
                    } else {
                        LOGGER.debug("singleSignOn.debug.principalFound, principal: {}, authType: {}", (entry.getPrincipal() != null ? entry.getPrincipal().getName() : ""), entry.getAuthType());
                        request.setNote("org.apache.catalina.request.SSOID", cookie.getValue());
                        if (!this.getRequireReauthentication()) {
                            request.setAuthType(entry.getAuthType());
                            request.setUserPrincipal(entry.getPrincipal());
                        }
                    }
                }
            } else {
                LOGGER.debug("singleSignOn.debug.hasPrincipal, principal: {}", request.getUserPrincipal().getName());
            }
            this.getNext().invoke(request, response);

        } catch (IOException | ServletException | RuntimeException ex) {
            LOGGER.error("Error processing request", ex);
            throw new BackendException();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void sessionDestroyed(String ssoId, Session session) {
        if (this.getState().isAvailable()) {
            if ((session.getMaxInactiveInterval() <= 0 ||
                    session.getIdleTimeInternal() < (long) (session.getMaxInactiveInterval() * 1000))
                    && session.getManager().getContext().getState().isAvailable()) {

                LOGGER.debug("singleSignOn.debug.sessionLogout, session: {}", session);
                this.removeSession(ssoId, session);
                if (this.manager.singleSignOnEntryExists(ssoId)) {
                    this.deregister(ssoId);
                }
                return;
            }

            LOGGER.debug("singleSignOn.debug.sessionTimeout, ssoId: {}, session: {}", ssoId, session);
            this.removeSession(ssoId, session);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected boolean associate(String ssoId, Session session) {
        SingleSignOnEntry entry = this.manager.getSingleSignOnEntry(ssoId);
        if (entry == null) {
            LOGGER.debug("singleSignOn.debug.associateFail, ssoId: {}, session: {}", ssoId, session);
            return false;
        }

        LOGGER.debug("singleSignOn.debug.associate, ssoId: {}, session: {}", ssoId, session);
        entry.addSession(ssoId, session);
        this.manager.setSingleSignOnEntry(ssoId, entry);
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected void deregister(String ssoId) {
        SingleSignOnEntry entry = this.manager.getSingleSignOnEntry(ssoId);
        this.manager.deleteSingleSignOnEntry(ssoId);
        if (entry == null) {
            LOGGER.debug("singleSignOn.debug.deregisterFail, ssoId: {}", ssoId);
            return;
        }

        Set<SingleSignOnSessionKey> ssoKeys = entry.findSessions();
        if (ssoKeys.isEmpty()) {
            LOGGER.debug("singleSignOn.debug.deregisterNone, ssoId: {}", ssoId);
        }

        for (SingleSignOnSessionKey ssoKey : ssoKeys) {
            this.expire(ssoKey);
            LOGGER.debug("singleSignOn.debug.deregister, ssoKey: {}, ssoId: {}", ssoKey, ssoId);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected boolean reauthenticate(String ssoId, Realm realm, Request request) {
        if (ssoId == null || realm == null) {
            return false;
        }

        boolean reAuthenticated = false;
        SingleSignOnEntry entry = this.manager.getSingleSignOnEntry(ssoId);
        if (entry != null && entry.getCanReauthenticate()) {
            String username = entry.getUsername();
            if (username != null) {
                Principal reAuthPrincipal = realm.authenticate(username, entry.getPassword());
                if (reAuthPrincipal != null) {
                    reAuthenticated = true;
                    request.setAuthType(entry.getAuthType());
                    request.setUserPrincipal(reAuthPrincipal);
                }
            }
        }
        return reAuthenticated;
    }

    /** {@inheritDoc} */
    @Override
    protected void register(String ssoId, Principal principal, String authType, String username, String password) {
        LOGGER.debug("singleSignOn.debug.register, ssoId: {}, principal: {}, authType: {}", ssoId, (principal != null ? principal.getName() : ""), authType);
        SingleSignOnEntry entry = new SingleSignOnEntry(principal, authType, username, password);
        this.manager.setSingleSignOnEntry(ssoId, entry);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean update(String ssoId, Principal principal, String authType, String username, String password) {
        SingleSignOnEntry entry = this.manager.getSingleSignOnEntry(ssoId);
        if (entry == null || !entry.getCanReauthenticate()) {
            return false;
        }

        LOGGER.debug("singleSignOn.debug.update, ssoId: {}, authType: {}", ssoId, authType);
        entry.updateCredentials(principal, authType, username, password);
        this.manager.setSingleSignOnEntry(ssoId, entry);
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected void removeSession(String ssoId, Session session) {
        LOGGER.debug("singleSignOn.debug.removeSession, ssoId: {}, session: {}", ssoId, session);
        SingleSignOnEntry entry = this.manager.getSingleSignOnEntry(ssoId);
        if (entry != null) {
            entry.removeSession(session);
            if (entry.findSessions().size() == 0) {
                this.deregister(ssoId);
            }
        }
    }

    /** To set session manager. */
    void setSessionManager(Manager manager) {
        this.manager = (SessionManager) manager;
    }

    /** To expire session. */
    private void expire(SingleSignOnSessionKey key) {
        if (this.engine == null) {
            LOGGER.warn("singleSignOn.sessionExpire.engineNull, key: {}", key);
        } else {
            Container host = this.engine.findChild(key.getHostName());
            if (host == null) {
                LOGGER.warn("singleSignOn.sessionExpire.hostNotFound, key: {}", key);
            } else {
                Context context = (Context) host.findChild(key.getContextName());
                if (context == null) {
                    LOGGER.warn("singleSignOn.sessionExpire.contextNotFound, key: {}", key);
                } else {
                    Session session;
                    try {
                        session = this.manager.findSession(key.getSessionId());
                    } catch (IOException ex) {
                        LOGGER.warn("singleSignOn.sessionExpire.managerError, key: {}, exception: {}", key, ex);
                        return;
                    }

                    if (session == null) {
                        LOGGER.warn("singleSignOn.sessionExpire.sessionNotFound, key: {}", key);
                    } else {
                        session.expire();
                    }
                }
            }
        }
    }
}
