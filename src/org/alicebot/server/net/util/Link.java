// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: Link.java,v 1.1.1.1 2001/06/17 19:00:19 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.html;


/* -------------------------------------------------------------------- */
/** HTML Link Block.
 * This is a HTML reference (not a CSS Link).
 * @see StyleLink
 */
public class Link extends Block
{

    /* ----------------------------------------------------------------- */
    /** Construct Link.
     * @param href The target URL of the link
     */
    public Link(String href)
    {
        super("A");
        attribute("HREF",href);
    }

    /* ----------------------------------------------------------------- */
    /** Construct Link.
     * @param href The target URL of the link
     * @param link Link Element
     */
    public Link(String href,Object link)
    {
        this(href);
        add(link);
    }
    
    /* ----------------------------------------------------------------- */
    /** Set the link target frame.
     */
    public Link target(String t)
    {
        if (t!=null && t.length()>0)
            attribute("TARGET",t);
        return this;
    }    
}




