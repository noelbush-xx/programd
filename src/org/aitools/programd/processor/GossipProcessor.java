/*    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

package org.aitools.programd.processor;

import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.parser.XMLNode;
import org.aitools.programd.util.Globals;
import org.aitools.programd.util.XMLResourceSpec;
import org.aitools.programd.util.logging.Log;
import org.aitools.programd.util.logging.XMLLog;

/**
 *  Handles a
 *  <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-gossip">gossip</a></code>
 *  element.
 *
 *  @version    4.1.3
 *  @author     Jon Baer
 *  @author     Thomas Ringate, Pedro Colla
 */
public class GossipProcessor extends AIMLProcessor
{
    public static final String label = "gossip";

    private static XMLResourceSpec spec = new XMLResourceSpec();
    static {
        spec.path =
            Globals.getProperty(
                "programd.logging.xml.gossip.path",
                "./aiml/gossip.aiml");
        spec.root = "aiml";
    }

    private static final String LINE_SEPARATOR =
        System.getProperty("line.separator", "\n");

    public String process(int level, XMLNode tag, TemplateParser parser)
        throws AIMLProcessorException
    {
        if (tag.XMLType == XMLNode.TAG)
        {
            // Get the gossip.
            String response = parser.evaluate(level++, tag.XMLChild);

            // Put the gossip in the log.
            if (Globals.gossipToXML())
            {
                XMLLog.log(response + LINE_SEPARATOR, spec);
            }
            Log.log(response, Log.GOSSIP);
            return EMPTY_STRING;
        }
        else
        {
            throw new AIMLProcessorException("<gossip></gossip> must have content!");
        }
    }
}
