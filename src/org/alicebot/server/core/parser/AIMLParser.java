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

import java.util.StringTokenizer;
import java.util.Vector;

import org.alicebot.server.core.Globals;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.logging.Trace;
import org.alicebot.server.core.processor.AIMLProcessor;
import org.alicebot.server.core.processor.InvalidAIMLException;
import org.alicebot.server.core.processor.ProcessorException;
import org.alicebot.server.core.util.LinkedList;
import org.alicebot.server.core.util.LinkedListItr;


/**
 *  <code>AIMLParser</code> is still a primitive class, implementing not a
 *  &quot;real&quot; XML parser, but just enough (hopefully) to get the job done.
 */
public class AIMLParser extends GenericParser
{
    /** The values captured from the input by wildcards in the <code>pattern</code>. */
    private Vector INPUT_STARS;

    /** The values captured from the input path by wildcards in the <code>that</code>. */
    private Vector THAT_STARS;

    /** The values captured from the input path by wildcards in the <code>topic</code>. */
    private Vector TOPIC_STARS;


    /**
     *  Initializes an <code>AIMLParser</code>.
     */
    public AIMLParser()
    {
        INPUT_STARS = new Vector();
        THAT_STARS = new Vector();
        TOPIC_STARS = new Vector();
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
     *  @throws InvalidAIMLException if the AIML cannot be processed
     */
    public String processTag(int level, String userid, XMLNode tag) throws InvalidAIMLException
    {
        try
        {
            return super.processTag(level, userid, tag);
        }
        catch (ProcessorException e)
        {
            // Drop down to the rest.
        }
        if (Globals.supportDeprecatedTags())
        {
            try
            {
                return DeprecatedAIMLParser.processTag(level, userid, tag, this);
            }
            catch (UnknownDeprecatedAIMLException e)
            {
                // For now, do nothing (drop down to next).
            }
        }
        if (Globals.nonAIMLRequireNamespaceQualification())
        {
            if (tag.XMLData.indexOf(COLON) == -1)
            {
                throw new InvalidAIMLException("Unknown element \"" + tag.XMLData + "\"");
            }
        }
        // Any tag not caught by now is an unknown tag, so expand it into text.
        return formatTag(level, userid, tag);
    }


    /**
     *  @return values captured from the input path by wildcards in the <code>pattern</code>
     */
    public Vector getInputStars()
    {
        return INPUT_STARS;
    }


    /**
     *  @return values captured from the input path by wildcards in the <code>that</code>
     */
    public Vector getThatStars()
    {
        return THAT_STARS;
    }


    /**
     *  @return values captured from the input path by wildcards in the <code>topic name</code>
     */
    public Vector getTopicStars()
    {
        return TOPIC_STARS;
    }


    /**
     *  Sets the <code>INPUT_STARS</code> Vector.
     *
     *  @param  values captured from the input path by wildcards in the <code>pattern</code>
     */
    public void setInputStars(Vector vector)
    {
        INPUT_STARS = vector;
    }


    /**
     *  Sets the <code>THAT_STARS</code> Vector.
     *
     *  @param  values captured from the input path by wildcards in the <code>that</code>
     */
    public void setThatStars(Vector vector)
    {
        THAT_STARS = vector;
    }


    /**
     *  Sets the <code>TOPIC_STARS</code> Vector.
     *
     *  @param  values captured from the input path by wildcards in the <code>topic name</code>
     */
    public void setTopicStars(Vector vector)
    {
        TOPIC_STARS = vector;
    }
}
