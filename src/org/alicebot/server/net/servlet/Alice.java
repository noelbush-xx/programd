package org.alicebot.server.net.servlet;
	
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import org.alicebot.server.core.*;
import org.alicebot.server.core.responder.*;

/**
 * This is the Chat servlet used to broker a conversation from a client.
 * 
 * It does not really do much except pass information to the SmartResponder
 * which is responsible for:
 *
 * 1. Determining the type of client requesting a bot response (via User-Agent)
 * 2. Obtains a bot response from the Graphmaster
 * 3. Forwards the bot response to the appropriate Responder
 *
 * @author Jon Baer
 * @author Kris Drent
 * @author Richard Wallace
 */

public class Alice extends HttpServlet {
	
	public void init() throws ServletException {
		
	}
	
	public void init(ServletConfig config) throws ServletException {
		
	}
	
	/**
	 * The doGet method.
	 * @param request
	 * @param response
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		
		// Get A Smart Responder For Request
		SmartResponder responder = new SmartResponder(request, response);
		responder.doResponse();
		
	}
	
	/**
	 * The doPost method.
	 * @param request
	 * @param response
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		doGet(request, response);
	}
	
}
