package org.alicebot.server.sql.pool;

    
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *  An abstract pool of objects that can be
 *  checked in and locked, checked out and
 *  unlocked.
 *
 *  @author Cristian Mircioiu
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
     *  Initializes the object pool
     *  with a default expiration time
     *  of one hour.
     */
    protected ObjectPool()
    {
        // Default = 1 hour
        expirationTime = ( 1000 * 60 * 60);   

        locked = new Hashtable();          
        unlocked = new Hashtable();
        
        lastCheckOut = System.currentTimeMillis();

        cleaner = new CleanUpThread(this, expirationTime);
        cleaner.setDaemon(true);
        cleaner.start();
    }


    /**
     *  Checks in and unlocks an object.
     *
     *  @param object   the object to check in and unlock.
     */
    protected void checkIn(Object object)
    {
        if(object != null)
        {
            locked.remove(object);
            unlocked.put(object, new Long(System.currentTimeMillis()));
        }
    }


    /**
     *  Checks out and locks the next
     *  available object in the pool.
     *
     *  @return the next available object in the pool
     */
    protected Object checkOut()
    {
        long now = System.currentTimeMillis();        
        lastCheckOut = now;
        Object object;               
        
        if(unlocked.size() > 0)
        {
            Enumeration e = unlocked.keys();  
            
            while(e.hasMoreElements())
            {
                object = e.nextElement();        

                if(validate(object))
                {
                    unlocked.remove(object);
                    locked.put(object, new Long(now));                 
                    return(object);
                }
                else
                {
                    unlocked.remove(object);
                    expire(object);
                    object = null;
                }
            }
        }         
        
        object = create();         
        
        locked.put(object, new Long(now)); 
        return object;
    }


    /**
     *  Cleans up the pool by checking
     *  for expired objects.
     */
    protected void cleanUp()
    {
        Object object;

        long now = System.currentTimeMillis();
        
        Enumeration e = unlocked.keys();  
        
        while(e.hasMoreElements())
        {
            object = e.nextElement();        

            if(( now - ((Long)unlocked.get(object)).longValue()) > expirationTime)
            {
                unlocked.remove(object);
                expire(object);
                object = null;
            }
        }
        System.gc();
    }


    /**
     *  Creates a new object to store in the pool.
     */
    protected abstract Object create();


    /**
     *  Expires an object in the pool.
     */
    protected abstract void expire(Object object);

    
    /**
     *  Validates an object in the pool.
     *
     *  @param object   the object to validate
     */
    protected abstract boolean validate(Object object);
}
