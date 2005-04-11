/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.botconfiguration;

import java.util.logging.Logger;

import org.w3c.dom.Element;

import org.aitools.programd.Core;
import org.aitools.programd.parser.GenericParser;
import org.aitools.programd.parser.BotsConfigurationFileParser;
import org.aitools.programd.processor.Processor;
import org.aitools.programd.processor.ProcessorException;

/**
 * A <code>BotConfigurationElementProcessor</code> is responsible for
 * processing an element in a Program D startup file.
 * 
 * @since 4.2
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
abstract public class BotConfigurationElementProcessor extends Processor
{
    /** The string &quot;{@value}&quot;. */
    protected static final String HREF = "href";

    protected static final Logger logger = Logger.getLogger("programd");

    /**
     * Creates a new BotConfigurationElementProcessor with the given Core.
     * 
     * @param coreToUse the Core to use
     */
    public BotConfigurationElementProcessor(Core coreToUse)
    {
        super(coreToUse);
    }

    /**
     * @see org.aitools.programd.processor.Processor#process(Element,
     *      GenericParser)
     */
    public String process(Element element, GenericParser parser) throws ProcessorException
    {
        try
        {
            process(element, (BotsConfigurationFileParser) parser);
            return EMPTY_STRING;
        }
        catch (ClassCastException e)
        {
            throw new ProcessorException("Tried to pass a non-BotsConfigurationFileParser to a BotConfigurationElementProcessor.", e);
        }
    }

    /**
     * Generic implementation of process -- just processes children.
     * 
     * @param element the element to process
     * @param parser the parser that is doing the processing
     * @throws ProcessorException if there is an error in processing
     */
    public void process(Element element, BotsConfigurationFileParser parser) throws ProcessorException
    {
        parser.evaluate(element.getChildNodes());
    }
}