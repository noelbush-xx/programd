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

/**
 * A <code>PredicateValue</code> is, naturally, the value of
 * a predicate.  It can either have a single String value,
 * or a list of values.
 * 
 * @author <a href="mailto:noel@alicebot.org">Noel Bush</a>
 * @since 4.5
 */
public class PredicateValue
{
    /** The single value (if assigned). */
    private String singleValue;
    
    /** The list of values (if assigned). */
    private ArrayList<String> valueList;
    
    /** Whether this PredicateValue has multiple values. */
    private boolean multiValued;
    
    /**
     * Creates a new <code>PredicateValue</code> with the
     * given single value.
     * 
     * @param value the single value to assign
     */
    public PredicateValue(String value)
    {
        this.singleValue = value;
        this.multiValued = false;
    }

    /**
     * Creates a new <code>PredicateValue</code> with the
     * given list of values.
     * 
     * @param values the list of values to assign
     */
    public PredicateValue(ArrayList<String> values)
    {
        this.valueList = values;
        this.multiValued = true;
    }
    
    /**
     * @return whether this <code>PredicateValue</code> is multi-valued
     */
    public boolean isMultiValued()
    {
        return this.multiValued;
    }
    
    /**
     * If this <code>PredicateValue</code> is single-valued,
     * simply returns the value.  If it is multi-valued,
     * returns the first in the list.
     * 
     * @return the first or only value
     */
    public String getFirstValue()
    {
        if (this.multiValued)
        {
            return this.valueList.get(0);
        }
        // otherwise...
        return this.singleValue;
    }
    
    /**
     * If this <code>PredicateValue</code> is multi-valued,
     * returns the list of values.  If it is single-valued,
     * returns the value inside of a list.
     * 
     * @return the list of values (or only value)
     */
    public ArrayList<String> asList()
    {
        if (this.multiValued)
        {
            return this.valueList;
        }
        // otherwise...
        ArrayList<String> result = new ArrayList<String>(1);
        result.add(this.singleValue);
        return result;
    }
    
    /**
     * Adds the given value.  In all cases,
     * this means the <code>PredicateValue</code>
     * becomes multi-valued.
     * 
     * @param value the value to add
     */
    public void add(String value)
    {
        this.multiValued = true;
        this.singleValue = null;
        if (this.valueList == null)
        {
            this.valueList = new ArrayList<String>(5);
            this.valueList.add(value);
        }
    }
}
