/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.aiml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.aitools.programd.Core;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.processor.Processor;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.util.Substituter;

/**
 * <p>
 * Handles a substitution element.
 * </p>
 * 
 * @version 4.5
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
abstract public class SubstitutionProcessor extends AIMLProcessor
{
    /**
     * Creates a new SubstitutionProcessor using the given Core.
     * 
     * @param coreToUse the Core object to use
     */
    public SubstitutionProcessor(Core coreToUse)
    {
        super(coreToUse);
    }

    /**
     * @param processor the processor that is requesting the substitution
     * @param element the element to process
     * @param parser the parser in use
     * @return the result of processing the substitution element
     * @throws ProcessorException 
     * @see AIMLProcessor#process(Element, TemplateParser)
     */
    protected String process(Class<? extends Processor> processor, Element element, TemplateParser parser) throws ProcessorException
    {
        if (element.getChildNodes().getLength() > 0)
        {
            // Return the processed contents of the element, properly
            // substituted.
            return applySubstitutions(processor, parser.evaluate(element.getChildNodes()), parser.getBotID());
        }
        return parser.shortcutTag(element, element.getTagName(), StarProcessor.label, Node.ELEMENT_NODE);
    }

    /**
     * Applies each substitution in the given map to the input.
     *
     * @param processor the processor whose substitutions should be applied
     * @param string the string on which to perform the replacement
     * @param botid the bot whose substitutions should be applied
     * @return the input with substitutions applied
     */
    public String applySubstitutions(Class<? extends Processor> processor, String string, String botid)
    {
        return Substituter.applySubstitutions(this.core.getBots().getBot(botid).getSubstitutionMap(processor), string);
    }
}