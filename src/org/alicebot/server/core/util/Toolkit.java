/*
    Alicebot Program D
    Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

/*
    Code cleanup (4.1.3 [00] - October 2001, Noel Bush)
    - formatting cleanup
    - general grammar fixes
    - added getImplementorsOf method
    - added breakLines and removed fixBR
    - removed process_load_tags (was only used in one place, not needed)
    - introduced filterWhitespace and associated private methods
    - removed these methods not used anywhere:
      checkpoint
      DialogToAIML
      readfilenames
      load
      unload
      unload_aiml_file
      load_aiml_file
      classify
      think
      countbranches
      cps
      initcounts
      vote
*/

/*
    More fixes (4.1.3 [02] - November 2001, Noel Bush)
    - corrected filterWhitespace so that line separators are
      converted to spaces rather than simply removed
    - added removeMarkup method (almost the same as breakLines,
      except it returns result as one string)
    - added unescapeXMLChars and escapeXMLChars methods
*/

/*
    4.1.4 [00] - December 2001, Noel Bush
    - made filterMultipleConsecutive actually work!
    - added tabs to characters that get filtered
*/

package org.alicebot.server.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;

import java.net.MalformedURLException;
import java.net.URL;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.Vector;

import java.util.jar.JarFile;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.parser.XMLParser;
import org.alicebot.server.core.parser.XMLNode;
import org.alicebot.server.core.util.Trace;


/**
 *  Contains various utilities.
 *
 *  @author Richard Wallace
 *  @author Jon Baer
 *  @author Thomas Ringate, Pedro Colla
 *  @author Noel Bush
 */
public class Toolkit
{
    /** An empty string. */
    private static final String EMPTY_STRING = "";
    
    /** An empty string array (one element). */
    private static final String[] EMPTY_STRING_ARRAY = {(EMPTY_STRING)};
    
    /** A tag start marker. */
    private static final char TAG_START = '<';
    
    /** A tag end marker. */
    private static final char TAG_END = '>';

    /** A space, for convenience. */
    private static final String SPACE = " ";

    /** CDATA start marker. */
    private static final String CDATA_START = "<![CDATA[";

    /** CDATA end marker. */
    private static final String CDATA_END   = "]]>";

    /** A tab, for convenience. */
    private static final String TAB = new Character('\u0009').toString();

    /** A common string we search for when parsing attributes in tags. */
    protected static final String EQUAL_QUOTE = "=\"";

    /** A quote mark, for convenience. */
    protected static final char QUOTE_MARK = '"';

    /** The system line separator. */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /** Mapping between XML chars prohibited in some contexts and their escaped equivalents. */
    private static final String[][] XML_ESCAPES = { {"&amp;",  "&"},
                                                    {"&lt;",   "<"},
                                                    {"&gt;",   ">"},
                                                    {"&apos;", "'"},
                                                    {"&quot;", "\""} };

    /** The string &quot;UTF-8&quot;. */
    private static final String UTF8 = "UTF-8";

    /** The start of an XML processing instruction. */
    private static final String XML_PI_START = "<?xml version=\"1.0\"";

    /** The length of {@link #XML_PI_START}. */
    private static final int XML_PI_START_LENGTH = XML_PI_START.length();

    /** The string &quot;encoding&quot;. */
    private static final String ENCODING = "encoding";

    /** The system default file encoding; defaults to UTF-8!!! */
    private static final String SYSTEM_ENCODING = System.getProperty("file.encoding", "UTF-8");

    /** The actual Map used to store prohibited-to-escaped mappings. */
    private static HashMap xmlProhibited;

    /** The actual Map used to store escaped-to-prohibited mappings. */
    private static HashMap xmlEscapes;

    /** The current working directory (used by load). */
    private static String WORKING_DIRECTORY = System.getProperty("user.dir");


    /**
     *  <p>
     *  Filters all whitespace: line separators
     *  and multiple consecutive spaces are replaced with a single
     *  space, and any leading or trailing whitespace characters are removed.
     *  Any data enclosed in <code>&lt;![CDATA[</code> <code>]]&gt;</code> sections,
     *  however, is left as-is (including the CDATA markers).
     *  </p>
     *
     *  @param input    the input to filter
     *
     *  @return the input with white space filtered.
     *
     *  @throws StringIndexOutOfBoundsException (from {@link removeAll} if there is malformed text in the input.
     */
    public static String filterWhitespace(String input) throws StringIndexOutOfBoundsException
    {
        // Check if this contains a cdata start marker.
        int cdataStart = input.indexOf(CDATA_START);

        // In the most common case (not), filter the whole string in one pass.
        if (cdataStart == -1)
        {
            return filterMultipleConsecutive(
                    Substituter.replace(TAB, SPACE,
                        Substituter.replace(LINE_SEPARATOR, SPACE, filterXML(input))), SPACE).trim();
        }
        // If there is a cdata start marker, this will be slower!
        else
        {
            // Ensure that there is a cdata end marker.
            int cdataEnd = input.indexOf(CDATA_END) + 2;
            if (cdataEnd != -1)
            {
                // Two possibilities: the string ends with cdata, or doesn't.
                if (cdataEnd < input.length())
                {
                    // Most likely (?) that it doesn't.
                    return filterWhitespace(input.substring(0, cdataStart)) +
                           input.substring(cdataStart, cdataEnd) +
                           filterWhitespace(input.substring(cdataEnd));
                }
                // As above, in either case, don't filter the cdata part.
                else
                {
                    return filterWhitespace(input.substring(0, cdataStart)) +
                           input.substring(cdataStart, cdataEnd);
                }
            }
            // If there was no cdata end marker, we have wasted our time.  Duplicate code from above.
            else
            {
                return filterMultipleConsecutive(
                        Substituter.replace(TAB, SPACE,
                            Substituter.replace(LINE_SEPARATOR, SPACE, filterXML(input))), SPACE).trim();
            }
        }
    }


    /**
     *  <p>
     *  Breaks a message into multiple lines at
     *  <i>any</i> tag, except if the tag comes at
     *  the beginning of the message.
     *  </p>
     *  <p>
     *  Generally used to format output nicely for
     *  a console.
     *  </p>
     *
     *  @param input    the string to break
     *
     *  @return one line per array item
     */
    public static String[] breakLines(String input)
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
        int tagStart = input.indexOf(TAG_START);
        if (tagStart == -1)
        {
            return new String[]{input};
        }
        else
        {
            // tagEnd indexes the end of a tag.
            int tagEnd = 0;

            // lastEnd indexes the previous end of a tag.
            int lastEnd = 0;

            // inputLength avoids recalculating input.length().
            int inputLength = input.length();

            // Results will be delivered by calling toArray() on this Vector.
            Vector result = new Vector();

            // Break lines at tags.
            while((tagStart > -1) && (tagEnd > -1))
            {
                // Get the end of a tag.
                tagEnd = input.indexOf(TAG_END, lastEnd);

                // Add the input until the tag as a line, as long as the tag is not the beginning.
                if (tagStart > 0)
                {
                    result.addElement(input.substring(lastEnd, tagStart).trim());
                }

                // Set last end to the character following the end of the tag.
                lastEnd = tagEnd + 1;

                // Look for another tag.
                tagStart = input.indexOf(TAG_START, lastEnd);
            }
            // All tags are exhausted; if there is still something left in the input,
            if ((lastEnd < inputLength) && (lastEnd > 0))
            {
                // Add the remainder as the final line.
                result.addElement(input.substring(lastEnd).trim());
            }
            return (String[])result.toArray(new String[]{});
        }
    }


    /**
     *  Removes all tags from a string (retains character content
     *  of tags, however).
     *
     *  @param input    the string from which to remove markup
     *
     *  @return the input without tags
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
        int tagStart = input.indexOf(TAG_START);
        if (tagStart == -1)
        {
            return input;
        }
        else
        {
            // tagEnd indexes the end of a tag.
            int tagEnd = 0;

            // lastEnd indexes the previous end of a tag.
            int lastEnd = 0;

            // inputLength avoids recalculating input.length().
            int inputLength = input.length();

            // Results will be built up in this buffer.
            StringBuffer result = new StringBuffer();

            // Break lines at tags.
            while((tagStart > -1) && (tagEnd > -1))
            {
                // Get the end of a tag.
                tagEnd = input.indexOf(TAG_END, lastEnd);

                // Add the input until the tag as a line, as long as the tag is not the beginning.
                if (tagStart > 0)
                {
                    result.append(input.substring(lastEnd, tagStart));
                }

                // Set last end to the character following the end of the tag.
                lastEnd = tagEnd + 1;

                // Look for another tag.
                tagStart = input.indexOf(TAG_START, lastEnd);
            }
            // All tags are exhausted; if there is still something left in the input,
            if ((lastEnd < inputLength) && (lastEnd > 0))
            {
                // Add the remainder as the final line.
                result.append(input.substring(lastEnd));
            }
            return result.toString();
        }
    }


    /**
     *  <p>
     *  Replaces the following &quot;escape&quot; strings
     *  with their character equivalents:
     *  </p>
     *  <code>
     *  <ul>
     *      <li>&amp;amp; with &amp;</li>
     *      <li>&amp;lt; with &lt;</li>
     *      <li>&amp;gt; with &gt;</li>
     *      <li>&amp;apos; with &apos;</li>
     *      <li>&amp;quot; with &quot;</li>
     *  </ul>
     *  </code>
     *
     *  @param input    the string on which to perform the replacement
     *
     *  @return the string with entities replaced
     */
    public static String unescapeXMLChars(String input)
    {
        /*
            This one-time-only initialization of xmlEscaped
            lets us avoid creating needless strings by
            repeated application of the simpler replace()
            method.
        */
        if (xmlEscapes == null)
        {
            xmlEscapes = new HashMap(XML_ESCAPES.length);
            for (int index = XML_ESCAPES.length; --index >= 0; )
            {
                xmlEscapes.put(XML_ESCAPES[index][0], XML_ESCAPES[index][1]);
            }
        }
        return Substituter.applySubstitutions(xmlEscapes, input);
    }


    /**
     *  <p>
     *  Replaces the following characters with their
     *  &quot;escaped&quot; equivalents:
     *  </p>
     *  <code>
     *  <ul>
     *      <li>&amp; with &amp;amp;</li>
     *      <li>&lt; with &amp;lt;</li>
     *      <li>&gt; with &amp;gt;</li>
     *      <li>&apos; with &amp;apos;</li>
     *      <li>&quot; with &amp;quot;</li>
     *  </ul>
     *  </code>
     *
     *  @param input    the string on which to perform the replacement
     *
     *  @return the string with entities replaced
     */
    public static String escapeXMLChars(String input)
    {
        /*
            This one-time-only initialization of xmlProhibited
            lets us avoid creating needless strings by
            repeated application of the simpler replace()
            method.
        */
        if (xmlProhibited == null)
        {
            xmlProhibited = new HashMap(XML_ESCAPES.length);
            for (int index = XML_ESCAPES.length; --index >= 0; )
            {
                xmlProhibited.put(XML_ESCAPES[index][1], XML_ESCAPES[index][0]);
            }
        }
        return Substituter.applySubstitutions(xmlProhibited, input);
    }


    /**
     *  Filters out multiple consecutive instances of a given string.
     *
     *  @param input        the string to filter
     *  @param filterChar   the string to filter out
     *
     *  @return the string without multiple consecutive instances of <code>filterChar</code>
     */
    private static String filterMultipleConsecutive(String input, String filter)
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

        // Index the input length.
        int inputLength = input.length();

        // Index the filter length.
        int filterLength = filter.length();

        // Calculate maximum index.
        int maxIndex = inputLength - filterLength + 1;

        // The result will be constructed in this StringBuffer.
        StringBuffer result = new StringBuffer(inputLength);

        if (maxIndex > -1)
        {
            // Previous strings will be compared using this String.
            String previous = input.substring(0, filterLength);

            // Append the first character no matter what.
            result.append(previous);

            for (int index = filterLength; index < maxIndex; index++)
            {
                String current = input.substring(index, index + filterLength);
                if (!(current.equals(previous) && current.equals(filter)))
                {
                    result.append(current);
                }
                previous = current;
            }
            return result.toString();
        }
        else
        {
            return input;
        }
    }


    /**
     *  Removes all instances of a given string from an input.
     *
     *  @param input    the string to filter
     *  @param filter   the string to remove
     *
     *  @return the input with all instances of <code>filter</code> removed
     *
     *  @throws StringIndexOutOfBoundsException if there is malformed text in the input.
     */
    private static String removeAll(String input, String filter)
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

        // If the filter is null, return the input as-is.
        if (filter == null)
        {
            return input;
        }

        // If the filter is an empty string, return the input as-is.
        if (filter.equals(EMPTY_STRING))
        {
            return input;
        }

        // Index the input length.
        int inputLength = input.length();

        // filterAddend is the amount to add to the index when comparing a substring of the input to the filter.
        int filterAddend = filter.length() - 1;

        // If the filter does not exist in the input, return the input as-is.
        if (input.indexOf(filter) == -1)
        {
            return input;
        }

        // Never look for the filter at an offset greater than inputLength - filterAddend.
        int maxIndex = inputLength - filterAddend;

        if (maxIndex > -1)
        {
            // The result will be constructed in this StringBuffer.
            StringBuffer result = new StringBuffer(inputLength);

            // Look through the string character by character.
            for (int index = 0; index <= maxIndex; index++)
            {
                // If the input at offset index doesn't start with the filter,
                if (!input.startsWith(filter, index))
                {
                    // append the present character.
                    result.append(input.substring(index, index + 1));
                }
                // Otherwise, we have found an instance of the filter,
                else
                {
                    // and so do not append anything, but increment index to skip ahead of the filter.
                    index += filterAddend;
                }
            }
            return result.toString();
        }
        else
        {
            return input;
        }
    }


    /**
     *  Removes all characters that are not considered
     *  <a href="http://www.w3.org/TR/2000/REC-xml-20001006#charsets">XML characters</a>
     *  from the input.
     *
     *  @param input    the input to filter
     *
     *  @return the input with all non-XML characters removed
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
        for (char aChar = iterator.first();
             aChar != StringCharacterIterator.DONE;
             aChar = iterator.next())
        {
            // Determine if this is a valid XML Character.
            if ( (aChar == '\u0009') ||
                 (aChar == '\n') ||
                 (aChar == '\r') ||
                 (('\u0020' <= aChar) && (aChar <= '\uD7FF')) ||
                 (('\uE000' <= aChar) && (aChar <= '\uFFFD'))    )
            {
                result.append(aChar);
            }
        }
        if (result.length() > input.length())
        {
            return result.toString();
        }
        else
        {
            return input;
        }
    }


    /**
     *  <p>
     *  Converts XML Unicode character entities into their
     *  character equivalents within a given string.
     *  </p>
     *  <p>
     *  This will handle entities in the form <code>&amp;#<i>xxxx</i>;</code>
     *  (decimal character code, where <i>xxxx</i> is a valid character code),
     *  or <code>&amp;#x<i>xxxx</i></code> (hexadecimal character code, where
     *  <i>xxxx</i> is a valid character code).
     *  </p>
     *
     *  @param input    the string to process
     *
     *  @return the input with all XML Unicode character entity codes replaced
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
                        // Check that the semicolon is not so far away that it is likely not part of this entity.
                        if (semicolon < pointer + 7)
                        {
                            try
                            {
                                // Integer.decode from pointer + 2 includes the "x".
                                result.append((char)Integer.decode(input.substring(pointer + 2, semicolon)).intValue());
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
                        // Check that the semicolon is not so far away that it is likely not part of this entity.
                        int semicolon = input.indexOf(';', pointer + 2);
                        if (semicolon < pointer + 7)
                        {
                            try
                            {
                                // Integer.parseInt from pointer + 2 excludes the "&#".
                                result.append((char)Integer.parseInt(input.substring(pointer + 2, semicolon)));
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
     *  Returns the declared encoding string from the XML resource
     *  supposedly connected to a given InputStream,
     *  or the system default if none is found.
     *
     *  @param in   the input stream
     *
     *  @result the declared encoding string
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
            String encoding = getAttributeValue(ENCODING, firstLine.substring(XML_PI_START_LENGTH));
            if (!encoding.trim().equals(EMPTY_STRING))
            {
                return encoding;
            }
        }
        // If encoding was unspecified, return the system encoding.
        return SYSTEM_ENCODING;
    }


    /**
     *  <p>
     *  Gets value of an attribute by name.
     *  </p>
     *  <p>
     *  This method looks in the supplied argument string, and parses
     *  it looking for the structure &quot;<i>attributeName</i>=&quot;,
     *  returning whatever is after the &quot;=&quot; until the next
     *  blank or the end of the string. This is mostly useful for
     *  extracting attributes from AIML tags.
     *  </p>
     *
     *  @param attribute    the attribute whose value is wanted
     *  @param args         the argument string
     *
     *  @return the value of the argument (or empty string if nothing)
     */
    public static String getAttributeValue(String attributeName, String args)
    {
        int index;
        String argvalue = EMPTY_STRING;
        String argpattern = new String(attributeName + EQUAL_QUOTE);
        index = args.indexOf(argpattern);
        if (index >= 0)
        {
            if (index + argpattern.length() >= args.length())
            {
                args = EMPTY_STRING;
            }
            else
            {
                args = args.substring(index + argpattern.length(), args.length());
            }
            index = args.indexOf(QUOTE_MARK);
            if (index >= 0)
            {
                argvalue = args.substring(0, index);
            }
        }
        return unescapeXMLChars(argvalue);
    }


    /**
     *  Formats AIML from a single long string into a nicely indented multi-line string.
     *
     *  @param content  the AIML content to format
     */
    public static String formatAIML(String content)
    {
        XMLParser parser = new XMLParser();
        LinkedList trie = parser.load(content);

        return formatAIML(trie, 0, true);
    }


    /**
     *  Formats AIML from a trie into a nicely indented multi-line string.
     *
     *  @param trie     the trie containing the AIML
     *  @param level    the level (for indenting)
     *  @param atStart  whether the whole XML string is at its beginning
     */
    public static String formatAIML(LinkedList trie, int level, boolean atStart)
    {
        ListIterator trieIterator = trie.listIterator();

        StringBuffer result = new StringBuffer();

        while (trieIterator.hasNext())
        {
            XMLNode node = (XMLNode)trieIterator.next();
            
            switch (node.XMLType)
            {
                // Append a start tag.
                case XMLNode.TAG :
                    if (!atStart)
                    {
                        result.append(LINE_SEPARATOR);
                    }
                    else
                    {
                        atStart = false;
                    }
                    result.append(tab(level) + TAG_START + node.XMLData + node.XMLAttr + TAG_END);
                    String contents = formatAIML(node.XMLChild, level + 1, true);
                    if (contents.trim().length() > 0)
                    {
                        result.append(LINE_SEPARATOR + contents);
                    }
                    result.append(LINE_SEPARATOR + tab(level) + TAG_START + '/' + node.XMLData + node.XMLAttr + TAG_END);
                    break;
                
                // Append an atomic tag.
                case XMLNode.EMPTY :
                    if (atStart)
                    {
                        result.append(tab(level));
                        atStart = false;
                    }
                    result.append(TAG_START + node.XMLData + node.XMLAttr + '/' + TAG_END);
                    // Treat HTML line breaks specially.
                    if (node.XMLData.trim().equals("br"))
                    {
                        result.append(LINE_SEPARATOR + tab(level));
                    }
                    break;
                
                // Append data.
                case XMLNode.DATA :
                    if (atStart)
                    {
                        if (node.XMLData.trim().length() > 0)
                        {
                            result.append(tab(level) + node.XMLData);
                            atStart = false;
                            break;
                        }
                    }
                    result.append(node.XMLData);
                    break;

                // Append CDATA.
                case XMLNode.CDATA :
                    if (atStart)
                    {
                        result.append(tab(level));
                        atStart = false;
                    }
                    result.append(LINE_SEPARATOR + tab(level) +
                        TAG_START + XMLParser.CDATA_START + node.XMLData + XMLParser.CDATA_END);
                    break;

                // Append comments.
                case XMLNode.COMMENT :
                    if (atStart)
                    {
                        result.append(tab(level));
                        atStart = false;
                    }
                    result.append(LINE_SEPARATOR + tab(level) +
                        TAG_START + XMLParser.COMMENT_START + node.XMLData + XMLParser.COMMENT_END);
                    break;
            }
        }
        return result.toString();
    }


    /**
     *  Returns a tab of the specified length.
     *
     *  @param length   the length of the tab
     */
    public static String tab(int level)
    {
        char[] result = new char[level];
        for (int index = level; --index >= 0; )
        {
            result[index] = '\t';
        }
        return new String(result);
    }


    /**
     *  Splits an input into words, breaking at spaces.
     *  This method is obviously limited in that it is not
     *  aware of other word boundaries.
     *
     *  @param input    the input to split
     *
     *  @return the input split into sentences
     */
    public static ArrayList wordSplit(String input)
    {
        ArrayList result = new ArrayList();

        int inputLength = input.length();
        if (inputLength == 0)
        {
            result.add(EMPTY_STRING);
            return result;
        }

        int wordStart = 0;
        
        StringCharacterIterator iterator = new StringCharacterIterator(input);

        for (char aChar = iterator.first();
             aChar != StringCharacterIterator.DONE;
             aChar = iterator.next())
        {
             if (aChar == ' ')
             {
                 int index = iterator.getIndex();
                 result.add(input.substring(wordStart, index));
                 wordStart = index + 1;
             }
        }
        if (wordStart < input.length())
        {
            result.add(input.substring(wordStart));
        }
        return result;
    }

    
    /**
     *  <p>
     *  Scans the classpath for classes that implement a given interface.
     *  </p>
     *  <p>
     *  Note that this does not make sense for all class loaders.  In cases
     *  where it doesn't make sense, the return value will be null.
     *  </p>
     *  <p>
     *  This can also be a very slow method if the classpath is long.
     *  </p>
     *
     *  @param interfaceName    the fully-qualified name of the interface whose implementations are wanted
     *  @param showFound        whether to output a devinfo message when implementations are found
     *
     *  @return classes that implement this interface
     */
    public static Class[] getImplementorsOf(String interfaceName, boolean showFound)
    {
        // Try to get the interface class.
        Class theInterface = null;
        try
        {
            theInterface = Class.forName(interfaceName);
        }
        catch (ClassNotFoundException e)
        {
            throw new DeveloperError("Could not find class \"" + interfaceName + "\".");
        }

        StringTokenizer tokenizer = new StringTokenizer(System.getProperty("java.class.path", (EMPTY_STRING)),
                                                        System.getProperty("path.separator", (EMPTY_STRING)));

        if (tokenizer.countTokens() == 0)
        {
            return null;
        }

        Vector results = new Vector();

        while(tokenizer.hasMoreTokens())
        {
            String directory = tokenizer.nextToken();
            Vector list = new Vector();

            if (directory.endsWith(".jar") || directory.endsWith(".zip"))
            {
                Enumeration entries = null;
                if (directory.endsWith(".jar"))
                {
                    JarFile jar = null;
                    try
                    {
                        jar = new JarFile(directory);
                    }
                    catch (IOException e)
                    {
                        Log.userinfo("Classpath contains invalid entry: \"" + directory + "\".", Log.ERROR);
                        continue;
                    }
                    entries = jar.entries();
                }
                else
                {
                    ZipFile zip = null;
                    try
                    {
                        zip = new ZipFile(directory);
                    }
                    catch (IOException e)
                    {
                        Log.userinfo("Classpath contains invalid entry: \"" + directory + "\".", Log.ERROR);
                        continue;
                    }
                    entries = zip.entries();
                }
                if (entries != null)
                {
                    while (entries.hasMoreElements())
                    {
                        list.addElement(((ZipEntry)entries.nextElement()).getName());
                    }
                }
            }
            else
            {
                String[] files;
                try
                {
                    files = new File(directory).list();
                }
                catch (SecurityException e)
                {
                    String error = "Security exception trying to open \"" + directory + "\".";
                    Log.log(error, Log.ERROR);
                    Log.devinfo(error, Log.STARTUP);
                    continue;
                }
                for (int index = files.length; --index >= 0; )
                {
                    list.addElement(files[index]);
                }
            }

            if (list.size() > 0)
            {
                Iterator iterator = list.iterator();
                while (iterator.hasNext())
                {
                    Class candidate = null;
                    String className = null;
                    try
                    {
                        className = ((String)iterator.next()).replace('/', '.');
                    }
                    catch (ClassCastException e)
                    {
                        continue;
                    }
                    int endName = className.lastIndexOf(".class");
                    if (endName > -1)
                    {
                        className = className.substring(0, endName);
                        if (!className.equals(interfaceName))
                        {
                            try
                            {
                                candidate = Class.forName(className);
                            }
                            catch (ClassNotFoundException e)
                            {
                                continue;
                            }
                            catch (NoClassDefFoundError e)
                            {
                                continue;
                            }
                            catch (ExceptionInInitializerError e)
                            {
                                continue;
                            }
                            if (theInterface.isAssignableFrom(candidate))
                            {
                                if (showFound)
                                {
                                    Log.devinfo("Found \"" + className + "\".", Log.STARTUP);
                                }
                                results.add(candidate);
                            }
                        }
                    }
                }
            }
        }
        Class[] toReturn = {};
        toReturn = (Class[])results.toArray(toReturn);
        return toReturn;
    }


    /**
     *  <p>
     *  Expands a localized file name that may contain wildcards
     *  to an array of file names without wildcards.
     *  All file separators in the file name must preceed any wildcard.
     *  </p>
     *  <p>
     *  Adapted, with gratitude, from the
     *  <a href="http://sourceforge.net/projects/jmk/">JMK</a>
     *  project.  (Under the GNU LGPL)
     *  </p>
     *
     *  @param path             localized file name that may contain wildcards
     *  @param workingDirectory the path to which relative paths should be considered relative
     *
     *  @return array of file names without wildcards
     *
     *  @author John D. Ramsdell
     *  @see <a href="http://sourceforge.net/projects/jmk/">JMK</a>
     *
     *  @throws FileNotFoundException if wild card is misused
     */
    public static String[] glob(String path, String workingDirectory) throws FileNotFoundException
    {
        int wildCardIndex = path.indexOf('*');
        if (wildCardIndex < 0)
        {
            return new String[] { path };
        }
        else
        {
            int separatorIndex = path.lastIndexOf(File.separatorChar);
            // In case someone used a file separator char that doesn't belong to this system....
            if (separatorIndex == -1)
            {
                separatorIndex = path.lastIndexOf('\\');
                if (separatorIndex == -1)
                {
                    separatorIndex = path.lastIndexOf('/');
                    if (separatorIndex == -1)
                    {
                        // This is really going the extra mile....
                        separatorIndex = path.lastIndexOf(':');
                    }
                }
            }
            if (separatorIndex > wildCardIndex)
            {
                throw new FileNotFoundException("Cannot expand " + path);
            }
            String pattern;
            String dirName;
            File dir;
            if (separatorIndex >= 0)
            {
                pattern = path.substring(separatorIndex + 1);
                dirName = path.substring(0, separatorIndex + 1);
                dir = new File(dirName);
                if (!dir.isDirectory())
                {
                    dir = new File(workingDirectory + File.separator + dirName);
                }
            }
            else
            {
                pattern = path;
                dirName = workingDirectory;
                dir = new File(dirName);
            }
            if (!dir.isDirectory())
            {
                throw new UserError("\"" + dir.getPath() + "\" is not a valid directory path!");
            }
            String[] list = dir.list(new WildCardFilter(pattern, '*'));
            if (list == null)
            {
                return new String[0];
            }
            for (int i = list.length; --i >= 0; )
            {
                list[i] = dirName + File.separator + list[i];
            }
            return list;
        }
    }


    /**
     *  Checks whether a file given by a path exists,
     *  and if not, creates it, along with any necessary
     *  subdirectories.
     *
     *  @param path         denoting the file to create
     *  @param description  describes what the file is for, for trace
     *                      messages. Should fit into a sentence like,
     *                      &quot;created new <i>description</i>&quot;.
     *                      May be null (which will result in less informative messages).
     */
    public static void checkOrCreate(String path, String description)
    {
        File file = new File(path);
        if (!file.exists())
        {
            if (description == null)
            {
                description = "file";
            }

            try
            {
                file.createNewFile();
            }
            catch (IOException e0)
            {
                // This may mean that some necessary directories don't exist.
                File directory = file.getParentFile();
                if (directory != null)
                {
                    if (directory.mkdirs())
                    {
                        try
                        {
                            file.createNewFile();
                        }
                        catch (IOException e1)
                        {
                            throw new UserError("Could not create " + description + " \"" + file.getAbsolutePath() + "\".");
                        }
                    }
                    else
                    {
                        throw new UserError("Could not create directory \"" + directory.getAbsolutePath() + "\".");
                    }
                }
                else
                {
                    throw new UserError("Could not create " + description + " directory.");
                }
            }
            Trace.devinfo("Created new " + description + " \"" + path + "\".");
        }
    }


    /**
     *  Returns the entire contents of a file as a String.
     *
     *  @param path     the path to the file (local file or URL)
     *
     *  @return the entire contents of a file as a String
     */
    public static String getFileContents(String path)
    {
        BufferedReader buffReader = null;

        // Guess if this is a URL.
        if (path.indexOf("://") != -1)
        {
            // Try to create this as a URL.
            URL url = null;

            try
            {
                url = new URL(path);
            }
            catch (MalformedURLException e)
            {
                Log.userinfo("Malformed URL: \"" + path + "\"", Log.ERROR);
            }

            try
            {
                String encoding = Toolkit.getDeclaredXMLEncoding(url.openStream());
                buffReader = new BufferedReader(new InputStreamReader(url.openStream(), encoding));
            }
            catch (IOException e)
            {
                Log.userinfo("I/O error trying to read \"" + path + "\"", Log.ERROR);
            }
        }
        // Handle paths which are apparently files.
        else
        {
            File toRead = new File(path);

            if (toRead.isAbsolute())
            {
                WORKING_DIRECTORY = toRead.getParent();
            }

            if (toRead.exists() && !toRead.isDirectory())
            {
                try
                {
                    String encoding = Toolkit.getDeclaredXMLEncoding(new FileInputStream(path));
                    buffReader = new BufferedReader(
                                    new InputStreamReader(
                                        new FileInputStream(path), encoding));
                }
                catch (IOException e)
                {
                    Log.userinfo("I/O error trying to read \"" + path + "\"", Log.ERROR);
                    return null;
                }
            }
            else
            {
                if (!toRead.exists())
                {
                    throw new UserError("\"" + path + "\" does not exist!");
                }
                if (toRead.isDirectory())
                {
                    throw new UserError("\"" + path + "\" is a directory!");
                }
            }
        }
        StringBuffer result = new StringBuffer();
        String line;
        try
        {
            while ( (line = buffReader.readLine()) != null)
            {
                result.append(line);
            }
            buffReader.close();
        }
        catch (IOException e)
        {
            Log.userinfo("I/O error trying to read \"" + path + "\"", Log.ERROR);
            return null;
        }
        return result.toString();
    }
}

