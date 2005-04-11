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

/**
 * Processes an
 * <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-input">input</a></code>
 * element.
 * 
 * @version 4.5
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class InputProcessor extends IndexedPredicateProcessor
{
    /** The label (as required by the registration scheme). */
    public static final String label = "input";

    /**
     * Creates a new InputProcessor using the given Core.
     * 
     * @param coreToUse the Core object to use
     */
    public InputProcessor(Core coreToUse)
    {
        super(coreToUse);
    }

    /**
     * Generalizes the processing of an <code>input</code> element to a job
     * for {@link IndexedPredicateProcessor}.
     * 
     * @param element the <code>input</code> element
     * @param parser the parser that is at work
     * @return the result of processing the element
     */
    public String process(Element element, TemplateParser parser)
    {
        return super.process(element, parser, label, 2);
    }
}