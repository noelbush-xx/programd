/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.test.aiml;

import java.util.Iterator;

/**
 * Represents an abstract Item that may represent a single
 * value, or multiple values nested in arbitrarily deep
 * "and" and "or" elements, but which may in all cases
 * be accessed using an iterator.
 * @param <T> the type of Item
 * @param <B> the "basic" type of the Item
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.5
 */
public interface Item<T, B> extends Iterable<B>
{
    /**
     * @return an iterator over all the individual strings produced by this Item
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<B> iterator();
}
