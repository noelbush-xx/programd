package org.alicebot.server.core;

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
import org.alicebot.server.core.util.*;

/**
 * The Classifier works with an associated database for performing property associations to an identifiable user.
 *
 * @author Richard Wallace, Jon Baer
 * @author Thomas Ringate/Pedro Colla
 * @version 4.1.1
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

        ////////////////////////// INITIAL TOPIC CLEAN UP /////////////////////
        public static synchronized void cleanValue(String property,String value) {

		value = value.trim();
		// Check to see if we need to Normalize anything (probably need utility method for this)
		if (property.indexOf("name") > 0 || property.indexOf("date") > 0) { 
			value = Substituter.capitalizeWords(value); 
		}
		
		value = URLEncoder.encode(value.trim());
		try {
			DbAccess dba = mgr.takeDbaRef();
                        ResultSet rs = dba.executeQuery("select value from properties where bot = '" + Globals.getBotName() + "' and property = '" + property + "'");
			int count = 0;
			while (rs.next()) {
				count++;
			}
			if (count > 0) {
                                dba.executeUpdate("update properties set value = '" + value + "' where property = '" + property + "'");
			}
			rs.close();
			mgr.returnDbaRef(dba);
		}
		catch (Exception e) {
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
                boolean varfound = false;
		try {
			DbAccess dba = mgr.takeDbaRef();
			ResultSet rs = dba.executeQuery("select value from properties where ip = '" + ip + "' and bot = '" + Globals.getBotName() + "' and property = '" + property + "'");
			int returnCount = 0;
			while (rs.next()) {
                                varfound = true;
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
                        result = Globals.EmptyDefault;
                        return result; //4.1.2 b2
		}
                if (varfound == false) {  //4.1.2 b2
                   result = Globals.EmptyDefault;
                   return result;
                }

		return result.trim();
	}
	
        /*-----------------------------------------------------------------
          Stack Variable Management Methods (4.1.1 b0 PEC 09-2001)
          Patch to support the new group of index based AIML 1.0 tags
          such as <index/>
          The arrangement is crude at the very least.
          Variables are stored into the DB as normal unit variables
          where the name get appended with the index
             (i.e. input[6] == input6)
          All variables are generated at the first push operation into
          the maximum depth allowed.
        -----------------------------------------------------------------*/

        /*
          getValueIndex
          Retrieve the value of an index variable provided the context used
          to store it (ip), the variable name (varname) and the index.
          Whenever the arguments doesn't support the validation the value
          returned is empty.
        */
          public static String getValueIndex (String varname, int index, String ip) {

            /*
              Verify the arguments, mostly a performance step rather than
              a consistency check.
            */

            if (varname.equals("")) {
               return "";
            }

            if (index <= 0) {
               return "";
            }

            if (index > Globals.MAX_INDEX_DEPTH) {
               return "";
            }

            /*
             Return the value if present
            */

            return getValue(varname+Integer.toString(index),ip);
          }

          /*
            setValueIndex
            Set the value of a particular instance of an index variable,
            proper controls are made not to mess-up with the ordered
            formation of a stack
          */
          public static String setValueIndex (String varname, int index, String ip, String varvalue) {

            /*
              Verify the arguments, mostly a performance step rather than
              a consistency check.
            */

            if (varname.equals("")) {
               return "";
            }

            if (index <= 0) {
               return "";
            }

            if (index > Globals.MAX_INDEX_DEPTH) {
               return "";
            }

            /*
              Everything is ok, so go set it
            */

            setValue(varname+Integer.toString(index),ip,varvalue);
            return varvalue;

          }

          /*
            pushValueIndex
            This method pushes a new value to the top of the stack of
            indexes preserving the integrity of the chain.
            The farthest value is just dropped when it exceeds the
            horizon given by MAX_INDEX_DEPTH.
            Proper shift of the stack [1..N-1] -> [2..N] is performed
            as part of the operation.
          */
          public static String pushValueIndex (String varname, String ip, String varvalue) {

            //System.out.println("*** INDEX ENTRY: var("+varname+") ip("+ip+") value("+varvalue+") ***");

            /*
              Verify the arguments, mostly a performance step rather than
              a consistency check.
            */

            if (varname.equals("")) {
               return "";
            }

            /*
             if other than the first element of the stack move all
             the elements one level deeper
            */

            int x = Globals.MAX_INDEX_DEPTH;
            while (x > 1) {
              setValueIndex(varname,x,ip,getValueIndex(varname,x-1,ip));
              x--;
            }

            /*
             Set the last value pushed at the first level
            */
            setValueIndex(varname,1,ip,varvalue);
            return varvalue;

          }

        /* ---------------------------------------------------------------- */
        /**
          doRespond
          This method obtains the reply to a given input, also take care
          of using the proper context values for THAT/TOPIC
        */
	public static String doRespond(String input, String ip, int depth) {
		
		String response="";

                String nthat  = getValue("that", ip);
		String ntopic = getValue("topic", ip);

                if (nthat.equals("") || nthat.equals(Globals.EmptyDefault) ) nthat = "*";    //4.1.2 b2
                if (ntopic.equals("") || ntopic.equals(Globals.EmptyDefault) ) ntopic = "*"; //4.1.2 b2

                nthat  = Substituter.deperiodize(nthat);
                nthat  = Substituter.normalize(nthat);
		ntopic = Substituter.deperiodize(ntopic);
		ntopic = Substituter.normalize(ntopic);

                try {
                      //System.out.println("*** DORESPOND: Input("+input+") that("+nthat+") topic("+ntopic+") ***");

                      /*
                      Call the Graphmaster pattern matching method to get
                      a matching pattern, recover the template
                      */
                      Graphmaster.MASTER_MATCH = Graphmaster.match(input,nthat,ntopic);
                      response     = (String)Graphmaster.MASTER_MATCH.getTemplate();
                      response = response.trim();
                      if (Globals.showConsole()) {
                         System.out.println(depth+". "+ Graphmaster.MASTER_MATCH.inputPattern+" : "+ Graphmaster.MASTER_MATCH.thatPattern+" : "+ Graphmaster.MASTER_MATCH.topicPattern+" ["+ (String)Graphmaster.MASTER_MATCH.node.get(Graphmaster.FILENAME)+"]");
                      }
                  }   catch (NullPointerException e) {

                        System.out.println("*** NO PATTERN MATCH INPUT("+input+") ***");
                        response = "";
                        return response;

                  }   catch (Exception e) {
			System.out.println(e);
                        response = "";
                        return response;
                  }

                /*
                 Retrieve multistar structures produced by the match
                */
                if (Graphmaster.MASTER_MATCH == null) {
                   return "";
                }

                Graphmaster.INPUT_STAR = Graphmaster.MASTER_MATCH.getInputStars();
                Graphmaster.THAT_STAR  = Graphmaster.MASTER_MATCH.getThatStars();
                Graphmaster.TOPIC_STAR = Graphmaster.MASTER_MATCH.getTopicStars();

                AIMLParser p   = new AIMLParser(depth+1);
                /*
                 This will make the template parser work with a local
                 copy of the star vectors structure in order to support
                 recursion and to ensure a single extraction since the
                 getInputStars/getThatStars/getTopicStars methods are
                 destructive (could be called once).
                */
                p.INPUT_STAR   = Graphmaster.INPUT_STAR;
                p.THAT_STAR    = Graphmaster.THAT_STAR;
                p.TOPIC_STAR   = Graphmaster.TOPIC_STAR;

                response       = p.processResponse(ip, response);
                response       = Substituter.capitalize(response);
		return response;
	} 
        /* ---------------------------------------------------------------- */
        /**
          doResponse
          This method prepares the user input with a normalization and
          deperioditization process and then seek the response of the
          bot to that input.
          Both the input and the reply are stored in a stack for later
          usage
        */
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

                                pushValueIndex("input",ip,sentence); //4.1.1 b1
				reply = doRespond(norm, ip, 1);
                                if ( (!reply.trim().equals("")) || (!norm.trim().equals("")) ) { //4.1.2 b6 Log inputs even if there is no output
                                   robot.log(norm, reply, ip);
                                }
                                String botreply = Substituter.deperiodize(reply); //Add 4.0.3 b7 09-2001 PEC

                                setValue("that",ip, botreply);

                                pushValueIndex("that",ip,botreply); //4.1.1 b1
				if (i > 0) reply = " " + reply;

                                reply    = Toolkit.filterLF(Toolkit.filterSpaces(reply));    //4.1.1 b15
                                response = Toolkit.filterLF(Toolkit.filterSpaces(response)); //4.1.1 b15
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
