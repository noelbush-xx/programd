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

import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.aitools.util.runtime.DeveloperError;
import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Namespace;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xmlresolver.Catalog;
import org.xmlresolver.Resolver;
import org.xmlresolver.ResourceResolver;
import org.xmlresolver.sunjaxp.xni.grammars.XMLGrammarPool;

/**
 * Utilities specific to SAX interfaces.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class SAX
{
    static
    {
        System.setProperty("javax.xml.parsers.SAXParserFactory", "org.xmlresolver.sunjaxp.jaxp.SAXParserFactoryImpl");
    }

    /**
     * Sets up a SAX XML reader that is namespace aware and uses the given feature settings.
     * @param logger 
     * 
     * @return the parser
     */
    public static XMLReader getReader(Logger logger)
    {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        //factory.setValidating(true);
        SAXParser parser = null;
        try
        {
            parser = factory.newSAXParser();
        }
        catch (ParserConfigurationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (SAXException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (parser == null)
        {
            throw new DeveloperError("SAXParser is null; cannot continue.", new NullPointerException());
        }
        try
        {
            parser.setProperty(
                "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                "http://www.w3.org/2001/XMLSchema"
            );
        }
        catch (SAXNotRecognizedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (SAXNotSupportedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        XMLReader reader = null;
        try
        {
            reader = parser.getXMLReader();
        }
        catch (SAXException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        ResourceResolver resolver = new ResourceResolver(new Catalog());
        Resolver entityResolver = new Resolver(resolver);
        resolver.setEntityResolver(entityResolver);
        
        if (reader == null)
        {
            throw new DeveloperError("XMLReader is null; cannot continue", new NullPointerException());
        }

        reader.setEntityResolver(entityResolver);
        
        XMLGrammarPool grammarPool = new SchemaNSResolvingXMLGrammarPool(resolver);
        
        try
        {
            reader.setFeature("http://xml.org/sax/features/use-entity-resolver2", true);
            reader.setFeature("http://xml.org/sax/features/validation", true);
            reader.setFeature("http://apache.org/xml/features/validation/dynamic", true);
            reader.setFeature("http://apache.org/xml/features/validation/schema", true);
            reader.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
            reader.setProperty("http://apache.org/xml/properties/internal/grammar-pool", grammarPool);
        }
        catch (SAXNotRecognizedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (SAXNotSupportedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        reader.setErrorHandler(new SimpleSAXErrorHandler(logger));
        return reader;
    }

    /**
     * Converts a SAX Attributes object to a List of Attribute objects.
     * 
     * @param attributes
     * @return the List of Attribute objects
     */
    public static List<Attribute> getAttributeList(Attributes attributes)
    {
        int count = attributes.getLength();
        List<Attribute> result = new ArrayList<Attribute>(count);
        for (int index = 0; index < count; index++)
        {
            // JDOM requires us to jump through this little hoop:
            Namespace namespace;
            String namespaceURI = attributes.getURI(index);
            if (namespaceURI.equals(XMLConstants.XML_NS_URI))
            {
                namespace = Namespace.XML_NAMESPACE;
            }
            else
            {
                namespace = Namespace.getNamespace(namespaceURI);
            }
            result.add(new Attribute(attributes.getLocalName(index), attributes.getValue(index),
                    JDOM.getAttributeType(attributes.getType(index)), namespace));
        }
        return result;
    }

    /**
     * Renders a given element name and set of attributes as a start tag, including a namespace declaration, if
     * requested.
     * 
     * @param elementName the name of the element to render
     * @param attributes the attributes to include
     * @param includeNamespaceAttribute whether or not to include the namespace attribute
     * @param namespaceURI the namespace URI
     * @return the rendering result
     */
    public static String renderStartTag(String elementName, Attributes attributes,
            boolean includeNamespaceAttribute, String namespaceURI)
    {
        return JDOM.renderStartTag(elementName, getAttributeList(attributes), includeNamespaceAttribute, namespaceURI);
    }
}
