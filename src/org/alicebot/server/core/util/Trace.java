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
    More fixes (4.1.3 [02] - November 2001, Noel Bush)
    - changed some server property names
*/

/*
    4.1.4 [00] - December 2001, Noel Bush
    - added use of StackParser to devinfo and devfail methods
    - moved to util package
*/

package org.alicebot.server.core.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.StringTokenizer;

import org.alicebot.server.core.Globals;


/**
 *  Notifies of message formatting to the console for tracing and debugging.
 *  There are a lot of very similar, trivial methods here; the only goal
 *  is to save some typing for the developer.
 *
 *  @version 4.1.3
 *  @author Noel Bush
 */
public class Trace
{
    /*
        General settings for what to show.
        Note that in any case one may want to check Globals values in
        the code to decide whether to perform some extra calculations,
        but these runtime constants will be used within Trace to decide
        whether to obey a call.
    */

    /** Whether to MessagePrinter.println general user information to the console (if available). */
    private static final boolean EMIT_USER_INFO        = Globals.showConsole();

    /** Whether to MessagePrinter.println user errors to the console (if available). */
    private static final boolean EMIT_USER_ERRORS      = EMIT_USER_INFO;

    /** Whether to MessagePrinter.println general developer info to the console (if available). */
    private static final boolean EMIT_DEVELOPER_INFO   = EMIT_USER_INFO ? Boolean.valueOf(Globals.getProperty("programd.console.developer", "false")).booleanValue() : false;

    /** Whether to print caller method names for <i>all</i> messages (not just dev*). */
    private static final boolean METHOD_NAMES_ALWAYS   = EMIT_DEVELOPER_INFO ? Boolean.valueOf(Globals.getProperty("programd.console.developer.method-names-always", "false")).booleanValue() : false;

    /** Whether to MessagePrinter.println developer errors to the console (if available). */
    private static final boolean EMIT_DEVELOPER_ERRORS = EMIT_DEVELOPER_INFO;

    /** Whether to use message type flags. */
    private static final boolean SHOW_MESSAGE_FLAGS    = Boolean.valueOf(Globals.getProperty("programd.console.message-flags", "true")).booleanValue();

    /** The package name of this class. */
    private static final String THIS_PACKAGE = "org.alicebot.server.core.util.";


    //  Built-in messages.

    /** The fatal user error message when user error messages are turned off. */
    private static final String FATAL_USER_UNINFORMATIVE      = "Fatal user error. User error messages are turned off.";

    /** The fatal user error message when user error messages are turned off. */
    private static final String FATAL_DEVELOPER_UNINFORMATIVE = "Fatal developer error. Developer error messages are turned off.";

    /** The exiting message. */
    private static final String EXITING = "Exiting.";


    // Message type flags.

    /** No flag (an empty string). */
    public static final String NO_FLAG = "";

    /** Flag for a program message to the console. */
    public static final String PROGRAM_MESSAGE = SHOW_MESSAGE_FLAGS ? "P " : NO_FLAG;

    /** Flag for a user info message to the console. */
    public static final String USER_INFO       = SHOW_MESSAGE_FLAGS ? "u " : NO_FLAG;

    /** Flag for a user failure message to the console. */
    public static final String USER_ERROR      = SHOW_MESSAGE_FLAGS ? "U " : NO_FLAG;

    /** Flag for a developer info message to the console. */
    public static final String DEVELOPER_INFO  = SHOW_MESSAGE_FLAGS ? "d " : NO_FLAG;

    /** Flag for a developer failure message to the console. */
    public static final String DEVELOPER_ERROR = SHOW_MESSAGE_FLAGS ? "D " : NO_FLAG;

    /** Flag for an insistent message. */
    public static final String INSIST          = SHOW_MESSAGE_FLAGS ? "! " : NO_FLAG;

    /** &quot;: &quot;. */
    public static final String COLON_SPACE     = ": ";


    /** Output stream -- defaults to System.out. */
    private static PrintStream outStream = System.out;


    /**
     *  Changes the output stream.
     *
     *  @param stream
     */
    public static void setOut(PrintStream out)
    {
        outStream = out;
    }


    /**
     *  Notifies of user failures from which recovery is impossible.
     *
     *  @param message
     */
    public static void userfail(String message)
    {
        if (EMIT_USER_ERRORS)
        {
            if (!METHOD_NAMES_ALWAYS)
            {
                emit(message, USER_ERROR);
            }
            else
            {
                emit(StackParser.getStackMethodBefore(THIS_PACKAGE, true) + COLON_SPACE + message, USER_ERROR);
            }
        }
        else
        {
            emit(FATAL_USER_UNINFORMATIVE, USER_ERROR);
        }
    }
    
    
    /**
     *  Notifies of user failures from which recovery is impossible.
     *
     *  @param exception
     */
    public static void userfail(Throwable e)
    {
        if (EMIT_USER_ERRORS)
        {
            emit(e, USER_ERROR);
        }
        else
        {
            emit(FATAL_USER_UNINFORMATIVE, USER_ERROR);
        }
    }
    
    
    /**
     *  Prints informative message to user.
     *
     *  @param message
     */
    public static void userinfo(String message)
    {
        if (EMIT_USER_INFO)
        {
            if (!METHOD_NAMES_ALWAYS)
            {
                emit(message, USER_INFO);
            }
            else
            {
                emit(StackParser.getStackMethodBefore(THIS_PACKAGE, true) + COLON_SPACE + message, USER_INFO);
            }
        }
    }
    
    
    /**
     *  Prints multi-line informative message to user.
     *
     *  @param message
     */
    public static void userinfo(String[] message)
    {
        if (EMIT_USER_INFO)
        {
            if (!METHOD_NAMES_ALWAYS)
            {
                emit(message, USER_INFO);
            }
            else
            {
                emit(message, StackParser.getStackMethodBefore(THIS_PACKAGE, true) + COLON_SPACE, USER_INFO);
            }
        }
    }

    
    /**
     *  Handle developer failure from which recovery is impossible.
     *
     *  @param message
     */
    public static void devfail(String message)
    {
        if (EMIT_DEVELOPER_ERRORS)
        {
            emit(StackParser.getStackMethodBefore(THIS_PACKAGE, true) + COLON_SPACE + message, DEVELOPER_ERROR);
        }
        else
        {
            emit(FATAL_DEVELOPER_UNINFORMATIVE, DEVELOPER_ERROR);
        }
    }
    
    
    /**
     *  Notifies of developer failure from which recovery is impossible.
     *
     *  @param message
     *  @param exception
     */
    public static void devfail(String message, Throwable e)
    {
        if (EMIT_DEVELOPER_ERRORS)
        {
            emit(message, DEVELOPER_ERROR);
            emit(e, DEVELOPER_ERROR);
        }
        else
        {
            emit(FATAL_DEVELOPER_UNINFORMATIVE, DEVELOPER_ERROR);
        }
    }
    
    
    /**
     *  Notifies of developer failure from which recovery is impossible.
     *
     *  @param exception
     */
    public static void devfail(Throwable e)
    {
        if (EMIT_DEVELOPER_ERRORS)
        {
            emit(e, DEVELOPER_ERROR);
        }
        else
        {
            emit(FATAL_DEVELOPER_UNINFORMATIVE, DEVELOPER_ERROR);
        }
    }
    
    
    /**
     *  Prints developer debug messages.
     *
     *  @param message
     */
    public static void devinfo(String message)
    {
        if (EMIT_DEVELOPER_INFO)
        {
            emit(StackParser.getStackMethodBefore(THIS_PACKAGE, true) + COLON_SPACE + message, DEVELOPER_INFO);
        }
    }
    
    
    /**
     *  Notifies of developer-level exception.
     *
     *  @param exception
     */
    public static void devinfo(Throwable e)
    {
        if (EMIT_DEVELOPER_ERRORS)
        {
            emit(e, DEVELOPER_ERROR);
        }
    }
    
    
    /**
     *  Insists on printing a message even if display flags are false.
     *
     *  @param message
     */
    public static void insist(String message)
    {
        emit(message, INSIST);
    }
    

    /**
     *  Insists on printing a message even if display flags are false.
     *
     *  @param multi-line message
     */
    public static void insist(String[] message)
    {
        emit(message, INSIST);
    }


    /**
     *  Emits a message with a given flag.
     *
     *  @param message  the message to emit
     *  @param flag     the flag
     */
    private static void emit(String message, String flag)
    {
        MessagePrinter.println(message, flag, outStream, MessagePrinter.CONSOLE);
    }


    /**
     *  Emits a multi-line message with a given flag.
     *
     *  @param message  the message to emit
     *  @param flag     the flag
     */
    private static void emit(String[] message, String flag)
    {
        for (int line = 0; line < message.length; line++)
        {
            MessagePrinter.println(message[line], flag, outStream, MessagePrinter.CONSOLE);
        }
    }


    /**
     *  Emits a prefixed multi-line message with a given flag.
     *
     *  @param message  the message to emit
     *  @param flag     the flag
     */
    private static void emit(String[] message, String prefix, String flag)
    {
        for (int line = 0; line < message.length; line++)
        {
            MessagePrinter.println(prefix + message[line], flag, outStream, MessagePrinter.CONSOLE);
        }
    }


    /**
     *  Emits an exception message with a given flag.
     *
     *  @param message  the message to emit
     *  @param flag     the flag
     */
    private static void emit(Throwable exception, String flag)
    {
        String message = exception.getMessage();
        if (message != null)
        {
            emit(message, flag);
        }
        StringTokenizer lines = StackParser.getStackTraceFor(exception);
        while (lines.hasMoreElements())
        {
            if (!METHOD_NAMES_ALWAYS)
            {
                emit(lines.nextToken(), flag);
            }
            else
            {
                emit(StackParser.getStackMethodBefore(THIS_PACKAGE, true) + COLON_SPACE + lines.nextToken(), flag);
            }
        }
    }
}
