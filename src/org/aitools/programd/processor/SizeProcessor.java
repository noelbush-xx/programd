/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor;

import org.aitools.programd.graph.Graphmaster;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.parser.XMLNode;

/**
 * Handles a
 * <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-size">size</a></code>
 * element.
 * 
 * @version 4.1.3
 * @author Noel Bush
 */
public class SizeProcessor extends AIMLProcessor
{
    public static final String label = "size";

    public String process(int level, XMLNode tag, TemplateParser parser) throws AIMLProcessorException
    {
        if (tag.XMLType == XMLNode.EMPTY)
        {
            return String.valueOf(Graphmaster.getTotalCategories());
        }
        // (otherwise...)
        throw new AIMLProcessorException("<size/> cannot have content!");
    }
}