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

import java.lang.reflect.Field;
import java.util.Hashtable;

import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.runtime.UserError;

/**
 * Registers classes with aliases.
 * 
 * @param <B> the base class for classes that will be registered
 */
abstract public class ClassRegistry<B>
{
    /** The Hashtable that stores the classes. */
    protected Hashtable<String, Class<? extends B>> registry;

    /** The string &quot;{@value}&quot;--the required name for a label field. */
    private static final String LABEL = "label";

    /**
     * Loads the registry with a set of classes.
     * 
     * @param classnames the names of the classes to register
     */
    public ClassRegistry(String[] classnames)
    {
        // Initialize the backing Hashtable.
        this.registry = new Hashtable<String, Class<? extends B>>(classnames.length);

        // Load in the classesToRegister.
        for (int index = classnames.length; --index >= 0;)
        {
            register(classnames[index]);
        }
    }

    /**
     * Registers an individual class.
     * 
     * @param classname the name of the class to register
     */
    @SuppressWarnings("unchecked")
    public void register(String classname)
    {
        Class<? extends B> classToRegister;

        // Get the class.
        try
        {
            classToRegister = (Class<? extends B>) Class.forName(classname);
        }
        catch (ClassNotFoundException e)
        {
            throw new UserError(String.format(
                    "\"%s\" is unavailable (not found in classpath).  Cannot initialize registry.", classname), e);
        }
        catch (ClassCastException e)
        {
            throw new DeveloperError(String.format(
                    "Developer has incorrectly specified \"%s\" as a registrable class.", classname), e);
        }

        // Get the label field.
        Field labelField = null;
        if (classToRegister != null)
        {
            try
            {
                labelField = classToRegister.getDeclaredField(LABEL);
            }
            catch (NoSuchFieldException e)
            {
                throw new DeveloperError(String.format("Unlikely error: \"%s\" is missing label field!", classname), e);
            }
        }
        else
        {
            throw new DeveloperError(String.format("Failed to get processor \"%s\"", classname),
                    new NullPointerException());
        }

        // Get the value in the label field.
        String label = null;
        try
        {
            label = (String) labelField.get(null);
        }
        catch (IllegalAccessException e)
        {
            throw new DeveloperError(String.format("Label field for \"%s\" is not accessible!", classname), e);
        }

        // (Finally!) register this class.
        if (label != null)
        {
            this.registry.put(label, classToRegister);
        }
        else
        {
            throw new DeveloperError("Tried to register class with null label!", new NullPointerException());
        }
    }

    /**
     * A wrapper for the internal Hashtable's get method.
     * 
     * @param label the label of the Class desired.
     * @return the Class corresponding to the given label.
     */
    public synchronized Class<? extends B> get(String label)
    {
        if (label != null)
        {
            if (this.registry.containsKey(label))
            {
                return this.registry.get(label);
            }
            throw new NullPointerException(String.format("Class registry does not contain label \"%s\".", label));
        }
        throw new NullPointerException("Passed a null label to ClassRegistry!");
    }
}
