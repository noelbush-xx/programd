package org.alicebot.server.core.responder;

/**

ALICEBOT.NET Artificial Intelligence Project
This version is Copyright (C) 2000 Jon Baer.
jonbaer@digitalanywhere.com
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
notice, this list of conditions, and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions, and the disclaimer that follows 
these conditions in the documentation and/or other materials 
provided with the distribution.

3. The name "ALICEBOT.NET" must not be used to endorse or promote products
derived from this software without prior written permission.  For
written permission, please contact license@alicebot.org.

4. Products derived from this software may not be called "ALICEBOT.NET",
nor may "ALICEBOT.NET" appear in their name, without prior written permission
from the ALICEBOT.NET Project Management (jonbaer@alicebot.net).

In addition, we request (but do not require) that you include in the 
end-user documentation provided with the redistribution and/or in the 
software itself an acknowledgement equivalent to the following:
"This product includes software developed by the
ALICEBOT.NET Project (http://www.alicebot.net)."
Alternatively, the acknowledgment may be graphical using the logos 
available at http://www.alicebot.org/images/logos.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED.  IN NO EVENT SHALL THE ALICE SOFTWARE FOUNDATION OR
ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

This software consists of voluntary contributions made by many 
individuals on behalf of the A.L.I.C.E. Nexus and ALICEBOT.NET Project
and was originally created by Dr. Richard Wallace <drwallace@alicebot.net>.

This version was created by Jon Baer <jonbaer@alicebot.net>.

http://www.alicebot.org
http://www.alicebot.net

This version contains open-source technologies from:
Netscape, Apache, HypersonicSQL, JDOM, Jetty, Chris Carlin, IBM

*/


import java.util.*;
import java.net.*;
import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.alicebot.server.core.*;
import org.alicebot.server.core.util.*;

public class BabelfishResponder implements Responder,Serializable {
	
	StringBuffer reply;
	String language;
	
	public BabelfishResponder(String language) {
		this.language = language;
		// Gotta add some header template methods here
		reply = new StringBuffer();
		reply.append("<html><body onload=\"document.forms[0].elements[0].focus()\" alink=\"#cccccc\" vlink=\"#cccccc\" link=\"#cccccc\">");
		reply.append("<form method=\"post\" action=\"/CHAT?lang=" + this.language + "\">");
		reply.append("<font size=\"2\" face=\"verdana,arial\" color=\"#000000\"><b>AIML Server 3.0.1 (Alpha)</b></font><br><br>");
	}

	public String pre_process(String input, String hname) {
		reply.append("<font size=\"2\" face=\"verdana,arial\" color=\"#ccccc\">><i>" + input + "</i></font><br>");
		return input;
	}
	public void log(String input, String response, String hname) {
		
	}
	public String append(String input, String response, String scroll) {
		return response;
	}
	
	public String post_process(String bot_reply) {
		// Gotta add some footer template methods here
		
		try {
			
			URL url = new URL("http://babel.altavista.com/translate.dyn?enc=utf8&doit=done&BabelFishFrontPage=yes&bblType=urltext&urltext=" + URLEncoder.encode(bot_reply) + "&lp=en_" + this.language);
			URLConnection connection = url.openConnection();
					
			BufferedReader in = new BufferedReader(
				new InputStreamReader(
				connection.getInputStream()));
			String inputLine;
			int line = 0;
			while ((inputLine = in.readLine()) != null) {
				line++;
				if (line == 155) bot_reply = inputLine;
			}
			
			in.close();		 
			
		} catch (IOException e) {
			System.err.println(e);
		}
		
		reply.append("<font size=\"2\" face=\"verdana,arial\" color=\"#000000\">" + bot_reply + "</font><br>");
		reply.append("<input type=\"text\" size=\"30\" name=\"text\"> &nbsp; <input type=\"submit\" value=\" OK \"><br><br>");
		reply.append("<font size=\"2\" face=\"verdana,arial\" color=\"#000000\"><a href=\"mailto:jonbaer@digitalanywhere.com?subject=AIML Server\">jonbaer@digitalanywhere.com</a></font>");
		reply.append("</body></html>");
		return reply.toString();
	}
	
} 

