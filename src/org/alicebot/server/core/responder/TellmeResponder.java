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

import org.alicebot.server.core.*;
import org.alicebot.server.core.logging.*;
import org.alicebot.server.core.util.*;

public class TellmeResponder implements Responder, Serializable {
	
	StringBuffer reply;
	
	public TellmeResponder() {
		reply = new StringBuffer();
		reply.append("<?xml version=\"1.0\"?>\n");
		reply.append("<!DOCTYPE vxml PUBLIC \"-//Tellme Networks//Voice Markup Language 1.0//EN\" \"http://resources.tellme.com/toolbox/vxml-tellme.dtd\">\n");
		reply.append("<vxml>\n");
		reply.append("<form id=\"query\">\n");
		reply.append("<var name=\"text\"/>\n");
		reply.append("<block><audio src=\"http://resources.tellme.com/audio/earcons/goodbye-se.wav\" /></block>");
		reply.append("<field name=\"text\">\n");
		reply.append("<grammar src=\"/GSL\" />\n");
	}

	public String pre_process(String input, String hname) {
		input = input.replace('_', ' ');
		return input;
	}
	public void log(String input, String response, String hname) {
		Date dt = new Date();
	Log.log(dt.toString() + " " + hname + "\nClient: " + input + "\nRobot:" + response+"\n", Log.CHAT);
	
	}
	public String append(String input, String response, String scroll) {
		return response;
	}
	
	public String post_process(String bot_reply) {
		reply.append("<prompt>");
		reply.append("<audio>" + bot_reply + "</audio>\n");reply.append("</prompt>\n");
		reply.append("<noinput><goto next=\"/CHAT?text=ASK+USER+A+QUESTION\" /></noinput>\n");
		reply.append("<nomatch><reprompt /></nomatch>\n");
		reply.append("<filled>\n");
		reply.append("<goto expr=\"'/CHAT?text=' + text\" /></filled>\n");
		reply.append("</field></form></vxml>");
		return reply.toString();
	}
} 
