/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is a trivial extension of {@link LinkedHashMap}that is limited to a
 * given number of entries.
 * 
 * @author Noel Bush
 */
public class LRUCache extends LinkedHashMap
{
    private static int maxEntries;

    /**
     * Creates a new <code>LRUCache</code> with <code>maxEntries</code>
     * maximum entries. The eldest will be automatically removed.
     * 
     * @param maxEntriesToUse
     *            the maximum capacity of the cache
     */
    public LRUCache(int maxEntriesToUse)
    {
        super();
        LRUCache.maxEntries = maxEntriesToUse;
    }

    protected boolean removeEldestEntry(Map.Entry eldest)
    {
        return size() > maxEntries;
    }
}