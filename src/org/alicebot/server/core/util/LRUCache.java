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

import java.util.LinkedHashMap;
import java.util.Map;


/**
 *  This is a trivial extension of {@link LinkedHashMap} that is
 *  limited to a given number of entries.
 *
 *  @author	Noel Bush
 */
public class LRUCache extends LinkedHashMap
{
    private static int maxEntries;
    
    
    /**
     *  Creates a new <code>LRUCache</code> with
     *  <code>maxEntries</code> maximum entries.
     *  The eldest will be automatically removed.
     *
     *  @param maxEntries	the maximum capacity of the cache
     */
    public LRUCache(int maxEntries)
    {
        super();
        this.maxEntries = maxEntries;
    }
 
    protected boolean removeEldestEntry(Map.Entry eldest)
    {
        return size() > maxEntries;
    }    
}
