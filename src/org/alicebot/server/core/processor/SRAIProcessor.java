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

import org.alicebot.server.core.ActiveMultiplexor;
import org.alicebot.server.core.Globals;
import org.alicebot.server.core.Graphmaster;
import org.alicebot.server.core.parser.AIMLParser;
import org.alicebot.server.core.parser.XMLNode;
import org.alicebot.server.core.logging.Trace;

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
     *  <parser>
     *  First, all elements contained within a given &lt;srai/&gt;
     *  are evaluated, and the result is recursively fed as input
     *  to the pattern matching process. The result of such evaluation
     *  (which itself might be recursive) is returned as the result.
     *
     *  @see AIMLProcessor#process
     */
    public String process(int level, String userid, XMLNode tag, AIMLParser parser) throws InvalidAIMLException
    {
        if (level >= Graphmaster.MAX_DEPTH)
        {
            throw new InvalidAIMLException("Exceeded maximum depth setting for: \"" + parser.formatTag(level, userid, tag) + "\"");
        }

        if (Globals.showMatchTrace())
        {
            Trace.userinfo("Symbolic Reduction:");
        }

        if (tag.XMLType == XMLNode.TAG)
        {
            // This may look ugly, but it avoids use of a temporary variable.
            return ActiveMultiplexor.StaticSelf.getInternalResponse(
                       parser.evaluate(level++, userid, tag.XMLChild).trim().replace('*',' '), userid);
        }
        else
        {
            throw new InvalidAIMLException("<srai></srai> must have content!");
        }
    }
}