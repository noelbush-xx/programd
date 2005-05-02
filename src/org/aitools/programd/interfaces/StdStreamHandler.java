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
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import org.aitools.programd.interfaces.shell.Shell;

/**
 * A <code>StdStreamHandler</code> publishes any record it's passed to the
 * given <code>stream</code>, except those messages which are discarded by
 * the given {@link StdFilter}.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.2
 */
public class StdStreamHandler extends StreamHandler
{
    /** A Shell to watch. */
    private Shell shell;

    /**
     * Creates a new StdStreamHandler.
     * 
     * @param consoleSettings the console settings to use
     * @param stream the stream to handle
     * @param filter the filter to use
     */
    public StdStreamHandler(ConsoleSettings consoleSettings, PrintStream stream, StdFilter filter)
    {
        super(stream, new ConsoleFormatter(consoleSettings));
        setFilter(filter);
        setLevel(Level.ALL);
    }

    /**
     * Assigns the given Shell to this handler, to watch it.
     * 
     * @param shellToWatch
     */
    public void watch(Shell shellToWatch)
    {
        this.shell = shellToWatch;
    }

    /**
     * Publishes the given record, also to the shell if one is attached.
     * 
     * @see java.util.logging.StreamHandler#publish(java.util.logging.LogRecord)
     */
    public void publish(LogRecord record)
    {
        super.publish(record);
        if (this.shell != null)
        {
            this.shell.gotLine();
        }
        super.flush();
    }
}
