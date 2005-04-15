/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

import java.lang.reflect.Field;
import java.util.Hashtable;

/**
 * Registers classes with aliases.
 * 
 * @param <B> the base class for classes that will be registered
 */
abstract public class ClassRegistry<B>
 {
    /** The Hashtable that stores the classes. */
    protected Hashtable<String, Class< ? extends B>> registry;

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
        this.registry = new Hashtable<String, Class< ? extends B>>(classnames.length);

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
    public void register(String classname)
    {
        Class< ? extends B> classToRegister;

        // Get the class.
        try
        {
            classToRegister = (Class< ? extends B>) Class.forName(classname);
        }
        catch (ClassNotFoundException e)
        {
            throw new UserError("\"" + classname + "\" is unavailable (not found in classpath).  Cannot initialize registry.", e);
        }
        catch (ClassCastException e)
        {
            throw new DeveloperError("Developer has incorrectly specified \"" + classname + "\" as a registrable class.", e);
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
                throw new DeveloperError("Unlikely error: \"" + classname + "\" is missing label field!", e);
            }
        }
        else
        {
            throw new DeveloperError("Failed to get processor \"" + classname + "\"", new NullPointerException());
        }

        // Get the value in the label field.
        String label = null;
        try
        {
            label = (String) labelField.get(null);
        }
        catch (IllegalAccessException e)
        {
            throw new DeveloperError("Label field for \"" + classname + "\" is not accessible!", e);
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
     * @throws NotARegisteredClassException if the given class is not registered
     */
    public synchronized Class<? extends B> get(String label) throws NotARegisteredClassException
    {
        if (label != null)
        {
            if (this.registry.containsKey(label))
            {
                return this.registry.get(label);
            }
            throw new NotARegisteredClassException(label);
        }
        throw new DeveloperError("Passed a null label to ClassRegistry!", new NullPointerException());
    }
}