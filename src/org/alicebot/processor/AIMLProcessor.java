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

package org.alicebot.processor;

import org.alicebot.parser.GenericParser;
import org.alicebot.parser.TemplateParser;
import org.alicebot.parser.XMLNode;

/**
 *  An <code>AIMLProcessor</code> is responsible for processing a
 *  particular AIML element.
 *
 *  @version    4.1.3
 *  @author     Jon Baer
 *  @author     Thomas Ringate, Pedro Colla
 *  @author     Noel Bush
 */
abstract public class AIMLProcessor extends Processor
{
    public String process(int level, XMLNode tag, GenericParser parser)
        throws ProcessorException
    {
        try
        {
            return process(level, tag, (TemplateParser) parser);
        }
        catch (ClassCastException e)
        {
            throw new ProcessorException("Tried to pass a non-TemplateParser to an AIMLProcessor.");
        }
    }

    abstract public String process(
        int level,
        XMLNode tag,
        TemplateParser parser)
        throws AIMLProcessorException;
}
