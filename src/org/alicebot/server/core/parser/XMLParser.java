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
    - made all imports explicit
*/

/*
    4.1.4 [00] - December 2001, Noel Bush
    - removed use of third-party "LinkedList" and "LinkedListItr"
*/

package org.alicebot.server.core.parser;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.StringTokenizer;

import org.alicebot.server.core.logging.Log;


/**
 *  <p>
 *  A simple XML parser.
 *  </p>
 *  <p>
 *  Author Pedro Colla describes it as a
 *  &quot;poor man's XML non-validating parser&quot;.
 *  </p>
 *  @version 4.1.4
 *  @author  Thomas Ringate/Pedro Colla
 */
public class XMLParser
{
    // Convenience constants.

    /** An XML marker start (&trie;). */
    public static final String MARKER_START  = "<";

    /** The start of an XML comment (!--). */
    public static final String COMMENT_START = "!--";

    /** The end of an XML comment (!--). */
    public static final String COMMENT_END   = "-->";

    /** A question mark (starts a processing instruction). */
    public static final String PI_START      = "?";

    /** End of a processing instruction. */
    private static final String PI_END        = "?";

    /** The beginning of a CDATA block. */
    public static final String CDATA_START   = "![CDATA[";

    /** A end of a CDATA block. */
    public static final String CDATA_END     = "]]>";

    /** A slash. */
    public static final String SLASH         = "/";

    /** An XML marker end (&gt;). */
    public static final String MARKER_END    = ">";

    /** A space. */
    public static final String SPACE         = " ";

    /** An empty string. */
    public static final String EMPTY_STRING  = "";


    /**
     *  Parses an input string containing an XML structure
     *  into its XML elements.
     *
     *  @param input    the input string
     *  @param XMLList  the list to which to add the parsed XML
     *
     *  @return a linear linked list of the parsed XML
     */
    public LinkedList XMLRead(String input, LinkedList XMLList)
    {
        /*
            XML decoding Finite State Machine
            Internal States
        */
        final int    Q_TEXT       = 0;
        final int    Q_TAG        = 1;
        final int    Q_ENDTAG     = 2;
        final int    Q_CDATA      = 3;
        final int    Q_COMMENT    = 4;
        final int    Q_XML        = 5;
        final int    Q_PI         = 6;

        // Linked List local iterator
        ListIterator xmlIterator;

        // Buffer and processing variables

        XMLNode node;

        StringBuffer text = new StringBuffer();
        String tagArguments;
        String tag;
        String token;
        String tagData;

        int tagType      = XMLNode.TAG;

        int     state    = Q_TEXT;
        int     index    = 0;
        int     spaceIndex        = 0;
        int     start    = 0;
        int     end      = 0;

        // Initialize the Linked List iterator.
        xmlIterator = XMLList.listIterator(0);

        // Process the input string.
        while (index < input.length())
        {
            token = String.valueOf(input.charAt(index));

            switch(state)
            {
                // Initial state: parse text.
                case Q_TEXT : 
                    // See if an XML construct begins with "<".
                    if (token.equals(MARKER_START))
                    {
                        // Change state to parsing XML.
                        state = Q_XML;

                        // If any text is already parsed, process it as DATA.
                        if (text.length() > 0)
                        {
                            node = new XMLNode();
                            node.XMLType = XMLNode.DATA;
                            node.XMLData = text.toString();
                            node.XMLAttr = EMPTY_STRING;

                            xmlIterator.add(node);
                        }

                        // Initialize parsing buffers.
                        text = new StringBuffer();
                    }
                    // Otherwise, just store the text.
                    else
                    {
                        text.append(token);
                    }
                    break;

                // XML state: XML just detected
                case Q_XML :
                    // See if this is a comment.
                    if (input.indexOf(COMMENT_START, index) - index == 0)
                    {
                        state = Q_COMMENT;
                        break;
                    }
                    // See if this is a processing instruction.
                    if (input.indexOf(PI_START, index) - index == 0)
                    {
                        state = Q_PI;
                        break;
                    }
                    // See if this is a CDATA block start.
                    if (input.indexOf(CDATA_START, index) - index == 0)
                    {
                        state = Q_CDATA;
                        break;
                    }
                    // See if this is an end tag.
                    if (input.indexOf(SLASH, index) - index == 0)
                    {
                        state = Q_ENDTAG;
                        break;
                    }
                    // Otherwise, just a normal tag.
                    state = Q_TAG;

                    // State: normal/empty tag
                    case Q_TAG:
                        // Compute start and end of tag and extract it.
                        start  = index;
                        end    = input.indexOf(MARKER_END, start);
                        tag    = input.substring(start, end);
                        tagArguments = EMPTY_STRING;

                        // If the last character is a slash, this is an "empty tag", otherwise not.
                        int tagLastIndex = tag.length() -1;
                        if (tag.charAt(tagLastIndex) == '/')
                        {
                            tagType = XMLNode.EMPTY;
                            tag = tag.substring(0, tagLastIndex);
                        }
                        else
                        {
                            tagType = XMLNode.TAG;
                        }
                        // Everything before the first space is the tag.
                        spaceIndex = tag.indexOf(SPACE);
                        if (spaceIndex > 0)
                        {
                            tagData = tag.substring(0, spaceIndex);
                            tagArguments = SPACE + tag.substring(spaceIndex + 1, tag.length());
                        }
                        else
                        {
                            tagData = tag;
                            tagArguments = EMPTY_STRING;
                        }
                        /*
                            Mark the input buffer as processed up to the closing
                            ">" and reset the state to Q_TEXT.
                        */
                        index = end;
                        state = Q_TEXT;
                        text = new StringBuffer();

                        // If the tag is not empty, process it as EMPTY or TAG.
                        if (tagData.length() > 0)
                        {
                            node = new XMLNode();
                            node.XMLType = tagType;
                            node.XMLData = tagData;
                            node.XMLAttr = tagArguments;
                            xmlIterator.add(node);

                            tag = EMPTY_STRING;
                        }
                        break;

                    // State: end tag
                    case Q_ENDTAG :
                        // Extract the tag.
                        start = index;
                        end   = input.indexOf(MARKER_END, start);
                        tag   = input.substring(start, end);

                        // Everything before the first space is the tag.
                        spaceIndex = tag.indexOf(SPACE);
                        if (spaceIndex > 0)
                        {
                            tagData = tag.substring(0, spaceIndex);
                            tagArguments = SPACE + tag.substring(spaceIndex + 1, tag.length());
                        }
                        else
                        {
                            tagData = tag;
                        }
                        // Flag it as ENDTAG.
                        tagType = XMLNode.ENDTAG;
                        tagArguments = EMPTY_STRING;

                        if (tagData.length() > 0)
                        {
                            node = new XMLNode();
                            node.XMLType = tagType;
                            node.XMLData = tagData;
                            node.XMLAttr = tagArguments;
                            xmlIterator.add(node);
                        }

                        /*
                            Mark the input buffer as processed up to the closing
                            ">" and reset the state to Q_TEXT.
                        */
                        index = end;
                        state = Q_TEXT;
                        text = new StringBuffer();

                        break;

                    // State: CDATA
                    case Q_CDATA  :
                        // Skip the CDATA leading header
                        start = index + 7;

                        // Extract the block of enclosed data.
                        end   = input.indexOf(CDATA_END, start);
                        tag   = input.substring(start, end);

                        tagType = XMLNode.CDATA;
                        tagData = tag;
                        tagArguments = EMPTY_STRING;

                        // If not empty, process as CDATA.
                        if (tagData.length() > 0)
                        {
                            node = new XMLNode();
                            node.XMLType = tagType;
                            node.XMLData = tagData;
                            node.XMLAttr = tagArguments;
                            xmlIterator.add(node);
                        }

                        // Mark the input buffer to continue after the tag; reset to text mode.
                        index     = end + 2;
                        state     = Q_TEXT;

                        break;

                    // State: comment
                    case Q_COMMENT:
                        // Skip the comment leading header.
                        start = index + 2;

                        // Find end of comment and extract contents as a block.
                        end   = input.indexOf(COMMENT_END, start);
                        tag   = input.substring(start, end);

                        tagType = XMLNode.COMMENT;
                        tagData = tag;
                        tagArguments = EMPTY_STRING;

                        if (tagData.length() > 0)
                        {
                            node = new XMLNode();
                            node.XMLType = tagType;
                            node.XMLData = tagData;
                            node.XMLAttr = tagArguments;
                            xmlIterator.add(node);
                        }

                        // Mark the input buffer to continue after the tag; reset to text mode.
                        index     = end + 2;
                        state     = Q_TEXT;

                        break;

                    // State: XML processing instruction.
                    case Q_PI:
                        start = index ;

                        // Extract the processing instruction
                        end   = input.indexOf(PI_END, start);
                        tag   = input.substring(start, end);

                        // Mark the input buffer to continue after the tag; reset to text mode.
                        index     = end + 1;
                        state     = Q_TEXT;

                        break;

                    // Invalid construct.
                    default:
                        Log.userinfo("Invalid tag format.", Log.ERROR);
                        return null;
                }

            // Advance to next character in the input buffer
            index++;
        }

        /*
            If the buffer ended while processing a text chunk,
            this chunk will be left unprocessed in the buffer
            and should be added as DATA.
        */
        if ((text.length() > 0) && (state == Q_TEXT))
        {
            node = new XMLNode();
            node.XMLType = XMLNode.DATA;
            node.XMLData = text.toString();
            node.XMLAttr = EMPTY_STRING;
            xmlIterator.add(node);
        }
        return XMLList;
    }


    /**
     *  Converts a linear linked list with parsed XML
     *  into a trie.
     *
     *  @param xmlIterator  an iterator on the XML linear linked list
     *  @param xmlList      the parsed XML as a linear linked list
     *  @param trie         the trie to modify and return
     *
     *  @return a trie representation of the parsed XML
     */
    public LinkedList scan(ListIterator xmlIterator, LinkedList xmlList, LinkedList trie)
    {
        /*
            A tokenized representation of the XML stream is held in
            one linear single Linked List (xmlIterator), which is scanned and
            transformed into a trie (trie & descendants) using recursion.
        */
        XMLNode node;
        XMLNode child;

        while (xmlIterator.hasNext())
        {
            // Retrieve a node from the Linked List.
            node = (XMLNode)xmlIterator.next();

            // Only process valid references.
            if (node != null)
            {
                switch(node.XMLType)
                {
                    case XMLNode.TAG :
                        // Create a node in the trie and recurse for its children.
                        child = new XMLNode();
                        child.XMLType = node.XMLType;
                        child.XMLData = node.XMLData;
                        child.XMLAttr = node.XMLAttr;
                        child.XMLChild = new LinkedList();
                        child.XMLChild = scan(xmlIterator, xmlList, child.XMLChild);
                        trie.add(child);
                        break;

                    case XMLNode.EMPTY :
                        // Create a new node; no need to recurse.
                        child = new XMLNode();
                        child.XMLType = node.XMLType;
                        child.XMLData = node.XMLData;
                        child.XMLAttr = node.XMLAttr;
                        trie.add(child);
                        break;

                    case XMLNode.DATA :
                    case XMLNode.CDATA :

                        // Handle both equally by adding a node with no attributes, type DATA.
                        child = new XMLNode();
                        child.XMLType = node.DATA;
                        child.XMLData = node.XMLData;
                        child.XMLAttr = EMPTY_STRING;
                        trie.add(child);
                        break;

                    case XMLNode.ENDTAG :

                        // This method is already recursing, or the XML is unbalanced.
                        return trie;

                    default :
                        break;
                }
            }
            else
            {
                Log.userinfo("XML element is null.", Log.ERROR);
            }
        }
        return trie;
    }


    /**
     *  Returns the full evaluation of a string containing
     *  an XML segment.
     *
     *  @param buffer   the XML segment
     *
     *  @return trie representation of the XML
     */
    public LinkedList load(String buffer)
    {
        LinkedList linearList = new LinkedList();
        LinkedList trie = new LinkedList();

        ListIterator listIterator;

        /*
            Process the XML tags in the buffer string and return
            one token (text or tag) per linked list node
            (linear representation).
        */
        linearList = XMLRead(buffer, linearList);

        if (linearList == null)
        {
            Log.userinfo("Invalid XML:", Log.ERROR);
            StringTokenizer lines = new StringTokenizer(buffer, System.getProperty("line.separator"));
            while (lines.hasMoreTokens())
            {
                Log.userinfo(lines.nextToken(), Log.ERROR);
            }
            return null;
        }

        // Get the linear list's iterator.
        listIterator = linearList.listIterator(0);

        /*
            Since the linear list is not so useful with a recursive
            parser, transform it into a trie that captures the
            parent-child relationships among elements in the list.
        */
        trie = scan(listIterator, linearList, trie);

        // Dispose of the linearList.
        linearList.clear();

        return trie;
    }
}
