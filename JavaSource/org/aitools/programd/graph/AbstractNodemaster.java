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

/**
 * This is an abstract Nodemaster containing all the things that are common
 * to the various optimization strategies.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @version 4.6
 */
abstract public class AbstractNodemaster implements Nodemapper
{
    /**
     * The hidden hashmap where some (or all) mappings may be stored,
     * depending upon optimization strategies.
     */
    protected LinkedHashMap<String, Object> hidden;

    /**
     * The minimum number of words needed to reach a leaf node from here.
     * Defaults to zero.
     */
    protected int height = 0;

    /** The parent of this Nodemaster. */
    protected Nodemapper parent;

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
    protected void fillInHeight(int heightToFillIn)
    {
        if (this.height > heightToFillIn)
        {
            this.height = heightToFillIn;
        }
        if (this.parent != null)
        {
            ((AbstractNodemaster) this.parent).fillInHeight(heightToFillIn + 1);
        }
    }
}