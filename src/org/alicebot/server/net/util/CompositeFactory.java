// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: CompositeFactory.java,v 1.1.1.1 2001/06/17 19:00:14 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.html;

/* --------------------------------------------------------------------- */
/** Composite Factory.
 * Abstract interface for production of composites
 */
public interface CompositeFactory
{
    public Composite newComposite();
}


