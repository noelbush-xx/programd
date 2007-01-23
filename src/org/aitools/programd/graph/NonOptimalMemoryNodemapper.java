/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.graph;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * This is the most trivial, and likely the most wasteful,
 * implementation of {@link Nodemapper Nodemapper}. It does not
 * attempt to do any optimizations.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class NonOptimalMemoryNodemapper extends AbstractNodemaster
{
    /**
     * @see org.aitools.programd.graph.Nodemapper#put(java.lang.String, java.lang.Object)
     */
    public Object put(String key, Object value)
    {
        if (value instanceof String)
        {
            if (this.hidden == null)
            {
                this.hidden = new LinkedHashMap<String, Object>();
            }
            return this.hidden.put(key.toUpperCase().intern(), ((String) value).intern());
        }
        // otherwise...
        return this.hidden.put(key.toUpperCase().intern(), value);
    }

    /**
     * @see org.aitools.programd.graph.Nodemapper#remove(java.lang.Object)
     */
    public void remove(Object value)
    {
        // Find the key for this value.
        Object key = null;
        if (this.hidden != null)
        {
            for (Map.Entry<String, Object> item : this.hidden.entrySet())
            {
                if (item.getValue().equals(value))
                {
                    // Found it.
                    key = item.getKey();
                    break;
                }
            }
        }
        if (key == null)
        {
            // We didn't find a key.
            Logger.getLogger("programd.graphmaster").error(
                    String.format("Key was not found for value when trying to remove \"%s\".", value));
            return;
        }
        // Remove the value from the HashMap (ignore the primary
        // value/key pair).
        this.hidden.remove(key);
    }

    /**
     * @see org.aitools.programd.graph.Nodemapper#get(java.lang.String)
     */
    public Object get(String key)
    {
        if (this.hidden == null)
        {
            return null;
        }
        return this.hidden.get(key.toUpperCase());
    }

    /**
     * @see org.aitools.programd.graph.Nodemapper#keySet()
     */
    public Set<String> keySet()
    {
        if (this.hidden == null)
        {
            return null;
        }
        return this.hidden.keySet();
    }

    /**
     * @see org.aitools.programd.graph.Nodemapper#containsKey(java.lang.String)
     */
    public boolean containsKey(String key)
    {
        if (this.hidden == null)
        {
            return false;
        }
        return this.hidden.containsKey(key.toUpperCase());
    }

    /**
     * @see org.aitools.programd.graph.Nodemapper#size()
     */
    public int size()
    {
        if (this.hidden == null)
        {
            return 0;
        }
        return this.hidden.size();
    }

    /**
     * @see org.aitools.programd.graph.Nodemapper#getAverageSize()
     */
    public double getAverageSize()
    {
        double total = 0d;
        if (this.hidden != null)
        {
            for (Object object : this.hidden.values())
            {
                if (object instanceof AbstractNodemaster)
                {
                    total += ((AbstractNodemaster) object).getAverageSize();
                }
            }
        }
        if (this._parent != null)
        {
            int size = this.hidden.size();
            return (size + (total / size)) / 2d;
        }
        // otherwise...
        return total / this.hidden.size();
    }
}
