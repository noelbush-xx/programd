/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.aiml;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

import org.aitools.programd.Core;
import org.aitools.programd.bot.Bot;
import org.aitools.programd.parser.GenericParser;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.XMLKit;

/**
 * Processes an indexed predicate.
 * 
 * @version 4.5
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
abstract public class IndexedPredicateProcessor extends AIMLProcessor
{
    /**
     * Creates a new IndexedPredicateProcessor using the given Core.
     * 
     * @param coreToUse the Core object to use
     */
    public IndexedPredicateProcessor(Core coreToUse)
    {
        super(coreToUse);
    }

    /**
     * Processes an indexed predicate with <code>dimensions</code> dimensions
     * (must be either <code>1</code> or <code>2</code>)
     * 
     * @param element
     * @param parser
     * @see AIMLProcessor#process(Element, TemplateParser)
     * @since 4.1.3
     * @param name predicate name
     * @param dimensions the number of dimensions (<code>1</code> or
     *            <code>2</code>)
     * @return the result of processing the element
     */
    public String process(Element element, TemplateParser parser, String name, int dimensions)
    {
        // Only 1 or 2 dimensions allowed.
        if (!((dimensions == 1) || (dimensions == 2)))
        {
            return EMPTY_STRING;
        }

        // Get a valid 2-dimensional index.
        int indexes[] = GenericParser.getValid2dIndex(element);

        if (indexes[0] <= 0)
        {
            return EMPTY_STRING;
        }

        // Get entire predicate value at this index (may contain multiple
        // "sentences").
        String value = parser.getCore().getPredicateMaster().get(name, indexes[0], parser.getUserID(), parser.getBotID());

        // Split predicate into sentences.
        Bot bot = parser.getCore().getBots().getBot(parser.getBotID());
        List<String> sentenceList = bot.sentenceSplit(value);

        int sentenceCount = sentenceList.size();

        // If there's only one sentence, just return the whole predicate value.
        if (sentenceCount == 0)
        {
            return value;
        }

        // Return "" for a sentence whose index is greater than sentence count.
        if (indexes[1] > sentenceCount)
        {
            return EMPTY_STRING;
        }
        // Get the nth "sentence" (1 is most recent, 2 just before that, etc.)
        return XMLKit.removeMarkup(sentenceList.get(sentenceCount - indexes[1])).trim();
    }

    /**
     * Processes an indexed predicate whose values are stored in the supplied
     * <code>predicates</code> ArrayList. Currently supports <i>only </i> a
     * 1-dimensional index (for handling
     * <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-star">star</a></code>,
     * <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-thatstar">thatstar</a></code>,
     * and
     * <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-topicstar">topicstar</a></code>
     * elements).
     * 
     * @param element
     * @param parser
     * @param predicates predicate values
     * @param dimensions the number of dimensions (<code>1</code> only)
     * @return the result of processing the element
     */
    public String process(Element element, TemplateParser parser, ArrayList<String> predicates, int dimensions)
    {
        // Only 1 dimension is supported.
        if (dimensions != 1)
        {
            throw new DeveloperError("Wrong number of dimensions: " + dimensions + " != 1", new IllegalArgumentException());
        }

        // No need to go further if no predicate values are available.
        if (predicates.isEmpty())
        {
            return EMPTY_STRING;
        }

        // Get a valid 1-dimensional index.
        int index = GenericParser.getValid1dIndex(element);

        // Vectors are indexed starting with 0, so shift -1.
        index--;

        // Return "" if index exceeds the number of predicates.
        if (index >= predicates.size())
        {
            return EMPTY_STRING;
        }

        // Retrieve and prettify the result.
        return XMLKit.removeMarkup(predicates.get(index)).trim();
    }
}