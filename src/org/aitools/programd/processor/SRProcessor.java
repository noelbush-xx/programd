/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.aitools.programd.Core;
import org.aitools.programd.parser.TemplateParser;

/**
 * Handles an
 * <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-sr">sr</a></code>
 * element.
 * 
 * @version 4.2
 * @author Noel Bush
 */
public class SRProcessor extends AIMLProcessor
{
    /** The label (as required by the registration scheme). */
    public static final String label = "sr";

    /**
     * Creates a new SRProcessor using the given Core.
     * @param coreToUse the Core object to use
     */
    public SRProcessor(Core coreToUse)
    {
        super(coreToUse);
    }
    
    /**
     * @see org.aitools.programd.processor.AIMLProcessor#process(org.w3c.dom.Element, org.aitools.programd.parser.TemplateParser)
     */
    public String process(Element element, TemplateParser parser)
    {
        return parser.shortcutTag(element, SRAIProcessor.label, StarProcessor.label, Node.ELEMENT_NODE);
    } 
}