package org.alicebot.server.net;

/**
Alice Program D
Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
USA.

@author  Richard Wallace
@author  Jon Baer
@author  Thomas Ringate/Pedro Colla
@version 4.1.1
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

                        System.out.println("Starting AliceBot Server Version "+graphmaster.VERSION);

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
