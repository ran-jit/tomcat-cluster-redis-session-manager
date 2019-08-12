package tomcat.request.session.manager;

import tomcat.request.session.SessionConstants;
import tomcat.request.session.data.cache.DataCache;

import java.util.Set;

/** author: Ranjith Manickam @ 12 Aug' 2019 */
public class SessionDetailsManager {

    private final DataCache dataCache;
    private final String sessionIdPrefix;

    public SessionDetailsManager(DataCache dataCache, String sessionIdPrefix) {
        this.dataCache = dataCache;
        this.sessionIdPrefix = sessionIdPrefix.equals(SessionConstants.EMPTY_STRING) ? sessionIdPrefix : sessionIdPrefix.concat("-*");
    }

    /** Returns active session count. */
    public int getActiveSessionCount() {
        Set<String> activeSessionIds = getActiveSessionIds();
        return activeSessionIds == null ? 0 : activeSessionIds.size();
    }

    /** Returns active session ids. */
    private Set<String> getActiveSessionIds() {
        return this.dataCache.keys(this.sessionIdPrefix);
    }
}
