// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: Comment.java,v 1.1.1.1 2001/06/17 19:00:13 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.html;
import java.io.IOException;
import java.io.Writer;


/* ------------------------------------------------------------ */
/** HTML Comment.
 * @version $Id: Comment.java,v 1.1.1.1 2001/06/17 19:00:13 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public class Comment extends Composite
{
    /* ----------------------------------------------------------------- */
    public void write(Writer out)
         throws IOException
    {
        out.write("<!--\n");
        super.write(out);
        out.write("\n-->");
    }
};
