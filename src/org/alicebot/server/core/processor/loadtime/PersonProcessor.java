/*
    Alicebot Program D
    Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

package org.alicebot.server.core.processor.loadtime;

import org.alicebot.server.core.parser.StartupFileParser;
import org.alicebot.server.core.parser.XMLNode;


/**
 *  The <code>person</code> element is a container
 *  for definitions of 
 *  <a href="http://www.alicebot.org/TR/2001/WD-aiml/#section-person">person</a> substitutions
 *  to be performed on a string.
 */
public class PersonProcessor extends StartupElementProcessor
{
    public static final String label = "person";


    public String process(int level, XMLNode tag, StartupFileParser parser) throws InvalidStartupElementException
    {
        SubstitutionsProcessor.addSubstitutions(SubstitutionsProcessor.PERSON, tag, parser);
        return EMPTY_STRING;
    }
}

