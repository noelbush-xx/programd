package org.aitools.programd.test.aiml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents a group of Items of which all
 * should be used.
 * @param <T> the type of Item that this "and" holds.
 * @param <B> the "basic" type of Item
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
abstract public class ItemAll<T extends Item, B> implements Item<T, B>
{
    /** The items. */
    protected ArrayList<Item<T, B>> contents = new ArrayList<Item<T, B>>();

    /**
     * @return a &quot;virtual iterator&quot; that spans all the Inputs contained in this element
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<B> iterator()
    {
        return new SequentialIterator();
    }
    
    /**
     * An ItemAll.SequentialIterator returns
     * each iterator from each element contained
     * by the ItemAll, until all are exhausted.
     */
    private class SequentialIterator implements Iterator<B>
    {
        /** The iterator over the ItemAll's contents. */
        private Iterator<Item<T, B>> andIterator;
        
        /** The currently selected iterator. */
        private Iterator<B> currentIterator;
        
        /**
         * Creates a new SequentialIterator.
         */
        public SequentialIterator()
        {
            this.andIterator = ItemAll.this.contents.iterator();
            this.currentIterator = this.andIterator.next().iterator();
        }
        
        /**
         * If the <T> pointed at by the iterator still
         * has more elements, then this returns true.
         * Otherwise, the <T> iterator is advanced, and the
         * <code>hasNext()</code> value of this next iterator
         * is returned.  When no more
         * iterators &amp; elements are available, returns false.

         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext()
        {
            if (this.currentIterator.hasNext())
            {
                return true;
            }
            else if (this.andIterator.hasNext())
            {
                this.currentIterator = this.andIterator.next().iterator();
                
                // We rely on the schema to be sure that each Input has at least one element/value.
                return true;
            }
            // otherwise...
            return false;
        }

        /**
         * If the <T> pointed at by the iterator still
         * has more elements, then its iterator is returned.
         * Otherwise, the <T> iterator is advanced, and the
         * iterator of the new <T> is returned.  When no more
         * iterators &amp; elements are available an exception
         * is thrown as per the API.  (Avoid this by checking
         * {@link #hasNext}.
         * 
         * @return the next element, if possible
         * 
         * @see java.util.Iterator#next()
         */
        public B next()
        {
            if (this.currentIterator.hasNext())
            {
                return this.currentIterator.next();
            }
            else if (this.andIterator.hasNext())
            {
                this.currentIterator = this.andIterator.next().iterator();
                
                // We rely on the schema to be sure that each Input has at least one element/value.
                return next();
            }
            // otherwise...
            throw new NoSuchElementException();
        }

        /**
         * Not supported.  Throws an exception every time.
         * @see java.util.Iterator#remove()
         */
        public void remove() throws UnsupportedOperationException
        {
            throw new UnsupportedOperationException();
        }
        
    }
}
