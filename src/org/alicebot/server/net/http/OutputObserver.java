// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: OutputObserver.java,v 1.1.1.1 2001/06/17 19:00:56 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http;

import java.io.IOException;

/* ------------------------------------------------------------ */
/** Observer output events.
 *
 * @see ChunkableOutputStream
 * @version $Id: OutputObserver.java,v 1.1.1.1 2001/06/17 19:00:56 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public interface OutputObserver
{
    public final static int
        __FIRST_WRITE=0,
        __RESET_BUFFER=1,
        __COMMITING=2,
        __COMMITED=3,
        __CLOSING=4,
        __CLOSED=5;
    
    /* ------------------------------------------------------------ */
    /** XXX 
     * @param out 
     * @param action 
     */
    void outputNotify(ChunkableOutputStream out, int action)
        throws IOException;
}
