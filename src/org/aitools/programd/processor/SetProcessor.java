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

import org.aitools.programd.multiplexor.PredicateMaster;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.parser.XMLNode;
import org.aitools.programd.util.XMLKit;

/**
 *  <p>
 *  Handles a
 *  <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-set">set</a></code>
 *  element.
 *  </p>
 *  <p>
 *  This is currently <i>not</i> AIML 1.0.1-compliant, because it
 *  fails to account for
 *  &quot;<a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-aiml-predicate-behaviors">return-name-when-set</a>&quot;
 *  predicates.
 *  </p>
 */
public class SetProcessor extends AIMLProcessor
{
    public static final String label = "set";

    public String process(int level, XMLNode tag, TemplateParser parser)
        throws AIMLProcessorException
    {
        if (tag.XMLType == XMLNode.TAG)
        {
            String name = XMLKit.getAttributeValue(NAME, tag.XMLAttr);

            // Can't process a predicate with no name.
            if (name.equals(EMPTY_STRING))
            {
                throw new AIMLProcessorException("<set></set> must have a name attribute!");
            }

            // Return the result of setting this predicate value (should check its type, but not yet implemented).
            return PredicateMaster.set(
                name,
                parser.evaluate(level++, tag.XMLChild),
                parser.getUserID(),
                parser.getBotID());
        }
        else
        {
            throw new AIMLProcessorException("<set></set> must have content!");
        }
    }
}
