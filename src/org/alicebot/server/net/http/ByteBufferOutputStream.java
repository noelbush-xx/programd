// ===========================================================================
// Copyright (c) 2001 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: ByteBufferOutputStream.java,v 1.1.1.1 2001/06/17 19:01:11 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.http;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.io.OutputStream;

/* ------------------------------------------------------------ */
/** ByteBuffer OutputStream.
 * This stream is similar to the java.io.ByteArrayOutputStream,
 * except that it maintains a reserve of bytes at the start of the
 * buffer and allows efficient prepending of data.
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class ByteBufferOutputStream extends OutputStream
{
    private int _fullAt;
    private byte[] _buf;
    private int _start;
    private int _end;
    private int _reserve;
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     */
    public ByteBufferOutputStream(){this(4096,4000,1024);}
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param capacity Buffer capacity
     */
    public ByteBufferOutputStream(int capacity)
    {
        this(capacity,(capacity*95)/100,capacity/4);
    }
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param capacity Buffer capacity.
     * @param fullAt The size at which isFull returns true.
     * @param reserve The reserve of byte for prepending
     */
    public ByteBufferOutputStream(int capacity,int fullAt,int reserve)
    {
        _buf=new byte[capacity+reserve];
        _reserve=reserve;
        _start=reserve;
        _end=reserve;
        _fullAt=fullAt+_start;
    }

    /* ------------------------------------------------------------ */
    public int size()
    {
        return _end-_start;
    }
    
    /* ------------------------------------------------------------ */
    public int getCapacity()
    {
        return _buf.length-_start;
    }
    
    /* ------------------------------------------------------------ */
    public boolean isFull()
    {
        return _end>=_fullAt;
    }
    
    /* ------------------------------------------------------------ */
    public void writeTo(OutputStream out)
        throws IOException
    {
        out.write(_buf,_start,_end-_start);
    }

    /* ------------------------------------------------------------ */
    public void write(int b)
    {
        ensureCapacity(1);
        _buf[_end++]=(byte)b;
    }
    
    /* ------------------------------------------------------------ */
    public void write(byte[] b)
    {
        ensureCapacity(b.length);
        System.arraycopy(b,0,_buf,_end,b.length);
        _end+=b.length;
    }
    
    /* ------------------------------------------------------------ */
    public void write(byte[] b,int offset, int length)
    {
        ensureCapacity(length);
        System.arraycopy(b,offset,_buf,_end,length);
        _end+=length;
    }
    
    /* ------------------------------------------------------------ */
    /** Write byte to start of the buffer.
     * @param b 
     */
    public void prewrite(int b)
    {
        ensureReserve(1);
        _buf[--_start]=(byte)b;
    }
    
    /* ------------------------------------------------------------ */
    /** Write byte array to start of the buffer.
     * @param b 
     */
    public void prewrite(byte[] b)
    {
        ensureReserve(b.length);
        System.arraycopy(b,0,_buf,_start-b.length,b.length);
        _start-=b.length;
    }
    
    /* ------------------------------------------------------------ */
    /** Write byte range to start of the buffer.
     * @param b 
     * @param offset 
     * @param length 
     */
    public void prewrite(byte[] b,int offset, int length)
    {
        ensureReserve(length);
        System.arraycopy(b,offset,_buf,_start-length,length);
        _start-=length;
    }

    /* ------------------------------------------------------------ */
    public void flush()
    {}

    /* ------------------------------------------------------------ */
    public void reset()
    {
        _end=_reserve;
        _start=_reserve;
    }

    /* ------------------------------------------------------------ */
    public void close()
    {}

    /* ------------------------------------------------------------ */
    public void ensureReserve(int n)
    {
        if (n>_start)
        {
            byte[] buf = new byte[_buf.length+n-_start];
            System.arraycopy(_buf,_start,buf,n,_end-_start);
            _end=n+_end-_start;
            _start=n;
            _buf=buf;
        }
    }
    
    /* ------------------------------------------------------------ */
    public void ensureCapacity(int n)
    {
        if (_end+n>_buf.length)
        {
            byte[] buf = new byte[(_buf.length+n)*4/3];
            System.arraycopy(_buf,_start,buf,_start,_end-_start);
            _buf=buf;
        }
    }
}
    
    
