package org.alicebot.server.core.util;

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

import java.util.*;
import java.text.*;
import java.lang.*;
import java.net.*;
import java.io.*;

import org.alicebot.server.core.*;
import org.alicebot.server.core.loader.*;
import org.alicebot.server.core.node.*;
import org.alicebot.server.core.parser.*;
import org.alicebot.server.core.processor.*;

/** 
 * The GraphmasterUtility is responsible for various tasks on the Graphmaster. 
 * 
 * @author Richard Wallace, Jon Baer
 * @author Thomas Ringate/Pedro Colla
 * @version 4.1.1
 */

public class Toolkit {
	
	public static StringSet vocabulary = new StringSet();

        /**
          fixBR
          Just lack of a better place for this little filter make me put
          it here..
          It filters out a <br/> tag out of a string and replace it with CR
        */
        public static String fixBR (String strIn) {

          if (strIn == null) {
             return "";
          }

          if (strIn.trim().equals("")) {
             return "";
          }


          if (strIn.length() < 5) {
             return strIn;
          }

          while (strIn.toLowerCase().indexOf("<br") >= 0) {

            //Extract start and end of the <br/> tag
            int start   = strIn.toLowerCase().indexOf("<br");
            int end     = strIn.indexOf("/>",start);

            if ((start >= 0) && (end >= 0)) {
               String head = strIn.substring(0, start);
               String tail = strIn.substring(end+2);

               //If start < end and either front head or back tail then replace
               if ( (start < end) &&
                  ( (!head.equals("")) || (!tail.equals("")) ) ) {
                  strIn = head + "\n" + tail;
               }

            }
          }
          return strIn;
        }

        /**
          filterLF
          Just lack of a better place for this little filter make me put
          it here..
          It filters out multiple CR in a single line
        */
        public static String filterLF (String strIn) {

          if (strIn == null) {
             return "";
          }

          strIn = strIn.trim();

          if (strIn.equals("")) {
             return "";
          }

          if (strIn.equals("\n")) {
             return "";
          }

          if (strIn.length() < 2) {
             return strIn;
          }

          while (strIn.toLowerCase().indexOf("\n\n") >= 0) {

            //Extract start and end of the <br/> tag
            int start   = strIn.toLowerCase().indexOf("\n\n");
            int end     = start+1;

            if ((start >= 0) && (end >= 0)) {
               String head = strIn.substring(0, start);
               String tail = strIn.substring(end+1);

               //If start < end and either front head or back tail then replace
               if ( (start < end) &&
                  ( (!head.equals("")) || (!tail.equals("")) ) ) {
                  strIn = head + "\n" + tail;
               }

            }
          }
          return strIn;
        }

        /**
          filterSpaces
          Just lack of a better place for this little filter make me put
          it here..
          It filters out multiple SPACES in a single line
        */
        public static String filterSpaces (String strIn) {


          if (strIn == null) {
             return "";
          }

          strIn = strIn.trim();

          if (strIn.equals("")) {
             return "";
          }

          if (strIn.equals("\n")) {
             return "";
          }

          if (strIn.length() < 2) {
             return strIn;
          }

          StringTokenizer st = new StringTokenizer(strIn,"\n");
          String strData = "";
          strIn = "";
          while (st.hasMoreTokens()) {
            strData = st.nextToken();
            if (!strData.trim().equals("")) {
               strIn = strIn + strData + "\n";
            }
          }

          return strIn;
        }

	/**
	 * Loads any AIML Files that appear in the &lt;template&gt;.
	 */
	public static void process_load_tags(String filename, String templ) {


             AIMLParser p    = new AIMLParser(1);
             String ip       = "127.0.0.1";
             String response = p.processResponse(ip, templ);
             return;
		
	} 

	
	public synchronized static void checkpoint(String fname) {
		FileOutputStream fos;
		PrintStream ps;
		if (Graphmaster.TARGETS.size() > 0) {
			try {
				fos = new FileOutputStream("./logs/TARGETS.log");
				ps = new PrintStream(fos);  
				ps.println("#Graphmaster.TARGETS from "+fname+" "+Graphmaster.TARGETS.size());
				int targetcount = 0;
				for (int i = 0; i < Graphmaster.TARGETS.size() && targetcount < Graphmaster.MAX_TARGETS; i++) {
					ps.println(Graphmaster.TARGETS.elementAt(i)); 
					targetcount++;
				} // for
				ps.flush();
				fos.close();
			} catch (Exception e) {}
			
			try {
				fos = new FileOutputStream("./logs/FULLTARGETS.log");
				ps = new PrintStream(fos);  
				ps.println("#Graphmaster.TARGETS from "+fname+" "+Graphmaster.TARGETS.size());
				int targetcount = 0;
				for (int i = 0; i < Graphmaster.TARGETS.size() && targetcount < Graphmaster.MAX_TARGETS; i++) {
					Hashtable w = Graphmaster.TARGETS.Countmap;
					StringSorter v = (StringSorter)w.get(Graphmaster.TARGETS.elementAt(i));
					ps.print(Graphmaster.TARGETS.Count(i)+" ");
					for (int k = 0; k < Graphmaster.TARGETS.Count(i); k++) {
						ps.print(v.elementAt(k)+" ");
					}
					ps.println();
					ps.println(Graphmaster.TARGETS.elementAt(i)); 
					targetcount++;
				} // for
				fos.close();
			} catch (Exception e) {}
		} // if
		
		if (Graphmaster.ACTIVATIONS.size() > 0) {
			try {
				fos = new FileOutputStream("./logs/ACTIVATIONS.log");
				ps = new PrintStream(fos);  
				ps.println("#Activated categories from "+fname+" "+Graphmaster.ACTIVATIONS.size());
				for (int i = 0; i < Graphmaster.ACTIVATIONS.size(); i++) {
					ps.print(Graphmaster.ACTIVATIONS.Count(i)+" "+(String)Graphmaster.ACTIVATIONS.elementAt(i));
					ps.println();
				} // for
				ps.flush();
				fos.close();
			} catch (Exception e) {}
			
			
			try {
				fos = new FileOutputStream("./logs/FULLACTIVATIONS.log");
				ps = new PrintStream(fos);  
				ps.println("#Activated categories from "+fname+" "+Graphmaster.ACTIVATIONS.size());
				Hashtable w = Graphmaster.ACTIVATIONS.Countmap;
				for (int i = 0; i < Graphmaster.ACTIVATIONS.size(); i++) {
					StringSorter v = (StringSorter)w.get(Graphmaster.ACTIVATIONS.elementAt(i));
					ps.println(Graphmaster.ACTIVATIONS.Count(i)+" "+(String)Graphmaster.ACTIVATIONS.elementAt(i));
					for (int k = 0; k < Graphmaster.ACTIVATIONS.Count(i); k++) {
						ps.println(v.elementAt(k)+" ");
					}
					ps.println();
				} // for
				ps.flush();
				fos.close();
			} catch (Exception e) {}
			if (Graphmaster.TARGETS.size() > Graphmaster.TARGET_SIZE) {
				Graphmaster.TARGETS = new StringVoter();
				Graphmaster.ACTIVATIONS = new StringVoter();
				Graphmaster.THAT_NODEMAPPER = new Hashtable();
			}
		}
	} 
	
	public static void DialogToAIML() {
		try {
			FileInputStream fis = new FileInputStream("new.txt");
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			FileOutputStream fos = new FileOutputStream("new.aiml");
			PrintStream ps = new PrintStream(fos);
			ps.println("<alice>");
			String line;
			String category="";
			String that="";
			String pattern="";
			String template="";
			String state = "START";
			while ((line = br.readLine()) != null) {
				//System.out.println(state+line);
				if (line.length()==0) {
					if (template.length() > 0) {
						if (that.length() > 0) {
							ps.println("<category>\n<pattern>"+pattern+"</pattern><that>"+that+"</that>");
							ps.println(Graphmaster.TEMPLATE+template+"</template>\n</category>");
						}
						else {
							ps.println("<category>\n<pattern>"+pattern+"</pattern>");
							ps.println(Graphmaster.TEMPLATE+template+"</template>\n</category>");
						} 
					}
					pattern = ""; that=""; template="";
				} 
				if (state.compareTo("START")==0)
				{
					if (line.startsWith("Client: ")) {
						state = "TYPICAL";
						pattern = line.substring("Client: ".length(),line.length());
					}
					else if (line.startsWith("Robot: ")) {
						state = "THAT";
						that = line.substring("Robot: ".length(),line.length());
					}
				}
				else if (state.compareTo("THAT")==0)
				{
					if (line.startsWith("Robot: ")) {
						template = line.substring("Robot: ".length(),line.length());
						state = "START";
					}
					if (line.startsWith("Client:"))
						pattern = line.substring("Client: ".length(),line.length());
				}
				else if (state.compareTo("TYPICAL")==0)
				{
					if (line.startsWith("Robot: ")) {
						template = line.substring("Robot: ".length(),line.length());
						state = "START";
					}
				}
			} // while
			ps.println("</alice>");
			ps.close();
			fos.close();
			br.close();
			fis.close();
		} catch (Exception e) {System.out.println("readDialog "+e);}
	} // readDialogFormat
	
	public static void readfilenames() {
		try {
			Graphmaster.FILENAME_SET = new StringSet();
			String fname = "./logs/STARTUP.log";
			FileInputStream fis = new FileInputStream(fname);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			String line; 
			while ((line = br.readLine()) != null) {
				Graphmaster.FILENAME_SET.add(line);
			}
		}
		catch (Exception e) { System.out.println("readfilenames "+e);}
	} // readfilenames
	
	public static void load(String fname) {
		load_aiml_file(fname);
	}
	
	public static void unload(String fname) {
		unload_aiml_file(fname);
	}
	
	public static void unload_aiml_file(String fname) {
		// System.out.println("*** ATTEMPTING TO FORGET: " + fname + " ***");
		
		BufferedReader br = null;
		// Resolve the URL (network, local, bot)
		try {
                        boolean islocalfile = false; //Add 4.0.3 b8 PEC 09-2001
			if (fname.startsWith("http:") || fname.startsWith("Http:"))
			{
				// System.out.println("*** LEARNING NETWORK AIML: " + fname + " ***");
				URL url = new URL(fname);
				br = new BufferedReader(new InputStreamReader(url.openStream()));
			} else {
                                islocalfile = true;
				if (fname.startsWith("c:") || fname.startsWith("C:") || fname.startsWith("/")) {
					// System.out.println("*** LEARNING LOCAL AIML: " + fname + " ***");
				} else {
					if (!fname.startsWith("bots")) {
						fname = "bots" + 
							System.getProperty("file.separator") +
							Globals.getBotName() + 
							System.getProperty("file.separator") +
							"aiml" + 
							System.getProperty("file.separator") + fname;
					}
					// System.out.println("*** LEARNING BOT AIML: " + fname + " ***");
				}
                                //Fix 4.0.3 b2 PEC 09-2001
                                File AIMLFile = new File(fname);
                                if (AIMLFile.exists() == true) {
                                   FileInputStream fis = new FileInputStream(fname); 
                                   InputStreamReader isr = new InputStreamReader(fis);
                                   br = new BufferedReader(isr);
                                } else {
                                   System.out.println("*** FILE("+fname+") do not exists, ignored");
                                   return;
                                }
                                AIMLFile = null;
                                //End of fix
			}
                      //System.out.println("*** LEARNING AIML: "+fname + " ***");
                      //Fix 4.0.3 b8 moved here not to break remote loading
                        if (islocalfile == true) {
                           File AIMLFile = new File(fname);
                           if ((AIMLFile.length()   >  0   ) &&
                               (AIMLFile.exists()   == true) &&
                               (AIMLFile.canRead()  == true)) {

                               // File is alright to load

                           } else {
                             System.out.println("*** FILE("+fname+") [TU] can not be opened, ignored");
                           }
                           AIMLFile = null;
                           return;
                        }
                      //End of Fix

                        AIMLReader alice = new AIMLReader(fname, br, new AIMLLoader(fname));
                        alice.read(); 
			
                      // System.out.println("*** FORGOT AIML: "+fname + " ***");
		} 
		catch (Exception e) {
			e.printStackTrace();	
		}			
		
	}
	
	public static void load_aiml_file(String fname) {
		
		if (fname.equals("*")) {
			
			File dir = new File(fname = "bots" + System.getProperty("file.separator") + Globals.getBotName());
			String[] allFiles = dir.list();
			
			for (int x = 0; x < allFiles.length; x++) {
				if (allFiles[x].endsWith(".aiml") && !allFiles[x].equals(Globals.getServerProps().getProperty("server.engine.startup"))) {
					load_aiml_file(allFiles[x]);
				}
			}
			
			return;
			
		}
		
		// System.out.println("*** ATTEMPTING TO LEARN: " + fname + " ***");
		BufferedReader br = null;
		// Resolve the URL (network, local, bot)
		try {
                        boolean islocalfile = false; //Add 4.0.3 b8 PEC 09-2001
			if (fname.startsWith("http:") || fname.startsWith("Http:"))
			{
				// System.out.println("*** LEARNING NETWORK AIML: " + fname + " ***");
				URL url = new URL(fname);
				br = new BufferedReader(new InputStreamReader(url.openStream()));
			} else {
                                islocalfile = true; //Add 4.0.3 b8 PEC 09-2001
				if (fname.startsWith("c:") || fname.startsWith("C:") || fname.startsWith("/")) {
					// System.out.println("*** LEARNING LOCAL AIML: " + fname + " ***");
				} else {
					if (!fname.startsWith("bots")) {
						fname = "bots" + 
							System.getProperty("file.separator") +
							Globals.getBotName() + 
							System.getProperty("file.separator") + fname;
					}
					// System.out.println("*** LEARNING BOT AIML: " + fname + " ***");
				}
                                //Fix 4.0.3 b2 PEC 09-2001 Verify if file exists
                                File AIMLFile = new File(fname);
                                if (AIMLFile.exists() == true) {
                                   FileInputStream fis = new FileInputStream(fname); 
                                   InputStreamReader isr = new InputStreamReader(fis);
                                   br = new BufferedReader(isr);
                                } else {
                                   System.out.println("*** FILE("+fname+") do not exists, ignored");
                                   return;
                                }
                                AIMLFile = null;
                                //end of fix
			}
                        boolean loadfile = true;
                      //Fix 4.0.3 b8 moved here not to break remote loading
                        if (islocalfile == true) {
                           File AIMLFile = new File(fname);
                           if ((AIMLFile.length()   >  0   ) &&
                               (AIMLFile.exists()   == true) &&
                               (AIMLFile.canRead()  == true)) {

                               // File is alright to load

                           } else {
                             System.out.println("*** FILE("+fname+") can not be loaded, ignored");
                             loadfile = false;
                           }
                           AIMLFile = null;
                        }
                      //End of Fix

                      //System.out.println("*** LEARNING AIML: "+fname + " ***");
                      if (loadfile == true) {
                         AIMLReader alice = new AIMLReader(fname, br, new AIMLLoader(fname));
                         alice.read(); 
                      }
		} 
		catch (Exception e) {
			e.printStackTrace();
		}	
		
	} 
	
	public static void classify(String fname, String client_start, String bot_start) {   
		// System.out.println("Classify :"+fname);
		if (fname==null || fname.length() < 1) return;
		int linecnt=0;
		String line=""; 
		String client_line_contains = client_start; // "Says: ";
		String robot_line_start = bot_start; // "ALICE Says:";
		try {
			FileInputStream fis = new FileInputStream(fname);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr, 1024);
			while ((line = br.readLine()) != null) {
				int m = line.indexOf(client_line_contains);
				linecnt++;
				if (linecnt % 1000 ==0) System.out.println(linecnt+" lines read");
				if (line != null && line.indexOf(robot_line_start) < 0 &&
					m >= 0) { 
					if (m < 1) Graphmaster.HNAME = "localhost";
					else Graphmaster.HNAME = line.substring(0, m-1);
					line = line.substring(m+client_line_contains.length(),line.length());
					line = line.trim();
					line = Substituter.cleanup_http(line);
					line = Substituter.deperiodize(line);
					line = Substituter.normalize(line);
					line = Substituter.normalize(line);
					while (line.endsWith(".")) line = line.substring(0,line.length()-1);
					if (line.length() < 1) line=Graphmaster.UNDEFINED;
					else {
						if (!Graphmaster.THAT_NODEMAPPER.containsKey(Graphmaster.HNAME)) 
							Graphmaster.THAT_NODEMAPPER.put(Graphmaster.HNAME, Graphmaster.UNDEFINED);
						String that = (String)Graphmaster.THAT_NODEMAPPER.get(Graphmaster.HNAME);
						String topic = new String(Graphmaster.UNDEFINED);

                                                /*Remove 4.1.2 b1 PEC 09-2001 reflect the change
                                                  introduced in the match method interface

						Nodemapper node = Graphmaster.match(line, that, topic);
						String thatpattern = Graphmaster.THAT_PATTERN;
						String pattern = Graphmaster.INPUT_PATTERN;
                                                */


                                                /*Add 4.1.2 b1 PEC 09-2001 functional equivalent of
                                                  the above replaced lines under the new interface
                                                */

                                                Match M = Graphmaster.match(line,that,topic);
                                                if (M == null) {
                                                   return;
                                                }
                                                Nodemapper node    = M.node;
                                                String thatpattern = M.thatPattern;
                                                String pattern     = M.inputPattern;

                                                /*-->End of Add*/

						String input = line;

						String templ = (node.get(Graphmaster.TEMPLATE) == null) ? "" : (String)node.get(Graphmaster.TEMPLATE);
						String gname = (node.get(Graphmaster.FILENAME) == null) ? "" : (String)node.get(Graphmaster.FILENAME);

						if (templ.indexOf("<sr") < 0) {
							vote(fname, that, input, thatpattern, pattern);
						} // if
					} // else
				} // if
				else {
					if (line.startsWith(robot_line_start)) {
						line = line.substring(robot_line_start.length(),line.length());
						line = Substituter.deperiodize(line);
						SentenceTokenizer st = new SentenceTokenizer(line, "?!.");      
						int x = st.countTokens(); 
						for (int i = 0; i < x; i++) line = st.nextToken();
						line = Substituter.normalize(line);
						line = Substituter.normalize(line);
						if (line.length() < 1) line=Graphmaster.UNDEFINED;
						Graphmaster.THAT_NODEMAPPER.put(Graphmaster.HNAME, line);
					} // if
				} // else
			} // while
		} // try
		catch (Exception e) { 
			//System.out.println("Classify: "+e);
			//System.out.println("On: "+line);
			//System.out.println("inputPattern="+inputPattern+" thatPattern="+thatPattern);
		}
	} // classify
	
	public static void think() {
		readfilenames();
		System.out.println(Graphmaster.FILENAME_SET.size()+" log files");
		int lastfile=0; int k=0;
		while (true) {
			if (lastfile <= 0) { 
				readfilenames();
				lastfile = Graphmaster.FILENAME_SET.size()-1;
			}
			try {    
				Graphmaster.TARGETS = new StringVoter();
				Graphmaster.ACTIVATIONS = new StringVoter();
				Graphmaster.THAT_NODEMAPPER = new Hashtable();
				for (k = lastfile; k >= 0; k--) {
					String fname = (String)Graphmaster.FILENAME_SET.elementAt(k);
					classify(fname, "", ""); // must change
					checkpoint(fname);
				} // for
			} // try
			catch (Exception e) { 
				lastfile = k;
				System.out.println("Think :"+e);
			} // catch
		} // while true
	} // think
	
	public static String countbranches() {
		return Graphmaster.THE_BRANCH_COUNT;
	}
	
	public static PrintStream cps;
	public static String countbranches(Nodemapper g) {
		String response= Graphmaster.UNDEFINED;
		try {
			Locale home = Locale.US;
			NumberFormat nf = NumberFormat.getNumberInstance(home); 
			nf.setMaximumFractionDigits(3);
			// initcounts();
			countbranches(g, 0);
			response = "";
			response = Graphmaster.TERMINAL_COUNT + " categories.<BR>\n";
			response += Graphmaster.TOTAL_NODE_COUNT +" total nodes.<BR>\n";
			response += vocabulary.size()+" words in pattern vocabulary.<BR>\n";
			response += "<table border=1>\n";
			response +="<tr><td>Graph <br>depth</td><td>Branch <br>count</td><td>Node <br>count</td><td>Branching <br>factor</td></tr>";
			for (int i = 0; i < Graphmaster.MAX_DEPTH && Graphmaster.NODE_COUNT[i] > 0; i++) {
				String ratio = nf.format(Graphmaster.BRANCH_COUNT[i]/(float)(Graphmaster.NODE_COUNT[i]));
				response += "<tr><td>"+i+"</td><td>"+nf.format(Graphmaster.BRANCH_COUNT[i])+"</td><td>"+nf.format(Graphmaster.NODE_COUNT[i])+"</td><td>"+ratio+"</td></tr>";
			}
			response += "</table>\n";
			cps.close();
			// fos.close();
		} catch (Exception e) {e.printStackTrace();}
		return response;
	} // countbranches


	// recursively count nodes and branches:
	public static void countbranches(Nodemapper parent, int depth) {
		cps.println(parent.toString());
		if (depth < Graphmaster.MAX_DEPTH) {
			Set words = parent.keySet();
			Iterator w = words.iterator();
			while (w.hasNext()) {
				String u = (String)w.next();
				if (!u.startsWith("<")) {
					// System.out.println(u);
					Graphmaster.BRANCH_COUNT[depth] ++;
				}
			} // while
			Graphmaster.NODE_COUNT[depth] += 1;
			Graphmaster.TOTAL_NODE_COUNT++;
			String word="";
			if (parent.get(Graphmaster.TERMINAL) != null) {
				if (parent.get(Graphmaster.SUBGRAPH) != null) {
					Nodemapper subgraph = (Nodemapper)parent.get(Graphmaster.SUBGRAPH);
					countbranches(subgraph, depth+1);
				}
				else Graphmaster.TERMINAL_COUNT++; // only count "real" terminal nodes
				if (Graphmaster.TERMINAL_COUNT % 1000 == 0) {
					// System.out.println(terminalcount);  System.out.flush();
				} // else
			} // if
			if (parent.keySet().size() > 0) {
				Iterator enum = parent.keySet().iterator(); //  next words
				while (enum.hasNext()) {
					word = (String)enum.next();
					if (!word.startsWith("<")) {
						vocabulary.add(word);
						Nodemapper next = (Nodemapper)parent.get(word);
						countbranches(next, depth+1);
					}
				} 
			} 
		} 
	} 
	
	public static void initcounts() {
		Graphmaster.TOTAL_CATEGORIES = 0;
		Graphmaster.TOTAL_NODE_COUNT = 0;
		Graphmaster.TERMINAL_COUNT = 0;
		vocabulary = new StringSet();
		for (int i = 0; i < Graphmaster.MAX_DEPTH; i++) {
			Graphmaster.BRANCH_COUNT[i] = 0;
			Graphmaster.NODE_COUNT[i] = 0;
		} 
	}
	
	public static void vote(String fname, String that, String input, String thatpattern, String pattern) {
		int x = pattern.indexOf("*");
		int y = thatpattern.indexOf("*");
		/* 
		 * x neg y neg = both atomic : no vote
		 * x pos y neg = that atomic, input wild : vote that
		 * x neg y pos = input atomic, that wild : vote that
		 * x pos y pos = both wild : vote 
		 *
		 * I * : ATOMIC = vote that
		 * YES : WILD * = vote that
		 * I * : WILD * = vote both (unless that is undefined)
		 */
		String vote;
		vote = input+" : "+that;
		Graphmaster.ACTIVATIONS.add(pattern+" : "+that, vote);
		if ((x >= 0 || y >= 0) && that.compareTo(Graphmaster.UNDEFINED) != 0) {
			vote = "Robot: "+that+"\nClient: "+input+"\nRobot: \n";
			Graphmaster.TARGETS.add(vote, Graphmaster.HNAME);
			if (Graphmaster.TARGETS.votescast % 1000 == 0) {checkpoint(fname); System.out.println(Graphmaster.TARGETS.size()+" targets");
			}
		}
		if (x >= 0 && y >= 0) {
			vote = "Client: "+input+"\nRobot: \n"; 
			Graphmaster.TARGETS.add(vote, Graphmaster.HNAME);
			if (Graphmaster.TARGETS.votescast % 1000 == 0) {checkpoint(fname); System.out.println(Graphmaster.TARGETS.size()+" targets");
			}
		} // if
	} // vote
	
	
}

