package tomcat.request.session.model;

/** author: Ranjith Manickam @ 12 Jul' 2018 */
public class SessionContext {

    private String id;
    private Session session;
    private boolean persisted;
    private SessionMetadata metadata;

    /** To get session id. */
    public String getId() {
        return id;
    }

    /** To set session id. */
    public void setId(String id) {
        this.id = id;
    }

    /** To get session. */
    public Session getSession() {
        return session;
    }

    /** To set session. */
    public void setSession(Session session) {
        this.session = session;
    }

    /** To check session is persisted. */
    public boolean isPersisted() {
        return persisted;
    }

    /** To set session persisted. */
    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }

    /** To get session meta-data. */
    public SessionMetadata getMetadata() {
        return metadata;
    }

    /** To set session meta-data. */
    public void setMetadata(SessionMetadata metadata) {
        this.metadata = metadata;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "SessionContext [id=" + id + "]";
    }
}
