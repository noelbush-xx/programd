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

import org.alicebot.server.core.PredicateMaster;
import org.alicebot.server.core.parser.StartupFileParser;
import org.alicebot.server.core.parser.XMLNode;
import org.alicebot.server.core.util.Toolkit;


/**
 *  The <code>predicate</code> element specifies
 *  for specifying characteristics of a
 *  <a href="http://alicebot.org/TR/2001/WD-aiml/#section-aiml-predicates">predicate</a>.
 */
public class PredicateProcessor extends StartupElementProcessor
{
    public static final String label = "predicate";


    private static final String NAME = "name";

    private static final String DEFAULT = "default";

    private static final String SET_RETURN = "set-return";

    private static final String VALUE = "value";


    public String process(int level, XMLNode tag, StartupFileParser parser) throws InvalidStartupElementException
    {
        if (tag.XMLType == XMLNode.EMPTY)
        {
            String name = Toolkit.getAttributeValue(NAME, tag.XMLAttr);
            if (name.equals(EMPTY_STRING))
            {
                throw new InvalidStartupElementException ("<predicate/> must specify a name!");
            }
            String defaultValue = Toolkit.getAttributeValue(DEFAULT, tag.XMLAttr);
            if (defaultValue.equals(EMPTY_STRING))
            {
                defaultValue = null;
            }
            String setReturn = Toolkit.getAttributeValue(SET_RETURN, tag.XMLAttr);
            boolean returnNameWhenSet;
            if (setReturn.equals(NAME))
            {
                returnNameWhenSet = true;
            }
            else if (setReturn.equals(VALUE))
            {
                returnNameWhenSet = false;
            }
            else
            {
                throw new InvalidStartupElementException("Invalid value for set-return attribute on <predicate/>.");
            }
            parser.getCurrentBot().addPredicateInfo(name, defaultValue, returnNameWhenSet);
            return EMPTY_STRING;
        }
        else
        {
            throw new InvalidStartupElementException("<predicate/> cannot have element content!");
        }
    }
}

