package org.alicebot.server.core;


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
import java.lang.*;
import java.net.*;
import java.io.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;

import org.alicebot.server.sql.*;
import org.alicebot.server.sql.pool.*;

import org.alicebot.server.core.logging.*;
import org.alicebot.server.core.node.*;
import org.alicebot.server.core.parser.*;
import org.alicebot.server.core.responder.*;
import org.alicebot.server.core.soundex.*;
import org.alicebot.server.core.util.*;

/**
 * The Classifier works with an associated database for performing property associations to an identifiable user.
 *
 * @author Richard Wallace, Jon Baer
 * @version 1.0
 */

public class Classifier implements Serializable {
	
	public static int asize=0;
	public static float tavg=0;
	public static int tcnt=0;
	
	public static Interpreter interpreter;
	
	public static Properties databaseConfig;
	
	public static DbAccessRefsPoolMgr mgr;
	
	public static void load(Properties serverConfig) {
		
		databaseConfig = serverConfig;
		mgr = new DbAccessRefsPoolMgr(
			databaseConfig.getProperty("server.database.driver"),
			databaseConfig.getProperty("server.database.url"),
			databaseConfig.getProperty("server.database.user"),
			databaseConfig.getProperty("server.database.password"));
		mgr.populate(Integer.parseInt(
			databaseConfig.getProperty("server.database.connections"))
			);
		
	}
	
	public static Random RNG = new Random();
	
	public static synchronized String getUserName(String ip) {
		String  result = "";
		try {
			DbAccess dba = mgr.takeDbaRef();
			ResultSet rs = dba.executeQuery("select value from properties where ip = '" + ip + "' and bot = '" + Globals.getBotName() + "' and property = 'username'");
			while (rs.next()) {
				result = rs.getString("value");
			}
			rs.close();
			mgr.returnDbaRef(dba);
		}
		catch (Exception e) {
			org.alicebot.server.core.logging.Log.log("*** DATABASE ERROR: " + e + " ***", org.alicebot.server.core.logging.Log.ERROR);
		}
		return result;
	}
	
	public static synchronized String getUserPass(String ip) {
		String  result = "";
		try {
			DbAccess dba = mgr.takeDbaRef();
			ResultSet rs = dba.executeQuery("select value from properties where ip = '" + ip + "' and bot = '" + Globals.getBotName() + "' and property = 'userpassword'");
			while (rs.next()) {
				result = rs.getString("value");
			}
			rs.close();
			mgr.returnDbaRef(dba);
		}
		catch (Exception e) {
			org.alicebot.server.core.logging.Log.log("*** DATABASE ERROR: " + e + " ***", org.alicebot.server.core.logging.Log.ERROR);
		}
		return result;
	}
/*Remove 4.0.3 b4 PEC 09-2001

	////////////////////////// INCREASE_VARIABLE ///////////////////////////////
	
	public static synchronized void increaseValue(String property, String ip, String value) {
		value = value.trim();
		value = URLEncoder.encode(value.trim());
		org.alicebot.server.core.logging.Log.log("*** DATABASE INCREASE: " + property + " > " + ip + " > " + value + " ***", org.alicebot.server.core.logging.Log.DATABASE);
		try {
			DbAccess dba = mgr.takeDbaRef();
			dba.executeUpdate("insert into properties values ('" + Globals.getBotName() + "','" + ip + "', null, null, null, null, '" + property + "','" + value + "')");
			mgr.returnDbaRef(dba);
		} catch (Exception e) {
			org.alicebot.server.core.logging.Log.log("*** DATABASE ERROR: " + e + " ***", org.alicebot.server.core.logging.Log.ERROR);
		}
		
	}
	
	////////////////////////// DECREASE_VARIABLE ///////////////////////////////
	
	public static synchronized void decreaseValue(String property, String ip, String value) {
		value = value.trim();
		value = URLEncoder.encode(value.trim());
		org.alicebot.server.core.logging.Log.log("*** DATABASE DECREASE: " + property + " > " + ip + " > " + value + " ***", org.alicebot.server.core.logging.Log.DATABASE);
		try {
			DbAccess dba = mgr.takeDbaRef();
			dba.executeUpdate("insert into properties values ('" + Globals.getBotName() + "','" + ip + "', null, null, null, null, '" + property + "','" + value + "')");
			mgr.returnDbaRef(dba);
		} catch (Exception e) {
			org.alicebot.server.core.logging.Log.log("*** DATABASE ERROR: " + e + " ***", org.alicebot.server.core.logging.Log.ERROR);
		}
		
	}

*/
	////////////////////////// ADD_VARIABLE ///////////////////////////////
	
	public static synchronized void addValue(String property, String ip, String value) {
		value = value.trim();
		value = URLEncoder.encode(value.trim());
		org.alicebot.server.core.logging.Log.log("*** DATABASE ADD: " + property + " > " + ip + " > " + value + " ***", org.alicebot.server.core.logging.Log.DATABASE);
		try {
			DbAccess dba = mgr.takeDbaRef();
			dba.executeUpdate("insert into properties values ('" + Globals.getBotName() + "','" + ip + "', null, null, null, null, '" + property + "','" + value + "')");
			mgr.returnDbaRef(dba);
		} catch (Exception e) {
			org.alicebot.server.core.logging.Log.log("*** DATABASE ERROR: " + e + " ***", org.alicebot.server.core.logging.Log.ERROR);
		}
		
	}
	
	////////////////////////// DELETE_VARIABLE ///////////////////////////////
	
	public static synchronized void deleteValue(String property, String ip, String value) {
		value = value.trim();
		value = URLEncoder.encode(value.trim());
		org.alicebot.server.core.logging.Log.log("*** DATABASE ADD: " + property + " > " + ip + " > " + value + " ***", org.alicebot.server.core.logging.Log.DATABASE);
		try {
			DbAccess dba = mgr.takeDbaRef();
			dba.executeUpdate("insert into properties values ('" + Globals.getBotName() + "','" + ip + "', null, null, null, null, '" + property + "','" + value + "')");
			mgr.returnDbaRef(dba);
		} catch (Exception e) {
			org.alicebot.server.core.logging.Log.log("*** DATABASE ERROR: " + e + " ***", org.alicebot.server.core.logging.Log.ERROR);
		}
		
	}
	
	////////////////////////// REMOVE_VARIABLE ///////////////////////////////
	
	public static synchronized void removeValue(String property, String ip, String value) {
		value = value.trim();
		value = URLEncoder.encode(value.trim());
		org.alicebot.server.core.logging.Log.log("*** DATABASE ADD: " + property + " > " + ip + " > " + value + " ***", org.alicebot.server.core.logging.Log.DATABASE);
		try {
			DbAccess dba = mgr.takeDbaRef();
			dba.executeUpdate("insert into properties values ('" + Globals.getBotName() + "','" + ip + "', null, '" + property + "','" + value + "')");
			mgr.returnDbaRef(dba);
		} catch (Exception e) {
			org.alicebot.server.core.logging.Log.log("*** DATABASE ERROR: " + e + " ***", org.alicebot.server.core.logging.Log.ERROR);
		}
		
	}
	
	////////////////////////// SET_VARIABLE ///////////////////////////////
	
	public static synchronized void setValue(String property, String ip, String value) {
		value = value.trim();
		ip = ip.trim().toLowerCase();
		// Check to see if we need to Normalize anything (probably need utility method for this)
		if (property.indexOf("name") > 0 || property.indexOf("date") > 0) { 
			value = Substituter.capitalizeWords(value); 
		}
		
		if (property.equals("that")) {
			String that = value;
			value = Substituter.suppress_html(value);
			value = Substituter.deperiodize(value);
			StringTokenizer qt = new StringTokenizer(value, ".!?");
			int ct = qt.countTokens();
			for (int i = 0; i < ct; i++) {
				String sentence = qt.nextToken();
				sentence = sentence.trim();
				if (sentence.length() > 0) that = sentence;
			}
			if (that.length() > 1024) that = that.substring(0,1023);
			if (that.length() <= 0) that = "that";
			value = that;
			
		}
		
		value = URLEncoder.encode(value.trim());
		if (value.indexOf("+my+") > 0 || value.startsWith("my+")) value = Substituter.replace("my+", "your+", value);
		try {
			DbAccess dba = mgr.takeDbaRef();
			ResultSet rs = dba.executeQuery("select value from properties where ip = '" + ip + "' and bot = '" + Globals.getBotName() + "' and property = '" + property + "'");
			int count = 0;
			while (rs.next()) {
				count++;
			}
			if (count > 0) {
				dba.executeUpdate("update properties set value = '" + value + "' where ip = '" + ip + "' and property = '" + property + "'");
			} else {
				dba.executeUpdate("insert into properties values ('" + Globals.getBotName() + "','" + ip + "', null, null, null, null, '" + property + "','" + value + "')");
			}
			rs.close();
			mgr.returnDbaRef(dba);
		}
		catch (Exception e) {
			org.alicebot.server.core.logging.Log.log("*** DATABASE ERROR: " + e + " ***", org.alicebot.server.core.logging.Log.ERROR);
		}
	}
	
	////////////////////////// GET_VARIABLE ///////////////////////////////
	
	public static String getValue(String property, String ip) {
		return getValue(property, ip, "and");
	}
	
	public static synchronized String getValue(String property, String ip, String conjunction) {
		String result = "";
		ip = ip.trim().toLowerCase();
		try {
			DbAccess dba = mgr.takeDbaRef();
			ResultSet rs = dba.executeQuery("select value from properties where ip = '" + ip + "' and bot = '" + Globals.getBotName() + "' and property = '" + property + "'");
			int returnCount = 0;
			while (rs.next()) {
				returnCount++;
				if (returnCount > 1) result = result + " " + conjunction + " "; 
				result = result + rs.getString("value");
				result = result.trim();
				result = URLDecoder.decode(result.trim());
			}
			rs.close();
			mgr.returnDbaRef(dba);
		}
		catch (Exception e) {
			org.alicebot.server.core.logging.Log.log("*** DATABASE ERROR: " + e + " ***", org.alicebot.server.core.logging.Log.ERROR);
		}
		return result.trim();
	}
	
	////////////////////////// GET_VARIABLE ///////////////////////////////
	
	public static String getRandomValue(String property, String ip) {
		String result = "";
		Vector results = new Vector();
		try {
			DbAccess dba = mgr.takeDbaRef();
			ResultSet rs = dba.executeQuery("select value from properties where ip = '" + ip + "' and bot = '" + Globals.getBotName() + "' and property = '" + property + "'");
			int returnCount = 0;
			while (rs.next()) {
				returnCount++;
				result = result + rs.getString("value");
				result = result.trim();
				result = URLDecoder.decode(result.trim());
				results.add(result);
			}
			rs.close();
			mgr.returnDbaRef(dba);
			double r = Classifier.RNG.nextDouble();
			int random_amt = (int)((double)(results.size())*r);        
			result = (String)(results.elementAt(random_amt));
		}
		catch (Exception e) {
			org.alicebot.server.core.logging.Log.log("*** DATABASE ERROR: " + e + " ***", org.alicebot.server.core.logging.Log.ERROR);
		}
		return result;
	}
	
	////////////////////////// DO RESPOND ///////////////////////////////
	
	public static String doRespond(String input, String ip, int depth) {
		
		String response="";
		String nthat = getValue("that", ip);
		String ntopic = getValue("topic", ip);
		if (nthat.equals("")) nthat = "*";
		if (ntopic.equals("")) ntopic = "*";
		nthat = Substituter.deperiodize(nthat);
		nthat = Substituter.normalize(nthat);
		ntopic = Substituter.deperiodize(ntopic);
		ntopic = Substituter.normalize(ntopic);
		
		try {
			Nodemapper rec = Graphmaster.match(input, nthat, ntopic);
			response = (String)rec.get(Graphmaster.TEMPLATE);
			response = response.trim();
			if (Globals.showConsole()) System.out.println(depth+". "+ Graphmaster.INPUT_PATTERN+" : "+ Graphmaster.THAT_PATTERN+" : "+ Graphmaster.TOPIC_PATTERN+" star="+ Graphmaster.INPUT_STAR+" ["+ (String)rec.get(Graphmaster.FILENAME)+"]");
		}   catch (NullPointerException e) {
			response = "<srai>NOT FOUND</srai>";
		}   catch (Exception e) {
			System.out.println(e);
		}
		
		AIMLParser p = new AIMLParser(depth+1);
		response = p.processResponse(ip, response);
		response = Substituter.capitalize(response);

/*Remove 4.0.3 b4 PEC 09-2001

		// Experimental replacement methods
		while (response.indexOf(" my ") > 0 && input.indexOf(" MY ") > 0) {
			response = Substituter.replace(" my ", " your ", response);
		}

*/
		return response;
	} 
	
	////////////////////////// DO RESPONSE ///////////////////////////////
	
	public static synchronized String doResponse(String input, String ip, Responder robot) {
		input = robot.pre_process(input, ip);
		input = Substituter.deperiodize(input);
		StringTokenizer st = new StringTokenizer(input, "?!.");
		int ct = st.countTokens();
		String reply="";
		String response="";
		Date T = new Date();
		long t = T.getTime();
		String sentence = "";
		for (int i = 0; i < ct; i++) {
			sentence = st.nextToken();
			sentence = sentence.trim();
			sentence = sentence.replace('*',' ');
			String norm = Substituter.normalize(sentence);
			if (norm.length() > 0) {
				setValue("beforethat", ip, getValue("justthat", ip)); 
				setValue("justthat", ip, getValue("input", ip)); 
				setValue("input", ip, sentence); 
				reply = doRespond(norm, ip, 1);
				robot.log(norm, reply, ip);
//Add 4.0.3 b7 PEC 09-2001 Remove periods from stacked values
                                reply = Substituter.deperiodize(reply);
//End of Add                                
				setValue("justbeforethat", ip, getValue("that", ip));
				setValue("that",ip, reply);
				if (i > 0) reply = " " + reply;
				response = robot.append(sentence, reply, response);
			}
		}
		response = robot.post_process(response);
		T = new Date();
		t = T.getTime()-t;
		tcnt = tcnt+1;
		tavg = (tavg * (tcnt-1) + t)/tcnt;
		if (Globals.showConsole()) System.out.println("Response "+t+"ms ("+tavg+") "+tcnt);
		return (response);   
	}
	
} 
