// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: TextArea.java,v 1.1.1.1 2001/06/17 19:00:13 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.html;

/* -------------------------------------------------------------------- */
/** A Text Area within a form.
 * <p> The text in the TEXTAREA is handled by the super class, Text
 * @see org.alicebot.server.net.html.Text
 */
public class TextArea extends Block
{
    /* ----------------------------------------------------------------- */
    /** @param name The name of the TextArea within the form */
    public TextArea(String name)
    {
        super("TEXTAREA");
        attribute("NAME",name);
    }

    /* ----------------------------------------------------------------- */
    /** @param name The name of the TextArea within the form
     * @param s The string in the text area */
    public TextArea(String name, String s)
    {
        this(name);
        add(s);
    }

    /* ----------------------------------------------------------------- */
    public TextArea setSize(int chars,int lines)
    {
        attribute("ROWS",lines);
        attribute("COLS",chars);
        return this;
    }
}

