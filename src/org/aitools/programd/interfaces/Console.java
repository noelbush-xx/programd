/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aitools.programd.Core;
import org.aitools.programd.CoreListener;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.UserError;

/**
 * Creating a Console essentially means that loggers (as configured)
 * will (may) also print output to the console.
 * 
 * @author Noel Bush
 * @since 4.2
 */
public class Console implements CoreListener
{
    /** The settings for this console. */
    private ConsoleSettings settings;
    
    /** The stdout handler. */
    private StdStreamHandler stdOutHandler;

    /** The stderr handler. */
    private StdStreamHandler stdErrHandler;

    /** The Shell that will (may) be activated for this console. */
    private Shell shell;
    
    /**
     * Creates a <code>Console</code> with default output streams.
     * @param settingsPath the path to the settings file for the console
     */
    public Console(String settingsPath)
    {
        initialize(settingsPath, System.out, System.err);
    }
    
    /**
     * Creates a <code>Console</code> with specified input, output and prompt streams
     * (this implies that a shell will be enabled).
     * @param settingsPath the path to the settings file
     * @param out the stream to use for normal messages
     * @param err the stream to use for error messages
     */
    public Console(String settingsPath, PrintStream out, PrintStream err)
    {
        initialize(settingsPath, out, err);
    }
    
    /**
     * Performs initialization common to all constructors.
     * 
     * @param settingsPath the path for the settings file to use
     * @param out the stream to use for normal output messages
     * @param err the stream to use for error messages
     */
    private void initialize(String settingsPath, PrintStream out, PrintStream err)
    {
        this.settings = new ConsoleSettings(settingsPath);

        // Messages to all logs will go up to the parent "programd" log, and out to the console.
        this.stdOutHandler = new StdStreamHandler(this.settings, out, new StdOutFilter());
        Logger.getLogger("programd").addHandler(this.stdOutHandler);
        
        // Error messages will be printed to stderr.
        this.stdErrHandler = new StdStreamHandler(this.settings, err, new StdErrFilter());
        Logger.getLogger("programd").addHandler(this.stdErrHandler);
    }
    
    /**
     * Attaches the Console to the given Core.
     * @param core the Core to which to attach
     */
    public void attachTo(Core core)
    {
        core.attach(this);
        if (this.settings.useShell())
        {
            addShell(new Shell(this.settings), core);
        }
        else
        {
            Logger.getLogger("programd").log(Level.INFO, "Interactive shell disabled.  Awaiting SIGHUP to shut down.");
        }
    }
    
    /**
     * Adds the given Shell to the Console
     * @param shellToAdd    the Shell to add
     * @param core  the core to which to attach the Shell
     */
    public void addShell(Shell shellToAdd, Core core)
    {
        this.shell = shellToAdd;
        this.shell.attachTo(core);
        this.stdOutHandler.watch(this.shell);
        this.stdErrHandler.watch(this.shell);
    }
    
    /**
     * Starts the Shell.
     * @see org.aitools.programd.CoreListener#coreReady()
     */
    public void coreReady()
    {
        if (this.settings.useShell())
        {
            this.shell.start();
        }
    }
    
    /**
     * Prints the stack trace associated with the given Throwable.
     * @param e the Throwable that caused the failure.
     * @see org.aitools.programd.CoreListener#failure(java.lang.Throwable)
     */
    public void failure(Throwable e)
    {
        if (e instanceof UserError || e instanceof DeveloperError)
        {
            e.getCause().printStackTrace(System.err);
        }
        else
        {
            e.printStackTrace(System.err);
        }
    }
    
    /**
     * @return the console settings
     */
    public ConsoleSettings getSettings()
    {
        return this.settings;
    }
}
