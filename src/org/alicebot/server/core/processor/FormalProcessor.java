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
    Further fixes and optimizations (4.1.3 [01] - November 2001, Noel Bush)
    - moved method from Substituter to here
*/

package org.alicebot.server.core.processor;

import java.util.StringTokenizer;

import org.alicebot.server.core.parser.TemplateParser;
import org.alicebot.server.core.parser.XMLNode;


/**
 *  Handles a
 *  <code><a href="http://www.alicebot.org/TR/2001/WD-aiml/#section-formal">formal</a></code>
 *  element.
 *
 *  @version    4.1.3
 *  @author     Jon Baer
 */
public class FormalProcessor extends AIMLProcessor
{
    public static final String label = "formal";

    // Convenience constants.
    private static final String SPACE = " ";

    public String process(int level, XMLNode tag, TemplateParser parser) throws AIMLProcessorException
    {
        if (tag.XMLType == XMLNode.TAG)
        {
            String response = parser.evaluate(level++, tag.XMLChild);
            if (response.equals(EMPTY_STRING))
            {
                return response;
            }
            StringTokenizer tokenizer = new StringTokenizer(response, SPACE);
            StringBuffer result = new StringBuffer(response.length());  
            while (tokenizer.hasMoreTokens())
            {
                String word = tokenizer.nextToken();
                if (result.length() > 0)
                {
                    result.append(SPACE);
                }
                result.append(word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase());
            }
            return result.toString();
        }
        else
        {
            throw new AIMLProcessorException("<formal></formal> must have content!");
        }
    }
}

