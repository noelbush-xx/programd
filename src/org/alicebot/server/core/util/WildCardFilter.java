/*
    Alicebot Program D
    Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

package org.alicebot.server.core.util;

import java.io.File;
import java.io.FilenameFilter;


/**
 *  <p>
 *  Implements a simple wildcard file filter.
 *  </p>
 *  <p>
 *  Taken, with gratitude from the <a href="http://sourceforge.net/projects/jmk/">JMK</a>
 *  project.  (Under the GNU LGPL)
 *  </p>
 *
 *  @author John D. Ramsdell, Olivier Refalo
 *  @see <a href="http://sourceforge.net/projects/jmk/">JMK</a>
 */
public final class WildCardFilter implements FilenameFilter
{
    private String pattern;
    private char wildCard;
    private int[] wildIndex;
    private String prefix;
    private String suffix;

    public WildCardFilter(String pattern, char wildCard)
    {
        this.pattern = pattern;
        this.wildCard = wildCard;
        int wilds = 0;
        for (int index = 0; index < pattern.length(); index++)
        {
            if (wildCard == pattern.charAt(index))
            {
                wilds++;
            }
        }
        wildIndex = new int[wilds];
        int windex = 0;
        for (int index = 0; windex < wilds; index++)
        {
            if (wildCard == pattern.charAt(index))
            {
                wildIndex[windex++] = index;
            }
        }
        if (wilds == 0)
        {
            prefix = null;
            suffix = null;
        }
        else
        {
            prefix = pattern.substring(0, wildIndex[0]);
            suffix = pattern.substring(wildIndex[wilds - 1] + 1);
        }
    }

    public boolean accept(File dir, String name)
    {
        if (wildIndex.length == 0)
        {
            return pattern.equals(name);
        }
        else if (!name.startsWith(prefix) || !name.endsWith(suffix))
        {
            return false;
        }
        else if (wildIndex.length == 1)
        {
            return true;
        }
        else
        {
            int flen = name.length() - suffix.length();
            int windex = wildIndex[0];
            int findex = windex;		// index into file
            for (int index = 1; index < wildIndex.length; index++)
            {
                /* index is the index into wildIndex */
                /* windex is wildIndex[index - 1] at loop start */
                /* pattern matched is pattern.substring(windex + 1, wildIndex[index]) */
                int pstart = windex + 1;
                windex = wildIndex[index];
                int plen = windex - pstart;
                // Find pattern in rest of name
                for (;;)
                {		
                    if (plen + findex > flen)
                    {
                        return false;
                    }
                    else if (name.regionMatches(findex, pattern, pstart, plen))
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
