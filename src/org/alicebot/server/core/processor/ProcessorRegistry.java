/*
    Alicebot Program D
    Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

package org.alicebot.server.core.processor;

import java.lang.reflect.Field;
import java.util.Hashtable;

import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.DeveloperErrorException;
import org.alicebot.server.core.util.UserErrorException;


/**
 *  Registers {@link AIMLProcessor}s for a given version of AIML.
 */
public class ProcessorRegistry extends Hashtable
{
    /** The version of the content type for which this registry is intended. */
    protected static String version;

    /** The list of processors (fully-qualified class names). */
    protected static String[] processorList;

    /** The fully-qualified name of the processor. */
    protected static String processorBaseClassName;

    /** The name of the field in a processor that contains the label. */
    protected static String labelFieldName;


    /**
     *  Loads the registry with all processors for AIML elements.
     */
    public ProcessorRegistry(String version, String[] processorList,
                             String processorBaseClassName, String labelFieldName)
    {
        // Initialize the Hastable that this is.
        super(processorList.length);

        // Set the field values for this.
        this.version = version;
        this.processorList = processorList;
        this.processorBaseClassName = processorBaseClassName;
        this.labelFieldName = labelFieldName;

        // Get a handle on AIMLProcessor, the base class for all processors.
        Class baseClass = null;
        try
        {
            baseClass = Class.forName(processorBaseClassName);
        }
        catch (ClassNotFoundException e)
        {
            throw new UserErrorException("Could not find processor base class \"" + processorBaseClassName + "\"!", e);
        }

        // Load in the subclasses of the processor.
        Class processor;
        Field labelField;
        String label;
        for (int index = 0; index < processorList.length; index++)
        {
            // Get the class.
            processor = null;
            try
            {
                processor = Class.forName(processorList[index]);
            }
            catch (ClassNotFoundException e)
            {
                throw new UserErrorException("\"" + processorList[index] +
                             "\" is missing from your classpath.  Cannot initialize processor registry.", e);
            }

            // Ensure that the class is actually an extension of the processor.
            if (!baseClass.isAssignableFrom(processor))
            {
                throw new DeveloperErrorException("Developer has incorrectly specified \"" + processorList[index] +
                            "\" as a processor.");
            }

            // Get the label field.
            labelField = null;
            if (processor != null)
            {
                try
                {
                    labelField = processor.getDeclaredField(labelFieldName);
                }
                catch (NoSuchFieldException e)
                {
                    throw new DeveloperErrorException("Unlikely error: \"" + processorList[index] + "\" is missing field \"" +
                                labelFieldName + "\"!");
                }
            }
            else
            {
                throw new DeveloperErrorException("Failed to get processor \"" + processorList[index] + "\"");
            }

            // Get the value in the label field.
            label = null;
            if (labelField != null)
            {
                try
                {
                    label = (String)labelField.get(null);
                }
                catch (IllegalAccessException e)
                {
                    throw new DeveloperErrorException(e.getMessage());
                }
            }

            // (Finally!) register this class.
            if (label != null)
            {
                this.put(label, processor);
            }
        }
    }
}
