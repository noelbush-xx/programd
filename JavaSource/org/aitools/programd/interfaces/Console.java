/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces;

import java.io.OutputStreamWriter;
import java.io.PrintStream;

import org.aitools.programd.Core;
import org.aitools.programd.Core.Status;
import org.aitools.programd.interfaces.shell.Shell;
import org.apache.log4j.Logger;

/**
 * Creating a Console essentially means that loggers (as configured) will (may)
 * also print output to the console.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.2
 */
public class Console
{
    /** The Core to which this console is (may be) attached. */
    private Core core;

    /** The stdout handler. */
    private ConsoleStreamAppender stdOutAppender;

    /** The stderr handler. */
    private ConsoleStreamAppender stdErrAppender;

    /** The Shell that will (may) be activated for this console. */
    private Shell shell;

    /**
     * Creates a <code>Console</code> with default output streams.
     */
    public Console()
    {
        initialize(System.out, System.err);
    }

    /**
     * Creates a <code>Console</code> with specified input, output and prompt
     * streams (this implies that a shell will be enabled).
     * 
     * @param out the stream to use for normal messages
     * @param err the stream to use for error messages
     */
    public Console(PrintStream out, PrintStream err)
    {
        initialize(out, err);
    }

    /**
     * Sets up the stdout and stderr appenders (if they are
     * defined in the log4j configuration).
     * 
     * @param out the stream to use for normal output messages
     * @param err the stream to use for error messages
     */
    private void initialize(PrintStream out, PrintStream err)
    {
        this.stdOutAppender = ((ConsoleStreamAppender)Logger.getLogger("programd").getAppender("stdout"));
        if (this.stdOutAppender != null)
        {
            this.stdOutAppender.setWriter(new OutputStreamWriter(out));
        }
        
        this.stdErrAppender = ((ConsoleStreamAppender)Logger.getLogger("programd").getAppender("stderr"));
        if (this.stdErrAppender != null)
        {
            this.stdErrAppender.setWriter(new OutputStreamWriter(err));
        }
    }
    
    /**
     * Attaches the console to the given core.
     * 
     * @param coreToUse the core to which to attach
     */
    public void attachTo(Core coreToUse)
    {
        this.core = coreToUse;

        if (this.core.getSettings().consoleUseShell())
        {
            addShell(new Shell(), this.core);
        }
        else
        {
            Logger.getLogger("programd").info("Interactive shell disabled.  Awaiting SIGHUP to shut down.");
        }
    }

    /**
     * Adds the given Shell to the Console
     * 
     * @param shellToAdd the Shell to add
     * @param coreToUse the core to which to attach the Shell
     */
    public void addShell(Shell shellToAdd, Core coreToUse)
    {
        this.shell = shellToAdd;
        this.shell.attachTo(coreToUse);
        if (this.stdOutAppender != null)
        {
            this.stdOutAppender.watch(this.shell);
        }
        if (this.stdErrAppender != null)
        {
            this.stdErrAppender.watch(this.shell);
        }
    }

    /**
     * Starts the attached shell (if one exists and the shell is enabled).
     */
    public void startShell()
    {
        if (this.core.getSettings().consoleUseShell())
        {
            this.shell.start();
        }

        // Now just run as long as the core status stays at READY.
        while (this.core.getStatus() == Status.READY)
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                this.core.getLogger().warn("Console was interrupted; shell will not run anymore.");
            }
        }
    }
}
