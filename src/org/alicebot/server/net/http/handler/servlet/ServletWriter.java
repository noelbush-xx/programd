// ========================================================================
// Copyright (c) 2000 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: ServletWriter.java,v 1.1.1.1 2001/06/17 19:02:41 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http.handler.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;
import java.io.FilterOutputStream;
import org.alicebot.server.net.http.util.IO;
import javax.servlet.ServletOutputStream;


/* ------------------------------------------------------------ */
/** Servlet PrintWriter.
 * This writer can be disabled.
 * It is crying out for optimization.
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
class ServletWriter extends PrintWriter
{
    Filter filter;
    
    /* ------------------------------------------------------------ */
    ServletWriter(OutputStream os, String encoding)
        throws IOException
    {
        super(IO.getNullWriter());
        filter=new Filter(os);
        out=(new OutputStreamWriter(filter,encoding));
        lock=os;
    }

    /* ------------------------------------------------------------ */
    public void disable()
    {
        filter.disable();
    }
    
    /* ------------------------------------------------------------ */
    private static class Filter extends FilterOutputStream
    {
        Filter(OutputStream os)
        {
            super(os);
        }
        void disable()
        {
            this.out=IO.getNullStream();
        }
    }
}
