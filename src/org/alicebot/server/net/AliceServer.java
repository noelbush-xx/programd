package org.alicebot.server.net;

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

import org.alicebot.server.core.*;
import org.alicebot.server.core.logging.*;
import org.alicebot.server.core.loader.*;

// import org.apache.tomcat.startup.*;
import org.alicebot.server.net.http.*;
import org.alicebot.server.net.http.server.*;
import org.alicebot.server.net.http.util.*;
import org.alicebot.server.net.listener.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.*;
import javax.servlet.*;

public class AliceServer extends Object {
	
	/** The Graphmaster used by the server. */
	private static Graphmaster graphmaster;
	
	/** The properties to the server. */
	private static Properties serverProps;
	
	/** The current bot. */
	private static String bot;
	
	public AliceServer() {
		
		// Load server properties - must be in root!
		try {
			serverProps = new Properties();
			serverProps.load(new FileInputStream("SERVER.properties"));
			
		} catch (IOException e) {
			// Error loading properties
			System.out.println("Could not find SERVER.properties!");
			System.out.println("Exiting...");
			System.exit(0);
		}
		
		try {
			// What Alicebot should we load?
			bot = serverProps.getProperty("server.engine.bot");
			start();
		} catch (Exception e) {
			System.out.println("*** ERROR STARTING SERVER: " + e + " ***");
		}
		
	}
	
	public static void start() {
		try {
			
			// Start any listeners before we start the Graphmaster ...			
			if (serverProps.getProperty("server.aim.screenname").length() > 0) {
				AliceAIM aim = new AliceAIM(
					serverProps.getProperty("server.aim.owner"),
					serverProps.getProperty("server.aim.screenname"), 
					serverProps.getProperty("server.aim.password"), 
					serverProps.getProperty("server.aim.bgcolor"), 
					serverProps.getProperty("server.aim.fontface"), 
					serverProps.getProperty("server.aim.fontsize"), 
					serverProps.getProperty("server.aim.fontcolor"),
					serverProps.getProperty("server.aim.buddies"),
					serverProps.getProperty("server.aim.message")
					);
			}
			
			if (serverProps.getProperty("server.irc.host").length() > 0) {
				AliceIRC irc = new AliceIRC(
					serverProps.getProperty("server.irc.host"), 
					serverProps.getProperty("server.irc.port"), 
					serverProps.getProperty("server.irc.nick"),
					serverProps.getProperty("server.irc.channel")
					);
			}
			
			if (serverProps.getProperty("server.icq.number").length() > 0) {
				AliceICQ icq = new AliceICQ(
					serverProps.getProperty("server.icq.number"), 
					serverProps.getProperty("server.icq.password")
					);
			}
			
			if (serverProps.getProperty("server.jabber.host").length() > 0) {
				AliceJAB jab = new AliceJAB(
					serverProps.getProperty("server.jabber.host"), 
					serverProps.getProperty("server.jabber.port"), 
					serverProps.getProperty("server.jabber.jid"), 
					serverProps.getProperty("server.jabber.password")
					);
			}
			
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		
		try {
			// Load globals 
			Globals.fromFile(bot);
			
			// Load Classifier (include serverProps)
			Classifier.load(serverProps);
			
			// The almighty Graphmaster (main core)
			graphmaster = new Graphmaster();
			graphmaster.setPriority(Thread.MAX_PRIORITY-1);
			graphmaster.start();
			
			// The main Alicebot server (Jetty)
			Server server = new Server("conf/SERVER.xml");
			server.start();

/*Remove 4.0.3 b1 PEC 09-2001
			// Should we also run an admin server?
			if (serverProps.getProperty("server.engine.admin").equals("true")) {
				HttpServer admin = new HttpServer();
				HashUserRealm realm= new HashUserRealm("Alicebot AliceServer", "users/USERS.properties");
				admin.addRealm(realm);
				SocketListener listener = (SocketListener)admin.addListener(new org.alicebot.server.net.http.util.InetAddrPort("2002"));
				listener.setMaxIdleTimeMs(60000);
				listener.setMaxReadTimeMs(60000);
				HandlerContext context=admin.addContext(null,"/");
				context.setRealm("Alicebot AliceServer");
				context.addAuthConstraint("/","server-administrator");
				context.addServlet("Admin","/","org.alicebot.server.net.servlet.Admin");
				admin.start();
			}

*/
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void main(String[] args)
	{
		new AliceServer();
	}
	
}
