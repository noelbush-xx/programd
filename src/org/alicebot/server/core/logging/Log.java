package org.alicebot.server.core.logging;

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
@version 4.1.2
*/


import java.io.*;
import java.util.*;

import org.alicebot.server.core.*;

/**
 * A generic logger for Alice events.
 *
 * @author Jon Baer
 * @version 1.0
 */

public class Log {
	
        public static final String ACCESS   = "./logs/access.log";
        public static final String CHAT     = "./logs/chat.log";
	public static final String DATABASE = "./logs/database.log";
        public static final String DEBUG    = "./logs/debug.log";
        public static final String ERROR    = "./logs/error.log";
        public static final String EVENT    = "./logs/event.log";
        public static final String GOSSIP   = "./logs/gossip.log";
	public static final String INTERPRETER = "./logs/interpreter.log";
        public static final String JASPER   = "./logs/jasper.log";
        public static final String LEARN    = "./logs/learn.log";
        public static final String MEMORY   = "./logs/memory.log";
        public static final String NETWORK  = "./logs/network.log";
        public static final String SERVLET  = "./logs/servlet.log";
        public static final String SPEECH   = "./logs/speech.log";
        public static final String STARTUP  = "./logs/startup.log";
        public static final String SYSTEM   = "./logs/system.log";
        public static final String LOGPATH  = "./logs";   //4.1.1 b1 PEC 09-2001
		
	public static FileWriter fw;
	
	public static void log(String log, String type) {
		try {
			fw = new FileWriter(type, true);
			fw.write(log+"\n");
			fw.close();
		} catch (Exception e) {
			if (type != Log.ERROR) {
				Log.log("*** ERROR WRITING TO LOG: " + e.toString() + " ***", Log.ERROR);
			} else {
				System.err.println("*** ERROR WRITING TO LOG: " + e.toString() + " ***");
			}
		}
	}
	
}

