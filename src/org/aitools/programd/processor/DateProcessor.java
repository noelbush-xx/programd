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

import java.util.Date;

import org.aitools.programd.Core;
import org.aitools.programd.parser.TemplateParser;

/**
 * Handles a
 * <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-date">date</a></code>
 * element.
 * 
 * @version 4.2
 * @author Pedro Colla
 * @author Noel Bush
 */
public class DateProcessor extends AIMLProcessor
{
    public static final String label = "date";

    public DateProcessor(Core coreToUse)
    {
        super(coreToUse);
    }
    
    public String process(Element element, TemplateParser parser)
    {
        return new Date().toString();
    } 
}