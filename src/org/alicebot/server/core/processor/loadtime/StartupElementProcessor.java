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

import org.alicebot.server.core.parser.GenericParser;
import org.alicebot.server.core.parser.StartupFileParser;
import org.alicebot.server.core.parser.XMLNode;
import org.alicebot.server.core.processor.Processor;
import org.alicebot.server.core.processor.ProcessorException;


/**
 *  A <code>StartupElementProcessor</code> is responsible for processing an
 *  element in a Program D startup file.
 *
 *  @since  4.1.3
 *  @author Noel Bush
 */
abstract public class StartupElementProcessor extends Processor
{
    public String process(int level, String id, XMLNode tag, GenericParser parser) throws ProcessorException
    {
        try
        {
            return process(level, id, tag, (StartupFileParser)parser);
        }
        catch (ClassCastException e)
        {
            throw new ProcessorException("Tried to pass a non-StartupFileParser to a StartupElementProcessor.");
        }
    }

    abstract public String process(int level, String botid, XMLNode tag, StartupFileParser parser) throws InvalidStartupElementException;
}
