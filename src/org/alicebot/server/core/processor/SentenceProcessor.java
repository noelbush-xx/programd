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
    Further optimizations {4.1.3 [0]1 - November 2001, Noel Bush)
    - changed to extend (not implement) AIMLProcessor (latter is now an abstract class)
      (includes necessary public field "label")
    - moved capitalize method from Substituter to here (was used nowhere else)
*/

package org.alicebot.server.core.processor;

import org.alicebot.server.core.parser.TemplateParser;
import org.alicebot.server.core.parser.XMLNode;


/**
 *  Handles a
 *  <code><a href="http://www.alicebot.org/TR/2001/WD-aiml/#section-sentence">sentence</a></code>
 *  element.
 *
 *  @version    4.1.3
 *  @author     Jon Baer
 *  @author     Thomas Ringate, Pedro Colla
 */
public class SentenceProcessor extends AIMLProcessor
{
    public static final String label = "sentence";


    public String process(int level, XMLNode tag, TemplateParser parser) throws AIMLProcessorException
    {
        if (tag.XMLType == XMLNode.TAG)
        {
            String response = parser.evaluate(level++, tag.XMLChild);
            if (response.equals(EMPTY_STRING))
            {
                return response;
            }
            if (response.trim().length() > 1)
            {
                return response.substring(0, 1).toUpperCase() + response.substring(1);
            }
            else
            {
                return response;
            }
        }
        else
        {
            throw new AIMLProcessorException("<sentence></sentence> must have content!");
        }
    }
}

