/*
 * aitools utilities
 * Copyright (C) 2006 Noel Bush
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.aitools.util.runtime;

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
        return String.format("Using Java VM %s from %s.", System.getProperty("java.vm.version"), System
                .getProperty("java.vendor"));
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
        return String.format("Running on %s version %s (%s) with %d processor%s available.", System
                .getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"),
                processorCount, processorCount == 1 ? "" : "s");
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
        return String.format(
                "%.1f MB of memory free out of %.1f MB total in JVM (%.1f MB used).  Configured maximum: %.1f MB.",
                freemem / BYTES_PER_MB, totalmem / BYTES_PER_MB, (totalmem - freemem) / BYTES_PER_MB, runtime
                        .maxMemory()
                        / BYTES_PER_MB);
    }
}
