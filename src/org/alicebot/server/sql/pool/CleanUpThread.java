package org.alicebot.server.sql.pool;

    

/**
 *  Calls the cleanup method of an {@link ObjectPool}
 *  at a period determined by its {@link #sleepTime}.
 *
 *  @author Cristian Mircioiu
 */
class CleanUpThread extends Thread
{
    /** The object pool that this will clean up. */
    private ObjectPool pool;
    
    /** The period (in milliseconds) to wait before cleaning up. */
    private long sleepTime;
    

    /**
     *  Creates a new <code>CleanUpThread</code> for a given
     *  object pool and with a given sleep time.
     *
     *  @param pool         the object pool to clean up
     *  @param sleepTime    the period (in milliseconds) to wait before cleaning up
     */
    public CleanUpThread(ObjectPool pool, long sleepTime)
    {
        super("Database Pool Cleanup Thread");
        this.pool = pool;
        this.sleepTime = sleepTime;
    }


    /**
     *  Cleans up the object pool at the period
     *  determined by {@link #sleepTime}.
     */
    public void run()
    {
        while(true)
        {
            try
            {
                sleep(sleepTime);
            }
            catch(InterruptedException e)
            {
                // ignore it
            }          
            pool.cleanUp();
        }
    }
}

