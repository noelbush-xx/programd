/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.loadtime;

import java.util.logging.Logger;

import org.w3c.dom.Element;

import org.aitools.programd.Core;
import org.aitools.programd.parser.GenericParser;
import org.aitools.programd.parser.StartupFileParser;
import org.aitools.programd.processor.Processor;
import org.aitools.programd.processor.ProcessorException;

/**
 * A <code>StartupElementProcessor</code> is responsible for processing an
 * element in a Program D startup file.
 * 
 * @since 4.2
 * @author Noel Bush
 */
abstract public class StartupElementProcessor extends Processor
{
    /** The string &quot;href&quot;. */
    protected static final String HREF = "href";

    protected static final Logger logger = Logger.getLogger("programd.startup");

    public StartupElementProcessor(Core coreToUse)
    {
        super(coreToUse);
    }
    
    public String process(Element element, GenericParser parser) throws ProcessorException
    {
        try
        {
            process(element, (StartupFileParser) parser);
            return EMPTY_STRING;
        } 
        catch (ClassCastException e)
        {
            throw new ProcessorException("Tried to pass a non-StartupFileParser to a StartupElementProcessor.");
        } 
    } 

	/**
	 * Generic implementation of process -- just processes children.
	 * 
	 * @param element	the element to process
	 * @param parser	the parser that is doing the processing
	 */
    public void process(Element element, StartupFileParser parser)
    {
        parser.evaluate(element.getChildNodes());
    }
}