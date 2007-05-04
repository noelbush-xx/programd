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
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.aitools.programd.Core;
import org.aitools.programd.processor.Processor;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.processor.ProcessorRegistry;
import org.aitools.util.Classes;
import org.aitools.util.resource.URLTools;
import org.aitools.util.xml.JDOM;
import org.apache.log4j.Logger;
import org.jdom.CDATA;
import org.jdom.Comment;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.input.SAXBuilder;

/**
 * A generic parser that allows us to register processors for any element type. This has been heavily modified
 * (simplified) to use DOM.
 * 
 * @param <P> the base class of Processor to be used
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
abstract public class GenericParser<P extends Processor>
{
    private ProcessorRegistry<P> _registry;
    
    protected URL _baseURL;
    
    protected String _namespaceURI;
    
    protected URL _xmlCatalog;
    
    protected Map<String, Boolean> _xmlParserFeatureSettings;

    protected Core _core;

    protected Logger _logger;

    // Convenience constants.

    /** The word &quot;index&quot;, for convenience. */
    protected static final String INDEX = "index";

    /**
     * Creates a new GenericParser with the given Core as its owner.
     * 
     * @param registry the registry of processors
     * @param core the Core that owns this
     */
    public GenericParser(ProcessorRegistry<P> registry,  Core core)
    {
        this._core = core;
        this._baseURL = this._core.getBaseURL();
        this._logger = this._core.getLogger();
        this._registry = registry;
        this._namespaceURI = this._registry.getNamespaceURI();
        this._xmlCatalog = this._core.getXMLCatalog();
        this._xmlParserFeatureSettings = this._core.getXMLParserFeatureSettings();
    }

    /**
     * Processes whatever is at the given URL and returns a response.
     * 
     * @param url where to find what is to be processed
     * @return the result of processing whatever is found at the URL
     * @throws ProcessorException if there is a problem processing what is found at the URL
     */
    public String processResponse(URL url) throws ProcessorException
    {
        return evaluate(JDOM.getDocument(url, this._logger));
    }

    /**
     * Processes an XML fragment provided in a string.
     * 
     * @param input the string from which to create the document fragment
     * @param baseURI the base URI to set for the document fragment
     * @return the result of processing the document fragment created from the given string
     * @throws ProcessorException if there was a problem processing the document fragment created from the given string
     * @throws JDOMException 
     * @throws IOException 
     */
    public String processResponse(String input, String baseURI) throws ProcessorException, JDOMException, IOException
    {
        Document document = new SAXBuilder().build(new StringReader(input));
        document.setBaseURI(baseURI);
        return evaluate(document);
    }

    /**
     * Processes an XML fragment provided in a string.  This version of the
     * method does <i>not</i> set a base URI for the document fragment.
     * 
     * @param input the string from which to create the document fragment
     * @return the result of processing the document fragment created from the given string
     * @throws ProcessorException if there was a problem processing the document fragment created from the given string
     * @throws JDOMException 
     * @throws IOException 
     */
    public String processResponse(String input) throws ProcessorException, JDOMException, IOException
    {
        return evaluate(new SAXBuilder().build(new StringReader(input)));
    }

    /**
     * Processes whatever is at the given URL, returning nothing.
     * 
     * @param url the URL at which to find whatever is to be processed
     * @throws ProcessorException if there is a problem processing whatever is at the given URL
     */
    public void process(URL url) throws ProcessorException
    {
        this._logger.info(String.format("Loading \"%s\".", URLTools.unescape(url)));
        processResponse(url);
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
        return evaluate(document.getRootElement());
    }

    /**
     * Evaluates the given content list and returns the result.
     * 
     * @param list the list of content to evaluate
     * @return the result of evaluating the given list of nodes
     * @throws ProcessorException if there is an error in processing
     */
    public String evaluate(List<Content> list) throws ProcessorException
    {
        StringBuilder result = new StringBuilder();
        for (Content node : list)
        {
            // Would be nice not to have to do this:
            if (node instanceof Element)
            {
                result.append(evaluate((Element)node));
            }
            else if (node instanceof Text)
            {
                result.append(evaluate((Text) node));
            }
            else if (node instanceof CDATA)
            {
                result.append(evaluate((CDATA)node));
            }
            else if (node instanceof Comment)
            {
                result.append(evaluate((Comment)node));
            }
            else
            {
                assert false : "Unknown subclass of jdom.org.Content!";
            }
        }
        return result.toString();
    }

    /**
     * Recursively evaluates an element.
     * 
     * @param element the element
     * @return the result of processing the element
     * @throws ProcessorException if there is an error in processing
     */
    @SuppressWarnings("unchecked")
    public String evaluate(Element element) throws ProcessorException
    {
        // Is it a valid element?
        if (element == null)
        {
            return "";
        }

        // Search for the tag in the processor registry.
        Class<? extends P> processorClass = null;

        String elementNamespaceURI = element.getNamespaceURI();
        Document elementDocument = element.getDocument();
        boolean emitXMLNS = elementDocument != null && 
                            (element.equals(element.getDocument().getRootElement())
                            || (elementNamespaceURI != null && !elementNamespaceURI.equals(element.getDocument()
                            .getRootElement().getNamespaceURI())));
        if (elementNamespaceURI == null || this._registry.getNamespaceURI().equals(elementNamespaceURI))
        {
            processorClass = this._registry.get(element.getName());

            // Process the element with a new instance of the processor.
            return Classes.getNewInstance(processorClass, "Processor", this._core).process(element, this);
        }
        // otherwise (if this element is from a different namespace)
        if (element.getContent().size() == 0)
        {
            return JDOM.renderEmptyElement(element, emitXMLNS);
        }
        // otherwise...
        return JDOM.renderStartTag(element, emitXMLNS) + evaluate(element.getContent()) + JDOM.renderEndTag(element);
    }

    /**
     * Returns formatted text.
     * 
     * @param text
     * @return the text
     */
    public String evaluate(Text text)
    {
        return text.getText();
    }

    /**
     * Formats a CDATA section node.
     * 
     * @param node
     * @return the formatted CDATA section node
     */
    public String evaluate(CDATA node)
    {
        return String.format("<![CDATA[%s]]>", node.getText());
    }

    /**
     * Formats a comment.
     * 
     * @param comment
     * @return the formatted comment
     */
    public String evaluate(Comment comment)
    {
        return String.format("<!--%s-->", comment.getText());
    }

    /**
     * <p> Creates a &quot;mini-template&quot; with a given tag and an optional child tag, then evaluates it
     * recursively. </p> <p> This method is used to map certain tags as combinations of other tags (as in <a
     * href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-short-cut-elements">short-cut elements </a>). </p>
     * 
     * @param element the element to modify
     * @param newElementName the new name to give the element
     * @param childContent the name or content for the child to add
     * @param childType the type of the child
     * @return the result of processing this structure
     * @throws ProcessorException if there is an error in processing
     */
    public String shortcutTag(Element element, String newElementName, String childContent, Class<? extends Content> childType)
            throws ProcessorException
    {
        String response = "";

        // If the node is empty, we need not continue.
        if (element == null)
        {
            return "";
        }

        /*
         * Process children (if any). Clearly, the root tag cannot have an empty type, and the children must exist.
         */
        if ((!"".equals(childContent)) && ((childType == Element.class) || (childType == Text.class)))
        {
            Element newElement = new Element(newElementName, element.getNamespaceURI());
            /*
             * Create an XML node for the child tag. Note that we assume that the child is an empty tag with no
             * attributes. This is reasonable for AIML 1.0.1, but might not always be.
             */
            if (childType == Element.class)
            {
                newElement.addContent(new Element(childContent, element.getNamespaceURI()));
            }
            else if (childType == Text.class)
            {
                newElement.setText(childContent);
            }

            // Now evaluate the node, just as if it came from the original AIML.
            response = response + evaluate(newElement);
        }

        return response;
    }

    /**
     * Corrects a tag to use a valid 2-dimensional index, and returns the indices. If either index is invalid or
     * missing, it is set to 1.
     * 
     * @param element the element for which to get a valid 2-dimensional index
     * @since 4.1.3
     * @return a valid 2-dimensional index
     */
    public static int[] getValid2dIndex(Element element)
    {
        String indexValue = element.getAttributeValue("index");
        int[] result = { 1, 1 };

        // Assign the default if the index attribute is empty.
        if (indexValue == null || "".equals(indexValue))
        {
            return result;
        }

        // If only one dimension is specified, fill in the other as 1.
        int comma = indexValue.indexOf(',');
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
     * Corrects a tag to use a valid 1-dimensional index, and returns the index. If the index is missing or valid, 1 is
     * returned.
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
            return Integer.parseInt(element.getAttributeValue("index"));
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
        assert this._core != null : "Tried to get the Core from a GenericParser that does not have one!";
        return this._core;
    }
}
