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
    - replaced String literals with constants (except for 0.9 bot predicates)
    - removed unused depth, length and interpreter fields and deprecated constructors using them
    - removed extraneous "extends Object" :-)
    - removed use of IdProcessor and DateProcessor (although they might return later)
    - renamed variables with more descriptive names
    - removed unused global fields (globals, classifier, graphmaster, bot)
    - changed tests like if(someVariable == true) to if(someVariable)
    - removed non-AIML1.0.1 support for <get></get> (only atomic form is supported)
    - inserted commented-out support for AIML 1.0.1 <gender/>
    - changed name of virtualtag() to shortcutTag()
    - changed formattag() to formatTag()
    - inlined some unnecessary variables that were created only for debugging
    - changed to use of ActiveMultiplexor
    - added getValid2dIndex method
    - added getValid1dIndex method
    - changed method names "countnode" to "nodeCount", "getnode" to "getNode"
    - made INPUT_STAR(S), THAT_STAR(S) and TOPIC_STAR(S) private and created get methods
*/

/*
    Further optimization (4.1.3 [01] - November 2001, Noel Bush
    - introduced pluggable processor usage
    - removed constructor that took Interpreter argument
    - moved processListItem to ConditionProcessor
    - moved processing of deprecated tags to DeprecatedAIMLParser and made optional
      (via setting in Globals)
    - changed to extend GenericParser and moved the following methods there:
      - getArg (renamed to getAttributeValue)
      - formatTag
      - nodeCount
      - getNode
      - processTag
      - processResponse
    - entirely removed constructor that takes depth as a parameter
*/

package org.alicebot.server.core.parser;

import java.util.Vector;

import org.alicebot.server.core.Globals;
import org.alicebot.server.core.processor.AIMLProcessorException;
import org.alicebot.server.core.processor.AIMLProcessorRegistry;
import org.alicebot.server.core.processor.ProcessorException;
import org.alicebot.server.core.processor.ProcessorRegistry;


/**
 *  <code>AIMLParser</code> is still a primitive class, implementing not a
 *  &quot;real&quot; XML parser, but just enough (hopefully) to get the job done.
 */
public class AIMLParser extends GenericParser
{
    /** The values captured from the input by wildcards in the <code>pattern</code>. */
    private Vector inputStars = new Vector();

    /** The values captured from the input path by wildcards in the <code>that</code>. */
    private Vector thatStars = new Vector();

    /** The values captured from the input path by wildcards in the <code>topic</code>. */
    private Vector topicStars = new Vector();

    /** The input that matched the <code>pattern</code> associated with this template (helps to avoid endless loops). */
    private Vector inputs = new Vector();


    /**
     *  Initializes an <code>AIMLParser</code>.
     *  The <code>input</code> is a required parameter!
     *
     *  @param input    the input that matched the <code>pattern</code>
     *                  associated with this template (helps to avoid endless loops)
     *
     *  @throws AIMLParserException if the <code>input</code> is null
     */
    public AIMLParser(String input) throws AIMLParserException
    {
        if (input == null)
        {
            throw new AIMLParserException("No input supplied for AIMLParser!");
        }
        this.inputs.add(input);
        super.processorRegistry = Globals.getAIMLProcessorRegistry();
    }


    /**
     *  Processes the AIML within and including a given AIML element.
     *
     *  @param level    the current level in the XML trie
     *  @param userid   the user identifier
     *  @param tag      the tag being evaluated
     *
     *  @return the result of processing the tag
     *
     *  @throws AIMLProcessorException if the AIML cannot be processed
     */
    public String processTag(int level, String userid, XMLNode tag) throws ProcessorException
    {
        try
        {
            return super.processTag(level, userid, tag);
        }
        // A ProcessorException at this point can mean several things.
        catch (ProcessorException e0)
        {
            // It could be a deprecated tag.
            if (Globals.supportDeprecatedTags())
            {
                try
                {
                    return DeprecatedAIMLParser.processTag(level, userid, tag, this);
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
            return formatTag(level, userid, tag);
        }
    }


    /**
     *  Adds an input to the inputs vector (for avoiding infinite loops).
     *
     *  @param input    the input to add
     */
    public void addInput(String input)
    {
        this.inputs.add(input);
    }


    /**
     *  Returns the input that matched the <code>pattern</code> associated with this template.
     *
     *  @return the input that matched the <code>pattern</code> associated with this template
     */
    public Vector getInputs()
    {
        return this.inputs;
    }


    /**
     *  Returns the values captured from the input path by wildcards in the <code>pattern</code>.
     *
     *  @return the values captured from the input path by wildcards in the <code>pattern</code>
     */
    public Vector getInputStars()
    {
        return this.inputStars;
    }


    /**
     *  Returns the the values captured from the input path by wildcards in the <code>that</code>.
     *
     *  @return the values captured from the input path by wildcards in the <code>that</code>
     */
    public Vector getThatStars()
    {
        return this.thatStars;
    }


    /**
     *  Returns the values captured from the input path by wildcards in the <code>topic name</code>.
     *
     *  @return the values captured from the input path by wildcards in the <code>topic name</code>
     */
    public Vector getTopicStars()
    {
        return this.topicStars;
    }


    /**
     *  Sets the <code>inputStars</code> Vector.
     *
     *  @param  values captured from the input path by wildcards in the <code>pattern</code>
     */
    public void setInputStars(Vector vector)
    {
        this.inputStars = vector;
    }


    /**
     *  Sets the <code>thatStars</code> Vector.
     *
     *  @param  values captured from the input path by wildcards in the <code>that</code>
     */
    public void setThatStars(Vector vector)
    {
        this.thatStars = vector;
    }


    /**
     *  Sets the <code>topicStars</code> Vector.
     *
     *  @param  values captured from the input path by wildcards in the <code>topic name</code>
     */
    public void setTopicStars(Vector vector)
    {
        this.topicStars = vector;
    }
}
