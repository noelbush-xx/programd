/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.parser;

import java.net.URL;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;

import org.aitools.programd.Bot;
import org.aitools.programd.graph.Graphmapper;
import org.aitools.programd.processor.aiml.AIMLProcessorRegistry;
import org.aitools.util.xml.Characters;
import org.aitools.util.xml.SAX;

/**
 * This reads in standard AIML and delivers categories to the Graphmapper.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class AIMLReader extends DefaultHandler2
{
    private String _defaultNamespaceURI;

    private Graphmapper _graphmapper;

    private URL _path;

    private Bot _bot;

    /** The most recently collected &lt;pattern&gt;&lt;/pattern&gt; contents. */
    private StringBuilder patternBuffer;

    /** The most recently collected &lt;that&gt;&lt;/that&gt; contents. */
    private StringBuilder thatBuffer;

    /** The most recently collected &lt;template&gt;&lt;/template&gt; contents. */
    private StringBuilder templateBuffer;

    /** A pointer to the current buffer. */
    private StringBuilder currentBuffer;

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
     * @param graphmapper the <code>Graphmapper</code> into which new categories are to be loaded.
     * @param path the path that is being read
     * @param bot the bot itself
     */
    public AIMLReader(Graphmapper graphmapper, URL path, Bot bot)
    {
        this._graphmapper = graphmapper;
        this._path = path;
        this._bot = bot;
        this.templateStartTag = String.format("<template xmlns=\"%s\">", AIMLProcessorRegistry.XMLNS);
        this.topic = "*";
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length)
    {
        if (this.currentBuffer != null)
        {
            this.currentBuffer.append(Characters.escapeXMLChars(ch, start, length));
        }
    }

    /**
     * @see org.xml.sax.ext.DefaultHandler2#startCDATA()
     */
    @Override
    public void startCDATA()
    {
        assert this.currentBuffer != null : "Got CDATA start outside of a known element!";
        this.currentBuffer.append("<![CDATA[");
    }

    /**
     * @see org.xml.sax.ext.DefaultHandler2#endCDATA()
     */
    @Override
    public void endCDATA()
    {
        assert this.currentBuffer != null : "Got CDATA end outside of a known element!";
        this.currentBuffer.append("]]>");
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
     *      org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
    {
        String elementName;
        if ("".equals(localName))
        {
            elementName = qName;
        }
        else
        {
            elementName = localName;
        }
        if (elementName.equals("pattern"))
        {
            this.currentBuffer = this.patternBuffer = new StringBuilder();
        }
        else if (elementName.equals("bot") && this.currentBuffer != null && this.currentBuffer == this.patternBuffer)
        {
            // Insert the value of the given bot predicate (no warning if doesn't exist!).
            this.patternBuffer.append(this._bot.getPropertyValue(attributes.getValue("name")));
        }
        else if (elementName.equals("that") && this.currentBuffer == null || this.currentBuffer != this.templateBuffer)
        {
            this.currentBuffer = this.thatBuffer = new StringBuilder();
        }
        else if (elementName.equals("template"))
        {
            this.currentBuffer = this.templateBuffer = new StringBuilder();
        }
        else if (this.currentBuffer != null && this.currentBuffer == this.templateBuffer)
        {
            /*
             * We don't want to parse the template into some big memory structure, since it may never be used. So we
             * just reconstitute the XML text for later processing.
             */
            this.templateBuffer.append(SAX.renderStartTag(elementName, attributes, !uri
                    .equals(this._defaultNamespaceURI), uri));
        }
        else if (elementName.equals("topic"))
        {
            // We don't check that it's valid, because it's supposed to have been schema-validated already!
            this.topic = attributes.getValue("name");
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    @SuppressWarnings({"unused","boxing"})
    public void endElement(String uri, String localName, String qName)
    {
        String elementName;
        if ("".equals(localName))
        {
            elementName = qName;
        }
        else
        {
            elementName = localName;
        }

        if (this.currentBuffer != null && this.currentBuffer == this.patternBuffer)
        {
            this.pattern = this.patternBuffer.toString();
            this.currentBuffer = null;
        }
        else if (this.currentBuffer != null && this.currentBuffer == this.thatBuffer)
        {
            this.that = this.thatBuffer.toString();
            this.currentBuffer = null;
        }
        else if (elementName.equals("template"))
        {
            // Whitespace-normalize the template contents.
            this.template = String.format("%s%s</template>", this.templateStartTag, this.templateBuffer.toString());
            // Finally, deliver the newly defined category to the Graphmapper.
            this._graphmapper.addCategory(this.pattern, this.that, this.topic, this.template, this._bot,
                    this._path);
            // Reset the pattern, that and template.
            this.pattern = this.that = this.template = null;
            this.currentBuffer = this.patternBuffer = this.thatBuffer = this.templateBuffer = null;
        }
        else if (this.currentBuffer != null && this.currentBuffer == this.templateBuffer)
        {
            // See if we are ending an empty element.
            boolean makeClosingTag = true;

            // Get the template length (just once).
            int templateLength = this.templateBuffer.length();
            // If the last char of the template string is a '>',
            if (this.templateBuffer.substring(templateLength - 1).equals('>'))
            {
                // and if the next to last char is *not* a '/',
                if (!this.templateBuffer.substring(templateLength - 2, templateLength - 1).equals('/'))
                {
                    // then if the name of the last tag is the same as this one,
                    // this is an empty element.
                    int lastMarkerStart = this.templateBuffer.lastIndexOf("<");
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
                this.templateBuffer.append(String.format("</%s>", elementName));
            }
        }
        else if (elementName.equals("topic"))
        {
            this.topic = "*";
        }
    }

    /**
     * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
     */
    @Override
    public void error(SAXParseException e) throws SAXException
    {
        throw e;
    }
}
