package org.aitools.programd.util.sql;

import org.apache.log4j.Logger;

/**
 * Calls the cleanup method of an {@link ObjectPool} at a period determined by
 * its {@link #sleepTime} .
 * 
 * @author Cristian Mircioiu
 */
class CleanUpThread extends Thread
{
    /** The object pool that this will clean up. */
    private ObjectPool pool;

    /** The period (in milliseconds) to wait before cleaning up. */
    private long sleepTime;

    /**
     * Creates a new <code>CleanUpThread</code> for a given object pool and
     * with a given sleep time.
     * 
     * @param poolToUse the object pool to clean up
     * @param sleepTimeToUse the period (in milliseconds) to wait before
     *            cleaning up
     */
    public CleanUpThread(ObjectPool poolToUse, long sleepTimeToUse)
    {
        super("Database Pool Cleanup Thread");
        this.pool = poolToUse;
        this.sleepTime = sleepTimeToUse;
    }

    /**
     * Cleans up the object pool at the period determined by {@link #sleepTime} .
     */
    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                sleep(this.sleepTime);
            }
            catch (InterruptedException e)
            {
                Logger.getLogger("programd").debug("ObjectPool cleanup thread was interrupted.");
            }
            this.pool.cleanUp();
        }
    }
}