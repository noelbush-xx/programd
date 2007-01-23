/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.graph;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * This is an optimization of {@link Nodemapper} that avoids creating the internal
 * {@link java.util.LinkedHashMap LinkedMap} until the number of mappings exceeds two (2).
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class TwoOptimalMemoryNodemapper extends AbstractNodemaster
{
    protected int size = 0;

    protected String key_0;

    protected String key_1;

    protected Object value_0;

    protected Object value_1;

    /**
     * @see org.aitools.programd.graph.Nodemapper#put(java.lang.String, java.lang.Object)
     */
    public Object put(String key, Object value)
    {
        if (this.size < 2)
        {
            // This is ugly, but allows our optimization.
            if (this.size == 0)
            {
                this.key_0 = key.toUpperCase().intern();
                if (value instanceof String)
                {
                    this.value_0 = ((String) value).intern();
                }
                else
                {
                    this.value_0 = value;
                }
                this.size = 1;
                return this.value_0;
            }
            // otherwise...
            this.key_1 = key.toUpperCase().intern();
            if (value instanceof String)
            {
                this.value_1 = ((String) value).intern();
            }
            else
            {
                this.value_1 = value;
            }
            this.size = 2;
            return this.value_1;
        }
        else if (this.size == 2)
        {
            this.hidden = new LinkedHashMap<String, Object>();
            this.hidden.put(this.key_0, this.value_0);
            this.hidden.put(this.key_1, this.value_1);
            this.key_0 = null;
            this.key_1 = null;
            this.value_0 = null;
            this.value_1 = null;
            this.size = 3;
            if (value instanceof String)
            {
                return this.hidden.put(key.toUpperCase().intern(), ((String) value).intern());
            }
            // otherwise...
            return this.hidden.put(key.toUpperCase().intern(), value);
        }
        else
        {
            this.size++;
            if (value instanceof String)
            {
                return this.hidden.put(key.toUpperCase().intern(), ((String) value).intern());
            }
            // otherwise...
            return this.hidden.put(key.toUpperCase().intern(), value);
        }
    }

    /**
     * @see org.aitools.programd.graph.Nodemapper#remove(java.lang.Object)
     */
    public void remove(Object value)
    {
        if (this.size == 2 || this.size == 1)
        {
            // ugly but optimal (see above)
            if (value.equals(this.value_0))
            {
                this.value_0 = null;
                this.key_0 = null;
            }
            else if (value.equals(this.value_1))
            {
                this.value_1 = null;
                this.key_1 = null;
            }
            else
            {
                // We didn't find a key.
                Logger.getLogger("programd.graphmaster").error(
                        String.format("Key was not found for value when trying to remove \"%s\".", value));
                return;
            }
            this.size--;
        }
        else if (this.size > 2)
        {
            // Find the key for this value.
            Object keyToRemove = null;
            for (Map.Entry<String, Object> item : this.hidden.entrySet())
            {
                if (value.equals(item.getValue()))
                {
                    // Found it.
                    keyToRemove = item.getKey();
                    break;
                }
            }
            if (keyToRemove == null)
            {
                // We didn't find a key.
                Logger.getLogger("programd.graphmaster").error(
                        String.format("Key was not found for value when trying to remove \"%s\".", value));
                return;
            }
            if (this.size > 3)
            {
                // Remove the value from the HashMap (ignore the primary
                // value/key pair).
                this.hidden.remove(keyToRemove);
                this.size--;
            }
            // otherwise it is exactly 3...
            else
            {
                // Remove this item from the HashMap.
                this.hidden.remove(keyToRemove);
                // Set the last two items in the HashMap to be the primary value/key
                // pairs for this Nodemapper.
                this.key_1 = this.hidden.keySet().iterator().next();
                this.value_1 = this.hidden.remove(this.key_1);
                this.key_0 = this.hidden.keySet().iterator().next();
                this.value_0 = this.hidden.remove(this.key_0);
                // Remove the empty HashMap to save space.
                this.hidden = null;
                this.size = 2;
            }
        }
        else if (this.size == 0)
        {
            // We didn't find a key.
            Logger.getLogger("programd.graphmaster").error(
                    String.format("No keys in Nodemapper when trying to remove \"%s\".", value));
        }
    }

    /**
     * @see org.aitools.programd.graph.Nodemapper#get(java.lang.String)
     */
    public Object get(String key)
    {
        if (this.size == 0)
        {
            return null;
        }
        else if (this.size == 2 || this.size == 1)
        {
            if (key.equalsIgnoreCase(this.key_0))
            {
                return this.value_0;
            }
            if (key.equalsIgnoreCase(this.key_1))
            {
                return this.value_1;
            }
            // (otherwise...)
            return null;
        }
        else
        {
            return this.hidden.get(key.toUpperCase());
        }
    }

    /**
     * @see org.aitools.programd.graph.Nodemapper#keySet()
     */
    public Set<String> keySet()
    {
        if (this.size < 3)
        {
            Set<String> result = new HashSet<String>();
            if (this.key_0 != null)
            {
                result.add(this.key_0);
            }
            if (this.key_1 != null)
            {
                result.add(this.key_1);
            }
            return result;
        }
        // (otherwise...)
        return this.hidden.keySet();
    }

    /**
     * @see org.aitools.programd.graph.Nodemapper#containsKey(java.lang.String)
     */
    public boolean containsKey(String key)
    {
        if (this.size == 0)
        {
            return false;
        }
        else if (this.size == 2 || this.size == 1)
        {
            return (key.equalsIgnoreCase(this.key_0) || key.equalsIgnoreCase(this.key_1));
        }
        return this.hidden.containsKey(key.toUpperCase());
    }

    /**
     * @see org.aitools.programd.graph.Nodemapper#size()
     */
    public int size()
    {
        return this.size;
    }

    /**
     * @see org.aitools.programd.graph.Nodemapper#getAverageSize()
     */
    public double getAverageSize()
    {
        double total = 0d;
        if (this.size < 3)
        {
            if (this.value_0 != null && this.value_0 instanceof AbstractNodemaster)
            {
                total += ((AbstractNodemaster) this.value_0).getAverageSize();
            }
            if (this.value_1 != null && this.value_1 instanceof AbstractNodemaster)
            {
                total += ((AbstractNodemaster) this.value_1).getAverageSize();
            }
        }
        else
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
            return (this.size + (total / this.size)) / 2d;
        }
        // otherwise...
        return total / this.size;
    }
}
