/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
 * @author Noel Bush
 * @author Eion Robb
 */
public class Nodemaster implements Nodemapper
{
    protected int size = 0;

    protected String key;

    protected Object value;

    protected HashMap Hidden;

    /**
     * The minimum number of words needed to reach a leaf node from here.
     * Defaults to zero.
     */
    protected int height = 0;

    protected Nodemapper parent;

    public Object put(String keyToPut, Object valueToPut)
    {
        if (this.size == 0)
        {
            this.key = keyToPut.toUpperCase().intern();
            if (valueToPut instanceof String)
            {
                this.value = ((String)valueToPut).intern();
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
            this.Hidden = new HashMap();
            this.Hidden.put(this.key, this.value);
            this.size = 2;
            if (valueToPut instanceof String)
            {
                return this.Hidden.put(keyToPut.toUpperCase().intern(), valueToPut);
            }
            // otherwise...
            return this.Hidden.put(keyToPut.toUpperCase().intern(), ((String)valueToPut).intern());
        } 
        else
        {
            this.size++;
            if (valueToPut instanceof String)
            {
                return this.Hidden.put(keyToPut.toUpperCase().intern(), valueToPut);
            }
            // otherwise...
            return this.Hidden.put(keyToPut.toUpperCase().intern(), ((String)valueToPut).intern());
        } 
    } 

    public void remove(Object valueToRemove) throws DeveloperError
    {
        if (this.size > 1)
        {
            // Find the key for this value.
            Object keyToRemove = null;
            for (Iterator iterator = this.Hidden.entrySet().iterator(); iterator.hasNext();)
            {
                Map.Entry item = (Map.Entry) iterator.next();
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
                throw new DeveloperError("Key was not found for value when trying to remove " + valueToRemove);
            } 
            if (this.size > 2)
            {
                // Remove the value from the HashMap (ignore the primary
                // value/key pair).
                this.Hidden.remove(keyToRemove);
                this.size--;
            } 
            else
            {
                // Remove this item from the HashMap.
                this.Hidden.remove(keyToRemove);
                // Set the last item in HashMap to be the primary value/key for
                // this Nodemapper.
                this.key = (String) this.Hidden.keySet().iterator().next();
                this.value = this.Hidden.remove(this.key);
                // Remove the empty HashMap to save space.
                this.Hidden = null;
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
            //          (otherwise...)
            return null;
        } 
        else
        {
            return this.Hidden.get(keyToGet.toUpperCase());
        } 
    } 

    public Set keySet()
    {
        if (this.size <= 1)
        {
            Set result = new HashSet();
            if (this.key != null)
            {
                result.add(this.key);
            } 
            return result;
        } 
        // (otherwise...)
        return this.Hidden.keySet();
    } 

    public boolean containsKey(String keyToCheck)
    {
        if (this.size == 0)
        {
            return false;
        } 
        else if (this.size <= 1)
        {
            return (keyToCheck.equalsIgnoreCase(this.key));
        } 
        else
        {
            return this.Hidden.containsKey(keyToCheck.toUpperCase());
        } 
    } 

    public int size()
    {
        return this.size;
    } 

    public void setParent(Nodemapper parentToSet)
    {
        this.parent = parentToSet;
    } 

    public Nodemapper getParent()
    {
        return this.parent;
    } 

    public int getHeight()
    {
        return this.height;
    } 

    public void setTop()
    {
        this.fillInHeight(0);
    } 

    /**
     * Sets the <code>height</code> of this <code>Nodemaster</code> to
     * <code>height</code>, and calls <code>fillInHeight()</code> on its
     * parent (if not null) with a height <code>height + 1</code>.
     * 
     * @param heightToFillIn
     *            the height for this node
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