/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.multiplexor;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.aitools.programd.Core;
import org.aitools.programd.CoreSettings;
import org.aitools.programd.bot.Bot;
import org.aitools.programd.bot.Bots;
import org.aitools.programd.graph.Graphmaster;
import org.aitools.programd.graph.Match;
import org.aitools.programd.logging.ChatLogEvent;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.util.InputNormalizer;
import org.aitools.programd.util.NoMatchException;
import org.aitools.util.resource.URLTools;
import org.aitools.util.runtime.DeveloperError;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * A <code>Multiplexor</code> multiplexes the clients of a bot and keeps track
 * of all their predicate values.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @author Richard Wallace, Jon Baer
 * @author Thomas Ringate/Pedro Colla
 * @param <M> some type of "storage mechanism", loosely defined, that will be used when preparing to save predicates
 */
abstract public class Multiplexor<M>
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

    /** The word &quot;value&quot;. */
    protected static final String VALUE = "value";

    /** The predicate empty default. */
    protected String predicateEmptyDefault;

    // Class variables.

    /** The Core that owns this Multiplexor. */
    protected Core _core;

    /** The Graphmaster in use by the Core. */
    protected Graphmaster graphmaster;

    /** The PredicateMaster in use by the Core. */
    protected PredicateMaster predicateMaster;

    /** The Bots object that belongs to the Core. */
    protected Bots bots;

    /** The general log where we will record some events. */
    protected static final Logger logger = Logger.getLogger("programd");

    /** The log where match info will be recorded. */
    protected static final Logger matchLogger = Logger.getLogger("programd.matching");

    /** The time that the Multiplexor started operation. */
    protected long startTime = System.currentTimeMillis();

    /** A counter for tracking the number of responses produced. */
    protected long responseCount = 0;

    /** The total response time. */
    protected long totalTime = 0;

    /** A counter for tracking average response time. */
    protected float avgResponseTime = 0;

    /**
     * Constructs the Multiplexor, using some values taken from the Core
     * object's settings. Note that the {@link #predicateMaster} is <i>not</i>
     * initialized -- it must be {@link #attach}ed subsequently.
     * 
     * @param owner the Core that owns this Multiplexor
     */
    public Multiplexor(Core owner)
    {
        this._core = owner;
        this.graphmaster = this._core.getGraphmaster();
        this.bots = this._core.getBots();
        CoreSettings coreSettings = this._core.getSettings();
        this.predicateEmptyDefault = coreSettings.getPredicateEmptyDefault();
    }

    /**
     * Attaches the given
     * {@link org.aitools.programd.multiplexor.PredicateMaster PredicateMaster}
     * to this <code>Multiplexor</code>.
     * 
     * @param predicateMasterToAttach
     */
    public void attach(PredicateMaster predicateMasterToAttach)
    {
        this.predicateMaster = predicateMasterToAttach;
    }

    /**
     * Can do whatever initialization is needed for the particular multiplexor.
     */
    abstract public void initialize();

    /**
     * Returns the response to a non-internal input.
     * 
     * @param input the &quot;non-internal&quot; (possibly multi-sentence,
     *            non-substituted) input
     * @param userid the userid for whom the response will be generated
     * @param botid the botid from which to get the response
     * @return the response
     */
    public synchronized String getResponse(String input, String userid, String botid)
    {
        // Get the specified bot object.
        Bot bot = this.bots.get(botid);

        // Split sentences (after performing substitutions).
        List<String> sentenceList = bot.sentenceSplit(bot.applyInputSubstitutions(input));

        // Get the replies.
        List<String> replies = getReplies(sentenceList, userid, botid);

        if (replies == null)
        {
            return null;
        }

        // Start by assuming an empty response.
        StringBuilder responseBuffer = new StringBuilder("");

        // Append each reply to the response.
        for (String reply : replies)
        {
            responseBuffer.append(reply);
        }

        String response = responseBuffer.toString();

        // Log the response.
        logResponse(input, response, userid, botid);

        // Return the response (may be just ""!)
        return response;
    }

    /**
     * <p>
     * Produces a response to an &quot;internal&quot; input sentence -- i.e., an
     * input that has been produced by a <code>srai</code>.
     * </p>
     * <p>
     * This method
     * takes an already-existing <code>TemplateParser</code>, <i>doesn't </i>
     * take a <code>Responder</code>, and assumes that the inputs have
     * already been normalized.
     * </p>
     * 
     * @param input the input sentence
     * @param userid the userid requesting the response
     * @param botid the botid from which to get the response
     * @param parser the parser object to update when generating the response
     * @return the response
     */
    @SuppressWarnings("boxing")
    public String getInternalResponse(String input, String userid, String botid, TemplateParser parser)
    {
        // Get the requested bot.
        Bot bot = this.bots.get(botid);
        
        parser.addInput(input);

        // Ready the that and topic predicates for constructing the match path.
        List<String> thatSentences = bot.sentenceSplit(this.predicateMaster.get(THAT, 1, userid, botid));
        String that = InputNormalizer.patternFitIgnoreCase(thatSentences.get(thatSentences.size() - 1));

        if ("".equals(that) || that.equals(this.predicateEmptyDefault))
        {
            that = "*";
        }
        parser.addThat(that);

        String topic = this.predicateMaster.get(TOPIC, userid, botid);
        if ("".equals(topic) || topic.equals(this.predicateEmptyDefault))
        {
            topic = "*";
        }
        parser.addTopic(topic);
        
        // Verify we've been tracking thats and topics correctly.
        List<String> inputs = parser.getInputs();
        List<String> thats = parser.getThats();
        List<String> topics = parser.getTopics();
        int stackSize = inputs.size();
        assert stackSize == thats.size() && thats.size() == topics.size() :
            String.format("%d inputs, %d thats, %d topics", stackSize, thats.size(), topics.size());

        // Check for some simple kinds of infinite loops.
        if (stackSize > 1)
        {
            for (int lookback = stackSize - 2; lookback > -1; lookback--)
            {
                String comparisonInput = inputs.get(lookback);
                String comparisonThat = thats.get(lookback);
                String comparisonTopic = topics.get(lookback);
                String infiniteLoopInput = parser.getCore().getSettings().getInfiniteLoopInput();
                if (that.equalsIgnoreCase(comparisonThat) && topic.equalsIgnoreCase(comparisonTopic))
                {
                    if (input.equalsIgnoreCase(infiniteLoopInput))
                    {
                        matchLogger.error("Unrecoverable infinite loop.");
                        return "";
                    }
                    if (input.equalsIgnoreCase(comparisonInput))
                    {
                        input = infiniteLoopInput;
                        inputs.set(stackSize - 1, infiniteLoopInput);
                        matchLogger.warn(String.format("Infinite loop detected; substituting \"%s\".", infiniteLoopInput));
                    }
                }
            }
        }                

        return getMatchResult(input, that, topic, userid, botid, parser);
    }

    /**
     * Gets the list of replies to some input sentences. Assumes that the
     * sentences have already had all necessary pre-processing and substitutions
     * performed.
     * 
     * @param sentenceList the input sentences
     * @param userid the userid requesting the replies
     * @param botid
     * @return the list of replies to the input sentences
     */
    @SuppressWarnings("boxing")
    protected List<String> getReplies(List<String> sentenceList, String userid, String botid)
    {
        if (sentenceList == null)
        {
            return null;
        }

        // All replies will be assembled in this ArrayList.
        List<String> replies = Collections.checkedList(new ArrayList<String>(sentenceList.size()), String.class);

        // Get the requested bot.
        Bot bot = this.bots.get(botid);

        // Ready the that and topic predicates for constructing the match path.
        List<String> thatSentences = bot.sentenceSplit(this.predicateMaster.get(THAT, 1, userid, botid));
        String that = null;
        if (thatSentences.size() > 0)
        {
            that = InputNormalizer.patternFitIgnoreCase(thatSentences.get(thatSentences.size() - 1));
        }

        if (that == null || "".equals(that) || that.equals(this.predicateEmptyDefault))
        {
            that = "*";
        }

        String topic = InputNormalizer.patternFitIgnoreCase(this.predicateMaster.get(TOPIC, userid, botid));
        if ("".equals(topic) || topic.equals(this.predicateEmptyDefault))
        {
            topic = "*";
        }

        // We might use this to track matching statistics.
        long time = 0;

        // Mark the time just before matching starts.
        time = System.currentTimeMillis();

        // Get a reply for each sentence.
        for (String sentence : sentenceList)
        {
            replies.add(getReply(sentence, that, topic, userid, botid));
        }

        // Increment the (static) response count.
        this.responseCount++;

        // Produce statistics about the response time.
        // Mark the time that processing is finished.
        time = System.currentTimeMillis() - time;

        // Calculate the average response time.
        this.totalTime += time;
        this.avgResponseTime = (float) this.totalTime / (float) this.responseCount;
        if (matchLogger.isDebugEnabled())
        {
            matchLogger.debug(String.format("Response %d in %dms. (Average: %.2fms)", this.responseCount, time,
                    this.avgResponseTime));
        }

        // Invoke targeting if appropriate.
        /*
         * if (responseCount % TARGET_SKIP == 0) { if (USE_TARGETING) {
         * Graphmaster.checkpoint(); } }
         */

        // If no replies, return an empty string.
        if (replies.size() == 0)
        {
            replies.add("");
        }
        return replies;
    }

    /**
     * Gets a reply to an input. Assumes that the input has already had all
     * necessary substitutions and pre-processing performed, and that the input
     * is a single sentence.
     * 
     * @param input the input sentence
     * @param that the input that value
     * @param topic the input topic value
     * @param userid the userid requesting the reply
     * @param botid
     * @return the reply to the input sentence
     */
    protected String getReply(String input, String that, String topic, String userid, String botid)
    {
        // Push the input onto the <input/> stack.
        this.predicateMaster.push(INPUT, input, userid, botid);

        // Create a new TemplateParser.
        TemplateParser parser = new TemplateParser(input, that, topic, userid, botid, this._core);

        String reply = getMatchResult(input, that, topic, userid, botid, parser);
        if (reply == null)
        {
            logger.error("getMatchReply generated a null reply!", new NullPointerException());
            return "";
        }

        // Push the reply onto the <that/> stack.
        this.predicateMaster.push(THAT, reply, userid, botid);

        return reply;
    }

    /**
     * Gets the match result from the Graphmaster.
     * 
     * @param input the input to match
     * @param that the current that value
     * @param topic the current topic value
     * @param userid the userid for whom to perform the match
     * @param botid the botid for whom to perform the match
     * @param parser the parser to use
     * @return the match result
     */
    protected String getMatchResult(String input, String that, String topic, String userid, String botid,
            TemplateParser parser)
    {
        // Show the input path.
        if (matchLogger.isDebugEnabled())
        {
            matchLogger.debug(String.format("[INPUT (%s)] %s : %s : %s : %s", userid, input, that, topic, botid));
        }

        Match match = null;

        try
        {
            match = this.graphmaster.match(InputNormalizer.patternFitIgnoreCase(input), that, topic, botid);
        }
        catch (NoMatchException e)
        {
            logger.warn(e.getMessage());
            return "";
        }

        if (match == null)
        {
            logger.warn(String.format("No match found for input \"%s\".", input));
            return "";
        }

        String matchFilename = match.getFileName();
        int comma = matchFilename.indexOf(",");
        if (comma > 0)
        {
            matchFilename = matchFilename.substring(0, comma);
        }
        if (matchLogger.isDebugEnabled())
        {
            matchLogger.debug(String.format("[MATCHED] %s (\"%s\")", match.getPath(), matchFilename));
        }
        
        parser.addMatch(match);

        String template = match.getTemplate();
        String reply = null;

        try
        {
            parser.pushContext(URLTools.createValidURL(matchFilename));
        }
        catch (FileNotFoundException e)
        {
            logger.error(String.format("AIML file that was loaded cannot be found: \"%s\"", matchFilename), e);
        }
        try
        {
            reply = parser.processResponse(template);
        }
        catch (ProcessorException e)
        {
            // Log the error message.
            Logger.getLogger("programd").error("Error while processing response: " + e.getExplanatoryMessage(), e);

            // Set response to empty string.
            return "";
        }
        catch (DeveloperError e)
        {
            // Log the error message.
            logger.error(String.format("Error while processing response: \"%s\"", e.getCause().getMessage()), e);

            // Set response to empty string.
            return "";
        }
        return reply;
    }

    /**
     * Logs a response to the chat log.
     * 
     * @param input the input that produced the response
     * @param response the response
     * @param userid the userid for whom the response was produced
     * @param botid the botid that produced the response
     */
    protected void logResponse(String input, String response, String userid, String botid)
    {
        /*
         * NOTA BENE: This is a very specific workaround for a problem with the log4j
         * JDBCAppender.  It appears that the appender fails to maintain the database
         * connection after some period of time, and thus stops working.  Here, we
         * catch the exception thrown in this case, and force log4j to reinitialize
         * the appender.  This is horribly specific and should go away as soon as possible.
         * - 2006-03-29, NB
         */
        try
        {
            logger.callAppenders(new ChatLogEvent(botid, userid, input, response));
        }
        catch (Exception e)
        {
            logger.error("A known bug with log4j has been encountered.  Attempting to reset logging configuration. This may or may not work.", e);
            LogManager.resetConfiguration();
        }
    }

    /**
     * Returns the average response time.
     * 
     * @return the average response time
     */
    public float averageResponseTime()
    {
        return this.avgResponseTime;
    }

    /**
     * Returns the number of queries per hour.
     * 
     * @return the number of queries per hour
     */
    public float queriesPerHour()
    {
        return this.responseCount / ((System.currentTimeMillis() - this.startTime) / 3600000.00f);
    }

    /**
     * Saves all predicates and removes them from memory.
     * 
     * @since 4.7
     */
    public void dumpPredicates()
    {
        for (String botid : this.bots.keySet())
        {
            Bot bot = this.bots.get(botid);
            Map<String, PredicateMap> predicateCache = bot.getPredicateCache();
            for (String userid : predicateCache.keySet())
            {
                M storageMechanism = getStorageMechanism(userid, botid);
                PredicateMap predicateMap = predicateCache.get(userid);
                for (String name : predicateMap.keySet())
                {
                    preparePredicateForStorage(storageMechanism, userid, botid, name, predicateMap.get(name));
                }
                predicateMap.clear();
                savePredicates(storageMechanism, userid, botid);
            }
        }
    }
    
    /**
     * Invoked by {@link #dumpPredicates}; returns whatever storage
     * mechanism is used for preparing saves.
     * 
     * @param userid
     * @param botid
     * @return the storage mechanism
     */
    abstract protected M getStorageMechanism(String userid, String botid);

    /**
     * Invoked by {@link #dumpPredicates}; puts a predicate into
     * whatever storage mechanism (a string, a Properties object, etc.)
     * is going to be used in the actual save operation.
     * 
     * @param mechanism the storage mechanism
     * @param userid
     * @param botid
     * @param name the predicate name
     * @param value the predicate value
     */
    abstract protected void preparePredicateForStorage(M mechanism, String userid, String botid, String name, PredicateValue value);
    

    /**
     * Invoked by {@link #dumpPredicates}; saves the predicates from the given
     * storage mechanism.
     * 
     * @param userid
     * @param botid
     * @param mechanism
     */
    abstract protected void savePredicates(M mechanism, String userid, String botid);
    
    /**
     * Loads a predicate into memory for a given <code>userid</code>. This
     * only applies to Multiplexors that provide long-term storage (others may
     * just do nothing).
     * 
     * @since 4.1.4
     * @param name predicate name
     * @param userid user identifier
     * @param botid
     * @return the predicate value
     * @throws NoSuchPredicateException if there is no predicate with this name
     */
    abstract public String loadPredicate(String name, String userid, String botid) throws NoSuchPredicateException;

    /**
     * Checks whether a given userid and password combination is valid.
     * Multiplexors for which this makes no sense should just return true.
     * 
     * @param userid the userid to check
     * @param botid
     * @param password the password to check
     * @return whether the userid and password combination is valid
     */
    abstract public boolean checkUser(String userid, String password, String botid);

    /**
     * Creates a new user entry, given a userid and password. Multiplexors for
     * which this makes no sense should just return true.
     * 
     * @param userid the userid to use
     * @param botid
     * @param password the password to assign
     * @throws DuplicateUserIDError if the given userid was already found in the
     *             system
     */
    abstract public void createUser(String userid, String password, String botid) throws DuplicateUserIDError;

    /**
     * Changes the password associated with a userid. Multiplexors for which
     * this makes no sense should just return true.
     * 
     * @param userid the userid
     * @param botid
     * @param password the new password
     * @return whether the change was successful
     */
    abstract public boolean changePassword(String userid, String password, String botid);

    /**
     * Returns a count of known userids. This may be defined differently for
     * different multiplexors.
     * 
     * @param botid the botid for which we want a count of known userids
     * @return a count of known userids
     */
    abstract public int useridCount(String botid);
}
