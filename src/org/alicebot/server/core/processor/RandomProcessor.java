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
    - complete javadoc
    - made all imports explicit
    - inlined some computations of needless temporary variables
    - added more efficient random method from Jay Myers <jay@twinact.com>
*/

/*
    Further optimizations {4.1.3 [0]1 - November 2001, Noel Bush)
    - changed to extend (not implement) AIMLProcessor (latter is now an abstract class)
      (includes necessary public field "label")
*/

package org.alicebot.server.core.processor;

import java.util.Random;

import org.alicebot.server.core.parser.AIMLParser;
import org.alicebot.server.core.parser.XMLNode;


/**
 *  Handles a
 *  <code><a href="http://www.alicebot.org/TR/2001/WD-aiml/#section-random">random</a></code>
 *  element.
 *
 *  @version    4.1.3
 *  @author     Jon Baer
 *  @author     Thomas Ringate, Pedro Colla
 *  @author     Jay Myers
 */
public class RandomProcessor extends AIMLProcessor
{
    public static final String label = "random";

    /** The string &quot;li&quot;, for convenience. */
    private static final String LI = "li";


    /** A random number generator. */
    private static Random randomNumberGenerator = new Random();

    /**
     *  Counts the number of &lt;li&gt;&lt;/li&gt; elements
     *  it contains, and chooses one at random.
     */
    public String process(int level, String userid, XMLNode tag, AIMLParser parser) throws AIMLProcessorException
    {
        if (tag.XMLType == XMLNode.TAG)
        {
            // Empty <random></random> doesn't produce anything.
            if (tag.XMLChild == null)
            {
                return EMPTY_STRING;
            }

            int nodeCount = parser.nodeCount(LI, tag.XMLChild, false);

            // Zero <li></li> children means that there's nothing to do.
            if (nodeCount == 0)
            {
                return EMPTY_STRING;
            }

            XMLNode node;

            // Only one <li></li> child means we don't have to pick anything.
            if (nodeCount == 1)
            {
                node = parser.getNode(LI, tag.XMLChild, 1);
                return parser.evaluate(level++, userid, node.XMLChild);
            }

            // Select a random element of the listitem.
            return parser.evaluate(level++, userid,
                                   parser.getNode(
                                     LI, tag.XMLChild, 
                                     (int)Math.ceil(nodeCount * randomNumberGenerator.nextDouble())).XMLChild);
        }
        else
        {
            throw new AIMLProcessorException("<random></random> must have content!");
        }
    }
}
