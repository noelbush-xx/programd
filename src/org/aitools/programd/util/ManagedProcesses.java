/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aitools.programd.Core;

/**
 * Controls processes that run in separate threads and need to be shut down
 * before the bot exits.
 */
public class ManagedProcesses
{
    /** The Core to which this is attached. */
    private Core core;
    
    /** The registry of all processes. */
    private Map<String, ManagedProcess> registry = Collections.checkedMap(new HashMap<String, ManagedProcess>(), String.class, ManagedProcess.class);

    /**
     * Adds a process to the registry and starts it.
     * 
     * @param process the process to add
     * @param name a name by which its thread will be identified
     */
    public void start(ManagedProcess process, String name)
    {
        this.registry.put(name, process);
        Thread botProcess = new Thread(process, name);

        // Set the thread as a daemon, in case the server terminates abnormally.
        botProcess.setDaemon(true);

        try
        {
            // Start the thread.
            botProcess.start();
        }
        catch (Throwable e)
        {
            this.core.fail(e);
        }
    }

    /**
     * Returns an iterator on the registry.
     * 
     * @return an iterator on the registry
     */
    public Collection<ManagedProcess> values()
    {
        return this.registry.values();
    }

    /**
     * Returns a given process.
     * 
     * @param name the name of the process
     * @return the process assigned to the name
     */
    public ManagedProcess get(String name)
    {
        return this.registry.get(name);
    }

    /**
     * Shuts down all registered processes.
     */
    public void shutdownAll()
    {
        Logger logger = Logger.getLogger("programd");
        logger.log(Level.INFO, "Shutting down all ManagedProcesses.");
        for (ManagedProcess process : this.registry.values())
        {
            logger.log(Level.FINE, "Shutting down " + process);
            process.shutdown();
        }
        logger.log(Level.INFO, "Finished shutting down ManagedProcesses.");
    }

    /**
     * Creates a ManagedProcesses object.
     * @param coreToUse the Core to which to attach this
     */
    public ManagedProcesses(Core coreToUse)
    {
        this.core = coreToUse;
    }
}