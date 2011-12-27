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
import java.util.List;

import org.aitools.util.resource.URLTools;
import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.runtime.UserError;
import org.apache.log4j.Logger;
import org.apache.xerces.util.XMLCatalogResolver;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.xml.sax.Attributes;

/**
 * XML utilities specific to JDOM.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class JDOM {
  
  /** JDOM relies on an underlying SAX parser; this is the implementation we want to use. */
  static String SAX_PARSER_IMPLEMENTATION = "org.apache.xerces.parsers.SAXParser";

  /**
   * Contextualizes the given path within the context of the given element's document.
   * 
   * @param path
   * @param element
   * @return the contextualized path
   */
  public static URL contextualize(String path, Element element) {
    return URLTools.contextualize(element.getDocument().getBaseURI(), path);
  }

  /**
   * Converts the given type as returned by {@link Attributes#getType(int)} into a type meaningful to {@link Attribute}.
   * 
   * @param typeName
   * @return the type
   */
  public static int getAttributeType(String typeName) {
    int type = Attribute.UNDECLARED_TYPE;
    if (typeName.equals("CDATA")) {
      type = Attribute.CDATA_TYPE;
    }
    else if (typeName.equals("ID")) {
      type = Attribute.ID_TYPE;
    }
    else if (typeName.equals("IDREF")) {
      type = Attribute.IDREF_TYPE;
    }
    else if (typeName.equals("IDREFS")) {
      type = Attribute.IDREFS_TYPE;
    }
    else if (typeName.equals("NMTOKEN")) {
      type = Attribute.NMTOKEN_TYPE;
    }
    else if (typeName.equals("NMTOKENS")) {
      type = Attribute.NMTOKENS_TYPE;
    }
    else if (typeName.equals("ENTITY")) {
      type = Attribute.ENTITY_TYPE;
    }
    else if (typeName.equals("ENTITIES")) {
      type = Attribute.ENTITIES_TYPE;
    }
    else if (typeName.equals("NOTATION")) {
      type = Attribute.NOTATION_TYPE;
    }
    return type;
  }

  /**
   * Converts the given namespace uri String to a {@link Namespace}, or {@link Namespace#NO_NAMESPACE} if the argument
   * is empty or null.
   * 
   * @param uri
   * @return the namespace
   */
  public static Namespace getNamespace(String uri) {
    Namespace namespace;
    if ("".equals(uri) || uri == null) {
      namespace = Namespace.NO_NAMESPACE;
    }
    else {
      namespace = Namespace.getNamespace(uri);
    }
    return namespace;
  }

  /**
   * Renders a set of attributes.
   * 
   * @param attributes the attributes to render
   * @return the rendered attributes
   */
  public static String renderAttributes(List<Attribute> attributes) {
    StringBuilder result = new StringBuilder();
    if (attributes != null) {
      for (Attribute attribute : attributes) {
        String attributeName = attribute.getName();
        if (attributeName != null && !"xmlns".equals(attributeName)) {
          result.append(String.format(" %s=\"%s\"", attributeName, attribute.getValue()));
        }
      }
    }
    return result.toString();
  }

  /**
   * Renders a given element as an empty element, including a namespace declaration, if requested.
   * 
   * @param element the element to render
   * @param includeNamespaceAttribute whether to include the namespace attribute
   * @return the result of the rendering
   */
  @SuppressWarnings("unchecked")
  public static String renderEmptyElement(Element element, boolean includeNamespaceAttribute) {
    StringBuilder result = new StringBuilder();
    result.append(String.format("<%s", element.getName()));
    if (includeNamespaceAttribute) {
      result.append(String.format(" xmlns=\"%s\"", element.getNamespaceURI()));
    }
    result.append(String.format("%s/>", renderAttributes(element.getAttributes())));
    return result.toString();
  }

  /**
   * Renders a given element as an end tag.
   * 
   * @param element the element to render
   * @return the result of the rendering
   */
  public static String renderEndTag(Element element) {
    return String.format("</%s>", element.getName());
  }

  /**
   * Renders a given element as a start tag, including a namespace declaration, if requested.
   * 
   * @param element the element to render
   * @param includeNamespaceAttribute whether to include the namespace attribute
   * @return the rendering of the element
   */
  @SuppressWarnings("unchecked")
  public static String renderStartTag(Element element, boolean includeNamespaceAttribute) {
    StringBuilder result = new StringBuilder();
    result.append(String.format("<%s", element.getName()));
    if (includeNamespaceAttribute) {
      result.append(String.format(" xmlns=\"%s\"", element.getNamespaceURI()));
    }
    result.append(String.format("%s>", renderAttributes(element.getAttributes())));
    return result.toString();
  }

  /**
   * Renders a given element name and set of attributes as a start tag, including a namespace declaration, if requested.
   * 
   * @param elementName the name of the element to render
   * @param attributes the attributes to include
   * @param includeNamespaceAttribute whether or not to include the namespace attribute
   * @param namespaceURI the namespace URI
   * @return the rendering result
   */
  public static String renderStartTag(String elementName, List<Attribute> attributes,
      boolean includeNamespaceAttribute, String namespaceURI) {
    StringBuilder result = new StringBuilder();
    result.append(String.format("<%s", elementName));
    if (includeNamespaceAttribute) {
      result.append(String.format(" xmlns=\"%s\"", namespaceURI));
    }
    result.append(String.format("%s>", renderAttributes(attributes)));
    return result.toString();
  }

  /**
   * Loads the given path and creates a JDOM Document. Also explicitly sets the base URI for the
   * Document object to the <code>path</code>, since JDOM doesn't seem to do this for us.
   * 
   * @param location
   * @param catalogPath 
   * @param logger
   * @return a DOM Document object
   */
  public static Document getDocument(URL location, String catalogPath, Logger logger) {

    // Get a SAXBuilder, so we can configure the underlying parser.
    SAXBuilder builder = new SAXBuilder(SAX_PARSER_IMPLEMENTATION, true);
    builder.setFeature("http://apache.org/xml/features/validation/schema", true);
    
    // Get a resolver, and attach it to the builder.
    XMLCatalogResolver resolver = Resolvers.newXMLCatalogResolver(catalogPath);
    builder.setProperty("http://apache.org/xml/properties/internal/entity-resolver", resolver);

    // Finally, attach an error handler to the builder.
    builder.setErrorHandler(new SimpleSAXErrorHandler(logger));

    // Parse the document using the document builder.
    Document document = null;
    try {
      document = builder.build(location.toExternalForm());
    }
    catch (JDOMException e) {
      throw new DeveloperError(String.format("Error while parsing XML at %s", location), e);
    }
    catch (IOException e) {
      throw new UserError(String.format("I/O error when parsing XML at %s", location), e);
    }

    document.setBaseURI(location.toExternalForm());
    return document;
  }
}
