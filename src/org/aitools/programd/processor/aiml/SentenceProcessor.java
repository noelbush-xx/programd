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
 * Handles a
 * <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-sentence">sentence</a></code>
 * element.
 * 
 * @version 4.5
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class SentenceProcessor extends AIMLProcessor
{
    /** The label (as required by the registration scheme). */
    public static final String label = "sentence";

    /**
     * Creates a new SentenceProcessor using the given Core.
     * 
     * @param coreToUse the Core object to use
     */
    public SentenceProcessor(Core coreToUse)
    {
        super(coreToUse);
    }

    /**
     * @see AIMLProcessor#process(Element, TemplateParser)
     */
    public String process(Element element, TemplateParser parser) throws ProcessorException
    {
        String response = parser.evaluate(element.getChildNodes());
        if (response.equals(EMPTY_STRING))
        {
            return response;
        }
        if (response.trim().length() > 1)
        {
            return response.substring(0, 1).toUpperCase() + response.substring(1);
        }
        // (otherwise...)
        return response;
    }
}