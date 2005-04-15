/*
 * Copyright (c) 1998-2004 Caucho Technology -- all rights reserved This file is
 * part of Resin(R) Open Source Each copy or derived work must preserve the
 * copyright notice and this notice unmodified. Resin Open Source is free
 * software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version. Resin Open
 * Source is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE, or any warranty of NON-INFRINGEMENT. See the GNU
 * General Public License for more details. You should have received a copy of
 * the GNU General Public License along with Resin Open Source; if not, write to
 * the Free SoftwareFoundation, Inc. 59 Temple Place, Suite 330 Boston, MA
 * 02111-1307 USA @author Scott Ferguson
 */

// originally: package com.caucho.util;
package org.aitools.programd.util;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * <p>
 * Fixed length cache with a LRU replacement policy. If cache items implement
 * CacheListener, they will be informed when they're removed from the cache.
 * </p>
 * <p>
 * Null keys are not allowed. LRUCache is synchronized.
 * </p>
 * 
 * @param <K> the key
 * @param <V> the value
 */
public class LRUCache<K, V>
 {
    private static final Integer NULL = new Integer(0);

    /*
     * hash table containing the entries. Its size is twice the capacity so it
     * will always remain at least half empty
     */
    protected CacheItem<K, V>[] _entries;

    /* maximum allowed entries */
    private int _capacity;

    /* size 1 capacity is half the actual capacity */
    private int _capacity1;

    /* mask for hash mapping */
    private int _mask;

    /* number of items in the cache seen once */
    private int _size1;

    /* head of the LRU list */
    private CacheItem<K, V> _head1;

    /* tail of the LRU list */
    private CacheItem<K, V> _tail1;

    /* number of items in the cache seen more than once */
    private int _size2;

    /* head of the LRU list */
    private CacheItem<K, V> _head2;

    /* tail of the LRU list */
    private CacheItem<K, V> _tail2;

    /* hit count statistics */
    private volatile long _hitCount;

    /* miss count statistics */
    private volatile long _missCount;

    /**
     * Create the LRU cache with a specific capacity. Originally called
     * "LruCache". Some minor changes (in coding style and formatting) made by
     * Noel.
     * 
     * @param initialCapacity minimum capacity of the cache
     */
    public LRUCache(int initialCapacity)
    {
        int capacity;

        for (capacity = 16; capacity < 2 * initialCapacity; capacity *= 2)
        {
            // Do nothing except this loop.
        }

        this._entries = new CacheItem[capacity];
        this._mask = capacity - 1;

        this._capacity = initialCapacity;
        this._capacity1 = this._capacity / 2;
    }

    /**
     * @return the current number of entries in the cache
     */
    public int size()
    {
        return this._size1 + this._size2;
    }

    /**
     * Clears the cache
     */
    public void clear()
    {
        ArrayList<CacheListener> listeners = null;

        synchronized (this)
        {
            for (int i = this._entries.length - 1; i >= 0; i--)
            {
                CacheItem<K, V> item = this._entries[i];

                if (item != null)
                {
                    if (item._value instanceof CacheListener)
                    {
                        if (listeners == null)
                        {
                            listeners = new ArrayList<CacheListener>();
                        }
                        listeners.add((CacheListener) item._value);
                    }
                }

                this._entries[i] = null;
            }

            this._size1 = 0;
            this._head1 = null;
            this._tail1 = null;
            this._size2 = 0;
            this._head2 = null;
            this._tail2 = null;
        }

        for (int i = listeners == null ? -1 : listeners.size() - 1; i >= 0; i--)
        {
            CacheListener listener = listeners.get(i);
            listener.removeEvent();
        }
    }

    /**
     * Get an item from the cache and make it most recently used.
     * 
     * @param key key to lookup the item
     * @return the matching object in the cache
     */
    public V get(K key)
    {
        Object okey = key;
        if (okey == null)
            okey = NULL;

        int hash = okey.hashCode() & this._mask;
        int count = this._size1 + this._size2 + 1;

        synchronized (this)
        {
            for (; count >= 0; count--)
            {
                CacheItem<K, V> item = this._entries[hash];

                if (item == null)
                {
                    this._missCount++;
                    return null;
                }

                if (item._key == key || item._key.equals(key))
                {
                    updateLru(item);

                    this._hitCount++;

                    return item._value;
                }

                hash = (hash + 1) & this._mask;
            }

            this._missCount++;
        }

        return null;
    }

    /**
     * Puts a new item in the cache. If the cache is full, remove the LRU item.
     * 
     * @param key key to store data
     * @param value value to be stored
     * @return old value stored under the key
     */
    public V put(K key, V value)
    {
        V oldValue = put(key, value, true);

        if (oldValue instanceof CacheListener)
            ((CacheListener) oldValue).removeEvent();

        return oldValue;
    }

    /**
     * Puts a new item in the cache. If the cache is full, remove the LRU item.
     * 
     * @param key key to store data
     * @param value value to be stored
     * @return the value actually stored
     */
    public V putIfNew(K key, V value)
    {
        V oldValue = put(key, value, false);

        if (oldValue != null)
        {
            return oldValue;
        }
        // otherwise...
        return value;
    }

    /**
     * Puts a new item in the cache. If the cache is full, remove the LRU item.
     * 
     * @param key key to store data
     * @param value value to be stored
     * @param replace whether or not to replace the old value
     * @return old value stored under the key
     */
    private V put(K key, V value, boolean replace)
    {
        Object okey = key;

        if (okey == null)
        {
            okey = NULL;
        }

        // remove LRU items until we're below capacity
        while (this._capacity <= this._size1 + this._size2)
        {
            removeTail();
        }

        int hash = key.hashCode() & this._mask;
        int count = this._size1 + this._size2 + 1;

        V oldValue = null;

        synchronized (this)
        {
            for (; count > 0; count--)
            {
                CacheItem<K, V> item = this._entries[hash];

                // No matching item, so create one
                if (item == null)
                {
                    item = new CacheItem<K, V>(key, value);
                    this._entries[hash] = item;
                    this._size1++;

                    item._next = this._head1;
                    if (this._head1 != null)
                    {
                        this._head1._prev = item;
                    }
                    else
                    {
                        this._tail1 = item;
                    }
                    this._head1 = item;

                    return null;
                }

                // matching item gets replaced
                if (item._key == okey || item._key.equals(okey))
                {
                    updateLru(item);

                    oldValue = item._value;

                    if (replace)
                    {
                        item._value = value;
                    }

                    break;
                }

                hash = (hash + 1) & this._mask;
            }
        }

        if (replace && oldValue instanceof CacheListener)
        {
            ((CacheListener) oldValue).removeEvent();
        }

        return null;
    }

    /**
     * Put item at the head of the used-twice lru list. This is always called
     * while synchronized.
     * 
     * @param item the item to put at the head of the list
     */
    private void updateLru(CacheItem<K, V> item)
    {
        CacheItem<K, V> prev = item._prev;
        CacheItem<K, V> next = item._next;

        if (item._isOnce)
        {
            item._isOnce = false;

            if (prev != null)
            {
                prev._next = next;
            }
            else
            {
                this._head1 = next;
            }

            if (next != null)
            {
                next._prev = prev;
            }
            else
            {
                this._tail1 = prev;
            }

            item._prev = null;
            if (this._head2 != null)
            {
                this._head2._prev = item;
            }
            else
            {
                this._tail2 = item;
            }

            item._next = this._head2;
            this._head2 = item;

            this._size1--;
            this._size2++;
        }
        else
        {
            if (prev == null)
            {
                return;
            }

            prev._next = next;

            item._prev = null;
            item._next = this._head2;

            this._head2._prev = item;
            this._head2 = item;

            if (next != null)
            {
                next._prev = prev;
            }
            else
            {
                this._tail2 = prev;
            }
        }
    }

    /**
     * @return the last item in the LRU
     */
    public boolean removeTail()
    {
        CacheItem<K, V> tail;

        if (this._capacity1 <= this._size1)
        {
            tail = this._tail1;
        }
        else
        {
            tail = this._tail2;
        }

        if (tail == null)
        {
            return false;
        }

        remove(tail._key);

        return true;
    }

    /**
     * Removes an item from the cache
     * 
     * @param key the key to remove
     * @return the value removed
     */
    public V remove(K key)
    {
        Object okey = key;
        if (okey == null)
        {
            okey = NULL;
        }

        int hash = key.hashCode() & this._mask;
        int count = this._size1 + this._size2 + 1;

        V value = null;

        synchronized (this)
        {
            for (; count > 0; count--)
            {
                CacheItem<K, V> item = this._entries[hash];

                if (item == null)
                {
                    return null;
                }

                if (item._key == okey || item._key.equals(okey))
                {
                    this._entries[hash] = null;

                    CacheItem<K, V> prev = item._prev;
                    CacheItem<K, V> next = item._next;

                    if (item._isOnce)
                    {
                        this._size1--;

                        if (prev != null)
                        {
                            prev._next = next;
                        }
                        else
                        {
                            this._head1 = next;
                        }

                        if (next != null)
                        {
                            next._prev = prev;
                        }
                        else
                        {
                            this._tail1 = prev;
                        }
                    }
                    else
                    {
                        this._size2--;

                        if (prev != null)
                        {
                            prev._next = next;
                        }
                        else
                        {
                            this._head2 = next;
                        }

                        if (next != null)
                        {
                            next._prev = prev;
                        }
                        else
                        {
                            this._tail2 = prev;
                        }
                    }

                    value = item._value;

                    // Shift colliding entries down
                    for (int i = 1; i <= count; i++)
                    {
                        int nextHash = (hash + i) & this._mask;
                        CacheItem<K, V> nextItem = this._entries[nextHash];
                        if (nextItem == null)
                        {
                            break;
                        }

                        this._entries[nextHash] = null;
                        refillEntry(nextItem);
                    }
                    break;
                }

                hash = (hash + 1) & this._mask;
            }
        }

        if (count < 0)
        {
            throw new RuntimeException("internal cache error");
        }

        if (value instanceof CacheListener)
        {
            ((CacheListener) value).removeEvent();
        }

        return value;
    }

    /**
     * Put the item in the best location available in the hash table.
     * 
     * @param item the item to put in the best location available
     */
    private void refillEntry(CacheItem<K, V> item)
    {
        int baseHash = item._key.hashCode();

        for (int count = 0; count < this._size1 + this._size2 + 1; count++)
        {
            int hash = (baseHash + count) & this._mask;

            if (this._entries[hash] == null)
            {
                this._entries[hash] = item;
                return;
            }
        }
    }

    /**
     * @return the keys stored in the cache
     */
    public Iterator<K> keys()
    {
        KeyIterator<K, V> iter = new KeyIterator<K, V>(this);
        iter.init(this);
        return iter;
    }

    /**
     * @param oldIter the old iterator to use
     * @return keys stored in the cache using an old iterator
     */
    public Iterator<K> keys(Iterator<K> oldIter)
    {
        KeyIterator<K, V> iter = (KeyIterator<K, V>) oldIter;
        iter.init(this);
        return oldIter;
    }

    /**
     * @return the values in the cache
     */
    public Iterator<V> values()
    {
        ValueIterator<K, V> iter = new ValueIterator<K, V>(this);
        iter.init(this);
        return iter;
    }

    /**
     * @param oldIter the old iterator
     * @return the values of the old iterator
     */
    public Iterator<V> values(Iterator<V> oldIter)
    {
        ValueIterator<K, V> iter = (ValueIterator<K, V>) oldIter;
        iter.init(this);
        return oldIter;
    }

    /**
     * @return the entries
     */
    public Iterator<Entry<K, V>> iterator()
    {
        return new EntryIterator(this);
    }

    /**
     * @return the hit count.
     */
    public long getHitCount()
    {
        return this._hitCount;
    }

    /**
     * @return the miss count.
     */
    public long getMissCount()
    {
        return this._missCount;
    }

    /**
     * A cache item
     * 
     * @param <K_> the key
     * @param <V_> the value
     */
    static class CacheItem<K_, V_>
    {
        LRUCache.CacheItem<K_, V_> _prev;

        LRUCache.CacheItem<K_, V_> _next;

        K_ _key;

        V_ _value;

        int _index;

        boolean _isOnce;

        CacheItem(K_ key, V_ value)
        {
            this._key = key;
            this._value = value;
            this._isOnce = true;
        }
    }

    /**
     * Iterator of cache keys
     * 
     * @param <K_> the key
     * @param <V_> the value
     */
    static class KeyIterator<K_, V_> implements Iterator<K_>
    {
        private LRUCache<K_, V_> _cache;

        private int _i = -1;

        KeyIterator(LRUCache<K_, V_> cache)
        {
            this._cache = cache;
        }

        void init(LRUCache<K_, V_> cache)
        {
            this._cache = cache;
            this._i = -1;
        }

        /**
         * @return the next entry in the cache
         */
        public boolean hasNext()
        {
            CacheItem<K_, V_>[] entries = this._cache._entries;
            int length = entries.length;

            for (this._i++; this._i < length; this._i++)
            {
                if (entries[this._i] != null)
                {
                    this._i--;
                    return true;
                }
            }

            return false;
        }

        /**
         * @return the next value
         */
        public K_ next()
        {
            CacheItem<K_, V_>[] entries = this._cache._entries;
            int length = entries.length;

            for (this._i++; this._i < length; this._i++)
            {
                CacheItem<K_, V_> entry = entries[this._i];

                if (entry != null)
                {
                    return entry._key;
                }
            }

            return null;
        }

        /**
         * @see java.util.Iterator#remove()
         */
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Iterator of cache values
     * 
     * @param <K_> the key
     * @param <V_> the value
     */
    static class ValueIterator<K_, V_> implements Iterator<V_>
    {
        private LRUCache<K_, V_> _cache;

        private int _i = -1;

        ValueIterator(LRUCache<K_, V_> cache)
        {
            init(cache);
        }

        void init(LRUCache<K_, V_> cache)
        {
            this._cache = cache;
            this._i = -1;
        }

        /**
         * @return the next entry in the cache.
         */
        public boolean hasNext()
        {
            CacheItem<K_, V_>[] entries = this._cache._entries;
            int length = entries.length;

            int i = this._i + 1;
            for (; i < length; i++)
            {
                if (entries[i] != null)
                {
                    this._i = i - 1;

                    return true;
                }
            }
            this._i = i;

            return false;
        }

        /**
         * @return the next value
         */
        public V_ next()
        {
            CacheItem<K_, V_>[] entries = this._cache._entries;
            int length = entries.length;

            int i = this._i + 1;
            for (; i < length; i++)
            {
                CacheItem<K_, V_> entry = entries[i];

                if (entry != null)
                {
                    this._i = i;
                    return entry._value;
                }
            }
            this._i = i;

            return null;
        }

        /**
         * @see java.util.Iterator#remove()
         */
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Interface for entry iterator;
     * 
     * @param <K_> the key
     * @param <V_> the value
     */
    public interface Entry<K_, V_>
    {
        /**
         * @return the key
         */
        public K_ getKey();

        /**
         * @return the value
         */
        public V_ getValue();
    }

    /**
     * Iterator of cache values
     */
    class EntryIterator implements Iterator<Entry<K, V>>, Entry<K, V>
    {
        private int _i = -1;

        private LRUCache<K, V> _cache;

        /**
         * Creates a new EntryIterator using the given cache.
         * 
         * @param cache the cache to use
         */
        public EntryIterator(LRUCache<K, V> cache)
        {
            this._cache = cache;
        }

        /**
         * @see java.util.Iterator#hasNext()
         */
        public boolean hasNext()
        {
            int i = this._i + 1;
            CacheItem<K, V>[] entries = this._cache._entries;
            int length = entries.length;

            for (; i < length && entries[i] == null; i++)
            {
                // Do nothing but this loop.
            }

            this._i = i - 1;

            return i < length;
        }

        /**
         * @return the next entry
         * @see java.util.Iterator#next()
         */
        public Entry<K, V> next()
        {
            int i = this._i + 1;
            CacheItem<K, V>[] entries = this._cache._entries;
            int length = entries.length;

            for (; i < length && entries[i] == null; i++)
            {
                // Do nothing but this loop.
            }

            this._i = i;

            if (this._i < length)
            {
                return this;
            }
            // otherwise...
            return null;
        }

        /**
         * @return the key
         */
        public K getKey()
        {
            CacheItem<K, V>[] entries = this._cache._entries;
            if (this._i < this._cache._entries.length)
            {
                CacheItem<K, V> entry = entries[this._i];

                return entry != null ? entry._key : null;
            }

            return null;
        }

        /**
         * @return the value
         */
        public V getValue()
        {
            CacheItem<K, V>[] entries = this._cache._entries;
            if (this._i < this._cache._entries.length)
            {
                CacheItem<K, V> entry = entries[this._i];

                return entry != null ? entry._value : null;
            }

            return null;
        }

        /**
         * @see java.util.Iterator#remove()
         */
        public void remove()
        {
            CacheItem<K, V>[] entries = this._cache._entries;
            if (this._i < this._cache._entries.length)
            {
                CacheItem<K, V> entry = entries[this._i];

                if (entry != null)
                {
                    LRUCache.this.remove(entry._key);
                }
            }
        }
    }
}
