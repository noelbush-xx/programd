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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * A minimal, basic implementation of this interface.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class NamespaceContextImpl implements NamespaceContext
{
    private Map<String, List<String>> uriToPrefix = new HashMap<String, List<String>>();
    
    private Map<String, String> prefixToURI = new HashMap<String, String>();
    
    /**
     * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
     */
    public String getNamespaceURI(String prefix)
    {
        if (prefix == null)
        {
            throw new IllegalArgumentException("Prefix cannot be null.");
        }
        if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX))
        {
            return XMLConstants.NULL_NS_URI;
        }
        if (!this.prefixToURI.containsKey(prefix))
        {
            return XMLConstants.NULL_NS_URI;
        }
        if (prefix.equals(XMLConstants.XML_NS_PREFIX))
        {
            return XMLConstants.XML_NS_URI;
        }
        if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE))
        {
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        }
        return this.prefixToURI.get(prefix);
    }

    /**
     * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
     */
    public String getPrefix(String namespaceURI)
    {
        if (namespaceURI == null)
        {
            throw new IllegalArgumentException("Namespace URI cannot be null.");
        }
        if (namespaceURI.equals(XMLConstants.NULL_NS_URI))
        {
            return XMLConstants.DEFAULT_NS_PREFIX;
        }
        if (namespaceURI.equals(XMLConstants.XML_NS_URI))
        {
            return XMLConstants.XML_NS_PREFIX;
        }
        if (namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI))
        {
            return XMLConstants.XMLNS_ATTRIBUTE;
        }
        if (!this.uriToPrefix.containsKey(namespaceURI))
        {
            return null;
        }
        return this.uriToPrefix.get(namespaceURI).get(0);
    }

    /**
     * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
     */
    public Iterator<String> getPrefixes(String namespaceURI)
    {
        if (namespaceURI == null)
        {
            throw new IllegalArgumentException("Namespace URI cannot be null.");
        }
        if (namespaceURI.equals(XMLConstants.NULL_NS_URI))
        {
            List<String> result = new ArrayList<String>(1);
            result.add(XMLConstants.DEFAULT_NS_PREFIX);
            return Collections.unmodifiableList(result).iterator();
        }
        if (namespaceURI.equals(XMLConstants.XML_NS_URI))
        {
            List<String> result = new ArrayList<String>(1);
            result.add(XMLConstants.XML_NS_PREFIX);
            return Collections.unmodifiableList(result).iterator();
        }
        if (namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI))
        {
            List<String> result = new ArrayList<String>(1);
            result.add(XMLConstants.XMLNS_ATTRIBUTE);
            return Collections.unmodifiableList(result).iterator();
        }
        if (!this.uriToPrefix.containsKey(namespaceURI))
        {
            List<String> result = new ArrayList<String>(0);
            return Collections.unmodifiableList(result).iterator();
        }
        return Collections.unmodifiableList(this.uriToPrefix.get(namespaceURI)).iterator();
    }

    /**
     * Associates the given namespace URI with the given prefix.
     * If the prefix is already associated with another namespace
     * URI, an exception is thrown.
     * 
     * @param namespaceURI
     * @param prefix
     * @throws IllegalArgumentException if the prefix is already associated with another namespace URI
     */
    public void add(String namespaceURI, String prefix)
    {
        if (this.prefixToURI.containsKey(prefix))
        {
            throw new IllegalArgumentException(String.format("Prefix \"%s\" is already associated with namespace URI \"%s\".", prefix, this.prefixToURI.get(prefix)));
        }
        List<String> prefixes;
        if (this.uriToPrefix.containsKey(namespaceURI))
        {
            prefixes = this.uriToPrefix.get(namespaceURI);
        }
        else
        {
            prefixes = new ArrayList<String>();
            this.uriToPrefix.put(namespaceURI, prefixes);
        }
        prefixes.add(prefix);
        this.prefixToURI.put(prefix, namespaceURI);
    }
}
