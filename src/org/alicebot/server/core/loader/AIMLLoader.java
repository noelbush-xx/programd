package org.alicebot.server.core.loader;

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

import org.alicebot.server.core.*;
import org.alicebot.server.core.logging.*;
import org.alicebot.server.core.node.*;
import org.alicebot.server.core.parser.*;
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

