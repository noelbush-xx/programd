/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces;

import java.io.OutputStream;

import org.aitools.programd.interfaces.shell.Shell;
import org.apache.log4j.Level;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

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
     */
    public ShellStreamAppender()
    {
        super();
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
    
    public synchronized void setWriter(OutputStream stream)
    {
        super.setWriter(createWriter(stream));
    }

    /**
     * @return whether the writer has been set
     */
    public boolean isWriterSet()
    {
        return this.qw != null;
    }
}
