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
    More fixes (4.1.3 [02] - November 2001, Noel Bush
    - changed *Predicate*() methods to *PredicateValue*()
    - added getInternalResponse(), getReply() and getReplies()
    - added createUser(), checkUser() and changePassword()
*/

/*
    4.1.4 [00] - December 2001, Noel Bush
    - moved all get/set methods to PredicateMaster
    - changed this now to require "save" and "load"
*/

/*
    4.1.5 - Noel Bush
    - made this an abstract class
*/

package org.alicebot.server.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.node.Nodemapper;
import org.alicebot.server.core.parser.TemplateParser;
import org.alicebot.server.core.parser.TemplateParserException;
import org.alicebot.server.core.processor.ProcessorException;
import org.alicebot.server.core.responder.Responder;
import org.alicebot.server.core.util.DeveloperError;
import org.alicebot.server.core.util.InputNormalizer;
import org.alicebot.server.core.util.Match;
import org.alicebot.server.core.util.NoMatchException;
import org.alicebot.server.core.util.Pulse;
import org.alicebot.server.core.util.Toolkit;
import org.alicebot.server.core.util.Trace;
import org.alicebot.server.core.util.UserError;


/**
 *  <p>
 *  &quot;To multiplex&quot; means &quot;to select one from many inputs&quot;.
 *  A <code>Multiplexor</code> multiplexes the clients of a bot
 *  and keeps track of all their predicate values.
 *  </p>
 *  <p>
 *  The following metaphor was supplied by Richard Wallace:
 *  The <code>Multiplexor</code> controls a
 *  short &quot;carnival ride&quot; for each user. The Multiplexor puts the
 *  client in his/her seat, hands him/her an id card, and closes the door.
 *  The client gets one &quot;turn of the crank&quot;.  He/she enters
 *  his/her id, multiline query, and then receives the reply.  The door opens,
 *  the Multiplexor ushers him/her out, and seats the next client.
 *  </p>
 *  <p>
 *  Historically, the functionality specified by this class was
 *  implemented in {@link Classifier}. However, {@link Classifier} evolved
 *  to include database access methods that were not desirable for all
 *  implementations. Furthermore, {@link Classifier} lost part of its
 *  original purpose as a &quot;classifier of user inputs into categories&quot;.
 *  Hence, the Program D {@link Classifier} has been left as-is, except it
 *  has been changed into an subclass of this abstract class. There are two
 *  new subclasses called {@link FlatFileMultiplexor} and {@link DBMultiplexor}.
 *  </p>
 *  <p>
 *  Starting in 4.1.5, this became an abstract class instead of an interface,
 *  and introduced the single public {@link #getResponse} method, rather than
 *  allowing multiple public methods for getting responses which can result in
 *  synchronization problems.  The former <code>AbstractClassifier</code> class
 *  was then removed.
 *  </p>
 *
 *  @since 4.1.3
 *
 *  @author Noel Bush
 *  @author Richard Wallace, Jon Baer
 *  @author Thomas Ringate/Pedro Colla
 *
 *  @see {@link FlatFileMultiplexor}
 *  @see {@link DBMultiplexor}
 */
abstract public class Multiplexor
{
    // Convenience constants.

    /** The name of the <code>that</code> special predicate. */
    protected static final String THAT = "that";

    /** The name of the <code>topic</code> special predicate. */
    protected static final String TOPIC = "topic";

    /** The name of the <code>input</code> special predicate. */
    protected static final String INPUT = "input";

    /** The name of the <code>star</code> special predicate. */
    protected static final String STAR = "star";

    /** An empty string. */
    protected static final String EMPTY_STRING = "";

    /** A space. */
    protected static final String SPACE = " ";

    /** The word &quot;value&quot;. */
    protected static final String VALUE = "value";

    /** An asterisk (used in String production) */
    protected static final String ASTERISK = "*";

    /** A quote mark. */
    protected static final String QUOTE_MARK = "\"";

    /** The string &quot;Match:&quot;. */
    private static final String LABEL_MATCH = "Match: ";

    /** The string &quot;Filename:&quot;. */
    private static final String LABEL_FILENAME = "Filename: ";

    /** The string &quot;Response &quot;. */
    private static final String RESPONSE_SPACE = "Response ";

    /** The string &quot; in &quot;. */
    private static final String SPACE_IN_SPACE = " in ";

    /** The string &quot; ms. Average: &quot;. */
    private static final String MS_AVERAGE = " ms. (Average: ";

    /* The string &quot; ms).&quot; */
    private static final String MS = " ms.)";

    /** The host name. */
    protected static final String HOST_NAME = Globals.getHostName();

    /** Whether to show the console. */
    protected static final boolean SHOW_CONSOLE = Globals.showConsole();

    /** Whether to show the match trace. */
    protected static final boolean SHOW_MATCH_TRACE = Globals.showMatchTrace();

    /** Whether to use targeting. */
    protected static final boolean USE_TARGETING = Globals.useTargeting();

    /** The response-count period for invoking targeting. */
    private static final int TARGET_SKIP = Globals.getTargetSkip();

    /** The predicate empty default. */
    protected static final String PREDICATE_EMPTY_DEFAULT = Globals.getPredicateEmptyDefault();
    
    /** A secret key used for (weakly) authorizing authentication requests. */
    protected static String SECRET_KEY;


    // Class variables.

    /** The time that the Multiplexor started operation. */
    protected static long startTime = System.currentTimeMillis();

    /** A counter for tracking the number of responses produced. */
    protected static long responseCount = 0;
    
    /** The total response time. */
    protected static long totalTime = 0;

    /** A counter for tracking average response time. */
    protected static float avgResponseTime = 0;
    
    /** A proxy multiplexor used for instantiating match threads. */
    private static Multiplexor proxy;
    
    /** Will hold a set of Pulses. */
    private static ArrayList pulses = new ArrayList();


    /**
     *  Initializes the <code>Multiplexor</code>, creating the secret key
     *  that can be used for a weak form of authentication.
     */
    public void initialize()
    {
        SECRET_KEY = new Double(Math.random() * System.currentTimeMillis()).toString();
        File keyFile = new File("secret.key");
        keyFile.delete();
        try
        {
            keyFile.createNewFile();
        }
        catch (IOException e)
        {
            throw new UserError("Error creating secret key file.");
        }
        PrintWriter out;
        try
        {
            out = new PrintWriter(new FileOutputStream(keyFile));
        }
        catch (FileNotFoundException e)
        {
            throw new UserError("Error writing secret key file.");
        }
        out.print(SECRET_KEY);
        out.flush();
        out.close();
        
        // Initialize the proxy.
        proxy = ActiveMultiplexor.getInstance();
        
        // Add a simple IAmAlive Pulse (this should be more configurable).
        addPulse(new org.alicebot.server.core.util.IAmAlivePulse());
    }


    /**
     *  Returns the response to a non-internal input, using a Responder.
     *
     *  @param input        the &quot;non-internal&quot; (possibly multi-sentence, non-substituted) input
     *  @param userid       the userid for whom the response will be generated
     *  @param botid		the botid from which to get the response
     *  @param responder    the Responder who cares about this response
     */
    public static synchronized String getResponse(String input, String userid, String botid, Responder responder)
    {
        // Get the specified bot object.
        Bot bot = Bots.getBot(botid);
        
        // Split sentences (after performing substitutions and responder pre-processing).
        ArrayList sentenceList = bot.sentenceSplit(
                                    bot.applyInputSubstitutions(
                                        responder.preprocess(input, HOST_NAME)));
                                        
        // Get an iterator on the replies.
        Iterator replies = getReplies(sentenceList, userid, botid).iterator();

        // Start by assuming an empty response.
        String response = EMPTY_STRING;

        // Get an iterator over the input sentences.
        Iterator sentences = sentenceList.iterator();

        // For each input sentence...
        while (sentences.hasNext())
        {
            // ...ask the responder to append the reply to the response, and accumulate the result.
            response = responder.append((String)sentences.next(), (String)replies.next(), response);
        }

        // Log the response.
        responder.log(input, response, HOST_NAME, userid, botid);

        // Finally, ask the responder to postprocess the response, and return the result.
        response = responder.postprocess(response);

        // Return the response (may be just EMPTY_STRING!)
        return response;
    }
    
    
    /**
     *  <p>
     *  Produces a response to an &quot;internal&quot; input sentence -- i.e.,
     *  an input that has been produced by a <code>srai</code>.
     *  </p>
     *  <p>
     *  The main differences between this and
     *  {@link #getResponse(String,String,TemplateParser,boolean,Responder)}
     *  are that this method takes an already-existing <code>TemplateParser</code>, <i>doesn't</i>
     *  take a <code>Responder</code>, and assumes that the inputs have already been normalized.
     *  </p>
     *
     *  @see {@link #getResponse(String,String,TemplateParser,boolean,Responder)}
     *
     *  @param input    the input sentence
     *  @param userid   the userid requesting the response
     *  @param botid	the botid from which to get the response
     *  @param parser   the parser object to update when generating the response
     */
    public static String getInternalResponse(String input, String userid, String botid, TemplateParser parser)
    {
        // Get the requested bot.
        Bot bot = Bots.getBot(botid);
        
        // Ready the that and topic predicates for constructing the match path.        
        ArrayList thatSentences  = bot.sentenceSplit(PredicateMaster.get(THAT, 1, userid, botid));
        String that = InputNormalizer.patternFitIgnoreCase((String)thatSentences.get(thatSentences.size() - 1));

        if (that.equals(EMPTY_STRING) || that.equals(PREDICATE_EMPTY_DEFAULT) )
        {
            that = ASTERISK;
        }

        String topic = InputNormalizer.patternFitIgnoreCase(PredicateMaster.get(TOPIC, userid, botid));
        if (topic.equals(EMPTY_STRING) || topic.equals(PREDICATE_EMPTY_DEFAULT) )
        {
            topic = ASTERISK;
        }

        return getMatchResult(input, that, topic, userid, botid, parser);
    }


    /**
     *  Gets the list of replies to some input sentences.
     *  Assumes that the sentences have already had all necessary
     *  pre-processing and substitutions performed.
     *
     *  @param sentenceList  the input sentences
     *  @param userid        the userid requesting the replies
     *  @param botid
     *
     *  @return the list of replies to the input sentences
     */
    private static ArrayList getReplies(ArrayList sentenceList, String userid, String botid)
    {
        // All replies will be assembled in this ArrayList.
        ArrayList replies = new ArrayList(sentenceList.size());

        // Get the requested bot.
        Bot bot = Bots.getBot(botid);
        
        // Ready the that and topic predicates for constructing the match path.
        ArrayList thatSentences  = bot.sentenceSplit(PredicateMaster.get(THAT, 1, userid, botid));
        String that = InputNormalizer.patternFitIgnoreCase((String)thatSentences.get(thatSentences.size() - 1));

        if (that.equals(EMPTY_STRING) || that.equals(PREDICATE_EMPTY_DEFAULT) )
        {
            that = ASTERISK;
        }

        String topic = InputNormalizer.patternFitIgnoreCase(PredicateMaster.get(TOPIC, userid, botid));
        if (topic.equals(EMPTY_STRING) || topic.equals(PREDICATE_EMPTY_DEFAULT) )
        {
            topic = ASTERISK;
        }

        Iterator sentences = sentenceList.iterator();

        // We might use this to track matching statistics.
        long time = 0;

        // If match trace info is on, mark the time just before matching starts.
        if (SHOW_MATCH_TRACE)
        {
            time = System.currentTimeMillis();
        }

        // Get a reply for each sentence.
        while (sentences.hasNext())
        {
            replies.add(getReply((String)sentences.next(), that, topic, userid, botid));
        }

        // Increment the (static) response count.
        responseCount++;;

        // If match trace info is on, produce statistics about the response time.
        if (SHOW_MATCH_TRACE)
        {
            // Mark the time that processing is finished.
            time = System.currentTimeMillis() - time;

            // Calculate the average response time.
            totalTime += time;
            avgResponseTime = (float)totalTime / (float)responseCount;
            Trace.userinfo(RESPONSE_SPACE + responseCount + SPACE_IN_SPACE + time + MS_AVERAGE + avgResponseTime + MS);
        }

        // Invoke targeting if appropriate.
        if (responseCount % TARGET_SKIP == 0)
        {
            if (USE_TARGETING)
            {
                Graphmaster.checkpoint();
            }
        }
        
        // If no replies, return an empty string.
        if (replies.size() == 0)
        {
            replies.add(EMPTY_STRING);
        }
        return replies;   
    }


    /**
     *  Gets a reply to an input.  Assumes that the
     *  input has already had all necessary substitutions and
     *  pre-processing performed, and that the input is
     *  a single sentence.
     *
     *  @param input    the input sentence
     *  @param that     the input that value
     *  @param topic    the input topic value
     *  @param userid   the userid requesting the reply
     *  @param botid
     *
     *  @return the reply to the input sentence
     */
    private static String getReply(String input, String that, String topic, String userid, String botid)
    {
        // Push the input onto the <input/> stack.
        PredicateMaster.push(INPUT, input, userid, botid);

        // Create a new TemplateParser.
        TemplateParser parser;
        try
        {
            parser = new TemplateParser(input, userid, botid);
        }
        catch (TemplateParserException e)
        {
            throw new DeveloperError(e);
        }

        String reply = null;
        try
        {
            reply = getMatchResult(input, that, topic, userid, botid, parser);
        }
        catch (DeveloperError e)
        {
            Log.devfail(e);
            Log.devfail("Exiting due to developer error.", Log.ERROR);
            System.exit(1);
        }
        catch (UserError e)
        {
            Log.userfail(e);
            Log.devfail("Exiting due to user error.", Log.ERROR);
            System.exit(1);
        }
        catch (RuntimeException e)
        {
            Log.devfail(e);
            Log.devfail("Exiting due to unforeseen runtime exception.", Log.ERROR);
            System.exit(1);
        }
        if (reply == null)
        {
            Log.devfail("getMatchReply generated a null reply!", Log.ERROR);
            System.exit(1);
        }

        // Push the reply onto the <that/> stack.
        PredicateMaster.push(THAT, reply, userid, botid);

        return Toolkit.filterWhitespace(reply);
    }


    /**
     *	Gets the match result from the Graphmaster.
     *
     * 	@param input
     *  @param that
     *  @param topic
     *  @param userid
     *  @param botid
     *  @param parser
     *  @param timeout	whether to control the match attempt with a timeout thread
     */
    private static String getMatchResult(String input, String that, String topic,
                                         String userid, String botid, TemplateParser parser)
    {
        // Always show the input path (in any case, if match trace is on).
        if (SHOW_MATCH_TRACE)
        {
            Trace.userinfo(PredicateMaster.get(Globals.getClientNamePredicate(), userid, botid) + '>' +
                           SPACE + input + SPACE + Graphmaster.PATH_SEPARATOR +
                           SPACE + that + SPACE + Graphmaster.PATH_SEPARATOR +
                           SPACE + topic + SPACE + Graphmaster.PATH_SEPARATOR +
                           SPACE + botid);
        }

        // Create a case-insensitive pattern-fitted version of the input.
        String inputIgnoreCase = InputNormalizer.patternFitIgnoreCase(input);

        Match match = null;
        
        try
        {
            match = Graphmaster.match(InputNormalizer.patternFitIgnoreCase(input),
                                      that, topic, botid);
        }
        catch (NoMatchException e)
        {
            Log.userinfo(e.getMessage(), Log.CHAT);
            return EMPTY_STRING;
        }
        
        if (match == null)
        {
            Log.userinfo("No match found for input \"" + input + "\".", Log.CHAT);
            return EMPTY_STRING;
        }

        if (SHOW_MATCH_TRACE)
        {
            Trace.userinfo(LABEL_MATCH + match.getPath());
            Trace.userinfo(LABEL_FILENAME + QUOTE_MARK +
                           match.getFileName() + QUOTE_MARK);
        }

        ArrayList stars = match.getInputStars();
        if (stars.size() > 0)
        {
            parser.setInputStars(stars);
        }

        stars = match.getThatStars();
        if (stars.size() > 0)
        {
            parser.setThatStars(stars);
        }

        stars = match.getTopicStars();
        if (stars.size() > 0)
        {
            parser.setTopicStars(stars);
        }

        String template = match.getTemplate();
        String reply = null;

        try
        {
            reply = parser.processResponse(template);
        }
        catch (ProcessorException e)
        {
            // Log the error message.
            Log.userinfo(e.getMessage(), Log.ERROR);

            // Set response to empty string.
            return EMPTY_STRING;
        }
        
        // Record activation, if targeting is in use.
        // Needs review in light of multi-bot update
        if (USE_TARGETING)
        {
            Nodemapper matchNodemapper = match.getNodemapper();
            if (matchNodemapper == null)
            {
                Trace.devinfo("Match nodemapper is null!");
            }
            else
            {
                Set activations = (Set)matchNodemapper.get(Graphmaster.ACTIVATIONS);
                if (activations == null)
                {
                    activations = new HashSet();
                }
                String path = match.getPath() + SPACE + Graphmaster.PATH_SEPARATOR + SPACE +
                              inputIgnoreCase + SPACE + Graphmaster.PATH_SEPARATOR + SPACE +
                              that + SPACE + Graphmaster.PATH_SEPARATOR + SPACE +
                              topic + SPACE + Graphmaster.PATH_SEPARATOR + SPACE + 
                              botid + SPACE + Graphmaster.PATH_SEPARATOR + SPACE + reply;
                if (!activations.contains(path))
                {
                    activations.add(path);
                    match.getNodemapper().put(Graphmaster.ACTIVATIONS, activations);
                    Graphmaster.activatedNode(match.getNodemapper());
                }
            }
        }
        return reply;
    }


    /**
     *  Returns the average response time.
     *
     *  @return the average response time'
     */
    public float averageResponseTime()
    {
        return avgResponseTime;
    }


    /**
     *  Returns the number of queries per hour.
     *
     *  @return the number of queries per hour
     */
    public float queriesPerHour()
    {
        return responseCount / ((System.currentTimeMillis() - startTime) / 3600000.00f);
    }
    
    
    /**
     *  Adds a Pulse to the registered list.
     *
     *  @param pulse	the Pulse to be added
     */
    public static void addPulse(Pulse pulse)
    {
        pulses.add(pulse);
    }
    
    
    /**
     *  Emits any registered pulses.
     */
    public synchronized static void pulse()
    {
        Iterator iterator = pulses.iterator();
        while (iterator.hasNext())
        {
            ((Pulse)iterator.next()).emit();
        }
    }


    /**
     *  Saves a predicate for a given <code>userid</code>.  This only applies to
     *  Multiplexors that provide long-term storage (others may just do nothing).
     *
     *  @since 4.1.4
     *
     *  @param name     predicate name
     *  @param value    predicate value
     *  @param userid   user identifier
     *  @param botid
     */
    abstract public void savePredicate(String name, String value, String userid, String botid);


    /**
     *  Loads a predicate into memory
     *  for a given <code>userid</code>.  This only applies to
     *  Multiplexors that provide long-term storage (others may just do nothing).
     *
     *  @since 4.1.4
     *
     *  @param name         predicate name
     *  @param userid       user identifier
     *  @param botid
     *
     *  @return the predicate value
     *
     *  @throws NoSuchPredicateException if there is no predicate with this name
     */
    abstract public String loadPredicate(String name, String userid, String botid) throws NoSuchPredicateException;


    /**
     *  Checks whether a given userid and password combination is valid.
     *  Multiplexors for which this makes no sense should just
     *  return true.
     *
     *  @param userid       the userid to check
     *  @param botid
     *  @param password     the password to check
     *  @param secretKey    the secret key that should authenticate this request
     *
     *  @return whether the userid and password combination is valid
     */
    abstract public boolean checkUser(String userid, String password, String secretKey, String botid);
    

    /**
     *  Creates a new user entry, given a userid and password.
     *  Multiplexors for which this makes no sense should just
     *  return true.
     *
     *  @param userid       the userid to use
     *  @param botid
     *  @param password     the password to assign
     *  @param secretKey    the secret key that should authenticate this request
     *
     *  @return whether the creation was successful
     */
    abstract public boolean createUser(String userid, String password, String secretKey, String botid);


    /**
     *  Changes the password associated with a userid.
     *  Multiplexors for which this makes no sense should just
     *  return true.
     *
     *  @param userid       the userid
     *  @param botid
     *  @param password     the new password
     *  @param secretKey    the secret key that should authenticate this request
     *
     *  @return whether the change was successful
     */
    abstract public boolean changePassword(String userid, String password, String secretKey, String botid);


    /**
     *  Returns a count of known userids.  This may be defined differently for different multiplexors.
     *
     *  @return a count of known userids
     */
    abstract public int useridCount(String botid);
 }
