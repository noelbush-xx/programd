// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: Target.java,v 1.1.1.1 2001/06/17 19:00:24 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.html;


/* -------------------------------------------------------------------- */
/** HTML Link Target.
 * This is a HTML reference (not a CSS Link).
 * @see StyleLink
 */
public class Target extends Block
{

    /* ----------------------------------------------------------------- */
    /** Construct Link.
     * @param target The target name 
     */
    public Target(String target)
    {
        super("A");
        attribute("NAME",target);
    }

    /* ----------------------------------------------------------------- */
    /** Construct Link.
     * @param target The target name 
     * @param link Link Element
     */
    public Target(String target,Object link)
    {
        this(target);
        add(link);
    }
}
