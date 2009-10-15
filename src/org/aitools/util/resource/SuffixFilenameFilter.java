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

package org.aitools.util.resource;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Provides a simple filename filter given an array of acceptable suffixes.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class SuffixFilenameFilter implements FilenameFilter
{
    /** The suffixes that will be used to determine acceptable files. */
    private static String[] SUFFIXES;

    /**
     * Constructs a new filename filter given an array of acceptable suffixes.
     * 
     * @param suffixes the acceptable suffixes
     */
    public SuffixFilenameFilter(String[] suffixes)
    {
        SUFFIXES = suffixes;
    }

    /**
     * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
     */
    public boolean accept(@SuppressWarnings("unused")
    File dir, String name)
    {
        if (name == null)
        {
            return false;
        }
        if (name.length() == 0)
        {
            return false;
        }
        for (int index = SUFFIXES.length; --index >= 0;)
        {
            if (name.endsWith(SUFFIXES[index]))
            {
                return true;
            }
        }
        return false;
    }
}
