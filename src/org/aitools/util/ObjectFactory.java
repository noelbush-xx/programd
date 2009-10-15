/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.util;

import org.aitools.util.Classes;
import org.aitools.util.runtime.UserError;

/**
 * A very simple parameterized object factory.
 * 
 * @param <T> the (super-)type of object that will be created
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class ObjectFactory<T>
{
    /** The classname of the objects that will be produced. */
    private String _classname;

    /** The actual implementation to create. */
    private Class<? extends T> _clazz;

    /** An empty argument set to be passed when constructing an object. */
    private static final Object[] EMPTY_ARGS = new Object[] {};

    /**
     * Creates a new <code>ObjectFactory</code>
     * that is configured to create instances of the 
     * subclass <code>classname</code>.
     * 
     * @param classname
     */
    @SuppressWarnings("unchecked")
    public ObjectFactory(String classname)
    {
        this._classname = classname;
        try
        {
            this._clazz = (Class<? extends T>) Class.forName(classname);
        }
        catch (ClassNotFoundException e)
        {
            throw new UserError(String.format("Could not find implementation \"%s\".", classname), e);
        }
        catch (ClassCastException e)
        {
            throw new UserError(String.format("\"%s\" is not an implementation of the base class.", classname), e);
        }
    }

    /**
     * Returns a new Nodemapper that requires no arguments.
     * 
     * @return a new Nodemapper
     */
    public T getNewInstance()
    {
        return Classes.getNewInstance(this._clazz, this._classname, EMPTY_ARGS);
    }

    /**
     * Returns a new Nodemapper using given arguments.
     * 
     * @param args arguments to pass to the Nodemapper constructor
     * @return a new Nodemapper
     */
    public T getNewInstance(Object[] args)
    {
        return Classes.getNewInstance(this._clazz, this._classname, args);
    }
}
