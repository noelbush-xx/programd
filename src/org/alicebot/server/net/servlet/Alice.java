/*
    Alicebot Program D
    Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

package org.alicebot.server.net.servlet;

	
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alicebot.server.core.responder.SmartResponder;

/**
 *  <p>
 *  This is the chat servlet used to broker a conversation from a client.
 *  It does not really do much except pass information to the SmartResponder,
 *  which is responsible for:
 *  </p>
 *  <ol>
 *  <li>
 *      Determining the type of client requesting a bot response (via User-Agent)
 *  </li>
 *  <li>
 *      Obtaining a bot response from the Graphmaster
 *  </li>
 *  <li>
 *      Forwarding the bot response to the appropriate Responder
 *  </li>
 *  </ol>
 *
 *  @author Jon Baer
 *  @author Kris Drent
 */
public class Alice extends HttpServlet
{
	public void init() throws ServletException
    {
		
	}
	
	public void init(ServletConfig config) throws ServletException
    {
		
	}
	

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
		SmartResponder responder = new SmartResponder(request, response);
		responder.doResponse();
		
	}

    
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
		doGet(request, response);
	}
}
