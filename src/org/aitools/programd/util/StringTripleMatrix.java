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
 * @author Noel Bush
 * @since 4.1.5
 */
public class StringTripleMatrix
{
    private LinkedList[] vertical;

    private LinkedList horizontal;

    public StringTripleMatrix()
    {
        this.vertical = new LinkedList[]
            { new LinkedList(), new LinkedList(), new LinkedList() } ;
        this.horizontal = new LinkedList();
    } 

    public LinkedList getAll()
    {
        return this.horizontal;
    } 

    public Iterator iterator()
    {
        return this.horizontal.iterator();
    } 

    public ListIterator listIterator()
    {
        return this.horizontal.listIterator();
    } 

    public boolean contains(StringTriple tuple)
    {
        return this.horizontal.contains(tuple);
    } 

    public LinkedList getFirsts()
    {
        return this.vertical[0];
    } 

    public LinkedList getSeconds()
    {
        return this.vertical[1];
    } 

    public LinkedList getThirds()
    {
        return this.vertical[2];
    } 

    public void add(StringTriple tuple)
    {
        this.horizontal.add(tuple);
        this.vertical[0].add(tuple.getFirst());
        this.vertical[1].add(tuple.getSecond());
        this.vertical[2].add(tuple.getThird());
    } 

    public void addAll(StringTripleMatrix matrix)
    {
        this.horizontal.addAll(matrix.getAll());
        this.vertical[0].addAll(matrix.getFirsts());
        this.vertical[1].addAll(matrix.getSeconds());
        this.vertical[2].addAll(matrix.getThirds());
    } 

    public int size()
    {
        if (!((this.vertical[0].size() == this.vertical[1].size())
                && (this.vertical[1].size() == this.vertical[2].size()) && (this.vertical[2].size() == this.horizontal
                .size())))
        {
            Trace.devinfo("vertical[0].size(): " + this.vertical[0].size());
            Trace.devinfo("vertical[1].size(): " + this.vertical[1].size());
            Trace.devinfo("vertical[2].size(): " + this.vertical[2].size());
            Trace.devinfo("horizontal.size(): " + this.horizontal.size());
            throw new DeveloperError("Triple matrix integrity violated!");
        } 
        return this.horizontal.size();
    } 

    public void ensureSize(int size)
    {
        if (size() >= size)
        {
            return;
        } 
        for (int index = 3; --index >= 0;)
        {
            Vector newVertical = new Vector(this.vertical[index]);
            newVertical.setSize(size);
            this.vertical[index] = new LinkedList(newVertical);
        } 
        Vector newHorizontal = new Vector(this.horizontal);
        newHorizontal.setSize(size);
        this.horizontal = new LinkedList(newHorizontal);
    } 
}