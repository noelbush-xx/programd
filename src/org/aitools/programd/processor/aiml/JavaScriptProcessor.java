/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.aiml;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Element;

import org.aitools.programd.Core;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.processor.ProcessorException;

/**
 * Handles a
 * <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-javascript">javascript</a></code>
 * element.
 * 
 * @version 4.5
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 */
public class JavaScriptProcessor extends AIMLProcessor
{
    /** The label (as required by the registration scheme). */
    public static final String label = "javascript";

    private static final Logger logger = Logger.getLogger("programd");

    /**
     * Creates a new JavaScriptProcessor using the given Core.
     * 
     * @param coreToUse the Core object to use
     */
    public JavaScriptProcessor(Core coreToUse)
    {
        super(coreToUse);
    }

    /**
     * Returns the result of processing the contents of the
     * <code>javascript</code> element by the JavaScript interpreter.
     * 
     * @param element the <code>javascript</code> element
     * @param parser the parser that is at work
     * @return the result of processing the element
     * @throws ProcessorException if there is an error in processing
     */
    public String process(Element element, TemplateParser parser) throws ProcessorException
    {
        // Don't use the system tag if not permitted.
        if (!parser.getCore().getSettings().javascriptAllowed())
        {
            logger.log(Level.WARNING, "Use of <javascript> prohibited!");
            return EMPTY_STRING;
        }
        logger.log(Level.FINE, "Calling JavaScript interpreter.");
        return parser.getCore().getInterpreter().evaluate(parser.evaluate(element.getChildNodes()));
    }
}