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
 * <p>
 * Implements a simple wildcard file filter.
 * </p>
 * <p>
 * Taken, with gratitude from the <a
 * href="http://sourceforge.net/projects/jmk/">JMK </a> project. (Under the GNU
 * LGPL)
 * </p>
 * 
 * @author John D. Ramsdell, Olivier Refalo
 * @see <a href="http://sourceforge.net/projects/jmk/">JMK </a>
 */
public final class WildCardFilter implements FilenameFilter
{
    private String pattern;

    private char wildCard;

    private int[] wildIndex;

    private String prefix;

    private String suffix;

    /**
     * Creates a new WildCardFilter that will use the given pattern and the
     * given wildcard.
     * 
     * @param patternToUse the pattern to use
     * @param wildCardToUse the wildcard to use
     */
    public WildCardFilter(String patternToUse, char wildCardToUse)
    {
        this.pattern = patternToUse;
        this.wildCard = wildCardToUse;
        int wilds = 0;
        for (int index = 0; index < this.pattern.length(); index++)
        {
            if (this.wildCard == this.pattern.charAt(index))
            {
                wilds++;
            }
        }
        this.wildIndex = new int[wilds];
        int windex = 0;
        for (int index = 0; windex < wilds; index++)
        {
            if (this.wildCard == this.pattern.charAt(index))
            {
                this.wildIndex[windex++] = index;
            }
        }
        if (wilds == 0)
        {
            this.prefix = null;
            this.suffix = null;
        }
        else
        {
            this.prefix = this.pattern.substring(0, this.wildIndex[0]);
            this.suffix = this.pattern.substring(this.wildIndex[wilds - 1] + 1);
        }
    }

    /**
     * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
     */
    public boolean accept(File dir, String name)
    {
        if (this.wildIndex.length == 0)
        {
            return this.pattern.equals(name);
        }
        else if (!name.startsWith(this.prefix) || !name.endsWith(this.suffix))
        {
            return false;
        }
        else if (this.wildIndex.length == 1)
        {
            return true;
        }
        else
        {
            int flen = name.length() - this.suffix.length();
            int windex = this.wildIndex[0];
            int findex = windex; // index into file
            for (int index = 1; index < this.wildIndex.length; index++)
            {
                /* index is the index into wildIndex */
                /* windex is wildIndex[index - 1] at loop start */
                /*
                 * pattern matched is pattern.substring(windex + 1,
                 * wildIndex[index])
                 */
                int pstart = windex + 1;
                windex = this.wildIndex[index];
                int plen = windex - pstart;
                // Find pattern in rest of name
                for (;;)
                {
                    if (plen + findex > flen)
                    {
                        return false;
                    }
                    else if (name.regionMatches(findex, this.pattern, pstart, plen))
                    {
                        break;
                    }
                    else
                    {
                        findex++;
                    }
                }
                findex += plen;
            }
            return true;
        }
    }
}