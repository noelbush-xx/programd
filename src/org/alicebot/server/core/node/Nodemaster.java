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

/*  Code cleanup (28 Oct 2001, Noel Bush)
    - formatting cleanup
    - complete javadoc (not for implemented methods, though)
*/

/*
    Further fixes and optimizations (4.1.3 [01] - November 2001, Noel Bush)
    - CHANGED key types to Strings to fit change in Nodemapper interface!!!
    - added remove() method
    - changed TreeMap to HashMap, because order does not matter!
*/

/*
	4.1.5 - Noel Bush
	- added support for height (per Richard Wallace's suggestion
*/

package org.alicebot.server.core.node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.alicebot.server.core.util.Trace;


/**
 *  <p>
 *  This non-trivial implementation of {@link Nodemapper Nodemapper}
 *  uses a {@link java.util.TreeMap TreeMap} internally, but only
 *  allocates it when the number of keys is two or more.
 *  </p>
 *  <p>
 *  The <code>Nodemaster</code> saves space when many of the
 *  {@link Nodemapper Nodemappers} have only one branch, as is
 *  often the case in a real-world @{link Graphmaster Graphmaster}.
 *  </p>
 *
 *  @author Richard Wallace
 *  @author	Noel Bush
 */
public class Nodemaster implements Nodemapper
{
    protected int size = 0;
    protected String key;
    protected Object value;
    protected HashMap Hidden;
    
    /** The minimum number of words needed to reach a leaf node from here. Defaults to <code>Integer.MAX_VALUE</code> (zero). */
    protected int height = Integer.MAX_VALUE;

    protected Nodemapper parent;
    
    
    public Object put(String key, Object value)
    {
        if (size == 0)
        {
            this.key = key.toUpperCase();
            this.value = value;
            size = 1;
            return value;
        }
        else if (size == 1)
        {
            Hidden = new HashMap();
            Hidden.put(this.key.toUpperCase(), this.value);
            size = 2;
            return Hidden.put(key.toUpperCase(), value);
        }
        else
        {
            return Hidden.put(key.toUpperCase(), value);
        }
    }


    public void remove(Object value)
    {
        if (size > 2)
        {
            Hidden.remove(value);
            size--;
        }
        else if (size == 2)
        {
            this.value = Hidden.remove(value);
            size = 1;
        }
        else if (size == 1)
        {
            this.value = null;
            size = 0;
        }
    }


    public Object get(String key)
    {
        if (size == 0)
        {
            return null;
        }
        else if (size == 1)
        {
            if (key.equalsIgnoreCase(this.key))
            {
                return this.value;
            }
            else
            {
                return null;
            }
        }
        else
        {
            return Hidden.get(key.toUpperCase());
        }
    }


    public Set keySet()
    {
        if (size <= 1)
        {
            Set result = new HashSet();
            if (this.key != null)
            {
                result.add(this.key);
            }
            return result;
        }
        else
        {
            return Hidden.keySet();
        }
    }


    public boolean containsKey(String key)
    {
        if (size == 0)
        {
            return false;
        }
        else if (size <= 1)
        {
            return (key.equalsIgnoreCase(this.key));
        }
        else
        {
            return Hidden.containsKey(key.toUpperCase());
        }
    }


    public int size()
    {
        return this.size;
    }


    public void setParent(Nodemapper parent)
    {
        this.parent = parent;
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
     *  Sets the <code>height</code> of this
     *  <code>Nodemaster</code> to <code>height</code>,
     *  and calls <code>fillInHeight()</code> on
     *  its parent (if not null) with a height <code>height + 1</code>.
     *
     *  @param height	the height for this node
     */
    private void fillInHeight(int height)
    {
        if (this.height > height)
        {
            this.height = height;
        }
        if (this.parent != null)
        {
            ((Nodemaster)this.parent).fillInHeight(height + 1);
        }
    }
}

