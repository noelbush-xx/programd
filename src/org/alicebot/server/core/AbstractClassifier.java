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

package org.alicebot.server.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.logging.Trace;
import org.alicebot.server.core.parser.AIMLParser;
import org.alicebot.server.core.processor.ProcessorException;
import org.alicebot.server.core.responder.Responder;
import org.alicebot.server.core.util.InputNormalizer;
import org.alicebot.server.core.util.Match;
import org.alicebot.server.core.util.NoMatchException;
import org.alicebot.server.core.util.Substituter;
import org.alicebot.server.core.util.Toolkit;

/**
 *  This is the database-independent part of the old
 *  <code>Classifier</code>.
 *
 *  @author Richard Wallace, Jon Baer
 *  @author Thomas Ringate/Pedro Colla
 *  @author Noel Bush (only moved methods here)
 *  @version 4.1.3
 */
abstract public class AbstractClassifier implements Multiplexor
{
    /** The response-count period for invoking targeting. */
    private static final int TARGET_SKIP = Globals.getTargetSkip();

    // Convenience constants.

    /** The name of the <code>that</code> special predicate. */
    public static final String THAT = "that";

    /** The name of the <code>topic</code> special predicate. */
    public static final String TOPIC = "topic";

    /** The name of the <code>input</code> special predicate. */
    public static final String INPUT = "input";

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
            Log.userfail("Error creating secret key file.", new String[] {Log.STARTUP, Log.DATABASE});
        }
        PrintWriter out = null;
        try
        {
            out = new PrintWriter(new FileOutputStream(keyFile));
        }
        catch (FileNotFoundException e)
        {
            Log.userfail("Error writing secret key file.", new String[] {Log.STARTUP, Log.DATABASE});
        }
        out.print(SECRET_KEY);
        out.flush();
        out.close();
    }


    public String getResponse(String input, String userid)
    {
        return StaticSelf.getResponse(input, userid);
    }


    public String getResponse(String input, String userid, Responder robot)
    {
        return StaticSelf.getResponse(input, userid, robot);
    }


    public String getInternalResponse(String input, String userid)
    {
        return StaticSelf.getInternalResponse(input, userid);
    }


    abstract public String setPredicateValue(String name, String value, String userid);

    public String setPredicateValue(String name, int index, String value, String userid)
    {
        return StaticSelf.setPredicateValue(name, index, value, userid);
    }


    public String pushPredicateValue(String name, String value, String userid)
    {
        return StaticSelf.pushPredicateValue(name, value, userid);
    }


    abstract public String getPredicateValue(String name, String userid) throws NoSuchPredicateException;


    public String getPredicateValue(String name, int index, String userid)
    {
        return StaticSelf.getPredicateValue(name, index, userid);
    }

    abstract public boolean createUser(String userid, String password, String secretKey);

    abstract public boolean checkUser(String userid, String password, String secretKey);

    abstract public boolean changePassword(String userid, String password, String secretKey);



    /**
     *  Contains static synchronized versions of
     *  {@link Multiplexor} methods, to ensure thread-safety.
     */
    public static class StaticSelf
    {
        public static synchronized String getResponse(String input, String userid)
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

        public static synchronized String getResponse(String input, String userid, Responder robot)
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


        public static synchronized String getInternalResponse(String input, String userid)
        {
            // Ready the that and topic predicates for constructing the match path.
            String that = null;
            String topic = null;
            try
            {
                that  = InputNormalizer.patternFitIgnoreCase(ActiveMultiplexor.StaticSelf.getPredicateValue(THAT, userid));
            }
            catch (NoSuchPredicateException e)
            {
                that = ASTERISK;
            }
            try
            {
                topic = InputNormalizer.patternFitIgnoreCase(ActiveMultiplexor.StaticSelf.getPredicateValue(TOPIC, userid));
            }
            catch (NoSuchPredicateException e)
            {
                topic = ASTERISK;
            }

            if (that.equals(EMPTY_STRING) || that.equals(Globals.getBotPredicateEmptyDefault()) )
            {
                that = ASTERISK;
            }
            if (topic.equals(EMPTY_STRING) || topic.equals(Globals.getBotPredicateEmptyDefault()) )
            {
                topic = ASTERISK;
            }

            return getInternalReply(input, that, topic, userid);
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
        private static synchronized ArrayList getReplies(ArrayList sentenceList, String userid)
        {
            // All replies will be assembled in this ArrayList.
            ArrayList replies = new ArrayList(sentenceList.size());

            // Ready the that and topic predicates for constructing the match path.
            String that  = null;
            String topic = null;
            try
            {
                that = InputNormalizer.patternFitIgnoreCase(ActiveMultiplexor.StaticSelf.getPredicateValue(THAT, userid));
            }
            catch (NoSuchPredicateException e)
            {
                that = ASTERISK;
            }
            try
            {
                topic = InputNormalizer.patternFitIgnoreCase(ActiveMultiplexor.StaticSelf.getPredicateValue(TOPIC, userid));
            }
            catch (NoSuchPredicateException e)
            {
                topic = ASTERISK;
            }

            if (that.equals(EMPTY_STRING) || that.equals(Globals.getBotPredicateEmptyDefault()) )
            {
                that = ASTERISK;
            }
            if (topic.equals(EMPTY_STRING) || topic.equals(Globals.getBotPredicateEmptyDefault()) )
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
            if (Globals.showMatchTrace())
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
        private static synchronized String getReply(String input, String that, String topic, String userid)
        {
            // Push the input onto the <input/> stack.
            pushPredicateValue(INPUT, input, userid);

            // Set the unindexed <input/>.
            ActiveMultiplexor.StaticSelf.setPredicateValue(INPUT, input, userid);

            String reply = getInternalReply(input, that, topic, userid);

            // Push the reply onto the <that/> stack.
            pushPredicateValue(THAT, reply, userid);

            // Set the unindexed <that/>.
            ActiveMultiplexor.StaticSelf.setPredicateValue(THAT, reply, userid);

            return Toolkit.filterWhitespace(reply);
        }


        /**
         *  Gets an internal reply.
         */
        private static synchronized String getInternalReply(String input, String that, String topic, String userid)
        {
            Match match = null;
            String reply = null;

            // Always show the input path (in any case, if showMatchTrace is on).
            if (Globals.showMatchTrace())
            {
                Trace.userinfo(LABEL_INPUT + SPACE +
                               input + SPACE + Graphmaster.PATH_SEPARATOR +
                               SPACE + that + SPACE +
                               Graphmaster.PATH_SEPARATOR + SPACE + topic);
            }

            // Call the Graphmaster pattern-matching method to get a matching pattern.
            try
            {
                match = Graphmaster.match(InputNormalizer.patternFitIgnoreCase(input), that, topic);
            }
            catch (NoMatchException e)
            {
                Log.userinfo(e.getMessage(), Log.CHAT);
                return EMPTY_STRING;
            }

            // Recover the template.
            reply = (String)match.getTemplate();

            if (Globals.showMatchTrace())
            {
                Trace.userinfo(LABEL_MATCH + SPACE + match.getPath());
                Trace.userinfo(LABEL_FILENAME + SPACE + QUOTE_MARK +
                               match.getFileName() + QUOTE_MARK);
            }

            AIMLParser parser   = new AIMLParser();

            /*
                This makes the template parser work with a local
                copy of the star vectors structure in order to support
                recursion and to ensure a single extraction, since the
                getInputStars/getThatStars/getTopicStars methods are
                destructive (can only be called once).
            */
            parser.setInputStars(match.getInputStars());
            parser.setThatStars(match.getThatStars());
            parser.setTopicStars(match.getTopicStars());

            try
            {
                reply = parser.processResponse(userid, reply);
            }
            catch (ProcessorException e)
            {
                // Set response to empty string.
                reply = EMPTY_STRING;
            }
            parser = null;
            return reply;
        }


        /**
         *  <p>
         *  A crude implementation of storing indexed values. The index is
         *  appended to the name of the predicate.
         *  </p>
         *  <p>
         *  This method is presently <i>not</i> AIML 1.0.1-compatible, because
         *  it does not support
         *  <a href="http://www.alicebot.org/TR/2001/WD-aiml/#section-aiml-predicate-behaviors">return-name-when-set</a>
         *  predicates. The predicate value is <i>always</i> returned in the
         *  present implementation.
         *  </p>
         *
         *  @see Multiplexor#setPredicate(String, int, String, String)
         */
        public static synchronized String setPredicateValue(String name, int index, String value, String userid)
        {
            // Verify arguments (for performance).
            if (name.equals(EMPTY_STRING))
            {
               return EMPTY_STRING;
            }
            if (index <= 0)
            {
               return EMPTY_STRING;
            }
            if (index > Globals.getMaxIndexDepth())
            {
               return EMPTY_STRING;
            }
            ActiveMultiplexor.StaticSelf.setPredicateValue(name + Integer.toString(index), value, userid);
            return value;
        }


        /**
         *  Pushes a new value to the top of the stack of
         *  indexes, preserving the integrity of the chain.
         *  The farthest-back value is simply dropped when
         *  it exceeds the horizon given by
         *  {@link Globals#MAX_INDEX_DEPTH}.
         *  Proper shift of the stack ([1..N-1] -&gt; [2..N]) is performed
         *  as part of the operation.
         *
         *  @param name         predicate name
         *  @param userid       the user identifier
         *  @param value        the predicate value to add to the stack
         */
        public static synchronized String pushPredicateValue(String name, String value, String userid)
        {
            if (name.equals(EMPTY_STRING))
            {
                return EMPTY_STRING;
            }
            // Move all elements one level deeper.
            int index = Globals.getMaxIndexDepth();
            while (index > 1)
            {
                StaticSelf.setPredicateValue(name, index,
                                    StaticSelf.getPredicateValue(name, index - 1, userid), userid);
                index--;
            }

            // Insert the new value.
            StaticSelf.setPredicateValue(name, 1, value, userid);
            return value;
        }


        /**
         *  Relying upon the crude method of storing indexed values
         *  in {@link #setPredicate(String, int, String)}, this method
         *  retrieves an indexed <code>predicate</code> value.
         *
         *  @see Multiplexor#getPredicateValue(String, int, String)
         */
        public static synchronized String getPredicateValue(String name, int index, String userid)
        {
            // Verify arguments (for performance).
            if (name.equals(EMPTY_STRING))
            {
               return EMPTY_STRING;
            }
            if (index <= 0)
            {
               return EMPTY_STRING;
            }
            if (index > Globals.getMaxIndexDepth())
            {
               return EMPTY_STRING;
            }

            // Return the value if present.
            try
            {
                return ActiveMultiplexor.StaticSelf.getPredicateValue(name + Integer.toString(index), userid);
            }
            catch (NoSuchPredicateException e)
            {
                return EMPTY_STRING;
            }
        }
    }
} 
