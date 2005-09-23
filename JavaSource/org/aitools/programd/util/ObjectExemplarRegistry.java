/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

import java.util.Hashtable;

/**
 * Similar to {@link ClassRegistry}, but registers object exemplars.
 * 
 * @param <B> the base class for objects that will be registered
 */
abstract public class ObjectExemplarRegistry<B>
 {
    /** The type of B (why, oh why, should I have to do this? */
    private Class<B> type;
    
    /** The Hashtable that stores the objects. */
    protected Hashtable<String, B> registry;

    /**
     * Loads the registry with a set of exemplar objects.
     * 
     * @param classnames the classnames for which to register exemplar objects
     * @param objectType the type of B (seems stupid to have to pass this in, but necessary to avoid some compiler errors)
     * @param constructorArguments the arguments for B's constructor
     */
    public ObjectExemplarRegistry(String[] classnames, Class<B> objectType, Object... constructorArguments)
    {
        this.type = objectType;
        
        // Initialize the backing Hashtable.
        this.registry = new Hashtable<String, B>(classnames.length);

        // Create and register exemplar objects.
        for (int index = classnames.length; --index >= 0;)
        {
            register(classnames[index], constructorArguments);
        }
    }

    /**
     * Registers an individual exemplar object.
     * 
     * @param classname the name of the object to register
     * @param constructorArguments the arguments to the constructor
     */
    public void register(String classname, Object ... constructorArguments)
    {
        // Create an instance of the class.
        B exemplar = ClassUtils.getSubclassInstance(this.type, classname, classname, constructorArguments);

        // Register an instance of the class.
        this.registry.put(classname, exemplar);
    }

    /**
     * A wrapper for the internal HashSet's get method.
     * 
     * @param classname the classname for which an exemplar object is desired
     * @return the registered exemplar object corresponding to the classname
     * @throws NotARegisteredObjectException if the given class is not
     *             registered
     */
    public synchronized B get(String classname) throws NotARegisteredObjectException
    {
        if (this.registry.containsKey(classname))
        {
            return this.registry.get(classname);
        }
        throw new NotARegisteredObjectException(classname);
    }
}