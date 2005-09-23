/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.graph;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aitools.programd.util.DeveloperError;

/**
 * <p>
 * This non-trivial implementation of {@link Nodemapper Nodemapper} uses a
 * {@link java.util.TreeMap TreeMap} internally, but only allocates it when the
 * number of keys is two or more.
 * </p>
 * <p>
 * The <code>Nodemaster</code> saves space when many of the
 * {@link Nodemapper Nodemappers} have only one branch, as is often the case in
 * a real-world {@link Graphmaster Graphmaster} .
 * </p>
 * 
 * @author Richard Wallace
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @author Eion Robb
 */
public class Nodemaster implements Nodemapper
{
    protected int size = 0;

    protected String key;

    protected Object value;

    protected Map<String, Object> hidden;

    /**
     * The minimum number of words needed to reach a leaf node from here.
     * Defaults to zero.
     */
    protected int height = 0;

    protected Nodemapper parent;

    /**
     * Puts the given object into the Nodemaster, associated with the given key.
     * 
     * @param keyToUse the key to use
     * @param valueToPut the value to put
     * @return the same object that was put into the Nodemaster
     */
    public Object put(String keyToUse, Object valueToPut)
    {
        if (this.size == 0)
        {
            this.key = keyToUse.toUpperCase().intern();
            if (valueToPut instanceof String)
            {
                this.value = ((String) valueToPut).intern();
            }
            else
            {
                this.value = valueToPut;
            }
            this.size = 1;
            return this.value;
        }
        else if (this.size == 1)
        {
            this.hidden = Collections.checkedMap(new HashMap<String, Object>(), String.class, Object.class);
            this.hidden.put(this.key, this.value);
            this.size = 2;
            if (valueToPut instanceof String)
            {
                return this.hidden.put(keyToUse.toUpperCase().intern(), ((String) valueToPut).intern());
            }
            // otherwise...
            return this.hidden.put(keyToUse.toUpperCase().intern(), valueToPut);
        }
        else
        {
            this.size++;
            if (valueToPut instanceof String)
            {
                return this.hidden.put(keyToUse.toUpperCase().intern(), ((String) valueToPut).intern());
            }
            // otherwise...
            return this.hidden.put(keyToUse.toUpperCase().intern(), valueToPut);
        }
    }

    /**
     * Removes the given object from the Nodemaster.
     * 
     * @param valueToRemove the object to remove
     */
    public void remove(Object valueToRemove)
    {
        if (this.size > 1)
        {
            // Find the key for this value.
            Object keyToRemove = null;
            for (Map.Entry<String, Object> item : this.hidden.entrySet())
            {
                if (item.getValue().equals(valueToRemove))
                {
                    // Found it.
                    keyToRemove = item.getKey();
                    break;
                }
            }
            if (keyToRemove == null)
            {
                // We didn't find a key.
                throw new DeveloperError("Key was not found for value when trying to remove " + valueToRemove, new NullPointerException());
            }
            if (this.size > 2)
            {
                // Remove the value from the HashMap (ignore the primary
                // value/key pair).
                this.hidden.remove(keyToRemove);
                this.size--;
            }
            else
            {
                // Remove this item from the HashMap.
                this.hidden.remove(keyToRemove);
                // Set the last item in HashMap to be the primary value/key for
                // this Nodemapper.
                this.key = this.hidden.keySet().iterator().next();
                this.value = this.hidden.remove(this.key);
                // Remove the empty HashMap to save space.
                this.hidden = null;
                this.size = 1;
            }
        }
        else if (this.size == 1)
        {
            this.value = null;
            this.key = null;
            this.size = 0;
        }
    }

    /**
     * Gets the object associated with the specified key.
     * 
     * @param keyToGet the key to use
     * @return the object associated with the given key
     */
    public Object get(String keyToGet)
    {
        if (this.size == 0)
        {
            return null;
        }
        else if (this.size == 1)
        {
            if (keyToGet.equalsIgnoreCase(this.key))
            {
                return this.value;
            }
            // (otherwise...)
            return null;
        }
        else
        {
            return this.hidden.get(keyToGet.toUpperCase());
        }
    }

    /**
     * @return the keyset of the Nodemaster
     */
    public Set keySet()
    {
        if (this.size <= 1)
        {
            Set<String> result = new HashSet<String>();
            if (this.key != null)
            {
                result.add(this.key);
            }
            return result;
        }
        // (otherwise...)
        return this.hidden.keySet();
    }

    /**
     * @param keyToCheck the key to check
     * @return whether or not the Nodemaster contains the given key
     */
    public boolean containsKey(String keyToCheck)
    {
        if (this.size == 0)
        {
            return false;
        }
        else if (this.size == 1)
        {
            return (keyToCheck.equalsIgnoreCase(this.key));
        }
        return this.hidden.containsKey(keyToCheck.toUpperCase());
    }

    /**
     * @return the size of the Nodemaster
     */
    public int size()
    {
        return this.size;
    }

    /**
     * Sets the parent of the Nodemaster.
     * 
     * @param parentToSet the parent to set
     */
    public void setParent(Nodemapper parentToSet)
    {
        this.parent = parentToSet;
    }

    /**
     * @return the parent of the Nodemaster
     */
    public Nodemapper getParent()
    {
        return this.parent;
    }

    /**
     * @return the height of the Nodemaster
     */
    public int getHeight()
    {
        return this.height;
    }

    /**
     * Sets the Nodemaster as being at the top.
     */
    public void setTop()
    {
        this.fillInHeight(0);
    }

    /**
     * Sets the <code>height</code> of this <code>Nodemaster</code> to
     * <code>height</code>, and calls <code>fillInHeight()</code> on its
     * parent (if not null) with a height <code>height + 1</code>.
     * 
     * @param heightToFillIn the height for this node
     */
    private void fillInHeight(int heightToFillIn)
    {
        if (this.height > heightToFillIn)
        {
            this.height = heightToFillIn;
        }
        if (this.parent != null)
        {
            ((Nodemaster) this.parent).fillInHeight(heightToFillIn + 1);
        }
    }
}