/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.loadtime;

import org.w3c.dom.Element;

import org.aitools.programd.Core;
import org.aitools.programd.parser.StartupFileParser;

/**
 * <p>
 * Sets bot predicate values at load-time.
 * </p>
 * 
 * @version 4.2
 * @since 4.1.2
 * @author Thomas Ringate, Pedro Colla
 * @author Noel Bush
 */
public class PropertyProcessor extends StartupElementProcessor
{
    public static final String label = "property";

    public PropertyProcessor(Core coreToUse)
    {
        super(coreToUse);
    }
    
    public void process(Element element, StartupFileParser parser)
    {
        parser.getCurrentBot().setPropertyValue(element.getAttribute(NAME),
                element.getAttribute(VALUE));
    }
}