/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

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
        java.util.logging.Logger jdkLogger = java.util.logging.Logger.getLogger("");
        java.util.logging.Handler[] handlers = jdkLogger.getHandlers();
        for (int index = 0; index < handlers.length; index++)
        {
            jdkLogger.removeHandler(handlers[index]);
        }
        jdkLogger.addHandler(new JDKLogHandler());
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
        
        org.apache.log4j.spi.LoggingEvent event =
            new org.apache.log4j.spi.LoggingEvent(record.getSourceClassName(),
                    log4jLogger, record.getMillis(),
                    log4jLevel, record.getMessage(), record.getThrown());
        event.getLocationInformation();
        
        // Call the logger's appenders on a new event created based on the JDK log record.
        log4jLogger.callAppenders(event);
    }

    @Override
    public void flush()
    {
        // Nothing to do.
    }

    @Override
    public void close() throws SecurityException
    {
        // Nothing to do.
    }
}
