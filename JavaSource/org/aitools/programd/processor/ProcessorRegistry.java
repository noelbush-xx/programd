/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor;

import org.aitools.util.ClassRegistry;

/**
 * Registers {@link Processor}s associated with a given namespace URI.
 * 
 * @param <B> the base class for the processors
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class ProcessorRegistry<B> extends ClassRegistry<B>
{
    /** The namespace URI of the content type for which this registry is intended. */
    protected String _namespaceURI;
    
    /** A description of the type of document handled by these processors. */
    protected String _type;

    /**
     * Creates a <code>ProcessorRegistry</code> associated with the given namespace URI.
     * 
     * @param namespaceURI the namespace URI for the processors
     * @param type a description of the type of document handled by these processors
     * @param classnames the names of the classes to register
     * @see ClassRegistry
     */
    protected ProcessorRegistry(String namespaceURI, String type, String[] classnames)
    {
        super(classnames);
        this._namespaceURI = namespaceURI;
        this._type = type;
    }

    /**
     * @return the namespace URI of the content type for which this registry manages processors
     */
    public String getNamespaceURI()
    {
        return this._namespaceURI;
    }
    
    /**
     * @return a description of the type of document handled by these processors
     */
    public String getType()
    {
        return this._type;
    }
}
