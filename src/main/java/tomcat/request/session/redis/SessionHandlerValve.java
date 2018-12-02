package tomcat.request.session.redis;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tomcat.request.session.exception.BackendException;

import javax.servlet.ServletException;
import java.io.IOException;

/** author: Ranjith Manickam @ 12 Jul' 2018 */
public class SessionHandlerValve extends ValveBase {

    private static final Log LOGGER = LogFactory.getLog(SessionHandlerValve.class);

    private SessionManager manager;

    /** To set session manager */
    public void setSessionManager(SessionManager manager) {
        this.manager = manager;
    }

    /** {@inheritDoc} */
    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        try {
            getNext().invoke(request, response);
        } catch (IOException | ServletException | RuntimeException ex) {
            LOGGER.error("Error processing request", ex);
            new BackendException();
        } finally {
            manager.afterRequest(request);
        }
    }

}
