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
import java.util.ArrayList;

import org.xmlresolver.Resource;
import org.xmlresolver.ResourceResolver;
import org.xmlresolver.sunjaxp.impl.xs.XMLSchemaLoader;
import org.xmlresolver.sunjaxp.util.XMLGrammarPoolImpl;
import org.xmlresolver.sunjaxp.xni.XNIException;
import org.xmlresolver.sunjaxp.xni.grammars.Grammar;
import org.xmlresolver.sunjaxp.xni.grammars.XMLGrammarDescription;
import org.xmlresolver.sunjaxp.xni.grammars.XMLSchemaDescription;
import org.xmlresolver.sunjaxp.xni.parser.XMLInputSource;

/**
 * A very hacky extension of Norm Walsh's XMLGrammarPoolImpl
 * that takes a ResourceResolver and tries to find a schema
 * based on a namespace URI.  I still don't understand why this
 * functionality isn't provided by standard JAXP stuff, but I'm
 * probably just stupid.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class SchemaNSResolvingXMLGrammarPool extends XMLGrammarPoolImpl
{
    private ResourceResolver _resolver;
    
    private ArrayList<String> _blacklist = new ArrayList<String>();
    
    private XMLSchemaLoader _loader = new XMLSchemaLoader();
    
    /**
     * Constructs a grammar pool that will use the given resolver.
     * 
     * @param resolver 
     */
    public SchemaNSResolvingXMLGrammarPool(ResourceResolver resolver)
    {
        super();
        init(resolver);
    }

    /**
     * Constructs a grammar pool that will use the given resolver.
     *
     * @param resolver 
     * @param initialCapacity 
     */
    public SchemaNSResolvingXMLGrammarPool(ResourceResolver resolver, int initialCapacity)
    {
        super(initialCapacity);
        init(resolver);
    }
    
    private void init(ResourceResolver resolver)
    {
        this._resolver = resolver;
        this._loader.setProperty("http://apache.org/xml/properties/internal/error-reporter", new XMLErrorSwallower());
    }

    /**
     * @see org.xmlresolver.sunjaxp.util.XMLGrammarPoolImpl#containsGrammar(org.xmlresolver.sunjaxp.xni.grammars.XMLGrammarDescription)
     */
    @Override
    public boolean containsGrammar(XMLGrammarDescription desc)
    {
        if (desc.getGrammarType().equals(XMLGrammarDescription.XML_SCHEMA) && !this._blacklist.contains(desc.getNamespace()))
        {
            return getResource((XMLSchemaDescription)desc) != null;
        }
        return false;
    }

    /**
     * @see org.apache.xerces.util.XMLGrammarPoolImpl#getGrammar(org.apache.xerces.xni.grammars.XMLGrammarDescription)
     */
    @Override
    public Grammar getGrammar(XMLGrammarDescription desc)
    {
        String namespace = desc.getNamespace();
        if (namespace != null && desc.getGrammarType().equals(XMLGrammarDescription.XML_SCHEMA)&& !this._blacklist.contains(namespace))
        {
            // Try to get it from the cache.
            Grammar grammar = super.getGrammar(desc);
            if (grammar != null)
            {
                return grammar;
            }
            Resource resource = getResource((XMLSchemaDescription)desc);
            if (resource != null)
            {
                try
                {
                    grammar = this._loader.loadGrammar(new XMLInputSource(null, resource.uri(), null));
                }
                catch (XNIException e)
                {
                    // TODO Auto-generated catch block
                    //e.printStackTrace();
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    //e.printStackTrace();
                }
                if (grammar != null)
                {
                    return grammar;
                }
            }
            this._blacklist.add(namespace);
        }
        return null;
    }

    private Resource getResource(XMLSchemaDescription desc)
    {
        String namespace = desc.getNamespace();
        if (namespace != null)
        {
            try
            {
                return this._resolver.resolveNamespaceURI(namespace, XMLGrammarDescription.XML_SCHEMA, "http://www.rddl.org/purposes#schema-validation");
            }
            catch (NullPointerException e)
            {
                return null;
            }
        }
        return null;
    }
}
