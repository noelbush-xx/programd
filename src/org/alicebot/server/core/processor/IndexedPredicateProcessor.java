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
    - type changes to fit with modified InputNormalizer.sentenceSplitter()
    - remove markup from retrieved value
    - changed to handle NoSuchPredicateException
*/

/*
    4.1.4 [00] - December 2001, Noel Bush
    - changed to use PredicateMaster
*/

package org.alicebot.server.core.processor;

import java.util.ArrayList;

import org.alicebot.server.core.Bot;
import org.alicebot.server.core.Bots;
import org.alicebot.server.core.PredicateMaster;
import org.alicebot.server.core.parser.TemplateParser;
import org.alicebot.server.core.parser.XMLNode;
import org.alicebot.server.core.util.DeveloperError;
import org.alicebot.server.core.util.InputNormalizer;
import org.alicebot.server.core.util.Toolkit;
import org.alicebot.server.core.util.Trace;


/**
 *  Processes an indexed predicate.
 *
 *  @version    4.1.3
 *  @author     Jon Baer
 *  @author     Thomas Ringate, Pedro Colla
 *  @author     Noel Bush
 */
abstract public class IndexedPredicateProcessor extends AIMLProcessor
{
    /**
     *  Processes an indexed predicate with <code>dimensions</code> dimensions
     *  (must be either <code>1</code> or <code>2</code>)
     *
     *  @see AIMLProcessor#process
     *  @since 4.1.3
     *
     *  @param name         predicate name
     *  @param dimensions   the number of dimensions (<code>1</code> or <code>2</code>)
     */
    public String process(int level, XMLNode tag, TemplateParser parser,
                              String name, int dimensions)
    {
        // Only 1 or 2 dimensions allowed.
        if (!((dimensions == 1) || (dimensions == 2)) )
        {
            return EMPTY_STRING;
        }

        // Get a valid 2-dimensional index.
        int indexes[] = TemplateParser.getValid2dIndex(tag);

        if (indexes[0] <= 0)
        {
            return EMPTY_STRING;
        }

        // Get entire predicate value at this index (may contain multiple "sentences").
        String value = PredicateMaster.get(name, indexes[0], parser.getUserID(), parser.getBotID());

        // Split predicate into sentences.
        Bot bot = Bots.getBot(parser.getBotID());
        ArrayList sentenceList = bot.sentenceSplit(value);

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
        return Toolkit.removeMarkup((String)sentenceList.get(sentenceCount - indexes[1])).trim();
    }


    /**
     *  Processes an indexed predicate whose values are stored in
     *  the supplied <code>predicates</code> ArrayList.  Currently supports
     *  <i>only</i> a 1-dimensional index (for handling
     *  <code><a href="http://www.alicebot.org/TR/2001/WD-aiml/#section-star">star</a></code>,
     *  <code><a href="http://www.alicebot.org/TR/2001/WD-aiml/#section-thatstar">thatstar</a></code>,
     *  and
     *  <code><a href="http://www.alicebot.org/TR/2001/WD-aiml/#section-topicstar">topicstar</a></code>
     *  elements).
     *
     *  @param predicates   predicate values
     *  @param dimensions   the number of dimensions (<code>1</code> only)
     */
    public String process(int level, XMLNode tag, TemplateParser parser,
                              ArrayList predicates, int dimensions)
    {
        // Only 1 dimension is supported.
        if (dimensions != 1)
        {
            throw new DeveloperError("Wrong number of dimensions: " + dimensions + " != 1");
        }

        // No need to go further if no predicate values are available.
        if (predicates.isEmpty())
        {
            return EMPTY_STRING;
        }

        // Get a valid 1-dimensional index.
        int index = TemplateParser.getValid1dIndex(tag);

        // Vectors are indexed starting with 0, so shift -1.
        index--;

        // Return "" if index exceeds the number of predicates.
        if (index >= predicates.size())
        {
            return EMPTY_STRING;
        }

        // Retrieve and prettify the result.
        return Toolkit.removeMarkup((String)predicates.get(index)).trim();
    }
}
