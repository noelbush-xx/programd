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

package org.aitools.programd.processor.loadtime;

import org.aitools.programd.parser.StartupFileParser;
import org.aitools.programd.parser.XMLNode;
import org.aitools.programd.util.XMLKit;

/**
 *  The <code>predicate</code> element specifies
 *  for specifying characteristics of a
 *  <a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-aiml-predicates">predicate</a>.
 */
public class PredicateProcessor extends StartupElementProcessor
{
    public static final String label = "predicate";

    private static final String DEFAULT = "default";

    private static final String SET_RETURN = "set-return";

    public String process(int level, XMLNode tag, StartupFileParser parser)
        throws InvalidStartupElementException
    {
        if (tag.XMLType == XMLNode.EMPTY)
        {
            String name = XMLKit.getAttributeValue(NAME, tag.XMLAttr);
            if (name.equals(EMPTY_STRING))
            {
                throw new InvalidStartupElementException("<predicate/> must specify a name!");
            }
            String defaultValue =
                XMLKit.getAttributeValue(DEFAULT, tag.XMLAttr);
            if (defaultValue.equals(EMPTY_STRING))
            {
                defaultValue = null;
            }
            String setReturn =
                XMLKit.getAttributeValue(SET_RETURN, tag.XMLAttr);
            boolean returnNameWhenSet;
            if (setReturn.equals(NAME))
            {
                returnNameWhenSet = true;
            }
            else if (setReturn.equals(VALUE))
            {
                returnNameWhenSet = false;
            }
            else
            {
                throw new InvalidStartupElementException("Invalid value for set-return attribute on <predicate/>.");
            }
            parser.getCurrentBot().addPredicateInfo(
                name,
                defaultValue,
                returnNameWhenSet);
            return EMPTY_STRING;
        }
        // (otherwise...)
        throw new InvalidStartupElementException("<predicate/> cannot have element content!");
    }
}
