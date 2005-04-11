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
 * <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-learn">learn</a></code>
 * element.
 * 
 * @version 4.5
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class LearnProcessor extends AIMLProcessor
{
    /** The label (as required by the registration scheme). */
    public static final String label = "learn";

    /**
     * Creates a new LearnProcessor using the given Core.
     * 
     * @param coreToUse the Core object to use
     */
    public LearnProcessor(Core coreToUse)
    {
        super(coreToUse);
    }

    /**
     * Attempts to load an AIML file whose location is described by the results
     * of processing the content of the element.
     * 
     * @param element the <code>learn</code> element
     * @param parser the parser that is at work
     * @return the result of processing the element
     * @throws ProcessorException if there is an error in processing
     */
    public String process(Element element, TemplateParser parser) throws ProcessorException
    {
        parser.getCore().getGraphmaster().load(parser.evaluate(element.getChildNodes()), parser.getBotID());
        return EMPTY_STRING;
    }
}