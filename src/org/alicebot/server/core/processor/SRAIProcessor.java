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
    - inlined method calls to avoid temporary variable
*/

/*
    Further optimizations {4.1.3 [01] - November 2001, Noel Bush)
    - changed to extend (not implement) AIMLProcessor (latter is now an abstract class)
      (includes necessary public field "label")
*/

/*
    More fixes (4.1.3 [02] - November 2001, Noel Bush)
    - added match trace indicator
    - changed to use getInternalResponse() method of ActiveMultiplexor
*/

package org.alicebot.server.core.processor;

import java.util.Iterator;

import org.alicebot.server.core.ActiveMultiplexor;
import org.alicebot.server.core.Globals;
import org.alicebot.server.core.Graphmaster;
import org.alicebot.server.core.Multiplexor;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.Trace;
import org.alicebot.server.core.parser.AIMLParser;
import org.alicebot.server.core.parser.XMLNode;


/**
 *  Implements the &lt;srai/&gt; element.
 *
 *  @version    4.1.3
 *  @author     Jon Baer
 *  @author     Thomas Ringate, Pedro Colla
 */
public class SRAIProcessor extends AIMLProcessor
{
    public static final String label = "srai";


    /**
     *  Processes a &lt;srai/&gt; element.
     *
     *  First, all elements contained within a given &lt;srai/&gt;
     *  are evaluated, and the result is recursively fed as input
     *  to the pattern matching process. The result of such evaluation
     *  (which itself might be recursive) is returned as the result.
     *
     *  @see AIMLProcessor#process
     */
    public String process(int level, String userid, XMLNode tag, AIMLParser parser) throws AIMLProcessorException
    {
        if (level >= Graphmaster.MAX_DEPTH)
        {
            throw new AIMLProcessorException("Exceeded maximum depth setting for: \"" + parser.formatTag(level, userid, tag) + "\"");
        }
        if (tag.XMLType == XMLNode.TAG)
        {
            // Check for infinite loops.
            if (tag.XMLChild.size() == 1)
            {
                XMLNode sraiChild = (XMLNode)tag.XMLChild.get(0);

                if (sraiChild.XMLType == XMLNode.DATA)
                {
                    String sraiContent = sraiChild.XMLData;
                    Iterator inputsIterator = parser.getInputs().iterator();

                    while (inputsIterator.hasNext())
                    {
                        String input = (String)inputsIterator.next();

                        if (sraiContent.equalsIgnoreCase(input))
                        {
                            // If possible, substitute the infinite loop text.
                            if (!sraiContent.equalsIgnoreCase(Globals.getInfiniteLoopInput()))
                            {
                                sraiChild.XMLData = Globals.getInfiniteLoopInput();
                                Log.userinfo("Infinite loop detected. Substituting infinite loop input.", Log.ERROR);
                            }
                            else
                            {
                                Log.userinfo("Infinite loop detected. Cannot substitute infinite loop input.", Log.ERROR);
                                return EMPTY_STRING;
                            }
                        }
                    }
                    parser.addInput(sraiContent);
                }
            }
            if (Globals.showMatchTrace())
            {
                Trace.userinfo("Symbolic Reduction:");
            }
            return ActiveMultiplexor.getInstance().getInternalResponse(
                parser.evaluate(level++, userid, tag.XMLChild), userid, parser);
        }
        else
        {
            throw new AIMLProcessorException("<srai></srai> must have content!");
        }
    }
}
