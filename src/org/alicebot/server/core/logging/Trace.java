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

package org.alicebot.server.core.logging;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.alicebot.server.core.Globals;


/**
 *  Handles message formatting to the console for tracing and debugging.
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
    private static final boolean EMIT_USER_INFO = Globals.showConsole();

    /** Whether to MessagePrinter.println user errors to the console (if available). */
    private static final boolean EMIT_USER_ERRORS = EMIT_USER_INFO;

    /** Whether to MessagePrinter.println general developer info to the console (if available). */
    private static final boolean EMIT_DEVELOPER_INFO = EMIT_USER_INFO ? Boolean.valueOf(Globals.getProperty("programd.console.developer", "false")).booleanValue() : false;

    /** Whether to MessagePrinter.println developer errors to the console (if available). */
    private static final boolean EMIT_DEVELOPER_ERRORS = EMIT_DEVELOPER_INFO;

    /** Whether to use message type flags. */
    private static final boolean SHOW_MESSAGE_FLAGS = Boolean.valueOf(Globals.getProperty("programd.console.message-flags", "true")).booleanValue();


    //  Built-in messages.

    /** The fatal user error message when user error messages are turned off. */
    private static final String FATAL_USER_UNINFORMATIVE = "Fatal user error. User error messages are turned off.";

    /** The fatal user error message when user error messages are turned off. */
    private static final String FATAL_DEVELOPER_UNINFORMATIVE = "Fatal developer error. Developer error messages are turned off.";

    /** The exiting message. */
    private static final String EXITING = "Exiting.";


    // Message type flags.

    /** No flag (an empty string). */
    static final String NO_FLAG = "";

    /** Flag for a program message to the console. */
    static final String PROGRAM_MESSAGE = SHOW_MESSAGE_FLAGS ? "P " : NO_FLAG;

    /** Flag for a line in an interactive console. */
    static final String CONSOLE = SHOW_MESSAGE_FLAGS ? "c " : NO_FLAG;

    /** Flag for a user info message to the console. */
    static final String USER_INFO = SHOW_MESSAGE_FLAGS ? "u " : NO_FLAG;

    /** Flag for a user error message to the console. */
    static final String USER_ERROR = SHOW_MESSAGE_FLAGS ? "U " : NO_FLAG;

    /** Flag for a developer info message to the console. */
    static final String DEVELOPER_INFO = SHOW_MESSAGE_FLAGS ? "d " : NO_FLAG;

    /** Flag for a developer error message to the console. */
    static final String DEVELOPER_ERROR = SHOW_MESSAGE_FLAGS ? "D " : NO_FLAG;

    /** Flag for an insistent message. */
    static final String INSIST = SHOW_MESSAGE_FLAGS ? "! " : NO_FLAG;

    /** The string to use for an interactive console. */
    static final String PROMPT = "> ";

    /** A BufferedReader for System.out. */
    private static final BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));


    /**
     *  <p>
     *  Displays a line for an interactive console,
     *  including the prompt.
     *  </p>
     *  <p>
     *  Currently uses hard-coded {@link java.lang.System.in System.in}.
     *  </p>
     *
     *  @param message
     *
     *  @return a reference to the console
     */
    public static BufferedReader console(String message)
    {
        MessagePrinter.print(message + PROMPT, CONSOLE, System.out, MessagePrinter.CONSOLE);
        return systemIn;    
    }
    
    
    /**
     *  <p>
     *  Displays a line in an interactive console
     *  in which the input is supplied by the bot. :-)
     *  </p>
     *  <p>
     *  Currently uses hard-coded {@link java.lang.System.in System.in}.
     *  </p>
     *    
     *  @param message
     *  @param response
     *
     *  @return a reference to the console
     */
    public static BufferedReader console(String message, String response)
    {
        MessagePrinter.println(message + PROMPT + response, CONSOLE, System.out, MessagePrinter.CONSOLE);
        return systemIn;    
    }
    
    
    /**
     *  <p>
     *  Displays a line in an interactive console
     *  in which the input is supplied by the bot. :-)
     *  </p>
     *  <p>
     *  Currently uses hard-coded {@link java.lang.System.in System.in}.
     *  </p>
     *    
     *  @param message
     *  @param response multi-line response
     *
     *  @return a reference to the console
     */
    public static BufferedReader console(String message, String[] response)
    {
        for (int index = 0; index < response.length; index++)
        {
            MessagePrinter.println(message + PROMPT + response[index], CONSOLE, System.out, MessagePrinter.CONSOLE);
        }
        return systemIn;    
    }
    
    
    /**
     *  Tells the MessagePrinter that we got an input; not midLine anymore.
     */
    public static void gotLine()
    {
        MessagePrinter.gotLine();
    }

    
    /**
     *  Handles user failures from which recovery is impossible.
     *
     *  @param message
     */
    public static void userfail(String message)
    {
        if (EMIT_USER_ERRORS)
        {
            MessagePrinter.println(message, USER_ERROR, System.err, MessagePrinter.CONSOLE);
            exit(1);    
        }
        else
        {
            MessagePrinter.println(FATAL_USER_UNINFORMATIVE, USER_ERROR, System.err, MessagePrinter.CONSOLE);
            exit(1);   
        }
    }
    
    
    /**
     *  Handles user failures from which recovery is impossible.
     *
     *  @param exception
     */
    public static void userfail(Exception e)
    {
        if (EMIT_USER_ERRORS)
        {
            StringTokenizer lines = new StringTokenizer(e.getMessage(), System.getProperty("line.separator"));
            while (lines.hasMoreTokens())
            {
                MessagePrinter.println(lines.nextToken(), USER_ERROR, System.err, MessagePrinter.CONSOLE);
            }
            exit(1);    
        }
        else
        {
            MessagePrinter.println(FATAL_USER_UNINFORMATIVE, USER_ERROR, System.err, MessagePrinter.CONSOLE);
            exit(1);
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
            MessagePrinter.println(message, USER_INFO, System.out, MessagePrinter.CONSOLE);
        }
    }
    
    
    /**
     *  Prints informative message to user.
     *
     *  @param message
     */
    public static void userinfo(String[] message)
    {
        if (EMIT_USER_INFO)
        {
            for (int index = 0; index < message.length; index++)
            {
                MessagePrinter.println(message[index], USER_INFO, System.out, MessagePrinter.CONSOLE);
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
            MessagePrinter.println(message, DEVELOPER_ERROR, System.err, MessagePrinter.CONSOLE);
            exit(1);    
        }
        else
        {
            MessagePrinter.println(FATAL_DEVELOPER_UNINFORMATIVE, DEVELOPER_ERROR, System.err, MessagePrinter.CONSOLE);
            exit(1);
        }
    }
    
    
    /**
     *  Handles developer failure from which recovery is impossible.
     *
     *  @param message
     *  @param exception
     */
    public static void devfail(String message, Exception e)
    {
        if (EMIT_DEVELOPER_ERRORS)
        {
            MessagePrinter.println(message, DEVELOPER_ERROR, System.err, MessagePrinter.CONSOLE);
            StringTokenizer lines = new StringTokenizer(e.getMessage(), System.getProperty("line.separator"));
            while (lines.hasMoreTokens())
            {
                MessagePrinter.println(lines.nextToken(), DEVELOPER_ERROR, System.err, MessagePrinter.CONSOLE);
            }
            e.printStackTrace();
            exit(1);    
        }
        else
        {
            MessagePrinter.println(FATAL_DEVELOPER_UNINFORMATIVE, DEVELOPER_ERROR, System.err, MessagePrinter.CONSOLE);
            exit(1);
        }
    }
    
    
    /**
     *  Handles developer failure from which recovery is impossible.
     *
     *  @param exception
     */
    public static void devfail(Exception e)
    {
        if (EMIT_DEVELOPER_ERRORS)
        {
            String message = (String)e.getMessage();
            StringTokenizer lines;
            if (message == null)
            {
                lines = new StringTokenizer(e.toString(), System.getProperty("line.separator"));
                if (lines == null)
                {
                    MessagePrinter.println("Undescribed exception!", USER_ERROR, System.err, MessagePrinter.CONSOLE);
                }
            }
            else
            {
                lines = new StringTokenizer(message, System.getProperty("line.separator"));
            }
            while (lines.hasMoreTokens())
            {
                MessagePrinter.println(lines.nextToken(), USER_ERROR, System.err, MessagePrinter.CONSOLE);
            }
            e.printStackTrace();
            exit(1);    
        }
        else
        {
            MessagePrinter.println(FATAL_DEVELOPER_UNINFORMATIVE, DEVELOPER_ERROR, System.err, MessagePrinter.CONSOLE);
            exit(1);
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
            MessagePrinter.println(message, DEVELOPER_INFO, System.out, MessagePrinter.CONSOLE);
        }
    }
    
    
    /**
     *  Insists on printing a message even if display flags are false.
     *
     *  @param message
     */
    public static void insist(String message)
    {
        MessagePrinter.println(message, INSIST, System.out, MessagePrinter.CONSOLE);
    }
    

    /**
     *  Insists on printing a message even if display flags are false.
     *
     *  @param multi-line message
     */
    public static void insist(String[] message)
    {
        for (int index = 0; index < message.length; index++)
        {
            MessagePrinter.println(message[index], INSIST, System.out, MessagePrinter.CONSOLE);
        }
    }

    
    /**
     *  Exits with a message.
     */
    private static void exit(int status)
    {
        MessagePrinter.println(EXITING, PROGRAM_MESSAGE, System.out, MessagePrinter.CONSOLE);
        System.exit(status);    
    }
}