/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.aitools.programd.Core;
import org.aitools.programd.bot.Bot;
import org.aitools.programd.bot.Bots;
import org.aitools.programd.interfaces.ConsoleSettings;
import org.aitools.programd.multiplexor.PredicateMaster;
import org.aitools.programd.responder.TextResponder;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.ManagedProcess;
import org.aitools.programd.util.UserError;
import org.aitools.programd.util.XMLKit;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Provides a simple shell for interacting with the bot at a command line.
 * 
 * @author Jon Baer
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @author Eion Robb
 */
public class Shell extends Thread
{
    /** The string to use for an interactive console. */
    public static final String PROMPT = "> ";

    /** Shell exit command. */
    private static final String EXIT = "/exit";

    private static final String EMPTY_STRING = "";

    // Instance variables.

    private static final String RBRACKET_SPACE = "] ";

    private SimpleDateFormat timestampFormat;

    private boolean showTimestamp;
    
    /** The command registry. */
    private ShellCommandRegistry commandRegistry;

    /** The Core to which this Shell is attached. */
    private Core core;

    /** The console settings to use. */
    private ConsoleSettings consoleSettings;

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
     * 
     * @param consoleSettingsToUse the object containing the console settings to
     *            use
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
     * 
     * @param consoleSettingsToUse
     * @param in the input stream
     * @param out the output stream
     * @param err the error stream
     * @param prompt the prompt output stream
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
     * 
     * @param consoleSettingsToUse the object containing the console settings to
     *            use
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
        this.predicateMaster = this.core.getPredicateMaster();
        this.bots = this.core.getBots();
        this.clientNamePredicate = this.core.getSettings().getClientNamePredicate();
        this.hostname = this.core.getHostname();
        
        this.commandRegistry = new ShellCommandRegistry();
        
        // Look for any shell command plugins and add them to the registry.
        NodeList commands = ((Element)this.core.getPluginConfig().getDocumentElement().getElementsByTagNameNS(Core.PLUGIN_CONFIG_SCHEMA_URI, "shell-commands").item(0)).getElementsByTagNameNS(Core.PLUGIN_CONFIG_SCHEMA_URI, "command");
        int commandCount = commands.getLength();
        for (int commandIndex = 0; commandIndex < commandCount; commandIndex++)
        {
            Element commandElement = (Element)commands.item(commandIndex);
            String classname = commandElement.getAttribute("class");
            NodeList parameterElements = commandElement.getElementsByTagNameNS(Core.PLUGIN_CONFIG_SCHEMA_URI, "parameter");
            int parameterCount = parameterElements.getLength();
            HashMap<String, String> parameters = new HashMap<String, String>(parameterCount);
            for (int parameterIndex = 0; parameterIndex < parameterCount; parameterIndex++)
            {
                Element parameter = (Element)parameterElements.item(parameterIndex);
                parameters.put(parameter.getAttribute("name"), parameter.getAttribute("value"));
            }
            this.commandRegistry.register(classname, parameters);
        }
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

        showMessage("Interactive shell: type \"" + EXIT + "\" to shut down; \"" + HelpCommand.COMMAND_STRING + "\" for help.");
        Bot bot = this.bots.getABot();
        if (bot == null)
        {
            showError("No bot to talk to!");
            return;
        }
        this.botid = bot.getID();
        this.botName = bot.getPropertyValue(this.botNamePredicate);

        while (true && this.core.getStatus() == Core.Status.READY)
        {
            showPrompt();
            String commandLine = null;
            try
            {
                commandLine = this.consoleIn.readLine();
            }
            catch (IOException e)
            {
                throw new DeveloperError("Cannot read from console!", e);
            }
            if (commandLine == null)
            {
                /*
                 * A null line probably means that the shell is being mistakenly
                 * run in interactive mode when in fact there is no System.in
                 * available. In this case, sleep for 100 days :-) and wait to
                 * be interrupted.
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
            if (commandLine.indexOf('#') == 0)
            {
                // Ignore this -- it's a comment.
            }
            else if (commandLine.indexOf('/') == 0)
            {
                // Exit command
                if (commandLine.toLowerCase().equals(EXIT))
                {
                    printExitMessage();
                    this.core.shutdown();
                    return;
                }
                // otherwise...
                // Try to find a command to handle this.
                ShellCommand command = null;
                try
                {
                    command = this.commandRegistry.getHandlerFor(commandLine);
                    try
                    {
                        command.handle(commandLine, this);
                    }
                    catch (UserError e)
                    {
                        showError("Error processing command: " + e.getMessage());
                    }
                }
                catch (NoSuchCommandException e)
                {
                    // May be a commandable.
                    try
                    {
                        commandCommandable(commandLine);
                    }
                    catch (NoCommandException ee)
                    {
                        showError("Please specify a command following the commandable.  For a list of commandables, type \"" + ListCommandablesCommand.COMMAND_STRING + "\".");
                    }
                    catch (NoSuchCommandableException ee)
                    {
                        showError("No such commandable is loaded.");
                    }
                }
            }
            else if (commandLine.length() > 0)
            {
                showConsole(this.botName, XMLKit.filterViaHTMLTags(this.core.getResponse(commandLine, this.hostname, this.botid)));
            }
            // If the command line has zero length, ignore it.
        }
    }
    
    /**
     * Allows an external class to call a command 
     * by sending a command line.  Prints the command
     * line to the console so it's possible to see
     * what was attempted.
     * 
     * @param commandLine the command line to process
     * @throws NoSuchCommandException if the command line did not contain a command that could be processed
     */
    public void processCommandLine(String commandLine) throws NoSuchCommandException
    {
        this.consolePrompt.println(timestamp() + this.hostname + PROMPT + commandLine);
        this.commandRegistry.getHandlerFor(commandLine).handle(commandLine, this);
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
     * @param preprompt the text to show before the prompt
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
     * @param message the message to display
     */
    public void showMessage(String message)
    {
        printlnOut(message);
    }

    /**
     * <p>
     * Displays an error message (no prompt) in an interactive console.
     * </p>
     * 
     * @param message the message to display
     */
    public void showError(String message)
    {
        printlnErr(message);
    }

    /**
     * <p>
     * Displays a multi-line message (after a prompt) in an interactive console.
     * </p>
     * 
     * @param preprompt the text to show before the prompt
     * @param message the multi-line message to display
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
     * @param message the message to print
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
     * Print a message line of error to the console. TODO: Make the formatting
     * different here.
     * 
     * @param message the message to print
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
     * Tells the Shell that something else was printed to the console; not
     * midLine anymore.
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
     * @return the Core in use
     */
    public Core getCore()
    {
        return this.core;
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
     * @return the command registry
     */
    public Collection<ShellCommand> getCommands()
    {
        return this.commandRegistry.getValues();
    }
    
    /**
     * @return the Bots object used by this shell
     */
    public Bots getBots()
    {
        return this.bots;
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
        showConsole(this.botName, XMLKit.filterViaHTMLTags(this.core.getResponse(this.core.getSettings().getConnectString(), this.hostname,
                this.botid, new TextResponder())));
    }

    /**
     * Sends a command to a shell commandable, if possible.
     * 
     * @param command the command (including the shell commandable name)
     * @throws NoCommandException if no command is given
     * @throws NoSuchCommandableException if an invalid commandable is specified
     */
    private void commandCommandable(String command) throws NoCommandException, NoSuchCommandableException
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
                ShellCommandable candidate = (ShellCommandable) process;
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