package tomcat.request.session.redis;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

/** author: Ranjith Manickam @ 12 Jul' 2018 */
public class SessionHandlerValve extends ValveBase {

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
        } finally {
            manager.afterRequest(request);
        }
    }

}
