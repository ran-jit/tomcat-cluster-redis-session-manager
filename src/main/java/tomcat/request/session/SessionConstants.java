package tomcat.request.session;

/** author: Ranjith Manickam @ 12 Jul' 2018 */
public interface SessionConstants {
    byte[] NULL_SESSION = "null".getBytes();
    String CATALINA_BASE = "catalina.base";
    String CONF = "conf";

    enum SessionPolicy {
        DEFAULT, SAVE_ON_CHANGE, ALWAYS_SAVE_AFTER_REQUEST;

        public static SessionPolicy fromName(String name) {
            for (SessionPolicy policy : SessionPolicy.values()) {
                if (policy.name().equalsIgnoreCase(name)) {
                    return policy;
                }
            }
            throw new IllegalArgumentException("Invalid session policy [" + name + "]");
        }
    }

}
