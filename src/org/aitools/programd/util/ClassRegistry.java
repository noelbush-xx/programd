/*    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

package org.aitools.programd.util;

import java.lang.reflect.Field;
import java.util.Hashtable;


/**
 *  Registers classes with aliases.
 */
abstract public class ClassRegistry extends Hashtable
{
    /** The version of the content type for which this registry is intended. */
    protected static String version;

    /** The list of classesToRegister (fully-qualified class names). */
    protected static String[] classesToRegister;

    /** The fully-qualified name of the base class for the classesToRegister. */
    protected static String baseClassName;

    /** The string &quot;label&quot;--the required name for a label field. */
    private static final String LABEL = "label";

    /**
     *  Loads the registry with all classes.
     */
    public ClassRegistry(
        String versionToUse,
        String[] classesToRegisterToUse,
        String baseClassNameToUse)
    {
        // Initialize the Hastable that this is.
        super(classesToRegisterToUse.length);

        // Set the field values for this.
        ClassRegistry.version = versionToUse;
        ClassRegistry.classesToRegister = classesToRegisterToUse;
        ClassRegistry.baseClassName = baseClassNameToUse;

        // Get a handle on the base class.
        Class baseClass = null;
        try
        {
            baseClass = Class.forName(baseClassName);
        }
        catch (ClassNotFoundException e)
        {
            throw new UserError(
                "Could not find base class \"" + baseClassName + "\"!",
                e);
        }

        // Load in the classesToRegister.
        Class classToRegister;

        for (int index = classesToRegister.length; --index >= 0;)
        {
            // Get the class.
            try
            {
                classToRegister = Class.forName(classesToRegister[index]);
            }
            catch (ClassNotFoundException e)
            {
                throw new UserError(
                    "\""
                        + classesToRegister[index]
                        + "\" is missing from your classpath.  Cannot initialize registry.",
                    e);
            }
            // Ensure that the class is actually an extension of the processor.
            if (!baseClass.isAssignableFrom(classToRegister))
            {
                throw new DeveloperError(
                    "Developer has incorrectly specified \""
                        + classesToRegister[index]
                        + "\" as a registrable class.");
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
                    throw new DeveloperError(
                        "Unlikely error: \""
                            + classesToRegister[index]
                            + "\" is missing label field!");
                }
            }
            else
            {
                throw new DeveloperError(
                    "Failed to get processor \""
                        + classesToRegister[index]
                        + "\"");
            }

            // Get the value in the label field.
            String label = null;
            try
            {
                label = (String) labelField.get(null);
            }
            catch (IllegalAccessException e)
            {
                throw new DeveloperError(
                    "Label field for \""
                        + classesToRegister[index]
                        + "\" is not accessible!");
            }

            // (Finally!) register this class.
            if (label != null)
            {
                this.put(label, classToRegister);
            }
            else
            {
                throw new DeveloperError("Tried to register class with null label!");
            }
        }
    }
}
