// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: Observed.java,v 1.1.1.1 2001/06/17 19:01:25 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.http;

import java.util.Observable;

/* ======================================================================== */
/** Helpful extension to Observable.
 * NotifyObservers will set a changed first.
 */
public class Observed  extends Observable
{
    public void notifyObservers(Object arg)
    {
        setChanged();
        super.notifyObservers(arg);
    }

    public void notifyObservers()
    {
        setChanged();
        super.notifyObservers(null);
    }
}
