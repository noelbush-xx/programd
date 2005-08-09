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
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.aitools.programd.Core;
import org.aitools.programd.processor.Processor;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.processor.ProcessorRegistry;
import org.aitools.programd.util.ClassUtils;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.NotARegisteredClassException;
import org.aitools.programd.util.URITools;
import org.aitools.programd.util.UserError;
import org.aitools.programd.util.XMLKit;

/**
 * A generic parser that allows us to register processors for any element type.
 * This has been heavily modified (simplified) to use DOM.
 * 
 * @param
 * <P>
 * the base class of Processor to be used
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.1.3
 * @version 4.5
 */
abstract public class GenericParser<P extends Processor>
{
    /** Set by subclasses. */
    private ProcessorRegistry<P> processorRegistry;

    /** The URL of this document. */
    protected URL docURL;

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
    
    /** The comment start marker. */
    protected static final String COMMENT_START = "<!--";

    /** The comment end marker. */
    protected static final String COMMENT_END = "-->";

    /**
     * Creates a new GenericParser with the given Core as its owner.
     * 
     * @param registry the registry of processors
     * @param coreToUse the Core that owns this
     */
    public GenericParser(ProcessorRegistry<P> registry, Core coreToUse)
    {
        this.core = coreToUse;
        this.processorRegistry = registry;
        if (utilDocBuilder == null)
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            try
            {
                utilDocBuilder = factory.newDocumentBuilder();
            }
            catch (ParserConfigurationException e)
            {
                throw new DeveloperError("Error creating utilDocBuilder for GenericParser.", e);
            }
        }
    }

    /**
     * <p>
     * Processes a given URL.
     * </p>
     * <p>
     * This is the general access method for external classes.
     * </p>
     * 
     * @param url the XML content
     * @return the DOM produced by parsing
     * @throws ProcessorException if the content cannot be processed
     */
    public Document parse(URL url) throws ProcessorException
    {
        contextualize(url);
        Document document;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try
        {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(this.docURL.toString());
        }
        catch (IOException e)
        {
            throw new ProcessorException("I/O error while parsing \"" + url + "\".", e);
        }
        catch (ParserConfigurationException e)
        {
            throw new ProcessorException("Parser configuration error while parsing \"" + url
                    + "\".", e);
        }
        catch (SAXParseException e)
        {
            throw new ProcessorException("SAX parsing error while parsing \"" + url + "\".", e);
        }
        catch (SAXException e)
        {
            throw new ProcessorException("SAX exception while parsing \"" + url + "\".", e);
        }

        return document;
    }

    /**
     * Processes whatever is at the given URL and returns a response.
     * 
     * @param url where to find what is to be processed
     * @return the result of processing whatever is found at the URL
     * @throws ProcessorException if there is a problem processing what is found
     *             at the URL
     */
    public String processResponse(URL url) throws ProcessorException
    {
        contextualize(url);
        Document document = parse(this.docURL);
        return evaluate(document);
    }

    /**
     * Processes whatever is at the given URL, returning nothing.
     * 
     * @param url the URL at which to find whatever is to be processed
     * @throws ProcessorException if there is a problem processing whatever is
     *             at the given URL
     */
    public void process(URL url) throws ProcessorException
    {
        contextualize(url);
        Document document = parse(this.docURL);
        evaluate(document);
    }

    private void contextualize(URL url)
    {
        if (url != null)
        {
            if (this.docURL != null)
            {
                this.docURL = URITools.contextualize(this.docURL, url);
            }
            else
            {
                this.docURL = url;
            }
        }
        else
        {
            throw new DeveloperError("Passed a null URL to GenericParser.contextualize!",
                    new NullPointerException());
        }
    }

    /**
     * Processes an XML fragment provided in a string.
     * 
     * @param input the string from which to create the document fragment
     * @return the result of processing the document fragment created from the
     *         given string
     * @throws ProcessorException if there was a problem processing the document
     *             fragment created from the given string
     */
    public String processResponse(String input) throws ProcessorException
    {
        Document template = XMLKit.parseAsDocumentFragment(input);
        return evaluate(template);
    }

    /**
     * Processes a given XML node.
     * 
     * @param element the element being evaluated
     * @return the result of processing the element
     * @throws ProcessorException if the content cannot be processed
     */
    public String processElement(Element element) throws ProcessorException
    {
        // Is it a valid element?
        if (element == null)
        {
            return EMPTY_STRING;
        }

        // Search for the tag in the processor registry.
        Class<? extends P> processorClass = null;

        String elementNamespaceURI = element.getNamespaceURI();
        boolean emitXMLNS = element.isSameNode(element.getOwnerDocument().getDocumentElement())
                || (elementNamespaceURI != null &&!elementNamespaceURI.equals(element.getOwnerDocument().getDocumentElement()
                        .getNamespaceURI()));
        if (elementNamespaceURI == null
                || this.processorRegistry.getNamespaceURI().equals(elementNamespaceURI))
        {
            try
            {
                processorClass = this.processorRegistry.get(element.getLocalName());
            }
            catch (NotARegisteredClassException e)
            {
                return handleUnknownElement(element, e);
            }

            // Process the element with a new instance of the processor.
            return XMLKit.filterWhitespace(ClassUtils.getNewInstance(processorClass, "Processor",
                    this.core).process(element, this));
        }
        // otherwise (if this element is from a different namespace)
        if (element.getChildNodes().getLength() == 0)
        {
            return XMLKit.renderEmptyElement(element, emitXMLNS);
        }
        // otherwise...
        return XMLKit.renderStartTag(element, emitXMLNS) + evaluate(element.getChildNodes())
                + XMLKit.renderEndTag(element);
    }

    /**
     * Handles an unknown element. By default it throws an exception. This can
     * be overridden by subclasses that want to do something else, like ignore
     * it, and/or just print a warning message.
     * 
     * @param element the element that could not be handled
     * @param e the exception generated by trying to find a processor for the
     *            element
     * @return the result of handling the unknown element (unless, of course, an
     *         exception is throw)
     */
    protected String handleUnknownElement(Element element, NotARegisteredClassException e)
    {
        throw new DeveloperError("Unknown processor \"" + element.getLocalName() + "\".", e);
    }

    /**
     * Evaluates the given document and returns the result.
     * 
     * @param document the document to evaluate
     * @return the result of evaluating the document
     * @throws ProcessorException if there is an error in processing
     */
    public String evaluate(Document document) throws ProcessorException
    {
        return evaluate(document.getDocumentElement());
    }

    /**
     * Evaluates the given node list and returns the result.
     * 
     * @param list the list of nodes to evaluate
     * @return the result of evaluating the given list of nodes
     * @throws ProcessorException if there is an error in processing
     */
    public String evaluate(NodeList list) throws ProcessorException
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
     * @throws ProcessorException if there is an error in processing
     */
    public String evaluate(Node node) throws ProcessorException
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
                response += processElement((Element) node);
                break;

            // Text chunks should just be added to the response.
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
                response += node.getNodeValue();
                break;
                
            case Node.COMMENT_NODE:
                response += COMMENT_START + node.getTextContent() + COMMENT_END;
                break;

            default:
                break;
        }
        return response;
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
     * @param element the element to modify
     * @param newElementName the new name to give the element
     * @param childContent the name or content for the child to add
     * @param childType the type of the child
     * @return the result of processing this structure
     * @throws ProcessorException if there is an error in processing
     */
    public String shortcutTag(Element element, String newElementName, String childContent,
            short childType) throws ProcessorException
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
        if ((!childContent.equals(EMPTY_STRING))
                && ((childType == Node.ELEMENT_NODE) || (childType == Node.TEXT_NODE)))
        {
            Document ownerDoc = element.getOwnerDocument();
            Element newElement = ownerDoc
                    .createElementNS(element.getNamespaceURI(), newElementName);
            /*
             * Create an XML node for the child tag. Note that we assume that
             * the child is an empty tag with no attributes. This is reasonable
             * for AIML 1.0.1, but might not always be.
             */
            if (childType == Node.ELEMENT_NODE)
            {
                newElement.appendChild(ownerDoc.createElementNS(element.getNamespaceURI(),
                        childContent));
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
     * Verifies that the given URL points to something real/accessible, then
     * processes it (not returning anything).
     * 
     * @param urlString the URL to check
     * @throws ProcessorException if there is an error in processing
     */
    public void verifyAndProcess(String urlString) throws ProcessorException
    {
        URL url = null;
        if (this.docURL != null)
        {
            url = URITools.contextualize(this.docURL, urlString);
        }
        verifyAndProcess(url);
    }

    /**
     * Verifies that the given URL points to something real/accessible, then
     * processes it, returning a response.
     * 
     * @param urlString the URL to check
     * @return the response produced by processing whatever is at the URL
     * @throws ProcessorException if there is an error in processing
     */
    public String verifyAndProcessResponse(String urlString) throws ProcessorException
    {
        URL url = null;
        if (this.docURL != null)
        {
            url = URITools.contextualize(this.docURL, urlString);
        }
        return verifyAndProcessResponse(url);
    }

    /**
     * Verifies that the given URL points to something real/accessible, then
     * processes it (not returning anything).
     * 
     * @param url the URL to check
     * @throws ProcessorException if there is an error in processing
     */
    public void verifyAndProcess(URL url) throws ProcessorException
    {
        try
        {
            url.openConnection();
        }
        catch (IOException e)
        {
            throw new UserError("Could not open a connection to \"" + url.toExternalForm() + "\".",
                    e);
        }
        process(url);
    }

    /**
     * Verifies that the given URL points to something real/accessible, then
     * processes it (not returning anything).
     * 
     * @param url the URL to check
     * @return the result of processing whatever is at the URL
     * @throws ProcessorException if there is an error in processing
     */
    public String verifyAndProcessResponse(URL url) throws ProcessorException
    {
        try
        {
            url.openConnection();
        }
        catch (IOException e)
        {
            throw new UserError("Could not open a connection to \"" + url.toExternalForm() + "\".",
                    e);
        }
        return processResponse(url);
    }

    /**
     * Corrects a tag to use a valid 2-dimensional index, and returns the
     * indices. If either index is invalid or missing, it is set to 1.
     * 
     * @param element the element for which to get a valid 2-dimensional index
     * @since 4.1.3
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
     * @param element the element for which to get a valid 1-dimensional index
     * @since 4.1.3
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
        if (this.core == null)
        {
            throw new DeveloperError(
                    "Tried to get the Core from a GenericParser that does not have one!",
                    new NullPointerException());
        }
        return this.core;
    }
}