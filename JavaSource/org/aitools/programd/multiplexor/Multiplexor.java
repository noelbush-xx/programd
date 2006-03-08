/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.multiplexor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aitools.programd.Core;
import org.aitools.programd.CoreSettings;
import org.aitools.programd.bot.Bot;
import org.aitools.programd.bot.Bots;
import org.aitools.programd.graph.Graphmaster;
import org.aitools.programd.graph.Match;
import org.aitools.programd.logging.ChatLogEvent;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.parser.TemplateParserException;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.InputNormalizer;
import org.aitools.programd.util.NoMatchException;
import org.apache.log4j.Logger;

/**
 * <p>
 * &quot;To multiplex&quot; means &quot;to select one from many inputs&quot;. A
 * <code>Multiplexor</code> multiplexes the clients of a bot and keeps track
 * of all their predicate values.
 * </p>
 * <p>
 * The following metaphor was supplied by Richard Wallace: The
 * <code>Multiplexor</code> controls a short &quot;carnival ride&quot; for
 * each user. The Multiplexor puts the client in his/her seat, hands him/her an
 * id card, and closes the door. The client gets one &quot;turn of the
 * crank&quot;. He/she enters his/her id, multiline query, and then receives the
 * reply. The door opens, the Multiplexor ushers him/her out, and seats the next
 * client.
 * </p>
 * 
 * @since 4.1.3
 * @version 4.5
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @author Richard Wallace, Jon Baer
 * @author Thomas Ringate/Pedro Colla
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

    /** The word &quot;value&quot;. */
    protected static final String VALUE = "value";

    /** An asterisk (used in String production) */
    protected static final String ASTERISK = "*";

    /** The predicate empty default. */
    protected String predicateEmptyDefault;

    // Class variables.

    /** The Core that owns this Multiplexor. */
    protected Core core;

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
        this.core = owner;
        this.graphmaster = this.core.getGraphmaster();
        this.bots = this.core.getBots();
        CoreSettings coreSettings = this.core.getSettings();
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
     * Returns the response to a non-internal input, without using a Responder.
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
        Bot bot = this.bots.getBot(botid);

        // Split sentences (after performing substitutions).
        List<String> sentenceList = bot.sentenceSplit(bot.applyInputSubstitutions(input));

        // Get the replies.
        List<String> replies = getReplies(sentenceList, userid, botid);
        
        if (replies == null)
        {
            return null;
        }

        // Start by assuming an empty response.
        StringBuilder responseBuffer = new StringBuilder(EMPTY_STRING);

        // Append each reply to the response.
        for (String reply : replies)
        {
            responseBuffer.append(reply);
        }
        
        String response = responseBuffer.toString();
        
        // Log the response.
        logResponse(input, response, userid, botid);

        // Return the response (may be just EMPTY_STRING!)
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
    public String getInternalResponse(String input, String userid, String botid, TemplateParser parser)
    {
        // Get the requested bot.
        Bot bot = this.bots.getBot(botid);

        // Ready the that and topic predicates for constructing the match path.
        List<String> thatSentences = bot.sentenceSplit(this.predicateMaster.get(THAT, 1, userid, botid));
        String that = InputNormalizer.patternFitIgnoreCase(thatSentences.get(thatSentences.size() - 1));

        if (that.equals(EMPTY_STRING) || that.equals(this.predicateEmptyDefault))
        {
            that = ASTERISK;
        }

        String topic = InputNormalizer.patternFitIgnoreCase(this.predicateMaster.get(TOPIC, userid, botid));
        if (topic.equals(EMPTY_STRING) || topic.equals(this.predicateEmptyDefault))
        {
            topic = ASTERISK;
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
    private List<String> getReplies(List<String> sentenceList, String userid, String botid)
    {
        if (sentenceList == null)
        {
            return null;
        }
        
        // All replies will be assembled in this ArrayList.
        List<String> replies = Collections.checkedList(new ArrayList<String>(sentenceList.size()), String.class);

        // Get the requested bot.
        Bot bot = this.bots.getBot(botid);

        // Ready the that and topic predicates for constructing the match path.
        List<String> thatSentences = bot.sentenceSplit(this.predicateMaster.get(THAT, 1, userid, botid));
        String that = InputNormalizer.patternFitIgnoreCase(thatSentences.get(thatSentences.size() - 1));

        if (that.equals(EMPTY_STRING) || that.equals(this.predicateEmptyDefault))
        {
            that = ASTERISK;
        }

        String topic = InputNormalizer.patternFitIgnoreCase(this.predicateMaster.get(TOPIC, userid, botid));
        if (topic.equals(EMPTY_STRING) || topic.equals(this.predicateEmptyDefault))
        {
            topic = ASTERISK;
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
            matchLogger.debug(String.format("Response %d in %dms. (Average: %.2fms)",
                    this.responseCount, time, this.avgResponseTime));
        }

        // Invoke targeting if appropriate.
        /*
         * if (responseCount % TARGET_SKIP == 0) { if (USE_TARGETING) {
         * Graphmaster.checkpoint(); } }
         */

        // If no replies, return an empty string.
        if (replies.size() == 0)
        {
            replies.add(EMPTY_STRING);
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
    private String getReply(String input, String that, String topic, String userid, String botid)
    {
        // Push the input onto the <input/> stack.
        this.predicateMaster.push(INPUT, input, userid, botid);

        // Create a new TemplateParser.
        TemplateParser parser;
        try
        {
            parser = new TemplateParser(input, userid, botid, this.core);
        }
        catch (TemplateParserException e)
        {
            throw new DeveloperError("Error occurred while creating new TemplateParser.", e);
        }

        String reply = getMatchResult(input, that, topic, userid, botid, parser);
        if (reply == null)
        {
            logger.error("getMatchReply generated a null reply!", new NullPointerException());
            return EMPTY_STRING;
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
    private String getMatchResult(String input, String that, String topic, String userid, String botid, TemplateParser parser)
    {
        // Show the input path.
        if (matchLogger.isDebugEnabled())
        {
            matchLogger.debug(String.format("[INPUT (%s)] %s : %s : %s : %s",
                    userid, input, that, topic, botid));
        }

        Match match = null;

        try
        {
            match = this.graphmaster.match(InputNormalizer.patternFitIgnoreCase(input), that, topic, botid);
        }
        catch (NoMatchException e)
        {
            logger.warn(e.getMessage());
            return EMPTY_STRING;
        }

        if (match == null)
        {
            logger.warn(String.format("No match found for input \"%s\".", input));
            return EMPTY_STRING;
        }

        if (matchLogger.isDebugEnabled())
        {
            matchLogger.debug(String.format("[MATCHED] %s (\"%s\")",
                    match.getPath(), match.getFileName()));
        }

        ArrayList<String> stars = match.getInputStars();
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
            Logger.getLogger("programd").error("Error while processing response: " + e.getExplanatoryMessage(), e);

            // Set response to empty string.
            return EMPTY_STRING;
        }
        catch (DeveloperError e)
        {
            // Log the error message.
            Logger.getLogger("programd").error("Error while processing response: " + e.getCause().getMessage(), e);

            // Set response to empty string.
            return EMPTY_STRING;
        }

        // Record activation, if targeting is in use.
        // Needs review in light of multi-bot update
        /*
         * if (USE_TARGETING) { Nodemapper matchNodemapper =
         * match.getNodemapper(); if (matchNodemapper == null) {
         * Trace.devinfo("Match nodemapper is null!"); } else { Set<Object>
         * activations = (Set<Object>)
         * matchNodemapper.get(Graphmaster.ACTIVATIONS); if (activations ==
         * null) { activations = new HashSet<Object>(); } String path =
         * match.getPath() + SPACE + Graphmaster.PATH_SEPARATOR + SPACE +
         * inputIgnoreCase + SPACE + Graphmaster.PATH_SEPARATOR + SPACE + that +
         * SPACE + Graphmaster.PATH_SEPARATOR + SPACE + topic + SPACE +
         * Graphmaster.PATH_SEPARATOR + SPACE + botid + SPACE +
         * Graphmaster.PATH_SEPARATOR + SPACE + reply; if
         * (!activations.contains(path)) { activations.add(path);
         * match.getNodemapper().put(Graphmaster.ACTIVATIONS, activations);
         * Graphmaster.activatedNode(match.getNodemapper()); } } }
         */
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
    private void logResponse(String input, String response, String userid, String botid)
    {
        logger.callAppenders(new ChatLogEvent(botid, userid, input, response));
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
     * Saves a predicate for a given <code>userid</code>. This only applies
     * to Multiplexors that provide long-term storage (others may just do
     * nothing).
     * 
     * @since 4.1.4
     * @param name predicate name
     * @param value predicate value
     * @param userid user identifier
     * @param botid
     */
    abstract public void savePredicate(String name, String value, String userid, String botid);

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