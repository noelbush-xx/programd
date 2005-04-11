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

import java.util.StringTokenizer;

import org.aitools.programd.Core;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.processor.ProcessorException;

/**
 * Handles a
 * <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-formal">formal</a></code>
 * element.
 * 
 * @version 4.5
 * @author Jon Baer
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class FormalProcessor extends AIMLProcessor
{
    /** The label (as required by the registration scheme). */
    public static final String label = "formal";

    // Convenience constants.
    private static final String SPACE = " ";

    /**
     * Creates a new FormalProcessor using the given Core.
     * 
     * @param coreToUse the Core object to use
     */
    public FormalProcessor(Core coreToUse)
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
        StringTokenizer tokenizer = new StringTokenizer(response, SPACE);
        StringBuffer result = new StringBuffer(response.length());
        while (tokenizer.hasMoreTokens())
        {
            String word = tokenizer.nextToken();
            if (result.length() > 0)
            {
                result.append(SPACE);
            }
            result.append(word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase());
        }
        return result.toString();
    }
}