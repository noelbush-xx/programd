/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A collection of XML utilities.
 */
public class XMLKit
{
    /** An empty string. */
    private static final String EMPTY_STRING = "";

    /** An empty string array (one element). */
    private static final String[] EMPTY_STRING_ARRAY = { (EMPTY_STRING) };

    /** A space, for convenience. */
    private static final String SPACE = " ";

    /** The system line separator. */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /** A tag start marker. */
    private static final char MARKER_START = '<';

    /** A tag end marker. */
    private static final char MARKER_END = '>';

    /** The beginning of an end tag. */
    private static final String END_TAG_START = "</";

    /** An empty element tag end marker. */
    private static final String EMPTY_ELEMENT_TAG_END = "/>";

    /** CDATA start marker. */
    private static final String CDATA_START = "<![CDATA[";

    /** CDATA end marker. */
    private static final String CDATA_END = "]]>";

    /** Comment start marker. */
    private static final String COMMENT_START = "<!--";

    /** Comment end marker. */
    private static final String COMMENT_END = "-->";

    /** A common string we search for when parsing attributes in tags. */
    protected static final String EQUAL_QUOTE = "=\"";

    /** A quote mark, for convenience. */
    protected static final char QUOTE_MARK = '"';

    /** The regex for whitespace. */
    protected static final String WHITESPACE_REGEX = "\\s+";

    /**
     * Mapping between XML chars prohibited in some contexts and their escaped
     * equivalents.
     */
    private static final String[][] XML_ESCAPES = { { "&amp;", "&" }, { "&lt;", "<" },
            { "&gt;", ">" }, { "&apos;", "'" }, { "&quot;", "\"" } };

    /** The start of an XML processing instruction. */
    private static final String XML_PI_START = "<?xml version=\"1.0\"";

    /** The string &apos;encoding=&quot;&apos;. */
    private static final String ENCODING_EQUALS_QUOTE = "encoding=\"";

    /** The length of {@link #ENCODING_EQUALS_QUOTE} . */
    private static final int ENCODING_EQUALS_QUOTE_LENGTH = ENCODING_EQUALS_QUOTE.length();

    /** The system default file encoding; defaults to UTF-8!!! */
    private static final String SYSTEM_ENCODING = System.getProperty("file.encoding", "UTF-8");

    /** The string &apos;{@value}&apos;. */
    private static final String SPACE_XMLNS_EQUALS_QUOTE = " xmlns=\"";

    /** The string &apos;{@value}&apos;. */
    private static final String XMLNS = "xmlns";

    /** An HTML &lt;br/&gt; element, including namespace attribute. (&apos;{@value}&apos;) */
    private static final String BR = "<br xmlns=\"http://www.w3.org/1999/xhtml\"/>";

    /**
     * An HTML &lt;/p&gt; end tag (<i>not</i> including namespace attribute).
     * (&apos;{@value}&apos;)
     */
    private static final String END_P = "</p>";

    /** The actual Map used to store prohibited-to-escaped mappings. */
    private static HashMap<Pattern, String> xmlProhibited;

    /** The actual Map used to store escaped-to-prohibited mappings. */
    private static HashMap<Pattern, String> xmlEscapes;

    /** A DocumentBuilder for producing new documents. */
    protected static DocumentBuilder utilBuilder;

    /** A document for producing new elements. */
    protected static Document utilDoc;

    static
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try
        {
            utilBuilder = factory.newDocumentBuilder();
            utilDoc = utilBuilder.newDocument();
        }
        catch (ParserConfigurationException e)
        {
            throw new DeveloperError("Error creating utilDoc.", e);
        }
    }

    /**
     * <p>
     * Replaces the following &quot;escape&quot; strings with their character
     * equivalents:
     * </p>
     * <code>
     *  <ul>
     *      <li>&amp;amp; with &amp;</li>
     *      <li>&amp;lt; with &lt;</li>
     *      <li>&amp;gt; with &gt;</li>
     *      <li>&amp;apos; with &apos;</li>
     *      <li>&amp;quot; with &quot;</li>
     *  </ul>
     *  </code>
     * 
     * @param input the string on which to perform the replacement
     * @return the string with entities replaced
     */
    public static String unescapeXMLChars(String input)
    {
        /*
         * This one-time-only initialization of xmlEscaped lets us avoid
         * creating needless strings by repeated application of the simpler
         * replace() method.
         */
        if (xmlEscapes == null)
        {
            xmlEscapes = new HashMap<Pattern, String>(XML_ESCAPES.length);
            for (int index = XML_ESCAPES.length; --index >= 0;)
            {
                xmlEscapes.put(Pattern.compile(XML_ESCAPES[index][0], Pattern.LITERAL),
                        XML_ESCAPES[index][1]);
            }
        }
        return Substituter.applySubstitutions(xmlEscapes, input);
    }

    /**
     * <p>
     * Replaces the following characters with their &quot;escaped&quot;
     * equivalents:
     * </p>
     * <code>
     *  <ul>
     *      <li>&amp; with &amp;amp;</li>
     *      <li>&lt; with &amp;lt;</li>
     *      <li>&gt; with &amp;gt;</li>
     *      <li>&apos; with &amp;apos;</li>
     *      <li>&quot; with &amp;quot;</li>
     *  </ul>
     *  </code>
     * 
     * @param input the string on which to perform the replacement
     * @return the string with entities replaced
     */
    public static String escapeXMLChars(String input)
    {
        /*
         * This one-time-only initialization of xmlProhibited lets us avoid
         * creating needless strings by repeated application of the simpler
         * replace() method.
         */
        if (xmlProhibited == null)
        {
            xmlProhibited = new HashMap<Pattern, String>(XML_ESCAPES.length);
            for (int index = XML_ESCAPES.length; --index >= 0;)
            {
                xmlProhibited.put(Pattern.compile(XML_ESCAPES[index][1], Pattern.LITERAL),
                        XML_ESCAPES[index][0]);
            }
        }
        return Substituter.applySubstitutions(xmlProhibited, input);
    }

    /**
     * Removes all characters that are not considered <a
     * href="http://www.w3.org/TR/2000/REC-xml-20001006#charsets">XML characters
     * </a> from the input.
     * 
     * @param input the input to filter
     * @return the input with all non-XML characters removed
     */
    private static String filterXML(String input)
    {
        // Null inputs return an empty string.
        if (input == null)
        {
            return EMPTY_STRING;
        }

        // trim() removes all whitespace, not only spaces.
        input = input.trim();

        // Empty inputs return an empty string.
        if (input.equals((EMPTY_STRING)))
        {
            return EMPTY_STRING;
        }

        // This StringBuffer will hold the result.
        StringBuffer result = new StringBuffer(input.length());

        // This StringCharacterIterator will iterate over the input.
        StringCharacterIterator iterator = new StringCharacterIterator(input);

        // Iterate over the input.
        for (char aChar = iterator.first(); aChar != CharacterIterator.DONE; aChar = iterator
                .next())
        {
            // Determine if this is a valid XML Character.
            if ((aChar == '\u0009') || (aChar == '\n') || (aChar == '\r')
                    || (('\u0020' <= aChar) && (aChar <= '\uD7FF'))
                    || (('\uE000' <= aChar) && (aChar <= '\uFFFD')))
            {
                result.append(aChar);
            }
        }
        if (result.length() > input.length())
        {
            return result.toString();
        }
        // (otherwise...)
        return input;
    }

    /**
     * <p>
     * Converts XML Unicode character entities into their character equivalents
     * within a given string.
     * </p>
     * <p>
     * This will handle entities in the form <code>&amp;#<i>xxxx</i>;</code>
     * (decimal character code, where <i>xxxx </i> is a valid character code),
     * or <code>&amp;#x<i>xxxx</i></code> (hexadecimal character code, where
     * <i>xxxx </i> is a valid character code).
     * </p>
     * 
     * @param input the string to process
     * @return the input with all XML Unicode character entity codes replaced
     */
    public static String convertXMLUnicodeEntities(String input)
    {
        int inputLength = input.length();
        int pointer = 0;

        StringBuffer result = new StringBuffer(inputLength);

        while (pointer < input.length())
        {
            if (input.charAt(pointer) == '&')
            {
                if (input.charAt(pointer + 1) == '#')
                {
                    // Hexadecimal character code.
                    if (input.charAt(pointer + 2) == 'x')
                    {
                        int semicolon = input.indexOf(';', pointer + 3);
                        // Check that the semicolon is not so far away that it
                        // is likely not part of this entity.
                        if (semicolon < pointer + 7)
                        {
                            try
                            {
                                // Integer.decode from pointer + 2 includes the
                                // "x".
                                result.append((char) Integer.decode(
                                        input.substring(pointer + 2, semicolon)).intValue());
                                pointer += (semicolon - pointer + 1);
                            }
                            catch (NumberFormatException e)
                            {
                                // drop out
                            }
                        }
                    }
                    // Decimal character code.
                    else
                    {
                        // Check that the semicolon is not so far away that it
                        // is likely not part of this entity.
                        int semicolon = input.indexOf(';', pointer + 2);
                        if (semicolon < pointer + 7)
                        {
                            try
                            {
                                // Integer.parseInt from pointer + 2 excludes
                                // the "&#".
                                result.append((char) Integer.parseInt(input.substring(pointer + 2,
                                        semicolon)));
                                pointer += (semicolon - pointer + 1);
                                continue;
                            }
                            catch (NumberFormatException e)
                            {
                                // drop out
                            }
                        }
                    }
                }
            }
            result.append(input.charAt(pointer));
            pointer++;
        }
        return result.toString();
    }

    /**
     * Returns the declared encoding string from the XML resource supposedly
     * connected to a given InputStream, or the system default if none is found.
     * 
     * @param in the input stream
     * @return the declared encoding
     * @throws IOException if there was a problem reading the input stream
     */
    public static String getDeclaredXMLEncoding(InputStream in) throws IOException
    {
        // Look at the input stream using the platform default encoding.
        BufferedReader buffReader = new BufferedReader(new InputStreamReader(in));

        // Read the first line. May throw an IOException.
        String firstLine = buffReader.readLine();

        if (firstLine == null)
        {
            return SYSTEM_ENCODING;
        }

        // Look for the XML processing instruction.
        int piStart = firstLine.indexOf(XML_PI_START);

        if (piStart != -1)
        {
            int attributeStart = firstLine.indexOf(ENCODING_EQUALS_QUOTE);
            if (attributeStart >= 0)
            {
                int nextQuote = firstLine.indexOf(QUOTE_MARK, attributeStart
                        + ENCODING_EQUALS_QUOTE_LENGTH);
                if (nextQuote >= 0)
                {
                    String encoding = firstLine.substring(attributeStart
                            + ENCODING_EQUALS_QUOTE_LENGTH, nextQuote);
                    return encoding.trim();
                }
            }
        }
        // If encoding was unspecified, return the system encoding.
        return SYSTEM_ENCODING;
    }
    
    /**
     * @param text a document fragment
     * @return a Document created by parsing the given text as a document fragment
     */
    public static Document parseAsDocumentFragment(String text)
    {
        Document result;
        try
        {
            result = utilBuilder.parse(new InputSource(new StringReader(text)));
        }
        catch (IOException e)
        {
            throw new DeveloperError("I/O Error processing XML fragment.", e);
        }
        catch (SAXException e)
        {
            throw new DeveloperError("SAX Exception processing XML fragment.", e);
        }
        return result;
    }

    /**
     * Formats XML from a single long string into a nicely indented multi-line
     * string (if <code>indent</code> is <code>true</code>), or just a long
     * string (if <code>indent</code> is <code>false</code>).
     * 
     * @param content the XML content to format
     * @param includeNamespaceAttribute whether to include the namespace
     *            attribute
     * @param indent whether to render the string in an indented, multiline
     *            fashion
     * @return the formatted XML
     */
    public static String renderXML(String content, boolean includeNamespaceAttribute, boolean indent)
    {
        Document document = parseAsDocumentFragment(content);
        StringBuffer result = new StringBuffer();
        renderXML(document.getDocumentElement(), 0, true, result, includeNamespaceAttribute, indent);
        return filterWhitespace(result.toString());
    }


    /**
     * Formats XML from a node list into a nicely indented multi-line string (if
     * <code>indent</code> is <code>true</code>), or just a long string (if
     * <code>indent</code> is <code>false</code>). This is a convenience method
     * that assumes that we should include namespace attributes.
     * 
     * @param list the list of XML nodes
     * @param indent whether to render the string in an indented, multiline
     *            fashion
     * @return the formatted XML
     */
    public static String renderXML(NodeList list, boolean indent)
    {
        return filterWhitespace(renderXML(list, 0, true, true, indent));
    }

    /**
     * Formats XML from a node list into a nicely indented multi-line string (if
     * <code>indent</code> is <code>true</code>), or just a long string (if
     * <code>indent</code> is <code>false</code>).
     * 
     * @param list the list of XML nodes
     * @param level the level (for indenting; no meaning if indenting is off)
     * @param atStart whether the whole XML string is at its beginning
     * @param includeNamespaceAttribute whether to include the namespace
     *            attribute
     * @param indent whether to render the string in an indented, multiline
     *            fashion
     * @return the formatted XML
     */
    private static String renderXML(NodeList list, int level, boolean atStart,
            boolean includeNamespaceAttribute, boolean indent)
    {
        StringBuffer result = new StringBuffer();

        int listLength = list.getLength();

        for (int index = 0; index < listLength; index++)
        {
            Node node = list.item(index);
            renderXML(node, level, atStart, result, includeNamespaceAttribute, indent);
        }
        return result.toString();
    }

    /**
     * Formats an XML node, putting the result into the buffer passed as an argument.
     * 
     * @param node the node to format
     * @param level the level (for indenting; no meaning if indenting is off)
     * @param atStart whether the whole XML string is at its beginning
     * @param result the buffer into which to place the result
     * @param includeNamespaceAttribute whether to include the namespace
     *            attribute
     * @param indent whether to render the string in an indented, multiline
     *            fashion
     */
    private static void renderXML(Node node, int level, boolean atStart, StringBuffer result,
            boolean includeNamespaceAttribute, boolean indent)
    {
        switch (node.getNodeType())
        {
            // Append a start tag.
            case Node.ELEMENT_NODE:
                if (indent)
                {
                    if (!atStart)
                    {
                        result.append(LINE_SEPARATOR);
                    }
                    else
                    {
                        atStart = false;
                    }
                }
                String contents = renderXML(node.getChildNodes(), level + 1, true,
                        includeNamespaceAttribute, indent);
                if (contents.trim().length() > 0)
                {
                    if (indent)
                    {
                        result.append(StringKit.tab(level));
                    }
                    result.append(renderStartTag((Element) node, includeNamespaceAttribute));
                    if (indent)
                    {
                        result.append(LINE_SEPARATOR);
                    }
                    result.append(contents);
                    if (indent)
                    {
                        result.append(LINE_SEPARATOR + StringKit.tab(level));
                    }
                    result.append(renderEndTag((Element) node));
                }
                else
                {
                    if (indent)
                    {
                        result.append(StringKit.tab(level));
                    }
                    result.append(renderEmptyElement((Element) node, includeNamespaceAttribute));
                }
                break;

            // Append text content.
            case Node.TEXT_NODE:
                if (indent && atStart)
                {
                    if (node.getTextContent().trim().length() > 0)
                    {
                        result.append(StringKit.tab(level) + node.getTextContent());
                        atStart = false;
                        break;
                    }
                }
                // (otherwise)
                result.append(node.getTextContent());
                break;

            // Append CDATA.
            case Node.CDATA_SECTION_NODE:
                if (indent)
                {
                    if (atStart)
                    {
                        result.append(StringKit.tab(level));
                        atStart = false;
                    }
                    result.append(LINE_SEPARATOR + StringKit.tab(level));
                }
                result.append(CDATA_START + node.getNodeValue() + CDATA_END);
                break;

            // Append comments.
            case Node.COMMENT_NODE:
                if (indent)
                {
                    if (atStart)
                    {
                        result.append(StringKit.tab(level));
                        atStart = false;
                    }
                    result.append(LINE_SEPARATOR + StringKit.tab(level));
                }
                result.append(MARKER_START + COMMENT_START + node.getNodeValue() + COMMENT_END);
                break;
        }
    }

    /**
     * <p>
     * Filters all whitespace: line separators and multiple consecutive spaces
     * are replaced with a single space, and any leading or trailing whitespace
     * characters are removed. Any data enclosed in
     * <code>&lt;![CDATA[</code> <code>]]&gt;</code> sections, however, is
     * left as-is (including the CDATA markers).
     * </p>
     * 
     * @param input the input to filter
     * @return the input with white space filtered.
     * @throws StringIndexOutOfBoundsException if there is malformed text in the
     *             input.
     */
    public static String filterWhitespace(String input) throws StringIndexOutOfBoundsException
    {
        // Check if this contains a cdata start marker.
        int cdataStart = input.indexOf(CDATA_START);

        // In the most common case (not), filter the whole string in one pass.
        if (cdataStart == -1)
        {
            return filterXML(input.trim()).replaceAll(WHITESPACE_REGEX, SPACE);
        }
        // If there is a cdata start marker, this will be slower!
        // Ensure that there is a cdata end marker.
        int cdataEnd = input.indexOf(CDATA_END) + 2;
        if (cdataEnd != -1)
        {
            // Two possibilities: the string ends with cdata, or doesn't.
            if (cdataEnd < input.length())
            {
                // Most likely (?) that it doesn't.
                return filterWhitespace(input.substring(0, cdataStart))
                        + input.substring(cdataStart, cdataEnd)
                        + filterWhitespace(input.substring(cdataEnd));
            }
            // As above, in either case, don't filter the cdata part.
            return filterWhitespace(input.substring(0, cdataStart))
                    + input.substring(cdataStart, cdataEnd);
        }
        // If there was no cdata end marker, we have wasted our time. Duplicate
        // code from above.
        return filterXML(input.trim()).replace(WHITESPACE_REGEX, SPACE);
    }

    /**
     * <p>
     * Breaks a message into multiple lines at an HTML &lt;br/&gt;, except if it
     * comes at the beginning of the message, or ending HTML &lt;/p&gt;. Other
     * tags are just removed.
     * </p>
     * <p>
     * Generally used to format output nicely for a console.
     * </p>
     * 
     * @param input the string to break
     * @return one line per array item
     */
    public static String[] filterViaHTMLTags(String input)
    {
        // Null inputs return an empty string array.
        if (input == null)
        {
            return EMPTY_STRING_ARRAY;
        }
        // Trim all whitespace at beginning and end.
        input = input.trim();

        // Empty trimmed inputs return an empty string array.
        if (input.equals(EMPTY_STRING))
        {
            return EMPTY_STRING_ARRAY;
        }

        // No tags means no breaks.
        int tagStart = input.indexOf(MARKER_START);
        if (tagStart == -1)
        {
            return new String[] { input };
        }
        // (otherwise...)
        // tagEnd indexes the end of a tag.
        int tagEnd = 0;

        // lastEnd indexes the previous end of a tag.
        int lastEnd = 0;

        // inputLength avoids recalculating input.length().
        int inputLength = input.length();

        // Results will be delivered by calling toArray() on this Vector.
        Vector<String> result = new Vector<String>();

        // This will hold each line as it is assembled.
        StringBuffer line = new StringBuffer();

        // Break lines at tags.
        while ((tagStart > -1) && (tagEnd > -1))
        {
            // Get the end of a tag.
            tagEnd = input.indexOf(MARKER_END, lastEnd);

            // Add the input until the tag as an addition to the line, as long
            // as the tag is not
            // the beginning.
            if (tagStart > 0)
            {
                line.append(input.substring(lastEnd, tagStart).trim());
            }

            // If the tag start begins a <br/> or a </p>, then this is the end
            // of the line.
            if (input.indexOf(BR, tagStart) == tagStart
                    || input.indexOf(END_P, tagStart) == tagStart)
            {
                result.addElement(line.toString());
                line = new StringBuffer();
            }

            // Set last end to the character following the end of the tag.
            lastEnd = tagEnd + 1;

            // Look for another tag.
            tagStart = input.indexOf(MARKER_START, lastEnd);
        }
        // All tags are exhausted; if there is still something left in the
        // input,
        if ((lastEnd < inputLength) && (lastEnd > 0))
        {
            // Add the remainder as the final line.
            result.addElement(input.substring(lastEnd).trim());
        }
        return result.toArray(new String[] {});
    }

    /**
     * Removes all tags from a string (retains character content of tags,
     * however).
     * 
     * @param input the string from which to remove markup
     * @return the input without tags
     */
    public static String removeMarkup(String input)
    {
        // Null inputs return an empty string.
        if (input == null)
        {
            return EMPTY_STRING;
        }
        // Trim all whitespace at beginning and end.
        input = input.trim();

        // Empty trimmed inputs return an empty string.
        if (input.equals(EMPTY_STRING))
        {
            return input;
        }

        // No tags means no processing necessary.
        int tagStart = input.indexOf(MARKER_START);
        if (tagStart == -1)
        {
            return input;
        }
        // (otherwise...)
        // tagEnd indexes the end of a tag.
        int tagEnd = 0;

        // lastEnd indexes the previous end of a tag.
        int lastEnd = 0;

        // inputLength avoids recalculating input.length().
        int inputLength = input.length();

        // Results will be built up in this buffer.
        StringBuffer result = new StringBuffer();

        // Break lines at tags.
        while ((tagStart > -1) && (tagEnd > -1))
        {
            // Get the end of a tag.
            tagEnd = input.indexOf(MARKER_END, lastEnd);

            // Add the input until the tag as a line, as long as the tag is not
            // the beginning.
            if (tagStart > 0)
            {
                result.append(input.substring(lastEnd, tagStart));
            }

            // Set last end to the character following the end of the tag.
            lastEnd = tagEnd + 1;

            // Look for another tag.
            tagStart = input.indexOf(MARKER_START, lastEnd);
        }
        // All tags are exhausted; if there is still something left in the
        // input,
        if ((lastEnd < inputLength) && (lastEnd > 0))
        {
            // Add the remainder as the final line.
            result.append(input.substring(lastEnd));
        }
        return result.toString();
    }

    /**
     * Renders a given element as a start tag, including a namespace
     * declaration, if requested.
     * 
     * @param element the element to render
     * @param includeNamespaceAttribute whether to include the namespace
     *            attribute
     * @return the rendering of the element
     */
    public static String renderStartTag(Element element, boolean includeNamespaceAttribute)
    {
        StringBuffer result = new StringBuffer();
        result.append(MARKER_START);
        String name = element.getLocalName();
        if (name == null)
        {
            name = element.getNodeName();
        }
        result.append(name);
        if (includeNamespaceAttribute)
        {
            result.append(SPACE_XMLNS_EQUALS_QUOTE + element.getNamespaceURI() + QUOTE_MARK);
        }
        result.append(renderAttributes(element.getAttributes()));
        result.append(MARKER_END);
        return result.toString();
    }

    /**
     * Renders a given element name and set of attributes as a start tag,
     * including a namespace declaration, if requested.
     * 
     * @param elementName the name of the element to render
     * @param attributes the attributes to include
     * @param includeNamespaceAttribute whether or not to include the namespace
     *            attribute
     * @param namespaceURI the namespace URI
     * @return the rendering result
     */
    public static String renderStartTag(String elementName, Attributes attributes,
            boolean includeNamespaceAttribute, String namespaceURI)
    {
        StringBuffer result = new StringBuffer();
        result.append(MARKER_START);
        result.append(elementName);
        if (includeNamespaceAttribute)
        {
            result.append(SPACE_XMLNS_EQUALS_QUOTE + namespaceURI + QUOTE_MARK);
        }
        result.append(renderAttributes(attributes));
        result.append(MARKER_END);
        return result.toString();
    }

    /**
     * Renders a given element as an empty element, including a namespace
     * declaration, if requested.
     * 
     * @param element the element to render
     * @param includeNamespaceAttribute whether to include the namespace
     *            attribute
     * @return the result of the rendering
     */
    public static String renderEmptyElement(Element element, boolean includeNamespaceAttribute)
    {
        StringBuffer result = new StringBuffer();
        result.append(MARKER_START);
        String name = element.getLocalName();
        if (name == null)
        {
            name = element.getNodeName();
        }
        result.append(name);
        if (includeNamespaceAttribute)
        {
            result.append(SPACE_XMLNS_EQUALS_QUOTE + element.getNamespaceURI() + QUOTE_MARK);
        }
        result.append(renderAttributes(element.getAttributes()));
        result.append(EMPTY_ELEMENT_TAG_END);
        return result.toString();
    }
    
    /**
     * Renders a set of attributes.
     * 
     * @param attributes the attributes to render
     * @return the rendered attributes
     */
    private static String renderAttributes(Attributes attributes)
    {
        StringBuffer result = new StringBuffer();
        if (attributes != null)
        {
            int attributeCount = attributes.getLength();
            for (int index = 0; index < attributeCount; index++)
            {
                String attributeName = attributes.getLocalName(index);
                if (attributeName == null || EMPTY_STRING.equals(attributeName))
                {
                    attributeName = attributes.getQName(index);
                }
                if (attributeName != null && !XMLNS.equals(attributeName) && !attributes.getQName(index).contains(XMLNS))
                {
                    result.append(SPACE);
                    result
                            .append(attributeName + EQUAL_QUOTE + attributes.getValue(index)
                                    + QUOTE_MARK);
                }
            }
        }
        return result.toString();
    }

    /**
     * Renders a set of attributes.
     * 
     * @param attributes the attributes to render
     * @return the rendered attributes
     */
    private static String renderAttributes(NamedNodeMap attributes)
    {
        StringBuffer result = new StringBuffer();
        if (attributes != null)
        {
            int attributeCount = attributes.getLength();
            for (int index = 0; index < attributeCount; index++)
            {
                Node attribute = attributes.item(index);
                String attributeName = attribute.getLocalName();
                if (attributeName == null || EMPTY_STRING.equals(attributeName))
                {
                    attributeName = attribute.getNodeName();
                }
                if (attributeName != null && !XMLNS.equals(attributeName) && !XMLNS.equals(attribute.getPrefix()))
                {
                    result.append(SPACE);
                    result
                            .append(attributeName + EQUAL_QUOTE + attribute.getNodeValue()
                                    + QUOTE_MARK);
                }
            }
        }
        return result.toString();
    }

    /**
     * Renders a given element as an end tag.
     * 
     * @param element the element to render
     * @return the result of the rendering
     */
    public static String renderEndTag(Element element)
    {
        String name = element.getLocalName();
        if (name == null)
        {
            name = element.getNodeName();
        }
        return END_TAG_START + name + MARKER_END;
    }

    /**
     * @param count the number of spaces to return
     * @return the given number of spaces.
     */
    public static String getSpaces(int count)
    {
        StringBuffer result = new StringBuffer(count);
        for (int index = 0; index < count; index++)
        {
            result.append(' ');
        }
        return result.toString();
    }

    /**
     * Sets up a SAX parser that is schema-aware, processes XIncludes, and is
     * set to use the schema at the given location.
     * 
     * @param schemaLocation location of the schema to use
     * @param schemaDescription short (one word or so) description of the schema
     * @return the parser
     */
    public static SAXParser getSAXParser(String schemaLocation, String schemaDescription)
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setXIncludeAware(true);

        factory.setSchema(getSchema(schemaLocation, schemaDescription));

        try
        {
            return factory.newSAXParser();
        }
        catch (SAXException e)
        {
            throw new DeveloperError(
                    "SAX exception occurred while creating parser for Graphmaster.", e);
        }
        catch (ParserConfigurationException e)
        {
            throw new DeveloperError(
                    "Parser configuration exception occurred while creating parser for Graphmaster.",
                    e);
        }
    }

    /**
     * Sets up a SAX parser that is schema-aware, processes XIncludes, and is
     * set to use the schema at the given location.
     * 
     * @param schemaLocation location of the schema to use
     * @param schemaDescription short (one word or so) description of the schema
     * @return the parser
     */
    public static DocumentBuilder getDocumentBuilder(String schemaLocation, String schemaDescription)
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setXIncludeAware(true);

        factory.setSchema(getSchema(schemaLocation, schemaDescription));

        try
        {
            return factory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e)
        {
            throw new DeveloperError(
                    "Parser configuration exception occurred while creating parser for Graphmaster.",
                    e);
        }
    }

    /**
     * Attempts to get the schema at the given location.
     * 
     * @param schemaLocation location of the schema to use
     * @param schemaDescription short (one word or so) description of the schema
     * @return the schema
     */
    public static Schema getSchema(String schemaLocation, String schemaDescription)
    {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try
        {
            return schemaFactory.newSchema(URITools.createValidURL(schemaLocation));
        }
        catch (SAXException e)
        {
            throw new DeveloperError("SAX error occurred while parsing " + schemaDescription
                    + " schema.", e);
        }
    }

    /**
     * Returns the element children of the given element.
     * 
     * @param element the element whose children are wanted
     * @return the element children of the given element
     */
    public static List<Element> getElementChildrenOf(Element element)
    {
        ArrayList<Element> result = new ArrayList<Element>();
        NodeList children = element.getChildNodes();
        int childCount = children.getLength();
        for (int index = 0; index < childCount; index++)
        {
            Node child = children.item(index);
            if (child.getNodeType() == Node.ELEMENT_NODE)
            {
                result.add((Element) child);
            }
        }
        return result;
    }

    /**
     * Returns the first element child of the given element.
     * 
     * @param element the element whose child is wanted
     * @return the first element child of the given element
     */
    public static Element getFirstElementChildOf(Element element)
    {
        return getElementChildrenOf(element).get(0);
    }

    /**
     * Returns the first element member (if there is one) of the given nodelist.
     * 
     * @param list the nodes to scan
     * @return the first element member of the given list
     */
    public static Element getFirstElementIn(NodeList list)
    {
        int listSize = list.getLength();
        for (int index = 0; index < listSize; index++)
        {
            Node node = list.item(index);
            if (node.getNodeType() == Node.ELEMENT_NODE)
            {
                return (Element)node;
            }
        }
        throw new DeveloperError("No element children of this nodelist!", new NullPointerException());
    }
}