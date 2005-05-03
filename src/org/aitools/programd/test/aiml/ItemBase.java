package org.aitools.programd.test.aiml;

import java.util.Iterator;

/**
 * Represents a "base" Item, that is, the inner
 * type of which this Item consists.
 * @param <T> the "outer" type of Item
 * @param <B> the type of the inner type
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
abstract public class ItemBase<T extends Item, B> implements Item<T, B>
{
    /** The content of this ItemBase. */
    protected B content;
    
    /**
     * Creates a new ItemBase with the given content.
     * 
     * @param value the content for the ItemBase
     */
    protected ItemBase(B value)
    {
        this.content = value;
    }
    
    /**
     * Allows for the creation of an ItemBase with no content.
     */
    protected ItemBase()
    {
        // Do nothing.
    }

    /**
     * @return a lightweight (fake) iterator for this ItemBase's single value
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<B> iterator()
    {
        return new FakeIterator();
    }
    
    /**
     * An ItemBase.FakeIterator returns the content
     * exactly once, and that's it.
     */
    private class FakeIterator implements Iterator<B>
    {
        /** Whether the value has been returned. */
        private boolean valueReturned = false;
        
        /**
         * Creates a new FakeIterator.
         */
        public FakeIterator()
        {
            // Do nothing.
        }
        
        /**
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext()
        {
            return !this.valueReturned;
        }

        /**
         * @return the ItemBase content
         * @see java.util.Iterator#next()
         */
        public B next()
        {
            this.valueReturned = true;
            return ItemBase.this.content;
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
