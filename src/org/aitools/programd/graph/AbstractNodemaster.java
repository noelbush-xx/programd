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
 * This is an abstract memory-based <code>Nodemapper</code> containing all the things that
 * are common to the various optimization strategies.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
abstract public class AbstractNodemaster implements Nodemapper
{
    /**
     * The hidden hashmap where some (or all) mappings may be stored, depending
     * upon optimization strategies.
     */
    protected LinkedHashMap<String, Object> hidden;

    /**
     * The minimum number of words needed to reach a leaf node from here.
     * Defaults to zero.
     */
    protected int _height = 0;

    /** The parent of this Nodemaster. */
    protected Nodemapper _parent;

    /**
     * @see org.aitools.programd.graph.Nodemapper#setParent(org.aitools.programd.graph.Nodemapper)
     */
    public void setParent(Nodemapper parent)
    {
        this._parent = parent;
    }

    /**
     * @see org.aitools.programd.graph.Nodemapper#getParent()
     */
    public Nodemapper getParent()
    {
        return this._parent;
    }

    /**
     * @see org.aitools.programd.graph.Nodemapper#getHeight()
     */
    public int getHeight()
    {
        return this._height;
    }

    /**
     * @see org.aitools.programd.graph.Nodemapper#setTop()
     */
    public void setTop()
    {
        this.fillInHeight(0);
    }

    /**
     * Sets the <code>height</code> of this <code>AbstractNodemaster</code> to
     * <code>height</code>, and calls <code>fillInHeight()</code> on its
     * parent (if not null) with a height <code>height + 1</code>.
     * 
     * @param height the height for this node
     */
    protected void fillInHeight(int height)
    {
        if (this._height > height)
        {
            this._height = height;
        }
        if (this._parent != null)
        {
            ((AbstractNodemaster) this._parent).fillInHeight(height + 1);
        }
    }
}
