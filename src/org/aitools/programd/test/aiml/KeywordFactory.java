/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.test.aiml;

import org.w3c.dom.Element;

import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.XMLKit;

/**
 * Creates Keyword objects.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.5
 */
public class KeywordFactory
{
    /**
     * Creates a new Keyword of the correct type based on the
     * contents of the given XML element.
     * 
     * @param element the element from which to create the Keyword
     * @return the created Input
     */
    public static Keyword create(Element element)
    {
        String tagName = element.getTagName();
        
        // If this is just a single Input element, create an ItemBase element.
        if (tagName.equals("Keyword"))
        {
            return new KeywordBase(element.getTextContent());
        }
        else if (tagName.equals("Or"))
        {
            return new KeywordOr(XMLKit.getElementChildrenOf(element));
        }
        else if (tagName.equals("And"))
        {
            return new KeywordAnd(XMLKit.getElementChildrenOf(element));
        }
        else
        {
            throw new DeveloperError("Some invalid element slipped past the schema!", new IllegalArgumentException());
        }
    }
}
