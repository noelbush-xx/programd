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
    - changed some wording and calls to methods (use "predicate")
*/

/*
    Further optimizations {4.1.3 [0]1 - November 2001, Noel Bush)
    - changed to extend (not implement) AIMLProcessor (latter is now an abstract class)
      (includes necessary public field "label")
*/

package org.alicebot.server.core.processor;

import org.alicebot.server.core.Bots;
import org.alicebot.server.core.parser.TemplateParser;
import org.alicebot.server.core.parser.XMLNode;
import org.alicebot.server.core.util.Toolkit;


/**
 *  Handles a
 *  <code><a href="http://www.alicebot.org/TR/2001/WD-aiml/#section-bot">bot</a></code>
 *  element.
 *
 *  @see AIMLProcessor
 */
public class BotProcessor extends AIMLProcessor
{
    public static final String label = "bot";


    /**
     *  Retrieves the value of the desired bot predicate from
     *  {@link Globals}.
     *
     *  @see    AIMLProcessor#process
     */
    public String process(int level, XMLNode tag, TemplateParser parser) throws AIMLProcessorException
    {
        if (tag.XMLType == XMLNode.EMPTY)
        {
            String name = Toolkit.getAttributeValue(NAME, tag.XMLAttr);
            if (name.equals(EMPTY_STRING))
            {
                return name;
            }
            return Bots.getBot(parser.getBotID()).getPropertyValue(name);
        }
        else
        {
            throw new AIMLProcessorException ("<bot/> cannot contain element content!");
        }
    }
}

