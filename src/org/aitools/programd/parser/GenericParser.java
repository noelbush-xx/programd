/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.parser;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.aitools.programd.Core;
import org.aitools.programd.processor.Processor;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.util.ClassRegistry;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.XMLKit;

/**
 * A generic parser that allows us to register processors for any element type.
 * This has been heavily modified (simplified) to use DOM.
 * 
 * @author Noel Bush
 * @since 4.1.3
 * @version 4.2
 */
abstract public class GenericParser
{
    /** Each subclass should set this. */
    protected ClassRegistry processorRegistry;
    
    /** The URI of this document. */
    protected URI docURI;
    
    /** The Core in use. */
    protected Core core;

    // Convenience constants.

    /** An empty string, for convenience. */
    protected static final String EMPTY_STRING = "";

    /** The word &quot;index&quot;, for convenience. */
    protected static final String INDEX = "index";

    /** A comma, for convenience. */
    protected static final String COMMA = ",";
	
	/** A DocumentBuilder for producing new Documents. */
	protected static DocumentBuilder utilDocBuilder;
    
    public GenericParser(Core coreToUse)
    {
        if (utilDocBuilder == null)
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            try
            {
                utilDocBuilder = factory.newDocumentBuilder();
            }
            catch (ParserConfigurationException e)
            {
                throw new DeveloperError("Error creating utilDocBuilder for GenericParser.", e);
            }
        }
        this.core = coreToUse;
    }
    
    /**
     * <p>
     * Processes a given URI.
     * </p>
     * <p>
     * This is the general access method for external classes.
     * </p>
     * 
     * @param uri
     *            the XML content
     * @return the DOM produced by parsing
     * @throws ProcessorException
     *             if the content cannot be processed
     */
    public Document parse(URI uri) throws ProcessorException
    {
		if (this.docURI != null)
		{
			this.docURI = this.docURI.resolve(uri);
		}
		else
		{
			this.docURI = uri;
		}
        Document document;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try
        {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(this.docURI.toString());
        }
        catch (IOException e)
        {
            throw new ProcessorException("I/O error while parsing \"" + uri + "\".", e);
        }
        catch (ParserConfigurationException e)
        {
            throw new ProcessorException("Parser configuration error while parsing \"" + uri + "\".", e);
        }
        catch (SAXParseException e)
        {
            throw new ProcessorException("SAX parsing error while parsing \"" + uri + "\".", e);
        }
        catch (SAXException e)
        {
            throw new ProcessorException("SAX exception while parsing \"" + uri + "\".", e);
        }

        return document;
    }

    public String processResponse(URI uri) throws ProcessorException
    {
		if (this.docURI != null)
		{
			this.docURI = this.docURI.resolve(uri);
		}
		else
		{
			this.docURI = uri;
		}
        Document document = parse(this.docURI);
        return evaluate(document);
    }
    
    public void process(URI uri) throws ProcessorException
    {
		if (this.docURI != null)
		{
			this.docURI = this.docURI.resolve(uri);
		}
		else
		{
			this.docURI = uri;
		}
        Document document = parse(this.docURI);
        evaluate(document);
    }
	
	/**
	 * Processes a response by creating a document fragment from the given string
	 * and returning the result of processing it.
	 */
	public String processResponse(String input) throws ProcessorException
	{
		Document template;
		try
		{
			template = utilDocBuilder.parse(new InputSource(new StringReader(input)));
		}
		catch (IOException e)
		{
			throw new ProcessorException("I/O Error processing template: " + e.getMessage());
		}
		catch (SAXException e)
		{
			throw new ProcessorException("SAX Exception processing template: " + e.getMessage());
		}
		return evaluate(template);
	}

    /**
     * Processes a given XML node for a given identifier.
     * 
     * @param element
     *            the element being evaluated
     * @return the result of processing the element
     * @throws ProcessorException
     *             if the content cannot be processed
     */
    public String processElement(Element element) throws ProcessorException
    {
        // Is it a valid element?
        if (element == null)
        {
            return EMPTY_STRING;
        }

        // Search for the tag in the processor registry.
        Class processorClass = null;

        if (this.processorRegistry != null)
        {
            processorClass = this.processorRegistry.get(element.getTagName());
        }
        else
        {
            throw new DeveloperError("processorRegistry has not been initialized!");
        }

        // Create a new instance of the processor.
        Processor processor = null;
        if (processorClass != null)
        {
            // Get the processor constructor that takes a Core as an argument.
            Constructor<Processor> constructor = null;
            try
            {
                constructor = processorClass.getDeclaredConstructor(new Class[] {Core.class});
            }
            catch (NoSuchMethodException e)
            {
                throw new DeveloperError("Developed specified an invalid constructor for Processor: " + e.getMessage());
            }
            catch (SecurityException e)
            {
                throw new DeveloperError("Permission denied to create new Processor with specified constructor: " + e.getMessage());
            }
            
            // Get a new instance of the Multiplexor.
            try
            {
                processor = constructor.newInstance(new Object[] {this});
            } 
            catch (IllegalAccessException e)
            {
                throw new DeveloperError("Underlying constructor for Processor is inaccessible: " + e.getMessage());
            } 
            catch (InstantiationException e)
            {
                throw new DeveloperError("Could not instantiate Processor: " + e.getMessage());
            } 
            catch (IllegalArgumentException e)
            {
                throw new DeveloperError("Illegal argument exception when creating Processor: " + e.getMessage());
            } 
            catch (InvocationTargetException e)
            {
                throw new DeveloperError("Constructor threw an exception when getting a Processor instance from it: " + e.getMessage());
            } 
        }
        else
        {
            throw new ProcessorException("Could not find a processor for \"" + element.getTagName() + "\"!");
        }
        // Return the results of processing the tag.
        if (processor != null)
        {
            return XMLKit.filterWhitespace(processor.process(element, this));
        }
        // (otherwise...)
        throw new DeveloperError("Corrupt processor set.");
    }

    public String evaluate(Document document)
    {
        return evaluate(document.getDocumentElement());
    }

    public String evaluate(NodeList list)
    {
        StringBuffer result = new StringBuffer();
        int listSize = list.getLength();
        for (int index = 0; index < listSize; index++)
        {
            result.append(evaluate(list.item(index)));
        }
        return result.toString();
    }

    /**
     * <p>
     * Recursively evaluates a tree.
     * </p>
     * <p>
     * Depending on whether the engine is at load-time or run time, determines
     * whether or not to process the tag. When the tag is processed, returns the
     * result of processing it.
     * </p>
     * 
     * @param node the node to parse
     * @return the result of processing the tag
     */
    public String evaluate(Node node)
    {
        String response = EMPTY_STRING;

        // Verify there is something to work with.
        if (node == null)
        {
            return response;
        }

        switch (node.getNodeType())
        {
            // Collect and process elements.
            case Node.ELEMENT_NODE:
                try
                {
                    response = response + processElement((Element) node);
                }
                catch (ProcessorException e)
                {
                    throw new DeveloperError(e.getMessage(), e);
                }
                break;

            // Text chunks should just be added to the response.
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
                response = response + node.getNodeValue();
                break;

            default:
                break;
        }
        return response;
    }

    public int hasElement(String tagname, NodeList list)
    {
        return elementCount(tagname, list, false);
    }

    public int elementCount(String tagname, NodeList list)
    {
        return elementCount(tagname, list, true);
    }

    /**
     * <p>
     * Counts the number of nodes of a given type.
     * </p>
     * <p>
     * Used mostly in connection with the &lt;random/&gt; tag in order to see
     * how many candidate listItem structures are beneath it and to set the
     * upper limit on the random number roll.
     * </p>
     * 
     * @param tagname
     *            the name of the tag sought
     * @param list
     *            the XML trie
     * @param allnodes
     *            if false, only count one of the desired type (just see if
     *            <i>any </i> are there)
     * @return the number of nodes of the given type at this level (or
     *         <code>1</code> if at least one node is present and
     *         <code>allnodes</code> is false
     */
    private int elementCount(String tagname, NodeList list, boolean allnodes)
    {
        Node node;
        int numbernodes = 0;

        // Verify there is something to work with.
        if (list == null)
        {
            return 0;
        }

        // Point to the start of the XML trie to parse.
        int nodeCount = list.getLength();

        // Navigate through this entire level.
        for (int index = 0; index < nodeCount; index++)
        {
            node = list.item(index);
            if (node != null)
            {
                // Collect and process only tag elements and empty tags.
                if (node.getNodeType() == Node.ELEMENT_NODE)
                {
                    if ((!node.getNodeName().equals(tagname)) && (allnodes == false))
                    {
                        break;
                    }
                    numbernodes++;
                }
            }
        }
        return numbernodes;
    }

    /**
     * <p>
     * Retrieves the ordernode-th node of a given tag.
     * </p>
     * <p>
     * Typically used to find specific tags beneath a given tag being evaluated
     * (e.g., an &lt;li/&gt; beneath a &lt;random/&gt;).
     * </p>
     * 
     * @param tagname
     *            the name of the tag sought
     * @param list
     *            the XML trie
     * @param ordernode
     *            index of the node we sought
     * @return the node sought
     */
    public Node getNode(String tagname, NodeList list, int ordernode)
    {
        Node node;

        // Verify there is something to work with.
        if (list == null)
        {
            return null;
        }

        // Navigate thru the entire level.
        int nodeCount = list.getLength();
        for (int index = 0; index < nodeCount; index++)
        {
            node = list.item(index);
            if (node != null)
            {
                if (node.getNodeType() == Node.ELEMENT_NODE)
                {
                    // Only deal with the desired one.
                    if (!node.getNodeName().equals(tagname))
                    {
                        break;
                    }
                    // We've found the one we're looking for when ordernode
                    // is zero (after decrementing).
                    if (--ordernode == 0)
                    {
                        return node;
                    }
                }
            }
        }
        return null;
    }

    /**
     * <p>
     * Creates a &quot;mini-template&quot; with a given tag and an optional
     * child tag, then evaluates it recursively.
     * </p>
     * <p>
     * This method is used to map certain tags as combinations of other tags (as
     * in <a
     * href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-short-cut-elements">short-cut
     * elements </a>).
     * </p>
     * 
     * @param element    the element to modify
     * @param newElementName  the new name to give the element
     * @param childContent
     *            the name or content for the child to add
     * @param childType
     *            the type of the child
     * @return the result of processing this structure
     */
    public String shortcutTag(Element element, String newElementName, String childContent, short childType)
    {
        String response = EMPTY_STRING;

        // If the node is empty, we need not continue.
        if (element == null)
        {
            return EMPTY_STRING;
        }

        /*
         * Process children (if any). Clearly, the root tag cannot have an empty
         * type, and the children must exist.
         */
        if ((!childContent.equals(EMPTY_STRING)) && ((childType == Node.ELEMENT_NODE) || (childType == Node.TEXT_NODE)))
        {
			Document ownerDoc = element.getOwnerDocument();
			Element newElement = ownerDoc.createElement(newElementName);
            /*
             * Create an XML node for the child tag. Note that we assume that
             * the child is an empty tag with no attributes. This is reasonable
             * for AIML 1.0.1, but might not always be.
             */
            if (childType == Node.ELEMENT_NODE)
            {
				newElement.appendChild(ownerDoc.createElement(childContent));
            }
            else if (childType == Node.TEXT_NODE)
            {
                newElement.setTextContent(childContent);
            }

            // Now evaluate the node, just as if it came from the original AIML.
            response = response + evaluate(newElement);
        }

        return response;
    }

    /**
     * Corrects a tag to use a valid 2-dimensional index, and returns the
     * indices. If either index is invalid or missing, it is set to 1.
     * 
     * @since 4.1.3
     * @param tag
     *            the tag whose 2-dimensional index we want
     * @return a valid 2-dimensional index
     */
    public static int[] getValid2dIndex(Element element)
    {
        String indexValue = element.getAttribute(INDEX);
        int[] result = { 1, 1 };

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
                // Nothing to do.
            }
            result[1] = 1;
            return result;
        }
        // (otherwise...)
        try
        {
            result[0] = Integer.parseInt(indexValue.substring(0, comma));
        }
        catch (NumberFormatException e)
        {
            // Nothing to do.
        }
        try
        {
            result[1] = Integer.parseInt(indexValue.substring(comma + 1));
        }
        catch (NumberFormatException e)
        {
            // Nothing to do.
        }
        return result;
    }

    /**
     * Corrects a tag to use a valid 1-dimensional index, and returns the index.
     * If the index is missing or valid, 1 is returned.
     * 
     * @since 4.1.3
     * @param tag
     *            the element whose 1-dimensional index we want
     * @return a valid 1-dimensional index
     */
    public static int getValid1dIndex(Element element)
    {
        // Get a valid 1-dimensional index.
        try
        {
            return Integer.parseInt(element.getAttribute(INDEX));
        }
        catch (NumberFormatException e)
        {
            return 1;
        }
    }
    
    /**
     * @return the Core
     */
    public Core getCore()
    {
        return this.core;
    }
}