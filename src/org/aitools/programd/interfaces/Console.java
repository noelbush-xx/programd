/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.aitools.programd.Core;

/**
 * Creating a Console essentially means that loggers (as configured)
 * will (may) also print output to the console.
 * 
 * @author Noel Bush
 * @since 4.2
 */
public class Console
{
    /** The settings for this console. */
    private ConsoleSettings settings;
    
    /** The stdout handler. */
    private StdStreamHandler stdOutHandler;

    /** The stderr handler. */
    private StdStreamHandler stdErrHandler;

    /** The Shell that will (may) be activated for this console. */
    private Shell shell;
    
    public Console(String settingsPath)
    {
        this.settings = new ConsoleSettings(settingsPath);

        // Messages to all logs will go up to the parent "programd" log, and out to the console.
        this.stdOutHandler = new StdStreamHandler(this.settings, System.out, new StdOutFilter());
        Logger.getLogger("programd").addHandler(this.stdOutHandler);
        
        // Error messages will be printed to stderr.
        this.stdErrHandler = new StdStreamHandler(this.settings, System.err, new StdErrFilter());
        Logger.getLogger("programd.error").addHandler(this.stdErrHandler);
    }
    
    public void attach(Core core)
    {
        if (this.settings.useShell())
        {
            this.shell = new Shell(this.settings);
            this.shell.attach(core);
            this.stdOutHandler.watch(this.shell);
            this.stdErrHandler.watch(this.shell);
            Logger logger = Logger.getLogger("programd");
            logger.log(Level.INFO, "Starting shell.");
            this.shell.run();
            logger.log(Level.INFO, "Shell exited.");
        }
        else
        {
            Logger.getLogger("programd").log(Level.INFO, "Interactive shell disabled.  Awaiting SIGHUP to shut down.");
            /*
            while (true)
            {
                try
                {
                    Thread.sleep(86400000);
                } 
                catch (InterruptedException e)
                {
                    // That's it!
                } 
            }
            */
        }
    }
}
