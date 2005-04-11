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
 * <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-set">set</a></code>
 * element.
 * </p>
 * <p>
 * This is currently <i>not </i> AIML 1.0.1-compliant, because it fails to
 * account for &quot; <a
 * href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-aiml-predicate-behaviors">return-name-when-set
 * </a>&quot; predicates.
 * </p>
 */
public class SetProcessor extends AIMLProcessor
{
    /** The label (as required by the registration scheme). */
    public static final String label = "set";

    /**
     * Creates a new SetProcessor using the given Core.
     * 
     * @param coreToUse the Core object to use
     */
    public SetProcessor(Core coreToUse)
    {
        super(coreToUse);
    }

    /**
     * @see AIMLProcessor#process(Element, TemplateParser)
     */
    public String process(Element element, TemplateParser parser) throws ProcessorException
    {
        // Return the result of setting this predicate value (should check
        // its type, but not yet implemented).
        return parser.getCore().getPredicateMaster().set(element.getAttribute(NAME), parser.evaluate(element.getChildNodes()), parser.getUserID(),
                parser.getBotID());
    }
}