/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.aitools.programd.Core;
import org.aitools.programd.bot.Bot;
import org.aitools.programd.bot.Bots;
import org.aitools.programd.graph.Graphmaster;
import org.aitools.programd.multiplexor.PredicateMaster;
import org.aitools.programd.responder.TextResponder;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.FileManager;
import org.aitools.programd.util.ManagedProcess;
import org.aitools.programd.util.XMLKit;

/**
 * Provides a simple shell for interacting with the bot at a command line.
 * 
 * @author Jon Baer
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @author Eion Robb
 */
public class Shell extends Thread
{
    // Private convenience constants.

    /** The string to use for an interactive console. */
    public static final String PROMPT = "> ";

    /** Shell help command. */
    private static final String HELP = "/help";

    /** Shell exit command. */
    private static final String EXIT = "/exit";

    /** Load file command. */
    private static final String LOAD = "/load";

    /** Unload file command. */
    private static final String UNLOAD = "/unload";

    /** Bot list command. */
    private static final String BOTLIST = "/bots";

    /** Talk to bot command. */
    private static final String TALKTO = "/talkto";

    /** Who is the bot command. */
    private static final String WHO = "/who";

    /** List bot files command. */
    private static final String BOT_FILES = "/files";

    /** Shell commandables list command. */
    private static final String COMMANDABLES = "/commandables";

    /** Shell help text. */
    private static final String[] HELP_TEXT = { "All shell commands are preceded by a forward slash (/).", "The commands available are:",
            "/help             - prints this help", "/exit             - shuts down the bot server",
            "/load filename    - loads/reloads given filename for active bot", "/unload filename  - unloads given filename for active bot",
            "/bots             - lists loaded bots", "/talkto botid     - switches conversation to given bot",
            "/who              - prints the id of the current bot", "/files            - lists the files loaded by the current bot",
            "/roll chatlog     - rolls over chat log", "/roll targets     - rolls over saved targeting data",
            "/commandables     - lists available \"shell commandables\" (such as listeners)" };
    
    private static final String EMPTY_STRING = "";

    // Instance variables.

    private static final String RBRACKET_SPACE = "] ";
    
    private SimpleDateFormat timestampFormat;
    
    private boolean showTimestamp;
    
    /** The Core to which this Shell is attached. */
    private Core core;
    
    /** The console settings to use. */
    private ConsoleSettings consoleSettings;
    
    /** The Graphmaster in use by the attached Core. */
    private Graphmaster graphmaster;
    
    /** The PredicateMaster in use by the attached Core. */
    private PredicateMaster predicateMaster;
    
    /** The Bots object in use by the attached Core. */
    private Bots bots;

    /** A BufferedReader for user input to the shell. */
    private BufferedReader consoleIn;

    /** Where regular console output will go. */
    private PrintStream consoleOut;

    /** Where console errors will go. */
    private PrintStream consoleErr;

    /** Where console prompt output will go. */
    private PrintStream consolePrompt;

    /** A bot id. */
    private String botid;

    /** A bot name. */
    private String botName;

    /** The client name predicate. */
    private String clientNamePredicate;

    /** The bot name predicate. */
    private String botNamePredicate;

    /** The host name. */
    private String hostname;
    
    /**
     * An indicator used to keep track of whether we're midline in a console
     * output (i.e., showing a prompt).
     */
    private boolean midLine = false;

    /**
     * A <code>Shell</code> with default input and output streams (
     * <code>System.in</code> and <code>System.out</code>).
     * @param consoleSettingsToUse the object containing the console settings to use
     */
    public Shell(ConsoleSettings consoleSettingsToUse)
    {
        super("Shell");
        this.consoleIn = new BufferedReader(new InputStreamReader(System.in));
        this.consoleOut = this.consoleErr = this.consolePrompt = System.out;
        initialize(consoleSettingsToUse);
    }

    /**
     * A <code>Shell</code> with custom input and output streams.
     * @param consoleSettingsToUse 
     * 
     * @param in
     *            the input stream
     * @param out the output stream
     * @param err the error stream
     * @param prompt
     *            the prompt output stream
     */
    public Shell(ConsoleSettings consoleSettingsToUse, InputStream in, PrintStream out, PrintStream err, PrintStream prompt)
    {
        super("Shell");
        this.consoleIn = new BufferedReader(new InputStreamReader(in));
        this.consoleOut = out;
        this.consoleErr = err;
        this.consolePrompt = prompt;
        initialize(consoleSettingsToUse);
    }
    
    /**
     * Initialization common to both constructors.
     * @param consoleSettingsToUse the object containing the console settings to use
     */
    private void initialize(ConsoleSettings consoleSettingsToUse)
    {
        this.setDaemon(true);
        this.consoleSettings = consoleSettingsToUse;
        String timestampFormatString = this.consoleSettings.getTimestampFormat();
        if (timestampFormatString.length() > 0)
        {
            this.timestampFormat = new SimpleDateFormat(timestampFormatString);
            this.showTimestamp = true;
        }
        else
        {
            this.showTimestamp = false;
        }
    }
    
    
    /**
     * Attach this shell to the given core.
     * 
     * @param coreToUse
     */
    public void attachTo(Core coreToUse)
    {
        this.core = coreToUse;
        this.botNamePredicate = this.core.getSettings().getBotNamePredicate();
        this.graphmaster = this.core.getGraphmaster();
        this.predicateMaster = this.core.getPredicateMaster();
        this.bots = this.core.getBots();
        this.clientNamePredicate = this.core.getSettings().getClientNamePredicate();
        this.hostname = this.core.getHostname();
    }
    
    /**
     * Runs the shell.
     */
    public void run()
    {
        if (this.core == null)
        {
            throw new DeveloperError("Must attach the shell to a Core before calling run()!", new NullPointerException());
        }
        
        showMessage("Interactive shell: type \"" + EXIT + "\" to shut down; \"" + HELP + "\" for help.");
        Bot bot = this.bots.getABot();
        if (bot == null)
        {
            showError("No bot to talk to!");
            return;
        }
        this.botid = bot.getID();
        this.botName = bot.getPropertyValue(this.botNamePredicate);

        while (true)
        {
            showPrompt();
            String theLine = null;
            try
            {
                theLine = this.consoleIn.readLine();
            }
            catch (IOException e)
            {
                throw new DeveloperError("Cannot read from console!", e);
            }
            if (theLine == null)
            {
                /*
                 * A null line probably means that the shell is being mistakenly
                 * run in interactive mode when in fact there is no System.in
                 * available. In this case, sleep for 100 days :-) and wait to be
                 * interrupted.
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
            // (otherwise...)
            this.midLine = false;

            // Handle commands.
            if (theLine.indexOf('/') == 0)
            {
                // Exit command
                if (theLine.toLowerCase().equals(EXIT))
                {
                    printExitMessage();
                    this.core.shutdown();
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
                    load(theLine, this.botid);
                }
                // Unload from Graphmaster command
                else if (theLine.toLowerCase().startsWith(UNLOAD))
                {
                    unload(theLine);
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
                    catch (NoCommandException e)
                    {
                        showError("Please specify a command following the commandable.");
                    }
                    catch (NoSuchCommandableException e)
                    {
                        showError("No such commandable is loaded.  Type \"" + COMMANDABLES + "\" for a list of loaded commandables.");
                    }
                }
            }
            else if (theLine.length() > 0)
            {
                showConsole(this.botName, XMLKit.breakLinesAtTags(this.core.getResponse(theLine, this.hostname, this.botid)));
            }
        }
    }
    
    /**
     * Displays a prompt.
     */
    private void showPrompt()
    {
        if (this.getState() != Thread.State.NEW)
        {
            promptConsole('[' + this.botName + "] " + this.predicateMaster.get(this.clientNamePredicate, this.hostname, this.botid).trim());
        }
    }

    /**
     * <p>
     * Displays a line for an interactive console, including the prompt.
     * </p>
     * 
     * @param preprompt
     *            the text to show before the prompt
     */
    private void promptConsole(String preprompt)
    {
        if (this.midLine)
        {
            this.consolePrompt.println();
        } 
        this.consolePrompt.print(timestamp() + preprompt + PROMPT);
        this.midLine = true;
    }

    /**
     * <p>
     * Displays a regular message (no prompt) in an interactive console.
     * </p>
     * 
     * @param message
     *            the message to display
     */
    private void showMessage(String message)
    {
        printlnOut(message);
    }

    /**
     * <p>
     * Displays a multi-line regular message (no prompt) in an interactive console.
     * </p>
     * 
     * @param message
     *            the message to display
     */
    private void showMessage(String[] message)
    {
        for (int index = 0; index < message.length; index++)
        {
            printlnOut(message[index]);
        }
    }

    /**
     * <p>
     * Displays a multi-line error message (no prompt) in an interactive console.
     * </p>
     * 
     * @param message
     *            the message to display
     */
    private void showError(String[] message)
    {
        for (int index = 0; index < message.length; index++)
        {
            printlnErr(message[index]);
        }
    }

    /**
     * <p>
     * Displays an error message (no prompt) in an interactive console.
     * </p>
     * 
     * @param message
     *            the message to display
     */
    private void showError(String message)
    {
        printlnErr(message);
    }

    /**
     * <p>
     * Displays a message (after a prompt) in an interactive console.
     * </p>
     * 
     * @param preprompt
     *            the text to show before the prompt
     * @param message
     *            the multi-line message to display
     */
    private void showConsole(String preprompt, String message)
    {
        printlnOut(preprompt + PROMPT + message);
    }

    /**
     * <p>
     * Displays a multi-line message (after a prompt) in an interactive console.
     * </p>
     * 
     * @param preprompt
     *            the text to show before the prompt
     * @param message
     *            the multi-line message to display
     */
    private void showConsole(String preprompt, String[] message)
    {
        for (int index = 0; index < message.length; index++)
        {
            printlnOut(preprompt + PROMPT + message[index]);
        }
    }

    /**
     * Print a message line of standard output to the console.
     * 
     * @param message
     *            the message to print
     */
    public void printlnOut(String message)
    {
        if (this.midLine)
        {
            this.consoleOut.println();
        } 
        this.consoleOut.println(timestamp() + message);
        this.midLine = false;
    } 

    /**
     * Print a message line of error to the console.
     * TODO: Make the formatting different here.
     * 
     * @param message
     *            the message to print
     */
    public void printlnErr(String message)
    {
        if (this.midLine)
        {
            this.consoleErr.println();
        } 
        this.consoleErr.println(timestamp() + message);
        this.midLine = false;
    } 

    /**
     * Tells the Shell that something else was printed to the console; not midLine anymore.
     */
    public void gotLine()
    {
        this.midLine = false;
    } 

    /**
     * Prints an exit message.
     */
    private void printExitMessage()
    {
        Logger.getLogger("programd").log(Level.INFO, "Exiting at user request.");
    }

    /**
     * @return the current bot id
     */
    public String getCurrentBotID()
    {
        return this.botid;
    }

    /**
     * @return timestamp in specified long or short format
     */
    private String timestamp()
    {
        if (this.showTimestamp)
        {
            return '[' + this.timestampFormat.format(new Date()) + RBRACKET_SPACE;
        } 
        // (otherwise...)
        return EMPTY_STRING;
    } 

    /**
     * Prints help text.
     */
    public void help()
    {
        showMessage(HELP_TEXT);
    }

    /**
     * Loads a given file for a given bot.
     * @param line the line that contains the name of the file to load
     * @param botidToUse the id of the bot for whom to load the file
     */
    public void load(String line, String botidToUse)
    {
        // See if there is a filename.
        int space = line.indexOf(' ');
        if (space == -1)
        {
            showError("You must specify a filename.");
        }
        else
        {
            int categories = this.graphmaster.getTotalCategories();
            String path;
            try
            {
                path = FileManager.getFile(line.substring(space + 1)).getCanonicalPath();
            }
            catch (IOException e)
            {
                showError("I/O exception trying to locate file.");
                return;
            }
            this.graphmaster.load(path, botidToUse);
            Logger.getLogger("programd").log(Level.INFO, this.graphmaster.getTotalCategories() - categories + " categories loaded from \"" + path + "\".");
        }
    }

    /**
     * Unloads a given file for a given bot.
     * @param line the line in which to find the file name to unload
     */
    private void unload(String line)
    {
        // See if there is a filename.
        int space = line.indexOf(' ');
        if (space == -1)
        {
            showError("You must specify a filename.");
        }
        else
        {
            int categories = this.graphmaster.getTotalCategories();
            Bot bot = this.bots.getBot(this.botid);
            String path = line.substring(space + 1);
            this.graphmaster.unload(path, bot);
            bot.getLoadedFilesMap().remove(path);
            Logger.getLogger("programd").log(Level.INFO, categories - this.graphmaster.getTotalCategories() + " categories unloaded.");
        }
    }

    /**
     * Shows a list of active bots.
     */
    public void showBotList()
    {
        showMessage("Active bots: " + this.bots.getNiceList());
    }

    /**
     * Switches conversation to a given botid.
     * @param line the line in which to find the botid to which to switch
     */
    private void talkto(String line)
    {
        // See if there is a botid.
        int space = line.indexOf(' ');
        if (space == -1)
        {
            showError("You must specify a bot id.");
        }
        else
        {
            switchToBot(line.substring(space + 1));
        }
    }

    /**
     * Switches to a bot, given an id.
     * 
     * @param newBotID
     */
    public void switchToBot(String newBotID)
    {
        if (!this.bots.include(newBotID))
        {
            showError("That bot id is not known. Check your startup files.");
            return;
        }
        this.botid = newBotID;
        this.botName = this.bots.getBot(newBotID).getPropertyValue(this.botNamePredicate);
        showMessage("Switched to bot \"" + newBotID + "\" (name: \"" + this.botName + "\").");
        // Send the connect string and print the first response.
        showConsole(this.botName, XMLKit.breakLinesAtTags(this.core.getResponse(this.core.getSettings().getConnectString(),
                this.hostname, this.botid, new TextResponder())));
    }

    /**
     * Prints the name of the current bot.
     */
    private void who()
    {
        showMessage("You are talking to \"" + this.botid + "\".");
    }

    /**
     * Prints a list of files loaded by the current bot.
     */
    public void listBotFiles()
    {
        Set<URL> keys = this.bots.getBot(this.botid).getLoadedFilesMap().keySet();
        int fileCount = keys.size();
        if (fileCount == 0)
        {
            showMessage("No files loaded by \"" + this.botid + "\".");
        }
        else if (fileCount > 1)
        {
            showMessage(fileCount + " files loaded by \"" + this.botid + "\":");
        }
        else
        {
            showMessage("1 file loaded by \"" + this.botid + "\":");
        }
        for (URL url : keys)
        {
            showMessage(url.toExternalForm());
        }
    }

    /**
     * Lists the shell commandables that are loaded to the console.
     */
    private void listCommandables()
    {
        int commandableCount = 0;
        showMessage("Available shell commandables:");
        for (ManagedProcess process : this.core.getManagedProcesses().values())
        {
            if (process instanceof ShellCommandable)
            {
                ShellCommandable commandable = (ShellCommandable)process;
                showMessage("/" + commandable.getShellID() + " - " + commandable.getShellDescription());
                commandableCount++;
            }
        }
        if (commandableCount == 0)
        {
            showError("No shell commandables are loaded.");
        }
        else
        {
            showMessage("Commands after the shell commandable will be sent to the commandable.");
            showMessage("Example: \"/irc /JOIN #foo\" tells thIRCListenerRC listener to join channel \"#foo\".");
        }
    }

    /**
     * Sends a command to a shell commandable, if possible.
     * 
     * @param command
     *            the command (including the shell commandable name)
     * @throws NoCommandException
     *             if no command is given
     * @throws NoSuchCommandableException
     *             if an invalid commandable is specified
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
        
        for (ManagedProcess process : this.core.getManagedProcesses().values())
        {
            if (process instanceof ShellCommandable)
            {
                ShellCommandable candidate = (ShellCommandable)process;
                if (commandableID.equals(candidate.getShellID()))
                {
                    commandable = candidate;
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
     * An exception thrown if no command is specified.
     */
    private class NoCommandException extends Exception
    {
        // No body.
    }

    /**
     * An exception thrown if an invalid commandable is specified.
     */
    private class NoSuchCommandableException extends Exception
    {
        // No body.
    }
}