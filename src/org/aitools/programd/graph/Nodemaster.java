/*    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

package org.aitools.programd.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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

    /** The minimum number of words needed to reach a leaf node from here. Defaults to zero. */
    protected int height = 0;

    protected Nodemapper parent;

    public Object put(String key, Object value)
    {
        if (this.size == 0)
        {
            this.key = key.toUpperCase().intern();
            this.value = value;
            size = 1;
            return value;
        }
        else if (this.size == 1)
        {
            Hidden = new HashMap();
            Hidden.put(this.key, this.value);
            size = 2;
            return Hidden.put(key.toUpperCase().intern(), value);
        }
        else
        {
            size++;
            return Hidden.put(key.toUpperCase().intern(), value);
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
            ((Nodemaster) this.parent).fillInHeight(height + 1);
        }
    }
}
