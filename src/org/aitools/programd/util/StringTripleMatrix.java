/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Vector;

/**
 * Manages a matrix of {@link StringTriple} s.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.1.5
 */
public class StringTripleMatrix
{
    private LinkedList<String>[] vertical;

    private LinkedList<StringTriple> horizontal;

    /**
     * Creates a new StringTripleMatrix.
     */
    public StringTripleMatrix()
    {
        this.vertical = new LinkedList[] { new LinkedList<String>(), new LinkedList<String>(), new LinkedList<String>() };
        this.horizontal = new LinkedList<StringTriple>();
    }

    /**
     * @return the horizontal component of the StringTripleMatrix
     */
    public LinkedList<StringTriple> getAll()
    {
        return this.horizontal;
    }

    /**
     * @return an iterator over the horizontal component
     */
    public Iterator iterator()
    {
        return this.horizontal.iterator();
    }

    /**
     * @return a list iterator for this StringTripleMatrix
     */
    public ListIterator listIterator()
    {
        return this.horizontal.listIterator();
    }

    /**
     * @param tuple the tuple to look for in this StringTripleMatrix
     * @return whether or not this StringTripleMatrix contains the given tuple
     */
    public boolean contains(StringTriple tuple)
    {
        return this.horizontal.contains(tuple);
    }

    /**
     * @return the first-position elements of the vertical component of this
     *         StringTripleMatrix
     */
    public LinkedList<String> getFirsts()
    {
        return this.vertical[0];
    }

    /**
     * @return the second-position elements of the vertical component of this
     *         StringTripleMatrix
     */
    public LinkedList<String> getSeconds()
    {
        return this.vertical[1];
    }

    /**
     * @return the third-position elements of the vertical component of this
     *         StringTripleMatrix
     */
    public LinkedList<String> getThirds()
    {
        return this.vertical[2];
    }

    /**
     * Adds the given StringTuple to this StringTripleMatrix.
     * 
     * @param tuple the tuple to add
     */
    public void add(StringTriple tuple)
    {
        this.horizontal.add(tuple);
        this.vertical[0].add(tuple.getFirst());
        this.vertical[1].add(tuple.getSecond());
        this.vertical[2].add(tuple.getThird());
    }

    /**
     * Adds the contents of the given StringTripleMatrix to this one.
     * 
     * @param matrix the matrix whose contents we want to add
     */
    public void addAll(StringTripleMatrix matrix)
    {
        this.horizontal.addAll(matrix.getAll());
        this.vertical[0].addAll(matrix.getFirsts());
        this.vertical[1].addAll(matrix.getSeconds());
        this.vertical[2].addAll(matrix.getThirds());
    }

    /**
     * @return the size of the StringTripleMatrix
     */
    public int size()
    {
        if (!((this.vertical[0].size() == this.vertical[1].size()) && (this.vertical[1].size() == this.vertical[2].size()) && (this.vertical[2]
                .size() == this.horizontal.size())))
        {
            throw new DeveloperError(new IllegalStateException("Triple matrix integrity violated!"));
        }
        return this.horizontal.size();
    }

    /**
     * Ensures that the StringTripleMatrix has the given size.
     * 
     * @param size the size to ensure
     */
    public void ensureSize(int size)
    {
        if (size() >= size)
        {
            return;
        }
        for (int index = 3; --index >= 0;)
        {
            Vector<String> newVertical = new Vector<String>(this.vertical[index]);
            newVertical.setSize(size);
            this.vertical[index] = new LinkedList<String>(newVertical);
        }
        Vector<StringTriple> newHorizontal = new Vector<StringTriple>(this.horizontal);
        newHorizontal.setSize(size);
        this.horizontal = new LinkedList<StringTriple>(newHorizontal);
    }
}