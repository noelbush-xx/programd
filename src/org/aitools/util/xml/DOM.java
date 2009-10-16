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

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aitools.util.runtime.DeveloperError;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xmlresolver.Catalog;
import org.xmlresolver.Resolver;
import org.xmlresolver.ResourceResolver;

/**
 * Utilities specific to the standard W3C DOM.
 *
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class DOM
{
    static
    {
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.xmlresolver.sunjaxp.jaxp.DocumentBuilderFactoryImpl");
    }

    /**
     * Parses the document and returns a DOM Document object.
     * XInclusions are performed first, then validation.
     * 
     * @param location
     * @param logger 
     * @return a DOM Document object
     */
    public static Document getDocument(URL location, Logger logger)
    {
        ResourceResolver resolver = new ResourceResolver(new Catalog());
        Resolver entityResolver = new Resolver(resolver);
        resolver.setEntityResolver(entityResolver);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setXIncludeAware(true);
        //factory.setValidating(true);
        DocumentBuilder builder = null;
        try
        {
            builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new SimpleSAXErrorHandler(logger));
            builder.setEntityResolver(entityResolver);
        }
        catch (ParserConfigurationException e)
        {
            throw new DeveloperError("Exception while creating DOM DocumentBuilder.", e);
        }
        
        Document document = null;
        try
        {
            document = builder.parse(location.toExternalForm());
        }
        catch (SAXException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return document;
        
        /*LSParser parser = ((DOMImplementationLS) builder.getDOMImplementation().getFeature("LS","3.0")).createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, XMLConstants.W3C_XML_SCHEMA_NS_URI);
        
        DOMConfiguration config = parser.getDomConfig();
        config.setParameter("validate", Boolean.TRUE);
        config.setParameter("schema-type", XMLConstants.W3C_XML_SCHEMA_NS_URI);
        config.setParameter("error-handler", new SimpleDOMErrorHandler(logger));
        config.setParameter("resource-resolver", entityResolver);
        
        return parser.parseURI(location.toExternalForm());*/
    }
}
