/*
 * aitools utilities
 * Copyright (C) 2006 Noel Bush
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.aitools.util.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Map;

/**
 * XML utilities that pertain to character handling (markup or character data),
 * without use of any XML libraries.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class Characters
{
    /** The system default file encoding; defaults to UTF-8!!! */
    private static final String SYSTEM_ENCODING = System.getProperty("file.encoding", "UTF-8");

    /*
     * XML chars prohibited in some contexts and their escaped equivalents.
     */
    private static final String AMPERSAND = "&";

    private static final String XML_AMPERSAND = "&amp;";

    private static final String XML_AMPERSAND_REGEX = "&(amp|#0*38|#x0*26);";

    private static final String LESS_THAN = "<";

    private static final String XML_LESS_THAN = "&lt;";

    private static final String XML_LESS_THAN_REGEX = "&(lt|#0*60|#x0*3[cC]);";

    private static final String GREATER_THAN = ">";

    private static final String XML_GREATER_THAN = "&gt;";

    private static final String XML_GREATER_THAN_REGEX = "&(gt|#0*62|#x0*3[eE]);";

    private static final String QUOTE = "\"";

    private static final String XML_QUOTE = "&quot;";

    private static final String XML_QUOTE_REGEX = "&(quot|#0*34|#x0*22);";

    private static final String APOSTROPHE = "'";

    private static final String XML_APOSTROPHE = "&apos;";

    private static final String XML_APOSTROPHE_REGEX = "&(apos|#0*39|#x0*27);";

    /**
     * <p>
     * Replaces the following characters with their &quot;escaped&quot; equivalents:
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
        if (input == null)
        {
            return "";
        }
        return input.replace(AMPERSAND, XML_AMPERSAND).replace(LESS_THAN, XML_LESS_THAN).replace(GREATER_THAN,
                XML_GREATER_THAN).replace(QUOTE, XML_QUOTE).replace(APOSTROPHE, XML_APOSTROPHE);
    }

    /**
     * Like {@link #escapeXMLChars(String)}, but takes an array of chars instead of a String. This might be faster (but
     * should be tested).
     * 
     * @param ch the array of chars
     * @param start where to start reading in the array
     * @param length the length to read from the array
     * @return the string with XML chars escaped
     */
    public static String escapeXMLChars(char[] ch, int start, int length)
    {
        if (ch == null || length < 1 || start >= ch.length || start < 0 || ch.length == 0)
        {
            return "";
        }
        StringBuilder result = new StringBuilder(length);
        int end = start + length;
        for (int index = start; index < end; index++)
        {
            char cha = ch[index];
            switch (cha)
            {
                case '&':
                    result.append(XML_AMPERSAND);
                    break;
                case '<':
                    result.append(XML_LESS_THAN);
                    break;
                case '>':
                    result.append(XML_GREATER_THAN);
                    break;
                case '"':
                    result.append(XML_QUOTE);
                    break;
                case '\'':
                    result.append(XML_APOSTROPHE);
                    break;
                default:
                    result.append(cha);
            }
        }
        return result.toString();
    }

    /**
     * <p>
     * Replaces the following &quot;escape&quot; strings with their character equivalents:
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
        return input.replaceAll(XML_LESS_THAN_REGEX, LESS_THAN).replaceAll(XML_GREATER_THAN_REGEX, GREATER_THAN)
                .replaceAll(XML_AMPERSAND_REGEX, AMPERSAND).replaceAll(XML_QUOTE_REGEX, QUOTE).replaceAll(
                        XML_APOSTROPHE_REGEX, APOSTROPHE);
    }

    /**
     * Removes all characters that are not considered <a href="http://www.w3.org/TR/2000/REC-xml-20001006#charsets">XML
     * characters </a> from the input.
     * 
     * @param input the input to filter
     * @return the input with all non-XML characters removed
     */
    public static String filterXML(String input)
    {
        // Null inputs return an empty string.
        if (input == null)
        {
            return "";
        }

        // trim() removes all whitespace, not only spaces.
        String _input = input.trim();

        // Empty inputs return an empty string.
        if (_input.equals(("")))
        {
            return "";
        }

        // This StringBuilder will hold the result.
        StringBuilder result = new StringBuilder(_input.length());

        // This StringCharacterIterator will iterate over the input.
        StringCharacterIterator iterator = new StringCharacterIterator(_input);

        // Iterate over the input.
        for (char aChar = iterator.first(); aChar != CharacterIterator.DONE; aChar = iterator.next())
        {
            // Determine if this is a valid XML Character.
            if ((aChar == '\u0009') || (aChar == '\n') || (aChar == '\r')
                    || (('\u0020' <= aChar) && (aChar <= '\uD7FF')) || (('\uE000' <= aChar) && (aChar <= '\uFFFD')))
            {
                result.append(aChar);
            }
        }
        if (result.length() > _input.length())
        {
            return result.toString();
        }
        // (otherwise...)
        return _input;
    }

    /**
     * <p>
     * Converts XML Unicode character entities into their character equivalents within a given string.
     * </p>
     * <p>
     * This will handle entities in the form <code>&amp;#<i>xxxx</i>;</code> (decimal character code, where <i>xxxx
     * </i> is a valid character code), or <code>&amp;#x<i>xxxx</i></code> (hexadecimal character code, where
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

        StringBuilder result = new StringBuilder(inputLength);

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
                                result
                                        .append((char) Integer.decode(input.substring(pointer + 2, semicolon))
                                                .intValue());
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
                                result.append((char) Integer.parseInt(input.substring(pointer + 2, semicolon)));
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
     * Returns the declared encoding string from the XML resource at the given URL.
     * 
     * @param url the resource to look at
     * @return the declared encoding
     * @throws IOException if there was a problem reading the input stream
     */
    public static String getDeclaredXMLEncoding(URL url) throws IOException
    {
        // Look at the input stream using the platform default encoding.
        InputStream stream = url.openStream();
        BufferedReader buffReader = new BufferedReader(new InputStreamReader(stream));

        // Read the first line. May throw an IOException.
        String firstLine = buffReader.readLine();

        if (firstLine == null)
        {
            return SYSTEM_ENCODING;
        }

        // Look for the XML processing instruction.
        int piStart = firstLine.indexOf("<?xml version=\"1.0\"");

        if (piStart != -1)
        {
            int attributeStart = firstLine.indexOf("encoding=\"");
            if (attributeStart >= 0)
            {
                int nextQuote = firstLine.indexOf('"', attributeStart + 10);
                if (nextQuote >= 0)
                {
                    String encoding = firstLine.substring(attributeStart + 10, nextQuote);
                    return encoding.trim();
                }
            }
        }
        stream.close();
        // If encoding was unspecified, return the system encoding.
        return SYSTEM_ENCODING;
    }

    /**
     * Removes all tags from a string (retains character content of tags, however).
     * 
     * @param input the string from which to remove markup
     * @return the input without tags
     */
    public static String removeMarkup(String input)
    {
        // Null inputs return an empty string.
        if (input == null)
        {
            return "";
        }
        // Trim all whitespace at beginning and end.
        String _input = input.trim();

        // Empty trimmed inputs return an empty string.
        if (_input.equals(""))
        {
            return _input;
        }

        // No tags means no processing necessary.
        int tagStart = _input.indexOf('<');
        if (tagStart == -1)
        {
            return _input;
        }
        // (otherwise...)
        // tagEnd indexes the end of a tag.
        int tagEnd = 0;

        // lastEnd indexes the previous end of a tag.
        int lastEnd = 0;

        // inputLength avoids recalculating input.length().
        int inputLength = _input.length();

        // Results will be built up in this buffer.
        StringBuilder result = new StringBuilder();

        // Break lines at tags.
        while ((tagStart > -1) && (tagEnd > -1))
        {
            // Get the end of a tag.
            tagEnd = _input.indexOf('>', lastEnd);

            // Add the input until the tag as a line, as long as the tag is not
            // the beginning.
            if (tagStart > 0)
            {
                result.append(_input.substring(lastEnd, tagStart));
            }

            // Set last end to the character following the end of the tag.
            lastEnd = tagEnd + 1;

            // Look for another tag.
            tagStart = _input.indexOf('<', lastEnd);
        }
        // All tags are exhausted; if there is still something left in the
        // input,
        if ((lastEnd < inputLength) && (lastEnd > 0))
        {
            // Add the remainder as the final line.
            result.append(_input.substring(lastEnd));
        }
        return result.toString();
    }

    /**
     * Renders a set of name-value pairs as attributes.
     * 
     * @param attributes the name-value pairs
     * @return the rendered attributes
     */
    public static String renderAttributes(Map<String, String> attributes)
    {
        StringBuilder result = new StringBuilder();
        if (attributes != null)
        {
            for (Map.Entry<String, String> attribute : attributes.entrySet())
            {
                String attributeName = attribute.getKey();
                if (attributeName != null && !"xmlns".equals(attributeName))
                {
                    result.append(String.format(" %s=\"%s\"", attributeName, attribute.getValue()));
                }
            }
        }
        return result.toString();
    }
}
