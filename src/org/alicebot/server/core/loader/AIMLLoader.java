package org.alicebot.server.core.loader;

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
import org.alicebot.server.core.node.*;
import org.alicebot.server.core.parser.*;
import org.alicebot.server.core.processor.*;
import org.alicebot.server.core.util.*;

import java.io.*;
import java.util.*;

/** 
 * The AIMLLoader is a utility class used by the Graphmaster to load AIML files. 
 * 
 * @author Richard Wallace, Kris Drent, Jon Baer
 * @version 1.0
 */

public class AIMLLoader implements AIMLReaderListener {
	
	String filename; 
	String policy;
	
	public AIMLLoader(String filename) {
		this.filename = filename;
		if (Globals.getServerProps() != null) {
			this.policy = (String)Globals.getServerProps().get("server.engine.merge");
		} else {
			this.policy = "true";
		}
	}

	/**
	 * A utility method to grab arguements of a tag set.
	 */
	public static String getArg(String argname, String args) {
		int m;
		String argvalue="";
		String argpattern = new String(argname+"=\"");
		m = args.indexOf(argpattern);
		if (m >= 0) { // locate predicate value:
			args = (m+argpattern.length() >= args.length()) ? "" : args.substring(m+argpattern.length(), args.length());
			m = args.indexOf("\"");
			if (m >= 0) {
				argvalue = args.substring(0, m);
			}
		}
		return (argvalue);
	}

	public void newCategory(String pattern, String that, String topic, String template)
	{
		boolean process = true;
		if (pattern == null) pattern = "*";
		if (that == null) that="*";
		if (topic == null) topic="*";
		if (template == null) template = "*";
		pattern = pattern.trim();
		topic = topic.trim();
		that = that.trim();
		template = template.trim();
		
		if (Graphmaster.TOTAL_CATEGORIES % 1000 == 0 && Graphmaster.TOTAL_CATEGORIES > 0) {
			if (Globals.showConsole()) System.out.println("*** " + Graphmaster.TOTAL_CATEGORIES+" CATEGORIES LEARNED ***");
		}
		
		while (pattern.indexOf(AIMLTag.NAME_VALUE) >= 0) {
			String botname = Globals.getBotName();
			botname = Substituter.normalize(botname);
			pattern = Substituter.replace("<name/>",botname,pattern);
		}
		
		while (pattern.indexOf(AIMLTag.BOT_NAME) >= 0) {
			String botname = Globals.getBotName();
			botname = Substituter.normalize(botname);
			pattern = Substituter.replace("<bot_name/>",botname,pattern);
		}

//Add 4.0.3 b4 PEC 09-2001 Implementation of <bot/> in Pattern side
                while (pattern.indexOf(AIMLTag.BOT_NEW_OPEN) >= 0) {
                        //System.out.println("*** PATTERN <bot/>: "+pattern+" ***");

			String bPredicate = "";
			String bReplace = "";
                        StringTokenizer st = new StringTokenizer(pattern, " ");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
                                if (token.endsWith("<bot")) {
					bPredicate = (new StringTokenizer(st.nextToken(), "/")).nextToken();
                                        bReplace = "<bot " + bPredicate + "/>";
				}
			}
                        String bargValue = getArg("name",bPredicate);
                        // This is a kludge, call it a "performance" kludge..
                        String solvedtag = Globals.getBotName();
                        if (!bargValue.endsWith("name")) {
                           //System.out.println("*** PROPERTY NAME: ("+bargValue+") ***");
                           solvedtag = Globals.getValue(bargValue);
                        }
                        pattern      = Substituter.replace(bReplace,solvedtag,pattern);

		}
//End of Add

		while (pattern.indexOf("<li>") >= 0) {
			int start = pattern.indexOf("<li>");
			int end = pattern.indexOf("</li>");
			String mid = pattern.substring(start + AIMLTag.LISTITEM.length(), end).trim();
			newCategory(mid, that, topic, template);
			pattern = Substituter.replace("<li>" + mid + "</li>", "", pattern);
		}

/* Remove 4.0.3 b4 PEC 09-2001

		while (pattern.indexOf("<typeof_") >= 0) {
			
			// First, located the type we are looking for...
			StringTokenizer st = new StringTokenizer(pattern, "_");
			String type = "";
			while (st.hasMoreTokens()) {
				String token = st.nextToken().trim();
				if (token.endsWith("<typeof")) {
					type = (new StringTokenizer(st.nextToken(), "/>")).nextToken();
					// System.out.println("*** TYPEOF: " + type + " ***");
				}
			}
			
			// Get the values from the Graphmaster
			String values = (String)Graphmaster.match("TYPEOF " + type.toUpperCase(), "*", "*").get(Graphmaster.TEMPLATE);
			// System.out.println("*** VALUES: " + values + " ***");
			
			StringTokenizer v = new StringTokenizer(values, ",");
			while (v.hasMoreTokens()) {
				String newPattern = Substituter.replace("<typeof_" + type + "/>", v.nextToken(), pattern);
				if (newPattern.indexOf("<typeof_") >= 0) {
					// Recurse this pattern if we need to
					newCategory(newPattern, that, topic, template);
				} else {
					// Otherwise drop this pattern into the Graphmaster
					newPattern = Substituter.normalize(newPattern);
					// System.out.println("*** ADDING: " + newPattern + " ***");
					Nodemapper node = Graphmaster.add(newPattern, that, topic);
					node.put(Graphmaster.FILENAME, filename);
					node.put(Graphmaster.TEMPLATE, template);
				}
			}
			
			pattern = "*";
			process = false;
			
		}

*/

		if (process) {
			Nodemapper node = Graphmaster.add(pattern, that, topic);
			if (node.get(Graphmaster.TEMPLATE) != null && !policy.equals("true")) {
				if (Globals.showConsole()) {
					try {
					PrintStream ps = new PrintStream(new FileOutputStream("logs/merge.log", true), true);
					System.out.println("*** DUPLICATE CATEGORY: "+pattern+" : "+that+" : "+topic + " : " + this.filename + "(1) CONFLICTS WITH " + node.get(Graphmaster.FILENAME) + "(1) ***");
					ps.println("*** DUPLICATE CATEGORY: "+pattern+" : "+that+" : "+topic + " : " + this.filename + "(1) CONFLICTS WITH " + node.get(Graphmaster.FILENAME) + "(1) ***");
					ps.close();
				} catch (Exception e) {}
				}
			}
			else {
				node.put(Graphmaster.FILENAME,filename);
				node.put(Graphmaster.TEMPLATE,template);
				Graphmaster.TOTAL_CATEGORIES ++;
			}
		}
		
		Toolkit.process_load_tags(filename, template);
		
	} 
	
}

