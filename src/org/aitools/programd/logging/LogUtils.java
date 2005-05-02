/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.logging;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.aitools.programd.util.FileManager;
import org.aitools.programd.util.UserError;

/**
 * Contains utilities for logging.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.5
 */
public class LogUtils
{
    /**
     * Sets up a Logger in a standard way. (A FileHandler is attached with some
     * generic settings.)
     * 
     * @param name the name of the logger
     * @param pattern the pattern for the determining the logger's file output
     *            file
     * @param timestampFormat the timestamp format to use
     * @return the Logger that was set up.
     */
    public static Logger setupLogger(String name, String pattern, String timestampFormat)
    {
        Logger newLogger = Logger.getLogger(name);
        FileManager.checkOrCreateDirectory((new File(pattern)).getParent(), "log file directory");
        FileHandler newHandler = null;
        try
        {
            newHandler = new FileHandler(pattern, 1048576, 10, true);
        }
        catch (IOException e)
        {
            throw new UserError("I/O Error setting up a logger: ", e);
        }
        newHandler.setFormatter(new SimpleFormatter(timestampFormat));
        newLogger.addHandler(newHandler);
        return newLogger;
    }
}
