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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Set;

import org.alicebot.gui.SimpleConsole;
import org.alicebot.server.core.ActiveMultiplexor;
import org.alicebot.server.core.Bot;
import org.alicebot.server.core.Bots;
import org.alicebot.server.core.BotProcess;
import org.alicebot.server.core.BotProcesses;
import org.alicebot.server.core.Globals;
import org.alicebot.server.core.Graphmaster;
import org.alicebot.server.core.Multiplexor;
import org.alicebot.server.core.PredicateMaster;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.logging.XMLLog;
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

    /** The client name predicate. */
    private static final String CLIENT_NAME_PREDICATE = Globals.getClientNamePredicate();

    /** The bot name predicate. */
    private static final String BOT_NAME_PREDICATE = Globals.getBotNamePredicate();

    /** The string to use for an interactive console. */
    public static final String PROMPT               = "> ";

    /** Whether to use message type flags. */
    private static final boolean SHOW_MESSAGE_FLAGS = Boolean.valueOf(Globals.getProperty("programd.console.message-flags", "true")).booleanValue();

    /** Flag for a line in an interactive shell. */
    public static final String SHELL                = SHOW_MESSAGE_FLAGS ? "s " : "";

    /** Shell help command. */
    private static final String HELP                = "/help";

    /** Shell exit command. */
    private static final String EXIT                = "/exit";

    /** Load file command. */
    private static final String LOAD                = "/load";

    /** Unload file command. */
    private static final String UNLOAD              = "/unload";

    /** Bot list command. */
    private static final String BOTLIST             = "/bots";

    /** Talk to bot command. */
    private static final String TALKTO              = "/talkto";

    /** Who is the bot command. */
    private static final String WHO                 = "/who";

    /** List bot files command. */
    private static final String BOT_FILES           = "/files";

    /** Roll chatlog command. */
    private static final String ROLL_CHATLOG        = "/roll chatlog";

    /** Roll targets command. */
    private static final String ROLL_TARGETS        = "/roll targets";

    /** Shell commandables list command. */
    private static final String COMMANDABLES        = "/commandables";

    /** Shell help text. */
    private static final String[] HELP_TEXT         = {"All shell commands are preceded by a forward slash (/).",
                                                       "The commands available are:",
                                                       "/help             - prints this help",
                                                       "/exit             - shuts down the bot server",
                                                       "/load filename    - loads/reloads given filename for active bot",
                                                       "/unload filename  - unloads given filename for active bot",
                                                       "/bots             - lists loaded bots",
                                                       "/talkto botid     - switches conversation to given bot",
                                                       "/who              - prints the id of the current bot",
                                                       "/files            - lists the files loaded by the current bot",
                                                       "/roll chatlog     - rolls over chat log",
                                                       "/roll targets     - rolls over saved targeting data",
                                                       "/commandables     - lists available \"shell commandables\" (such as listeners)"};

    // Instance variables.

    /** A BufferedReader for user input to the shell. */
    private BufferedReader consoleIn;

    /** Where console display output will go. */
    private PrintStream consoleDisplay;

    /** Where console prompt output will go. */
    private PrintStream consolePrompt;

    /** A bot id. */
    private String botid;

    /** A bot name. */
    private String botName;


    /**
     *  A <code>Shell</code> with default input and output streams
     *  (<code>System.in</code> and <code>System.out</code>).
     */
    public Shell()
    {
        consoleIn = new BufferedReader(new InputStreamReader(System.in));
        consoleDisplay = consolePrompt = System.out;
    }


    /**
     *  A <code>Shell</code> with custom input and output streams.
     *
     *  @param in       the input stream
     *  @param display  the display output stream
     *  @param prompt   the prompt output stream
     */
    public Shell(InputStream in, PrintStream display, PrintStream prompt)
    {
        consoleIn = new BufferedReader(new InputStreamReader(in));
        consoleDisplay = display;
        consolePrompt = prompt;
    }


    /**
     *  Runs the shell.
     */
    public void run()
    {
        showConsole("Interactive shell: type \"" + EXIT + "\" to shut down; \"" + HELP + "\" for help.");
        Bot bot = Bots.getFirstBot();
        if (bot == null)
        {
            showConsole("No bot to talk to!");
            return;
        }
        botid = bot.getID();
        botName = bot.getPropertyValue(BOT_NAME_PREDICATE);
        
        // Send the connect string and print the first response.
        showConsole(botName,
                    Toolkit.breakLines(
                        Multiplexor.
                                getResponse(Globals.getProperty("programd.connect-string", "CONNECT"),
                                            HOSTNAME,
                                            botid,
                                            new TextResponder())));       

        while (true)
        {
            promptConsole('[' + botName + "] " + PredicateMaster.get(CLIENT_NAME_PREDICATE, HOSTNAME, botid));
            String theLine = null;
            try
            {
                theLine = consoleIn.readLine();
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
                        printExitMessage();
                        return;
                    }
                    // Help command
                    else if (theLine.toLowerCase().equals(HELP))
                    {
                        help();
                    }
                    // Load into Graphmaster command
                    else if (theLine.toLowerCase().startsWith(LOAD))
                    {
                        load(theLine, botid);
                    }
                    // Unload from Graphmaster command
                    else if (theLine.toLowerCase().startsWith(UNLOAD))
                    {
                        unload(theLine, botid);
                    }
                    // Bot list command
                    else if (theLine.toLowerCase().equals(BOTLIST))
                    {
                        showBotList();
                    }
                    // Talk to bot command
                    else if (theLine.toLowerCase().startsWith(TALKTO))
                    {
                        talkto(theLine);
                    }
                    // Who's the bot command
                    else if (theLine.toLowerCase().equals(WHO))
                    {
                        who();
                    }
                    // List bot files command
                    else if (theLine.toLowerCase().equals(BOT_FILES))
                    {
                        listBotFiles();
                    }
                    // Roll chatlog command
                    else if (theLine.toLowerCase().startsWith(ROLL_CHATLOG))
                    {
                        rollChatLog(botid);
                    }
                    // Roll targets command
                    else if (theLine.toLowerCase().equals(ROLL_TARGETS))
                    {
                        rollTargets();
                    }
                    // List commandables command
                    else if (theLine.toLowerCase().equals(COMMANDABLES))
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
                            showConsole("No such commandable is loaded.  Type \"" + COMMANDABLES + "\" for a list of loaded commandables.");
                        }
                    }
                }
                else if (theLine.length() > 0)
                {
                    showConsole(botName,
                                Toolkit.breakLines(
                                    Multiplexor.
                                            getResponse(theLine,
                                                        HOSTNAME,
                                                        botid,
                                                        new TextResponder())));
                }
            }
        }
    }


    public String getCurrentBotID()
    {
        return botid;
    }


    /**
     *  <p>
     *  Displays a line for an interactive console,
     *  including the prompt.
     *  </p>
     *
     *  @param preprompt    the text to show before the prompt
     */
    private void promptConsole(String preprompt)
    {
        MessagePrinter.print(preprompt + PROMPT, SHELL, consolePrompt, MessagePrinter.CONSOLE);
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
    private void showConsole(String message)
    {
        MessagePrinter.println(message, SHELL, consoleDisplay, MessagePrinter.CONSOLE);
    }
    
    
    /**
     *  <p>
     *  Displays a multi-line message (no prompt) in an interactive console.
     *  </p>
     *    
     *  @param message      the message to display
     */
    private void showConsole(String[] message)
    {
        for (int index = 0; index < message.length; index++)
        {
            MessagePrinter.println(message[index], SHELL, consoleDisplay, MessagePrinter.CONSOLE);
        }
    }
    
    
    /**
     *  <p>
     *  Displays a message (after a prompt) in an interactive console.
     *  </p>
     *    
     *  @param preprompt    the text to display before the prompt
     *  @param message      the message to display
     */
    private void showConsole(String preprompt, String message)
    {
        MessagePrinter.println(preprompt + PROMPT + message, SHELL, consoleDisplay, MessagePrinter.CONSOLE);
    }
    
    
    /**
     *  <p>
     *  Displays a multi-line message (after a prompt) in an interactive console.
     *  </p>
     *    
     *  @param preprompt    the text to show before the prompt
     *  @param message      the multi-line message to display
     */
    private void showConsole(String preprompt, String[] message)
    {
        for (int index = 0; index < message.length; index++)
        {
            MessagePrinter.println(preprompt + PROMPT + message[index], SHELL, consoleDisplay, MessagePrinter.CONSOLE);
        }
    }


    /**
     *  Prints an exit message.
     */
	private void printExitMessage()
	{
		Log.userinfo("Exiting at user request.", Log.STARTUP);
	}
	

    /**
     *  Prints help text.
     */
	public void help()
	{
		showConsole(HELP_TEXT);
	}


    /**
     *  Loads a given file for a given bot.
     */
	public void load(String line, String botid)
	{
		// See if there is a filename.
		int space = line.indexOf(' ');
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
			path = new File(line.substring(space + 1)).getCanonicalPath();
		    }
		    catch (IOException e)
		    {
			showConsole("I/O exception trying to locate file.");
			return;
		    }
		    Graphmaster.load(path, botid);
		    Log.userinfo(Graphmaster.getTotalCategories() - categories +
			" categories loaded from \"" + path + "\".", Log.LEARN);
		}
	}


    /**
     *  Unloads a given file for a given bot.
     */
	private void unload(String line, String botid)
	{
		// See if there is a filename.
		int space = line.indexOf(' ');
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
			path = new File(line.substring(space + 1)).getCanonicalPath();
		    }
		    catch (IOException e)
		    {
			showConsole("I/O exception trying to locate file.");
			return;
		    }
		    Graphmaster.unload(path, Bots.getBot(botid));
		    Log.userinfo(categories - Graphmaster.getTotalCategories() +
			" categories unloaded.", Log.LEARN);
		}
	}


    /**
     *  Shows a list of active bots.
     */
	public void showBotList()
	{
        showConsole("Active bots: " + Bots.getNiceList());
	}


    /**
     *  Switches conversation to a given botid.
     */
	private void talkto(String line)
	{
		// See if there is a botid.
		int space = line.indexOf(' ');
		if (space == -1)
		{
		    showConsole("You must specify a bot id.");
		}
		else
		{
            switchToBot(line.substring(space + 1));
        }
    }


    /**
     *  Switches to a bot, given an id.
     *
     *  @param newBotID
     */
    public void switchToBot(String newBotID)
    {
        if (!Bots.knowsBot(newBotID))
        {
            showConsole("That bot id is not known. Check your startup files.");
            return;
        }
        botid = newBotID;
        botName = Bots.getBot(newBotID).getPropertyValue(BOT_NAME_PREDICATE);
        showConsole("Switched to bot \"" + newBotID + "\" (name: \"" + botName + "\").");
        // Send the connect string and print the first response.
        showConsole(botName,
                    Toolkit.breakLines(
                        Multiplexor.
                                getResponse(Globals.getProperty("programd.connect-string", "CONNECT"),
                                            HOSTNAME,
                                            botid,
                                            new TextResponder()))); 
	}


    /**
     *  Prints the name of the current bot.
     */
	private void who()
	{
		showConsole("You are talking to \"" + botid + "\".");
    }


    /**
     *  Prints a list of files loaded by the current bot.
     */
    public void listBotFiles()
    {
        Set keys = Bots.getBot(botid).getLoadedFilesMap().keySet();
        Iterator iterator = keys.iterator();
        int fileCount = keys.size();
        if (fileCount > 1)
        {
            showConsole(fileCount + " files loaded by \"" + botid + "\":");
        }
        else
        {
            showConsole("1 file loaded by \"" + botid + "\":");
        }
        while (iterator.hasNext())
        {
            showConsole(((File)iterator.next()).getAbsolutePath());
        }
    }


    /**
     *  Rolls over the chat log file.
     */
	public void rollChatLog(String botid)
	{
		showConsole("Rolling over chat log for \"" + botid + "\".");
		XMLWriter.rollover(Bots.getBot(botid).getChatlogSpec());
		showConsole("Finished rolling over chat log.");
	}


    /**
     *  Rolls over the targets data file.
     */
	public void rollTargets()
	{
		TargetMaster.rollTargetData();
	}
    
    
    /**
     *  Lists the shell commandables that are loaded to the console.
     */
    private void listCommandables()
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
    private void sendCommand(String command) throws NoCommandException, NoSuchCommandableException
    {
        // Parse out the commandable.
        int space = command.indexOf(' ');
        if (space == -1)
        {
            throw new NoCommandException();
        }
        if (space == command.length())
        {
            throw new NoCommandException();
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
            throw new NoSuchCommandableException();
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