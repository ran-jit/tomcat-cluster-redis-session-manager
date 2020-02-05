package tomcat.request.session.redis;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tomcat.request.session.exception.BackendException;

import javax.servlet.ServletException;
import java.io.IOException;

/** author: Ranjith Manickam @ 12 Jul' 2018 */
public class SessionHandlerValve extends ValveBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionHandlerValve.class);

    private SessionManager manager;

    /** To set session manager */
    void setSessionManager(SessionManager manager) {
        this.manager = manager;
    }

    /** {@inheritDoc} */
    @Override
    public void invoke(Request request, Response response) throws BackendException {
        try {
            getNext().invoke(request, response);
        } catch (IOException | ServletException | RuntimeException ex) {
            LOGGER.error("Error processing request", ex);
            throw new BackendException();
        } finally {
            this.manager.afterRequest();
        }
    }
}
