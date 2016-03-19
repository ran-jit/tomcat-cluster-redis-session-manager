package com.r.tomcat.session.management;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

/**
 * Tomcat clustering implementation
 * 
 * This class is uses to store and retrieve the HTTP request session objects from catalina to data cache
 *
 * @author Ranjith Manickam
 * @since 1.0
 */
public class RequestSessionHandlerValve extends ValveBase
{
	private RequestSessionManager manager;

	public void setRedisSessionManager(RequestSessionManager manager) {
		this.manager = manager;
	}

	@Override
	public void invoke(Request request, Response response) throws IOException, ServletException {
		try {
			getNext().invoke(request, response);
		} finally {
			manager.afterRequest(request);
		}
	}
}