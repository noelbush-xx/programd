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
import java.text.*;
import java.lang.*;
import java.net.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.alicebot.server.core.loader.*;
import org.alicebot.server.core.node.*;
import org.alicebot.server.core.parser.*;
import org.alicebot.server.core.responder.*;
import org.alicebot.server.core.util.*;


/**
 * The Graphmaster is the "brain" of ALICE.  
 * It holds all the patterns and subpatterns and its associated templates.
 *
 * @author Richard Wallace, Jon Baer
 * @author Thomas Ringate/Pedro Colla
 * @version 4.1.1
 */

public class Graphmaster extends Thread implements Serializable {
	
	/** An undefined. */
	public static final String UNDEFINED="UNDEFINED"; 
	
	/** An unknown. */
	public static final String UNKNOWN = "unknown";
	
	/** A terminal. */
	public static final String TERMINAL = "<terminal>";
	
	/** A subgraph. */
	public static final String SUBGRAPH = "<subgraph>";
	
	/** A pattern. */
	public static final String PATTERN = "<pattern>";
	
	/** A template. */
	public static final String TEMPLATE = "<template>";
	
	/** A that. */
	public static final String THAT = "<that>";
	
	/** A topic. */
	public static final String TOPIC = "<topic>";
	
	/** A star (*). */
	public static final String STAR = "<star>";
	
	/** A type (*). */
	public static final String TYPE = "<type>";
	
	/** The filename. */
	public static final String FILENAME = "<filename>";
	
	/** True string literal. */
	public static final String TRUE = "true";
	
	/** The "*" used in matching. */
	public static final String ASTERICK = "*";
	
	/** The "_" used in matching. */
	public static final String UNDERSCORE = "_";
	
	/** The "" used in matching. */
	public static final String EMPTY = "";
	
	/** A spaced used in production of string literals. */
	public static final String SPACE = " ";
	
	/** The root nodemaster. */
	public static Nodemapper ROOT = new Nodemaster(); 
	
	/** The total number of categories read. */
	public static int TOTAL_CATEGORIES = 0; 
	
	/** The total number of nodes in the Graphmaster. */
	public static int TOTAL_NODE_COUNT = 0; 
	
	/** The total number of terminal nodes in the Graphmaster. */
	public static int TERMINAL_COUNT = 0; 
	
	/** The max depth allowed in the Graphmaster. */
	public static int MAX_DEPTH = 24; 
	
	/** The number of branches at each depth. */
	public static int BRANCH_COUNT[] = new int[MAX_DEPTH];
	
	/** The number of nodes at each depth. */
	public static int NODE_COUNT[] = new int[MAX_DEPTH];

	public static String THAT_PATTERN;
	public static String INPUT_PATTERN;
	public static String TOPIC_PATTERN;

        /**
          Multiple stars on INPUT detected during the last match
        */
        public static Vector INPUT_STAR = null;

        /**
          Multiple stars on THAT detected during the last match
        */
        public static Vector THAT_STAR  = null;

        /**
          Multiple stars on TOPIC detected during the last match
        */
        public static Vector TOPIC_STAR = null;

        /**
          Match object to store the results of the last match
          performed
        */
        public static Match  MASTER_MATCH = null;

	/** The input type. */
	public static String INPUT_TYPE;
	
	/** The filename set. */
	public static StringSet FILENAME_SET;
	
	/** The host name. */
	public static String HNAME;
	
	/** The that nodemapper hashtable. */
	public static Hashtable THAT_NODEMAPPER = new Hashtable();
	
	/** The targets. */
	public static StringVoter TARGETS;
	
	/** The activations. */
	public static StringVoter ACTIVATIONS;
	
	/** The max targets for checkpoint. */
	public static final int MAX_TARGETS = 1000; 
	
	/** The target size (due to memory limits). */
	public static final int TARGET_SIZE = 100000; 
	
	/** The branch count. */
	public static String THE_BRANCH_COUNT;
	
	/** The mode to run the Graphmaster in. */
	public static int MODE;

        /** The current Topic of the conversation */  //4.1.1 b5 PEC 09-2001
        public static String CURRENT_TOPIC = "";
	
	/** Server mode. */
	public static final int SERVER = 1;
	
	/** Console mode. */
	public static final int CONSOLE = 2;

//Add (4.0.3 b1) PEC 09-2001 Version & Build Global Values

        /** Version of this package */
        public static final String VERSION = "4.1.2";

        /** Build Number of this package (internal regression test control) */
        public static final String BUILD = "06";

        /** CopyRight Notice */
        public static final String COPYRIGHT = "AliceBot Server (c) 2001 A.L.I.C.E. A.I Foundation";

        /** Load Time Marker */
        public static boolean loadtime = true;
//EAdd

	public Graphmaster() {}
	
	public Graphmaster(int mode) {
		this.MODE = mode;
	}

        /** Add a new path topic-that-pattern to the Graphmaster */
	public static Nodemapper
		add(String pattern, String that, String topic) {
		/////////////////////////////////////////////
		String inPattern = "";
		char[] c = (new String(pattern)).toCharArray();
		for (int x = 0; x < c.length; x++) {
			inPattern = inPattern + Character.getNumericValue(c[x]);
		}
		/////////////////////////////////////////////

		String path = pattern+" <that> "+that+" <topic> "+topic;
                //System.out.println("*** GRAPHMASTER ADD: Pattern("+path+") ***");
		Nodemapper node = add(path, ROOT, 0);
		return(node);
	} 

        /** Add a child node to the Graphmaster */
	public static Nodemapper
		add(String sentence, Nodemapper parent, int depth) {
		StringTokenizer st = new StringTokenizer(sentence);
		int count = st.countTokens();
		return add(st, parent, count, depth, sentence);
	} // Graphmaster.add 3-args
	
	public static Nodemapper
		add(StringTokenizer st, Nodemapper parent, int count, int depth, String sentence) {
		if (count == 0) {
			return parent;
		} // if (count == 0)
		else {
			String word = st.nextToken();
			Nodemapper d;
			if (parent.containsKey(word)) 
				d = (Nodemapper)parent.get(word);
			else {
				d = new Nodemaster();
				parent.put(word, d);
			}
			return add(st, d, count-1, depth+1, sentence);
		} 
	} 
/* =========================================================================*/
/** match
    Are the methods involved in the pattern matching process
    Adapted from ProgramB code excerpt provided by Dr. R.Wallace,
    targetting has been removed from this version since it will be
    updated by Dr. R.Wallace when the new version become released
*/

        /*=====================================================
          High Level Prototype of the method
          Used for external access
          =====================================================*/
        public static Match match(String input, String that, String topic) {

         Match M;

         /*
          Adapt the input to be conforming with the way paths are
          stored in the Graphmaster
              {input} <that> {that} <topic> {topic}
         */
         String path  = input+" <that> "+that+" <topic> "+topic;
         CURRENT_TOPIC= topic.trim();
         //System.out.println("*** GRAPHMASTER START: Path("+path+") Current Topic("+CURRENT_TOPIC+") ***");

         /*
          Perform the actual matching
         */
         M = match(ROOT, ROOT, path, "", "",true,false,false);

         /*
          Format the path
         */
         path = Substituter.replace("<that>",":",path);
         path = Substituter.replace("<topic>",":",path);
         M.path = M.inputPattern + " : " + M.thatPattern + " : " +
                  M.topicPattern + " : " + path;

         return M;
        }

        /*=====================================================
          Low Level Prototype of the method, used for internal
          recursion.
          Extracted from ProgramB, interface modified to allow
          the capture of THATSTAR and TOPICSTAR
          =====================================================*/
        public static Match match(Nodemapper node, Nodemapper parent,
                String input, String star, String path, boolean insideInput, boolean insideThat, boolean insideTopic) {
		
		StringTokenizer st = new StringTokenizer(input);
		int count = st.countTokens();
		Match M;
		if (count == 0) {
			if (node.containsKey("<template>")) {
                           M = new Match();
                           M.topicStar.push(star.trim());
                           M.topicPattern=path.trim();
                           M.node = node;
                           return M;
			} // if
			else return null;
		} // if
		String word = st.nextToken(); String tail = "";
		if (st.hasMoreTokens())
			tail = input.substring(word.length()+1, input.length());
		if (node.containsKey("_")) {
                        M = match((Nodemapper)node.get("_"), node, tail, word, path+" _",insideInput,insideThat,insideTopic);
			if (M != null) return M;
		} // if
		
		if (node.containsKey(word)) {
                        if (word.startsWith("<")) {

                           /* Define which part of the path we're working with */

                           if (word.compareTo("<that>") == 0) {
                              insideInput= false;
                              insideThat = true;
                              insideTopic= false;
                           } else {
                             if (word.compareTo("<topic>") == 0) {
                                insideInput = false;
                                insideThat  = false;
                                insideTopic = true;
                             }
                           }

                           M = match((Nodemapper)node.get(word), node, tail, "", "",insideInput,insideThat,insideTopic);
                        } else {
                           M = match((Nodemapper)node.get(word), node, tail, star, path+" "+word,insideInput,insideThat,insideTopic);
                        }
			
			if (M != null) {
				if (word.compareTo("<that>")==0) {
					insideInput = false;
                                        insideThat  = true;
                                        insideTopic = false;
					M.inputStar.push(star.trim());
					M.inputPattern = path.trim();
				} // if
				else if (word.compareTo("<topic>")==0) {
                                        //System.out.println("*** GRAPHMASTER THAT END/TOPIC START: PUSH Star("+star+") Path("+path+") ***");
					insideInput = false;
                                        insideThat  = false;
                                        insideTopic = true;
					M.thatStar.push(star.trim());
					M.thatPattern = path.trim();
				} else {
					//
				}
				return M;
			} // if
			
		} // if node contains word
         
		if (node.containsKey("*")) {
                        M = match((Nodemapper)node.get("*"), node, tail, word, path+" *",insideInput,insideThat,insideTopic);
			if (M != null) {

                                //System.out.println("*** GRAPHMASTER (*): Path("+path+" *) FLAGS insideInput("+insideInput+") insideThat("+insideThat+") insideTopic("+insideTopic+") ***");

                                /* Capture and push intermediate stars for INPUT, THAT, TOPIC */
				if (insideInput) {
                                   if (star.length() > 0) {
                                      //System.out.println("*** GRAPHMASTER (*): PUSH INPUTStar("+star+") Path("+path+") ***");
                                      M.inputStar.push(star.trim());
                                   }
                                } else {
                                  if (insideThat) {
                                     if (star.length() > 0) {
                                        //System.out.println("*** GRAPHMASTER (*): PUSH THATStar("+star+") Path("+path+") ***");
                                        M.thatStar.push(star.trim());
                                     }
                                  } else {
                                    if (insideTopic) {
                                       if (star.length() > 0) {
                                          //System.out.println("*** GRAPHMASTER (*): PUSH TOPICStar("+star+") Path("+path+") ***");
                                          M.topicStar.push(star.trim());
                                       }
                                    }
                                  }
                                }
                                //System.out.println("*** GRAPHMASTER (*): Star("+star+") Path("+path+") ***");
				return M;
			}
		} // if node contains *
                if (node.equals(parent.get("*")) || node.equals(parent.get("_"))) {
                        //System.out.println("*** GRAPHMASTER (Parent *): Star("+star+") Path("+path+") ***");
                        return match(node, parent, tail, star+" "+word, path,insideInput,insideThat,insideTopic);
                }
		
		return null;
	} // match

    public static int activationCount = 0;


/* =========================================================================*/

	
	public static boolean remove(String pattern, String that, String topic) {
		/////////////////////////////////////////////
		String inPattern = "";
		char[] c = (new String(pattern)).toCharArray();
		for (int x = 0; x < c.length; x++) {
			inPattern = inPattern + Character.getNumericValue(c[x]);
		}
		/////////////////////////////////////////////
		String path = pattern+" <that> "+that+" <topic> "+topic;
		boolean p = remove(path, ROOT, 0);
		return(p);
	} 
	
	public static boolean remove(String sentence, Nodemapper parent, int depth) {
		StringTokenizer st = new StringTokenizer(sentence);
		int count = st.countTokens();
		return remove(st, parent, count, depth, sentence);
	} 
	
	public static boolean remove(StringTokenizer st, Nodemapper parent, int count, int depth, String sentence) {
		if (count == 0) {
			return true;
		} // if (count == 0)
		else {
			String word = st.nextToken();
			Nodemapper d;
			if (parent.containsKey(word))
				d = (Nodemapper)parent.get(word);
			else {
				return false; // the sentence was not in the graph
			} // else
			boolean p = remove(st, d, count-1, depth+1, sentence);
			if (p && d.keySet().size()==1)
				parent.put(word, null);  // should be parent.remove(word);
			return p;
		} 
	} 
	
	/**
	 * <p>
	 * The main entry into the Graphmaster thread.
	 * You must feed it with the initial startup file (Startup.aiml).
	 * </p>
	 */

	public void run() {
                Globals._version = VERSION + " Build("+BUILD+")";  //4.0.3 b4

		Graphmaster.load(Globals.getBotFile());
		Graphmaster.load(Globals.getTargetFile());
                Globals._size    = String.valueOf(Graphmaster.TOTAL_CATEGORIES); //4.0.3 b4

                System.out.println(COPYRIGHT+" Version "+Globals.getversion()); //4.1.1 b3

//4.1.1 b14     System.out.println(Globals.getBotName() + " is thinking with " + Globals.getsize() + "."); //4.0.3 b4 
                System.out.println(Globals.getValue("name") + " is thinking with " + Globals.getsize() + "."); //4.0.3 b4 


                loadtime = false; //4.1.1 b8 PEC 09-2001 Mark the end of the Load Time

                /*Remove 4.0.3 b2 PEC 09-2001
		System.out.println("Try http://localhost:2001 for server");
                */
		//System.out.println("Try http://localhost:2002 for admin");
		try {
                        System.out.println("Try http://" + InetAddress.getLocalHost().getHostAddress() +":2001"); //Change 4.0.3 b2
		} catch (Exception e) {}

                //Clean up all previous topics on startup (4.1.1 b4 PEC 09-2001)
                Classifier.cleanValue("topic","");

                if (Globals.isWatcherActive()) {  //4.1.2 b1 Watcher is optional
                   // This is a simple Alice shell that is useful for debugging your AIML
                   AIMLWatcher watcher = new AIMLWatcher();
                   watcher.start();
                }

		if (Globals.showShell()) {
			
			System.out.println("Type 'exit' to shutdown server");
					
			try {
				// 02-Sep-01: NB: Replaced deprecated DataInputStream with BufferedInputStream
				//DataInputStream din = new DataInputStream(System.in);
                                BufferedReader din = new BufferedReader(new InputStreamReader(System.in));
				
				String name = Substituter.formal(Classifier.getValue("name", "localhost"));
				
				if (name.equals("") || name.equals("?")) name = "localhost";
				
				System.out.print(name + "> ");
				
				while (true) {
					
					String theLine = din.readLine();
                                        /* Remove 4.0.3 b2 PEC 09-2001
					System.out.println("theLine is " + theLine);
                                        */
					String response = "";
					
					if (theLine == null) break;  // end of stream
					
					if (theLine.toLowerCase().equals("exit") || theLine.toLowerCase().equals("bye") || theLine.toLowerCase().equals("shutdown")) System.exit(0); // . on line by itself
					
					if (theLine.toLowerCase().equals("startup")) break;
					
					if (theLine.toLowerCase().equals("connect to localhost")) Classifier.setValue("connect", "localhost", "localhost");
					
					String connection = Classifier.getValue("connect", "localhost");
					
					connection = Substituter.replace("-", ".", connection);
					
                                        if ( (connection.equals("") ) ||
                                             (connection.equals("localhost")) ||
                                             (connection.equals(Globals.EmptyDefault)) ) {  //4.1.2 b2
						
						// ask local alicebot
						response = Classifier.doResponse(theLine, "localhost", new TextResponder());
						
					} else {
						
						// ask remote alicebot
						
					}
					
                                        //4.1.1 b14 --> System.out.print(Globals.getBotName()+ "> " + response + "\n");

                                        /**
                                          Add a little filter to remove the annoying <br /> tag
                                          from the console. A more elegant address for the issue should
                                          be to handle the tag at the parser, but it's not AIML and
                                          that would broke HTML responses so it's implemented as a
                                          quick hack here. 4.1.1 b14 */
                                        if (!Globals.showConsole()) {
                                           response = Toolkit.fixBR(response);
                                        }
                                        System.out.print(Globals.getValue("name")+ "> " + response + "\n");  


					// Synthesizer.speak(response);
					
					name = Substituter.formal(Classifier.getValue("name", "localhost"));
					
					if (name.equals("") || name.equals("?")) name = "localhost";
					
					System.out.print(name + "> ");
					
				}    
				
			}
			
			catch (IOException e) {
				
				System.err.println(e);   
				
			}
			
		}
		
	}
	
	public static Set ACTIVATED_NODES = new HashSet();
	
	public static void checkpoint() {
		try {
			PrintStream ps = new PrintStream(new FileOutputStream("targets/TARGETS.data"), true);
			Iterator x = ACTIVATED_NODES.iterator();
			Set Z = new TreeSet();
			while (x.hasNext()) {
				Nodemapper n = (Nodemapper)x.next();
				String template = (String)n.get("<template>");
				if (template.indexOf("<sr") < 0 || template.indexOf("FIND") >= 0)
				{
					Set S = (Set)n.get("<activations>");
					Iterator y = S.iterator();
					while (y.hasNext()) {
						String path = (String)y.next();
						String pattern = path.substring(0, path.indexOf(":")-1);
        					pattern = pattern.trim();
						if (pattern.indexOf("*") >= 0) {
							String input = path;
							while (input.indexOf(":") >= 0) {
								input = input.substring(input.indexOf(":")+1);
							}
							input = input.substring(0, input.indexOf("<")-1);
							input = input.trim();
							StringTokenizer st = new StringTokenizer(pattern);
							StringTokenizer ut = new StringTokenizer(input);
							String target="";
							while (st.hasMoreTokens()) {
								String s = st.nextToken();
								String u = ut.nextToken();
								target = target + " " +u;
							}
							if (ut.hasMoreTokens()) target = target + " *";
							target = target.trim();
							if (!pattern.startsWith("FIND ") &&
								!pattern.startsWith("MY NAME IS ")
								)
								Z.add(target);
						} 
					}
				} 
			} 
			x = Z.iterator();
			StringBuffer buffer = new StringBuffer();
			while (x.hasNext()) ps.println(x.next());
			ps.close();
			
			if (Z.size() > 10000) {
				x = ACTIVATED_NODES.iterator();
				while (x.hasNext()) {
					Nodemapper n = (Nodemapper)x.next();
					n.put("<activations>", null);
				}
				ACTIVATED_NODES = new TreeSet();
				
			} 
		} catch (Exception e) { System.out.println("CHECKPOINT "+e); }
	}
	
	public void restart() {
		ROOT = new Nodemaster();
		System.gc();
		TOTAL_CATEGORIES = 0;
		load("*");
	}
	
	public static void unload(String fname) {
		// To do
	}

        /**
        This method loads the Graphmaster with the content of the supplied
        file parsing the AIML content of it */

	public static void load(String fname) {

		if (fname.equals("*")) {
			
			File dir = new File(fname = "bots" + System.getProperty("file.separator") + Globals.getBotName());
			String[] allFiles = dir.list();
			
			for (int x = 0; x < allFiles.length; x++) {
				if (allFiles[x].endsWith(".aiml") && !allFiles[x].equals(Globals.getServerProps().getProperty("server.engine.startup"))) {
                                   load(allFiles[x]);
                                 }
			}
			
			return;
			
		}
		
		BufferedReader br = null;
		
		try {
                        boolean fload = true;

			if (fname.startsWith("http:") || fname.startsWith("Http:"))
			{
				URL url = new URL(fname);
				br = new BufferedReader(new InputStreamReader(url.openStream()));
			} else {
                                //Fix 4.0.3 b2 PEC 09-2001 Verify if the file exists, could be read
                                //and have a length > 0
                                //Fix 4.0.3 b8 moved here not to break remote loading
                                File AIMLFile = new File(fname);
                                if ((AIMLFile.length()   >  0   ) &&
                                    (AIMLFile.exists()   == true) &&
                                    (AIMLFile.canRead()  == true)) {

                                    // File is alright to load
                                    FileInputStream fis = new FileInputStream(fname); 
                                    InputStreamReader isr = new InputStreamReader(fis);
                                    br = new BufferedReader(isr);

                                } else {
                                  System.out.println("*** FILE("+fname+") can not be opened, ignored");
                                  fload = false;
                                }
                                AIMLFile = null;
                                //End of Fix
			}

//Fix 4.0.3 b8 PEC 09-2001
                        if ( fload == true ) {
                           AIMLReader alice = new AIMLReader(fname, br, new AIMLLoader(fname));
                           alice.read();
                        }
//End of Fix
		} 
		catch (Exception e) {
			e.printStackTrace();
		}	
		
        }
}
