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

package org.alicebot.server.core.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;


/**
 *  Manages a matrix of {@link StringTriple}s.
 *
 *  @author Noel Bush
 *  @since  4.1.5
 */
public class StringTripleMatrix
{
    private LinkedList[] vertical;
    private LinkedList horizontal;


    public StringTripleMatrix()
    {
        vertical = new LinkedList[] {new LinkedList(), new LinkedList(), new LinkedList()};
        horizontal = new LinkedList();
    }


    public LinkedList getAll()
    {
        return horizontal;
    }


    public Iterator iterator()
    {
        return horizontal.iterator();
    }


    public ListIterator listIterator()
    {
        return horizontal.listIterator();
    }


    public boolean contains(StringTriple tuple)
    {
        return horizontal.contains(tuple);
    }


    public LinkedList getFirsts()
    {
        return vertical[0];
    }


    public LinkedList getSeconds()
    {
        return vertical[1];
    }


    public LinkedList getThirds()
    {
        return vertical[2];
    }


    public void add(StringTriple tuple)
    {
        horizontal.add(tuple);
        vertical[0].add(tuple.getFirst());
        vertical[1].add(tuple.getSecond());
        vertical[2].add(tuple.getThird());
    }


    public void addAll(StringTripleMatrix matrix)
    {
        horizontal.addAll(matrix.getAll());
        vertical[0].addAll(matrix.getFirsts());
        vertical[1].addAll(matrix.getSeconds());
        vertical[2].addAll(matrix.getThirds());
    }


    public int size()
    {
        if ( !( (vertical[0].size() == vertical[1].size()) &&
                (vertical[1].size() == vertical[2].size()) &&
                (vertical[2].size() == horizontal.size())    ) )
        {
            Trace.devinfo("vertical[0].size(): " + vertical[0].size());
            Trace.devinfo("vertical[1].size(): " + vertical[1].size());
            Trace.devinfo("vertical[2].size(): " + vertical[2].size());
            Trace.devinfo("horizontal.size(): " + horizontal.size());
            throw new DeveloperError("Triple matrix integrity violated!");
        }
        return horizontal.size();
    }


    public void ensureSize(int size)
    {
        if (size() >= size)
        {
            return;
        }
        for (int index = 3; --index >= 0; )
        {
            Vector newVertical = new Vector(vertical[index]);
            newVertical.setSize(size);
            vertical[index] = new LinkedList(newVertical);
        }
        Vector newHorizontal = new Vector(horizontal);
        newHorizontal.setSize(size);
        horizontal = new LinkedList(newHorizontal);
    }
}
