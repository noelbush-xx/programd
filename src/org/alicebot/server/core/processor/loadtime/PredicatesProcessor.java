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

import java.io.File;

import org.alicebot.server.core.Graphmaster;
import org.alicebot.server.core.parser.StartupFileParser;
import org.alicebot.server.core.parser.XMLNode;
import org.alicebot.server.core.processor.ProcessorException;
import org.alicebot.server.core.util.Toolkit;
import org.alicebot.server.core.util.Trace;
import org.alicebot.server.core.util.UserError;


/**
 *  The <code>predicates</code> element is a container
 *  for specifying characteristics of some
 *  <a href="http://alicebot.org/TR/2001/WD-aiml/#section-aiml-predicates">predicates</a>.
 */
public class PredicatesProcessor extends StartupElementProcessor
{
    public static final String label = "predicates";


    public String process(int level, XMLNode tag, StartupFileParser parser) throws InvalidStartupElementException
    {
        // Does it have an href attribute?
        String href = getHref(tag);

        if (href.length() > 0)
        {
            try
            {
                return parser.
                		processResponse(
                			Toolkit.getFileContents(
                				Graphmaster.getWorkingDirectory() + File.separator + href));
            }
            catch (ProcessorException e)
            {
                throw new UserError(e);
            }
        }
        else
        {
            return parser.evaluate(level++, tag.XMLChild);
        }
    }
}

