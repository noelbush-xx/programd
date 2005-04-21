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

import org.aitools.programd.Core;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.processor.ProcessorException;

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
public class PersonProcessor extends SubstitutionProcessor
{
    /** The label (as required by the registration scheme). */
    public static final String label = "person";

    /**
     * Creates a new PersonProcessor using the given Core.
     * 
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
        return process(PersonProcessor.class, element, parser);
    }

    /**
     * Applies substitutions as defined in the substitution map. Comparisons are
     * case-insensitive.
     * 
     * @param input the input on which to perform substitutions
     * @param botid the botid whose substitutions should be applied
     * @return the input with substitutions performed
     */
    public String applySubstitutions(String input, String botid)
    {
        return applySubstitutions(PersonProcessor.class, input, botid);
    }
}