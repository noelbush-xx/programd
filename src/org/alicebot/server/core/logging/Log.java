package org.alicebot.server.core.logging;

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
	
	public static final String ACCESS = "./logs/access.log";
	public static final String CHAT = "./logs/chat.log";
	public static final String DATABASE = "./logs/database.log";
	public static final String DEBUG = "./logs/debug.log";
	public static final String ERROR = "./logs/error.log";
	public static final String EVENT = "./logs/event.log";
	public static final String GOSSIP = "./logs/gossip.log";
	public static final String INTERPRETER = "./logs/interpreter.log";
	public static final String JASPER = "./logs/jasper.log";
	public static final String LEARN = "./logs/learn.log";
	public static final String MEMORY = "./logs/memory.log";
	public static final String NETWORK = "./logs/network.log";
	public static final String SERVLET = "./logs/servlet.log";
	public static final String SPEECH = "./logs/speech.log";
	public static final String STARTUP = "./logs/startup.log";
	public static final String SYSTEM = "./logs/system.log";
		
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

