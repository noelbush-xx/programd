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
    Further fixes and optimizations (4.1.3 [01] - November 2001, Noel Bush)
    - removed use of MASTER_MATCH in Graphmaster (it is a local object)
    - Stopped weird setting of INPUT_STAR, THAT_STAR and TOPIC_STAR on Graphmaster
    - modified match trace so input path is always shown, even if no match
*/

/*
    More fixes (4.1.3 [02] - November 2001, Noel Bush
    - changed *Predicate*() methods to *PredicateValue*()
    - added getInternalResponse(), getReply() and getReplies()
    - changed to use changed method names for Responder
    - introduced use of NoMatchException and NoSuchPredicateException
    - some more checks that resulted from testing the bot with an empty category set
*/

/*
    4.1.4 [00] - December 2001, Noel Bush
    - changed response time display back so that it shows when console is in use
    - added support of *not* saving special predicates <that/>, <input/> and <star/>
*/

package org.alicebot.server.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.Trace;
import org.alicebot.server.core.parser.AIMLParser;
import org.alicebot.server.core.parser.AIMLParserException;
import org.alicebot.server.core.processor.ProcessorException;
import org.alicebot.server.core.responder.Responder;
import org.alicebot.server.core.util.DeveloperErrorException;
import org.alicebot.server.core.util.InputNormalizer;
import org.alicebot.server.core.util.Match;
import org.alicebot.server.core.util.NoMatchException;
import org.alicebot.server.core.util.Toolkit;
import org.alicebot.server.core.util.UserErrorException;


/**
 *  This is a significantly reworked version of the
 *  database-independent part of the old
 *  <code>Classifier</code>.  Quite possibly the
 *  Multiplexor framework should change, and the
 *  reply management parts of this class should be
 *  in a non-abstract class -- there's very little
 *  about them that one can imagine changing.
 *
 *  @author Richard Wallace, Jon Baer
 *  @author Thomas Ringate/Pedro Colla
 *  @author Noel Bush
 *  @version 4.1.4
 */
abstract public class AbstractClassifier implements Multiplexor
{
    /** The response-count period for invoking targeting. */
    private static final int TARGET_SKIP = Globals.getTargetSkip();

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

    /** The host name. */
    protected static final String HOSTNAME = Globals.getHostName();

    /** The word &quot;value&quot;. */
    protected static final String VALUE = "value";

    /** An asterisk (used in String production) */
    protected static final String ASTERISK = "*";

    /** A quote mark. */
    protected static final String QUOTE_MARK = "\"";

    /** The string &quot;Input:&quot;. */
    private static final String LABEL_INPUT = "Input:";

    /** The string &quot;Match:&quot;. */
    private static final String LABEL_MATCH = "Match:";

    /** The string &quot;Filename:&quot;. */
    private static final String LABEL_FILENAME = "Filename:";

    /** The string &quot;Response &quot;. */
    private static final String RESPONSE_SPACE = "Response ";

    /** The string &quot; in &quot;. */
    private static final String SPACE_IN_SPACE = " in ";

    /** The string &quot; ms. Running average: &quot;. */
    private static final String MS_RUNNING_AVERAGE = " ms. (Running average: ";

    /* The string &quot; ms).&quot; */
    private static final String MS = " ms).";

    /** A secret key used for (weakly) authorizing authentication requests. */
    protected static String SECRET_KEY;


    // Class variables.

    /** A counter for tracking the number of responses produced. */
    protected static int responseCount = 0;

    /** A counter for tracking average response time. */
    protected static float avgResponseTime = 0;


    /**
     *  Creates the secret key.
     */
    public void initialize()
    {
        SECRET_KEY = new Double(Math.random() * (double)new Date().getTime()).toString();
        File keyFile = new File("secret.key");
        keyFile.delete();
        try
        {
            keyFile.createNewFile();
        }
        catch (IOException e)
        {
            throw new UserErrorException("Error creating secret key file.");
        }
        PrintWriter out;
        try
        {
            out = new PrintWriter(new FileOutputStream(keyFile));
        }
        catch (FileNotFoundException e)
        {
            throw new UserErrorException("Error writing secret key file.");
        }
        out.print(SECRET_KEY);
        out.flush();
        out.close();
    }

    abstract public void savePredicate(String name, String value, String userid);

    abstract public String loadPredicate(String name, String userid) throws NoSuchPredicateException;

    abstract public boolean createUser(String userid, String password, String secretKey);

    abstract public boolean checkUser(String userid, String password, String secretKey);

    abstract public boolean changePassword(String userid, String password, String secretKey);



    public synchronized String getResponse(String input, String userid)
    {
        // Get replies to each sentence.
        Iterator replies = (getReplies(InputNormalizer.sentenceSplit(
                                        InputNormalizer.applySubstitutions(input)), userid)).iterator();

        // The result will be created in this StringBuffer.
        StringBuffer response = new StringBuffer(EMPTY_STRING);

        while (replies.hasNext())
        {
            response.append((String)replies.next());
        }

        return response.toString();
    }


    public synchronized String getResponse(String input, String userid, Responder robot)
    {
        // Split sentences (after performing substitutions and robot pre-processing).
        ArrayList sentenceList = InputNormalizer.sentenceSplit(
                                    InputNormalizer.applySubstitutions(
                                        robot.preprocess(input, HOSTNAME)));
        // Get replies to each sentence.
        Iterator replies = getReplies(sentenceList, userid).iterator();

        String response = EMPTY_STRING;

        Iterator sentences = sentenceList.iterator();

        while (sentences.hasNext())
        {
            // Ask the robot to append the reply to the response, and accumulate the result.
            response = robot.append((String)sentences.next(), (String)replies.next(), response);
        }

        // Log the response.
        robot.log(input, response, HOSTNAME, userid, Globals.getBotName());

        // Finally, ask the robot to postprocess the response, and return the result.
        response = robot.postprocess(response);

        if (response == null)
        {
            return EMPTY_STRING;
        }
        else
        {
            return response;
        }
    }


    public synchronized String getInternalResponse(String input, String userid, AIMLParser parser)
    {
        // Ready the that and topic predicates for constructing the match path.
        ArrayList thatSentences  = InputNormalizer.sentenceSplit(PredicateMaster.get(THAT, 1, userid));
        String that = InputNormalizer.patternFitIgnoreCase((String)thatSentences.get(thatSentences.size() - 1));
        if (that.equals(EMPTY_STRING) || that.equals(Globals.getPredicateEmptyDefault()) )
        {
            that = ASTERISK;
        }

        String topic = InputNormalizer.patternFitIgnoreCase(PredicateMaster.get(TOPIC, userid));
        if (topic.equals(EMPTY_STRING) || topic.equals(Globals.getPredicateEmptyDefault()) )
        {
            topic = ASTERISK;
        }

        return getInternalReply(input, that, topic, userid, parser);
    }


    /**
     *  Gets the list of replies to some input sentences.
     *  Assumes that the sentences have already had all necessary
     *  pre-processing and substitutions performed.
     *
     *  @param sentenceList  the input sentences
     *  @param userid        the userid requesting the replies
     *
     *  @return the list of replies to the input sentences
     */
    private synchronized ArrayList getReplies(ArrayList sentenceList, String userid)
    {
        // All replies will be assembled in this ArrayList.
        ArrayList replies = new ArrayList(sentenceList.size());

        // Ready the that and topic predicates for constructing the match path.
        ArrayList thatSentences  = InputNormalizer.sentenceSplit(PredicateMaster.get(THAT, 1, userid));
        String that = InputNormalizer.patternFitIgnoreCase((String)thatSentences.get(thatSentences.size() - 1));
        if (that.equals(EMPTY_STRING) || that.equals(Globals.getPredicateEmptyDefault()) )
        {
            that = ASTERISK;
        }

        String topic = InputNormalizer.patternFitIgnoreCase(PredicateMaster.get(TOPIC, userid));
        if (topic.equals(EMPTY_STRING) || topic.equals(Globals.getPredicateEmptyDefault()) )
        {
            topic = ASTERISK;
        }

        // We might use this to track matching statistics.
        long time = 0;

        // If the console is shown, mark the time just before matching starts.
        if (Globals.showConsole())
        {
            time = new Date().getTime();
        }

        Iterator sentences = sentenceList.iterator();

        // Get a reply for each sentence.
        while (sentences.hasNext())
        {
            replies.add(getReply((String)sentences.next(), that, topic, userid));
        }

        // Increment the (static) response count.
        responseCount++;;

        // Invoke targeting if appropriate.
        if (responseCount % TARGET_SKIP == 0)
        {
            if (Globals.useTargeting())
            {
                Graphmaster.checkpoint();
            }
        }
        
        // If the console is in use, produce statistics about the response time.
        if (Globals.showConsole())
        {
            // Mark the time that processing is finished.
            time = new Date().getTime() - time;

            // Calculate the running average response time.
            avgResponseTime = (avgResponseTime * (responseCount - 1) + time) / responseCount;
            Trace.userinfo(RESPONSE_SPACE + responseCount + SPACE_IN_SPACE +
                           time + MS_RUNNING_AVERAGE + avgResponseTime + MS);
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
     *
     *  @return the reply to the input sentence
     */
    private synchronized String getReply(String input, String that, String topic, String userid)
    {
        // Push the input onto the <input/> stack.
        PredicateMaster.push(INPUT, input, userid);

        String reply = null;
        try
        {
            reply = getInternalReply(input, that, topic, userid);
        }
        catch (DeveloperErrorException e)
        {
            Log.devfail(e);
            Log.devfail("Exiting due to developer error.", Log.ERROR);
            System.exit(1);
        }
        catch (UserErrorException e)
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
            Log.devfail("getInternalReply generated a null reply!", Log.ERROR);
            System.exit(1);
        }

        // Push the reply onto the <that/> stack.
        PredicateMaster.push(THAT, reply, userid);

        return Toolkit.filterWhitespace(reply);
    }


    /**
     *  Gets an internal reply.
     */
    private synchronized String getInternalReply(String input, String that, String topic, String userid)
    {
        AIMLParser parser;
        try
        {
            parser = new AIMLParser(input);
        }
        catch (AIMLParserException e)
        {
            throw new DeveloperErrorException(e);
        }

        return getInternalReply(input, that, topic, userid, parser);
    }


    private synchronized String getInternalReply(String input, String that, String topic,
                                                 String userid, AIMLParser parser)
    {
        Match match;

        // Always show the input path (in any case, if showMatchTrace is on).
        if (Globals.showMatchTrace())
        {
            Trace.userinfo(LABEL_INPUT + SPACE +
                           input + SPACE + Graphmaster.PATH_SEPARATOR +
                           SPACE + that + SPACE +
                           Graphmaster.PATH_SEPARATOR + SPACE + topic);
        }

        // Call the Graphmaster pattern-matching method to get a matching pattern.
        String inputIgnoreCase = InputNormalizer.patternFitIgnoreCase(input);
        try
        {
            match = Graphmaster.match(inputIgnoreCase, that, topic);
        }
        catch (NoMatchException e)
        {
            Log.userinfo(e.getMessage(), Log.CHAT);
            return EMPTY_STRING;
        }

        if (Globals.showMatchTrace())
        {
            Trace.userinfo(LABEL_MATCH + SPACE + match.getPath());
            Trace.userinfo(LABEL_FILENAME + SPACE + QUOTE_MARK +
                           match.getFileName() + QUOTE_MARK);
        }

        parser.setInputStars(match.getInputStars());
        parser.setThatStars(match.getThatStars());
        parser.setTopicStars(match.getTopicStars());

        String template = match.getTemplate();
        String reply = null;

        try
        {
            reply = parser.processResponse(userid, template);
        }
        catch (ProcessorException e)
        {
            // Log the error message.
            Log.userinfo(e.getMessage(), Log.ERROR);

            // Set response to empty string.
            reply = EMPTY_STRING;
        }
        parser = null;
        return reply;
    }
} 
