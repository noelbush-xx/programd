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
 */
abstract public class ClassRegistry
{
    /** The hidden Hashtable that backs this. */
    private Hashtable<String, Class> registry;
    
    /** The namespace URI of the content type for which this registry is intended. */
    protected String namespaceURI;

    /** The list of classesToRegister (fully-qualified class names). */
    protected String[] classes;

    /** The fully-qualified name of the base class for the registered classes. */
    protected String baseClassName;
    
    /** The actual Class represented by the {@link #baseClassName}. */
    protected Class<?> baseClass;

    /** The string &quot;label&quot;--the required name for a label field. */
    private static final String LABEL = "label";

    /**
     * Loads the registry with a set of classes.
     * @param namespaceURIToUse the namespace URI to use
     * @param classesToRegister the classes to register
     * @param baseClassNameToUse the base class name to use
     */
    public ClassRegistry(String namespaceURIToUse, String[] classesToRegister, String baseClassNameToUse)
    {
        // Initialize the backing Hashtable.
        this.registry = new Hashtable<String, Class>(classesToRegister.length);

        // Set the field values for this.
        this.namespaceURI = namespaceURIToUse;
        this.classes = classesToRegister;
        this.baseClassName = baseClassNameToUse;

        // Get a handle on the base class.
        try
        {
            this.baseClass = Class.forName(this.baseClassName);
        } 
        catch (ClassNotFoundException e)
        {
            throw new UserError("Could not find base class \"" + this.baseClassName + "\"!", e);
        } 

        // Load in the classesToRegister.
        for (int index = classesToRegister.length; --index >= 0;)
        {
        	register(classesToRegister[index]);
        }
    }
    
    /**
     * Registers an individual class.
     * 
     * @param nameOfClassToRegister	the name of the class to register
     */
    public void register(String nameOfClassToRegister)
    {
    	Class classToRegister;
    	
        // Get the class.
        try
        {
            classToRegister = Class.forName(nameOfClassToRegister);
        } 
        catch (ClassNotFoundException e)
        {
            throw new UserError("\"" + nameOfClassToRegister
                    + "\" is missing from your classpath.  Cannot initialize registry.", e);
        } 
        // Ensure that the class is actually an extension of the processor.
        if (!this.baseClass.isAssignableFrom(classToRegister))
        {
            throw new DeveloperError("Developer has incorrectly specified \"" + nameOfClassToRegister
                    + "\" as a registrable class.", new ClassCastException());
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
                throw new DeveloperError("Unlikely error: \"" + nameOfClassToRegister
                        + "\" is missing label field!", e);
            } 
        } 
        else
        {
            throw new DeveloperError("Failed to get processor \"" + nameOfClassToRegister + "\"", new NullPointerException());
        } 

        // Get the value in the label field.
        String label = null;
        try
        {
            label = (String) labelField.get(null);
        } 
        catch (IllegalAccessException e)
        {
            throw new DeveloperError("Label field for \"" + nameOfClassToRegister + "\" is not accessible!", e);
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
     * 
     * @return the Class corresponding to the given label.
     * @throws NotARegisteredClassException if the given class is not registered
     */
    public synchronized Class get(String label) throws NotARegisteredClassException
    {
        if (this.registry.containsKey(label))
        {
            return this.registry.get(label);
        }
        throw new NotARegisteredClassException(label);
    }
    
    /**
     * @return the namespace URI of the content type for which
     *         this registry manages processors
     */
    public String getNamespaceURI()
    {
        return this.namespaceURI;
    }
}