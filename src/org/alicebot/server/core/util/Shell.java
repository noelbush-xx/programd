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

package org.alicebot.server.core.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.util.Iterator;

import org.alicebot.server.core.ActiveMultiplexor;
import org.alicebot.server.core.BotProcess;
import org.alicebot.server.core.BotProcesses;
import org.alicebot.server.core.Globals;
import org.alicebot.server.core.Graphmaster;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.logging.XMLLog;
import org.alicebot.server.core.PredicateMaster;
import org.alicebot.server.core.responder.TextResponder;
import org.alicebot.server.core.targeting.TargetMaster;


/**
 *  Provides a simple shell for interacting with the bot
 *  at a command line (was originally in Graphmaster).
 *
 *  @author Jon Baer
 *  @author Noel Bush
 */
public class Shell
{
    // Private convenience constants.

    /** Name of the local host. */
    private static final String HOSTNAME = Globals.getHostName();

    /** A BufferedReader for System.out. */
    private static final BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));

    /** The string to use for an interactive console. */
    public static final String PROMPT               = "> ";

    /** Whether to use message type flags. */
    private static final boolean SHOW_MESSAGE_FLAGS = Boolean.valueOf(Globals.getProperty("programd.console.message-flags", "true")).booleanValue();

    /** Flag for a line in an interactive console. */
    public static final String CONSOLE              = SHOW_MESSAGE_FLAGS ? "c " : "";

    /** Shell help command. */
    private static final String HELP                = "/help";

    /** Shell exit command. */
    private static final String EXIT                = "/exit";

    /** Load file command. */
    private static final String LOAD                = "/load";

    /** Delete targets command. */
    private static final String ROLL_CHATLOG        = "/roll chatlog";

    /** Delete targets command. */
    private static final String ROLL_TARGETS        = "/roll targets";

    /** Shell commandables list command. */
    private static final String LIST                = "/list";

    /** Shell help text. */
    private static final String[] HELP_TEXT         = {"All shell commands are preceded by a forward slash (/).",
                                                       "The commands available are:",
                                                       "/help           - prints this help",
                                                       "/exit           - shuts down the bot",
                                                       "/load filename  - loads given filename into Graphmaster",
                                                       "/roll chatlog   - rolls over chat log",
                                                       "/roll targets   - rolls over saved targeting data",
                                                       "/list           - lists available \"shell commandables\" (such as listeners)"};

    /** Flag whether the shell is started. */
    private static boolean started = false;

    /** Private instance of the shell. */
    private static final Shell myself = new Shell();


    /**
     *  Prevents instantiating of this class.
     */
    private Shell()
    {
    }


    /**
     *  Runs the shell.
     */
    public static void run()
    {
        started = true;
        showConsole("Interactive shell: type \"" + EXIT + "\" to shut down; \"" + HELP + "\" for help.");
        String botName = Globals.getBotName();
        BufferedReader console = null;
        while (true)
        {
            console = getConsole(PredicateMaster.get(Globals.getClientNamePredicate(), HOSTNAME));
            String theLine = null;
            try
            {
                theLine = console.readLine();
            }
            catch (IOException e)
            {
                Log.userinfo("Cannot read from console!", Log.ERROR);
                return;
            }
            if (theLine == null)
            {
                /*
                    A null line probably means that the shell is being mistakenly
                    run in interactive mode when in fact there is no System.in
                    available.  In this case, sleep for days :-) and wait to be interrupted.
                */
                while (true)
                {
                    try
                    {
                        Thread.sleep(86400000);
                    }
                    catch (InterruptedException e)
                    {
                        break;
                    }
                }
                break;
            }
            else
            {
                MessagePrinter.gotLine();

                // Handle commands.
                if (theLine.indexOf('/') == 0)
                {
                    // Exit command
                    if (theLine.toLowerCase().equals(EXIT))
                    {
                        Log.userinfo("Exiting at user request.", Log.STARTUP);
                        return;
                    }
                    // Help command
                    else if (theLine.toLowerCase().equals(HELP))
                    {
                        showConsole(HELP_TEXT);
                    }
                    // Load into Graphmaster command
                    else if (theLine.toLowerCase().startsWith(LOAD))
                    {
                        // See if there is a filename.
                        int space = theLine.indexOf(' ');
                        if (space == -1)
                        {
                            showConsole("You must specify a filename.");
                        }
                        else
                        {
                            int categories = Graphmaster.getTotalCategories();
                            String path;
                            try
                            {
                                path = new File(theLine.substring(space + 1)).getCanonicalPath();
                            }
                            catch (IOException e)
                            {
                                showConsole("I/O exception trying to locate file.");
                                return;
                            }
                            Graphmaster.load(path);
                            Log.userinfo(Graphmaster.getTotalCategories() - categories +
                                " categories loaded from \"" + path + "\".", Log.LEARN);
                        }
                    }
                    // Roll chatlog command
                    else if (theLine.toLowerCase().equals(ROLL_CHATLOG))
                    {
                        Trace.userinfo("Rolling over chat log.");
                        XMLWriter.rollover(XMLLog.CHAT);
                        Trace.userinfo("Finished rolling over chat log.");
                    }
                    // Roll targets command
                    else if (theLine.toLowerCase().equals(ROLL_TARGETS))
                    {
                        TargetMaster.rollTargetData();
                    }
                    // List commandables command
                    else if (theLine.toLowerCase().equals(LIST))
                    {
                        listCommandables();
                    }
                    else
                    {
                        try
                        {
                            sendCommand(theLine);
                        }
                        catch (NoCommandException e0)
                        {
                            showConsole("Please specify a command following the commandable.");
                        }
                        catch (NoSuchCommandableException e1)
                        {
                            showConsole("No such commandable is loaded.  Type \"" + LIST + "\" for a list of loaded commandables.");
                        }
                    }
                }
                else if (theLine.length() > 0)
                {
                    showConsole(botName,
                                Toolkit.breakLines(
                                ActiveMultiplexor.getInstance().getResponse(theLine, HOSTNAME, new TextResponder())));
                }
            }
        }
    }


    /**
     *  <p>
     *  Displays a prompt.
     *  </p>
     *  <p>
     *  Currently uses hard-coded {@link java.lang.System.in System.in}.
     *  </p>
     *
     *  @return a reference to the console
     */
    private static BufferedReader getConsole()
    {
        MessagePrinter.print(PROMPT, CONSOLE, System.out, MessagePrinter.CONSOLE);
        return systemIn;    
    }
    
    
    /**
     *  <p>
     *  Displays a line for an interactive console,
     *  including the prompt.
     *  </p>
     *  <p>
     *  Currently uses hard-coded {@link java.lang.System.in System.in}.
     *  </p>
     *
     *  @param preprompt    the text to show before the prompt
     *
     *  @return a reference to the console
     */
    private static BufferedReader getConsole(String preprompt)
    {
        MessagePrinter.print(preprompt + PROMPT, CONSOLE, System.out, MessagePrinter.CONSOLE);
        return systemIn;    
    }
    
    
    /**
     *  <p>
     *  Displays a message (no prompt) in an interactive console.
     *  </p>
     *  <p>
     *  Currently uses hard-coded {@link java.lang.System.in System.in}.
     *  </p>
     *    
     *  @param message      the message to display
     */
    private static void showConsole(String message)
    {
        MessagePrinter.println(message, CONSOLE, System.out, MessagePrinter.CONSOLE);
    }
    
    
    /**
     *  <p>
     *  Displays a multi-line message (no prompt) in an interactive console.
     *  </p>
     *  <p>
     *  Currently uses hard-coded {@link java.lang.System.in System.in}.
     *  </p>
     *    
     *  @param message      the message to display
     */
    private static void showConsole(String[] message)
    {
        for (int index = 0; index < message.length; index++)
        {
            MessagePrinter.println(message[index], CONSOLE, System.out, MessagePrinter.CONSOLE);
        }
    }
    
    
    /**
     *  <p>
     *  Displays a message (after a prompt) in an interactive console.
     *  </p>
     *  <p>
     *  Currently uses hard-coded {@link java.lang.System.in System.in}.
     *  </p>
     *    
     *  @param preprompt    the text to display before the prompt
     *  @param message      the message to display
     */
    private static void showConsole(String preprompt, String message)
    {
        MessagePrinter.println(preprompt + PROMPT + message, CONSOLE, System.out, MessagePrinter.CONSOLE);
    }
    
    
    /**
     *  <p>
     *  Displays a multi-line message (after a prompt) in an interactive console.
     *  </p>
     *  <p>
     *  Currently uses hard-coded {@link java.lang.System.in System.in}.
     *  </p>
     *    
     *  @param preprompt    the text to show before the prompt
     *  @param message      the multi-line message to display
     */
    private static void showConsole(String preprompt, String[] message)
    {
        for (int index = 0; index < message.length; index++)
        {
            MessagePrinter.println(preprompt + PROMPT + message[index], CONSOLE, System.out, MessagePrinter.CONSOLE);
        }
    }


    /**
     *  Lists the shell commandables that are loaded to the console.
     */
    private static void listCommandables()
    {
        Iterator processes = BotProcesses.getRegistryIterator();
        int commandableCount = 0;
        if (processes.hasNext())
        {
            showConsole("Available shell commandables:");
            while (processes.hasNext())
            {
                try
                {
                    ShellCommandable commandable = (ShellCommandable)processes.next();
                    showConsole("/" + commandable.getShellID() +
                        " - " + commandable.getShellDescription());
                    commandableCount++;
                }
                catch (ClassCastException e)
                {
                    // Do nothing; this is not a ShellCommandable.
                }
            }
        }
        if (commandableCount == 0)
        {
            showConsole("No shell commandables are loaded.");
        }
        else
        {
            showConsole("Commands after the shell commandable will be sent to the commandable.");
            showConsole("Example: \"/irc /JOIN #foo\" tells the AliceIRC listener to join channel \"#foo\".");
        }
    }


    /**
     *  Sends a command to a shell commandable, if possible.
     *
     *  @param command  the command (including the shell commandable name)
     *
     *  @throws NoCommandException if no command is given
     *  @throws NoSuchCommandableException if an invalid commandable is specified
     */
    private static void sendCommand(String command) throws NoCommandException, NoSuchCommandableException
    {
        // Parse out the commandable.
        int space = command.indexOf(' ');
        if (space == -1)
        {
            throw myself.new NoCommandException();
        }
        if (space == command.length())
        {
            throw myself.new NoCommandException();
        }

        String commandableID = command.substring(1, space);
        ShellCommandable commandable = null;

        Iterator processes = BotProcesses.getRegistryIterator();
        if (processes.hasNext())
        {
            while (processes.hasNext())
            {
                try
                {
                    ShellCommandable candidate = (ShellCommandable)processes.next();
                    if (commandableID.equals(candidate.getShellID()))
                    {
                        commandable = candidate;
                    }
                }
                catch (ClassCastException e)
                {
                    // Do nothing; this is not a ShellCommandable.
                }
            }
        }
        if (commandable == null)
        {
            throw myself.new NoSuchCommandableException();
        }
        commandable.processShellCommand(command.substring(space + 1));
    }


    /**
     *  An exception thrown if no command is specified.
     */
    private class NoCommandException extends Exception
    {
        public NoCommandException()
        {
        }
    }


    /**
     *  An exception thrown if an invalid commandable is specified.
     */
    private class NoSuchCommandableException extends Exception
    {
        public NoSuchCommandableException()
        {
        }
    }
}