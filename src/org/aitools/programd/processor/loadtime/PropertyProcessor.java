/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.loadtime;

import org.aitools.programd.parser.StartupFileParser;
import org.aitools.programd.parser.XMLNode;
import org.aitools.programd.util.XMLKit;

/**
 * <p>
 * Sets bot predicate values at load-time.
 * </p>
 * <p>
 * &lt;property/&gt; is not an AIML element, and may be removed in future
 * versions of Program D.
 * </p>
 * 
 * @since 4.1.2
 * @author Thomas Ringate, Pedro Colla
 */
public class PropertyProcessor extends StartupElementProcessor
{
    public static final String label = "property";

    public String process(int level, XMLNode tag, StartupFileParser parser) throws InvalidStartupElementException
    {
        if (tag.XMLType == XMLNode.EMPTY)
        {
            String name = XMLKit.getAttributeValue(NAME, tag.XMLAttr);
            if (!name.equals(EMPTY_STRING))
            {
                String value = XMLKit.getAttributeValue(VALUE, tag.XMLAttr);
                parser.getCurrentBot().setPropertyValue(name, value);
            } 
        } 
        else
        {
            throw new InvalidStartupElementException("<property/> cannot have contents!");
        } 
        return EMPTY_STRING;
    } 
}