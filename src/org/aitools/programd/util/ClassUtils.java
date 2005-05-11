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

/**
 * Contains utilities related to manipulating classes.
 *
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.5
 */
public class ClassUtils
{
    /**
     * Returns the class which is a subclass of <code>T</code>,
     * instantiated using a constructor that takes the arguments given.
     * @param classname the classname to instantiate
     * @param <T> the class of which the instantiated class must be a subclass
     * @param baseType the base class type
     * @param description a short (one word or so) description of the desired class
     * @param constructorArgs the arguments to the constructor (actual arguments, not types)
     * @return the desired class
     */
    public static <T> T getSubclassInstance(Class<T> baseType, String classname, String description, Object ... constructorArgs)
    {
        // Get the subclass.
        Class<? extends T> subclass = null;
        try
        {
            subclass = (Class<? extends T>) Class.forName(classname);
        }
        catch (ClassNotFoundException e)
        {
            throw new UserError("Specified " + description + " (\"" + classname + "\") could not be found.", e);
        }
        catch (ClassCastException e)
        {
            throw new UserError("\"" + classname
                    + "\" is not a " + description + "subclass.", e);
        }
        
        return getNewInstance(subclass, description, constructorArgs);
    }
    
    /**
     * Returns an instance of the given class,
     * instantiated using a constructor that takes the arguments given.
     * @param <T> the type of the class
     * @param theClass the class to instantiate
     * @param description a short (one word or so) description of the desired class
     * @param constructorArgs the arguments to the constructor (actual arguments, not types)
     * @return the desired class
     */
    public static <T> T getNewInstance(Class<T> theClass, String description, Object ... constructorArgs)
    {
        // Get the types of the arguments.
        int argCount = constructorArgs.length;
        ArrayList<Class> argumentTypes = new ArrayList<Class>(argCount);
        for (int index = 0; index < argCount; index++)
        {
             argumentTypes.add(constructorArgs[index].getClass());
        }

        // Get the constructor that takes the given argument types.
        Constructor<T> constructor = null;
        try
        {
            constructor = theClass.getConstructor(argumentTypes.toArray(new Class[]{}));
        }
        catch (NoSuchMethodException e)
        {
            throw new DeveloperError("Developer specified an invalid constructor for " + description + ".",
                    e);
        }
        catch (SecurityException e)
        {
            throw new DeveloperError(
                    "Permission denied to create new " + description + " with specified constructor.", e);
        }

        // Get a new instance of the class.
        try
        {
            return constructor.newInstance(constructorArgs);
        }
        catch (IllegalAccessException e)
        {
            throw new DeveloperError("Underlying constructor for " + description + " is inaccessible.", e);
        }
        catch (InstantiationException e)
        {
            throw new DeveloperError("Could not instantiate " + description + ".", e);
        }
        catch (IllegalArgumentException e)
        {
            throw new DeveloperError("Illegal argument exception when creating " + description + ".", e);
        }
        catch (InvocationTargetException e)
        {
            throw new DeveloperError(
                    "Constructor threw an exception when getting a " + description + " instance from it.",
                    e.getTargetException());
        }
    }
}
