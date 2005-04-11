/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.responder.xml;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Element;

import org.aitools.programd.parser.GenericParser;
import org.aitools.programd.responder.AbstractXMLResponder;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.NotARegisteredClassException;

/**
 * <code>XMLTemplateParser</code> processes an HTML template, such as used by
 * {@link org.aitools.programd.responder.HTMLResponder HTMLResponder}.
 */
public class XMLTemplateParser extends GenericParser
{
    /** The responder that invoked this parser. */
    private AbstractXMLResponder responder;

    private static final Logger logger = Logger.getLogger("programd");

    /**
     * Initializes an <code>BotsConfigurationFileParser</code>.
     * 
     * @param registry the registry of XML template processors
     * @param responderSource the responder that is invoking this parser
     */
    public XMLTemplateParser(XMLTemplateProcessorRegistry registry, AbstractXMLResponder responderSource)
    {
        super(registry);
        this.responder = responderSource;
    }

    /**
     * Initializes an <code>BotsConfigurationFileParser</code> <i>without</i>
     * a responder source. This means that some tags will not be handled. It
     * only makes sense to create a parser this way if the goal is just to parse
     * a template, not process it.
     * 
     * @param registry the registry of XML template processors
     */
    public XMLTemplateParser(XMLTemplateProcessorRegistry registry)
    {
        super(registry);
    }

    /**
     * @return the responder that invoked this parser
     */
    public AbstractXMLResponder getResponder()
    {
        if (this.responder != null)
        {
            return this.responder;
        }
        throw new DeveloperError("Trying to get the responder from an XMLTemplateParser that was not initialized with one!",
                new NullPointerException());
    }

    /**
     * Ignores the unknown element and prints a warning message.
     * 
     * @param element the unknown element
     * @param e will be ignored
     * @see GenericParser#handleUnknownElement
     */
    protected void handleUnknownElement(Element element, NotARegisteredClassException e)
    {
        logger.log(Level.WARNING, "Ignoring unknown element \"" + element.getTagName() + "\".");
    }
}