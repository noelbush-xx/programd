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

abstract public class Settings
{
    /** The properties. */
    protected Properties properties;

    public Settings()
    {
        this.properties = new Properties();
        initialize();
    }

    public Settings(String propertiesPath)
    {
        try
        {
            this.properties.loadFromXML(new FileInputStream(propertiesPath));
        }
        catch (InvalidPropertiesFormatException e)
        {
            throw new UserError("Invalid properties format: ", e);
        }
        catch (IOException e)
        {
            throw new DeveloperError("I/O Exception while loading properties: ", e);
        }
        initialize();
    }

    /**
     * Initializes the Settings object with values from properties as read, or
     * defaults (if properties are not provided).
     */
    abstract protected void initialize();
}
