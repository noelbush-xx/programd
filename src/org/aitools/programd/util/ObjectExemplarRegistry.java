/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Similar to {@link ClassRegistry}, but registers object exemplars.
 * 
 * @param <B> the base class for objects that will be registered
 */
abstract public class ObjectExemplarRegistry<B>
 {
    /** The Hashtable that stores the objects. */
    protected Hashtable<String, B> registry;

    /**
     * Loads the registry with a set of exemplar objects.
     * 
     * @param classnames the classnames for which to register exemplar objects
     * @param constructorArguments the arguments for B's constructor
     */
    public ObjectExemplarRegistry(String[] classnames, Object... constructorArguments)
    {
        // Initialize the backing Hashtable.
        this.registry = new Hashtable<String, B>(classnames.length);

        ArrayList<Class> constructorArgumentClasses = new ArrayList<Class>();
        for (int index = 0; index < constructorArguments.length; index++)
        {
            constructorArgumentClasses.add(constructorArguments[index].getClass());
        }

        Class[] argumentClassesArray = constructorArgumentClasses.toArray(new Class[] {});

        // Create and register exemplar objects.
        for (int index = classnames.length; --index >= 0;)
        {
            register(classnames[index], constructorArguments, argumentClassesArray);
        }
    }

    /**
     * Registers an individual exemplar object.
     * 
     * @param classname the name of the object to register
     * @param constructorArguments the arguments to the constructor
     * @param constructorArgumentClasses the corresponding class of each
     *            argument
     */
    public void register(String classname, Object[] constructorArguments, Class[] constructorArgumentClasses)
    {
        Class<B> exemplarClass;

        // Get the class.
        try
        {
            exemplarClass = (Class<B>) Class.forName(classname);
        }
        catch (ClassNotFoundException e)
        {
            throw new UserError("\"" + classname + "\" is unavailable (not found in classpath).  Cannot initialize registry.", e);
        }
        catch (ClassCastException e)
        {
            throw new DeveloperError("Developer has incorrectly specified \"" + classname + "\" as a registrable class.", e);
        }

        Constructor<B> constructor;
        try
        {
            constructor = exemplarClass.getDeclaredConstructor(constructorArgumentClasses);
        }
        catch (NoSuchMethodException e)
        {
            throw new DeveloperError("Developed specified an invalid constructor for \"" + classname + "\".", e);
        }
        catch (SecurityException e)
        {
            throw new DeveloperError("Permission denied to create new \"" + classname + "\" with specified constructor.", e);
        }

        // Create an instance of the class.
        B exemplar;
        try
        {
            exemplar = constructor.newInstance(constructorArguments);
        }
        catch (IllegalAccessException e)
        {
            throw new DeveloperError("Underlying constructor for \"" + classname + "\" is inaccessible.", e);
        }
        catch (InstantiationException e)
        {
            throw new DeveloperError("Could not instantiate \"" + classname + "\".", e);
        }
        catch (IllegalArgumentException e)
        {
            throw new DeveloperError("Illegal argument exception when creating \"" + classname + "\".", e);
        }
        catch (InvocationTargetException e)
        {
            throw new DeveloperError("Constructor threw an exception when getting a \"" + classname + "\" instance from it.", e);
        }

        // Register the exemplar object.
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