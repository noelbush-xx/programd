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
    - changed back to return gossip tag contents (rather than empty string)
*/

package org.alicebot.server.core.processor;

import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.parser.TemplateParser;
import org.alicebot.server.core.parser.XMLNode;

/**
 *  Handles a
 *  <code><a href="http://www.alicebot.org/TR/2001/WD-aiml/#section-gossip">gossip</a></code>
 *  element.
 *
 *  @version    4.1.3
 *  @author     Jon Baer
 *  @author     Thomas Ringate, Pedro Colla
 */
public class GossipProcessor extends AIMLProcessor
{
    public static final String label = "gossip";


    public String process(int level, XMLNode tag, TemplateParser parser) throws AIMLProcessorException
    {
        if (tag.XMLType == XMLNode.TAG)
        {
            // Get the gossip.
            String response = parser.evaluate(level++, tag.XMLChild);

            // Put the gossip in the log.
            Log.log(response, Log.GOSSIP);
            return response;
        }
        else
        {
            throw new AIMLProcessorException("<gossip></gossip> must have content!");
        }
    }
}

