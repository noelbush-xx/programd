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
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * A <code>ConsoleStreamAppender</code> publishes any record it's passed to the
 * given <code>stream</code>.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.6
 */
public class ConsoleStreamAppender extends WriterAppender
{
    /** A Shell to watch. */
    private Shell shell;

    /** Whether to print stack traces for exceptions. */
    private boolean printStackTraces = false;

    /**
     * Creates a new ConsoleStreamAppender.
     */
    public ConsoleStreamAppender()
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
    
    /**
     * Specifies whether or not to print stack traces for &quot;caught&quot;
     * exceptions (for uncaught exceptions, see {@link org.aitools.programd.CoreSettings}).
     * 
     * @param value whether or not to print stack traces for &quot;caught&quot; exceptions
     */
    public void setPrintStackTraces(boolean value)
    {
        this.printStackTraces = value;
    }

    /**
     * Overrides this method of the parent class so that it will not print stack
     * traces if it isn't supposed to.
     * 
     * @see org.apache.log4j.WriterAppender#subAppend(org.apache.log4j.spi.LoggingEvent)
     */
    @Override
    protected void subAppend(LoggingEvent event)
    {
        this.qw.write(this.layout.format(event));

        // This if statement is the only addition.
        if (this.printStackTraces)
        {
            if (this.layout.ignoresThrowable())
            {
                String[] s = event.getThrowableStrRep();
                if (s != null)
                {
                    int len = s.length;
                    for (int i = 0; i < len; i++)
                    {
                        this.qw.write(s[i]);
                        this.qw.write(Layout.LINE_SEP);
                    }
                }
            }
        }
        else
        {
        	ThrowableInformation ti = event.getThrowableInformation();
        	if (ti != null)
        	{
        		Throwable t = ti.getThrowable();
        		if (t != null)
        		{
        			String message = t.getMessage();
        			if (message != null)
        			{
        	            this.qw.write(event.getThrowableInformation().getThrowable().getMessage());
        	            this.qw.write(Layout.LINE_SEP);
        			}
        		}
        	}
        }

        if (this.immediateFlush)
        {
            this.qw.flush();
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
