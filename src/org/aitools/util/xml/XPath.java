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

import java.util.List;

import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.runtime.UserError;
import org.jdom.IllegalNameException;
import org.jdom.JDOMException;

/**
 * Utility methods for XPath.  (Just thin convenience wrappers
 * around JDOM's XPath stuff.)
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class XPath
{
    /**
     * Returns the string value of the given XPath expression, evaluated from the given node context.
     *
     * @param path
     * @param namespaceURI a namespace URI
     * @param prefix a prefix associated with the namespace URI
     * @param context
     * @return value
     */
    public static String getStringValue(String path, String namespaceURI, String prefix, Object context)
    {
        try
        {
            return getXPath(path, namespaceURI, prefix).valueOf(context);
        }
        catch (JDOMException e)
        {
            throw new UserError("Failed to evaluate XPath expression.", e);
        }
    }

    /**
     * Returns the number value of the given XPath expression, evaluated from the given node context.
     *
     * @param path
     * @param namespaceURI a namespace URI
     * @param prefix a prefix associated with the namespace URI
     * @param context
     * @return value
     */
    public static Number getNumberValue(String path, String namespaceURI, String prefix, Object context)
    {
        try
        {
            return getXPath(path, namespaceURI, prefix).numberValueOf(context);
        }
        catch (JDOMException e)
        {
            throw new UserError("Failed to evaluate XPath expression.", e);
        }
    }

    /**
     * Returns the number value of the given XPath expression, evaluated from the given node context.
     *
     * @param path
     * @param namespaceURI a namespace URI
     * @param prefix a prefix associated with the namespace URI
     * @param context
     * @return value
     */
    public static Object getNode(String path, String namespaceURI, String prefix, Object context)
    {
        try
        {
            return getXPath(path, namespaceURI, prefix).selectSingleNode(context);
        }
        catch (JDOMException e)
        {
            throw new UserError("Failed to evaluate XPath expression.", e);
        }
    }

    /**
     * Returns the number value of the given XPath expression, evaluated from the given node context.
     *
     * @param path
     * @param namespaceURI a namespace URI
     * @param prefix a prefix associated with the namespace URI
     * @param context
     * @return value
     */
    @SuppressWarnings("unchecked")
    public static List<Object> getNodeList(String path, String namespaceURI, String prefix, Object context)
    {
        try
        {
            return getXPath(path, namespaceURI, prefix).selectNodes(context);
        }
        catch (JDOMException e)
        {
            throw new UserError("Failed to evaluate XPath expression.", e);
        }
    }
    
    /**
     * Creates a new XPath object with the given path,
     * with the given namespace URI and prefix mappings.
     *
     * @param path
     * @param namespaceURI a namespace URI
     * @param prefix a prefix associated with the namespace URI
     * @return XPath object
     */
    protected static org.jdom.xpath.XPath getXPath(String path, String namespaceURI, String prefix)
    {
        org.jdom.xpath.XPath xpath;
        try
        {
            xpath = org.jdom.xpath.XPath.newInstance(path);
        }
        catch (JDOMException e)
        {
            throw new UserError("Error in settings.", e);
        }
        try
        {
            xpath.addNamespace(prefix, namespaceURI);
        }
        catch (IllegalNameException e)
        {
            throw new DeveloperError(String.format("Illegal namespace \"%s\".", namespaceURI), e);
        }
        return xpath;
    }
}
