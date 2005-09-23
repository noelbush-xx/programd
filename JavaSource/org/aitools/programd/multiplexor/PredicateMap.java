/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.multiplexor;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A map of predicate names to values.
 *
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.5
 */
public class PredicateMap extends HashMap<String, PredicateValue>
{
    /**
     * Creates a new <code>PredicateMap</code>.
     */
    public PredicateMap()
    {
        super();
    }
    
    /**
     * Puts a single-valued predicate into the map.
     * 
     * @param name the predicate name
     * @param value the predicate value
     */
    public void put(String name, String value)
    {
        put(name, new PredicateValue(value));
    }
    
    /**
     * Puts a multi-valued predicate into the map.
     * 
     * @param name the predicate name
     * @param values the predicate values
     */
    public void put(String name, ArrayList<String> values)
    {
        put(name, new PredicateValue(values));
    }
    
    /**
     * Returns the value associated with the given name.
     * 
     * @param name the name of the predicate whose value is wanted
     * @return the value of the named predicate
     * @throws NoSuchPredicateException if no such predicate exists
     */
    public PredicateValue get(String name) throws NoSuchPredicateException
    {
        PredicateValue value = super.get(name);
        if (value == null)
        {
            throw new NoSuchPredicateException(name);
        }
        return value;
    }
}
