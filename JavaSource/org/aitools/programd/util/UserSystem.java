/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

/**
 * Contains utilities for getting/setting system information.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class UserSystem
{
    private static double BYTES_PER_MB = 1024.0 * 1024.0; 
    
    /**
     * Returns a description of the JVM.
     * 
     * @return a description of the JVM
     */
    public static String jvmDescription()
    {
        return String.format("Using Java VM %s from %s.",
                            System.getProperty("java.vm.version"),
                            System.getProperty("java.vendor"));
    }
    
    /**
     * Returns a description of the operating system and processor configuration.
     * 
     * @return a description of the operating system and processor configuration
     */
    @SuppressWarnings("boxing")
    public static String osDescription()
    {
        int processorCount = Runtime.getRuntime().availableProcessors();
        return String.format("Running on %s version %s (%s) with %d processor%s available.",
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"),
                processorCount,
                processorCount == 1 ? "" : "s");
    }
    
    /**
     * Returns a report of used and available memory.
     * 
     * @return a report of used and available memory
     */
    @SuppressWarnings("boxing")
    public static String memoryReport()
    {
        Runtime runtime = Runtime.getRuntime();
        long freemem = runtime.freeMemory();
        long totalmem = runtime.totalMemory();
        return String
        .format(
                "%.1f MB of memory free out of %.1f MB total in JVM (%.1f MB used).  Configured maximum: %.1f MB.",
                freemem / BYTES_PER_MB,
                totalmem / BYTES_PER_MB,
                (totalmem - freemem) / BYTES_PER_MB,
                runtime.maxMemory() / BYTES_PER_MB);
    }
}
