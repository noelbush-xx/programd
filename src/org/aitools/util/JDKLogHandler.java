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

/**
 * This handler takes a log message sent to the JDK's logging
 * system and sends it to log4j.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class JDKLogHandler extends java.util.logging.Handler
{
    /**
     * Removes any handlers attached to the JDK root logger,
     * and adds an instance of this.
     */
    public static void setupInterception()
    {
        replaceHandlers(java.util.logging.Logger.getLogger(""), new JDKLogHandler());
    }
    
    private static void replaceHandlers(java.util.logging.Logger jdkLogger, JDKLogHandler handler)
    {
        java.util.logging.Handler[] handlers = jdkLogger.getHandlers();
        for (int index = 0; index < handlers.length; index++)
        {
            jdkLogger.removeHandler(handlers[index]);
        }
        jdkLogger.addHandler(handler);
    }

    /**
     * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
     */
    @Override
    public void publish(java.util.logging.LogRecord record)
    {
        // Translate the level to log4j.
        java.util.logging.Level jdkLevel = record.getLevel();
        org.apache.log4j.Level log4jLevel;
        if (jdkLevel == java.util.logging.Level.SEVERE)
        {
            log4jLevel = org.apache.log4j.Level.ERROR;
        }
        else if (jdkLevel == java.util.logging.Level.WARNING)
        {
            log4jLevel = org.apache.log4j.Level.WARN;
        }
        else if (jdkLevel == java.util.logging.Level.INFO || jdkLevel == java.util.logging.Level.CONFIG)
        {
            log4jLevel = org.apache.log4j.Level.INFO;
        }
        else if (jdkLevel == java.util.logging.Level.FINE)
        {
            log4jLevel = org.apache.log4j.Level.DEBUG;
        }
        else if (jdkLevel == java.util.logging.Level.FINER || jdkLevel == java.util.logging.Level.FINEST)
        {
            log4jLevel = org.apache.log4j.Level.TRACE;
        }
        else
        {
            assert false : String.format("Unknown JDK logging level %s.", jdkLevel);
            return;
        }

        // Get the log4j logger.
        String loggerName = record.getLoggerName();
        org.apache.log4j.Logger log4jLogger = org.apache.log4j.Logger.getLogger(loggerName);

        org.apache.log4j.spi.LoggingEvent event = new org.apache.log4j.spi.LoggingEvent(record.getSourceClassName(),
                log4jLogger, record.getMillis(), log4jLevel, record.getMessage(), record.getThrown());
        event.getLocationInformation();

        // Call the logger's appenders on a new event created based on the JDK log record.
        log4jLogger.callAppenders(event);
    }

    /**
     * @see java.util.logging.Handler#flush()
     */
    @Override
    public void flush()
    {
        // Nothing to do.
    }

    /**
     * @see java.util.logging.Handler#close()
     */
    @Override
    public void close() throws SecurityException
    {
        // Nothing to do.
    }
}
