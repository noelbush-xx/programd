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
 * The <code>person</code> element is a container for definitions of <a
 * href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-person">person </a>
 * substitutions to be performed on a string.
 */
public class PersonProcessor extends StartupElementProcessor
{
    public static final String label = "person";

    public PersonProcessor(Core coreToUse)
    {
        super(coreToUse);
    }
    
    public void process(Element element, StartupFileParser parser)
    {
        SubstitutionsProcessor.addSubstitutions(SubstitutionsProcessor.SubstitutionType.PERSON, element, parser);
    } 
}