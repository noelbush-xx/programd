/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.multiplexor;

/**
 * A very simple data class that describes a predicate.
 */
public class PredicateInfo
{
    /** The default value of the predicate. */
    private String defaultValue;

    /**
     * Whether the predicate should return its name, or its newly-set value,
     * when set.
     */
    private boolean returnNameWhenSet;

    /**
     * Creates a new PredicateInfo.
     * @param predicateName the predicate name
     * @param defaultPredicateValue the default predicate value
     * @param returnNameSetting whether to return the name when setting the predicate
     */
    public PredicateInfo(String predicateName, String defaultPredicateValue, boolean returnNameSetting)
    {
        this.defaultValue = defaultPredicateValue;
        this.returnNameWhenSet = returnNameSetting;
    }
    
    /**
     * @return the default value
     */
    public String getDefaultValue()
    {
        return this.defaultValue;
    }
    
    /**
     * @return whether the predicate is supposed to return its name when set
     */
    public boolean returnNameWhenSet()
    {
        return this.returnNameWhenSet;
    }
}