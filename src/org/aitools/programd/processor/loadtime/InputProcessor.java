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
 * The <code>input</code> element is a container for definitions of <a
 * href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-substitution-normalizations">substitution
 * normalizations </a> to be performed on an input.
 */
public class InputProcessor extends StartupElementProcessor
{
    public static final String label = "input";

    public String process(int level, XMLNode tag, StartupFileParser parser) throws InvalidStartupElementException
    {
        SubstitutionsProcessor.addSubstitutions(SubstitutionsProcessor.INPUT, tag, parser);
        return EMPTY_STRING;
    }
}