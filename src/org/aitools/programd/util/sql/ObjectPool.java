package org.aitools.programd.util.sql;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * An abstract pool of objects that can be checked in and locked, checked out
 * and unlocked.
 * 
 * @author Cristian Mircioiu
 */
public abstract class ObjectPool
{
    /** The time after which an object should expire. */
    private long expirationTime;

    /** The last time an object was checked out. */
    private long lastCheckOut;

    /** The objects that are locked by this pool. */
    private Hashtable locked;

    /** The objects that are unlocked in this pool. */
    private Hashtable unlocked;

    /** The thread that manages pool cleanup. */
    private CleanUpThread cleaner;

    /**
     * Initializes the object pool with a default expiration time of one hour.
     */
    protected ObjectPool()
    {
        // Default = 1 hour
        this.expirationTime = (1000 * 60 * 60);

        this.locked = new Hashtable();
        this.unlocked = new Hashtable();

        this.lastCheckOut = System.currentTimeMillis();

        this.cleaner = new CleanUpThread(this, this.expirationTime);
        this.cleaner.setDaemon(true);
        this.cleaner.start();
    }

    /**
     * Checks in and unlocks an object.
     * 
     * @param object
     *            the object to check in and unlock.
     */
    protected void checkIn(Object object)
    {
        if (object != null)
        {
            this.locked.remove(object);
            this.unlocked.put(object, new Long(System.currentTimeMillis()));
        }
    }

    /**
     * Checks out and locks the next available object in the pool.
     * 
     * @return the next available object in the pool
     */
    protected Object checkOut()
    {
        long now = System.currentTimeMillis();
        this.lastCheckOut = now;
        Object object;

        if (this.unlocked.size() > 0)
        {
            Enumeration e = this.unlocked.keys();

            while (e.hasMoreElements())
            {
                object = e.nextElement();

                if (validate(object))
                {
                    this.unlocked.remove(object);
                    this.locked.put(object, new Long(now));
                    return (object);
                }
                // (otherwise...)
                this.unlocked.remove(object);
                expire(object);
                object = null;
            }
        }

        object = create();

        this.locked.put(object, new Long(now));
        return object;
    }

    /**
     * Cleans up the pool by checking for expired objects.
     */
    protected void cleanUp()
    {
        Object object;

        long now = System.currentTimeMillis();

        Enumeration e = this.unlocked.keys();

        while (e.hasMoreElements())
        {
            object = e.nextElement();

            if ((now - ((Long) this.unlocked.get(object)).longValue()) > this.expirationTime)
            {
                this.unlocked.remove(object);
                expire(object);
                object = null;
            }
        }
        System.gc();
    }

    /**
     * Creates a new object to store in the pool.
     */
    protected abstract Object create();

    /**
     * Expires an object in the pool.
     */
    protected abstract void expire(Object object);

    /**
     * Validates an object in the pool.
     * 
     * @param object
     *            the object to validate
     */
    protected abstract boolean validate(Object object);
}