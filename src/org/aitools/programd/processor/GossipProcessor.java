/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Element;

import org.aitools.programd.Core;
import org.aitools.programd.parser.TemplateParser;

/**
 * Handles a
 * <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-gossip">gossip</a></code>
 * element.
 * 
 * @version 4.1.3
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 */
public class GossipProcessor extends AIMLProcessor
{
    public static final String label = "gossip";
    
    private static final Logger logger = Logger.getLogger("programd.gossip");

    public GossipProcessor(Core coreToUse)
    {
        super(coreToUse);
    }
    
    public String process(Element element, TemplateParser parser)
    {
        // Get the gossip.
        String response = parser.evaluate(element.getChildNodes());

        // Put the gossip in the log.
        logger.log(Level.INFO, response);
        return EMPTY_STRING;
    }
}