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

/*
    Code cleanup (4.1.3 [00] - October 2001, Noel Bush)
    - formatting cleanup
    - general grammar fixes
    - complete javadoc
    - made all imports explicit
    - made log locations fully configurable from server.properties
    - changed log method to use regularized formatter in org.alicebot.server.core.logging.MessagePrinter;
*/

/*
    More fixes (4.1.3 [02] - November 2001, Noel Bush)
    - changed some server property names
    - added methods for logging to multiple files
    - added more javadoc for constants
*/

package org.alicebot.server.core.logging;

import java.io.FileWriter;
import java.io.IOException;

import org.alicebot.server.core.Globals;
import org.alicebot.server.core.util.Toolkit;

/**
 *  Handles logging of Alicebot events to log files.
 *
 *  @author Jon Baer
 *  @author Thomas Ringate, Pedro Colla
 *  @author Noel Bush
 *  @version 4.1.3
 */
public class Log
{
    // Log name constants.

    /** Chat log path. */
    public static final String CHAT        = Globals.getProperty("programd.logging.chat.path", "./logs/chat.log");

    /** Database log path. */
    public static final String DATABASE    = Globals.getProperty("programd.logging.database.path", "./logs/database.log");

    /** Error log path. */
    public static final String ERROR       = Globals.getProperty("programd.logging.error.path", "./logs/error.log");

    /** Gossip log path. */
    public static final String GOSSIP      = Globals.getProperty("programd.logging.gossip.path", "./logs/gossip.log");

    /** Interpreter log path. */
    public static final String INTERPRETER = Globals.getProperty("programd.logging.interpreter.path", "./logs/interpreter.log");

    /** Learn log path. */
    public static final String LEARN       = Globals.getProperty("programd.logging.learn.path", "./logs/learn.log");

    /** Merge log path. */
    public static final String MERGE       = Globals.getProperty("programd.logging.merge.path", "./logs/merge.log");

    /** Startup log path. */
    public static final String STARTUP     = Globals.getProperty("programd.logging.startup.path", "./logs/startup.log");

    /** OS shell use log path. */
    public static final String SYSTEM      = Globals.getProperty("programd.logging.system.path", "./logs/system.log");

    /** Targeting log path. */
    public static final String TARGETING   = Globals.getProperty("programd.logging.targeting.path", "./logs/targeting.log");

    /** The object used to write to a file. */
    private static FileWriter fileWriter;

    /** The phrase &quot;log file&quot;. */
    private static final String LOGFILE = "log file";
    

    /**
     *  Writes a message to the appropriate logfile.
     *
     *  @param message  the text of the log event
     *  @param type     the type of the log event
                    (one of <code>ACCESS</code>, <code>CHAT</code>, <code>DATABASE</code>, 
                    <code>DEBUG</code>, <code>ERROR</code>, <code>EVENT</code>, 
                    <code>GOSSIP</code>, <code>INTERPRETER</code>, <code>MERGE</code>, 
                    <code>SERVLET</code>, <code>STARTUP</code>, <code>SYSTEM</code>, <code>TARGETING</code>)
     */
    public static void log(String message, String type)
    {
        // Check if the file exists.
        Toolkit.checkOrCreate(type, LOGFILE);
        
        // Get a FileWriter to the file.
        try
        {
            fileWriter = new FileWriter(type, true);
        }
        catch (IOException e)
        {
            Trace.userfail("Could not create log file \"" + type + "\".");
        }
        MessagePrinter.println(message, Trace.NO_FLAG, fileWriter, MessagePrinter.LOG);
    }

    
    /**
     *  Writes a multiline log message to the appropriate logfile.
     *  @param message  the text of the log event
     *  @param type     the type of the log event
                    (one of <code>ACCESS</code>, <code>CHAT</code>, <code>DATABASE</code>, 
                    <code>DEBUG</code>, <code>ERROR</code>, <code>EVENT</code>, 
                    <code>GOSSIP</code>, <code>INTERPRETER</code>, <code>MERGE</code>, 
                    <code>SERVLET</code>, <code>STARTUP</code>, <code>SYSTEM</code>, <code>TARGETING</code>)
     */
    public static void log(String[] message, String type)
    {
        int lineCount = message.length;
        for (int index = 0; index < lineCount; index++)
        {
            log(message[index], type);
        }
    }


    /**
     *  Logs a message <i>and</i> prints it to the console as a user info message.
     *
     *  @param message  the text of the log event
     *  @param type     the type of the log event
                    (one of <code>ACCESS</code>, <code>CHAT</code>, <code>DATABASE</code>, 
                    <code>DEBUG</code>, <code>ERROR</code>, <code>EVENT</code>, 
                    <code>GOSSIP</code>, <code>INTERPRETER</code>, <code>MERGE</code>, 
                    <code>SERVLET</code>, <code>STARTUP</code>, <code>SYSTEM</code>)
    */
    public static void userinfo(String message, String type)
    {
        Trace.userinfo(message);
        log(message, type);
    }


    /**
     *  Same as {@link #userinfo(String, String)}, but
     *  logs to multiple log files.
     *  @param message  the text of the log event
     *  @param types    the log types
     */
    public static void userinfo(String message, String[] types)
    {
        int lastType = types.length - 1;
        for (int index = 0; index < lastType; index++)
        {
            log(message, types[index]);
        }
        userinfo(message, types[lastType]);
    }


    /**
     *  Logs a multiline message <i>and</i> prints it to the console as a user info message.
     *
     *  @param message  the text of the log event
     *  @param type     the type of the log event
                    (one of <code>ACCESS</code>, <code>CHAT</code>, <code>DATABASE</code>, 
                    <code>DEBUG</code>, <code>ERROR</code>, <code>EVENT</code>, 
                    <code>GOSSIP</code>, <code>INTERPRETER</code>, <code>MERGE</code>, 
                    <code>SERVLET</code>, <code>STARTUP</code>, <code>SYSTEM</code>)
    */
    public static void userinfo(String[] message, String type)
    {
        int lineCount = message.length;
        for (int index = 0; index < lineCount; index++)
        {
            Trace.userinfo(message[index]);
            log(message[index], type);
        }
    }


    /**
     *  Same as {@link #userinfo(String[], String)}, but
     *  logs to multiple log files.
     *  @param message  the text of the log event
     *  @param types    the log types
     */
    public static void userinfo(String[] message, String[] types)
    {
        int lastType = types.length - 1;
        for (int index = 0; index < lastType; index++)
        {
            log(message, types[index]);
        }
        userinfo(message, types[lastType]);
    }


    /**
     *  Logs a message <i>and</i> prints it to the console as a user failure message.
     *
     *  @param message  the text of the log event
     *  @param type     the type of the log event
                    (one of <code>ACCESS</code>, <code>CHAT</code>, <code>DATABASE</code>, 
                    <code>DEBUG</code>, <code>ERROR</code>, <code>EVENT</code>, 
                    <code>GOSSIP</code>, <code>INTERPRETER</code>, <code>MERGE</code>, 
                    <code>SERVLET</code>, <code>STARTUP</code>, <code>SYSTEM</code>)
    */
    public static void userfail(String message, String type)
    {
        log(message, type);
        Trace.userfail(message);
    }


    /**
     *  Same as {@link #userfail(String, String)}, but
     *  logs to multiple log files.
     *  @param message  the text of the log event
     *  @param types    the log types
     */
    public static void userfail(String message, String[] types)
    {
        int lastType = types.length - 1;
        for (int index = 0; index < lastType; index++)
        {
            log(message, types[index]);
        }
        userfail(message, types[lastType]);
    }


    /**
     *  Logs a message <i>and</i> prints it to the console as a developer info message.
     *
     *  @param message  the text of the log event
     *  @param type     the type of the log event
                    (one of <code>ACCESS</code>, <code>CHAT</code>, <code>DATABASE</code>, 
                    <code>DEBUG</code>, <code>ERROR</code>, <code>EVENT</code>, 
                    <code>GOSSIP</code>, <code>INTERPRETER</code>, <code>MERGE</code>, 
                    <code>SERVLET</code>, <code>STARTUP</code>, <code>SYSTEM</code>)
    */
    public static void devinfo(String message, String type)
    {
        Trace.devinfo(message);
        log(message, type);
    }


    /**
     *  Same as {@link #devinfo(String, String)}, but
     *  logs to multiple log files.
     *  @param message  the text of the log event
     *  @param types    the log types
     */
    public static void devinfo(String message, String[] types)
    {
        int lastType = types.length - 1;
        for (int index = 0; index < lastType; index++)
        {
            log(message, types[index]);
        }
        devinfo(message, types[lastType]);
    }


    /**
     *  Logs a message <i>and</i> prints it to the console as a developer failure message.
     *
     *  @param message  the text of the log event
     *  @param type     the type of the log event
                    (one of <code>ACCESS</code>, <code>CHAT</code>, <code>DATABASE</code>, 
                    <code>DEBUG</code>, <code>ERROR</code>, <code>EVENT</code>, 
                    <code>GOSSIP</code>, <code>INTERPRETER</code>, <code>MERGE</code>, 
                    <code>SERVLET</code>, <code>STARTUP</code>, <code>SYSTEM</code>)
    */
    public static void devfail(String message, String type)
    {
        Trace.devfail(message);
        log(message, type);
    }


    /**
     *  Same as {@link #devfail(String, String)}, but
     *  logs to multiple log files.
     *  @param message  the text of the log event
     *  @param types    the log types
     */
    public static void devfail(String message, String[] types)
    {
        int lastType = types.length - 1;
        for (int index = 0; index < lastType; index++)
        {
            log(message, types[index]);
        }
        devfail(message, types[lastType]);
    }
}

