// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: Tag.java,v 1.1.1.1 2001/06/17 19:00:24 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.html;
import java.io.IOException;
import java.io.Writer;

/* -------------------------------------------------------------------- */
/** HTML Tag Element.
 * A Tag element is of the generic form &lt;TAG attributes... &gt;
 * @see  org.alicebot.server.net.html.Element
 */
public class Tag extends Element
{
    /* ---------------------------------------------------------------- */
    protected String tag;

    /* ---------------------------------------------------------------- */
    public Tag(String tag)
    {
        this.tag=tag;
    }
    
    /* ---------------------------------------------------------------- */
    public void write(Writer out)
         throws IOException
    {
        out.write('<'+tag+attributes()+'>');
    }
}

