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

/**
 * The <code>person</code> element is a container for definitions of <a
 * href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-person2">person2 </a>
 * substitutions to be performed on a string.
 */
public class Person2Processor extends StartupElementProcessor
{
    public static final String label = "person2";

    public String process(int level, XMLNode tag, StartupFileParser parser) throws InvalidStartupElementException
    {
        SubstitutionsProcessor.addSubstitutions(SubstitutionsProcessor.PERSON2, tag, parser);
        return EMPTY_STRING;
    } 
}