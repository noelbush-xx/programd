// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: List.java,v 1.1.1.1 2001/06/17 19:00:20 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.html;


/* -------------------------------------------------------------------- */
/** HTML List Block.
 * Each Element added to the List (which is a Composite) is treated
 * as a new List Item.
 * @see  org.alicebot.server.net.html.Block
 */
public class List extends Block
{
    /* ----------------------------------------------------------------- */
    public static final String Unordered="UL";
    public static final String Ordered="OL";
    public static final String Menu="MENU";
    public static final String Directory="DIR";
    
    /* ----------------------------------------------------------------- */
    public List(String type)
    {
        super(type);
    }   
    
    /* ----------------------------------------------------------------- */
    /** 
     * @param o The item
     * @return This List.
     */
    public Composite add(Object o)
    {
        super.add("<LI>");
        super.add(o);
        return this;
    }
    
    /* ----------------------------------------------------------------- */
    /** 
     * @return The new Item composite
     */
    public Composite newItem()
    {
        super.add("<LI>");
        Composite composite=new Composite();
        super.add(composite);
        return composite;
    }

    
}






