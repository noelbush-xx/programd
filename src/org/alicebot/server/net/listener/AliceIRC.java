package org.alicebot.server.net.listener;

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


import java.io.*;
import java.net.*;
import java.util.*;

import org.alicebot.server.core.*;
import org.alicebot.server.core.responder.*;

/**
 * Alicebot IRC Chat Listener
 *
 * This code is from the sIRC project and was written
 * by Chris Knight http://www.chrisknight.com/sirc/
 * and modified to work with an Alicebot server.
 *
 * @author Chris Knight
 * @author Jon Baer
 * @version 1.0
 */

public class AliceIRC extends Thread
{
	// ------------------------------------------------------------------------
	
	private static final String VERSION = "0.86.0b";
	private static final String VERDATE = "1999.04.07";
	
	// ------------------------------------------------------------------------
	
	private static final boolean DEBUG = false;
	
	// ------------------------------------------------------------------------
	
	private static final int    MAXARGC        = 16;
	
	private static final String SERVERPREFIX   = "** ";
	private static final String SIRCMESSAGE    = "-- ";
	private static final String DEBUGPREFIX    = "!! ";
	private static final String NONE           = "";
	
	private static final byte   NOTCONNECTED   = 0;
	private static final byte   CONNECTING     = 1;
	private static final byte   CONNECTED      = 2;
	private static final byte   DISCONNECTING  = 3;
	
	// ------------------------------------------------------------------------
	
	private byte clientStatus = NOTCONNECTED;
	
	private Socket socket;
	private BufferedReader reader;    // new BufferedReader(new InputStreamReader(socketthing));
	private PrintWriter writer;       // new PrintWriter(sockething);
	private Thread thread;
	
	private String host, nick, channel;
	private int port;
	
	
	// ------------------------------------------------------------------------
	// CONSTRUCTOR: AliceIRC()
	// ------------------------------------------------------------------------  
	
	public AliceIRC(String host, String port, String nick, String channel)
	{
		super();
		this.host = host;
		this.port = Integer.parseInt(port);
		this.nick = nick;
		this.channel = channel;
		processMessageCommandClient("CONNECT", this.host + " " + this.port);
		try {
			Thread.sleep(5000);
		} catch (Exception e) {}
		processMessage("/NICK " + this.nick);
		processMessage("/JOIN " + this.channel);
	}
	
	// ------------------------------------------------------------------------
	// METHOD: getVersion()
	// ------------------------------------------------------------------------  
	
	public String getVersion()
	{
		return VERSION + " " + VERDATE;
	}
	
	
	// ------------------------------------------------------------------------
	// METHOD: connect()
	// ------------------------------------------------------------------------  
	
	private void connect()
	{
		if (clientStatus == NOTCONNECTED)
		{
			clientStatus = CONNECTING;
			System.out.println("*** IRC: " + SIRCMESSAGE + "Contacting " + this.host + ":" + this.port + " ***\n");
			
			try
			{
				socket = new Socket(this.host, this.port);
				System.out.println("*** IRC: " +SIRCMESSAGE + "Connected to " + this.host + " " + this.port + " ***\n");
				
			}
			catch (UnknownHostException e)
			{
				System.out.println("*** IRC: " +SIRCMESSAGE + "Cannot Connect; Unknown server. ***\n");
				clientStatus = NOTCONNECTED;
			}
			catch (IOException e2)
			{
				System.out.println("*** IRC: " +SIRCMESSAGE + "Cannot Connect; The server is down or is not responding. ***\n");
				clientStatus = NOTCONNECTED;
			}
			
			
			if (clientStatus == CONNECTING)    // If we didn'y have any problems connecting
			{
				try
				{
					reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					writer = new PrintWriter(socket.getOutputStream(), true);
					clientStatus = CONNECTED;
				}
				catch (IOException e3)
				{
					System.out.println("*** IRC: " +SIRCMESSAGE + "Cannot Connect; I/O Error. ***\n");
					clientStatus = DISCONNECTING;
					
					try
					{
						socket.close();
					}
					catch (IOException e4)
					{
						// !
					}
					finally
					{
						socket = null;
						clientStatus = NOTCONNECTED;
					}
				}
			}
			
			if (clientStatus == CONNECTED)
			{
				thread = new Thread(this);
				thread.start();  // start the thread to listen
			}
			
		}
		else
		{
			switch(clientStatus)
			{
			case CONNECTED     : System.out.println("*** IRC: " +SIRCMESSAGE + "Cannot Connect; We're already connected. ***\n");
				break;
				
			case CONNECTING    : System.out.println("*** IRC: " +SIRCMESSAGE + "Cannot Connect; We're already connecting. ***\n");
				break;
				
			case DISCONNECTING : System.out.println("*** IRC: " +SIRCMESSAGE + "Cannot Connect; Hold on! We're still trying to disconnect! ***\n");
				break;
				
			default            : System.out.println("*** IRC: " +SIRCMESSAGE + "WARNING: Unknown clientStatusCode: " + clientStatus + " ***\n");
				break;
			}
		}
	}
	
	
	// ------------------------------------------------------------------------
	// METHOD: disconnect()
	// ------------------------------------------------------------------------  
	
	private void disconnect()
	{
		if (clientStatus == CONNECTED)
		{
			clientStatus = DISCONNECTING;
			try
			{
				socket.close();
			}
			catch (IOException e)
			{
				// !
			}
			finally
			{
				reader = null;
				writer = null;
				// socket = null;
				thread.stop();
				thread = null;
				System.out.println("*** IRC: " +SIRCMESSAGE + "Connection Closed. ***\n");
				clientStatus = NOTCONNECTED;
			}
		}
		else
		{
			switch(clientStatus)
			{
			case NOTCONNECTED  : System.out.println("*** IRC: " +SIRCMESSAGE + "Cannot close conenction; We're not connected.***\n");
				break;
				
			case CONNECTING    : System.out.println("*** IRC: " +SIRCMESSAGE + "Cannot close conenction; We're in the process of connecting connecting.***\n");
				break;
				
			case DISCONNECTING : System.out.println("*** IRC: " +SIRCMESSAGE + "Cannot close conenction; Hold on! We're still trying to close the current one!***\n");
				break;
				
			default            : System.out.println("*** IRC: " +SIRCMESSAGE + "WARNING: Unknown clientStatusCode: " + clientStatus + "***\n");
				break;
			}
		}
	}
	
	
	// ------------------------------------------------------------------------
	// METHOD: processMessage()
	// ------------------------------------------------------------------------  
	
	protected void processMessage(String message)
	{
		if (!message.equals(""))             // if the message isn't blank ...
		{
			
			if (message.charAt(0) == '/')      // ... and if it is a command
			{
				String command;
				
				try
				{
					command = message.substring(1, message.indexOf(' '));
				}
				catch (StringIndexOutOfBoundsException e)
				{
					command = message.substring(1);
				}
				
				if (processMessageCommand(command, message)) { }
				else if (processMessageCommandClient(command, message)) { }
				else if (processMessageCommandDebug(command, message) && DEBUG) { }
				else
				{
					sendMessage("*** IRC: " +SIRCMESSAGE,"Unknown Command: " + command + ". ***");
				}
				
				// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
			}
			else                        
			{
				if (clientStatus == CONNECTED)
				{
					if (!channel.equals(""))
					{
						sendMessage(NONE, "[" + nick + "] " + message);
						
						System.out.println("A message from:" + nick + " > " + message);
						
						sendServerMessage("/MSG " + " " + channel + " :" + message);  // send to server here
						
					String bot_response = Classifier.doResponse(message, nick+"_IRC", new TextResponder());
					processMessage("/PRVMSG " + nick + " " + bot_response);
						
					}
				}
			}
		}
	}
	
	
	// ------------------------------------------------------------------------
	// METHOD: processMessageCommand()
	// ------------------------------------------------------------------------  
	
	private boolean processMessageCommand(String command, String message)
	{
		boolean processed = false;
		
		// here I will extract the params this.portion of the message and put it
		// in a string of its own...
		
		String params = message.substring((message.indexOf(' ')+1));
		if (params.equals("/" + command)) { params = ""; }
		
		// now I will go through each command in order and do the good stuff for
		// each one!
		
		if (clientStatus == CONNECTED)
		{
			// the following commands are processed if the client is connected.
			
			if (command.equalsIgnoreCase("AWAY"))  // - - - - - - - - - - - - - - -
			{
				sendServerMessage("AWAY :" + params);
				processed = true;
			}
			else if (command.equalsIgnoreCase("INVITE")) // - - - - - - - - - - - -
			{
				if (!params.equals(""))
				{
					sendServerMessage("INVITE " + params + " " + channel);
				}
				processed = true;
			}
			else if (command.equalsIgnoreCase("KICK"))   // - - - - - - - - - - - -
			{
				if (!params.equals(""))
				{
					int firstindex = params.indexOf(' ');
					int secondindex = params.indexOf(' ', (firstindex+1));
					try
					{
						sendServerMessage("KICK " + channel + " " + params.substring(0, secondindex) + " :" + params.substring((secondindex+1)));
					}
					catch (StringIndexOutOfBoundsException eMSG)
					{
						sendServerMessage("KICK " + channel + " " + params);
					}
				}
				processed = true;
			}
			else if (command.equalsIgnoreCase("LIST"))  // - - - - - -- - - - - - - 
			{
				if (params.equals(""))
				{
					sendServerMessage("LIST " + channel);
				}
				else
				{
					sendServerMessage("LIST " + params);
				}
				processed = true;
			}
			else if (command.equalsIgnoreCase("JOIN"))  // - - - - - -- - - - - - - 
			{
				if (params.equals("")) // check the channel
				{
					if (channel.equals(""))
					{
						sendMessage(SIRCMESSAGE, "You're not in a channel.");
					}
					else
					{
						sendMessage(SIRCMESSAGE, "You're currently in: " + channel + ".");
					}
				}
				else if (channel.equals("")) // join a new channel
				{
					sendServerMessage("JOIN " + params);
				}
				else if(params.equals("0"))  // leave the channel
				{
					sendServerMessage("PART " + channel);
				}
				else                         // change channels
				{
					sendServerMessage("PART " + channel);
					sendServerMessage("JOIN " + params);
				}
				processed = true;
			}
			else if (command.equalsIgnoreCase("MODE")) // - - - - - - - - - - - - -
			{
				if (!params.equals(""))
				{
					sendServerMessage("MODE " + channel + " " + params);
				}
				processed = true;
			}
			else if (command.equalsIgnoreCase("MSG"))  // - - - - - - - - - - - - -
			{
				if (!params.equals(""))
				{
					try
					{
						int paramsindex = params.indexOf(' ');
						sendServerMessage("PRIVMSG " + params.substring(0, paramsindex) + " :" + params.substring((paramsindex+1)));
					}
					catch (StringIndexOutOfBoundsException eMSG)
					{
						sendServerMessage("PRIVMSG " + params);
					}
					sendMessage(NONE, "*" + this.nick + "* " + params);
				}
				processed = true;
			}
			else if (command.equalsIgnoreCase("NAMES"))  // - - - - - - - - - - - - 
			{
				if (params.equals(""))
				{
					sendServerMessage("NAMES " + channel);
				}
				else
				{
					sendServerMessage("NAMES " + params);
				}
				processed = true;
			}
			else if (command.equalsIgnoreCase("NICK")) // - - - - - - - - - - - - -
			{
				if (params.equals(""))
				{
					sendMessage(SIRCMESSAGE, "You're currently known as " + this.nick);
				}
				else
				{
					sendServerMessage("NICK " + params);
				}
				processed = true;
			}
			else if (command.equalsIgnoreCase("QUIT")) // - - - - - - - - - - - - -
			{
				if (params.equals(""))
				{
					sendServerMessage("QUIT");
				}
				else
				{
					sendServerMessage("QUIT :" + params);
				}
				
				channel = "";
				disconnect();
				processed = true;
			}
			else if (command.equalsIgnoreCase("TOPIC"))  // - - - - - - - - - - - - 
			{
				if (channel.equals(""))
				{
					sendMessage(SIRCMESSAGE, "You must be in a channel to set the topic!");
				}
				else
				{
					if (params.equals(""))
					{
						sendServerMessage("TOPIC " + channel);
					}
					else
					{
						sendServerMessage("TOPIC " + channel + " :" + params);
					}
				}
				processed = true;
			}
			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
		}
		else
		{
			// The following commands are processed if we're not connected...
			
			if (command.equalsIgnoreCase("NICK"))  // - - - - - - - - - - - - - - - 
			{
				if (params.equals(""))
				{
					sendMessage(SIRCMESSAGE, "You're currently known as " + this.nick);
				}
				else
				{
					this.nick = params;
					sendMessage(SIRCMESSAGE, "You're now known as " + this.nick);
				}
				processed = true;
			}
			// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
		}
		return processed;
	}
	
	
	// ------------------------------------------------------------------------
	// METHOD: processMessageCommandClient()
	// ------------------------------------------------------------------------  
	
	private boolean processMessageCommandClient(String command, String message)
	{
		boolean processed = false;
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
		
		int argc = -1;
		int index[] = new int[(MAXARGC + 1)];
		
		do
		{
			argc++;
			if (argc == 0) { index[argc] = message.indexOf(" ");                      }
			else           { index[argc] = message.indexOf(" ", (index[argc-1] + 1)); }
		}
		while (index[argc] != -1);
		
		// note: we have one extra in index[] at this point (a '-1')
		
		String args[] = new String[MAXARGC];
		
		for (int x=0; x < argc; x++)
		{
			if ((x+1) >= argc)
			{
				args[x] = message.substring(index[x]+1);
			}
			else
			{
				args[x] = message.substring(index[x]+1, index[x+1]);
			}
		}
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
		
		if (command.equalsIgnoreCase("SERVER") || command.equalsIgnoreCase("CONNECT"))
		{
			if (this.nick.equals(""))
			{
				sendMessage(SIRCMESSAGE, "You cannot connect to a server unless your NICK is set.");
			}
			else if (argc == 0)
			{
				sendMessage(SIRCMESSAGE, "Please specify a server to connect to.");
			}
			else if (argc == 1)
			{
				connect();
				
				if(clientStatus == CONNECTED)
				{
					sendServerMessage("USER " + this.nick + " " + socket.getInetAddress().getHostName() + " server :" + this.nick);
					sendServerMessage("NICK " + this.nick);
				}
			}
			else
			{
				try
				{
					connect();
					
					if(clientStatus == CONNECTED)
					{
						sendServerMessage("USER " + this.nick + " " + socket.getInetAddress().getHostName() + " server :" + this.nick);
						sendServerMessage("NICK " + this.nick);
					}
				}
				catch(NumberFormatException e)
				{
					sendMessage(SIRCMESSAGE, "The Port you specified is invalid.");
				}
			}
			processed = true;
		}
		else if (command.equalsIgnoreCase("EXIT"))
		{
			if (clientStatus == CONNECTED) { disconnect(); } 
			// messagerelay.exit();
			processed = true;
		}
		else if (command.equalsIgnoreCase("COMMANDS") || command.equalsIgnoreCase("HELP"))
		{
			sendMessage(NONE, "");
			sendMessage(SIRCMESSAGE, "sIRC Commands:");
			sendMessage(SIRCMESSAGE, "  /away [<message>]");
			sendMessage(SIRCMESSAGE, "  /commands");
			sendMessage(SIRCMESSAGE, "  /connect <server> [<this.port>]");
			sendMessage(SIRCMESSAGE, "  /exit");
			sendMessage(SIRCMESSAGE, "  /invite <this.nickname>");
			sendMessage(SIRCMESSAGE, "  /join [<channel> | \'0\']");
			sendMessage(SIRCMESSAGE, "  /kick <this.nickname> [<message>]");
			sendMessage(SIRCMESSAGE, "  /list [<channel>]");
			sendMessage(SIRCMESSAGE, "  /mode <(+|-)mode> [<this.nickname>]");
			sendMessage(SIRCMESSAGE, "  /msg <this.nickname> <message>");
			sendMessage(SIRCMESSAGE, "  /names [<channel>]");
			sendMessage(SIRCMESSAGE, "  /this.nick [<this.nickname>]");
			sendMessage(SIRCMESSAGE, "  /quit [<message>]");
			sendMessage(SIRCMESSAGE, "  /server <server> [<this.port>]");
			sendMessage(SIRCMESSAGE, "  /topic [<topic>]");
			sendMessage(SIRCMESSAGE, "  /version");
			sendMessage(NONE, "");
			processed = true;
		}
		else if (command.equalsIgnoreCase("VERSION"))  
		{
			/*
			String[] versions = // messagerelay.getClassVersions();
			sendMessage(NONE, "");
			sendMessage(SIRCMESSAGE, "sIRC Version: " + versions[0]);
			sendMessage(SIRCMESSAGE, "UserInterface: " + versions[1]);
			sendMessage(SIRCMESSAGE, "AliceIRC: " + versions[2]);
			sendMessage(NONE, "");
			processed = true;
			*/
		}
		
		return processed;
	}
	
	
	// ------------------------------------------------------------------------
	// METHOD: processMessageCommandDebug()
	// ------------------------------------------------------------------------  
	
	private boolean processMessageCommandDebug(String command, String message)
	{
		boolean processed = false;
		
		int argc = -1;
		int index[] = new int[(MAXARGC + 1)];
		
		do
		{
			argc++;
			if (argc == 0) { index[argc] = message.indexOf(" ");                      }
			else           { index[argc] = message.indexOf(" ", (index[argc-1] + 1)); }
		}
		while (index[argc] != -1);
		
		String args[] = new String[MAXARGC];
		
		for (int x=0; x < argc; x++)
		{
			if ((x+1) >= argc)
			{
				args[x] = message.substring(index[x]+1);
			}
			else
			{
				args[x] = message.substring(index[x]+1, index[x+1]);
			}
		}
		
		// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
		
		if (command.equals("testargs"))  
		{
			StringBuffer teststring = new StringBuffer("Test Arguments; argc=" + argc + " command=" + command);
			
			for (int x = 0; x < argc; x++)
			{
				teststring.append(" [" + (x+1) + ";" + args[x] + "]");
			}
			
			System.out.println(SIRCMESSAGE + teststring + "\n");
			processed = true;
		}
		else if (command.equals("debug"))
		{
			System.out.println(SIRCMESSAGE + "- - - - - - - - - - - - - - - - - - - - \n");
			System.out.println(SIRCMESSAGE + "clientStatus=" + clientStatus + "\n");
			System.out.println(SIRCMESSAGE + "socket=" + socket + "\n");
			System.out.println(SIRCMESSAGE + "reader=" + reader + "\n");
			System.out.println(SIRCMESSAGE + "writer=" + writer + "\n");
			System.out.println(SIRCMESSAGE + "thread=" + thread + "\n");
			if (thread != null) { 
				System.out.println(SIRCMESSAGE + "thread.isAlive()=" + thread.isAlive() + "\n"); }
			System.out.println(SIRCMESSAGE + "- - - - - - - - - - - - - - - - - - - - \n");
			processed = true;
		}
		else if (command.equals("raw"))    
		{
			String messagex = "";
			
			for(int x = 0; x < argc; x++)
			{
				messagex = messagex + args[x] + " ";
			}
			
			sendServerMessage(messagex);        
			processed = true;
		}
		
		return processed;
		
	}
	
	private void processServerMessage(String message)
	{
		if (message == null)
		{
			clientStatus = DISCONNECTING;
			reader = null;
			writer = null;
			// socket = null;
			// thread.stop();
			thread = null;
			System.out.println(SIRCMESSAGE + "Connection Closed by Server.\n");
			clientStatus = NOTCONNECTED;
			channel="";
		}
		else if (!message.equals(""))
		{
			if (DEBUG)
			{
				System.out.println(message);
			}
			
			processServerMessage2(message);
		}
		
	}
	
	
	// ------------------------------------------------------------------------
	// METHOD: processServerMessage2()
	// ------------------------------------------------------------------------  
	
	private void processServerMessage2(String message)
	{
		String prefix = "";
		String command = "";
		String params = "";
		String targetnick = "";
		
		if(message.charAt(0) == ':')  // We have a Prefix
		{
			int firstspace = message.indexOf(' ');
			int secondspace = message.indexOf(' ', (firstspace + 1));
			prefix = message.substring(0, firstspace); 
			command = message.substring((firstspace+1), secondspace);
			params = message.substring((secondspace+1));
		}
		else                          // We've got no Prefix
		{
			int firstspace = message.indexOf(' ');
			command = message.substring(0, firstspace);
			params = message.substring((firstspace+1));
		}
		
		if(!prefix.equals(""))
		{
			try
			{
				targetnick = prefix.substring(1, prefix.indexOf('!'));
			}
			catch (StringIndexOutOfBoundsException e)
			{
				targetnick = prefix.substring(1);
			}
		}
		
		try
		{
			switch(Integer.parseInt(command))
			{
			case 001 : // - - - - - - - - - - - - - - - - - - - - - - - - - - - -
				break;
				
			case 321 : // - - - - - - - - - - - - - - - - - - - - - - - - - - - -
				break;
				
			case 322 : // - - - - - - - - - - - - - - - - - - - - - - - - - - - -
				{
					int firstSpaceIndex = params.indexOf(' ');
					int secondSpaceIndex = params.indexOf(' ', (firstSpaceIndex+1));
					int colonIndex = params.indexOf(':');
					sendMessage(SERVERPREFIX, params.substring((firstSpaceIndex+1), secondSpaceIndex) + ": " + params.substring((secondSpaceIndex+1), (colonIndex-1)) + " " + params.substring((colonIndex+1)));
				}
				break;
				
			case 353 : // - - - - - - - - - - - - - - - - - - - - - - - - - - - -
				{
					int colonIndex = params.indexOf(':');
					int equalsIndex = params.indexOf('=');
					sendMessage(SERVERPREFIX, "Users on " + params.substring((equalsIndex+2), (colonIndex-1)) + ": " + params.substring((colonIndex+1)));
				}
				break;
				
			case 372 : // - - - - - - - - - - - - - - - - - - - - - - - - - - - -
				sendMessage(SERVERPREFIX, params.substring((params.indexOf(':')+1)));
				break;
				
			default  : // - - - - - - - - - - - - - - - - - - - - - - - - - - - -
				sendMessage(SERVERPREFIX, "(" + command + ") " + params.substring((params.indexOf(':')+1)));
				break;
			}
		}
		catch(NumberFormatException e)
		{
			if (command.equals("INVITE"))  // - - - - - - - - - - - - - - - - - - -
			{
				int firstindex = params.indexOf(' ');
				sendMessage(SERVERPREFIX, targetnick + " has invited you to " + helpExtractIRCString(params.substring((firstindex+1))) + ".");
			}
			else if (command.equals("JOIN"))   // - - - - - - - - - - - - - - - - -
			{
				String channelx = helpExtractIRCString(params);
				
				if (targetnick.equals(this.nick))
				{
					sendMessage(SERVERPREFIX, "You're now on " + channelx + ".");
					channel = channelx;
				}
				else
				{
					sendMessage(SERVERPREFIX, targetnick + " has joined the channel.");
				}
			}
			else if(command.equals("KICK"))  // - - - - - - - - - - - - - - - - - - 
			{
				String kickchannel = "";
				String kickuser = "";
				String kickcomment = "";
				
				int firstindex = params.indexOf(' ');
				int secondindex = params.indexOf(' ', (firstindex+1));
				
				try
				{
					kickchannel = params.substring(0, firstindex);
					kickuser = params.substring((firstindex+1), secondindex);
					kickcomment = helpExtractIRCString(params.substring((secondindex+1)));
					
					if (kickuser.equals(this.nick))
					{
						sendMessage(SERVERPREFIX, "You've just been kicked off " + kickchannel + " by " + targetnick + " (" + kickcomment + ").");
						channel = "";
					}
					else
					{
						sendMessage(SERVERPREFIX, kickuser + " has been kicked off " + kickchannel + " by " + targetnick + " (" + kickcomment + ").");
					}
				}
				catch (StringIndexOutOfBoundsException eKICK)
				{
					kickchannel = params.substring(0, firstindex);
					kickuser = params.substring((firstindex+1));
					
					if (kickuser.equals(this.nick))
					{
						sendMessage(SERVERPREFIX, "You've just been kicked off " + kickchannel + " by " + targetnick + ".");
						channel = "";
					}
					else
					{
						sendMessage(SERVERPREFIX, targetnick + " has been kicked off " + kickchannel + " by " + targetnick + ".");
					}
				}
			}
			else if(command.equals("NICK"))  // - - - - - - - - - - - - - - - - - - 
			{
				if (targetnick.equals(this.nick))
				{
					String newnick = helpExtractIRCString(params);
					sendMessage(SERVERPREFIX, "You're now known as " + newnick + ".");
					this.nick = newnick;
				}
				else
				{
					sendMessage(SERVERPREFIX, targetnick + " is now known as " + params.substring(1) + ".");
				}
			}
			else if(command.equals("PART"))  // - - - - - - - - - - - - - - - - - - 
			{
				if (targetnick.equals(this.nick))
				{
					sendMessage(SERVERPREFIX, "You've just left " + params + ".");
					channel = "";
				}
				else
				{
					sendMessage(SERVERPREFIX, targetnick + " has left the channel.");
				}
			}
			else if(command.equals("PING"))  // - - - - - - - - - - - - - - - - - - 
			{
				sendServerMessage("PONG " + params);
			}
			else if (command.equals("PRIVMSG"))  // - - - - - - - - - - - - - - - -
			{
				String target = params.substring(0, params.indexOf(' '));
				String gitter = params.substring((params.indexOf(':') + 1));
				
				if (target.equals(this.nick))
				{
					sendMessage(NONE, "*" + targetnick + "* " + gitter);
					System.out.println("*** IRC REQUEST: " +targetnick + " > " + gitter + " ***");
					String bot_response = Classifier.doResponse(gitter, targetnick+"_ICQ", new TextResponder());
					processMessage("/MSG " + targetnick + " " + bot_response);
				}
				else // target equals channel
				{
					sendMessage(NONE, "[" + targetnick + "] " + gitter);
					
				
				}
			}
			else if (command.equals("QUIT"))   // - - - - - - - - - - - - - - - - -
			{
				if (params.equals(""))
				{
					sendMessage(SERVERPREFIX, targetnick + " has quit.");
				}
				else
				{
					sendMessage(SERVERPREFIX, targetnick + " has quit (" + helpExtractIRCString(params) + ").");
				}
			}
			else if (command.equals("TOPIC"))  // - - - - - - - - - - - - - - - - -
			{
				if(targetnick.equals(this.nick))
				{
					sendMessage(SERVERPREFIX, "The topic is now: " + params.substring((params.indexOf(':')+1)));
				}
				else
				{
					sendMessage(SERVERPREFIX, targetnick + " has set the topic to: " + helpExtractIRCString(params.substring((params.indexOf(' ')+1))));
				}
			}
			else // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
			{
			}
		}
	}
	
	// ------------------------------------------------------------------------
	// METHOD: helpExtractIRCString()
	// ------------------------------------------------------------------------  
	
	private String helpExtractIRCString(String string)
	{
		try
		{
			if (string.charAt(0) == ':')
			{
				return string.substring(1);
			}
			else
			{
				return string;
			}
		}
		catch (StringIndexOutOfBoundsException e)
		{
			return "";
		}
	}
	
	
	// ------------------------------------------------------------------------
	// METHOD: sendMessage()
	// ------------------------------------------------------------------------  
	
	private void sendMessage(String type, String message)
	{
		System.out.println("*** IRC: " + type + message + " ***\n");
	}
	
	
	// ------------------------------------------------------------------------
	// METHOD: sendServerMessage()
	// ------------------------------------------------------------------------  
	
	private void sendServerMessage(String message)
	{
		if (clientStatus == CONNECTED)
		{
			writer.println(message);
			if(DEBUG)
			{
				System.out.println(message);
			}
		}
	}
	
	
	// ------------------------------------------------------------------------
	// METHOD: run()
	// ------------------------------------------------------------------------
	
	public void run()
	{
		String line;
		
		while(clientStatus == CONNECTED)
		{
			try
			{
				line = reader.readLine();
				processServerMessage(line);
			}
			catch (IOException e)
			{
				// !
			}
		}
		
	}
	
}


