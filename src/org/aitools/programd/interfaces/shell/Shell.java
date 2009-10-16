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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.aitools.programd.Bot;
import org.aitools.programd.Bots;
import org.aitools.programd.Core;
import org.aitools.programd.predicates.PredicateManager;
import org.aitools.programd.util.ManagedProcess;
import org.aitools.util.runtime.Errors;
import org.aitools.util.runtime.UserError;
import org.aitools.util.xml.XHTML;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * Provides a simple shell for interacting with the bot at a command line.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @author Jon Baer
 * @author Eion Robb
 */
public class Shell extends Thread
{
    /** The command registry. */
    private ShellCommandRegistry commandRegistry;

    /** The Core to which this Shell is attached. */
    private Core _core;

    /** The PredicateMaster in use by the attached Core. */
    private PredicateManager predicateMaster;

    /** The Bots object in use by the attached Core. */
    private Bots bots;

    /** A BufferedReader for user input to the shell. */
    private BufferedReader inReader;

    /** Where regular console output will go. */
    private PrintStream outStream;

    /** Where console errors will go. */
    private PrintStream errStream;

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

    /** An indicator used to keep track of whether we're midline in a console output (i.e., showing a prompt). */
    private boolean midLine = false;
    
    private static final Namespace PLUGIN_CONFIG_NS = Namespace.getNamespace(Core.PLUGIN_CONFIG_NS_URI);

    /**
     * A <code>Shell</code> with default input and output streams ( <code>System.in</code> and
     * <code>System.out</code>).
     * 
     */
    public Shell()
    {
        super("Shell");
        this.inReader = new BufferedReader(new InputStreamReader(System.in));
        this.outStream = this.errStream = this.consolePrompt = System.out;
        this.setDaemon(true);
    }

    /**
     * A <code>Shell</code> with custom input and output streams.
     * 
     * @param in the input stream
     * @param out the output stream
     * @param err the error stream
     * @param prompt the prompt output stream
     */
    public Shell(InputStream in, PrintStream out, PrintStream err, PrintStream prompt)
    {
        super("Shell");
        this.inReader = new BufferedReader(new InputStreamReader(in));
        this.outStream = out;
        this.errStream = err;
        this.consolePrompt = prompt;
        this.setDaemon(true);
    }

    /**
     * Attach this shell to the given core.
     * 
     * @param core
     */
    @SuppressWarnings("unchecked")
    public void attachTo(Core core)
    {
        this._core = core;
        this.botNamePredicate = this._core.getSettings().getBotNameProperty();
        this.predicateMaster = this._core.getPredicateMaster();
        this.bots = this._core.getBots();
        this.clientNamePredicate = this._core.getSettings().getClientNamePredicate();
        this.hostname = this._core.getHostname();

        this.commandRegistry = new ShellCommandRegistry();

        // Look for any shell command plugins and add them to the registry.
        Document plugins = this._core.getPluginConfig(); 
        if (plugins != null)
        {
            Element shellCommandSet = plugins.getRootElement().getChild("shell-commands", PLUGIN_CONFIG_NS);
            if (shellCommandSet != null)
            {
                List<Element> commands = shellCommandSet.getChildren("command", PLUGIN_CONFIG_NS);
                if (commands != null)
                {
                    for (Element commandElement : commands)
                    {
                        String classname = commandElement.getAttributeValue("class");
                        List<Element> parameterElements = commandElement.getChildren("parameter", PLUGIN_CONFIG_NS);
                        if (parameterElements != null)
                        {
                            HashMap<String, String> parameters = new HashMap<String, String>(parameterElements.size());
                            for (Element parameter : parameterElements)
                            {
                                parameters.put(parameter.getAttributeValue("name"), parameter.getAttributeValue("value"));
                            }
                            this.commandRegistry.register(classname, parameters);
                        }
                    }
                }
            }
        }
    }

    /**
     * Runs the shell.
     */
    @Override
    public void run()
    {
        if (this._core == null)
        {
            throw new NullPointerException("Must attach the shell to a Core before calling run()!");
        }

        showMessage(String.format("Interactive shell: type \"/exit\" to shut down; \"%s\" for help.", HelpCommand.COMMAND_STRING));
        Bot bot = this.bots.getABot();
        if (bot == null)
        {
            throw new NullPointerException("No bot to talk to!");
        }
        this.botid = bot.getID();
        this.botName = bot.getPropertyValue(this.botNamePredicate);

        while (true /* && this.core.getStatus() == Core.Status.READY */)
        {
            showPrompt();
            String commandLine = null;
            try
            {
                commandLine = this.inReader.readLine();
            }
            catch (IOException e)
            {
                noShell();
            }
            this.midLine = false;

            // Handle commands.
            if (commandLine != null)
            {
                if (commandLine.indexOf('#') == 0)
                {
                    // Ignore this -- it's a comment.
                }
                else if (commandLine.indexOf('/') == 0)
                {
                    // Exit command
                    if ("/exit".toLowerCase().equals(commandLine))
                    {
                        printExitMessage();
                        this._core.shutdown();
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
                            showError(String.format("Error processing command: \"%s\"", Errors.describe(e)));
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
                            showError("Please specify a command following the commandable.  For a list of commandables, type \""
                                    + ListCommandablesCommand.COMMAND_STRING + "\".");
                        }
                        catch (NoSuchCommandableException ee)
                        {
                            showError("No such commandable is loaded.");
                        }
                    }
                }
                else if (commandLine.length() > 0)
                {
                    showConsole(this.botName, XHTML.breakLines(this._core.getResponse(commandLine, this.hostname,
                            this.botid)));
                }
                // If the command line has zero length, ignore it.
            }
        }
    }

    /**
     * Notes that the shell will not run, and sleeps.
     */
    protected void noShell()
    {
        this._core.getLogger().warn("No input stream found; shell is disabled.");
        while (true)
        {
            try
            {
                Thread.sleep(86400000);
            }
            catch (InterruptedException e)
            {
                this._core.getLogger().warn("Shell was interrupted; shell will not run anymore.");
            }
        }
    }

    /**
     * Allows an external class to call a command by sending a command line. Prints the command line to the console so
     * it's possible to see what was attempted.
     * 
     * @param commandLine the command line to process
     * @throws NoSuchCommandException if the command line did not contain a command that could be processed
     */
    public void processCommandLine(String commandLine) throws NoSuchCommandException
    {
        this.consolePrompt.println(String.format("%s> %s", this.hostname, commandLine));
        this.commandRegistry.getHandlerFor(commandLine).handle(commandLine, this);
    }

    /**
     * Displays a prompt.
     */
    protected void showPrompt()
    {
        if (this.getState() != Thread.State.NEW)
        {
            promptConsole('[' + this.botName + "] "
                    + this.predicateMaster.get(this.clientNamePredicate, this.hostname, this.botid).trim());
        }
    }

    /**
     * Displays a line for an interactive console, including the prompt.
     * 
     * @param preprompt the text to show before the prompt
     */
    protected void promptConsole(String preprompt)
    {
        if (this.midLine)
        {
            this.consolePrompt.println();
        }
        this.consolePrompt.print(String.format("%s> ", preprompt));
        this.midLine = true;
    }

    /**
     * Displays a regular message (no prompt) in an interactive console.
     * 
     * @param message the message to display
     */
    public void showMessage(String message)
    {
        printlnOut(message);
    }

    /**
     * Displays an error message (no prompt) in an interactive console.
     * 
     * @param message the message to display
     */
    public void showError(String message)
    {
        printlnErr(message);
    }

    /**
     * Displays a multi-line message (after a prompt) in an interactive console.
     * 
     * @param preprompt the text to show before the prompt
     * @param message the multi-line message to display
     */
    protected void showConsole(String preprompt, String[] message)
    {
        for (int index = 0; index < message.length; index++)
        {
            printlnOut(String.format("%s> %s", preprompt, message[index]));
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
            this.outStream.println();
        }
        this.outStream.println(message);
        this.midLine = false;
    }

    /**
     * Print a message line of error to the console.
     * 
     * @param message the message to print
     */
    public void printlnErr(String message)
    {
        if (this.midLine)
        {
            this.errStream.println();
        }
        this.errStream.println(message);
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
    protected void printExitMessage()
    {
        Logger.getLogger("programd").info("Exiting at user request.");
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
        return this._core;
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
        if (!this.bots.containsKey(newBotID))
        {
            showError("That bot id is not known. Check your startup files.");
            return;
        }
        this.botid = newBotID;
        this.botName = this.bots.get(newBotID).getPropertyValue(this.botNamePredicate);
        showMessage("Switched to bot \"" + newBotID + "\" (name: \"" + this.botName + "\").");
        // Send the connect string and print the first response.
        showConsole(this.botName, XHTML.breakLines(this._core.getResponse(this._core.getSettings()
                .getConnectString(), this.hostname, this.botid)));
    }

    /**
     * Sends a command to a shell commandable, if possible.
     * 
     * @param command the command (including the shell commandable name)
     * @throws NoCommandException if no command is given
     * @throws NoSuchCommandableException if an invalid commandable is specified
     */
    protected void commandCommandable(String command) throws NoCommandException, NoSuchCommandableException
    {
        // Parse out the commandable.
        int space = command.indexOf(' ');
        if (space == -1)
        {
            throw new Shell.NoCommandException();
        }
        if (space == command.length())
        {
            throw new Shell.NoCommandException();
        }

        String commandableID = command.substring(1, space);
        ShellCommandable commandable = null;

        for (ManagedProcess process : this._core.getManagedProcesses().values())
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
            throw new Shell.NoSuchCommandableException();
        }
        commandable.processShellCommand(command.substring(space + 1));
    }

    /**
     * An exception thrown if no command is specified.
     */
    public class NoCommandException extends Exception
    {
        // No body.
    }

    /**
     * An exception thrown if an invalid commandable is specified.
     */
    public class NoSuchCommandableException extends Exception
    {
        // No body.
    }
}
