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
 * @version 1.0
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
	
	/** The that pattern. */
	public static String THAT_PATTERN;
	
	/** The input pattern. */
	public static String INPUT_PATTERN;
	
	/** The topic pattern. */
	public static String TOPIC_PATTERN;
	
	/** The that star. */
	public static String THAT_STAR;
	
	/** The input star. */
	public static String INPUT_STAR;
	
	/** The input type. */
	public static String INPUT_TYPE;
	
	/** The input soundex. */
	public static String INPUT_SOUNDEX;
	
	/** The topic star. */
	public static String TOPIC_STAR;
	
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
	
	/** Server mode. */
	public static final int SERVER = 1;
	
	/** Console mode. */
	public static final int CONSOLE = 2;

//Add (4.0.3 b1) PEC 09-2001 Version & Build Global Values

        public static final String VERSION = "4.1.0";
        public static final String BUILD = "10";

//EAdd

	public Graphmaster() {}
	
	public Graphmaster(int mode) {
		this.MODE = mode;
	}
	
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
		Nodemapper node = add(path, ROOT, 0);
		return(node);
	} 
	
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
	
	public static Nodemapper match(String input, String that, String topic) {
		/////////////////////////////////////////////
		String inPattern = "";
		char[] c = (new String(input)).toCharArray();
		for (int x = 0; x < c.length; x++) {
			inPattern = inPattern + Character.getNumericValue(c[x]);
		}
		/////////////////////////////////////////////
		String path = input+" <that> "+that+" <topic> "+topic;
		Nodemapper node;
		node = match(ROOT, ROOT, path, "", "");
		path = INPUT_PATTERN + " : " + THAT_PATTERN + " : " + TOPIC_PATTERN + " : " + path;
		Set S = (Set)node.get("<activations>");
		if (S==null) S = new TreeSet();
		S.add(path);
		node.put("<activations>", S);
		ACTIVATED_NODES.add(node);
		checkpoint();
		return node;
	}
	
	public static Nodemapper
		match(Nodemapper node, Nodemapper parent,
		String input, String star, String path) {
		StringTokenizer st = new StringTokenizer(input);
		int count = st.countTokens();
		if (count == 0) {
			if (node.containsKey("<template>")) {
				TOPIC_STAR=star.trim();
				TOPIC_PATTERN=path.trim();
				return node;
			} // if
			else return null;
		} // if
		String word = st.nextToken(); String tail = "";
		if (st.hasMoreTokens())
			tail = input.substring(word.length()+1, input.length());
		Nodemapper rec;
		if (node.containsKey("_")) {
			rec = match((Nodemapper)node.get("_"), node, tail, word, path+" _");
			if (rec != null) return rec;
		} // if
		if (node.containsKey(word)) {
			if (word.startsWith("<")) 
				rec = match((Nodemapper)node.get(word), node, tail, "", "");
			else
				rec = match((Nodemapper)node.get(word), node, tail, star, path+" "+word);
			if (rec != null) {
				if (word.compareTo("<that>")==0) {
					INPUT_STAR = star.trim();
					INPUT_PATTERN = path.trim();
				} // if
				else if (word.compareTo("<topic>")==0) {
					THAT_STAR = star.trim();
					THAT_PATTERN = path.trim();
				}
				return rec;
			} 
		} 
		if (node.containsKey("*")) {
			rec = match((Nodemapper)node.get("*"), node, tail, word, path+" *");
			if (rec != null) return rec; 
		} 
		if (node.equals(parent.get("*")) || node.equals(parent.get("_"))) 
			return match(node, parent, tail, star+" "+word, path);
		return null;
	} 
	
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

                System.out.println("Program D Server Version "+Globals.getversion()+" is running..."); //4.0.3 b4
                System.out.println(Globals.getBotName() + " is thinking with " + Globals.getsize() + "."); //4.0.3 b4 

                /*Remove 4.0.3 b2 PEC 09-2001
		System.out.println("Try http://localhost:2001 for server");
                */
		//System.out.println("Try http://localhost:2002 for admin");
		try {
                        System.out.println("Try http://" + InetAddress.getLocalHost().getHostAddress() +":2001"); //Change 4.0.3 b2
		} catch (Exception e) {}

		// This is a simple Alice shell that is useful for debugging your AIML
		
		AIMLWatcher watcher = new AIMLWatcher();
		watcher.start();

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
					
					if (connection.equals("") || connection.equals("localhost")) {
						
						// ask local alicebot
						response = Classifier.doResponse(theLine, "localhost", new TextResponder());
						
					} else {
						
						// ask remote alicebot
						
					}
					
					System.out.print(Globals.getBotName()+ "> " + response + "\n");  
					
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
