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

package org.aitools.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.runtime.UserError;

/**
 * Contains utilities related to manipulating classes.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.5
 */
public class Classes
{
    /**
     * Returns the class which is a subclass of <code>T</code>, instantiated using a constructor that takes the
     * arguments given.
     * 
     * @param classname the classname to instantiate
     * @param <T> the class of which the instantiated class must be a subclass
     * @param baseType the base class type
     * @param description a short (one word or so) description of the desired class
     * @param constructorArgs the arguments to the constructor (actual arguments, not types)
     * @return the desired class
     */
    @SuppressWarnings("unchecked")
    public static <T> T getSubclassInstance(Class<T> baseType, String classname, String description, Object... constructorArgs)
    {
        // Get the subclass.
        Class<? extends T> subclass = null;
        try
        {
            subclass = (Class<? extends T>) Class.forName(classname);
        }
        catch (ClassNotFoundException e)
        {
            throw new UserError(String.format("Specified %s (\"%s\") could not be found.", description, classname), e);
        }
        catch (ClassCastException e)
        {
            throw new UserError(String.format("\"%s\" is not a %s subclass.", classname, description), e);
        }

        return getNewInstance(subclass, description, constructorArgs);
    }

    /**
     * Convenience wrapper for {@link Classes#getSubclassInstance(Class, String, String, Object...)}
     * that does not require a description and assumes zero constructor arguments.
     * 
     * @param classname the classname to instantiate
     * @param <T> the class of which the instantiated class must be a subclass
     * @param baseType the base class type
     * @return the desired class
     */
    public static <T> T getSubclassInstance(Class<T> baseType, String classname)
    {
        return getSubclassInstance(baseType, classname, "[no description]", new Object[]{});
    }
    
    /**
     * Returns an instance of the given class, instantiated using a constructor that takes the arguments given.
     * 
     * @param <T> the type of the class
     * @param theClass the class to instantiate
     * @param description a short (one word or so) description of the desired class
     * @param constructorArgs the arguments to the constructor (actual arguments, not types)
     * @return the desired class
     */
    @SuppressWarnings("unchecked")
    public static <T> T getNewInstance(Class<T> theClass, String description, Object... constructorArgs)
    {
        // Get the types of the arguments.
        int argCount = constructorArgs.length;
        ArrayList<Class<T>> argumentTypes = new ArrayList<Class<T>>(argCount);
        for (int index = 0; index < argCount; index++)
        {
            try
            {
                argumentTypes.add((Class<T>) constructorArgs[index].getClass());
            }
            catch (ClassCastException e)
            {
                throw new DeveloperError(String.format("Invalid arguments provided for constructor to create new %s.",
                        description), e);
            }
        }

        // Get the constructor that takes the given argument types.
        Constructor<T> constructor = null;
        try
        {
            constructor = theClass.getConstructor(argumentTypes.toArray(new Class[] {}));
        }
        catch (NoSuchMethodException e)
        {
            throw new DeveloperError(String.format("Developer specified an invalid constructor for %s.", description),
                    e);
        }
        catch (SecurityException e)
        {
            throw new DeveloperError(String.format("Permission denied to create new %s with specified constructor.",
                    description), e);
        }

        // Get a new instance of the class.
        try
        {
            return constructor.newInstance(constructorArgs);
        }
        catch (IllegalAccessException e)
        {
            throw new DeveloperError(String.format("Underlying constructor for %s is inaccessible.", description), e);
        }
        catch (InstantiationException e)
        {
            throw new DeveloperError(String.format("Could not instantiate %s.", description), e);
        }
        catch (IllegalArgumentException e)
        {
            throw new DeveloperError(String.format("Illegal argument exception when creating %s.", description), e);
        }
        catch (InvocationTargetException e)
        {
            throw new DeveloperError(String.format(
                    "Constructor threw an exception when getting a %s instance from it.", description), e
                    .getTargetException());
        }
    }
}
