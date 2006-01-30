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

import org.aitools.programd.interfaces.shell.Shell;
import org.apache.log4j.Level;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.varia.LevelRangeFilter;

/**
 * A <code>ShellStreamAppender</code> publishes any record it's passed to the
 * given <code>stream</code>, except those messages which are discarded by
 * the given {@link StdFilter}.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.2
 */
public class ShellStreamAppender extends WriterAppender
{
    /** A Shell to watch. */
    private Shell shell;

    /**
     * Creates a new ShellStreamAppender.
     * 
     * @param stream the stream to handle
     * @param min the minimum level to log (may be null for no minimum)
     * @param max the maximum level to log (may be null for no maximum)
     * @param settings the console settings to use to configure the console output
     */
    public ShellStreamAppender(PrintStream stream, Level min, Level max, ConsoleSettings settings)
    {
        super(new ConsoleLayout(settings), stream);
        LevelRangeFilter filter = new LevelRangeFilter();
        if (min != null)
        {
            filter.setLevelMin(min);
        }
        if (max != null)
        {
            filter.setLevelMax(max);
        }
        addFilter(filter);
        setImmediateFlush(true);
        setThreshold(Level.ALL);
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
    @Override
    public void doAppend(LoggingEvent event)
    {
        super.doAppend(event);
        if (this.shell != null)
        {
            this.shell.gotLine();
        }
    }
}
