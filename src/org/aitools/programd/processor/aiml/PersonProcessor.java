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
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.util.Substituter;
import org.aitools.programd.util.UserError;

/**
 * <p>
 * Handles a
 * <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-person">person</a></code>
 * element.
 * </p>
 * 
 * @version 4.5
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class PersonProcessor extends AIMLProcessor
{
    /** The label (as required by the registration scheme). */
    public static final String label = "person";

    /**
     * Creates a new PersonProcessor using the given Core.
     * @param coreToUse the Core object to use
     */
    public PersonProcessor(Core coreToUse)
    {
        super(coreToUse);
    }
    
    /**
     * @see AIMLProcessor#process(Element, TemplateParser)
     */
    public String process(Element element, TemplateParser parser) throws ProcessorException
    {
        if (element.getChildNodes().getLength() > 0)
        {
            try
            {
                // Return the processed contents of the element, properly
                // substituted.
                return applySubstitutions(parser.evaluate(element.getChildNodes()), parser
                        .getBotID());
            } 
            catch (ProcessorException e)
            {
                //return EMPTY_STRING;
                throw new UserError(e.getExplanatoryMessage(), e);
            } 
        } 
        // (otherwise...)
        return parser.shortcutTag(element, label, StarProcessor.label, Node.ELEMENT_NODE);
    } 

    /**
     * Applies substitutions as defined in the substitution map. Comparisons are
     * case-insensitive.
     * 
     * @param input
     *            the input on which to perform substitutions
     * @param botid the botid whose substitutions should be applied
     * @return the input with substitutions performed
     */
    public String applySubstitutions(String input, String botid)
    {
        return Substituter.applySubstitutions(this.core.getBots().getBot(botid).getPersonSubstitutionsMap(), input);
    } 
}