/*
    Alicebot Program D
    Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

/*
    Code cleanup (4.1.3 [00] - October 2001, Noel Bush)
    - formatting cleanup
    - general grammar fixes
    - complete javadoc
    - made all imports explicit
    - removed/commented out all unused constants
    - removed code fragments whose results weren't being used
    - put some constants into action
    - removed restart method
    - replaced some uses of String with StringBuffers (in checkpoint)
    - renamed single-letter variables with more informative names
    - removed debugging messages (even those commented out!)
    - deprecated inefficient add methods and inserted a new one for future use
    - changed class to implement Runnable rather than extend Thread (since it does
      not significantly implement new behavior for Thread)
    - changed all file path handling to expect/use canonical
    - removed note about http server (doesn't belong here -- went to AliceServer)
    - removed loading from here (went to AliceServer)
*/

/*
    Further fixes and optimizations (4.1.3 [01] - November 2001, Noel Bush)
    - fixed bug in which values matched by _ failed to be captured
    - removed MASTER_MATCH (and corresponding dependencies elsewhere)
    - sorted constants better
    - removed HNAME, UNDEFINED, SUBGRAPH, TERMINAL,
      TOTAL_NODE_COUNT, TERMINAL_COUNT, FILENAME_SET, CURRENT_TOPIC (unused)
    - removed deprecated methods already
    - cleaned up match state tracking (use one int set to a state constant
      instead of three booleans!!!)
    - more adjustment of variables: now only actual constants are public
    - added getTotalCategories() method (otherwise could have been changed outside)
    - reworked load() to (hopefully) do a better job with paths
*/

/*
    More fixes (4.1.3 [02] - November 2001, Noel Bush
    - made match() ensure no empty that or topic
    - added use of NoMatchException
    - removed notice of AIMLWatcher status from here (to AliceServer)
    - load method now adds file to AIMLWatcher
*/

/*
    Targeting re-integration (4.1.3 [04] - December 2001, Noel Bush)
*/

/*
    4.1.4 [00] - December 2001, Noel Bush
    - changed to use PredicateMaster
    - removed some more unneeded fields (and imports)
    - moved shell out to its own class
*/

/*
	4.1.5 - Noel Bush
	- added support for nodemapper height (per Richard Wallace's suggestion)
*/


package org.alicebot.server.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.alicebot.server.core.loader.AIMLLoader;
import org.alicebot.server.core.loader.AIMLWatcher;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.Trace;
import org.alicebot.server.core.node.Nodemapper;
import org.alicebot.server.core.node.Nodemaster;
import org.alicebot.server.core.parser.AIMLReader;
import org.alicebot.server.core.responder.Responder;
import org.alicebot.server.core.responder.TextResponder;
import org.alicebot.server.core.targeting.TargetMaster;
import org.alicebot.server.core.util.Match;
import org.alicebot.server.core.util.NoMatchException;
import org.alicebot.server.core.util.Shell;
import org.alicebot.server.core.util.Substituter;
import org.alicebot.server.core.util.Toolkit;


/**
 *  <p>
 *  The <code>Graphmaster</code> is the &quot;brain&quot; of an Alicebot.  
 *  It consists of a collection of nodes called <code>Nodemapper</code>s.
 *  These <code>Nodemapper</code>s map the branches from each node.
 *  The branches are either single words or wildcards.
 *  </p>
 *  <p>
 *  The root of the <code>Graphmaster</code> is a <code>Nodemapper</code>
 *  with many branches, one for each of the first words of all the patterns
 *  (40,000 in the case of the A.L.I.C.E. brain). The number of leaf nodes
 *  in the graph is equal to the number of categories, and each leaf node
 *  contains the &lt;template&gt; tag.
 *  </p>
 *
 *  @author Richard Wallace, Jon Baer
 *  @author Thomas Ringate/Pedro Colla
 *  @author Noel Bush
 *
 *  @version 4.1.5
 */
public class Graphmaster
{
    // Public access informational constants.

    /** Copyright notice. */
    public static final String[] COPYRIGHT = {"Alicebot Program D (c) 1995-2002 A.L.I.C.E. AI Foundation",
                                              "All Rights Reserved.",
                                              "This program is free software; you can redistribute it and/or",
                                              "modify it under the terms of the GNU General Public License",
                                              "as published by the Free Software Foundation; either version 2",
                                              "of the License, or (at your option) any later version."};

    /** Version of this package. */
    public static final String VERSION     = "4.1.5";

    /** Build Number of this package (internal regression test control). */
    public static final String BUILD       = "00";


    // Public access convenience constants.

    /** A template marker. */
    public static final String TEMPLATE       = "<template>";

    /** A that marker. */
    public static final String THAT           = "<that>";

    /** A topic marker. */
    public static final String TOPIC          = "<topic>";

    /** A botid marker. */
    public static final String BOTID          = "<botid>";

    /** A filename marker. */
    public static final String FILENAME       = "<filename>";

    /** The <code>*</code> wildcard. */
    public static final String ASTERISK       = "*";

    /** The <code>_</code> wildcard. */
    public static final String UNDERSCORE     = "_";

    /** A path separator. */
    public static final String PATH_SEPARATOR = ":";
    

    
    // Private convenience constants.

    /** An empty string. */
    private static final String EMPTY_STRING = "";

    /** The start of a marker. */
    private static final String MARKER_START = "<";

    /** The end of a marker. */
    private static final String MARKER_END = ">";

    /** A space. */
    private static final String SPACE = " ";


    // State constants

    /** Match state: in <code>input</code> portion of path. */
    private static final int S_INPUT = 0;

    /** Match state: in <code>that</code> portion of path. */
    private static final int S_THAT  = 1;

    /** Match state: in <code>topic</code> portion of path. */
    private static final int S_TOPIC = 2;

    /** Match state: in <code>botid</code> portion of path. */
    private static final int S_BOTID = 3;


    // Class variables.

    /** The root {@link Nodemaster}. */
    private static Nodemapper ROOT = new Nodemaster(); 

    /** The total number of categories read. */
    private static int TOTAL_CATEGORIES = 0;

    /** The pattern vocabulary. */
    private static TreeSet patternVocabulary = new TreeSet();

    /** Load time marker. */
    private static boolean loadtime = true;

    /** Indicates whether the startup file has been loaded. */
    private static boolean startupLoaded = false;

    /** The current working directory (used by load). */
    private static Stack WORKING_DIRECTORY = new Stack();
    static
    {
        WORKING_DIRECTORY.push(System.getProperty("user.dir"));
    }


    // Constants used by targeting.

    /** Set of activated nodes. */
    private static Set ACTIVATED_NODES = new HashSet();

    /** Activations marker. */
    public static final String ACTIVATIONS = "<activations>";

    /** The response timeout. */
    protected static int RESPONSE_TIMEOUT;
    static
    {
        try
        {
            RESPONSE_TIMEOUT = Integer.parseInt(Globals.getProperty("programd.response-timeout", "1000"));
        }
        catch (NumberFormatException e)
        {
            RESPONSE_TIMEOUT = 1000;
        }
    }


    /**
     *  Prevents external creation of a new <code>Graphmaster</code>.
     */
    private Graphmaster()
    {
    }


    /**
     *  Adds a new pattern-that-topic path to the <code>Graphmaster</code> root.
     *
     *  @param pattern  &lt;pattern/&gt; path component
     *  @param that     &lt;that/&gt; path component
     *  @param topic    &lt;topic/&gt; path component
     *  @param botid
     *
     *  @return <code>Nodemapper</code> which is the result of adding the path.
     */
    public static Nodemapper add(String pattern, String that, String topic, String botid)
    {
        ArrayList path = Toolkit.wordSplit(pattern);
        path.add(THAT);
        path.addAll(Toolkit.wordSplit(that));
        path.add(TOPIC);
        path.addAll(Toolkit.wordSplit(topic));
        path.add(BOTID);
        path.add(botid);

        Nodemapper node = add(path.listIterator(), ROOT);

        return(node);
    } 


    /**
     *  Adds a new path to the <code>Graphmaster</code> at a given node.
     *
     *  @since  4.1.3
     *
     *  @param pathIterator an iterator over the List containing the elements of the path
     *  @param parent       the <code>Nodemapper</code> parent to which the child should be appended
     *
     *  @return <code>Nodemapper</code> which is the result of adding the node
     */
    private static Nodemapper add(ListIterator pathIterator, Nodemapper parent)
    {
        // If there are no more words in the path, return the parent node
        if (!pathIterator.hasNext())
        {
            parent.setTop();
            return parent;
        }
        else
        {
            // Otherwise, get the next word.
            String word = (String)pathIterator.next();

            Nodemapper node;

            // If the parent contains this word, get the node with the word.
            if (parent.containsKey(word))
            {
                node = (Nodemapper)parent.get(word);
            }
            // Otherwise create a new node with this word.
            else
            {
                node = new Nodemaster();
                parent.put(word, node);
                node.setParent(parent);
            }

            // Add the word to the pattern vocabulary.
            patternVocabulary.add(word);

            // Return the result of adding the new node to the parent.
            return add(pathIterator, node);
        } 
    }


    /**
     *  Removes a node, as well as many of its ancestors as
     *  have no descendants other than this node or its ancestors.
     *
     *  @param nodemapper   the mapper for the node to remove
     */
    private static void remove(Nodemapper nodemapper)
    {
        Nodemapper parent = nodemapper.getParent();
        if (parent != null)
        {
            parent.remove(nodemapper);
            if (parent.size() == 0)
            {
                remove(parent);
            }
        }
        nodemapper = null;
    }

    
    /**
     *  <p>
     *  Searches for a match in the <code>Graphmaster</code> to a given path.
     *  </p>
     *  <p>
     *  This is a high-level prototype, used for external access.  It is not synchronized!
     *  </p>
     *
     *  @see #match(Nodemapper, Nodemapper, String, String, String, boolean, boolean, boolean)
     *
     *  @param input    &lt;input/&gt; path component
     *  @param that     &lt;that/&gt; path component
     *  @param topic    &lt;topic/&gt; path component
     *  @param botid    &lt;botid/&gt; path component
     *
     *  @return the resulting <code>Match</code> object
     *
     *  @throws NoMatchException if no match was found
     */
    public static Match match(String input, String that, String topic, String botid) throws NoMatchException
    {
        // Compose the input path. Fill in asterisks for empty values.
        ArrayList inputPath;

        // Input text part.
        if (input.length() > 0)
        {
            inputPath = Toolkit.wordSplit(input);
        }
        else
        {
            inputPath = new ArrayList();
            inputPath.add(ASTERISK);
        }

        // <that> marker.
        inputPath.add(THAT);

        // Input <that> part.
        if (that.length() > 0 )
        {
            inputPath.addAll(Toolkit.wordSplit(that));
        }
        else
        {
            inputPath.add(ASTERISK);
        }

        // <topic> marker.
        inputPath.add(TOPIC);

        // Input <topic> part.
        if (topic.length() > 0 )
        {
            inputPath.addAll(Toolkit.wordSplit(topic));
        }
        else
        {
            inputPath.add(ASTERISK);
        }

        // <botid> marker.
        inputPath.add(BOTID);

        // Input [directed to] botid.
        inputPath.add(botid);
        
        // Get the match, starting at the root, with an empty star and path, starting in "in input" mode.
        Match match = match(ROOT, ROOT, inputPath, EMPTY_STRING, new StringBuffer(),
                            S_INPUT, System.currentTimeMillis() + RESPONSE_TIMEOUT);

        // Return it if not null; throw an exception if null.
        if (match != null)
        {
            return match;
        }
        else
        {
            Trace.devinfo("Match is null.");
            throw new NoMatchException(input);
        }
    }


    /**
     *  <p>
     *  Searches for a match in the <code>Graphmaster</code> to a given path.
     *  </p>
     *  <p>
     *  This is a low-level prototype, used for internal recursion.
     *  </p>
     *
     *  @see #match(String, String, String)
     *
     *  @param nodemapper   the nodemapper where we start matching
     *  @param parent       the parent of the nodemapper where we start matching
     *  @param input        the input path (possibly a sublist of the original)
     *  @param star         contents absorbed by a wildcard
     *  @param path         the path matched so far
     *  @param matchState   state variable tracking which part of the path we're in
     *  @param expiration	when this response process expires
     *
     *  @return the resulting <code>Match</code> object
     */
    private static Match match(Nodemapper nodemapper, Nodemapper parent,
                               List input, String star, StringBuffer path,
                               int matchState, long expiration)
    {
        // Return null if expiration has been reached.
        if (System.currentTimeMillis() >= expiration)
        {
            return null;
        }
        
        // Halt matching if this node is higher than the length of the input.
        if (input.size() < nodemapper.getHeight())
        {
            return null;
        }
        
        // The match object that will be returned.
        Match match;

        // If no more tokens in the input, see if this is a template.
        if (input.size() == 0)
        {
            // If so, the star is from the topic, and the path component is the topic.
            if (nodemapper.containsKey(TEMPLATE))
            {
                //Trace.devinfo("nodemapper contains template");
                match = new Match();
                match.setBotID(path.toString());
                match.setNodemapper(nodemapper);
                return match;
            }
            else
            {
                return null;
            }
        }

		// Take the first word of the input as the head.
        String head = ((String)input.get(0)).trim();
        
        // Take the rest as the tail.
        List tail = input.subList(1, input.size());
        
        /*
            See if this nodemapper has a _ wildcard.
            _ comes first in the AIML "alphabet".
        */
        if (nodemapper.containsKey(UNDERSCORE))
        {
            // If so, construct a new path from the current path plus a _ wildcard.
            StringBuffer newPath = new StringBuffer();
            synchronized(newPath)
            {
                newPath.append(path);
                newPath.append(' ');
                newPath.append('_');
            }
            
            // Try to get a match with the tail and this new path, using the head as the star.
            match = match((Nodemapper)nodemapper.get(UNDERSCORE), nodemapper,
            			  tail, head, newPath, matchState, expiration);

			// If that did result in a match,
            if (match != null)
            {
                // capture and push the star content appropriate to the current match state.
                switch (matchState)
                {
                    case S_INPUT :
                        if (star.length() > 0)
                        {
                            match.pushInputStar(star);
                        }
                        break;
                    
                    case S_THAT :
                        if (star.length() > 0)
                        {
                            match.pushThatStar(star);
                        }
                        break;

                    case S_TOPIC :
                        if (star.length() > 0)
                        {
                            match.pushTopicStar(star);
                        }
                        break;
                }
                // ...and return this match.
                return match;
            }
        }

        /*
            The nodemapper may have contained a _, but this led to no match.
            Or it didn't contain a _ at all.
        */
        if (nodemapper.containsKey(head))
        {
            /*
                Check now whether this is a marker for the <that>,
                <topic> or <botid> segments of the path.  If it is,
                set the match state variable accordingly.
            */
            if (head.startsWith(MARKER_START))
            {
                if (head.equals(THAT))
                {
                    matchState = S_THAT;
                }
                else if (head.equals(TOPIC))
                {
                    matchState = S_TOPIC;
                }
                else if (head.equals(BOTID))
                {
                    matchState = S_BOTID;
                }

                // Now try to get a match using the tail and an empty star and empty path.
                match = match((Nodemapper)nodemapper.get(head), nodemapper,
                              tail, EMPTY_STRING, new StringBuffer(), matchState, expiration);
                
                // If that did result in a match,
                if (match != null)
                {
                    // capture and push the star content appropriate to the *previous* match state.
                    switch (matchState)
                    {
                        case S_THAT :
                            if (star.length() > 0)
                            {
                                match.pushInputStar(star);
                            }
                            // Remember the pattern segment of the matched path.
                            match.setPattern(path.toString());
                            break;

                        case S_TOPIC :
                            if (star.length() > 0)
                            {
                                match.pushThatStar(star);
                            }
                            // Remember the that segment of the matched path.
                            match.setThat(path.toString());
                            break;

                        case S_BOTID :
                            if (star.length() > 0)
                            {
                                match.pushTopicStar(star);
                            }
                            // Remember the topic segment of the matched path.
                            match.setTopic(path.toString());
                            break;

                    }
                    // ...and return this match.
                    return match;
                }
            }
            /*
                In the case that the nodemapper contained the head,
                but the head was not a marker, it must be that the
                head is a regular word.  So try to match the rest of the path.
            */
            else
            {
                // Construct a new path from the current path plus the head.
                StringBuffer newPath = new StringBuffer();
                synchronized(newPath)
                {
                    newPath.append(path);
                    newPath.append(' ');
                    newPath.append(head);
                }
                
                // Try to get a match with the tail and this path, using the current star.
                match = match((Nodemapper)nodemapper.get(head), nodemapper,
                              tail, star, newPath, matchState, expiration);

                // If that did result in a match, just return it.
                if (match != null)
                {
                    return match;
                }
            }
        }
        
        /*
            The nodemapper may have contained the head, but this led to no match.
            Or it didn't contain the head at all.  In any case, check to see if
            it contains a * wildcard.  * comes last in the AIML "alphabet".
        */
        if (nodemapper.containsKey(ASTERISK))
        {
            // If so, construct a new path from the current path plus a * wildcard.
            StringBuffer newPath = new StringBuffer();
            synchronized(newPath)
            {
                newPath.append(path);
                newPath.append(' ');
                newPath.append('*');
            }
            
            // Try to get a match with the tail and this new path, using the head as the star.
            match = match((Nodemapper)nodemapper.get(ASTERISK), nodemapper,
                          tail, head, newPath, matchState, expiration);

            // If that did result in a match,
            if (match != null)
            {
                // capture and push the star content appropriate to the current match state.
                switch (matchState)
                {
                    case S_INPUT :
                        if (star.length() > 0)
                        {
                            match.pushInputStar(star);
                        }
                        break;

                    case S_THAT :
                        if (star.length() > 0)
                        {
                            match.pushThatStar(star);
                        }
                        break;

                    case S_TOPIC :
                        if (star.length() > 0)
                        {
                            match.pushTopicStar(star);
                        }
                        break;
                }
                // ...and return this match.
                return match;
            }
        }

        /*
            The nodemapper has failed to match at all: it contains neither _, nor the head,
            nor *.  However, if it itself is a wildcard, then the match continues to be
            valid and can proceed with the tail, the current path, and the star content plus
            the head as the new star.
        */
        if (nodemapper.equals(parent.get(ASTERISK)) || nodemapper.equals(parent.get(UNDERSCORE)))
        {
            return match(nodemapper, parent, tail, star + SPACE + head, path, matchState, expiration);
        }

        /*
        	If we get here, we've hit a dead end; this null match will be passed back up the
        	recursive chain of matches, perhaps even hitting the high-level match method
        	(which will react by throwing a NoMatchException), though this is assumed to be
        	the rarest occurence.
        */
        return null;
    }



    /**
     *  Marks the end of loadtime.  Depending on settings in {@link Globals},
     *  displays various trace information on the console,
     *  and writes startup information to the log..
     */
    public static void markReady()
    {
        // Mark the end of the load time.
        loadtime = false;

        // Display some console information (if set).
        if (Globals.showConsole())
        {
            // Give number of loaded categories.
            Log.userinfo(Bots.getCount() + " bots thinking with " + TOTAL_CATEGORIES + " categories.", Log.STARTUP);
        }

        // Always print the copyright, regardless of console settings.
        Trace.insist(COPYRIGHT);
        Log.userinfo("Alicebot Program D version " + VERSION + " Build [" + BUILD + "]", Log.STARTUP);
    }
    

    /**
     *  Tells the PredicateMaster to
     *  save all predicates.
     */
    public static void shutdown()
    {
        PredicateMaster.saveAll();
    }


    /**
     *  Adds a given Nodemapper to the set of activated nodes.
     */
    public static void activatedNode(Nodemapper nodemapper)
    {
        ACTIVATED_NODES.add(nodemapper);
    }


    /**
     *  Sends new targeting data to
     *  {@link org.alicebot.server.core.util.TargetMaster}.
     */
    public static void checkpoint()
    {
        // Log this checkpoint event.
        Log.log("Targeting checkpoint.", Log.TARGETING);

        Iterator activatedNodesIterator = ACTIVATED_NODES.iterator();
        while (activatedNodesIterator.hasNext())
        {
            Nodemapper nodemapper = (Nodemapper)activatedNodesIterator.next();
            Set activations = (Set)nodemapper.get(ACTIVATIONS);
            Iterator activationsIterator = activations.iterator();
            while (activationsIterator.hasNext())
            {
                String path = (String)activationsIterator.next();
                StringTokenizer pathTokenizer = new StringTokenizer(path, PATH_SEPARATOR);
                // int tokenCount = pathTokenizer.countTokens();
                
                String matchPattern = pathTokenizer.nextToken().trim();
                String matchThat = pathTokenizer.nextToken().trim();
                String matchTopic = pathTokenizer.nextToken().trim();
                /* String matchBot = */ pathTokenizer.nextToken().trim();
                String matchTemplate = (String)nodemapper.get(TEMPLATE);
                String inputText = pathTokenizer.nextToken().trim();
                String inputThat = pathTokenizer.nextToken().trim();
                String inputTopic = pathTokenizer.nextToken().trim();
                /* String inputBot = */ pathTokenizer.nextToken().trim();
                String response = pathTokenizer.nextToken().trim();
                TargetMaster.add(matchPattern, matchThat, matchTopic, matchTemplate,
                                 inputText, inputThat, inputTopic, response);
                activationsIterator.remove();
            } 
        }
    }
    

    /**
     *  Loads the <code>Graphmaster</code> with the contents of a given path.
     *
     *  @param path path to the file(s) to load
     *  @param botid
     */
    public static void load(String path, String botid)
    {
        // Check for obviously invalid paths of zero length.
        if (path.length() < 1)
        {
            Log.userinfo("Cannot open a file whose name has zero length.", Log.ERROR);
        }

        Bot bot = null;
        boolean localFile = true;

        // Don't reload the startup file.
        if (path.equals(Globals.getStartupFilePath()))
        {
            if (startupLoaded)
            {
                Log.userinfo("Cannot reload startup file.", Log.ERROR);
            }
            else
            {
                startupLoaded = true;
                Log.userinfo("Starting up with \"" + path + "\".", Log.STARTUP);
            }
        }
        else
        {
            bot = Bots.getBot(botid);
        }

        // This BufferedReader will be passed to AIMLReader to read the file.
        BufferedReader buffReader = null;

        // Guess if this is a URL.
        if (path.indexOf("://") != -1)
        {
            // Try to create this as a URL.
            URL url = null;

            try
            {
                url = new URL(path);
            }
            catch (MalformedURLException e)
            {
                Log.userinfo("Malformed URL: \"" + path + "\"", Log.ERROR);
            }

            try
            {
                String encoding = Toolkit.getDeclaredXMLEncoding(url.openStream());
                buffReader = new BufferedReader(new InputStreamReader(url.openStream(), encoding));
            }
            catch (IOException e)
            {
                Log.userinfo("I/O error trying to read \"" + path + "\"", Log.ERROR);
            }

            if (!loadCheck(url, bot))
            {
                return;
            }
            localFile = false;
        }

        // Handle paths which are apparently files.
        else
        {
            if (path.indexOf(ASTERISK) != -1)
            {
                //Trace.devinfo(path + " looks like a list of files.");
                String[] files = null;

                try
                {
                    files = Toolkit.glob(path, (String)WORKING_DIRECTORY.peek());
                }
                catch (FileNotFoundException e)
                {
                    Log.userinfo(e.getMessage(), Log.ERROR);
                }
                if (files != null)
                {
                    //Trace.devinfo("Found them using wd " + WORKING_DIRECTORY.peek());
                    for (int index = files.length; --index >= 0; )
                    {
                        load(files[index], botid);
                    }
                    return;
                }
            }

            File toRead = new File(path);
            if (!toRead.exists())
            {
                //Trace.devinfo(path + " does not exist; trying " + WORKING_DIRECTORY.peek() + File.separator + path);
                toRead = new File(WORKING_DIRECTORY.peek() + File.separator + path);
                if (toRead.exists())
                {
                    path = WORKING_DIRECTORY.peek() + File.separator + path;
                    //Trace.devinfo(path + " exists.");
                }
                else
                {
                    Trace.userinfo("Couldn't find \"" + path + "\".");
                    //Trace.devinfo("wd: " + WORKING_DIRECTORY.toString());
                    return;
                }
            }

            if (toRead.exists() && !toRead.isDirectory())
            {
                if (!loadCheck(toRead, bot))
                {
                    return;
                }

                try
                {
                    String encoding = Toolkit.getDeclaredXMLEncoding(new FileInputStream(path));
                    buffReader = new BufferedReader(
                                    new InputStreamReader(
                                        new FileInputStream(path), encoding));
                }
                catch (IOException e)
                {
                    Log.userinfo("I/O error trying to read \"" + path + "\"", Log.ERROR);
                    return;
                }
                // Add it to the AIMLWatcher, if active.
                if (Globals.isWatcherActive())
                {
                    AIMLWatcher.addWatchFile(path, botid);
                }
    	    	WORKING_DIRECTORY.push(toRead.getParent());
	        	//Trace.devinfo("Pushed a wd; now: " + WORKING_DIRECTORY.toString());
            }
            else
            {
                if (!toRead.exists())
                {
                    Log.userinfo("\"" + path + "\" does not exist!", Log.ERROR);
                }
                if (toRead.isDirectory())
                {
                    Log.userinfo("\"" + path + "\" is a directory!", Log.ERROR);
                }
            }
        }
        new AIMLReader(path, buffReader, new AIMLLoader(path, botid), Globals.warnAboutDeprecatedTags()).read();

		if (localFile)
		{
            if (WORKING_DIRECTORY.size() > 1)
            {
            	//Trace.devinfo("Going to pop off a wd from: " + WORKING_DIRECTORY.toString());
            	WORKING_DIRECTORY.pop();
            	//Trace.devinfo("wd now: " + WORKING_DIRECTORY.toString());
            }
		}
    }


    /**
     *  Tracks/checks whether a given path should be loaded, depending
     *  on whether or not it's currently &quot;loadtime&quot;;
     *  if the file has already been loaded and is allowed to be
     *  reloaded, unloads the file first.  A null value for bot
     *  causes this to return true!!!
     *
     *  @param path
     *  @param botid
     */
    private static boolean loadCheck(Object path, Bot bot)
    {
        if (bot == null)
        {
            return true;
        }

        HashMap loadedFiles = bot.getLoadedFilesMap();

        if (loadedFiles.keySet().contains(path))
        {
            // At load time, don't load an already-loaded file.
            if (loadtime)
            {
                return false;
            }
            // At other times, unload the file before loading it again.
            else
            {
                unload(path, bot);
            }
        }
        else
        {
            loadedFiles.put(path, new HashSet());
        }
        return true;
    }


    /**
     *  Removes all nodes associated with a given filename,
     *  and removes the file from the list of loaded files.
     *
     *  @param filename the filename
     */
    public static void unload(Object path, Bot bot)
    {
        HashSet nodemappers = (HashSet)bot.getLoadedFilesMap().get(path);
        Iterator nodemapperIterator = nodemappers.iterator();

        while (nodemapperIterator.hasNext())
        {
            remove((Nodemapper)nodemapperIterator.next());
            TOTAL_CATEGORIES--;
        }

        //GraphPrinter.printXML(ROOT);
    }


    /**
     *  Returns the number of categories presently loaded.
     *
     *  @return the number of categories presently loaded
     */
    public static int getTotalCategories()
    {
        return TOTAL_CATEGORIES;
    }


    /**
     *  Increments the total categories.
     *
     *  @return the number of categories presently loaded
     */
    public static int incrementTotalCategories()
    {
        return TOTAL_CATEGORIES++;
    }


    /**
     *  Returns the pattern vocabulary size.
     *
     *  @return the pattern vocabulary size
     */
    public static int patternVocabularySize()
    {
        return patternVocabulary.size();
    }
    
    
    /**
     *  Returns the working directory.
     *
     *  @return the working directory
     */
    public static String getWorkingDirectory()
    {
        return (String)WORKING_DIRECTORY.peek();
    }
}
