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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.aitools.programd.Core;
import org.aitools.programd.parser.GenericParser;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.responder.AbstractXMLResponder;
import org.aitools.programd.responder.xml.XMLTemplateProcessor;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.NotARegisteredClassException;
import org.aitools.programd.util.StringKit;
import org.aitools.programd.util.UserError;
import org.aitools.programd.util.XMLKit;

/**
 * <code>XMLTemplateParser</code> processes an HTML template, such as used by
 * {@link org.aitools.programd.responder.HTMLResponder HTMLResponder}.
 */
public class XMLTemplateParser extends GenericParser<XMLTemplateProcessor>
{
    /** The responder that invoked this parser. */
    private AbstractXMLResponder responder;
    
    /** Whether to convert HTML line breakers to line breaks in the output. */
    private boolean convertHTMLLineBreakers;
    
    /** Whether to strip markup from the output. */
    private boolean stripMarkup;

    private static final Logger logger = Logger.getLogger("programd");

    /**
     * Initializes an <code>BotsConfigurationFileParser</code>.
     * 
     * @param registry the registry of XML template processors
     * @param responderSource the responder that is invoking this parser
     * @param convert whether to convert HTML line breakers to line breaks in output
     * @param strip whether to strip markup from the output
     */
    public XMLTemplateParser(XMLTemplateProcessorRegistry registry, AbstractXMLResponder responderSource, boolean convert, boolean strip)
    {
        super(registry, responderSource.getManager().getCore());
        this.responder = responderSource;
        this.convertHTMLLineBreakers = convert;
        this.stripMarkup = strip;
    }

    /**
     * Initializes an <code>BotsConfigurationFileParser</code> <i>without</i>
     * a responder source. This means that some tags will not be handled. It
     * only makes sense to create a parser this way if the goal is just to parse
     * a template, not process it.
     * 
     * @param registry the registry of XML template processors
     * @param coreToUse the core to use
     * @param convert whether to convert HTML line breakers to line breaks in output
     * @param strip whether to strip markup from the output
     */
    public XMLTemplateParser(XMLTemplateProcessorRegistry registry, Core coreToUse, boolean convert, boolean strip)
    {
        super(registry, coreToUse);
        this.convertHTMLLineBreakers = convert;
        this.stripMarkup = strip;
    }

    /**
     * Processes a response, stripping markup and/or converting
     * HTML line breakers as configured.
     * 
     * @see org.aitools.programd.parser.GenericParser#evaluate(Document)
     */
    public String evaluate(Document document) throws ProcessorException
    {
        String response = super.evaluate(document);
        if (this.convertHTMLLineBreakers)
        {
            response = StringKit.renderAsLines(XMLKit.filterViaHTMLTags(response));
        }
        if (this.stripMarkup)
        {
            response = XMLKit.removeMarkup(response);
        }
        return response;
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
     * Ignores the unknown element, processes children, and prints a warning message.
     * 
     * @param element the unknown element
     * @param e will be ignored
     * @return the result of processing the children
     * @see GenericParser#handleUnknownElement
     */
    protected String handleUnknownElement(Element element, NotARegisteredClassException e)
    {
        logger.log(Level.WARNING, "Ignoring unknown element \"" + element.getTagName() + "\".");
        try
        {
            return evaluate(element.getChildNodes());
        }
        catch (ProcessorException ee)
        {
            throw new UserError("Could not process contents of unknown element \"" + element.getTagName() + "\".", ee);
        }
    }
}