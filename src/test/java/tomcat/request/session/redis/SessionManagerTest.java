package tomcat.request.session.redis;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/")
public class SessionManagerTest extends HttpServlet {

	private static final long serialVersionUID = 7464510533820701851L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String action = request.getParameter("action");
		action = (action == null) ? "" : action;
		String responseData = null;

		switch (action.toUpperCase()) {
		case "SET":
			responseData = setSessionValues(request.getSession());
			break;
		case "GET":
			responseData = getSessionValues(request.getSession(), action);
			break;
		default:
			responseData = getActions(request);
			break;
		}

		sendResponse(response, responseData);
	}

	/**
	 * method to send response
	 * 
	 * @param response
	 * @param responseData
	 * @throws IOException
	 */
	private void sendResponse(HttpServletResponse response, String responseData) throws IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(responseData);
	}

	/**
	 * method to get actions
	 * 
	 * @param request
	 * @return
	 */
	private String getActions(HttpServletRequest request) {
		StringBuffer xml = new StringBuffer();

		xml.append("<!DOCTYPE html>");
		xml.append("<html>");
		xml.append("<head>");
		xml.append("<meta charset='UTF-8'>");
		xml.append("<title>tomcat-cluster-redis-session-manager-test</title>");
		xml.append("</head>");
		xml.append("<body>");
		xml.append("<h2>tomcat-cluster-redis-session-manager-test</h2>");
		xml.append("<h4>actions</h4>");

		String url = request.getRequestURL().toString();
		url = (url.contains("?action=") ? (url.substring(0, url.indexOf("?action="))) : url).concat("?action=");

		xml.append("<a href='");
		xml.append(url);
		xml.append("SET");
		xml.append("'>SET</a>");
		xml.append("<br><br>");
		xml.append("<a href='");
		xml.append(url);
		xml.append("GET");
		xml.append("'>GET</a>");

		xml.append("</body>");
		xml.append("</html>");

		return xml.toString();
	}

	/**
	 * method to set session values
	 * 
	 * @param session
	 * @return
	 */
	private String setSessionValues(HttpSession session) {
		for (int i = 0; i < 10; i++) {
			session.setAttribute("test-" + i, "test-" + new Date().getTime());
		}
		return getSessionValues(session, "SET");
	}

	/**
	 * method to get session values
	 * 
	 * @param session
	 * @param action
	 * @return
	 */
	private String getSessionValues(HttpSession session, String action) {
		StringBuffer xml = new StringBuffer();
		xml.append("<!DOCTYPE html>");
		xml.append("<html>");
		xml.append("<head>");
		xml.append("<meta charset='UTF-8'>");
		xml.append("<title>tomcat-cluster-redis-session-manager-test</title>");
		xml.append("</head>");
		xml.append("<body>");
		xml.append("<h2>tomcat-cluster-redis-session-manager-test-results</h2>");
		xml.append("<h4>action: ");
		xml.append(action);
		xml.append("</h4>");
		xml.append("<table width='30%' border='1' style='text-align: center;'>");
		xml.append("<tr>");
		xml.append("<th width='50%'>Key</th>");
		xml.append("<th width='50%'>Value</th>");
		xml.append("</tr>");

		Enumeration<String> names = session.getAttributeNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			xml.append("<tr>");
			xml.append("<td>");
			xml.append(name);
			xml.append("</td>");
			xml.append("<td>");
			xml.append(session.getAttribute(name));
			xml.append("</td>");
			xml.append("</tr>");
		}

		xml.append("</table>");
		xml.append("</body>");
		xml.append("</html>");

		return xml.toString();
	}
}