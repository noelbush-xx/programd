// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: Font.java,v 1.1.1.1 2001/06/17 19:00:15 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.html;


/* -------------------------------------------------------------------- */
/** HTML Font Block.
 * Each Element added to the List (which is a Composite) is treated
 * as a new List Item.
 * @see  org.alicebot.server.net.html.Block
 */
public class Font extends Block
{    
    /* ----------------------------------------------------------------- */
    public Font()
    {
        super("FONT");
    }
    
    /* ----------------------------------------------------------------- */
    public Font(int size)
    {
        this();
        size(size);
    }
    
    /* ----------------------------------------------------------------- */
    public Font(int size, boolean relativeSize)
    {
        this();
        size(((relativeSize && size>=0)?"+":"")+size);
    }
    
    /* ----------------------------------------------------------------- */
    public Font(int size,String attributes)
    {
        this();
        size(size);
        this.attribute(attributes);
    }
    
    /* ----------------------------------------------------------------- */
    public Font(String attributes)
    {
        super("FONT",attributes);
    }
    
    /* ----------------------------------------------------------------- */
    public Font face(String face)
    {
        attribute("FACE",face);
        return this;
    }
    
}



