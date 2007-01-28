/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.parser;

import java.util.ArrayList;
import java.util.List;

import org.aitools.programd.Core;
import org.aitools.programd.graph.Match;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.processor.aiml.AIMLProcessor;
import org.jdom.Element;

/**
 * <code>TemplateParser</code> parses templates!  :-)
 */
public class TemplateParser extends GenericParser<AIMLProcessor>
{
    /** The inputs that matched the <code>pattern</code> associated with this template (helps to avoid endless loops). */
    private ArrayList<String> _inputs = new ArrayList<String>();

    /** The thats that matched the <code>pattern</code> associated with this template (helps to avoid endless loops). */
    private ArrayList<String> _thats = new ArrayList<String>();

    /** The topics that matched the <code>pattern</code> associated with this template (helps to avoid endless loops). */
    private ArrayList<String> _topics = new ArrayList<String>();

    /** The match(es) responsible for this template parser. */
    private ArrayList<Match> _matches = new ArrayList<Match>();

    /** The userid for which this parser is used. */
    private String _userid;

    /** The botid on whose behalf this parser is working. */
    private String _botid;

    /**
     * @param core
     */
    public TemplateParser(Core core)
    {
        this("", "", "", "", "", core);
    }
    
    /**
     * @param userid
     * @param botid
     * @param core
     */
    public TemplateParser(String userid, String botid, Core core)
    {
        this("", "", "", userid, botid, core);
    }
    
    /**
     * @param input 
     * @param that 
     * @param topic 
     * @param userid the userid for whom the template will be parsed
     * @param botid the botid for whom the template will be parsed
     * @param core the Core in use
     */
    public TemplateParser(String input, String that, String topic, String userid, String botid, Core core)
    {
        super(core.getAIMLProcessorRegistry(), core);
        this._inputs.add(input);
        this._thats.add(that);
        this._topics.add(topic);
        this._userid = userid;
        this._botid = botid;
    }

    /**
     * @param inputs
     * @param thats 
     * @param topics 
     * @param userid the userid for whom the template will be parsed
     * @param botid the botid for whom the template will be parsed
     * @param core the Core in use
     */
    public TemplateParser(List<String> inputs, List<String> thats, List<String> topics, String userid, String botid,
            Core core)
    {
        super(core.getAIMLProcessorRegistry(), core);
        this._inputs.addAll(inputs);
        this._thats.addAll(thats);
        this._topics.addAll(topics);
        this._userid = userid;
        this._botid = botid;
    }

    /**
     * Processes the AIML within and including a given AIML element.
     * 
     * @param element the elment to process
     * @return the result of processing the tag
     * @throws ProcessorException if the AIML cannot be processed
     */
    public String processTag(Element element) throws ProcessorException
    {
        try
        {
            return super.evaluate(element);
        }
        catch (StackOverflowError e)
        {
            this._logger.error(String.format("Stack overflow error processing <%s/>.", element.getName()));
            return "";
        }
    }

    /**
     * Adds a {@link Match} object to the list of matches.
     * 
     * @param match
     */
    public void addMatch(Match match)
    {
        this._matches.add(match);
    }

    /**
     * @return the most recent match
     */
    public Match getMostRecentMatch()
    {
        return this._matches.get(this._matches.size() - 1);
    }

    /**
     * Adds an input to the inputs list (for avoiding infinite loops).
     * 
     * @param input the input to add
     */
    public void addInput(String input)
    {
        this._inputs.add(input);
    }

    /**
     * Returns the input that matched the <code>pattern</code> associated with this template.
     * 
     * @return the input that matched the <code>pattern</code> associated with this template
     */
    public ArrayList<String> getInputs()
    {
        return this._inputs;
    }

    /**
     * Adds a that to the thats list (for avoiding infinite loops).
     * 
     * @param that the that to add
     */
    public void addThat(String that)
    {
        this._thats.add(that);
    }

    /**
     * Returns the that that matched the <code>that</code> associated with this template.
     * 
     * @return the that that matched the <code>that</code> associated with this template
     */
    public ArrayList<String> getThats()
    {
        return this._thats;
    }

    /**
     * Adds a topic to the topics list (for avoiding infinite loops).
     * 
     * @param topic the topic to add
     */
    public void addTopic(String topic)
    {
        this._topics.add(topic);
    }

    /**
     * Returns the topics that matched the <code>topic</code> associated with this template.
     * 
     * @return the topic that matched the <code>topic</code> associated with this template
     */
    public ArrayList<String> getTopics()
    {
        return this._topics;
    }

    /**
     * @return the userid
     */
    public String getUserID()
    {
        return this._userid;
    }

    /**
     * @return the botid
     */
    public String getBotID()
    {
        return this._botid;
    }
}
