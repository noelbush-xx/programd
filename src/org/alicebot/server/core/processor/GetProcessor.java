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
*/

/*
    More fixes (4.1.3 [02] - November 2001, Noel Bush)
    - added handling of NoSuchPredicateExpression
*/

/*
    4.1.4 [00] - December 2001, Noel Bush
    - changed to use PredicateMaster
*/

package org.alicebot.server.core.processor;

import org.alicebot.server.core.PredicateMaster;
import org.alicebot.server.core.parser.TemplateParser;
import org.alicebot.server.core.parser.XMLNode;
import org.alicebot.server.core.util.Toolkit;
import org.alicebot.server.core.util.Trace;


/**
 *  Handles a
 *  <code><a href="http://www.alicebot.org/TR/2001/WD-aiml/#section-get">get</a></code>
 *  element.
 *
 *  @version    4.1.3
 *  @author     Jon Baer
 *  @author     Thomas Ringate, Pedro Colla
 */
public class GetProcessor extends AIMLProcessor
{
    public static final String label = "get";


    public String process(int level, XMLNode tag, TemplateParser parser) throws AIMLProcessorException
    {
        if (tag.XMLType == XMLNode.EMPTY)
        {
            String name = Toolkit.getAttributeValue(NAME, tag.XMLAttr);

            if (name.equals(EMPTY_STRING))
            {
                throw new AIMLProcessorException("<get/> must have a non-empty name attribute.");
            }
            return PredicateMaster.get(name, parser.getUserID(), parser.getBotID());
        }
        else
        {
            throw new AIMLProcessorException("<get/> cannot have content!");
        }
    }
}

