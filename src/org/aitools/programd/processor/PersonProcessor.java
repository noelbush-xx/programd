/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.aitools.programd.Core;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.util.Substituter;

/**
 * <p>
 * Handles a
 * <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-person">person</a></code>
 * element.
 * </p>
 * 
 * @version 4.2
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 * @author Noel Bush
 */
public class PersonProcessor extends AIMLProcessor
{
    public static final String label = "person";

    public PersonProcessor(Core coreToUse)
    {
        super(coreToUse);
    }
    
    public String process(Element element, TemplateParser parser)
    {
        if (element.getChildNodes().getLength() > 0)
        {
            try
            {
                // Return the processed contents of the element, properly
                // substituted.
                return parser.processResponse(applySubstitutions(parser.evaluate(element.getChildNodes()), parser
                        .getBotID()));
            } 
            catch (ProcessorException e)
            {
                return EMPTY_STRING;
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
     * @return the input with substitutions performed
     */
    public String applySubstitutions(String input, String botid)
    {
        return Substituter.applySubstitutions(this.core.getBots().getBot(botid).getPersonSubstitutionsMap(), input);
    } 
}