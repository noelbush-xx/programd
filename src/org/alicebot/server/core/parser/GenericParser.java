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

package org.alicebot.server.core.parser;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.StringTokenizer;

import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.Trace;
import org.alicebot.server.core.processor.Processor;
import org.alicebot.server.core.processor.ProcessorException;
import org.alicebot.server.core.util.ClassRegistry;
import org.alicebot.server.core.util.DeveloperError;
import org.alicebot.server.core.util.Toolkit;


/**
 *  A generic parser.
 *
 *  @since 4.1.3
 */
abstract public class GenericParser
{
    /** Each subclass should set this. */
    protected ClassRegistry processorRegistry;

    // Convenience constants.
    
    /** An empty string, for convenience. */
    protected static final String EMPTY_STRING = "";

    /** The start of a tag marker for an element open tag. */
    protected static final String OPEN_MARKER_START = "<";

    /** The start of a tag marker for an element close tag. */
    protected static final String CLOSE_MARKER_START = "</";

    /** The end of a tag marker for a &quot;non-atomic&quot; tag. */
    protected static final String NONATOMIC_MARKER_END = ">";

    /** The end of a tag marker for an &quot;atomic&quot; tag. */
    protected static final String ATOMIC_MARKER_END = "/>";

    /** The word &quot;index&quot;, for convenience. */
    protected static final String INDEX = "index";

    /** A comma, for convenience. */
    protected static final String COMMA = ",";

    /** A colon, for convenience. */
    protected static final String COLON = ":";


    /**
     *  <p>
     *  Processes a given XML content string for a given identifier.
     *  </p>
     *  <p>
     *  This is the general access method for external classes.
     *  </p>
     *
     *  @param content      the XML content
     *
     *  @return the result of processing the XML content for the given <code>id</code>
     *
     *  @throws ProcessorException if the content cannot be processed
     */
    public String processResponse(String content) throws ProcessorException
    {
        // Create a new XML parser and load it with the template.
        XMLParser parser = new XMLParser();
        LinkedList list = parser.load(content);

        // If XML processing went badly, return error.
        if (list == null)
        {
            Log.userinfo("Invalid content:", Log.ERROR);
            StringTokenizer lines = new StringTokenizer(content, System.getProperty("line.separator"));
            while (lines.hasMoreTokens())
            {
                Log.userinfo(lines.nextToken(), Log.ERROR);
            }
            throw new ProcessorException("Invalid content (see log file).");
        }

        /*
            Evaluate the template starting from its first token.
            The evaluate method will recurse to explore the full trie.
        */
        String response = evaluate(0, list);

        // Perhaps unnecessary: a controlled cleanup
        list.clear();

        return response;
    }


    /**
     *  Processes a given XML node for a given identifier.
     *
     *  @param level    the current level in the XML trie
     *  @param tag      the tag being evaluated
     *
     *  @return the result of processing the tag
     *
     *  @throws ProcessorException if the content cannot be processed
     */
    public String processTag(int level, XMLNode tag) throws ProcessorException
    {
        // Is it a valid tag?
        if (tag == null)
        {
            return EMPTY_STRING;
        }

        // Search for the tag in the processor registry.
        Class processorClass = null;
        
        if (processorRegistry != null)
        {
            processorClass = (Class)processorRegistry.get(tag.XMLData);
        }
        else
        {
            throw new DeveloperError("processorRegistry has not been initialized!");
        }

        // Create a new instance of the processor.
        Processor processor = null;
        if (processorClass != null)
        {
            try
            {
                processor = (Processor)processorClass.newInstance();
            }
            catch (InstantiationException e)
            {
                throw new DeveloperError(e);
            }
            catch (IllegalAccessException e)
            {
                throw new DeveloperError(e);
            }
            catch (RuntimeException e)
            {
                throw new DeveloperError(e);
            }
        }
        else
        {
            throw new ProcessorException("Could not find a processor for \"" + tag.XMLData + "\"!");
        }

        // Return the results of processing the tag.
        if (processor != null)
        {
            return Toolkit.filterWhitespace(processor.process(level++, tag, this));
        }
        else
        {
            throw new DeveloperError("Corrupt processor set.");
        }
    }


    /**
     *  <p>
     *  Recursively evaluates an XML trie.;
     *  Both the <code>level</code> and the client <code>id</code>
     *  are carried through recursion.
     *  </p>
     *  <p>
     *  Depending on whether the engine is at load-time or run time, determines
     *  whether or not to process the tag. When the tag is processed, returns the
     *  result of processing it.
     *  </p>
     *
     *  @param level    the current level in the XML trie
     *  @param list     the XML trie to parse
     *
     *  @return the result of processing the tag
     */
    public String evaluate(int level, LinkedList list)
    {
        String response = EMPTY_STRING;
        ListIterator iterator;
        XMLNode node;

        // Verify there is something to work with.
        if (list == null)
        {
            return EMPTY_STRING;
        }

        // Point to the start of the XML trie to parse
        iterator = list.listIterator(0);

        // Navigate thru the entire level of it
        while (iterator.hasNext())
        {
            node = (XMLNode)iterator.next();
            if (node != null)
            {
                switch(node.XMLType)
                {
                    // Collect and process tags.
                    case XMLNode.TAG   :
                    case XMLNode.EMPTY :
                        try
                        {
                            response = response + processTag(level, node);
                        }
                        catch (ProcessorException e)
                        {
                            throw new DeveloperError(e.getMessage(), e);
                        }
                        break;

                    // Text chunks should just be added to the response.
                    case XMLNode.DATA  :
                    case XMLNode.CDATA :
                        response = response + node.XMLData;
                        break;

                    default :
                        break;
                }
            }
        }
        return response;
    }


    /**
     *  <p>
     *  Formats a tag from an XML node into &quot;pure&quot;
     *  ????. Mostly used when a tag cannot be evaluated and then
     *  literally included in the input.
     *  </p>
     *  <p>
     *  THIS WORDING NEEDS TO BE CLARIFIED.
     *  </p>
     *
     *  @param level    index of the current level in the XML trie (just passed through)
     *  @param tag      the tag to format
     *
     *  @return the formatted result
     */
    public String formatTag(int level, XMLNode tag)
    {
        // A given level always starts with an empty answer.
        String response = EMPTY_STRING;

        /*
            Format according with the XML element type. Handling of
            text has been added for generality since no text will
            ever be passed under the main usage of the method.
        */
        switch(tag.XMLType)
        {
            case XMLNode.TAG :
                // This is a XML tag, so it might have children. Format the head.
                response = response + OPEN_MARKER_START + tag.XMLData;

                // Include any attributes.
                if (!tag.XMLAttr.equals(EMPTY_STRING))
                {
                    response = response + tag.XMLAttr;
                }
                // Close the head.
                response = response + NONATOMIC_MARKER_END;

                // If there are any children, resolve them recursively.
                if (tag.XMLChild != null)
                {
                    response = response + evaluate(level++, tag.XMLChild);
                }

                // Format the response tag.
                response = response + CLOSE_MARKER_START + tag.XMLData + NONATOMIC_MARKER_END;
                break;

            case XMLNode.EMPTY :
                // Same as for XMLNode.TAG, but no recursion.
                response = response + OPEN_MARKER_START + tag.XMLData;
                if (!tag.XMLAttr.equals(EMPTY_STRING))
                {
                    response = response + tag.XMLAttr;
                }
                response = response + ATOMIC_MARKER_END;
                break;

            case XMLNode.DATA  :
            case XMLNode.CDATA :
                // Format text.
                response = response + tag.XMLData;
                break;

            default :
                break;

        }
        return response;
    }


    /**
     *  <p>
     *  Counts the number of nodes of a given type at a
     *  particular level of the XML trie.
     *  </p>
     *  <p>
     *  Used mostly in connection with the &lt;random/&gt; tag in order
     *  to see how many candidate listItem structures are beneath it and
     *  to set the upper limit on the random number roll.
     *  </p>
     *
     *  @param tagname  the name of the tag sought
     *  @param list     the XML trie
     *  @param allnodes if false, only count one of the desired type (just see if <i>any</i> are there)
     *
     *  @return the number of nodes of the given type at this level (or <code>1</code> if at least one node is present and <code>allnodes</code> is false
     */
    public int nodeCount(String tagname, LinkedList list, boolean allnodes)
    {
        ListIterator iterator;
        XMLNode node;
        int numbernodes = 0;

        // Verify there is something to work with.
        if (list == null)
        {
            return 0;
        }

        // Point to the start of the XML trie to parse.
        iterator = list.listIterator(0);

        // Navigate through this entire level.
        while (iterator.hasNext())
        {
            node = (XMLNode)iterator.next();
            if (node != null)
            {
                switch(node.XMLType)
                {
                    // Collect and process only tag elements and empty tags.
                    case XMLNode.TAG   :
                    case XMLNode.EMPTY :
                        // Only deal with the desired one.
                        if ( (!node.XMLData.equals(tagname)) &&
                             (allnodes == false) )
                        {
                            break;
                        }
                        numbernodes++;
                        break;

                    // Just ignore everything else.
                    default :
                        break;
                }
            }
        }
        return numbernodes;
    }


    /**
     *  <p>
     *  Retrieves the ordernode-th node of a given tag at a particular
     *  level of the XML trie.
     *  </p>
     *  <p>
     *  Typically used to find specific tags beneath a given tag being
     *  evaluated (e.g., an &lt;li/&gt; beneath a &lt;random/&gt;).
     *  </p>
     *
     *  @param tagname      the name of the tag sought
     *  @param list         the XML trie
     *  @param ordernode    index of the node we sought
     *
     *  @return the node sought
     */
    public XMLNode getNode(String tagname, LinkedList list, int ordernode)
    {
        ListIterator iterator;
        XMLNode node;

        // Verify there is something to work with.
        if (list == null)
        {
            return null;
        }

        // Point to the start of the XML trie to parse.
        iterator = list.listIterator(0);

        // Navigate thru the entire level.
        while (iterator.hasNext())
        {
            node = (XMLNode)iterator.next();
            if (node != null)
            {
                switch(node.XMLType)
                {
                    // Collect and process only tag elements and empty tags.
                    case XMLNode.TAG   :
                    case XMLNode.EMPTY :
                        // Only deal with the desired one.
                        if (!node.XMLData.equals(tagname))
                        {
                            break;
                        }
                        // We've found the one we're looking for when ordernode is zero (after decrementing).
                        if (--ordernode == 0)
                        {
                            return node;
                        }
                        // Otherwise, continue.
                        break;

                    // Just ignore everything else.
                    default :
                        break;
                }
            }
        }
        return null;
    }



    /**
     *  <p>
     *  Creates a &quot;mini-template&quot; with a given tag and
     *  an optional child tag, then evaluates it recursively.
     *  </p>
     *  <p>
     *  This method is used mostly to map certain tags as combinations
     *  of other tags (as in <a href="http://www.alicebot.org/TR/2001/WD-aiml/#section-short-cut-elements">short-cut elements</a>),
     *  as well as various &quot;AIML 0.9&quot; tags.
     *  </p>
     *
     *  @param level        the current level in the XML trie
     *  @param rootTag      the name of the root tag
     *  @param rootType     the type of the root tag
     *  @param rootAttr     an optional attribute on the root tag (use &quot;&quot; if no attribute)
     *  @param childTag     the name of the child tag
     *  @param childType    the type of the child tag
     *
     *  @return the result of processing this structure
     */
    public String shortcutTag(int level, String rootTag, int rootType,
                              String rootAttr, String childTag, int childType)
    {
        String response = EMPTY_STRING;

        // If the root tag is empty, we need not continue.
        if (rootTag.equals(EMPTY_STRING))
        {
            return EMPTY_STRING;
        }

        /*
            Create a new node for use in evaluation, with the
            content and attributes of the root tag.
        */
        XMLNode node = new XMLNode();
        node.XMLType = rootType;
        node.XMLData = rootTag;
        node.XMLAttr = rootAttr;

        /*
            Create an XML trie for this tag, so that we can use other
            methods of this class to process it normally (as if it
            came from the original AIML in this form.
        */
        LinkedList list = new LinkedList();

        // Prepare the list to receive inserts.
        ListIterator iterator = list.listIterator(0);

        /*
            Process child tags (if any). Clearly, the root tag cannot
            have an empty type, and the children must exist.
        */
        XMLNode nodeChild = new XMLNode();
        LinkedList childList = new LinkedList();
        if ( ( rootType  == node.TAG) &&
             (!childTag.equals(EMPTY_STRING))  &&
             ((childType  == node.EMPTY) || (childType == node.DATA)) )
        {
            /*
                Create an XML node for the child tag. Note that
                we assume that the child is an empty tag with
                no attributes.  This is reasonable for AIML 1.0.1, but
                might not always be.
            */
            switch(childType)
            {
                case XMLNode.EMPTY :  
                    nodeChild.XMLType = nodeChild.EMPTY;
                    nodeChild.XMLData = childTag;
                    nodeChild.XMLAttr = EMPTY_STRING;
                    break;

                case XMLNode.DATA  :
                case XMLNode.CDATA :
                    nodeChild.XMLType = nodeChild.DATA;
                    nodeChild.XMLData = childTag;
                    nodeChild.XMLAttr = EMPTY_STRING;
                    break;
            }

            // Insert the child tag into the child trie.
            ListIterator childListIterator = childList.listIterator(0);

            childListIterator.add(nodeChild);

            // Point the root tag to the child tag.
            node.XMLChild = childList;

        }

        // Insert the root tag.
        iterator.add(node);

        // Now evaluate this XML trie, just as if it came from the original AIML.
        response = response + evaluate(level++, list);

        // De-reference the heavy temporary objects created (to expedite garbage collection).
        nodeChild = null;
        childList = null;
        node = null;
        list = null;

        return response;
    }


    /**
     *  Corrects a tag to use a valid 2-dimensional index,
     *  and returns the indices. If either index is invalid or
     *  missing, it is set to 1.
     *
     *  @since  4.1.3
     *
     *  @param tag  the tag whose 2-dimensional index we want
     *
     *  @return a valid 2-dimensional index
     */
    public static int[] getValid2dIndex(XMLNode tag)
    {
        String indexValue = Toolkit.getAttributeValue(INDEX, tag.XMLAttr);
        int[] result = {1, 1};

        // Assign the default if the index attribute is empty.
        if (indexValue.equals(EMPTY_STRING))
        {
            return result;
        }

        // If only one dimension is specified, fill in the other as 1.
        int comma = indexValue.indexOf(COMMA);
        if (comma < 0)
        {
            try
            {
                result[0] = Integer.parseInt(indexValue);
            }
            catch (NumberFormatException e)
            {
            }
            result[1] = 1;
            return result;
        }
        else
        {
            try
            {
                result[0] = Integer.parseInt(indexValue.substring(0, comma));
            }
            catch (NumberFormatException e)
            {
            }
            try
            {
                result[1] = Integer.parseInt(indexValue.substring(comma + 1));
            }
            catch (NumberFormatException e)
            {
            }
            return result;
        }
    }


    /**
     *  Corrects a tag to use a valid 1-dimensional index,
     *  and returns the index.  If the index is missing or valid,
     *  1 is returned.
     *
     *  @since  4.1.3
     *
     *  @param tag  the element whose 1-dimensional index we want
     *
     *  @return a valid 1-dimensional index
     */
    public static int getValid1dIndex(XMLNode tag)
    {
        // Get a valid 1-dimensional index.
        try
        {
            return Integer.parseInt(Toolkit.getAttributeValue(INDEX, tag.XMLAttr));
        }
        catch (NumberFormatException e)
        {
            return 1;
        }
    }
}
