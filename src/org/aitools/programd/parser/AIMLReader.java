/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import org.aitools.programd.util.XMLKit;

/**
 * <p>
 * This reads in standard AIML and delivers categories to the Graphmaster.
 * </p>
 * <p>
 * This has been rewritten starting with 4.2 to use SAX.
 * </p>
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @version 4.5
 */
public class AIMLReader extends DefaultHandler
{
    /** The <code>Listener</code> that will handle new items. */
    private AIMLReaderListener listener;

    private String defaultNamespaceURI;

    /*
     * Constants used in parsing.
     */

    /** The start of a tag marker. */
    private static final String MARKER_START = "<";

    /** The end of a tag marker. */
    private static final String MARKER_END = ">";

    /** An empty string. */
    private static final String EMPTY_STRING = "";

    private static final String OPEN_TEMPLATE_START_TAG = "<template xmlns=\"";

    private static final String QUOTE_MARKER_END = "\">";

    /** A slash. */
    private static final String SLASH = "/";

    /** End of a template element. */
    private static final String TEMPLATE_END_TAG = "</template>";

    /** The system line separator. */
    protected static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /** Parser states. */
    private enum State
    {
    /** inside the pattern element */
    IN_PATTERN,

    /** inside the that element */
    IN_THAT,

    /** inside the template element */
    IN_TEMPLATE,

    /** inside an element that is not specially handled */
    IN_UNHANDLED
    }

    /** The string &quot;{@value}&quot;. */
    private static final String PATTERN = "pattern";

    /** The string &quot;{@value}&quot;. */
    private static final String THAT = "that";

    /** The string &quot;{@value}&quot;. */
    //private static final String TOPIC = "topic";

    /** The string &quot;{@value}&quot;. */
    private static final String TEMPLATE = "template";

    /** The current state. */
    private State state = State.IN_UNHANDLED;

    /** The most recently collected &lt;pattern&gt;&lt;/pattern&gt; contents. */
    private StringBuffer patternBuffer;

    /** The most recently collected &lt;that&gt;&lt;/that&gt; contents. */
    private StringBuffer thatBuffer;

    /** The most recently collected &lt;topic&gt;&lt;/topic&gt; contents. */
    //private StringBuffer topicBuffer;

    /** The most recently collected &lt;template&gt;&lt;/template&gt; contents. */
    private StringBuffer templateBuffer;

    /** The finalized &lt;pattern&gt;&lt;/pattern&gt; contents. */
    private String pattern;

    /** The finalized &lt;that&gt;&lt;/that&gt; contents. */
    private String that;

    /** The finalized &lt;topic&gt;&lt;/topic&gt; contents. */
    private String topic;

    /** The finalized &lt;template&gt;&lt;/template&gt; contents. */
    private String template;

    /** The start of a template element. */
    private String templateStartTag;

    /**
     * Creates a new AIMLReader.
     * 
     * @param readerListener the listener that will (presumably) create new
     *            categories as they are found by the reader.
     * @param defaultNamespaceURIToUse the namespace URI to use when none other
     *            is specified (?)
     */
    public AIMLReader(AIMLReaderListener readerListener, String defaultNamespaceURIToUse)
    {
        this.listener = readerListener;
        this.defaultNamespaceURI = defaultNamespaceURIToUse;
        this.templateStartTag = OPEN_TEMPLATE_START_TAG + defaultNamespaceURIToUse + QUOTE_MARKER_END;
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
    {
        String s = XMLKit.escapeXMLChars(new String(ch, start, length));

        switch (this.state)
        {
            case IN_PATTERN:
                this.patternBuffer.append(s);
                break;

            case IN_THAT:
                this.thatBuffer.append(s);
                break;

            case IN_TEMPLATE:
                this.templateBuffer.append(s);
                break;
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName)
    {
        String elementName;
        if (localName.equals(EMPTY_STRING))
        {
            elementName = qName;
        }
        else
        {
            elementName = localName;
        }

        if (this.state == State.IN_PATTERN)
        {
            this.pattern = XMLKit.filterWhitespace(this.patternBuffer.toString());
            this.state = State.IN_UNHANDLED;
        }
        else if (this.state == State.IN_THAT)
        {
            this.that = XMLKit.filterWhitespace(this.thatBuffer.toString());
            this.state = State.IN_UNHANDLED;
        }
        else if (elementName.equals(TEMPLATE))
        {
            // Whitespace-normalize the template contents.
            this.template = XMLKit.filterWhitespace(this.templateStartTag + this.templateBuffer.toString() + TEMPLATE_END_TAG);
            // Finally, deliver the newly defined category to the listener.
            this.listener.newCategory(this.pattern, this.that, this.topic, this.template);
            // Reset the pattern, that and template.
            this.pattern = this.that = this.template = null;
            this.patternBuffer = this.thatBuffer = this.templateBuffer = null;
            this.state = State.IN_UNHANDLED;
        }
        else if (this.state == State.IN_TEMPLATE)
        {
            // See if we are ending an empty element.
            boolean makeClosingTag = true;

            // Get the template length (just once).
            int templateLength = this.templateBuffer.length();
            // If the last char of the template string is a '>',
            if (this.templateBuffer.substring(templateLength - 1).equals(MARKER_END))
            {
                // and if the next to last char is *not* a '/',
                if (!this.templateBuffer.substring(templateLength - 2, templateLength - 1).equals(SLASH))
                {
                    // then if the name of the last tag is the same as this one,
                    // this is an empty element.
                    int lastMarkerStart = this.templateBuffer.lastIndexOf(MARKER_START);
                    if (this.templateBuffer.indexOf(elementName, lastMarkerStart) == lastMarkerStart + 1)
                    {
                        // So just insert a '/' before the '>'.
                        this.templateBuffer.insert(templateLength - 1, '/');
                        makeClosingTag = false;
                    }
                }
            }
            // Otherwise, make a closing tag.
            if (makeClosingTag)
            {
                this.templateBuffer.append(MARKER_START);
                this.templateBuffer.append('/' + elementName);
                this.templateBuffer.append(MARKER_END);
            }
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName, Attributes attributes)
    {
        String elementName;
        if (localName.equals(EMPTY_STRING))
        {
            elementName = qName;
        }
        else
        {
            elementName = localName;
        }
        if (elementName.equals(PATTERN))
        {
            this.state = State.IN_PATTERN;
            this.patternBuffer = null;
            this.patternBuffer = new StringBuffer();
        }
        else if (elementName.equals(THAT) && this.state != State.IN_TEMPLATE)
        {
            this.state = State.IN_THAT;
            this.thatBuffer = null;
            this.thatBuffer = new StringBuffer();
        }
        else if (elementName.equals(TEMPLATE))
        {
            this.state = State.IN_TEMPLATE;
            this.templateBuffer = null;
            this.templateBuffer = new StringBuffer();
        }
        else if (this.state == State.IN_TEMPLATE)
        {
            // This seems stupid -- I wish there were a better way.
            // The point is that we don't want to parse the template into
            // some big memory structure, since it may never be used. So
            // really here we are just reconstituting the XML text for later
            // processing.
            this.templateBuffer.append(XMLKit.renderStartTag(elementName, attributes, !uri.equals(this.defaultNamespaceURI), uri));
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException e) throws SAXException
    {
        throw e;
    }
}