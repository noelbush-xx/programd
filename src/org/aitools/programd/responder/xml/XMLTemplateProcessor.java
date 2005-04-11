/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.responder.xml;

import org.w3c.dom.Element;

import org.aitools.programd.Core;
import org.aitools.programd.parser.GenericParser;
import org.aitools.programd.processor.Processor;
import org.aitools.programd.processor.ProcessorException;

/**
 * An <code>XMLTemplateProcessor</code> is responsible for processing a
 * particular element in an HTML template.
 * 
 * @since 4.5
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
abstract public class XMLTemplateProcessor extends Processor
{
    /**
     * Creates a new XMLTemplateProcessor using the given Core.
     * 
     * @param coreToUse the Core object to use with the new XMLTemplateProcessor
     */
    public XMLTemplateProcessor(Core coreToUse)
    {
        super(coreToUse);
    }

    /**
     * @see org.aitools.programd.processor.Processor#process(org.w3c.dom.Element,
     *      org.aitools.programd.parser.GenericParser)
     */
    public String process(Element element, GenericParser parser) throws ProcessorException
    {
        try
        {
            return process(element, (XMLTemplateParser) parser);
        }
        catch (ClassCastException e)
        {
            throw new ProcessorException("Tried to pass a non-XMLTemplateParser to an XMLTemplateProcessor.", e);
        }
    }

    /**
     * Processes the given element, using the given parser if needed.
     * 
     * @param element the element to process
     * @param parser the parser that has ordered the processing
     * @return the result of processing the element
     * @throws XMLTemplateProcessorException if there is an unrecoverable
     *             problem processing the element
     */
    abstract public String process(Element element, XMLTemplateParser parser) throws XMLTemplateProcessorException;
}