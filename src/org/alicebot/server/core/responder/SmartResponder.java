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
import org.alicebot.server.net.*;

/**
 * The SmartResponder is a broker for requests coming into the server.
 * Its goal is to take and process a response and format it for the appropriate
 * device, formatting the output as needed through out Responders.
 * What makes it smart is its ability to take and parse the User-Agent and forward
 * the request to that Responder.
 *
 * In general a Responder must have:
 * 
 * 1. A method of output (text output, speech, XML)
 * 2. A method of input (text input, speech, XML)
 *
 * The main method of this class is forward() which will redirect the HttpRequest as needed.
 *
 * @author Jon Baer
 * @version 1.0
 */

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SmartResponder {
	
	/** The service agent. */
	public String serviceAgent;
	
	/** The service response. */
	public HttpServletResponse serviceResponse;
	
	/** The service request. */
	public HttpServletRequest serviceRequest;
	
	/** The responder. */
	public Responder responder;
	
	/** The bots response. */
	public String botResponse;
	
	/** The users request. */
	public String userRequest;
	
	/** The users identification. */
	public String userID;
	
	/** The service type. */
	public int serviceType;
	
	/** The service output stream. */
	public ServletOutputStream serviceOutputStream;
	
	/** The public constructor. */
	public SmartResponder(HttpServletRequest request, HttpServletResponse response) {
		this.serviceRequest = request;
		this.serviceResponse = response;
		this.userRequest = request.getParameter("text");
		// If nothing then we assume a connection
		if (this.userRequest == null) this.userRequest = "CONNECT";
		// Check for blank or startup request
		if (this.userRequest.equals("")) this.userRequest = "INACTIVITY";
		this.userID = request.getParameter("virtual_ip");
		if (this.userID == null) this.userID = request.getRemoteHost();
	}
	
	/** 
	 * Sets a request.
	 * @param request
	 */
	public void setRequest(HttpServletRequest request) {
		this.serviceRequest = request;
	}
	
	/** 
	 * Sets a response.
	 * @param response
	 */
	public void setResponse(HttpServletResponse response) {
		this.serviceResponse = response;
	}
	
	/** 
	 * Invokes a response.
	 */
	public void doResponse() {
		
		try {
			
			this.serviceOutputStream = this.serviceResponse.getOutputStream();
			
			switch (getServiceType()) {
				
			case ServiceType.UNKNOWN:
				this.serviceResponse.setContentType("text/plain");
				this.botResponse = Classifier.doResponse(this.userRequest, this.userID,  new TextResponder());
				this.serviceOutputStream.println(this.botResponse);
				// System.out.println("*** DETECTED UNKNOWN DEVICE ***");
				break;
				
			case ServiceType.PALM:
				this.serviceResponse.setContentType("text/html");
				this.botResponse = Classifier.doResponse(this.userRequest, this.userID,  new PalmVIIResponder());
				this.serviceOutputStream.println(this.botResponse);
				// System.out.println("*** DETECTED PALM DEVICE ***");
				break;
				
			case ServiceType.GRAPHMASTER:
				// Not implemented
				break;
				
			case ServiceType.PLAIN_TEXT:
				this.serviceResponse.setContentType("text/plain");
				this.botResponse = Classifier.doResponse(this.userRequest, this.userID,  new TextResponder());
				this.serviceOutputStream.println(this.botResponse);
				// System.out.println("*** DETECTED PLAIN TEXT DEVICE ***");
				break;
				
			case ServiceType.AIML:
				this.serviceResponse.setContentType("text/x-aiml-response");
				this.botResponse = Classifier.doResponse(this.userRequest, this.userID,  new AIMLResponder());
				this.serviceOutputStream.println(this.botResponse);
				// System.out.println("*** DETECTED AIML DEVICE ***");
				break;
				
			case ServiceType.HTML:
				this.serviceResponse.setContentType("text/html");
				this.botResponse = Classifier.doResponse(this.userRequest, this.userID,  new HTMLResponder());
				this.serviceOutputStream.println(this.botResponse);
				// System.out.println("*** DETECTED HTML DEVICE ***");
				break;
				
			case ServiceType.FLASH:
				this.serviceResponse.setContentType("text/plain");
				this.botResponse = Classifier.doResponse(this.userRequest, this.userID,  new FlashResponder());
				this.serviceOutputStream.println(this.botResponse);
				// System.out.println("*** DETECTED FLASH DEVICE ***");
				break;
				
			case ServiceType.IMODE:
				this.serviceResponse.setContentType("text/html");
				this.botResponse = Classifier.doResponse(this.userRequest, this.userID,  new iModeResponder());
				this.serviceOutputStream.println(this.botResponse);
				// System.out.println("*** DETECTED I-MODE DEVICE ***");
				break;
				
			case ServiceType.HDML:
				this.serviceResponse.setContentType("text/x-hdml");
				this.botResponse = Classifier.doResponse(this.userRequest, this.userID,  new HDMLResponder());
				this.serviceOutputStream.println(this.botResponse);
				// System.out.println("*** DETECTED HDML DEVICE ***");
				break;
				
			case ServiceType.TELLME:
				this.serviceResponse.setContentType("text/xml");
				this.botResponse = Classifier.doResponse(this.userRequest, this.userID,  new TellmeResponder());
				this.serviceOutputStream.println(this.botResponse);
				// System.out.println("*** DETECTED TELLME DEVICE ***");
				break;
				
			case ServiceType.BEVOCAL:
				this.serviceResponse.setContentType("text/xml");
				this.botResponse = Classifier.doResponse(this.userRequest, this.userID,  new BeVocalResponder());
				this.serviceOutputStream.println(this.botResponse);
				break;
				
			case ServiceType.WML:
				this.serviceResponse.setContentType("text/vnd.wap.wml");
				this.botResponse = Classifier.doResponse(this.userRequest, this.userID,  new WMLResponder());
				this.serviceOutputStream.println(this.botResponse);
				break;
				
			case ServiceType.SPEAK:
				this.serviceResponse.setContentType("text/html");
				this.botResponse = Classifier.doResponse(this.userRequest, this.userID,  new SpeakResponder());
				this.serviceOutputStream.println(this.botResponse);
				// System.out.println("*** DETECTED SPEECH DEVICE ***");
				break;
				
			case ServiceType.MSAGENT:
				this.serviceResponse.setContentType("text/html");
				this.botResponse = Classifier.doResponse(this.userRequest, this.userID,  new MSAgentResponder());
				this.serviceOutputStream.println(this.botResponse);
				// System.out.println("*** DETECTED MSAGENT DEVICE ***");
				break;
				
			case ServiceType.ACTOR:
				this.serviceResponse.setContentType("text/html");
				this.botResponse = Classifier.doResponse(this.userRequest, this.userID,  new LivingActorResponder());
				this.serviceOutputStream.println(this.botResponse);
				// System.out.println("*** DETECTED LIVING ACTOR DEVICE ***");
				break;
				
			}
			
			this.serviceOutputStream.flush();
			this.serviceOutputStream.close();
			
		} catch (Exception e) {}
		
	}
	
	public int getServiceType() {
		if (this.serviceRequest.getParameter("speak") != null) return ServiceType.SPEAK;
		if (this.serviceRequest.getParameter("plain_text") != null) return ServiceType.PLAIN_TEXT;
		if (this.serviceRequest.getParameter("flash") != null) return ServiceType.FLASH;
		if (this.serviceRequest.getParameter("pattern") != null) return ServiceType.GRAPHMASTER; 
		if (this.serviceRequest.getHeader("USER-AGENT").indexOf("Elaine") > -1) return ServiceType.PALM;
		if (this.serviceRequest.getHeader("USER-AGENT").indexOf("Mozilla") > -1) return ServiceType.HTML;
		if (this.serviceRequest.getHeader("USER-AGENT").indexOf("MSIE") > -1) return ServiceType.HTML;
		if (this.serviceRequest.getHeader("USER-AGENT").indexOf("AIML") > -1) return ServiceType.AIML;
		if (this.serviceRequest.getHeader("USER-AGENT").indexOf("Alice") > -1) return ServiceType.AIML;
		if (this.serviceRequest.getHeader("ACCEPT").indexOf("wmlscript") > -1) return ServiceType.WML;
		if (this.serviceRequest.getHeader("USER-AGENT").indexOf("UP.B") > -1) return ServiceType.HDML;
		if (this.serviceRequest.getHeader("USER-AGENT").indexOf("BeVocal") > -1) return ServiceType.BEVOCAL;
		if (this.serviceRequest.getHeader("USER-AGENT").indexOf("Tellme") > -1) return ServiceType.TELLME;
		if (this.serviceRequest.getHeader("USER-AGENT").indexOf("DoCoMo") > -1) return ServiceType.IMODE;
		if (this.serviceRequest.getHeader("USER-AGENT").indexOf("PMLI") > -1) return ServiceType.VOICEGENIE;
		if (this.serviceRequest.getHeader("USER-AGENT").indexOf("Voyager") > -1) return ServiceType.NUANCE;
		return ServiceType.UNKNOWN;
	}
	
} 

class ServiceType {
	public static final int UNKNOWN = 0;
	public static final int HTML = 1;
	public static final int AIML = 2;
	public static final int XML = 3;
	public static final int HDML = 4;
	public static final int WML = 5;
	public static final int J2ME = 6;
	public static final int CLDC = 7;
	public static final int PALM = 8;
	public static final int TELLME = 9;
	public static final int BEVOCAL = 10;
	public static final int IMODE = 11;
	public static final int VOICEGENIE = 12;
	public static final int SPEAK = 13;
	public static final int BABELFISH = 14;
	public static final int MSAGENT = 15;
	public static final int PLAIN_TEXT = 16;
	public static final int ACTOR = 17;
	public static final int NUANCE = 18;
	public static final int GRAPHMASTER = 19;
	public static final int FLASH = 20;
}
