// ========================================================================
// Copyright (c) 2000 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: ServletOut.java,v 1.1.1.1 2001/06/17 19:02:37 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http.handler.servlet;

import java.io.IOException;
import java.io.OutputStream;
import org.alicebot.server.net.http.util.IO;
import javax.servlet.ServletOutputStream;


class ServletOut extends ServletOutputStream
{
    OutputStream _out;

    /* ------------------------------------------------------------ */
    ServletOut(OutputStream out)
    {
        _out=out;
    }

    /* ------------------------------------------------------------ */
    public void write(int ch)
        throws IOException
    {
        _out.write(ch);
    }
    
    /* ------------------------------------------------------------ */
    public void write(byte[]b)
        throws IOException
    {
        _out.write(b);
    }
    
    /* ------------------------------------------------------------ */
    public void write(byte[]b,int o,int l)
        throws IOException
    {
        _out.write(b,o,l);
    }

    /* ------------------------------------------------------------ */
    public void flush()
        throws IOException
    {
        _out.flush();
    }
    
    /* ------------------------------------------------------------ */
    public void close()
        throws IOException
    {
        _out.close();
    }
    
    /* ------------------------------------------------------------ */
    public void disable()
        throws IOException
    {
        _out=IO.getNullStream();
    }
}
