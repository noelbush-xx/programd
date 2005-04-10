/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces.graphical;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * An ActionListener that is aware of its parent.
 * @param <P> the parent class
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
abstract public class ParentAwareActionListener<P> implements ActionListener
{
    protected P parent;

    /**
     * Creates a new ParentAwareActionListener
     * @param parentToUse the parent to be aware of
     */
    public ParentAwareActionListener(P parentToUse)
    {
        this.parent = parentToUse;
    } 

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    abstract public void actionPerformed(ActionEvent ae);
}