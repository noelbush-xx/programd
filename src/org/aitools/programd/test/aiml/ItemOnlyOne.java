package org.aitools.programd.test.aiml;

import java.util.ArrayList;
import java.util.Iterator;

import org.aitools.programd.util.MersenneTwisterFast;

/**
 * Represents a group of alternative Items.
 * @param <T> the type of Item that this "or" holds
 * @param <B> the "basic" type of Item
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class ItemOnlyOne<T extends Item, B> implements Item<T, B>
{
    /** A random number generator. */
    protected static MersenneTwisterFast generator = new MersenneTwisterFast(System.currentTimeMillis());
    
    /** The alternatives. */
    protected ArrayList<Item<T, B>> contents = new ArrayList<Item<T, B>>();

    /**
     * @return an iterator for one member of this ItemOnlyOne, randomly selected
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<B> iterator()
    {
        return new RandomlySelectedIterator();
    }
    
    /**
     * An ItemOnlyOne.RandomlySelectedIterator returns
     * the iterator from one of the ItemOnlyOne's
     * elements, randomly chosen.  The same underlying
     * iterator is used for the life of this
     * RandomlySelectedIterator.
     */
    private class RandomlySelectedIterator implements Iterator<B>
    {
        /** The randomly selected iterator. */
        private Iterator<B> iterator;
        
        /**
         * Creates a new RandomlySelectedIterator.
         */
        public RandomlySelectedIterator()
        {
            this.iterator = ItemOnlyOne.this.contents.get(generator.nextInt(ItemOnlyOne.this.contents.size())).iterator();
        }
        
        /**
         * Always returns true.
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext()
        {
            return this.iterator.hasNext();
        }

        /**
         * @return the next element from the randomly chosen member of the ItemOnlyOne.
         * @see java.util.Iterator#next()
         */
        public B next()
        {
            return this.iterator.next();
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
