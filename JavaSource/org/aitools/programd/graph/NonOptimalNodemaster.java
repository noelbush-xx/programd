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
 * This is the most trivial, and likely the most wasteful, implementation of
 * {@link Nodemapper Nodemapper}.  It does not attempt to do any optimizations.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @version 4.6
 */
public class NonOptimalNodemaster extends AbstractNodemaster
{
    /**
     * Puts the given object into the Nodemaster, associated with the given key.
     * 
     * @param keyToUse the key to use
     * @param valueToPut the value to put
     * @return the same object that was put into the Nodemaster
     */
    public Object put(String keyToUse, Object valueToPut)
    {
        if (valueToPut instanceof String)
        {
            if (this.hidden == null)
            {
                this.hidden = new LinkedHashMap<String, Object>();
            }
            return this.hidden.put(keyToUse.toUpperCase().intern(), ((String) valueToPut).intern());
        }
        // otherwise...
        return this.hidden.put(keyToUse.toUpperCase().intern(), valueToPut);
    }

    /**
     * Removes the given object from the Nodemaster.
     * 
     * @param valueToRemove the object to remove
     */
    public void remove(Object valueToRemove)
    {
        // Find the key for this value.
        Object keyToRemove = null;
        if (this.hidden != null)
        {
            for (Map.Entry<String, Object> item : this.hidden.entrySet())
            {
                if (item.getValue().equals(valueToRemove))
                {
                    // Found it.
                    keyToRemove = item.getKey();
                    break;
                }
            }
        }
        if (keyToRemove == null)
        {
            // We didn't find a key.
            Logger.getLogger("programd.graphmaster").error(String.format("Key was not found for value when trying to remove \"%s\".", valueToRemove));
            return;
        }
        // Remove the value from the HashMap (ignore the primary
        // value/key pair).
        this.hidden.remove(keyToRemove);
    }

    /**
     * Gets the object associated with the specified key.
     * 
     * @param keyToGet the key to use
     * @return the object associated with the given key
     */
    public Object get(String keyToGet)
    {
        if (this.hidden == null)
        {
            return null;
        }
        return this.hidden.get(keyToGet.toUpperCase());
    }

    /**
     * @return the keyset of the Nodemaster
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
     * @param keyToCheck the key to check
     * @return whether or not the Nodemaster contains the given key
     */
    public boolean containsKey(String keyToCheck)
    {
        if (this.hidden == null)
        {
            return false;
        }
        return this.hidden.containsKey(keyToCheck.toUpperCase());
    }

    /**
     * @return the size of the Nodemaster
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
     * Sets the parent of the Nodemaster.
     * 
     * @param parentToSet the parent to set
     */
    @Override
    public void setParent(Nodemapper parentToSet)
    {
        this.parent = parentToSet;
    }

    /**
     * @return the parent of the Nodemaster
     */
    @Override
    public Nodemapper getParent()
    {
        return this.parent;
    }

    /**
     * @return the height of the Nodemaster
     */
    @Override
    public int getHeight()
    {
        return this.height;
    }

    /**
     * Sets the Nodemaster as being at the top.
     */
    @Override
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
    @Override
    protected void fillInHeight(int heightToFillIn)
    {
        if (this.height > heightToFillIn)
        {
            this.height = heightToFillIn;
        }
        if (this.parent != null)
        {
            ((NonOptimalNodemaster) this.parent).fillInHeight(heightToFillIn + 1);
        }
    }
    
    public double getAverageSize()
    {
        double total = 0d;
        if (this.hidden != null)
        {
            for (Object object : this.hidden.values())
            {
                if (object instanceof Nodemapper)
                {
                    total += ((Nodemapper)object).getAverageSize();
                }
            }
        }
        if (this.parent != null)
        {
            int size = this.hidden.size();
            return (size + (total / size)) / 2d;
        }
        // otherwise...
        return total / this.hidden.size();
    }
}