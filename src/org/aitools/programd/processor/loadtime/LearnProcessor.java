/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.loadtime;

import org.aitools.programd.graph.Graphmaster;
import org.aitools.programd.parser.StartupFileParser;
import org.aitools.programd.parser.XMLNode;

/**
 * Loads AIML at load-time.
 * 
 * @since 4.1.2
 * @author Noel Bush
 */
public class LearnProcessor extends StartupElementProcessor
{
    public static final String label = "learn";

    public String process(int level, XMLNode tag, StartupFileParser parser) throws InvalidStartupElementException
    {
        if (tag.XMLType == XMLNode.TAG)
        {
            Graphmaster.load(parser.evaluate(level++, tag.XMLChild), parser.getCurrentBot().getID());
            return EMPTY_STRING;
        } 
        // (otherwise...)
        throw new InvalidStartupElementException("<learn></learn> must have content!");
    } 
}