/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.responder.xml;

import org.aitools.programd.Core;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.responder.Responder;
import org.aitools.programd.responder.ServletRequestResponder;
import org.aitools.programd.responder.xml.XMLTemplateProcessorException;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class LoggedInProcessor extends XMLTemplateProcessor
{
    /** The string &quot;{@value}&quot;. */
    private static final String LOGGED_IN = "logged-in";

    /**
     * Creates a new LoggedInProcessor with the given Core.
     * 
     * @param coreToUse the Core to use in creating the LoggedInProcessor
     */
    public LoggedInProcessor(Core coreToUse)
    {
        super(coreToUse);
    }

    /** The label (as required by the registration scheme). */
    public static final String label = "logged-in";

    /**
     * @param element the <code>logged-in</code> element
     * @param parser the parser that is at work (unused)
     * @return the contents of this element, if the session is logged in; otherwise, empty string
     * @throws XMLTemplateProcessorException if the responder associated with the parser does not implement ServletRequestResponder
     */
    public String process(Element element, XMLTemplateParser parser) throws XMLTemplateProcessorException
    {
        Responder associatedResponder = parser.getResponder();
        if (!(associatedResponder instanceof ServletRequestResponder))
        {
            throw new XMLTemplateProcessorException("Tried to use a <logged-in> element on a responder that is not servlet-aware!", new ClassCastException());
        }
        ServletRequestResponder responder = (ServletRequestResponder)associatedResponder;
        if (responder.getEnvelope().sessionAttributeEquals(LOGGED_IN, true))
        {
            try
            {
                return parser.evaluate(element.getChildNodes());
            }
            catch (ProcessorException e)
            {
                throw new XMLTemplateProcessorException("Error processing content of <logged-in> element.", e);
            }
        }
        // otherwise...
        return EMPTY_STRING;
    }
}
