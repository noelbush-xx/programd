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


/**
 * Alicebot Jabber Chat Listener
 *
 * @author Jon Baer
 * @version 1.0
 */

import java.io.*;

public class AliceJAB extends Thread {
	
	private String server, nickname, password;
	private int port;
	private boolean connected = false;
	private BufferedReader reader;
	
	public AliceJAB(String server, String port, String nickname, String password) {
		this.server = server;
		this.port = Integer.parseInt(port);
		this.nickname = nickname;
		this.password = password;
		start();
	}
	
	public void run()
	{
		String line;
		
		while(connected = true)
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
	
	public void processServerMessage(String input) {
		
	}
	
	public void processClientMessage(String output) {
		
	}

}
