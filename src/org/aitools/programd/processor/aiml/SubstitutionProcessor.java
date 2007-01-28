/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.aiml;

import org.jdom.Element;

import org.aitools.programd.Core;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.processor.Processor;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.util.Substituter;

/**
 * Handles a substitution element.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
abstract public class SubstitutionProcessor extends AIMLProcessor
{
    /**
     * Creates a new SubstitutionProcessor using the given Core.
     * 
     * @param core the Core object to use
     */
    public SubstitutionProcessor(Core core)
    {
        super(core);
    }

    /**
     * @param processor the processor that is requesting the substitution
     * @param element the element to process
     * @param parser the parser in use
     * @return the result of processing the substitution element
     * @throws ProcessorException
     * @see AIMLProcessor#process(Element, TemplateParser)
     */
    @SuppressWarnings("unchecked")
    protected String process(Class<? extends Processor> processor, Element element, TemplateParser parser)
            throws ProcessorException
    {
        if (element.getContent().size() > 0)
        {
            // Return the processed contents of the element, properly substituted.
            return applySubstitutions(processor, parser.evaluate(element.getContent()), parser.getBotID());
        }
        return parser.shortcutTag(element, element.getName(), StarProcessor.label, Element.class);
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
        return Substituter.applySubstitutions(this._core.getBot(botid).getSubstitutionMap(processor), string);
    }
}
