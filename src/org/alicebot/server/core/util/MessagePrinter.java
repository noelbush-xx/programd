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
    Moved to util package
*/

package org.alicebot.server.core.util;

import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.alicebot.server.core.Globals;


/**
 *  Provides standard formatting for messages.
 *
 *  @version 4.1.3
 *  @author Noel Bush
 */
public class MessagePrinter
{
    /** A destination flag indicating the console. */
    public static final int CONSOLE = 1;

    /** A destination flag indicating logging. */
    public static final int LOG = 2;

    /** The system line separator. */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    /** A space. */
    private static final String SPACE = " ";

    /** A left bracket. */
    private static final String LBRACKET = "[";

    /** A right bracket. */
    private static final String RBRACKET = "]";

    /** An empty string. */
    private static final String EMPTY_STRING = "";

    /** The timestamp format for logging. */
    private static final String TIMESTAMP_LOG_FORMAT = Globals.getProperty("programd.logging.timestamp-format", "yyyy-MM-dd H:mm:ss");

    /** The timestamp format for console display. */
    private static final String TIMESTAMP_CONSOLE_FORMAT = Globals.getProperty("programd.console.timestamp-format", "H:mm:ss");

    /** Whether to show a console timestamp at all. */
    private static final boolean consoleTimestamp = TIMESTAMP_CONSOLE_FORMAT.length() > 0 ? true : false;

    /** Whether to show a log timestamp at all. */
    private static final boolean logTimestamp = TIMESTAMP_LOG_FORMAT.length() > 0 ? true : false;

    /** An error string when cannot write to file. */
    private static final String FILE_WRITER_ERROR = "Error writing to filewriter ";

    /** An indicator used to keep track of whether we're midline in a console output (i.e., showing a prompt). */
    private static boolean midLine = false;

    /** The last message to the console. */
    private static String lastPrompt;

    /** The last type flag used at the console. */
    private static String lastTypeFlag;

    /** The last longFormat flag used at the console. */
    private static boolean lastLongFormat = false;


    /**
     *  Print a message line with a type flag to a {@link java.io.PrintStream PrintStream}.
     *
     *  @param message      the message to print
     *  @param typeFlag     user-defined
     *  @param out          (such as @{link java.lang.System.out System.out})
     *  @param destination  destination flag (for choosing format)
     */
    public static void println(String message, String typeFlag, PrintStream out, int destination)
    {
        if (midLine)
        {
            out.println();
        }
        out.println(typeFlag + timestamp(destination) + message);
        midLine = false;
    }
    
    
    /**
     *  Print a message (no linebreak) with a type flag to a {@link java.io.PrintStream PrintStream}.
     *
     *  @param message      the message to print
     *  @param typeFlag     user-defined
     *  @param out          (such as @{link java.lang.System.out System.out})
     *  @param destination  destination flag (for choosing format)
     */
    public static void print(String message, String typeFlag, PrintStream out, int destination)
    {
        if (midLine)
        {
            out.println();
        }
        out.print(typeFlag + timestamp(destination) + message);
        midLine = true;
    }
    
    
    /**
     *  Print a message with a type flag to a {@link java.io.FileWriter FileWriter}.
     *
     *  @param message      the message to print
     *  @param out          the {@link java.io.FileWriter FileWriter} to use
     *  @param destination  destination flag (for choosing format)
     */
    public static void println(String message, String typeFlag, FileWriter out, int destination)
    {
        try
        {
            out.write(typeFlag + timestamp(destination) + message + LINE_SEPARATOR);    
            out.flush();
       }
        catch (IOException e)
        {
            Trace.devinfo(FILE_WRITER_ERROR + out);
            Trace.devinfo(e);
        }
    }
    
    
    /**
     *  Returns the date & time in a consistent format.
     *
     *  @param destination  destination flag (for choosing format)
     *
     *  @return timestamp in specified long or short format
     */
    private static String timestamp(int destination)
    {
        if (destination == CONSOLE)
        {
            if (consoleTimestamp)
            {
                return LBRACKET +
                    new SimpleDateFormat(TIMESTAMP_CONSOLE_FORMAT).format(new Date()) + RBRACKET + SPACE;
            }
            else
            {
                return EMPTY_STRING;
            }
        }
        else if (destination == LOG)
        {
            if (logTimestamp)
            {
                return LBRACKET +
                    new SimpleDateFormat(TIMESTAMP_LOG_FORMAT).format(new Date()) + RBRACKET + SPACE;
            }
            else
            {
                return EMPTY_STRING;
            }
        }   
        else
        {
            return EMPTY_STRING;
        }
    }


    /**
     *  Tells the MessagePrinter that we got an input; not midLine anymore.
     */
    public static void gotLine()
    {
        midLine = false;
    }
}
