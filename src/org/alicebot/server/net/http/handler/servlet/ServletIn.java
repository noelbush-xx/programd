// ========================================================================
// Copyright (c) 2000 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: ServletIn.java,v 1.1.1.1 2001/06/17 19:02:37 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http.handler.servlet;

import org.alicebot.server.net.http.ChunkableInputStream;
import java.io.IOException;
import javax.servlet.ServletInputStream;


class ServletIn extends ServletInputStream
{
    ChunkableInputStream _in;

    /* ------------------------------------------------------------ */
    ServletIn(ChunkableInputStream in)
    {
        _in=in;
    }
    
    /* ------------------------------------------------------------ */
    public int read()
        throws IOException
    {
        return _in.read();
    }
    
    /* ------------------------------------------------------------ */
    public int read(byte b[]) throws IOException
    {
        return _in.read(b);
    }
    
    /* ------------------------------------------------------------ */
    public int read(byte b[], int off, int len) throws IOException
    {    
        return _in.read(b,off,len);
    }
    
    /* ------------------------------------------------------------ */
    public long skip(long len) throws IOException
    {
        return _in.skip(len);
    }
    
    /* ------------------------------------------------------------ */
    public int available()
        throws IOException
    {
        return _in.available();
    }
    
    /* ------------------------------------------------------------ */
    public void close()
        throws IOException
    {
        _in.close();
    }
    
    /* ------------------------------------------------------------ */
    public boolean markSupported()
    {
        return _in.markSupported();
    }
    
    /* ------------------------------------------------------------ */
    public void reset()
        throws IOException
    {
        _in.reset();
    }
    
    /* ------------------------------------------------------------ */
    public void mark(int readlimit)
    {
        _in.mark(readlimit);
    }
    
}


