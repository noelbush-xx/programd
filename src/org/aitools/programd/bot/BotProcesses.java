/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.bot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controls processes that run in separate threads and need to be shut down
 * before the bot exits.
 */
public class BotProcesses
{
    /** The registry of all bot processes. */
    private static HashMap<String, BotProcess> registry = new HashMap<String, BotProcess>();

    /**
     * Adds a process to the registry and starts it.
     * 
     * @param process
     *            the process to add
     * @param name
     *            a name by which its thread will be identified
     */
    public static void start(BotProcess process, String name)
    {
        registry.put(name, process);
        Thread botProcess = new Thread(process, name);

        // Set the thread as a daemon, in case the server terminates abnormally.
        botProcess.setDaemon(true);

        // Start the thread.
        botProcess.start();
    } 

    /**
     * Returns an iterator on the registry.
     * 
     * @return an iterator on the registry
     */
    public static Iterator getRegistryIterator()
    {
        return registry.values().iterator();
    } 

    /**
     * Returns a given process.
     * 
     * @param name
     *            the name of the process
     * @return the process assigned to the name
     */
    public static BotProcess get(String name)
    {
        return registry.get(name);
    } 

    /**
     * Shuts down all registered processes.
     */
    public static void shutdownAll()
    {
        Logger logger = Logger.getLogger("programd");
        logger.log(Level.INFO, "Shutting down all BotProcesses.");
        for (BotProcess process : registry.values())
        {
            logger.log(Level.FINE, "Shutting down " + process);
            process.shutdown();
        } 
        logger.log(Level.INFO, "Finished shutting down BotProcesses.");
    } 

    /**
     * Prevents instantiation of this class.
     */
    private BotProcesses()
    {
        // Nothing to do.
    } 
}