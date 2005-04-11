/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.aitools.programd.util.FileManager;

/**
 * A Settings object can read properties from a given path, or initialize itself
 * with default values. It also contains getter and setter methods for every
 * property value. Usually the subclasses of Settings will be generated
 * automatically from some other structure, such as (as currently) the
 * properties file itself, since it's annoying to create and maintain these by
 * hand.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
abstract public class Settings
{
    /** The properties. */
    protected Properties properties;

    /**
     * Creates a new Settings object, initializing it with default values.
     */
    public Settings()
    {
        this.properties = new Properties();
        initialize();
    }

    /**
     * Creates a new Settings object, filling it with properties read from the
     * given path.
     * 
     * @param propertiesPath the path to the properties file
     */
    public Settings(String propertiesPath)
    {
        this.properties = new Properties();
        try
        {
            this.properties.loadFromXML(new FileInputStream(FileManager.getExistingFile(propertiesPath)));
        }
        catch (InvalidPropertiesFormatException e)
        {
            throw new UserError("Invalid properties format: ", e);
        }
        catch (IOException e)
        {
            throw new DeveloperError("I/O Exception while loading properties: ", e);
        }
        catch (NullPointerException e)
        {
            throw new DeveloperError("Unable to open FileInputStream for \"" + propertiesPath + "\".", e);
        }
        catch (ClassCastException e)
        {
            throw new UserError(
                    "There is a problem with the format of your XML properties file.  Not all valid XML is actually accepted by the JDK's XML Properties parser!",
                    e);
        }
        initialize();
    }

    /**
     * Initializes the Settings object with values from properties as read, or
     * defaults (if properties are not provided).
     */
    abstract protected void initialize();
}
