package tomcat.request.session;

/**
 * Tomcat clustering with Redis data-cache implementation.
 *
 * Session constants.
 *
 * @author Ranjith Manickam
 * @since 2.0
 */
public interface SessionConstants {

  byte[] NULL_SESSION = "null".getBytes();

  String CATALINA_BASE = "catalina.base";

  String CONF = "conf";
}