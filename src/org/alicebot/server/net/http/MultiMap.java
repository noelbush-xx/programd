// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: MultiMap.java,v 1.1.1.1 2001/06/17 19:01:25 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/* ------------------------------------------------------------ */
/** A multi valued Map.
 * This Map specializes HashMap and provides methods
 * that operate on multi valued items.  Multi valued items are Lists,
 * arrays of Strings and java.util.Vectors.
 *
 * Multi-values items are always stored as a List.
 *
 * 
 * @version $Id: MultiMap.java,v 1.1.1.1 2001/06/17 19:01:25 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public class MultiMap extends HashMap
    implements Cloneable
{
    /* ------------------------------------------------------------ */
    /** Constructor. 
     */
    public MultiMap()
    {}
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param size Capacity of the map
     */
    public MultiMap(int size)
    {
        super(size);
    }
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param map Copy contents of this map.
     */
    public MultiMap(Map map)
    {
        super((map.size()*3)/2);
        putAll(map);
    }
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param map Copy contents of this map.
     */
    public MultiMap(MultiMap map)
    {
        super(map);
        
        Iterator i = map.entrySet().iterator();
        while(i.hasNext())
        {
            Map.Entry entry =
                (Map.Entry)i.next();
            if (entry.getValue() instanceof List)
                entry.setValue(new ArrayList((List)entry.getValue()));
        }
    }

    
    /* ------------------------------------------------------------ */
    /** Get multiple values.
     * Single valued entries are converted to singleton lists.
     * @param name The entry key. 
     * @return Unmodifieable List of values.
     */
    public List getValues(Object name)
    {
        Object o=get(name);
        if (o==null)
            return null;
        if (o instanceof List)
            return Collections.unmodifiableList((List)o);
        Object[] oa = {o};
        return Arrays.asList(oa);
    }
    
    /* ------------------------------------------------------------ */
    /** Get value as String.
     * Single valued items are converted to a String with the toString()
     * Object method. Multi valued entries are converted to a coma separated
     * List.  No quoting of commas within values is performed.
     * @param name The entry key. 
     * @return String value.
     */
    public String getString(Object name)
    {
        Object o=get(name);
        
        if (o==null)
            return null;
        if (o instanceof List)
        {
            List l=(List)o;
            if (l.size()>0)
            {
                StringBuffer values=new StringBuffer(128);
                synchronized(values)
                {
                    for (int i=0; i<l.size(); i++)              
                    {
                        Object e=l.get(i);
                        if (e!=null)
                        {
                            if (values.length()>0)
                                values.append(',');
                            values.append(e.toString());
                        }
                    }   
                    return values.toString();
                }
            }
            return null;
        }
        return o.toString();
    }
    
    /* ------------------------------------------------------------ */
    /** Put and entry into the map.
     * All supported multi value values are converted to Lists,
     * @param name The entry key. 
     * @param value The entry value.
     * @return The previous value or null.
     */
    public Object put(Object name, Object value) 
    {
        if (value instanceof List)
            return putValues(name,(List)value);
        if (value instanceof String[])
            return putValues(name,(String[])value);
        if (value instanceof java.util.Vector)
            return putValues(name,(java.util.Vector)value);

        return super.put(name,value);
    }

    /* ------------------------------------------------------------ */
    /** Put multi valued entry.
     * @param name The entry key. 
     * @param value The entry multiple values.
     * @return The previous value or null.
     */
    public Object putValues(Object name, List values) 
    {
        return super.put(name,values);
    }
    
    /* ------------------------------------------------------------ */
    /** Put multi valued entry.
     * @param name The entry key. 
     * @param value The entry multiple values.
     * @return The previous value or null.
     */
    public Object putValues(Object name, String[] values) 
    {
        return putValues(name,new ArrayList(Arrays.asList(values)));
    }
    
    /* ------------------------------------------------------------ */
    /** Put multi valued entry.
     * @param name The entry key. 
     * @param value The entry multiple values.
     * @return The previous value or null.
     */
    public Object putValues(Object name, java.util.Vector values) 
    {
        ArrayList l = new ArrayList(values.size());
        for (int i=0;i<values.size();i++)
            l.add(values.elementAt(i));
        return putValues(name,l);
    }
    
    /* ------------------------------------------------------------ */
    /** Add value to multi valued entry.
     * If the entry is single valued, it is converted to the first
     * value of a multi valued entry.
     * @param name The entry key. 
     * @param value The entry value.
     */
    public void add(Object name, Object value) 
    {
        if (value instanceof List)
            addValues(name,(List)value);
        else if (value instanceof String[])
            addValues(name,(String[])value);
        else if (value instanceof java.util.Vector)
            addValues(name,(java.util.Vector)value);
        else
        {
            Object o=get(name);
            if (o==null)
                put(name,value);
            else if (o instanceof List)
                ((List)o).add(value);
            else
            {
                List l=new ArrayList(8);
                l.add(o);
                l.add(value);
                put(name,l);
            }
        }
    }

    /* ------------------------------------------------------------ */
    /** Add values to multi valued entry.
     * If the entry is single valued, it is converted to the first
     * value of a multi valued entry.
     * @param name The entry key. 
     * @param value The entry multiple values.
     */
    public void addValues(Object name, List values) 
    {
        Object o=get(name);
        if (o==null)
            putValues(name,values);
        else if (o instanceof List)
        {
            try
            {
                ((List)o).addAll(values);
            }
            catch(UnsupportedOperationException e)
            {
                List l=new ArrayList(((List)o).size()+
                                     values.size());
                l.addAll((List)o);
                l.addAll(values);
            }
        }
        else
        {
            List l=new ArrayList(8+values.size());
            l.add(o);
            l.addAll(values);
            put(name,l);
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Add values to multi valued entry.
     * If the entry is single valued, it is converted to the first
     * value of a multi valued entry.
     * @param name The entry key. 
     * @param value The entry multiple values.
     */
    public void addValues(Object name, String[] values) 
    {
        addValues(name,new ArrayList(Arrays.asList(values)));
    }
    
    /* ------------------------------------------------------------ */
    /** Add values to multi valued entry.
     * If the entry is single valued, it is converted to the first
     * value of a multi valued entry.
     * @param name The entry key. 
     * @param value The entry multiple values.
     */
    public void addValues(Object name, java.util.Vector values) 
    {
        ArrayList l = new ArrayList(values.size());
        for (int i=0;i<values.size();i++)
            l.add(values.elementAt(i));
        addValues(name,l);
    }
    
    /* ------------------------------------------------------------ */
    /** Remove value.
     * Single valued entries are converted to singleton lists.
     * @param name The entry key. 
     * @param value The entry value. 
     * @return true if it was removed.
     */
    public boolean removeValue(Object name,Object value)
    {
        Object o=get(name);
        if (o==null)
            return false;
        if (o instanceof List)
        {
            List l=(List)o;
            return l.remove(value);
        }
        if (o.equals(value))
        {
            remove(name);
            return true;
        }
        return false;
    }
    
    /* ------------------------------------------------------------ */
    /** Put all contents of map.
     * @param m Map
     */
    public void putAll(Map m)
    {
        Iterator i = m.entrySet().iterator();
        while(i.hasNext())
        {
            Map.Entry entry =
                (Map.Entry)i.next();
            put(entry.getKey(),entry.getValue());
        }        
    }

    /* ------------------------------------------------------------ */
    /** Clone MultiMap.
     * Medium depth clone of map and lists, but not values.
     * @return cloned MultiMap
     */
    public Object clone()
    {
        return new MultiMap(this);
    }
    
}
