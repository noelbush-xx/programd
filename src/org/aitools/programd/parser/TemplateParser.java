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

import org.aitools.programd.processor.AIMLProcessorException;
import org.aitools.programd.processor.AIMLProcessorRegistry;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.util.Globals;
import org.aitools.programd.util.logging.Log;

/**
 * <code>TemplateParser</code> is still a primitive class, implementing not a
 * &quot;real&quot; XML parser, but just enough (hopefully) to get the job done.
 */
public class TemplateParser extends GenericParser
{
    /**
     * The values captured from the input by wildcards in the
     * <code>pattern</code>.
     */
    private ArrayList<String> inputStars = new ArrayList<String>();

    /**
     * The values captured from the input path by wildcards in the
     * <code>that</code>.
     */
    private ArrayList<String> thatStars = new ArrayList<String>();

    /**
     * The values captured from the input path by wildcards in the
     * <code>topic</code>.
     */
    private ArrayList<String> topicStars = new ArrayList<String>();

    /**
     * The input that matched the <code>pattern</code> associated with this
     * template (helps to avoid endless loops).
     */
    private ArrayList<String> inputs = new ArrayList<String>();

    /** The userid for which this parser is used. */
    private String userid;

    /** The botid on whose behalf this parser is working. */
    private String botid;

    /**
     * Initializes an <code>TemplateParser</code>. The <code>input</code>
     * is a required parameter!
     * 
     * @param input
     *            the input that matched the <code>pattern</code> associated
     *            with this template (helps to avoid endless loops)
     * @throws TemplateParserException
     *             if the <code>input</code> is null
     */
    public TemplateParser(String input, String useridToUse, String botidToUse) throws TemplateParserException
    {
        if (input == null)
        {
            throw new TemplateParserException("No input supplied for TemplateParser!");
        } 
        this.inputs.add(input);
        this.userid = useridToUse;
        this.botid = botidToUse;
        super.processorRegistry = AIMLProcessorRegistry.getSelf();
    } 

    /**
     * Processes the AIML within and including a given AIML element.
     * 
     * @param level
     *            the current level in the XML trie
     * @param tag
     *            the tag being evaluated
     * @return the result of processing the tag
     * @throws AIMLProcessorException
     *             if the AIML cannot be processed
     */
    public String processTag(int level, XMLNode tag) throws ProcessorException
    {
        try
        {
            return super.processTag(level, tag);
        } 
        // A ProcessorException at this point can mean several things.
        catch (ProcessorException e0)
        {
            // It could be a deprecated tag.
            if (Globals.supportDeprecatedTags())
            {
                try
                {
                    return DeprecatedAIMLParser.processTag(level, this.userid, tag, this);
                } 
                catch (UnknownDeprecatedAIMLException e1)
                {
                    // For now, do nothing (drop down to next).
                } 
            } 
            // It could also be a non-AIML tag.
            if (Globals.nonAIMLRequireNamespaceQualification())
            {
                // If namespace qualification is required, check for a colon.
                if (tag.XMLData.indexOf(COLON) == -1)
                {
                    throw new AIMLProcessorException("Unknown element \"" + tag.XMLData + "\"");
                } 
            } 
            // But if namespace qualification is not required, don't care.
            return formatTag(level, tag);
        } 
        catch (StackOverflowError e)
        {
            Log.userinfo("Stack overflow error processing " + tag.XMLData + " tag.", Log.ERROR);
            return EMPTY_STRING;
        } 
    } 

    /**
     * Adds an input to the inputs list (for avoiding infinite loops).
     * 
     * @param input
     *            the input to add
     */
    public void addInput(String input)
    {
        this.inputs.add(input);
    } 

    /**
     * Returns the input that matched the <code>pattern</code> associated with
     * this template.
     * 
     * @return the input that matched the <code>pattern</code> associated with
     *         this template
     */
    public ArrayList getInputs()
    {
        return this.inputs;
    } 

    /**
     * Returns the values captured from the input path by wildcards in the
     * <code>pattern</code>.
     * 
     * @return the values captured from the input path by wildcards in the
     *         <code>pattern</code>
     */
    public ArrayList getInputStars()
    {
        return this.inputStars;
    } 

    /**
     * Returns the the values captured from the input path by wildcards in the
     * <code>that</code>.
     * 
     * @return the values captured from the input path by wildcards in the
     *         <code>that</code>
     */
    public ArrayList getThatStars()
    {
        return this.thatStars;
    } 

    /**
     * Returns the values captured from the input path by wildcards in the
     * <code>topic name</code>.
     * 
     * @return the values captured from the input path by wildcards in the
     *         <code>topic name</code>
     */
    public ArrayList getTopicStars()
    {
        return this.topicStars;
    } 

    /**
     * Sets the <code>inputStars</code> list.
     * 
     * @param stars
     *            values captured from the input path by wildcards in the
     *            <code>pattern</code>
     */
    public void setInputStars(ArrayList<String> stars)
    {
        this.inputStars = stars;
    } 

    /**
     * Sets the <code>thatStars</code> list.
     * 
     * @param stars
     *            values captured from the input path by wildcards in the
     *            <code>that</code>
     */
    public void setThatStars(ArrayList<String> stars)
    {
        this.thatStars = stars;
    } 

    /**
     * Sets the <code>topicStars</code> Vector.
     * 
     * @param stars
     *            captured from the input path by wildcards in the
     *            <code>topic name</code>
     */
    public void setTopicStars(ArrayList<String> stars)
    {
        this.topicStars = stars;
    } 

    /**
     * Returns the userid.
     * 
     * @return the userid
     */
    public String getUserID()
    {
        return this.userid;
    } 

    /**
     * Returns the botid.
     * 
     * @return the botid
     */
    public String getBotID()
    {
        return this.botid;
    } 
}