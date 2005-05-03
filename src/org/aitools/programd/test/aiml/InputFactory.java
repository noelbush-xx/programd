/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.test.aiml;

import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.XMLKit;
import org.w3c.dom.Element;

/**
 * Creates Input objects.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class InputFactory
{
    /**
     * Creates a new Input of the correct type based on the
     * contents of the given XML element.
     * 
     * @param element the element from which to create the Input
     * @return the created Input
     */
    public static Input create(Element element)
    {
        String tagName = element.getTagName();
        
        // If this is just a single Input element, create an ItemBase element.
        if (tagName.equals("Input"))
        {
            return new InputBase(element.getTextContent());
        }
        else if (tagName.equals("Or"))
        {
            return new InputOr(XMLKit.getElementChildrenOf(element));
        }
        else if (tagName.equals("And"))
        {
            return new InputAnd(XMLKit.getElementChildrenOf(element));
        }
        else
        {
            throw new DeveloperError("Some invalid element slipped past the schema!", new IllegalArgumentException());
        }
    }
}
