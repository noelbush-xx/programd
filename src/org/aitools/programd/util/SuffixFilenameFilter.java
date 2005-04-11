/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

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
    public boolean accept(File dir, String name)
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