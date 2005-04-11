/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd;

/**
 * Runs the shutdown method of a Core when it is started. This is intended to be
 * attached via
 * {@link java.lang.Runtime#addShutdownHook Runtime.addShutdownHook()} for any
 * configuration where it is possible that the entire system may exit. Of course
 * this doesn't apply to many cases (such as if the Core is a component in an
 * application server), so this may not always be used. When this class's
 * {@link #run} method is called, it first checks that the {@link Core} has not
 * been shut down already.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class CoreShutdownHook extends Thread
{
    private Core core;

    /**
     * Creates a new <code>CoreShutdownHook</code> to manage the shutdown of
     * the given Core
     * 
     * @param aCore the Core whose shutdown method should be run when this
     *            thread is run
     */
    public CoreShutdownHook(Core aCore)
    {
        super("Core Shutdown Thread");
        this.core = aCore;
    }

    /**
     * Shuts down the Core assigned to this thread.
     * 
     * @see Thread#run
     */
    public void run()
    {
        // The Core may already have been shut down -- don't do it again.
        if (this.core.getStatus() != Core.Status.SHUT_DOWN)
        {
            this.core.shutdown();
        }
    }
}
