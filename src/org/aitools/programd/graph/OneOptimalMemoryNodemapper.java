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
 * {@link java.util.LinkedHashMap LinkedMap} until the number of mappings exceeds one (1).
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class OneOptimalMemoryNodemapper extends AbstractNodemaster
{
    protected int size = 0;

    protected String _key;

    protected Object _value;

    /**
     * @see org.aitools.programd.graph.Nodemapper#put(java.lang.String, java.lang.Object)
     */
    public Object put(String key, Object value)
    {
        if (this.size == 0)
        {
            this._key = key.toUpperCase().intern();
            if (value instanceof String)
            {
                this._value = ((String) value).intern();
            }
            else
            {
                this._value = value;
            }
            this.size = 1;
            return this._value;
        }
        else if (this.size == 1)
        {
            this.hidden = new LinkedHashMap<String, Object>();
            this.hidden.put(this._key, this._value);
            this._key = null;
            this._value = null;
            this.size = 2;
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
        if (this.size == 1)
        {
            if (value.equals(this._value))
            {
                this._value = null;
                this._key = null;
            }
            else
            {
                // We didn't find a key.
                Logger.getLogger("programd.graphmaster").error(
                        String.format("Key was not found for value when trying to remove \"%s\".", value));
                return;
            }
            this.size = 0;
        }
        else if (this.size > 1)
        {
            // Find the key for this value.
            Object keyToRemove = null;
            for (Map.Entry<String, Object> item : this.hidden.entrySet())
            {
                if (item.getValue().equals(value))
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
            if (this.size > 2)
            {
                // Remove the value from the HashMap (ignore the primary
                // value/key pair).
                this.hidden.remove(keyToRemove);
                this.size--;
            }
            // otherwise it is exactly 2...
            else
            {
                // Remove this item from the HashMap.
                this.hidden.remove(keyToRemove);
                // Set the last item in the HashMap to be the primary value/key
                // pair for this Nodemapper.
                this._key = this.hidden.keySet().iterator().next();
                this._value = this.hidden.remove(this._key);
                // Remove the empty HashMap to save space.
                this.hidden = null;
                this.size = 1;
            }
        }
        else
        // if (this.size == 0)
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
        else if (this.size == 1)
        {
            if (key.equalsIgnoreCase(this._key))
            {
                return this._value;
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
        if (this.size == 1)
        {
            Set<String> result = new HashSet<String>();
            if (this._key != null)
            {
                result.add(this._key);
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
        else if (this.size == 1)
        {
            return (key.equalsIgnoreCase(this._key));
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
        if (this.size == 1)
        {
            if (this._value != null && this._value instanceof AbstractNodemaster)
            {
                total = ((AbstractNodemaster) this._value).getAverageSize();
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
