package org.alicebot.server.core.parser;
/**

$Id: AIMLParser.java,v 1.6.2.4 2001/09/13 00:04:57 tringate Exp $

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


import org.alicebot.server.core.*;
import org.alicebot.server.core.logging.*;
import org.alicebot.server.core.processor.*;
import org.alicebot.server.core.util.*;
import org.alicebot.server.core.parser.*;

/**
 * The AIMLParser class interprets the right-hand side template expressions.
 *
 * The primary method of the AIMLParser class is the processResponse() that
 * controls the parsing and interpretation process.
 *
 * IMPORTANT!!! This AIMLParser will be superceded by a new one in the future.
 * It is provided as a platform testing parser to finalize and debug certain
 * tag sets.  Please do not edit it, instead send feature requests to the Alicebot
 * list (alicebot-subscribe@listbot.com) and voice your opinion.
 *
 * @author Richard Wallace
 * @author Kris Drent
 * @author Jon Baer
 * @version 1.0
 */

public class AIMLParser {
	public int length;
	public int depth;

	public Globals globals;
	public Interpreter interpreter;
	public Classifier classifier;
	public Graphmaster graphmaster;
	public String bot;

	/**
	 * The AIML Parser.
	 */
	public AIMLParser()  {
		length = 0;
		depth = 0;
	}

	/**
	 * The AIML Parser (with depth known).
	 */
	public AIMLParser(int depth)  {
		this();
		this.depth = depth;
	}

	/**
	 * AIML Parser (with depth known and a given interpreter).
	 */
	public AIMLParser(int depth, Interpreter interpreter)  {
		this();
		this.depth = depth;
		this.interpreter = interpreter;
	}

	/**
	 * Method to process an AIML tag set.
	 */
	public String processTag(String ip, String stag, String etag, String response, TagProcessor tp) {
		while (response.indexOf(stag) >= 0) {
			int start = response.indexOf(stag);
			int end = indexOfEndTag(stag, etag, response);
			if (end <= start) response = Substituter.replace(stag," ",response);
			else {
				String head = response.substring(0, start);
				String mid = response.substring(start+stag.length(), end);
				String tail =
					(end+etag.length() < response.length()) ? response.substring(end+etag.length()) : "";
				mid = tp.processAIML(ip, mid, this);
				tail = processResponse(ip, tail);
				response = head + mid + tail;
			}
		}
		return response;
	}


	public String processElse(String elseString) {
		if (elseString.indexOf("<else/>") < 0) {
			return "";
		} else {
			int start = elseString.indexOf("<else/>") + 7;
			return elseString.substring(start, elseString.length());
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


	/**
	 * This is either a bug or a feature that restricts the
	 * XML grammar somewhat.  The assumption of
	 * indexOfEndTag is that the "etag" matching "stag" is
	 * the NEXT etag in the input.  This prohibits recursive
	 * constructs like <srai><srai>...</srai></srai>, but the
	 * semantics of those expressions are unclear anyway.
	 * The language DOES permit <srai>X</srai><srai>Y</srai>.
	 */

	public int indexOfEndTag(String stag, String etag, String input) {
		int index = input.indexOf(etag);
		return (index);
	}

//Add 4.0.3 b6 PEC 09-2001
        /**
        This method process <li></li> within a <condition></condition> tag
        */

        public String processLI(String ip, String predname, String response) {

                //System.out.println("*** PROCESSLI ENTRY: (" + response + ") ***");
                boolean hit = false;

                while (response.indexOf(AIMLTag.LISTITEM_OPEN) >= 0) {

                   //System.out.println("*** LISTITEM("+response+") ***");

                   //Extract start and end of the <li> tag
                   int start   = response.indexOf(AIMLTag.LISTITEM_OPEN);
                   int end     = response.indexOf(AIMLTag.LISTITEM_CLOSE,start);
                   String head = response.substring(0, start);
                   String tail = response.substring(end+AIMLTag.LISTITEM_CLOSE.length());
                   //System.out.println("*** LISTITEM HEAD("+head+") ***");
                   //System.out.println("*** LISTITEM TAIL("+tail+") ***");


                   //Extract the opening <li> tag plus attributes
                   int index        = start;
                   String predicate = "";
                   while ( (!predicate.endsWith(">"))  && (index < end)) {
                      predicate = predicate+(String.valueOf(response.charAt(index)));
                      index++;
                   }

                   //System.out.println("*** LISTITEM("+predicate+") ***");

                   //Extract content between open and close <li> tags
                   String mid = "";
                   index      = start+predicate.length();
                   while (index < end) {
                      mid = mid+(String.valueOf(response.charAt(index)));
                      index++;
                   }

                   //System.out.println("*** LISTITEM MID: ("+mid+") ***");

                   //Extract arguments from the open <li> tag
                   String args = "";
                   StringTokenizer st = new StringTokenizer(predicate, " ");
                   while (st.hasMoreTokens()) {
                     String token = st.nextToken();
                     if (!token.endsWith("<li")) {
                        if (token.endsWith(">")) {
                           token = token.substring(0,token.length()-1);
                        }
                        if (args.equals("")) {
                           args = token;
                        } else {
                           args = args + " " + token;
                        }
                     }
                   }
                   //System.out.println("*** ARGS: ("+args+") ***");

                   //Now process the content between li tags
                   String predvalue="";

                   predvalue= getArg("value",args);
                   predvalue= Substituter.normalize(predvalue);

                   String sx = "";
                   response  = "";

                   //<li> with value informed
                   if (!predvalue.equals("")) {   

                      sx = Classifier.getValue(predname, ip);
                      //System.out.println("*** LISTITEM PREDICATE VALUE: "+sx+" ***");
                      sx = Substituter.normalize(sx);

                      if (!sx.equals(predvalue)) {
                        response = "";
                      } else {
                        response = mid;
                        hit      = true;
                        System.out.println("*** LISTITEM HIT VALUE: "+sx+" ***");

                      }
                   }

                   //if the evaluation was false response is ""
                   //if the evaluation was true response is the child of <li>
                   response = head + response + tail;
                }

                //All <li> with values processed already, now process the
                //ones without them, only need to process if no hit were
                //found in the previous block, still need to extract it
                //from the string.

                while (response.indexOf(AIMLTag.LISTITEM) >= 0) {

                   //System.out.println("*** LISTITEM("+response+") ***");

                   //Extract start and end of the <li> tag
                   int start   = response.indexOf(AIMLTag.LISTITEM);
                   int end     = response.indexOf(AIMLTag.LISTITEM_CLOSE,start);
                   String head = response.substring(0, start);
                   String tail = response.substring(end+AIMLTag.LISTITEM_CLOSE.length());
                   //System.out.println("*** LISTITEM HEAD("+head+") ***");
                   //System.out.println("*** LISTITEM TAIL("+tail+") ***");

                   //if previous block yield a hit then just nullify
                   if (hit == true) {
                      response = head + tail;
                   } else {

                      //Extract content between open and close <li> tags
                      String mid = "";
                      int index  = start+AIMLTag.LISTITEM.length();
                      while (index < end) {
                         mid = mid+(String.valueOf(response.charAt(index)));
                         index++;
                      }

                      //if previous block didn't yield a hit then return content
                      response = head + mid + tail;
                   }

                }

               return response;
        }
//End of Add

	/**
	 * The method processResponse: the Program Formerly Known as Hello.
	 * This method interprets the AIML template and constucts the reply.
	 * Pfkh is normally called by the Classifier method respond().
	 * If the Template contains an <sr> or <srai> function, then the
	 * processResponse() executes a recursive call back to the Classifier method respond().
	 * Note that respond may create another instance of this class AIMLParser.
	 * These recursions create the possibility of infinite loops, so the
	 * Botmaster should take care not to allow loops in AIML.
	 */

	public String processResponse(String ip, String response) {

		TagProcessor tp;
		String templ = response;
		int n;
		String stag, etag;

                //System.out.println("*** PROCESSRESPONSE ENTRY: (" + response + ") ***");

                while (response.indexOf(AIMLTag.INPUT_VALUE) >= 0) {
                        String sx = Classifier.getValue("input", ip);
                        response = Substituter.replace(AIMLTag.INPUT_VALUE,sx,response);
		}

                while (response.indexOf(AIMLTag.INPUT_OPEN) >= 0) {
                        //System.out.println("*** PROCESSING INPUT: (" + response + ") ***");
			String bPredicate = "";
			String bReplace = "";
                        StringTokenizer st = new StringTokenizer(response, " ");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
                                if (token.endsWith("<input")) {
					bPredicate = (new StringTokenizer(st.nextToken(), "/")).nextToken();
                                        bReplace = "<input " + bPredicate + "/>";
				}
			}
                        String bIndex = getArg("index",bPredicate);
                        String sx     = "";
                        //System.out.println("*** PROCESSING INPUT PREDICATE: (" + bPredicate + ") ***");
                        //System.out.println("*** PROCESSING INPUT INDEX: (" + bIndex + ") ***");
                        if (bIndex.equalsIgnoreCase("1")) {
                           sx = Classifier.getValue("input", ip);
                        } else {
                          if (bIndex.equalsIgnoreCase("2")) {
                             sx = Classifier.getValue("justthat", ip);
                         } else {
                           if (bIndex.equalsIgnoreCase("3")) {
                              sx = Classifier.getValue("beforethat", ip);
                           } else {
                             sx = "";
                           }
                         }
                        }
                        //System.out.println("*** PROCESSING INPUT RESULT: (" + sx + ") ***");
                        response = Substituter.replace(bReplace,sx,response);
		}

//End of Add

		while (response.indexOf(AIMLTag.STAR_VALUE) >= 0) {
			String sx = Substituter.pretty(graphmaster.INPUT_STAR);
			response = Substituter.replace(AIMLTag.STAR_VALUE,sx,response);
		}

//Add 4.0.3 b5 Support for <star index="N"/>
                while (response.indexOf(AIMLTag.STAR_NEW) >= 0) {
                        //System.out.println("*** PROCESSING STAR: (" + response + ") ***");
			String bPredicate = "";
			String bReplace = "";
                        StringTokenizer st = new StringTokenizer(response, " ");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
                                if (token.endsWith("<star")) {
					bPredicate = (new StringTokenizer(st.nextToken(), "/")).nextToken();
                                        bReplace = "<star " + bPredicate + "/>";
				}
			}
                        String bIndex = getArg("index",bPredicate);
                        String sx     = "";
                        //System.out.println("*** PROCESSING STAR PREDICATE: (" + bPredicate + ") ***");
                        //System.out.println("*** PROCESSING STAR INDEX: (" + bIndex + ") ***");
                        if (bIndex.equalsIgnoreCase("1")) {
                           sx = Substituter.pretty(graphmaster.INPUT_STAR);
                        } else {
                           sx = "";
                        }
                        //System.out.println("*** PROCESSING STAR RESULT: (" + sx + ") ***");
                        response = Substituter.replace(bReplace,sx,response);
		}

//End of Add

//Add 4.0.3 b5 Support for <thatstar index="N"/>

                while (response.indexOf(AIMLTag.THATSTAR_NEW) >= 0) {
                        //System.out.println("*** PROCESSING THATSTAR: (" + response + ") ***");
			String bPredicate = "";
			String bReplace = "";
                        StringTokenizer st = new StringTokenizer(response, " ");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
                                if (token.endsWith("<thatstar")) {
					bPredicate = (new StringTokenizer(st.nextToken(), "/")).nextToken();
                                        bReplace = "<thatstar " + bPredicate + "/>";
				}
			}
                        String bIndex = getArg("index",bPredicate);
                        String sx     = "";
                        //System.out.println("*** PROCESSING THATSTAR PREDICATE: (" + bPredicate + ") ***");
                        //System.out.println("*** PROCESSING THATSTAR INDEX: (" + bIndex + ") ***");
                        if (bIndex.equalsIgnoreCase("1")) {
                           sx = Substituter.pretty(graphmaster.THAT_STAR);
                        } else {
                           sx = "";
                        }
                        //System.out.println("*** PROCESSING THATSTAR RESULT: (" + sx + ") ***");
                        response = Substituter.replace(bReplace,sx,response);
		}


//End of Add


//Add 4.0.3 b5 Support for <topicstar index="N"/>

                while (response.indexOf(AIMLTag.TOPICSTAR_NEW) >= 0) {
                        //System.out.println("*** PROCESSING TOPICSTAR: (" + response + ") ***");
			String bPredicate = "";
			String bReplace = "";
                        StringTokenizer st = new StringTokenizer(response, " ");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
                                if (token.endsWith("<topicstar")) {
					bPredicate = (new StringTokenizer(st.nextToken(), "/")).nextToken();
                                        bReplace = "<topicstar " + bPredicate + "/>";
				}
			}
                        String bIndex = getArg("index",bPredicate);
                        String sx     = "";
                        //System.out.println("*** PROCESSING TOPICSTAR PREDICATE: (" + bPredicate + ") ***");
                        //System.out.println("*** PROCESSING TOPICSTAR INDEX: (" + bIndex + ") ***");
                        if (bIndex.equalsIgnoreCase("1")) {
                           sx = Substituter.pretty(graphmaster.TOPIC_STAR);
                        } else {
                           sx = "";
                        }
                        //System.out.println("*** PROCESSING TOPICSTAR RESULT: (" + sx + ") ***");
                        response = Substituter.replace(bReplace,sx,response);
		}


//End of Add

		while (response.indexOf(AIMLTag.THATSTAR_VALUE) >= 0) {
			String sx = Substituter.pretty(graphmaster.THAT_STAR);
			response = Substituter.replace(AIMLTag.THATSTAR_VALUE,sx,response);
		}
		while (response.indexOf(AIMLTag.TOPICSTAR_VALUE) >= 0) {
			String sx = Substituter.pretty(graphmaster.TOPIC_STAR);
			response = Substituter.replace(AIMLTag.TOPICSTAR_VALUE,sx,response);
		}

		while (response.indexOf(AIMLTag.THAT_VALUE) >= 0) {
			String sx = Classifier.getValue("that", ip);
			sx = sx.toLowerCase();
			response = Substituter.replace(AIMLTag.THAT_VALUE,sx,response);
		}

		while (response.indexOf(AIMLTag.JUSTTHAT_VALUE) >= 0) {
			String sx = Classifier.getValue("justthat", ip);
			sx = sx.toLowerCase();
			response = Substituter.replace(AIMLTag.JUSTTHAT_VALUE,sx,response);
		}

		while (response.indexOf(AIMLTag.BEFORETHAT_VALUE) >= 0) {
			String sx = Classifier.getValue("beforethat", ip);
			sx = sx.toLowerCase();
			response = Substituter.replace(AIMLTag.BEFORETHAT_VALUE,sx,response);
		}

//Add 4.0.3 b5 PEC 09-2001 Support for <that index="N"/>
                while (response.indexOf(AIMLTag.THAT_NEW) >= 0) {
                        System.out.println("*** PROCESSING THAT: (" + response + ") ***");
			String bPredicate = "";
			String bReplace = "";
                        StringTokenizer st = new StringTokenizer(response, " ");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
                                if (token.endsWith("<that")) {
					bPredicate = (new StringTokenizer(st.nextToken(), "/")).nextToken();
                                        bReplace = "<that " + bPredicate + "/>";
				}
			}
                        String bIndex = getArg("index",bPredicate);
                        String sx     = "";
                        StringTokenizer ix = new StringTokenizer(bIndex, ",");
                        if (ix.hasMoreTokens()) {
                          bIndex = ix.nextToken();
                        } else {
                          bIndex = "1";
                        }

                        System.out.println("*** PROCESSING THAT PREDICATE: (" + bPredicate + ") ***");
                        System.out.println("*** PROCESSING THAT INDEX: (" + bIndex + ") ***");

                        if (bIndex.equalsIgnoreCase("1")) {
                           sx = Classifier.getValue("that", ip);
                           sx = sx.toLowerCase();  //Fix 4.0.3 b6 THAT Uppercase
                        } else {
                          if (bIndex.equalsIgnoreCase("2")) {
                             sx = Classifier.getValue("justbeforethat", ip);
                             sx = sx.toLowerCase();
                          } else {
                            sx = "";
                          }
                        }
                        System.out.println("*** PROCESSING THAT RESULT: (" + sx + ") ***");
                        response = Substituter.replace(bReplace,sx,response);
		}

//End of Add



		while (response.indexOf(AIMLTag.JUSTBEFORETHAT_VALUE) >= 0) {
			String sx = Classifier.getValue("justbeforethat", ip);
			sx = sx.toLowerCase();
			response = Substituter.replace(AIMLTag.JUSTBEFORETHAT_VALUE,sx,response);
		}


		while (response.indexOf(AIMLTag.IP) >= 0) {
			String sx = ip;
			response = Substituter.replace(AIMLTag.IP,sx,response);
		}

/*Remove 4.0.3 b2 
		while (response.indexOf(AIMLTag.OS) >= 0) {
			String sx = System.getProperty("os.name");
			response = Substituter.replace(AIMLTag.OS,sx,response);
		}
*/

/*Remove 4.0.3 b6 PEC 09-2001

		// Login first if needed
		while (response.indexOf("<login ") >= 0) {
			tp = new LoginProcessor();
			response = tp.processAIML(ip, response, this);
		}

		// Logout first if needed
		while (response.indexOf("<logout ") >= 0) {
			tp = new LogoutProcessor();
			response = tp.processAIML(ip, response, this);
		}

		while (response.indexOf("<register ") >= 0) {
			tp = new RegisterProcessor();
			response = tp.processAIML(ip, response, this);
		}

End of Remove */

		stag = "<ip>"; etag = "</ip>";
		tp = new IPProcessor();
		response = processTag(ip,  stag, etag, response, tp);

		stag = "<connect>"; etag = "</connect>";
		tp = new ConnectProcessor();
		response = processTag(ip,  stag, etag, response, tp);

/*Remove 4.0.3 b2 
		while (response.indexOf(AIMLTag.TYPEOF) >= 0) {
			StringTokenizer typeOf = new StringTokenizer(response, "_");
			String type = "";
			//this loop sets type=xxx from <typeof_xxx/>
			while (typeOf.hasMoreTokens()) {
				String token = typeOf.nextToken();
				if (token.endsWith("<typeof")) {
					type = (new StringTokenizer(typeOf.nextToken(), "/>")).nextToken();
					// System.out.println("*** TYPEOF: " + type + " ***");
				}
			}
			String template = (String)Graphmaster.match("TYPEOF " + type.toUpperCase(), "*", "*").get(Graphmaster.TEMPLATE);
			// System.out.println("*** VALUES: " + template + " ***");
			StringTokenizer values = new StringTokenizer(template, ",");
			boolean found = false;
			while (values.hasMoreTokens()) {
				String value = values.nextToken().toLowerCase().trim();
				String input = Classifier.getValue("input", ip).toLowerCase().trim();
				if (input.indexOf(value) >= 0) {
					// System.out.println("*** RESPONSE: " + response + " ***");
					// System.out.println("*** COMPARING: " + value + " ***");
					response = Substituter.replace("<typeof_" + type + "/>", value, response);
					found = true;
					break;
				}
			}
			if (!found) response = Substituter.replace("<typeof_" + type + "/>", type, response);

		}
*/

                while (response.indexOf(AIMLTag.RANDOM) >= 0)
                {
		  stag = AIMLTag.RANDOM; etag = AIMLTag.RANDOM_CLOSE;
        	  tp = new RandomProcessor();
		  response = processTag(ip,  stag, etag, response, tp);
                }

		stag = AIMLTag.IF_OPEN;
		etag = AIMLTag.IF_CLOSE;
		while (response.indexOf(stag) >= 0) {
			// System.out.println("*** ENTERING A IF CONDITION ***");
			String predname="";
			String predvalue="";
			String predcontains = "";
			String predstartswith = "";
			String predendswith = "";
			String predexists = "";
			String elsestring = "";
			String expr = "";
			String sx = "";
			String head = "";
			String mid = "";
			String tail = "";
			int start = response.indexOf(stag);
			int end = indexOfEndTag(stag, etag, response);
			head = response.substring(0, start);
			mid = response.substring(start+stag.length(), end);
			int m = mid.indexOf(">");
			if (m >= 0) {
				String args = mid.substring(0, m);
				mid = (m+1 >= mid.length()) ? "" : mid.substring(m+1, mid.length());
				predname = getArg("name", args);
				predvalue = getArg("value", args);
				expr = getArg("expr", args);
				predcontains = getArg("contains", args);
				predstartswith = getArg("startswith", args);
				predendswith = getArg("endswith", args);
				predexists = getArg("exists", args);
			}
			if (expr.length() == 0) {
				if (predname.equals("*")) {
					sx = graphmaster.INPUT_STAR;
				} else {
					sx = Classifier.getValue(predname, ip);
				}
				// System.out.println("*** EVALUATING: " + predname + " AGAINST: " + sx + " ***");
				while (true) {
/*Mod 4.0.3 b6 Kim 09-2001
					if (predexists != "" && sx != "") {
						break;
					} else {
						mid = processElse(mid);
					}
*/

/*Void 4.0.3 b8 New patch from Kim
//Add 4.0.3 b6 Kim 09-2001

					if (predexists != "") 
						if (sx!=""){
							break;
						} else {
							mid = processElse(mid);
							break;
						}

//End of Add
End of Void */


/*void 4.0.3 b9 duplicate patch from Kim removed
//Add 4.0.3 b8 Kim 09-2001
                                      //System.out.println("*** EVALUATING: " + predname + " AGAINST: " + sx + " ***");
                                        if (predexists != "") {
                                           if (sx=="") {
                                              mid = processElse(mid);
                                           }
                                           break;
                                        }

//End of Add
*/

					if (!predvalue.equals("")) {
						if (!sx.toLowerCase().equals(predvalue.toLowerCase())) mid = processElse(mid);
						break;
					}
					if (!predcontains.equals("")) {
						if (sx.toLowerCase().indexOf(predcontains.toLowerCase()) < 0) mid = processElse(mid);
						break;
					}
					if (!predstartswith.equals("")) {
						if (!sx.toLowerCase().startsWith(predstartswith.toLowerCase())) mid = processElse(mid);
						break;
					}
					if (!predendswith.equals("")) {
						if (!sx.toLowerCase().endsWith(predendswith.toLowerCase())) mid = processElse(mid);
						break;
					}
					break;
				}
			} else {
				String result = Interpreter.evaluate(ip, expr);
				if (!result.equals("true")) { mid = processElse(mid); }
			}

			tail = (end+etag.length() < response.length()) ? response.substring(end+etag.length()) : "";

			if (mid.indexOf(AIMLTag.ELSE) >= 0) {
				mid = mid.substring(0, mid.indexOf(AIMLTag.ELSE));
			}

			response = head + mid + tail;
		}

                while (response.indexOf(AIMLTag.CONDITION) >= 0)
                {
		  stag = AIMLTag.CONDITION; etag = AIMLTag.CONDITION_CLOSE;
		  tp = new ConditionProcessor();
		  response = processTag(ip,  stag, etag, response, tp);
                }

//Add 4.0.3 b6 <condition name="varname" value="valuename"></condition>

                while (response.indexOf(AIMLTag.CONDITION_OPEN) >= 0) {

                   //System.out.println("*** CONDITION("+response+") ***");

                   //Extract start and end of the <condition> tag
                   int start   = response.indexOf(AIMLTag.CONDITION_OPEN);
                   int end     = response.indexOf(AIMLTag.CONDITION_CLOSE,start);
                   String head = response.substring(0, start);
                   String tail = response.substring(end+AIMLTag.CONDITION_CLOSE.length());

                   //Extract the opening <condition> tag plus attributes
                   int index        = start;
                   String predicate = "";
                   while ( (!predicate.endsWith(">"))  && (index < end)) {
                      predicate = predicate+(String.valueOf(response.charAt(index)));
                      index++;
                   }

                   //System.out.println("*** CONDITION("+predicate+") ***");

                   //Extract content between open and close <condition> tags
                   String mid = "";
                   index      = start+predicate.length();
                   while (index < end) {
                      mid = mid+(String.valueOf(response.charAt(index)));
                      index++;
                   }

                   //System.out.println("*** MID: ("+mid+") ***");

                   //Extract arguments from the open <condition> tag
                   String args = "";
                   StringTokenizer st = new StringTokenizer(predicate, " ");
                   while (st.hasMoreTokens()) {
                     String token = st.nextToken();
                     if (!token.endsWith("<condition")) {
                        if (token.endsWith(">")) {
                           token = token.substring(0,token.length()-1);
                        }
                        if (args.equals("")) {
                           args = token;
                        } else {
                           args = args + " " + token;
                        }
                     }
                   }
                   //System.out.println("*** ARGS: ("+args+") ***");

                   //Now process the content between condition tags
                   String predname="";
                   String predvalue="";

                   predname = getArg("name",args);
                   predvalue= getArg("value",args);
                   predvalue= Substituter.normalize(predvalue);

                   String sx = "";

                   //<condition> with name and value informed
                   if ((!predvalue.equals("")) && (!predname.equals("")) ) {   

                      sx = Classifier.getValue(predname, ip);
                      //System.out.println("*** PREDICATE VALUE: "+sx+" ***");
                      sx = Substituter.normalize(sx);

                      if (!sx.equals(predvalue)) {
                        response = "";
                      } else {
                        response = mid;
                      }

                   } else {

                     //<condition with name but not value
                     if ((predvalue.equals("")) && (!predname.equals("")) ) {

                        response = processLI(ip , predname, mid);

                     } else {
                       //malformed <condition> tag, no name included
                       response = "";
                     }
                   }

                   response    = head + response + tail;
                }

/* Remove 4.0.3 b4 PEC 09-2001

		stag = AIMLTag.CONDITION_OPEN;
		etag = AIMLTag.CONDITION_CLOSE;
		while (response.indexOf(stag) > 0) {
                        System.out.println("*** ENTERING A CONDITION ***");
			String predname="";
			String predvalue="";
			int start = response.indexOf(stag);
			int end = indexOfEndTag(stag, etag, response);
			if (end <= start) response = Substituter.replace(stag,"",response);
			else {
				String head = response.substring(0, start);
				String mid = response.substring(start+stag.length(), end);
				int m = mid.indexOf(">");
				if (m >= 0) {
					String args = mid.substring(0, m);
					mid = (m+1 >= mid.length()) ? "" : mid.substring(m+1, mid.length());
					predname = getArg("name", args);
					predvalue = getArg("value", args);
				} // m >= 0
                                System.out.println("*** PREDICATE NAME: "+predname+" ***");
                                System.out.println("*** CONDITION VALUE: "+predvalue+" ***");
				String sx = Classifier.getValue(predname, ip);
                                System.out.println("*** PREDICATE VALUE: "+sx+" ***");
				sx = Substituter.normalize(sx);
				predvalue = Substituter.normalize(predvalue);
				if (!sx.equals(predvalue)) mid = "";
				String tail =
					(end+etag.length() < response.length()) ? response.substring(end+etag.length()) : "";
				response = head + mid + tail;
                                System.out.println("*** NEW CONDITION: " + response + " ***");
			}
		}
  
*/

                while (response.indexOf(AIMLTag.THINK) >= 0)
                {
		  stag = AIMLTag.THINK; etag = AIMLTag.THINK_CLOSE;
		  tp = new ThinkProcessor();
		  response = processTag(ip,  stag, etag, response, tp);
                }

//Add 4.0.3 b4 PEC 09-2001 support for <id/>,<size/>,<version/>,<date/>

                while (response.indexOf(AIMLTag.ID_VALUE) >= 0) {
			String sx = ip;
                        response = Substituter.replace(AIMLTag.ID_VALUE,sx,response);

		}

                while (response.indexOf(AIMLTag.SIZE_VALUE) >= 0) {
                        String sx = Globals.getsize();
                        response = Substituter.replace(AIMLTag.SIZE_VALUE,sx,response);
		}

                while (response.indexOf(AIMLTag.VERSION_VALUE) >= 0) {
                        String sx = Globals.getversion();
                        response = Substituter.replace(AIMLTag.VERSION_VALUE,sx,response);
		}

                while (response.indexOf(AIMLTag.DATE_VALUE) >= 0) {
                        Date    t = new Date();
                        String sx = t.toString();
                        response = Substituter.replace(AIMLTag.DATE_VALUE,sx,response);
		}

//EAdd

                while (response.indexOf(AIMLTag.UPPERCASE) >= 0)
                {
		  stag = AIMLTag.UPPERCASE; etag = AIMLTag.UPPERCASE_CLOSE;
		  tp = new UppercaseProcessor();
		  response = processTag(ip,  stag, etag, response, tp);
                }

                while (response.indexOf(AIMLTag.LOWERCASE) >= 0)
                {
		  stag = AIMLTag.LOWERCASE; etag = AIMLTag.LOWERCASE_CLOSE;
		  tp = new LowercaseProcessor();
		  response = processTag(ip,  stag, etag, response, tp);
                }

                while (response.indexOf(AIMLTag.FORMAL) >= 0)
                {
		  stag = AIMLTag.FORMAL; etag = AIMLTag.FORMAL_CLOSE;
		  tp = new FormalProcessor();
		  response = processTag(ip,  stag, etag, response, tp);
                }

                while (response.indexOf(AIMLTag.SENTENCE) >= 0)
                {
		  stag = AIMLTag.SENTENCE; etag = AIMLTag.SENTENCE_CLOSE;
		  tp = new SentenceProcessor();
		  response = processTag(ip,  stag, etag, response, tp);
                }

                while (response.indexOf(AIMLTag.GOSSIP) >= 0)
                {
		  stag = AIMLTag.GOSSIP; etag = AIMLTag.GOSSIP_CLOSE;
		  tp = new GossipProcessor();
		  response = processTag(ip,  stag, etag, response, tp);
                }

                while (response.indexOf(AIMLTag.LEARN) >= 0)
                {
		  stag = AIMLTag.LEARN; etag = AIMLTag.LEARN_CLOSE;
		  tp = new LearnProcessor();
		  response = processTag(ip,  stag, etag, response, tp);
                }

/*Remove 4.0.3 b2 
                while (response.indexOf(AIMLTag.FORGET) >= 0)
                {
		  stag = AIMLTag.FORGET; etag = AIMLTag.FORGET_CLOSE;
		  tp = new ForgetProcessor();
		  response = processTag(ip,  stag, etag, response, tp);
                }
*/

/*Remove 4.0.3 b2
                while (response.indexOf(AIMLTag.DISPLAY_OPEN) >= 0)
                {
		  stag = AIMLTag.DISPLAY_OPEN;
		  etag = AIMLTag.DISPLAY_CLOSE;
		  while (response.indexOf(stag) >= 0) {
			String target="";
			String head = "";
			String mid = "";
			String tail = "";
			String height = "";
			String width = "";
			String status = "";
			// System.out.println("*** ENTERING A DISPLAY ***");
			int start = response.indexOf(stag);
			int end = indexOfEndTag(stag, etag, response);
			head = response.substring(0, start);
			mid = response.substring(start+stag.length(), end);
			int m = mid.indexOf(">");
			if (m >= 0) {
				String args = mid.substring(0, m);
				mid = (m+1 >= mid.length()) ? "" : mid.substring(m+1, mid.length());
				target = getArg("target", args);
				height = getArg("height", args);
				width = getArg("width", args);
				status = getArg("status", args);
				// System.out.println("*** URL: " + mid + " ***");
				// // System.out.println("*** TARGET: " + target + " ***");
			}
			boolean displayed = false;
			String newDisplay = "<script language=\"javascript\">function display() { ";
			if (target.equals("blank")) newDisplay = newDisplay + "win = window.open('" + mid + "'); win.focus();"; displayed = true;
			if (target.equals("sized")) newDisplay = newDisplay + "win = window.open('" + mid + "','alicebot','height=" + height + ",width=" + width + ",status=" + status + "'); win.focus()"; displayed = true;
			if (!displayed) newDisplay = newDisplay + "top." + target + ".document.location = '" + mid + "';"; displayed = true;
			newDisplay = newDisplay + "} display(); </script>";
			mid = newDisplay;
			tail = (end+etag.length() < response.length()) ? response.substring(end+etag.length()) : "";
			response = head + mid + tail;
		  }
                }
*/

/*Remove 4.0.3 b2
		while (response.indexOf(AIMLTag.FACE_OPEN) > -1) {
			String value = getArg("value", response);
			if (value.endsWith(".gif") || value.endsWith(".jpg") || value.endsWith(".png")) {
				response = Substituter.replace("<face value=\"" + value + "\"/>", "<script language=\"javascript\">faceValue=\"" + value + "\";</script>", response);
			}
		}
*/

//Add 4.0.3 b2 PEC 09-2001 Implementation of <bot/>
                while (response.indexOf(AIMLTag.BOT_NEW_OPEN) >= 0) {
                        //System.out.println("*** PROCESSING BOT PREDICATE: " + response + " ***");
			String bPredicate = "";
			String bReplace = "";
                        StringTokenizer st = new StringTokenizer(response, " ");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
                                if (token.endsWith("<bot")) {
					bPredicate = (new StringTokenizer(st.nextToken(), "/")).nextToken();
                                        bReplace = "<bot " + bPredicate + "/>";
				}
			}
                        bPredicate = getArg("name",bPredicate);
                        //System.out.println("*** BOT PREDICATE: " + bPredicate + " ***");
                        //System.out.println("*** REPLACING: " + bReplace + " ***");
			response = Substituter.replace(bReplace,Globals.getValue(bPredicate),response);
		}



//End of Add
		while (response.indexOf(AIMLTag.BOT_OPEN) >= 0) {
			// System.out.println("*** PROCESSING BOT PREDICATE: " + response + " ***");
			String bPredicate = "";
			String bReplace = "";
			StringTokenizer st = new StringTokenizer(response, "_");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (token.endsWith("<bot")) {
					bPredicate = (new StringTokenizer(st.nextToken(), "/")).nextToken();
					bReplace = "<bot_" + bPredicate + "/>";
				}
			}
			// System.out.println("*** BOT PREDICATE: " + bPredicate + " ***");
			// System.out.println("*** REPLACING: " + bReplace + " ***");
			response = Substituter.replace(bReplace,Globals.getValue(bPredicate),response);
		}

		while (response.indexOf(AIMLTag.SET_OPEN) >=0 && response.indexOf(AIMLTag.SET_CLOSE) >=0) {
			// System.out.println("*** PROCESSING CUSTOM PREDICATE (SET): " + response + " ***");
			String sPredicate = "";
			String sContent = "";
			String sReplace = "";
			StringTokenizer st = new StringTokenizer(response, "_");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (token.endsWith("<set")) {
					sPredicate = (new StringTokenizer(st.nextToken(), ">")).nextToken();
					String begin = "<set_" + sPredicate + ">";
					String end = "</set_" + sPredicate + ">";

                                        // ** MA ** 07/12/2001
                                        // sanity check - make sure we have both tags

                                        // missing begin tag
                                        if ( response.indexOf(begin) < 0)
                                        {
                                          sContent = "";
					  sReplace = "</set_" + sPredicate + ">";
                                        }
                                        else
                                        // missing end tag
                                        if ( response.indexOf(end) < 0)
                                        {
                                          sContent = "";
					  sReplace = "<set_" + sPredicate + ">";
                                        }
                                        else
                                        {
                                          // we have both tags - process normally
					  sContent = response.substring(
						response.indexOf(begin) + begin.length(),
						response.indexOf(end));
					  sReplace = "<set_" + sPredicate + ">" + sContent + "</set_" + sPredicate + ">";
                                        }
//					sReplace = "<set_" + sPredicate + ">" + sContent + "</set_" + sPredicate + ">";
				}
			}
			 //System.out.println("*** CUSTOM PREDICATE: " + sPredicate + " ***");
			 //System.out.println("*** CUSTOM PREDICATE VALUE: " + sContent + " ***");
			 //System.out.println("*** REPLACING: " + sReplace + " ***");

			Classifier.setValue(sPredicate, ip, sContent);
			response = Substituter.replace(sReplace,sContent,response);
		}

//New 4.0.3 b2 PEC 09-2001 Placeholder for tag
//Mod 4.0.3 b3 PEC 09-2001 Implementation of Tag
//Fix 4.0.10 b10 PEC 09-2001 Fix on parsing logic (substring out of index trap)

                while (response.indexOf(AIMLTag.SET_NEW_OPEN) >=0 && response.indexOf(AIMLTag.SET_NEW_CLOSE) >=0) {

                   //Extract start and end of the <set> tag
                   int start   = response.indexOf(AIMLTag.SET_NEW_OPEN);
                   int end     = response.indexOf(AIMLTag.SET_NEW_CLOSE,start);
                   String head = response.substring(0, start);
                   String tail = response.substring(end+AIMLTag.SET_NEW_CLOSE.length());

                   //Extract the opening <set> tag plus attributes
                   int index        = start;
                   String predicate = "";
                   while ( (!predicate.endsWith(">"))  && (index < end)) {
                      predicate = predicate+(String.valueOf(response.charAt(index)));
                      index++;
                   }

                   //System.out.println("*** SET PREDICATE("+predicate+") ***");

                   //Extract content between open and close <condition> tags
                   String mid = "";
                   index      = start+predicate.length();
                   while (index < end) {
                      mid = mid+(String.valueOf(response.charAt(index)));
                      index++;
                   }

                   //System.out.println("*** SET CONTENT: ("+mid+") ***");

                   //Extract arguments from the open <set> tag
                   String args = "";
                   StringTokenizer st = new StringTokenizer(predicate, " ");
                   while (st.hasMoreTokens()) {
                     String token = st.nextToken();
                     if (!token.endsWith("<set")) {
                        if (token.endsWith(">")) {
                           token = token.substring(0,token.length()-1);
                        }
                        if (args.equals("")) {
                           args = token;
                        } else {
                           args = args + " " + token;
                        }
                     }
                   }
                   //System.out.println("*** SET ARGS: ("+args+") ***");
                   predicate = getArg("name",predicate);
                   //System.out.println("*** VARNAME: " + predicate + " ***");
                   Classifier.setValue(predicate, ip, mid);
                   //4.0.3 b3      Added trim to content ----------VVVVVVV
                   response = head + mid + tail;
		}

//End of Add

/*Remove 4.0.3 b2
		while (response.indexOf(AIMLTag.ADD_OPEN) >=0 && response.indexOf(AIMLTag.ADD_CLOSE) >=0) {
			// System.out.println("*** PROCESSING CUSTOM PREDICATE (ADD): " + response + " ***");
			String aPredicate = "";
			String aContent = "";
			String aReplace = "";
			StringTokenizer st = new StringTokenizer(response, "_");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (token.endsWith("<add")) {
					aPredicate = (new StringTokenizer(st.nextToken(), ">")).nextToken();
					String begin = "<add_" + aPredicate + ">";
					String end = "</add_" + aPredicate + ">";
					aContent = response.substring(
						response.indexOf(begin) + begin.length(),
						response.indexOf(end));
					aReplace = "<add_" + aPredicate + ">" + aContent + "</add_" + aPredicate + ">";
				}
			}
			// System.out.println("*** CUSTOM PREDICATE: " + aPredicate + " ***");
			// System.out.println("*** CUSTOM PREDICATE VALUE: " + aContent + " ***");
			// System.out.println("*** REPLACING: " + aReplace + " ***");
			Classifier.addValue(aPredicate, ip, aContent);
			response = Substituter.replace(aReplace,"",response);
		}

*/
		while (response.indexOf(AIMLTag.GET_CLOSE) >=0) {
			// System.out.println("*** PROCESSING CUSTOM PREDICATE (GET): " + response + " ***");
			String ggPredicate = "";
			String ggContent = "";
			String ggReplace = "";
			StringTokenizer st = new StringTokenizer(response, "_");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (token.endsWith("<get")) {
					ggPredicate = (new StringTokenizer(st.nextToken(), ">")).nextToken();
					String begin = "<get_" + ggPredicate + ">";
					String end = "</get_" + ggPredicate + ">";
					ggContent = response.substring(
						response.indexOf(begin) + begin.length(),
						response.indexOf(end));
					ggReplace = "<get_" + ggPredicate + ">" + ggContent + "</get_" + ggPredicate + ">";
				}
			}
			// System.out.println("*** CUSTOM PREDICATE: " + ggPredicate + " ***");
			// System.out.println("*** CUSTOM DEFAULT VALUE: " + ggContent + " ***");
			// System.out.println("*** REPLACING: " + ggReplace + " ***");
			String ggValue = Classifier.getValue(ggPredicate, ip);
			if (ggPredicate.equals("name")) ggValue = Substituter.capitalizeWords(ggValue);
			if (ggValue.equals("unknown") || ggValue.equals("") || ggValue.equals("?")) {
				response = Substituter.replace(ggReplace, ggContent, response);
			} else {
				response = Substituter.replace(ggReplace, ggValue, response);
			}
		}

		while (response.indexOf(AIMLTag.GET_OPEN) >= 0) {
			// System.out.println("*** PROCESSING CUSTOM PREDICATE (GET): " + response + " ***");
			String gPredicate = "";
			String gReplace = "";
			StringTokenizer st = new StringTokenizer(response, "_");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (token.endsWith("<get")) {
					gPredicate = (new StringTokenizer(st.nextToken(), "/")).nextToken();
					gReplace = "<get_" + gPredicate + "/>";
				}
			}
			// System.out.println("*** CUSTOM PREDICATE: " + gPredicate + " ***");
			// System.out.println("*** REPLACING: " + gReplace + " ***");
			String gValue = Classifier.getValue(gPredicate,ip);
			if (gPredicate.equals("name")) gValue = Substituter.capitalizeWords(gValue);
			response = Substituter.replace(gReplace, gValue ,response);
		}
//Add 4.0.3 b2 PEC 09-2001
                while (response.indexOf(AIMLTag.GET_VALUE) >= 0) {
                        //System.out.println("*** PROCESSING <GET/> PREDICATE: " + response + " ***");
			String bPredicate = "";
			String bReplace = "";
                        StringTokenizer st = new StringTokenizer(response, " ");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
                                if (token.endsWith("<get")) {
					bPredicate = (new StringTokenizer(st.nextToken(), "/")).nextToken();
                                        bReplace = "<get " + bPredicate + "/>";
				}
			}
                        bPredicate = getArg("name",bPredicate);
                        //System.out.println("*** GET PREDICATE: " + bPredicate + " ***");
                        //System.out.println("*** REPLACING: " + bReplace + " ***");
                        String bValue = Classifier.getValue(bPredicate,ip);
                        //System.out.println("*** VALUE: " + bValue + " ***");
                        response = Substituter.replace(bReplace,bValue,response);
		}

//End of Add

/*Remove 4.0.3 b2 
		while (response.indexOf(AIMLTag.RND_OPEN) >= 0) {
			// System.out.println("*** PROCESSING CUSTOM PREDICATE (GET)(RANDOM): " + response + " ***");
			String rPredicate = "";
			String rReplace = "";
			StringTokenizer st = new StringTokenizer(response, "_");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (token.endsWith("<random")) {
					rPredicate = (new StringTokenizer(st.nextToken(), "/")).nextToken();
					rReplace = "<random_" + rPredicate + "/>";
				}
			}
			// System.out.println("*** CUSTOM PREDICATE: " + rPredicate + " ***");
			// System.out.println("*** REPLACING: " + rReplace + " ***");
			String rValue = Classifier.getRandomValue(rPredicate,ip);
			if (rPredicate.equals("name")) rValue = Substituter.capitalizeWords(rValue);
			response = Substituter.replace(rReplace, rValue ,response);
		}
/*

/*Remove 4.0.3 b2 
                while (response.indexOf(AIMLTag.ENCODE) >= 0)
                {
		  stag = AIMLTag.ENCODE; etag = AIMLTag.ENCODE_CLOSE;
		  tp = new EncodeProcessor();
		  response = processTag(ip,  stag, etag, response, tp);
                }

                while (response.indexOf(AIMLTag.DECODE) >= 0)
                {
		  stag = AIMLTag.DECODE; etag = AIMLTag.DECODE_CLOSE;
		  tp = new DecodeProcessor();
		  response = processTag(ip,  stag, etag, response, tp);
                }
*/

                while (response.indexOf(AIMLTag.SYSTEM) >= 0)
                {
		  stag = AIMLTag.SYSTEM; etag = AIMLTag.SYSTEM_CLOSE;
		  tp = new SystemProcessor();
		  response = processTag(ip,  stag, etag, response, tp);
                }


/*Remove 4.0.3 b2
                while (response.indexOf(AIMLTag.LOG) >= 0)
                {
		  stag = AIMLTag.LOG; etag = AIMLTag.LOG_CLOSE;
		  tp = new LogProcessor();
		  response = processTag(ip,  stag, etag, response, tp);
                }
*/

/*Remove 4.0.3 b2
                while (response.indexOf(AIMLTag.TIMER_OPEN) >= 0)
                {
		  stag = AIMLTag.TIMER_OPEN; etag = AIMLTag.TIMER_CLOSE;
		  tp = new TimerProcessor();
		  response = processTag(ip,  stag, etag, response, tp);
                }
*/

/*Remove 4.0.3 b2
                while (response.indexOf(AIMLTag.NQL) >= 0)
                {
		  stag = AIMLTag.NQL; etag = AIMLTag.NQL_CLOSE;
		  tp = new NQLProcessor();
		  response = processTag(ip,  stag, etag, response, tp);
                }
*/

                while (response.indexOf(AIMLTag.SCRIPT) >= 0)
                {
		  stag = AIMLTag.SCRIPT; etag = AIMLTag.SCRIPT_CLOSE;
		  tp = new ScriptProcessor();
		  response = processTag(ip,  stag, etag, response, tp);
                }

/*Remove 4.0.3 b2
                while (response.indexOf(AIMLTag.EVENT) >= 0)
                {
		  stag = AIMLTag.EVENT; etag = AIMLTag.EVENT_CLOSE;
		  tp = new EventProcessor();
		  response = processTag(ip,  stag, etag, response, tp);
                }
*/

		response = Substituter.replace(AIMLTag.SR,AIMLTag.SR_STAR,response);

                while (response.indexOf(AIMLTag.SRAI) >= 0)
                {
                  //System.out.println("*** SRAI: " + response + " ***");
		  stag = AIMLTag.SRAI; etag = AIMLTag.SRAI_CLOSE;
		  tp = new SraiProcessor();
		  response = processTag(ip,  stag, etag, response, tp);
                }

		return response;
	}
}
