// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: LifeCycle.java,v 1.1.1.1 2001/06/17 19:01:55 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http.util;

/* ------------------------------------------------------------ */
/** A component LifeCycle.
 * Represents the life cycle interface for an abstract
 * software component. Implementations should respect
 * the following state table:<PRE>
 * State: Destroyed (initial state)
 *    start()      -> Started
 *    stop()       -> Stopped
 *    destroy()    -> Destroyed
 *
 * State: Stopped
 *    start()      -> Started
 *    stop()       -> Stopped
 *    destroy()    -> Destroyed
 *
 * State: Started
 *    start()      -> Started
 *    stop()       -> Stopped
 *    destroy()    -> Destroyed
 * </PRE>
 *
 * @version $Id: LifeCycle.java,v 1.1.1.1 2001/06/17 19:01:55 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public interface LifeCycle
{
    /* ------------------------------------------------------------ */
    /** Start the LifeCycle.
     */
    public void start();
    
    /* ------------------------------------------------------------ */
    /** Stop the LifeCycle.
     * The LifeCycle may wait for current activities to complete
     * normally, but it can be interrupted.
     */
    public void stop()
        throws InterruptedException;
    
    /* ------------------------------------------------------------ */
    /** Destroy the LifeCycle.
     * Activities are terminated.
     */
    public void destroy();

    /* ------------------------------------------------------------ */
    /** 
     * @return True if the LifeCycle has been started. 
     */
    public boolean isStarted();
    
    /* ------------------------------------------------------------ */
    /** 
     * @return True if the LifeCycle has been destroyed. 
     */
    public boolean isDestroyed();
    
}
